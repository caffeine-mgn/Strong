package org.tlsys.strong.logger.rt

import org.tlsys.strong.Strong
import org.tlsys.strong.StrongProvider
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Провайдер логирования [java.util.logging.Logger]
 *
 * Использование:
 * ```
 * val strong = Strong(RTLogger())
 *
 * class Foo {
 *      private val LOG by strong.LOG
 * }
 * ```
 * или
 * ```
 * val strong = Strong(RTLogger())
 *
 * class Foo {
 *      private val LOG by strong.inject(java.util.logging.Logger::class)
 * }
 * ```
 */
class RTLogger : StrongProvider {
    override fun <T : Any> getInjector(clazz: KClass<T>, property: KProperty<T>, thisRef: Any?, params: Array<out Any?>): StrongProvider.Injector<T>? {
        if (clazz.java !== Logger::class.java)
            return null
        thisRef ?: throw IllegalArgumentException("Was not setted reference to class for inject")
        return when (clazz) {
            Logger::class -> LogInjector(thisRef::class.java) as StrongProvider.Injector<T>
            else -> null
        }
    }

    private class LogInjector(selfClass: Class<*>) : StrongProvider.Injector<Logger> {
        private val logger = Logger.getLogger(selfClass.name)
        override fun getBean(): Logger = logger

    }
}

inline val Strong.LOG: Strong.StrongDelegate<Logger>
    get() = inject(Logger::class)