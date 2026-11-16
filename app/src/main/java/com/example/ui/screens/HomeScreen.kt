package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Brightness5
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.PrayerName
import com.example.data.PrayerTimeEntry
import com.example.ui.components.GlowingMosqueArchFrame
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BgDeepForest
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.CardElevated
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldInactiveBottom
import com.example.ui.theme.EmeraldInactiveRing
import com.example.ui.theme.EmeraldInactiveTop
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NamazUiState
import com.example.ui.viewmodel.formatDurationHms
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager

@Composable
fun HomeScreen(
    state: NamazUiState,
    onToggleMode: () -> Unit,
    onToggleAutoCall: (Boolean) -> Unit,
    onSelectDuration: (Int) -> Unit,
    onToggleLanguage: () -> Unit,
    onRequestLocation: () -> Unit,
    onRefreshLocation: () -> Unit,
    onOpenFocusView: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrayerNotifications: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenQuranDua: () -> Unit,
    onOpenPrayerTracker: () -> Unit,
    onOpenRamadan: () -> Unit
) {
    val s = AppStrings.get(state.settings.appLanguage)
    val feedback = LocalFeedbackManager.current
    val isActive = state.settings.isNamazModeActive
    val durations = listOf(5, 10, 15, 20, 30)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseVal"
    )

    val ringColor by animateColorAsState(
        targetValue = if (isActive) EmeraldPrimary else EmeraldInactiveRing,
        animationSpec = tween(400),
        label = "ring"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(showMoon = true)

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        feedback?.play(FeedbackAction.SETTINGS_CLICK)
                        onOpenSettings()
                    },
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = s.settings,
                        tint = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardDarkEmerald.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .testTag("language_toggle_pill")
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            onToggleLanguage()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Translate,
                            contentDescription = "Switch Language",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (state.settings.appLanguage == AppLanguage.ENGLISH) {
                                "BN / EN"
                            } else {
                                "EN / বাং"
                            },
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = s.appTitle,
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = s.tagline,
                color = GoldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                GlowingMosqueArchFrame(
                    isActive = isActive,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    modifier = Modifier
                        .size(114.dp)
                        .scale(pulse)
                        .testTag("toggle_namaz_button")
                        .clip(CircleShape)
                        .clickable {
                            feedback?.play(
                                if (isActive) FeedbackAction.NAMAZ_OFF else FeedbackAction.NAMAZ_ON
                            )
                            onToggleMode()
                        },
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(3.dp, ringColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = if (isActive) {
                                        listOf(EmeraldGlow, EmeraldPrimary, EmeraldDeep)
                                    } else {
                                        listOf(EmeraldInactiveTop, EmeraldInactiveBottom)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = s.namazMode,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Text(
                text = if (isActive) s.modeActiveHeadline else s.modeInactiveHeadline,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("namaz_status_headline")
            )
            Text(
                text = if (isActive) s.on else s.off,
                color = if (isActive) EmeraldGlow else TextSecondary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                durations.forEach { mins ->
                    val selected = state.settings.durationMinutes == mins
                    FilterChip(
                        selected = selected,
                        onClick = {
                            feedback?.play(FeedbackAction.HOME_CLICK)
                            onSelectDuration(mins)
                        },
                        modifier = Modifier.testTag("duration_chip_$mins"),
                        label = {
                            Text(
                                text = "$mins${s.minsShort}",
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardDarkEmerald.copy(alpha = 0.85f),
                            labelColor = TextSecondary,
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.22f),
                            selectedLabelColor = GoldAccent
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = BorderEmeraldGlow,
                            selectedBorderColor = GoldAccent
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Auto Call Response Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneInTalk,
                            contentDescription = null,
                            tint = EmeraldGlow
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.autoCallResponse,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (state.settings.autoCallResponseEnabled) s.active else s.inactive,
                            color = if (state.settings.autoCallResponseEnabled) EmeraldGlow else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Switch(
                        checked = state.settings.autoCallResponseEnabled,
                        onCheckedChange = { checked ->
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            onToggleAutoCall(checked)
                        },
                        modifier = Modifier.testTag("auto_call_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BgDeepForest
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Remaining Time Gold Digital Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remaining_time_card")
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { if (isActive) onOpenFocusView() else onOpenSettings() },
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.remainingTime,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = formatDurationHms(state.remainingMillis),
                            color = GoldAccent,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInFull,
                            contentDescription = s.openFocusView,
                            tint = GoldSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Daily Prayer Times Card (Location-Aware + Live Countdown)
            DailyPrayerTimesCard(
                state = state,
                onRequestLocation = onRequestLocation,
                onRefreshLocation = onRefreshLocation,
                onOpenPrayerNotifications = onOpenPrayerNotifications
            )

            Spacer(Modifier.height(14.dp))

            // Islamic Lifestyle Quick Access Grid
            IslamicLifestyleGrid(
                language = state.settings.appLanguage,
                onOpenTasbih = onOpenTasbih,
                onOpenQuranDua = onOpenQuranDua,
                onOpenPrayerTracker = onOpenPrayerTracker,
                onOpenRamadan = onOpenRamadan
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IslamicLifestyleGrid(
    language: AppLanguage,
    onOpenTasbih: () -> Unit,
    onOpenQuranDua: () -> Unit,
    onOpenPrayerTracker: () -> Unit,
    onOpenRamadan: () -> Unit
) {
    val s = AppStrings.get(language)
    val feedback = LocalFeedbackManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = s.quickLifestyleTools,
            color = GoldAccent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LifestyleTileCard(
                icon = Icons.Rounded.TouchApp,
                title = s.tasbihTitle,
                subtitle = s.tasbihSub,
                accent = GoldAccent,
                testTag = "home_tile_tasbih",
                modifier = Modifier.weight(1f),
                onClick = {
                    feedback?.play(FeedbackAction.HOME_CLICK)
                    onOpenTasbih()
                }
            )
            LifestyleTileCard(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                title = s.quranDuaTitle,
                subtitle = s.quranDuaSub,
                accent = EmeraldGlow,
                testTag = "home_tile_quran",
                modifier = Modifier.weight(1f),
                onClick = {
                    feedback?.play(FeedbackAction.HOME_CLICK)
                    onOpenQuranDua()
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LifestyleTileCard(
                icon = Icons.Rounded.CheckCircle,
                title = s.prayerTrackerTitle,
                subtitle = s.prayerTrackerSub,
                accent = EmeraldGlow,
                testTag = "home_tile_tracker",
                modifier = Modifier.weight(1f),
                onClick = {
                    feedback?.play(FeedbackAction.HOME_CLICK)
                    onOpenPrayerTracker()
                }
            )
            LifestyleTileCard(
                icon = Icons.Rounded.NightsStay,
                title = s.ramadanModeTitle,
                subtitle = s.ramadanModeSub,
                accent = GoldAccent,
                testTag = "home_tile_ramadan",
                modifier = Modifier.weight(1f),
                onClick = {
                    feedback?.play(FeedbackAction.HOME_CLICK)
                    onOpenRamadan()
                }
            )
        }
    }
}

@Composable
private fun LifestyleTileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, BorderEmeraldGlow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 15.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyPrayerTimesCard(
    state: NamazUiState,
    onRequestLocation: () -> Unit,
    onRefreshLocation: () -> Unit,
    onOpenPrayerNotifications: () -> Unit
) {
    val s = AppStrings.get(state.settings.appLanguage)
    val feedback = LocalFeedbackManager.current
    val prayerEntries = state.dailyPrayerTimes.entries

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_times_card"),
        shape = RoundedCornerShape(18.dp),
        color = CardDarkEmerald.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, BorderEmeraldGlow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mosque,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = s.todaysPrayerTimes,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        if (state.dailyPrayerTimes.locationLabel.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                val locationText = if (state.isDetectingLocation) {
                                    s.detectingLocation
                                } else {
                                    val coords = String.format(
                                        java.util.Locale.US,
                                        "%.2f°, %.2f°",
                                        state.settings.latitude,
                                        state.settings.longitude
                                    )
                                    if (state.dailyPrayerTimes.locationLabel.contains("°")) {
                                        state.dailyPrayerTimes.locationLabel
                                    } else {
                                        "${state.dailyPrayerTimes.locationLabel} ($coords)"
                                    }
                                }
                                Text(
                                    text = locationText,
                                    color = EmeraldGlow,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            feedback?.play(FeedbackAction.SETTINGS_CLICK)
                            onOpenPrayerNotifications()
                        },
                        modifier = Modifier.testTag("home_prayer_notifications_button")
                    ) {
                        Icon(
                            imageVector = if (state.prayerNotifications.enabled) {
                                Icons.Rounded.NotificationsActive
                            } else {
                                Icons.Rounded.NotificationsOff
                            },
                            contentDescription = s.prayerAlertsTitle,
                            tint = if (state.prayerNotifications.enabled) GoldAccent else TextSecondary
                        )
                    }
                    if (state.hasLocationPermission) {
                        IconButton(
                            onClick = onRefreshLocation,
                            enabled = !state.isDetectingLocation,
                            modifier = Modifier.testTag("refresh_location_button")
                        ) {
                            if (state.isDetectingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = GoldAccent
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = s.refreshLocationDesc,
                                    tint = GoldAccent
                                )
                            }
                        }
                    }
                }
            }

            if (state.dailyPrayerTimes.nextPrayer != null && state.nextPrayerCountdownMillis > 0L) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("next_prayer_countdown_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = CardElevated,
                    border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.prayerLabel(state.dailyPrayerTimes.nextPrayer),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = formatDurationHms(state.nextPrayerCountdownMillis),
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                !state.hasLocationPermission -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = BgDeepForest.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = s.locationPromptDesc,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onRequestLocation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("grant_location_access_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(s.detectLocation, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (prayerEntries.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            prayerEntries.forEach { entry ->
                                PrayerTimeRow(
                                    entry = entry,
                                    label = s.prayerLabel(entry.prayer),
                                    nextBadgeLabel = s.nextBadge
                                )
                            }
                        }
                    }
                }

                state.isDetectingLocation && prayerEntries.isEmpty() -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = GoldAccent
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(s.detectingLocation, color = TextSecondary, fontSize = 13.sp)
                    }
                }

                state.locationError && prayerEntries.isEmpty() -> {
                    Text(
                        text = s.locationUnavailableError,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onRefreshLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("retry_location_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.retryLocation, fontWeight = FontWeight.SemiBold)
                    }
                }

                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prayerEntries.forEach { entry ->
                            PrayerTimeRow(
                                entry = entry,
                                label = s.prayerLabel(entry.prayer),
                                nextBadgeLabel = s.nextBadge
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    val methodLabel = if (state.settings.appLanguage == AppLanguage.BENGALI) {
                        "${state.settings.calculationMethod.titleBn} • ${state.settings.asrSchool.titleBn}"
                    } else {
                        "${state.settings.calculationMethod.titleEn} • ${state.settings.asrSchool.titleEn}"
                    }
                    Text(
                        text = methodLabel,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeRow(
    entry: PrayerTimeEntry,
    label: String,
    nextBadgeLabel: String
) {
    val icon: ImageVector = when (entry.prayer) {
        PrayerName.FAJR -> Icons.Rounded.WbTwilight
        PrayerName.DHUHR -> Icons.Rounded.WbSunny
        PrayerName.ASR -> Icons.Rounded.Brightness5
        PrayerName.MAGHRIB -> Icons.Rounded.NightsStay
        PrayerName.ISHA -> Icons.Rounded.Bedtime
    }

    val containerColor = if (entry.isNext) {
        CardElevated
    } else {
        BgDeepForest.copy(alpha = 0.55f)
    }

    val border = if (entry.isNext) {
        BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f))
    } else {
        BorderStroke(1.dp, BorderEmeraldGlow.copy(alpha = 0.45f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${entry.prayer.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (entry.isNext) GoldAccent else EmeraldGlow,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                color = if (entry.isNext) GoldAccent else TextPrimary,
                fontWeight = if (entry.isNext) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (entry.isNext) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldPrimary.copy(alpha = 0.22f),
                    border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = nextBadgeLabel,
                        color = EmeraldGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = entry.formattedTime,
                color = if (entry.isNext) GoldAccent else TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
