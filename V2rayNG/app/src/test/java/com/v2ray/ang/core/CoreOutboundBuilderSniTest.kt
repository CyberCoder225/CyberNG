package com.v2ray.ang.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Covers the branches of [resolveTlsServerName] that do not call Android framework APIs
 * (Utils.isDomainName uses android.util.Patterns, which is unavailable in local JVM tests).
 */
class CoreOutboundBuilderSniTest {

    @Test
    fun customSniOverridesProfileSni() {
        assertEquals(
            "custom.example.com",
            resolveTlsServerName(
                customSni = "custom.example.com",
                profileSni = "profile.example.com",
                sniExt = null,
                server = null,
            )
        )
    }

    @Test
    fun customSniIsUsedWhenProfileSniIsEmpty() {
        assertEquals(
            "custom.example.com",
            resolveTlsServerName(customSni = "custom.example.com", profileSni = "", sniExt = null, server = null)
        )
    }

    @Test
    fun emptyCustomSniIsIgnored() {
        assertEquals(
            "profile.example.com",
            resolveTlsServerName(customSni = "", profileSni = "profile.example.com", sniExt = null, server = null)
        )
    }

    @Test
    fun profileSniIsUsedWhenNoCustomSniIsSet() {
        assertEquals(
            "profile.example.com",
            resolveTlsServerName(customSni = null, profileSni = "profile.example.com", sniExt = "ext.example.com", server = null)
        )
    }

    @Test
    fun noValuesResolvesToNull() {
        assertNull(resolveTlsServerName(customSni = null, profileSni = null, sniExt = null, server = null))
    }
}
