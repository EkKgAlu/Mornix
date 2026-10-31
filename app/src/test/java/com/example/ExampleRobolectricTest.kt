package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ForexRateItem
import com.example.data.model.FreshnessStatus
import com.example.data.model.GoldSilverRates
import com.example.data.model.UserPreferences
import com.example.data.service.BriefingSynthesizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mornix", appName)
    }

    @Test
    fun `test gold calculation methodology`() {
        // 1 tola = 11.6638 grams
        // If 24K per 10 grams is NPR 240,000, then per gram is 24,000
        val fineGold10g = 240000.0
        val perGram24K = fineGold10g / 10.0
        val perTola24K = perGram24K * 11.6638

        assertEquals(24000.0, perGram24K, 0.01)
        assertEquals(279931.2, perTola24K, 0.1)

        // 22K Tejabi ratio is 22 / 24
        val perTola22K = perTola24K * (22.0 / 24.0)
        assertEquals(256603.6, perTola22K, 0.1)
    }

    @Test
    fun `test briefing synthesis with live verified data`() {
        val synthesizer = BriefingSynthesizer()
        val rates = GoldSilverRates(
            fineGold10g = 240310.0,
            fineGoldTola = 280300.0,
            tejabiGold10g = 220284.0,
            tejabiGoldTola = 256941.0,
            silver10g = 3280.0,
            silverTola = 3825.0,
            publishedDate = "October 30, 2026",
            publishedTime = "10:30 AM",
            yesterdayFineGoldTola = 283500.0,
            yesterdaySilverTola = 3930.0,
            changeGoldTola = -3200.0,
            changeGoldTolaPercent = -1.13,
            changeSilverTola = -105.0,
            changeSilverTolaPercent = -2.67,
            sourceName = "FENEGOSIDA",
            sourceUrl = "https://fenegosida.org",
            freshnessStatus = FreshnessStatus.LIVE
        )

        val forex = listOf(
            ForexRateItem("USD", "U.S. Dollar", "🇺🇸", 1, 152.12, 152.72, "2026-10-30", "2026-10-30 16:52", FreshnessStatus.LIVE),
            ForexRateItem("INR", "Indian Rupee", "🇮🇳", 100, 160.00, 160.15, "2026-10-30", "2026-10-30 16:52", FreshnessStatus.LIVE)
        )

        val briefing = synthesizer.generateBriefing(
            goldSilver = rates,
            forexList = forex,
            weather = null,
            articles = emptyList(),
            alerts = emptyList(),
            prefs = UserPreferences()
        )

        assertNotNull(briefing)
        assertTrue(briefing.goldForexSnippet.contains("280,300"))
        assertTrue(briefing.goldForexSnippet.contains("152.12"))
        assertTrue(briefing.spokenScript.contains("Mornix"))
    }
}
