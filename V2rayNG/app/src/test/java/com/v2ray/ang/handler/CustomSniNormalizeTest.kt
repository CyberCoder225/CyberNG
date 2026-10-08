package com.v2ray.ang.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CustomSniNormalizeTest {

    @Test
    fun blankInputClearsTheSetting() {
        assertEquals("", SettingsManager.normalizeCustomSni(""))
        assertEquals("", SettingsManager.normalizeCustomSni("   "))
    }

    @Test
    fun validInputIsTrimmed() {
        assertEquals("example.com", SettingsManager.normalizeCustomSni("  example.com  "))
    }

    @Test
    fun invalidInputIsRejected() {
        assertNull(SettingsManager.normalizeCustomSni("https://example.com"))
        assertNull(SettingsManager.normalizeCustomSni("not a host"))
    }
}
