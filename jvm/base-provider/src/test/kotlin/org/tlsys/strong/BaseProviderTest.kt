package org.tlsys.strong

import org.junit.Assert
import org.junit.Test
import java.util.*
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

    @Test
    fun dublicateClass() {
        class Bean

        val provider = BaseProvider()

        provider.singleton(Bean::class) { Bean() }
        try {
            provider.singleton(Bean::class) { Bean() }
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            //NOP
        }
    }

    @Test
    fun dublicateClassAndName() {
        class Bean

        val provider = BaseProvider()

        val key1 = UUID.randomUUID().toString()
        val key2 = UUID.randomUUID().toString()
        provider.singleton(Bean::class, name = key1) { Bean() }
        provider.singleton(Bean::class, name = key2) { Bean() }
        try {
            provider.singleton(Bean::class, name = key2) { Bean() }
            Assert.fail()
        } catch (e: IllegalArgumentException) {
            //NOP
        }
    }

    @Test
    fun testInjectByName() {
        val key1 = UUID.randomUUID().toString()
        val key2 = UUID.randomUUID().toString()
        val key3 = UUID.randomUUID().toString()

        class Bean(val value: String)

        val provider = BaseProvider()
        val strong = Strong(provider)

        provider.singleton(Bean::class, name = key1) { Bean(key1) }
        provider.singleton(Bean::class, name = key2) { Bean(key2) }
        provider.singleton(Bean::class) { Bean(key3) }

        val b1 by strong.baseInject(Bean::class, key1)
        val b2 by strong.baseInject(Bean::class, key2)
        val b3 by strong.inject(Bean::class)
        Assert.assertEquals(b1.value, key1)
        Assert.assertEquals(b2.value, key2)
        Assert.assertEquals(b3.value, key3)
    }
}