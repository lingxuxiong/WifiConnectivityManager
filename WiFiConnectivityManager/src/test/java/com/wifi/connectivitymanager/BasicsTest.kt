package com.wifi.connectivitymanager

import org.junit.Test

class BasicsTest {

    interface IntPredicate {
        fun accept(i: Int): Boolean
    }

    @Test
    fun testSamInterface() {
        val isEven = object : IntPredicate {
            override fun accept(i: Int): Boolean {
                return i % 2 == 0
            }
        }
    }
}