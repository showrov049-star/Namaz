package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.data.MosqueUiStatus
import com.example.ui.components.AdMobTestBanner
import com.example.ui.screens.ActiveFocusScreen
import com.example.ui.screens.CallMonitorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.NearbyMosqueScreen
import com.example.ui.screens.PrayerNotificationsScreen
import com.example.ui.screens.PrayerTrackerScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranDuaScreen
import com.example.ui.screens.RamadanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.theme.BgDeepForest
import com.example.ui.theme.BgNavBar
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NamazTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NamazViewModel
import com.example.ui.viewmodel.NearbyMosqueViewModel
import com.example.ui.viewmodel.QuranViewModel
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager
import com.example.util.PermissionHelper

private enum class MainTab { HOME, FIND_MOSQUE, QIBLA, MORE }

private enum class SecondaryScreen {
    NONE,
    SETTINGS,
    PRAYER_NOTIFICATIONS,
    TASBIH,
    QURAN_DUA,
    PRAYER_TRACKER,
    RAMADAN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdMobManager.initialize(this)
        setContent {
            NamazTheme {
                val vm: NamazViewModel = viewModel()
                val mosqueVm: NearbyMosqueViewModel = viewModel()
                val quranVm: QuranViewModel = viewModel()
                val uiState by vm.uiState.collectAsStateWithLifecycle()
                val mosqueUiState by mosqueVm.uiState.collectAsStateWithLifecycle()
                val quranUiState by quranVm.uiState.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(MainTab.HOME) }
                var secondaryScreen by remember { mutableStateOf(SecondaryScreen.NONE) }
                var showActiveFocus by remember { mutableStateOf(false) }

                val s = AppStrings.get(uiState.settings.appLanguage)

                CompositionLocalProvider(LocalFeedbackManager provides vm.feedbackManager) {

                val notifLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                val callPermsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { }

                val screeningRoleLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { }

                val locationPermLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    vm.onLocationPermissionsResult(permissions)
                }

                val mosqueLocationPermLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    vm.onLocationPermissionsResult(permissions)
                    mosqueVm.onPermissionResult(permissions)
                }

                LaunchedEffect(uiState.shouldRequestLocationPermission) {
                    if (uiState.shouldRequestLocationPermission) {
                        vm.consumeLocationPermissionRequest()
                        locationPermLauncher.launch(PermissionHelper.requiredLocationPermissions())
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !PermissionHelper.hasNotificationPermission(this@MainActivity)
                    ) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                if (uiState.lastCallEvent.isActive) {
                    CallMonitorScreen(
                        title = s.incomingCall,
                        endCallContentDescription = s.endCall,
                        callEvent = uiState.lastCallEvent,
                        onDismissOrEnd = { vm.stopVoiceAndDismissCall() }
                    )
                } else if (showActiveFocus && uiState.settings.isNamazModeActive) {
                    ActiveFocusScreen(
                        state = uiState,
                        onTurnOff = {
                            vm.toggleNamazMode()
                            showActiveFocus = false
                        },
                        onBack = { showActiveFocus = false }
                    )
                } else {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GoldAccent,
                        selectedTextColor = GoldAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.22f)
                    )

                    val showBanner = when (secondaryScreen) {
                        SecondaryScreen.NONE -> currentTab != MainTab.FIND_MOSQUE
                        SecondaryScreen.TASBIH,
                        SecondaryScreen.QURAN_DUA,
                        SecondaryScreen.PRAYER_TRACKER,
                        SecondaryScreen.RAMADAN -> true
                        SecondaryScreen.SETTINGS,
                        SecondaryScreen.PRAYER_NOTIFICATIONS -> false
                    }

                    Scaffold(
                        containerColor = BgDeepForest,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            Column {
                                if (showBanner) {
                                    AdMobTestBanner()
                                }
                                if (secondaryScreen == SecondaryScreen.NONE) {
                                    HorizontalDivider(thickness = 1.dp, color = BorderEmeraldGlow)
                                    NavigationBar(
                                        containerColor = BgNavBar,
                                        contentColor = TextPrimary
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.HOME,
                                            onClick = {
                                                vm.feedbackManager.play(FeedbackAction.HOME_CLICK)
                                                currentTab = MainTab.HOME
                                            },
                                            modifier = Modifier.testTag("nav_home"),
                                            icon = { Icon(Icons.Rounded.Home, contentDescription = s.home) },
                                            label = {
                                                Text(
                                                    text = s.home,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = navItemColors
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.FIND_MOSQUE,
                                            onClick = {
                                                vm.feedbackManager.play(FeedbackAction.MOSQUE_FINDER)
                                                currentTab = MainTab.FIND_MOSQUE
                                                if (PermissionHelper.hasLocationPermission(this@MainActivity)) {
                                                    if (mosqueUiState.userLat == null ||
                                                        mosqueUiState.status !is MosqueUiStatus.Success
                                                    ) {
                                                        mosqueVm.startDiscovery()
                                                    }
                                                } else {
                                                    mosqueLocationPermLauncher.launch(
                                                        PermissionHelper.requiredLocationPermissions()
                                                    )
                                                }
                                            },
                                            modifier = Modifier.testTag("nav_find_mosque"),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.Mosque,
                                                    contentDescription = s.findMosque
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = s.findMosque,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = navItemColors
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.QIBLA,
                                            onClick = {
                                                vm.feedbackManager.play(FeedbackAction.HOME_CLICK)
                                                currentTab = MainTab.QIBLA
                                            },
                                            modifier = Modifier.testTag("nav_qibla"),
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.Explore,
                                                    contentDescription = s.qiblaTab
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = s.qiblaTab,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = navItemColors
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.MORE,
                                            onClick = {
                                                vm.feedbackManager.play(FeedbackAction.SETTINGS_CLICK)
                                                currentTab = MainTab.MORE
                                            },
                                            modifier = Modifier.testTag("nav_more"),
                                            icon = {
                                                Icon(Icons.Rounded.MoreHoriz, contentDescription = s.more)
                                            },
                                            label = {
                                                Text(
                                                    text = s.more,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = navItemColors
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (secondaryScreen) {
                                SecondaryScreen.SETTINGS -> SettingsScreen(
                                    settings = uiState.settings,
                                    onSave = { updated -> vm.saveSettings(updated) },
                                    onRestoreTouchSoundDefaults = { vm.restoreTouchSoundDefaults() },
                                    onRequestCallPermissions = {
                                        callPermsLauncher.launch(
                                            PermissionHelper.requiredCallPermissions()
                                        )
                                    },
                                    onRequestScreeningRole = {
                                        PermissionHelper.createCallScreeningRoleIntent(
                                            this@MainActivity
                                        )?.let { screeningRoleLauncher.launch(it) }
                                    },
                                    onBack = { secondaryScreen = SecondaryScreen.NONE }
                                )

                                SecondaryScreen.PRAYER_NOTIFICATIONS -> PrayerNotificationsScreen(
                                    config = uiState.prayerNotifications,
                                    appLanguage = uiState.settings.appLanguage,
                                    onUpdateConfig = { cfg -> vm.updatePrayerNotifications(cfg) },
                                    onSendTestAlert = { vm.sendTestPrayerNotification() },
                                    onBack = { secondaryScreen = SecondaryScreen.NONE }
                                )

                                SecondaryScreen.TASBIH -> TasbihScreen(
                                    tasbih = uiState.tasbih,
                                    appLanguage = uiState.settings.appLanguage,
                                    onTapCount = { vm.incrementTasbih() },
                                    onResetRound = { vm.resetTasbihRound() },
                                    onSelectZikr = { z -> vm.selectTasbihZikr(z) },
                                    onSetDailyGoal = { g -> vm.setTasbihDailyGoal(g) },
                                    onBack = { secondaryScreen = SecondaryScreen.NONE }
                                )

                                SecondaryScreen.QURAN_DUA -> QuranDuaScreen(
                                    quranState = quranUiState,
                                    duaBookmarks = uiState.duaBookmarks,
                                    appLanguage = uiState.settings.appLanguage,
                                    onSearchQueryChange = { q -> quranVm.onSearchQueryChanged(q) },
                                    onOpenSurah = { num -> quranVm.openSurah(num) },
                                    onCloseSurah = { quranVm.closeSurah() },
                                    onPlayAyah = { ayah -> quranVm.playOrToggleAyah(ayah) },
                                    onPlaySurahFromStart = { quranVm.playSurahFromStart() },
                                    onStopAudio = { quranVm.stopAudio() },
                                    onSelectDuaCategory = { cat -> quranVm.selectDuaCategory(cat) },
                                    onToggleDuaBookmark = { id -> vm.toggleDuaBookmark(id) },
                                    onBack = { secondaryScreen = SecondaryScreen.NONE }
                                )

                                SecondaryScreen.PRAYER_TRACKER -> PrayerTrackerScreen(
                                    history = uiState.prayerTrackerHistory,
                                    appLanguage = uiState.settings.appLanguage,
                                    onTogglePrayer = { dateKey, prayer ->
                                        vm.togglePrayerTracked(dateKey, prayer)
                                    },
                                    onBack = { secondaryScreen = SecondaryScreen.NONE }
                                )

                                SecondaryScreen.RAMADAN -> RamadanScreen(
                                    state = uiState,
                                    onRequestLocation = { vm.requestFineLocationPermission() },
                                    onBack = { secondaryScreen = SecondaryScreen.NONE }
                                )

                                SecondaryScreen.NONE -> when (currentTab) {
                                    MainTab.HOME -> HomeScreen(
                                        state = uiState,
                                        onToggleMode = { vm.toggleNamazMode() },
                                        onToggleAutoCall = { enabled ->
                                            vm.toggleAutoCall(enabled)
                                            if (enabled &&
                                                !PermissionHelper.isCallScreeningRoleHeld(this@MainActivity)
                                            ) {
                                                PermissionHelper.createCallScreeningRoleIntent(
                                                    this@MainActivity
                                                )?.let { screeningRoleLauncher.launch(it) }
                                            }
                                        },
                                        onSelectDuration = { mins -> vm.selectDurationMinutes(mins) },
                                        onToggleLanguage = { vm.toggleAppLanguage() },
                                        onRequestLocation = {
                                            vm.requestFineLocationPermission()
                                        },
                                        onRefreshLocation = { vm.refreshLocation() },
                                        onOpenFocusView = { showActiveFocus = true },
                                        onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS },
                                        onOpenPrayerNotifications = {
                                            secondaryScreen = SecondaryScreen.PRAYER_NOTIFICATIONS
                                        },
                                        onOpenTasbih = { secondaryScreen = SecondaryScreen.TASBIH },
                                        onOpenQuranDua = { secondaryScreen = SecondaryScreen.QURAN_DUA },
                                        onOpenPrayerTracker = {
                                            secondaryScreen = SecondaryScreen.PRAYER_TRACKER
                                        },
                                        onOpenRamadan = { secondaryScreen = SecondaryScreen.RAMADAN }
                                    )

                                    MainTab.FIND_MOSQUE -> NearbyMosqueScreen(
                                        uiState = mosqueUiState,
                                        appLanguage = uiState.settings.appLanguage,
                                        onRequestLocationPermission = {
                                            mosqueLocationPermLauncher.launch(
                                                PermissionHelper.requiredLocationPermissions()
                                            )
                                        },
                                        onSelectRadius = { km -> mosqueVm.selectRadiusKm(km) },
                                        onSearchQueryChange = { q -> mosqueVm.onSearchQueryChanged(q) },
                                        onSelectMosque = { mosque -> mosqueVm.selectMosque(mosque) },
                                        onToggleListMode = { showList -> mosqueVm.toggleListMode(showList) },
                                        onRefresh = { mosqueVm.refresh() },
                                        onBack = { currentTab = MainTab.HOME }
                                    )

                                    MainTab.QIBLA -> QiblaScreen(
                                        state = uiState,
                                        onRequestLocation = { vm.requestFineLocationPermission() },
                                        onRefreshLocation = { vm.refreshLocation() }
                                    )

                                    MainTab.MORE -> MoreScreen(
                                        settings = uiState.settings,
                                        onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS },
                                        onOpenPrayerNotifications = {
                                            secondaryScreen = SecondaryScreen.PRAYER_NOTIFICATIONS
                                        },
                                        onOpenTasbih = { secondaryScreen = SecondaryScreen.TASBIH },
                                        onOpenQuranDua = { secondaryScreen = SecondaryScreen.QURAN_DUA },
                                        onOpenPrayerTracker = {
                                            secondaryScreen = SecondaryScreen.PRAYER_TRACKER
                                        },
                                        onOpenRamadan = { secondaryScreen = SecondaryScreen.RAMADAN },
                                        onTestVoicePreview = { vm.triggerVoicePreview() },
                                        onRequestCallPermissions = {
                                            callPermsLauncher.launch(
                                                PermissionHelper.requiredCallPermissions()
                                            )
                                        },
                                        onRequestScreeningRole = {
                                            PermissionHelper.createCallScreeningRoleIntent(
                                                this@MainActivity
                                            )?.let { screeningRoleLauncher.launch(it) }
                                        },
                                        onShowTestInterstitial = {
                                            AdMobManager.showInterstitial(
                                                activity = this@MainActivity,
                                                onUnavailable = {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        s.adLoadingLabel,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        },
                                        onShowTestRewarded = {
                                            AdMobManager.showRewarded(
                                                activity = this@MainActivity,
                                                onRewardEarned = { amount, type ->
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Test Reward Earned: $amount $type",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                onUnavailable = {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        s.adLoadingLabel,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        },
                                        onBack = { currentTab = MainTab.HOME }
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
