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
            override fun getBean(): T {
                if (imp == null)
                    imp = f()
                return imp!!
            }
        }
    }

    private val factory = HashMap<KClass<*>, IRecord<*>>()


    fun <T : Any> bind(vararg interfaces: KClass<*>, scope: ScopeType, imp: () -> T) {
        val record = when (scope) {
            ScopeType.SINGLETON -> SingletonRecord(imp)
            ScopeType.STATELESS -> StatelessRecord(imp)
            ScopeType.STATEFUL -> StatefulRecord(imp)
        }

        interfaces.forEach {
            if (factory.containsKey(it)) TODO()
        }
        interfaces.forEach {
            factory[it] = record
        }
    }

    inline fun <reified T : Any> stateless(vararg classes: KClass<*>, noinline imp: () -> T) {
        bind(interfaces = *classes, imp = imp, scope = ScopeType.STATELESS)
    }

    inline fun <reified T : Any> stateful(vararg classes: KClass<*>, noinline imp: () -> T) {
        bind(interfaces = *classes, imp = imp, scope = ScopeType.STATEFUL)
    }

    inline fun <reified T : Any> singleton(vararg classes: KClass<*>, noinline imp: () -> T) {
        bind(interfaces = *classes, imp = imp, scope = ScopeType.SINGLETON)
    }

    override fun <T : Any> getInjector(clazz: KClass<T>, property: KProperty<T>): StrongProvider.Injector<T>? =
            factory[clazz]?.getInject() as StrongProvider.Injector<T>

}