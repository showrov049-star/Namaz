package com.example

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdMobManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdMobTest {

    @Test
    fun `official google test admob ids are configured`() {
        assertEquals("ca-app-pub-3940256099942544~3347511713", AdMobManager.TEST_APP_ID)
        assertEquals("ca-app-pub-3940256099942544/6300978111", AdMobManager.TEST_BANNER_AD_UNIT_ID)
        assertEquals("ca-app-pub-3940256099942544/9214589741", AdMobManager.TEST_ADAPTIVE_BANNER_AD_UNIT_ID)
        assertEquals("ca-app-pub-3940256099942544/1033173712", AdMobManager.TEST_INTERSTITIAL_AD_UNIT_ID)
        assertEquals("ca-app-pub-3940256099942544/5224354917", AdMobManager.TEST_REWARDED_AD_UNIT_ID)
    }

    @Test
    fun `manifest contains official test admob application id`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
        }
        val appId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
        assertNotNull(appId)
        assertEquals(AdMobManager.TEST_APP_ID, appId)

        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
        }
        val perms = pkgInfo.requestedPermissions?.toList().orEmpty()
        assertEquals(false, perms.contains("android.permission.ACCESS_ADSERVICES_ATTRIBUTION"))
        assertEquals(false, perms.contains("android.permission.ACCESS_ADSERVICES_TOPICS"))
    }
}
