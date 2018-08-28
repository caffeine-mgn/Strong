package org.tlsys.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Базовый провайдер
 *
 * Поддерживает три типа работы бизнес-объекта:
 * Singleton
 * Stateless
 * Stateful
 */
open class BaseProvider : StrongProvider {

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Singleton(val name: String = "")

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Stateless(val name: String = "")

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Stateful(val name: String = "")

    enum class ScopeType {
        STATELESS,
        STATEFUL,
        SINGLETON
    }

    internal interface IRecord<T : Any> {
        fun getInject(): StrongProvider.Injector<T>
    }

    internal class StatelessRecord<T : Any>(val f: () -> T) : IRecord<T> {
        val inj = object : StrongProvider.Injector<T> {
            override fun getBean(): T = f()
        }

        override fun getInject(): StrongProvider.Injector<T> = inj
    }

    internal inner class SingletonRecord<T : Any>(val f: () -> T) : IRecord<T> {
        override fun getInject(): StrongProvider.Injector<T> = inj
        private var imp: T? = null

        val inj = object : StrongProvider.Injector<T> {
            override fun getBean(): T {
                return synchronized(this@BaseProvider) {
                    if (imp == null) {
                        imp = f()
                    }
                    imp!!
                }
            }
        }


    }

    internal class StatefulRecord<T : Any>(val f: () -> T) : IRecord<T> {
        override fun getInject(): StrongProvider.Injector<T> = object : StrongProvider.Injector<T> {
            private var imp: T? = null

            @Synchronized
            override fun getBean(): T {
                if (imp == null)
                    imp = f()
                return imp!!
            }
        }
    }

    private class BeanId(val clazz: KClass<*>, val name: String?)

    private val factory = HashMap<BeanId, IRecord<*>>()

    private fun getScope(clazz: KClass<*>): ScopeType =
            when {
                clazz.java.annotations.any { it::class.java.name == "javax.ejb.Singleton" || it.annotationClass.java === Singleton::class.java } -> ScopeType.SINGLETON
                clazz.java.annotations.any { it::class.java.name == "javax.ejb.Stateless" || it.annotationClass.java === Stateless::class.java } -> ScopeType.STATELESS
                clazz.java.annotations.any { it::class.java.name == "javax.ejb.Stateful" || it.annotationClass.java === Stateful::class.java } -> ScopeType.STATEFUL
                else -> throw RuntimeException("Unknown Scope of class ${clazz.java.name}")
            }

    private fun getName(clazz: KClass<*>): String? {
        val annatation = clazz.java.declaredAnnotations.find {
            val className = it::class.java.name
            className == "javax.ejb.Singleton" ||
                    className == "javax.ejb.Stateless" ||
                    className == "javax.ejb.Stateful" ||
                    it === Singleton::class.java ||
                    it === Stateless::class.java ||
                    it === Stateful::class.java
        } ?: return null

        val name = annatation::class.java.getField("name").get(annatation) as String
        return name.takeIf { it.isNotEmpty() }
    }

    fun <T : Any> bind(interfaces: Array<KClass<*>>, name: String?, impClass: KClass<T>, scope: ScopeType?, imp: () -> T) {
        val record = when (scope ?: getScope(impClass)) {
            ScopeType.SINGLETON -> SingletonRecord(imp)
            ScopeType.STATELESS -> StatelessRecord(imp)
            ScopeType.STATEFUL -> StatefulRecord(imp)
        }

        interfaces.forEach {
            if (factory.any { i -> i.key.clazz == it && i.key.name == name })
                if (name == null)
                    throw IllegalArgumentException("Bean with interface ${it.java.name} already was bindded")
                else
                    throw IllegalArgumentException("Bean with interface ${it.java.name} and name \"$name\" already was bindded")
        }
        interfaces.forEach {
            factory[BeanId(clazz = it, name = name)] = record
        }
    }

    fun clear() {
        factory.clear()
    }

    /**
     * Бинд бина
     *
     * @param classes классы, на которые должен возвращаться результат лямбды [imp]
     * @param scope тип работы бизнес-объекта
     * @param imp функция провайдер фактического объекта для включения
     * @param T тип класса, который фактический будет возвращаться при включении
     */
    inline fun <reified T : Any> bind(vararg classes: KClass<*>, scope: ScopeType? = null, name: String? = null, noinline imp: () -> T) {
        bind(
                interfaces = if (classes.isEmpty())
                    arrayOf(T::class) as Array<KClass<*>>
                else
                    classes as Array<KClass<*>>,
                impClass = T::class,
                imp = imp,
                scope = scope,
                name = name)
    }

    override fun <T : Any> getInjector(clazz: KClass<T>, property: KProperty<T>, thisRef: Any?, params: Array<out Any?>): StrongProvider.Injector<T>? {
        val name = params.find { it is String && it.startsWith("Base") }?.let { it as String; it.removePrefix("Base:") }
        val record = factory.entries.find { it.key.clazz == clazz && it.key.name == name }
        return record?.value?.getInject() as StrongProvider.Injector<T>?
    }
}

inline fun <reified T : Any> BaseProvider.singleton(vararg classes: KClass<*>, name: String? = null, noinline imp: () -> T) {
    bind(interfaces = if (classes.isEmpty())
        arrayOf(T::class) as Array<KClass<*>>
    else
        classes as Array<KClass<*>>,
            scope = BaseProvider.ScopeType.SINGLETON,
            impClass = T::class,
            imp = imp,
            name = name)
}

inline fun <reified T : Any> BaseProvider.stateless(vararg classes: KClass<*>, name: String? = null, noinline imp: () -> T) {
    bind(interfaces = if (classes.isEmpty())
        arrayOf(T::class) as Array<KClass<*>>
    else
        classes as Array<KClass<*>>,
            scope = BaseProvider.ScopeType.STATELESS,
            impClass = T::class,
            imp = imp,
            name = name)
}

inline fun <reified T : Any> BaseProvider.stateful(vararg classes: KClass<*>, name: String? = null, noinline imp: () -> T) {
    bind(interfaces = if (classes.isEmpty())
        arrayOf(T::class) as Array<KClass<*>>
    else
        classes as Array<KClass<*>>,
            scope = BaseProvider.ScopeType.STATEFUL,
            impClass = T::class,
            imp = imp,
            name = name)
}

fun <T : Any> Strong.baseInject(clazz: KClass<T>, name: String) = inject(clazz, "Base:$name")

