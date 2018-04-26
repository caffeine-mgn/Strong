package org.tlsys.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Контекст Strong
 *
 * @param providers спислк провайдеров бинов
 */
class Strong(vararg val providers: StrongProvider) {
    /**
     * Метод подключения бинов
     *
     * @param clazz класс бина, который требуется подключить
     * @return делегатор, который возвращает экземпляр класса [clazz]
     */
    fun <T : Any> inject(clazz: KClass<T>): StrongDelegate<T> = StrongDelegateImp(clazz)

    /**
     * Интерфейс делегатора
     */
    interface StrongDelegate<out T : Any> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    }


    private inner class StrongDelegateImp<out T : Any>(private val clazz: KClass<T>) : StrongDelegate<T> {
        private lateinit var injector: StrongProvider.Injector<T>

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!this::injector.isInitialized) {
                injector = this@Strong.providers
                        .asSequence()
                        .map {
                            it.getInjector(clazz = clazz, property = property as KProperty<T>)
                        }
                        .filterNotNull()
                        .firstOrNull() ?: throw BeanNotFoundException(clazz)
            }

            return injector.getBean()
        }
    }

    /**
     * Происходит, если требуемый класс [clazz] не найден
     *
     * @param clazz класс бина, который не найден
     */
    class BeanNotFoundException(clazz: KClass<*>) : RuntimeException() {
        override val message: String = "Bean $clazz not found"
    }

    /**
     * Интерфейс профиля
     */
    interface Profile {
        fun apply(strong: Strong)
    }

    /**
     * Включает профили [profiles]
     *
     * @param profiles список прифилей, которые необходимо включить
     */
    fun use(vararg profiles: Profile) {
        profiles.forEach {
            it.apply(this)
        }
    }
}

expect val Strong.Profile.name: String