package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

enum class AdLoadStatus {
    IDLE,
    LOADING,
    READY,
    FAILED
}

object AdMobManager {
    // Official Google AdMob Test IDs (safe for development & testing)
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private val initialized = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val _isSdkReady = MutableStateFlow(false)
    val isSdkReady: StateFlow<Boolean> = _isSdkReady.asStateFlow()

    private val _interstitialStatus = MutableStateFlow(AdLoadStatus.IDLE)
    val interstitialStatus: StateFlow<AdLoadStatus> = _interstitialStatus.asStateFlow()

    private val _rewardedStatus = MutableStateFlow(AdLoadStatus.IDLE)
    val rewardedStatus: StateFlow<AdLoadStatus> = _rewardedStatus.asStateFlow()

    fun initialize(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        scope.launch {
            runCatching {
                MobileAds.setRequestConfiguration(
                    RequestConfiguration.Builder()
                        .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                        .build()
                )
                MobileAds.initialize(appContext) {
                    _isSdkReady.value = true
                    CoroutineScope(Dispatchers.Main).launch {
                        loadInterstitial(appContext)
                        loadRewarded(appContext)
                    }
                }
            }
        }
    }

    fun loadInterstitial(context: Context) {
        if (_interstitialStatus.value == AdLoadStatus.LOADING || interstitialAd != null) return
        val appContext = context.applicationContext
        _interstitialStatus.value = AdLoadStatus.LOADING
        runCatching {
            InterstitialAd.load(
                appContext,
                TEST_INTERSTITIAL_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        _interstitialStatus.value = AdLoadStatus.READY
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        _interstitialStatus.value = AdLoadStatus.FAILED
                    }
                }
            )
        }.onFailure {
            interstitialAd = null
            _interstitialStatus.value = AdLoadStatus.FAILED
        }
    }

    fun showInterstitial(
        activity: Activity,
        onCompleted: () -> Unit = {},
        onUnavailable: () -> Unit = {}
    ) {
        val ad = interstitialAd
        if (ad == null) {
            loadInterstitial(activity)
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                _interstitialStatus.value = AdLoadStatus.IDLE
                loadInterstitial(activity)
                onCompleted()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                _interstitialStatus.value = AdLoadStatus.FAILED
                loadInterstitial(activity)
                onUnavailable()
            }
        }
        ad.show(activity)
    }

    fun loadRewarded(context: Context) {
        if (_rewardedStatus.value == AdLoadStatus.LOADING || rewardedAd != null) return
        val appContext = context.applicationContext
        _rewardedStatus.value = AdLoadStatus.LOADING
        runCatching {
            RewardedAd.load(
                appContext,
                TEST_REWARDED_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        _rewardedStatus.value = AdLoadStatus.READY
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        _rewardedStatus.value = AdLoadStatus.FAILED
                    }
                }
            )
        }.onFailure {
            rewardedAd = null
            _rewardedStatus.value = AdLoadStatus.FAILED
        }
    }

    fun showRewarded(
        activity: Activity,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onUnavailable: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewarded(activity)
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _rewardedStatus.value = AdLoadStatus.IDLE
                loadRewarded(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _rewardedStatus.value = AdLoadStatus.FAILED
                loadRewarded(activity)
                onUnavailable()
            }
        }
        ad.show(activity) { rewardItem ->
            onRewardEarned(rewardItem.amount, rewardItem.type)
        }
    }
}
