package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.PrayerName
import com.example.data.PrayerNotificationConfig
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager
import com.example.util.PermissionHelper

@Composable
fun PrayerNotificationsScreen(
    config: PrayerNotificationConfig,
    appLanguage: AppLanguage,
    onUpdateConfig: (PrayerNotificationConfig) -> Unit,
    onSendTestAlert: () -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }

    val context = LocalContext.current
    val s = AppStrings.get(appLanguage)
    val offsets = listOf(0, 5, 10, 15)

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onUpdateConfig(config.copy(enabled = true))
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(showMoon = false, showMosqueArt = false)

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        feedback?.play(FeedbackAction.BACK_TAP)
                        onBack()
                    },
                    modifier = Modifier.testTag("prayer_notif_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(s.prayerAlertsTitle, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(s.prayerAlertsSub, color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Master Switch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = GoldAccent)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.enablePrayerAlerts, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(s.prayerAlertsSub, color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = { wantEnable ->
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            if (wantEnable &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !PermissionHelper.hasNotificationPermission(context)
                            ) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onUpdateConfig(config.copy(enabled = wantEnable))
                            }
                        },
                        modifier = Modifier.testTag("master_prayer_alerts_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Per-Prayer Toggles
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    PrayerName.entries.forEach { prayer ->
                        val checked = config.isPrayerEnabled(prayer)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = s.prayerLabel(prayer),
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = checked,
                                enabled = config.enabled,
                                onCheckedChange = { isChecked ->
                                    feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                                    val updated = when (prayer) {
                                        PrayerName.FAJR -> config.copy(fajrEnabled = isChecked)
                                        PrayerName.DHUHR -> config.copy(dhuhrEnabled = isChecked)
                                        PrayerName.ASR -> config.copy(asrEnabled = isChecked)
                                        PrayerName.MAGHRIB -> config.copy(maghribEnabled = isChecked)
                                        PrayerName.ISHA -> config.copy(ishaEnabled = isChecked)
                                    }
                                    onUpdateConfig(updated)
                                },
                                modifier = Modifier.testTag("notif_switch_${prayer.name.lowercase()}"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Offset & Sound Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.reminderTiming, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        offsets.forEach { mins ->
                            val selected = config.minutesBefore == mins
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    feedback?.play(FeedbackAction.HOME_CLICK)
                                    onUpdateConfig(config.copy(minutesBefore = mins))
                                },
                                modifier = Modifier.weight(1f),
                                label = {
                                    Text(
                                        text = if (mins == 0) s.atPrayerTime else "${mins}m",
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = CardDarkEmerald,
                                    labelColor = TextSecondary,
                                    selectedContainerColor = EmeraldPrimary.copy(alpha = 0.24f),
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = GoldAccent)
                        Spacer(Modifier.width(10.dp))
                        Text(s.alertSound, color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(
                            checked = config.playSound,
                            onCheckedChange = { soundOn ->
                                feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                                onUpdateConfig(config.copy(playSound = soundOn))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            feedback?.play(FeedbackAction.MOSQUE_FINDER)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !PermissionHelper.hasNotificationPermission(context)
                            ) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onSendTestAlert()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_test_prayer_alert_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldAccent)
                    ) {
                        Text(s.sendTestAlert, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
