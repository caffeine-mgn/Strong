package org.tlsys.strong

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrongTest {

    @Test
    fun testInject() {
        class SomeClass {
            fun sum(a: Int, b: Int) = a + b
        }

        class Injector<out T : Any> : StrongProvider.Injector<T> {
            override fun getBean(): T = SomeClass() as T

        }

        class Provider : StrongProvider {
            override fun <T : Any> getInjector(clazz: KClass<T>, property: KProperty<T>): StrongProvider.Injector<T>? =
                    Injector()

        }

        val provider = Provider()
        val strong = Strong(provider)

        val bean by strong.inject(SomeClass::class)
        assertEquals(4, bean.sum(2, 2))
    }
}