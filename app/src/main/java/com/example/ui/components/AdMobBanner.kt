package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ads.AdMobManager
import com.example.ui.theme.BgDeepForest
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextSecondary
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdMobTestBanner(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var adLoaded by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_test_banner_container"),
        color = CardDarkEmerald.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, BorderEmeraldGlow.copy(alpha = 0.7f))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val widthDp = maxWidth.value.toInt().coerceAtLeast(320)

            if (isPreview) {
                AdMobBannerFallbackBar(label = "AdMob Test Banner Preview")
            } else {
                val adView = remember(widthDp) {
                    runCatching {
                        AdView(context).apply {
                            adUnitId = AdMobManager.TEST_ADAPTIVE_BANNER_AD_UNIT_ID
                            val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                                context,
                                widthDp
                            )
                            setAdSize(adaptiveSize)
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    adLoaded = true
                                    adFailed = false
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    adLoaded = false
                                    adFailed = true
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }.getOrNull()
                }

                if (adView == null) {
                    AdMobBannerFallbackBar(label = "AdMob Test Ad")
                } else {
                    DisposableEffect(lifecycleOwner, adView) {
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> runCatching { adView.resume() }
                                Lifecycle.Event.ON_PAUSE -> runCatching { adView.pause() }
                                Lifecycle.Event.ON_DESTROY -> runCatching { adView.destroy() }
                                else -> Unit
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                            runCatching { adView.destroy() }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!adLoaded) {
                            AdMobBannerFallbackBar(
                                label = if (adFailed) "AdMob Test Banner (Offline)" else "Loading AdMob Test Ad..."
                            )
                        }
                        AndroidView(
                            factory = { adView },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdMobBannerFallbackBar(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(BgDeepForest.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = GoldAccent.copy(alpha = 0.18f),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
        ) {
            Text(
                text = "AD",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
