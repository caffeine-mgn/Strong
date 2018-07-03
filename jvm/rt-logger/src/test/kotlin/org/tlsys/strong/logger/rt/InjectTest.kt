package org.tlsys.strong.logger.rt

import org.junit.Assert
import org.junit.Test
import org.tlsys.strong.Strong
import java.util.logging.Logger

class InjectTest {

    @Test
    fun testInject() {

        val strong = Strong(RTLogger())

        class Test {
            val LOG1 by strong.LOG
            val LOG2 by strong.inject(Logger::class)
        }

        val test = Test()

        Assert.assertEquals(test.LOG1.name, Test::class.java.name)
        Assert.assertEquals(test.LOG1::class.java, Logger::class.java)


        Assert.assertEquals(test.LOG2::class.java, Logger::class.java)
        Assert.assertEquals(test.LOG2.name, Test::class.java.name)
    }
}