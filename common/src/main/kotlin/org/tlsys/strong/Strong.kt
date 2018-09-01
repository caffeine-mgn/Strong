package org.tlsys.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Контекст Strong
 *
 * @param providers спислк провайдеров бинов
 */
class Strong(vararg providers: StrongProvider) {
    private val _providers = ArrayList(providers.toList())

    fun clearProviders() {
        _providers.forEach {
            it.shutdown()
        }
        _providers.clear()
    }

    fun addProvider(provider: StrongProvider) {
        _providers += provider
    }

    fun removeProvider(provider: StrongProvider) = _providers.remove(provider)

    val providers: List<StrongProvider>
        get() = _providers

    /**
     * Метод подключения бинов
     *
     * @param clazz класс бина, который требуется подключить
     * @return делегатор, который возвращает экземпляр класса [clazz]
     */
    fun <T : Any> inject(clazz: KClass<T>, vararg params: Any?): StrongDelegate<T> = StrongDelegateImp(clazz = clazz, params = params)

    fun <T : StrongProvider> provider(clazz: KClass<T>): T? = providers.find { clazz === it::class } as T?

    /**
     * Интерфейс делегатора
     */
    interface StrongDelegate<out T : Any> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    }


    private inner class StrongDelegateImp<out T : Any>(private val clazz: KClass<T>, val params: Array<out Any?>) : StrongDelegate<T> {
        private lateinit var injector: StrongProvider.Injector<T>

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!this::injector.isInitialized) {
                injector = this@Strong.providers
                        .asSequence()
                        .map {
                            it.getInjector(clazz = clazz, property = property as KProperty<T>, thisRef = thisRef, params = params)
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
        fun shutdown() {

        }
    }

    private val _profiles = ArrayList<Strong.Profile>()
    val profiles: List<StrongProvider>
        get() = profiles

    /**
     * Включает профили [profiles]
     *
     * @param profiles список прифилей, которые необходимо включить
     */
    fun use(vararg profiles: Profile) {
        profiles.forEach {
            it.apply(this)
            _profiles += it
        }
    }

    fun clearProfiles() {
        _providers.forEach {
            it.shutdown()
        }
        _providers.clear()
    }
}

expect val Strong.Profile.name: String