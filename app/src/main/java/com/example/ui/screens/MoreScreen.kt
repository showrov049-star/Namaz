package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdLoadStatus
import com.example.ads.AdMobManager
import com.example.data.NamazSettingsState
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InfoSky
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager
import com.example.util.PermissionHelper

@Composable
fun MoreScreen(
    settings: NamazSettingsState,
    onOpenSettings: () -> Unit,
    onOpenPrayerNotifications: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenQuranDua: () -> Unit,
    onOpenPrayerTracker: () -> Unit,
    onOpenRamadan: () -> Unit,
    onTestVoicePreview: () -> Unit,
    onRequestCallPermissions: () -> Unit,
    onRequestScreeningRole: () -> Unit,
    onShowTestInterstitial: () -> Unit,
    onShowTestRewarded: () -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }

    val context = LocalContext.current
    val s = AppStrings.get(settings.appLanguage)
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogBody by remember { mutableStateOf("") }

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
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = GoldAccent)
                Spacer(Modifier.width(10.dp))
                Text(s.more, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(14.dp))

            MoreMenuItem(
                icon = Icons.Rounded.NotificationsActive,
                iconTint = GoldAccent,
                title = s.prayerAlertsTitle,
                subtitle = s.prayerAlertsSub,
                testTag = "more_prayer_alerts",
                onClick = onOpenPrayerNotifications
            )

            MoreMenuItem(
                icon = Icons.Rounded.TouchApp,
                iconTint = GoldAccent,
                title = s.tasbihTitle,
                subtitle = s.tasbihSub,
                testTag = "more_tasbih",
                onClick = onOpenTasbih
            )

            MoreMenuItem(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                iconTint = EmeraldGlow,
                title = s.quranDuaTitle,
                subtitle = s.quranDuaSub,
                testTag = "more_quran_dua",
                onClick = onOpenQuranDua
            )

            MoreMenuItem(
                icon = Icons.Rounded.CheckCircle,
                iconTint = EmeraldGlow,
                title = s.prayerTrackerTitle,
                subtitle = s.prayerTrackerSub,
                testTag = "more_prayer_tracker",
                onClick = onOpenPrayerTracker
            )

            MoreMenuItem(
                icon = Icons.Rounded.NightsStay,
                iconTint = GoldAccent,
                title = s.ramadanModeTitle,
                subtitle = s.ramadanModeSub,
                testTag = "more_ramadan",
                onClick = onOpenRamadan
            )

            MoreMenuItem(
                icon = Icons.Rounded.Settings,
                iconTint = EmeraldGlow,
                title = s.settings,
                subtitle = "${s.voiceLanguage} • ${s.touchSoundTitle}",
                testTag = "more_settings",
                onClick = onOpenSettings
            )

            Spacer(Modifier.height(8.dp))

            MoreMenuItem(
                icon = Icons.Rounded.RecordVoiceOver,
                iconTint = EmeraldGlow,
                title = s.testCallPreview,
                subtitle = s.testCallSub,
                testTag = "more_test_voice",
                onClick = onTestVoicePreview
            )

            MoreMenuItem(
                icon = Icons.Rounded.Info,
                iconTint = EmeraldGlow,
                title = s.aboutNamaz,
                subtitle = s.aboutSub,
                testTag = "more_about",
                onClick = {
                    dialogTitle = s.aboutNamaz
                    dialogBody = s.aboutBody
                }
            )

            MoreMenuItem(
                icon = Icons.Rounded.Share,
                iconTint = EmeraldGlow,
                title = s.shareApp,
                subtitle = s.shareSub,
                testTag = "more_share",
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, s.shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, s.shareApp))
                }
            )

            MoreMenuItem(
                icon = Icons.Rounded.Security,
                iconTint = GoldAccent,
                title = s.callLimitations,
                subtitle = s.callLimitationsSub,
                testTag = "more_call_limits",
                onClick = {
                    val screeningOk = PermissionHelper.isCallScreeningRoleHeld(context)
                    val permsOk = PermissionHelper.hasCallPermissions(context)
                    dialogTitle = s.callLimitations
                    dialogBody = buildString {
                        append("• Call Screening Role: ${if (screeningOk) "Granted" else "Not granted"}\n")
                        append("• Phone Permissions: ${if (permsOk) "Granted" else "Not granted"}\n\n")
                        append(s.androidLimitBanner)
                    }
                }
            )

            MoreMenuItem(
                icon = Icons.Rounded.Email,
                iconTint = EmeraldGlow,
                title = s.contactUs,
                subtitle = s.contactSub,
                testTag = "more_contact",
                onClick = {
                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, "NAMAZ Smart Prayer Mode Feedback")
                    }
                    runCatching { context.startActivity(mailIntent) }
                }
            )

            MoreMenuItem(
                icon = Icons.Rounded.VerifiedUser,
                iconTint = InfoSky,
                title = s.privacyPolicy,
                subtitle = s.privacySub,
                testTag = "more_privacy",
                onClick = {
                    dialogTitle = s.privacyPolicy
                    dialogBody = s.privacyBody
                }
            )

            Spacer(Modifier.height(10.dp))

            AdMobTestControlsCard(
                title = s.admobTestTitle,
                subtitle = s.admobTestSub,
                interstitialLabel = s.showInterstitialTestAd,
                rewardedLabel = s.showRewardedTestAd,
                loadingLabel = s.adLoadingLabel,
                readyLabel = s.adReadyLabel,
                onShowInterstitial = onShowTestInterstitial,
                onShowRewarded = onShowTestRewarded
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (dialogTitle != null) {
        AlertDialog(
            onDismissRequest = { dialogTitle = null },
            containerColor = CardDarkEmerald,
            titleContentColor = GoldAccent,
            textContentColor = TextPrimary,
            title = { Text(dialogTitle ?: "") },
            text = { Text(dialogBody, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = { dialogTitle = null }) {
                    Text("OK", color = EmeraldGlow)
                }
            },
            dismissButton = if (dialogTitle == s.callLimitations) {
                {
                    TextButton(onClick = {
                        dialogTitle = null
                        onRequestScreeningRole()
                        onRequestCallPermissions()
                    }) {
                        Text(s.grantPermissions, color = GoldAccent)
                    }
                }
            } else null
        )
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                feedback?.play(FeedbackAction.HOME_CLICK)
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, BorderEmeraldGlow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun AdMobTestControlsCard(
    title: String,
    subtitle: String,
    interstitialLabel: String,
    rewardedLabel: String,
    loadingLabel: String,
    readyLabel: String,
    onShowInterstitial: () -> Unit,
    onShowRewarded: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    val interstitialStatus by AdMobManager.interstitialStatus.collectAsStateWithLifecycle()
    val rewardedStatus by AdMobManager.rewardedStatus.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("more_admob_test_card"),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Campaign,
                        contentDescription = null,
                        tint = GoldAccent
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        feedback?.play(FeedbackAction.HOME_CLICK)
                        onShowInterstitial()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("show_test_interstitial_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderEmeraldGlow),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (interstitialStatus == AdLoadStatus.LOADING) loadingLabel
                        else if (interstitialStatus == AdLoadStatus.READY) "$interstitialLabel ($readyLabel)"
                        else interstitialLabel,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = {
                        feedback?.play(FeedbackAction.HOME_CLICK)
                        onShowRewarded()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("show_test_rewarded_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (rewardedStatus == AdLoadStatus.LOADING) loadingLabel
                        else if (rewardedStatus == AdLoadStatus.READY) "$rewardedLabel ($readyLabel)"
                        else rewardedLabel,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
