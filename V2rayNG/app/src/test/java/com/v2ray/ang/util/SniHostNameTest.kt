package com.v2ray.ang.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SniHostNameTest {

    @Test
    fun acceptsPlainDnsHostNames() {
        assertTrue(Utils.isValidSniHostName("example.com"))
        assertTrue(Utils.isValidSniHostName("sub.example.co"))
        assertTrue(Utils.isValidSniHostName("a-b.example.org"))
    }

    @Test
    fun rejectsUrlsPathsPortsAndIpLiterals() {
        assertFalse(Utils.isValidSniHostName("https://example.com"))
        assertFalse(Utils.isValidSniHostName("example.com/path"))
        assertFalse(Utils.isValidSniHostName("example.com:443"))
        assertFalse(Utils.isValidSniHostName("1.2.3.4"))
    }

    @Test
    fun rejectsEmptyAndMalformedNames() {
        assertFalse(Utils.isValidSniHostName(""))
        assertFalse(Utils.isValidSniHostName("localhost"))
        assertFalse(Utils.isValidSniHostName("-bad.example.com"))
        assertFalse(Utils.isValidSniHostName("under_score.example.com"))
        assertFalse(Utils.isValidSniHostName("bad host.example.com"))
    }
}
