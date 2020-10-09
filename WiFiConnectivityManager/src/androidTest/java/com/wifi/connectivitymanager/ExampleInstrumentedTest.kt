package com.wifi.connectivitymanager

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.wifi.connectivitymanager.test", appContext.packageName)
    }

    @Test
    fun testStringReplaceWithRegex() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val wcm = WifiConnectivityManagerAndroid10Imp(context)
        val original = "Hello"
        val quotedString = "\"$original\""
        val replaced = wcm.unquote(quotedString)
        assertEquals(original, replaced)
    }
}