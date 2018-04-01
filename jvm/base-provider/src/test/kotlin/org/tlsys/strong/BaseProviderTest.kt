package org.tlsys.strong

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaseProviderTest {

    @Test
    fun testStateful() {
        assertTrue(true)

        val provider = BaseProvider()

        var createCount = 0

        @BaseProvider.Stateful
        class TestBean1 {
            var field = 10

            init {
                createCount++
            }
        }

        val strong = Strong(provider)
        provider.bind(TestBean1::class) { TestBean1() }

        val bean1 by strong.inject(TestBean1::class)
        val bean2 by strong.inject(TestBean1::class)
        bean1.field = 20
        bean2.field = 30
        assertEquals(20, bean1.field)
        assertEquals(30, bean2.field)
        assertEquals(2, createCount)
    }

    @Test
    fun testStateless() {
        assertTrue(true)

        val provider = BaseProvider()

        var createCount = 0

        @BaseProvider.Stateless
        class TestBean1 {
            var field = 10

            init {
                createCount++
            }
        }


        val strong = Strong(provider)
        provider.bind(TestBean1::class) { TestBean1() }

        val bean1 by strong.inject(TestBean1::class)
        val bean2 by strong.inject(TestBean1::class)
        bean1.field = 20
        bean2.field = 30
        assertEquals(10, bean1.field)
        assertEquals(10, bean2.field)
        assertEquals(4, createCount)
    }

    @Test
    fun testSingleton() {
        assertTrue(true)

        val provider = BaseProvider()

        var createCount = 0

        @BaseProvider.Singleton
        class TestBean1 {
            var field = 10

            init {
                createCount++
            }
        }


        val strong = Strong(provider)
        provider.bind(TestBean1::class) { TestBean1() }

        val bean1 by strong.inject(TestBean1::class)
        val bean2 by strong.inject(TestBean1::class)
        bean1.field = 20
        assertEquals(20, bean2.field)
        assertEquals(1, createCount)

    }
}