package org.tlsys.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Провайдер бинов для Strong
 */
interface StrongProvider {

    /**
     * Класс провайдера бинов для конкретного подключения
     * @param T класс, который должен наследовать подключаемый бин
     */
    interface Injector<out T : Any> {
        fun getBean(): T
    }

    /**
     * Возвращает класс провайдера для данного включения
     *
     * @param clazz класс бина, который требуется подключать
     * @param property информация о поле, которое подключает бин
     * @param thisRef указатель на класс, который который устанавливает зависимость от класса [clazz]
     * @return поставщик бинов для конкретного подключения бинов
     */
    fun <T : Any> getInjector(clazz: KClass<T>, property: KProperty<T>, thisRef: Any?, params: Array<out Any?>): Injector<T>?
}