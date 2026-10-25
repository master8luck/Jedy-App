package com.kit.jedyapp.sdk.admob

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialExpiryTest {
    @Test fun `cached ad remains usable before one hour`() {
        assertFalse(isInterstitialExpired(loadedAt = 10_000L, now = 3_609_999L))
    }

    @Test fun `cached ad expires at one hour and remains expired afterwards`() {
        assertTrue(isInterstitialExpired(loadedAt = 10_000L, now = 3_610_000L))
        assertTrue(isInterstitialExpired(loadedAt = 10_000L, now = 7_210_000L))
    }

    @Test fun `replacement ad has a fresh lifetime`() {
        assertFalse(isInterstitialExpired(loadedAt = 3_610_000L, now = 3_610_000L))
    }
}