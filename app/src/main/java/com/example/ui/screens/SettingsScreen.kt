package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AsrJuristicSchool
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.example.data.CallHandlingStrategy
import com.example.data.HapticIntensity
import com.example.data.NamazSettingsState
import com.example.data.PrayerCalculationMethod
import com.example.data.VoiceLanguage
import com.example.data.VoiceMessagePreset
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.CardElevated
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
fun SettingsScreen(
    settings: NamazSettingsState,
    onSave: (NamazSettingsState) -> Unit,
    onRestoreTouchSoundDefaults: () -> Unit,
    onRequestCallPermissions: () -> Unit,
    onRequestScreeningRole: () -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }

    val context = LocalContext.current
    val s = AppStrings.get(settings.appLanguage)

    var draft by remember(settings) { mutableStateOf(settings) }
    var showVoiceLangMenu by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }
    var showDurationMenu by remember { mutableStateOf(false) }
    var showStrategyMenu by remember { mutableStateOf(false) }
    var showCalcMethodMenu by remember { mutableStateOf(false) }
    var showAsrSchoolMenu by remember { mutableStateOf(false) }

    val durations = listOf(5, 10, 15, 20, 30)

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        feedback?.play(FeedbackAction.BACK_TAP)
                        onBack()
                    },
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = s.settings,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null, tint = EmeraldGlow)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.autoCallResponse, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(s.autoCallSubtitle, color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = draft.autoCallResponseEnabled,
                        onCheckedChange = { enabled ->
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            draft = draft.copy(autoCallResponseEnabled = enabled)
                            if (enabled) {
                                if (!PermissionHelper.isCallScreeningRoleHeld(context)) {
                                    onRequestScreeningRole()
                                }
                                if (!PermissionHelper.hasCallPermissions(context)) {
                                    onRequestCallPermissions()
                                }
                            }
                        },
                        modifier = Modifier.testTag("settings_auto_call_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Android Restriction Transparency Note
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardElevated.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = s.androidLimitBanner,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box {
                SettingsDropdownCard(
                    icon = Icons.Rounded.Security,
                    title = s.callHandlingMode,
                    value = if (draft.appLanguage == AppLanguage.BENGALI) {
                        draft.callStrategy.titleBn
                    } else {
                        draft.callStrategy.titleEn
                    },
                    testTag = "settings_strategy_dropdown",
                    onClick = { showStrategyMenu = true }
                )
                DropdownMenu(
                    expanded = showStrategyMenu,
                    onDismissRequest = { showStrategyMenu = false }
                ) {
                    CallHandlingStrategy.entries.forEach { strategy ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (draft.appLanguage == AppLanguage.BENGALI) {
                                        strategy.titleBn
                                    } else {
                                        strategy.titleEn
                                    }
                                )
                            },
                            onClick = {
                                draft = draft.copy(callStrategy = strategy)
                                showStrategyMenu = false
                                if (strategy == CallHandlingStrategy.AUTO_ANSWER_AND_TTS &&
                                    !PermissionHelper.hasCallPermissions(context)
                                ) {
                                    onRequestCallPermissions()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Box {
                SettingsDropdownCard(
                    icon = Icons.Rounded.Language,
                    title = s.voiceLanguage,
                    value = draft.voiceLanguage.label,
                    testTag = "settings_voice_lang_dropdown",
                    onClick = { showVoiceLangMenu = true }
                )
                DropdownMenu(
                    expanded = showVoiceLangMenu,
                    onDismissRequest = { showVoiceLangMenu = false }
                ) {
                    VoiceLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.label) },
                            onClick = {
                                draft = draft.copy(voiceLanguage = lang)
                                showVoiceLangMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Box {
                SettingsDropdownCard(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    title = s.voiceMessage,
                    value = if (draft.messagePreset == VoiceMessagePreset.DEFAULT) {
                        s.defaultMessage
                    } else {
                        s.customMessagePreset
                    },
                    testTag = "settings_message_preset_dropdown",
                    onClick = { showPresetMenu = true }
                )
                DropdownMenu(
                    expanded = showPresetMenu,
                    onDismissRequest = { showPresetMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(s.defaultMessage) },
                        onClick = {
                            draft = draft.copy(messagePreset = VoiceMessagePreset.DEFAULT)
                            showPresetMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(s.customMessagePreset) },
                        onClick = {
                            draft = draft.copy(messagePreset = VoiceMessagePreset.CUSTOM)
                            showPresetMenu = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            SettingsCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(s.customMessageLabel, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        val currentMsg = if (draft.voiceLanguage == VoiceLanguage.BENGALI) {
                            draft.customMessageBn
                        } else {
                            draft.customMessageEn
                        }
                        OutlinedTextField(
                            value = currentMsg,
                            onValueChange = { text ->
                                if (text != currentMsg) {
                                    feedback?.play(FeedbackAction.KEYBOARD_TAP)
                                }
                                draft = if (draft.voiceLanguage == VoiceLanguage.BENGALI) {
                                    draft.copy(
                                        customMessageBn = text,
                                        messagePreset = VoiceMessagePreset.CUSTOM
                                    )
                                } else {
                                    draft.copy(
                                        customMessageEn = text,
                                        messagePreset = VoiceMessagePreset.CUSTOM
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_message_input"),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = BorderEmeraldGlow,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Box {
                SettingsDropdownCard(
                    icon = Icons.Rounded.Schedule,
                    title = s.duration,
                    value = "${draft.durationMinutes} ${s.minutesUnit}",
                    testTag = "settings_duration_dropdown",
                    onClick = { showDurationMenu = true }
                )
                DropdownMenu(
                    expanded = showDurationMenu,
                    onDismissRequest = { showDurationMenu = false }
                ) {
                    durations.forEach { mins ->
                        DropdownMenuItem(
                            text = { Text("$mins ${s.minutesUnit}") },
                            onClick = {
                                draft = draft.copy(durationMinutes = mins)
                                showDurationMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.EventAvailable, contentDescription = null, tint = EmeraldGlow)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.autoTurnOff, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(s.autoTurnOffSub, color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = draft.autoTurnOff,
                        onCheckedChange = {
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            draft = draft.copy(autoTurnOff = it)
                        },
                        modifier = Modifier.testTag("settings_auto_off_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box {
                SettingsDropdownCard(
                    icon = Icons.Rounded.Schedule,
                    title = s.prayerCalcMethod,
                    value = if (draft.appLanguage == AppLanguage.BENGALI) {
                        draft.calculationMethod.titleBn
                    } else {
                        draft.calculationMethod.titleEn
                    },
                    testTag = "settings_calc_method_dropdown",
                    onClick = { showCalcMethodMenu = true }
                )
                DropdownMenu(
                    expanded = showCalcMethodMenu,
                    onDismissRequest = { showCalcMethodMenu = false }
                ) {
                    PrayerCalculationMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (draft.appLanguage == AppLanguage.BENGALI) {
                                        method.titleBn
                                    } else {
                                        method.titleEn
                                    }
                                )
                            },
                            onClick = {
                                draft = draft.copy(calculationMethod = method)
                                showCalcMethodMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Box {
                SettingsDropdownCard(
                    icon = Icons.Rounded.Schedule,
                    title = s.asrJuristicMethod,
                    value = if (draft.appLanguage == AppLanguage.BENGALI) {
                        draft.asrSchool.titleBn
                    } else {
                        draft.asrSchool.titleEn
                    },
                    testTag = "settings_asr_school_dropdown",
                    onClick = { showAsrSchoolMenu = true }
                )
                DropdownMenu(
                    expanded = showAsrSchoolMenu,
                    onDismissRequest = { showAsrSchoolMenu = false }
                ) {
                    AsrJuristicSchool.entries.forEach { school ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (draft.appLanguage == AppLanguage.BENGALI) {
                                        school.titleBn
                                    } else {
                                        school.titleEn
                                    }
                                )
                            },
                            onClick = {
                                draft = draft.copy(asrSchool = school)
                                showAsrSchoolMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Dedicated Touch & Sound Experience Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = s.touchSoundTitle,
                    color = GoldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = EmeraldGlow)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.enableTouchSounds, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(s.enableTouchSoundsSub, color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = draft.touchSoundsEnabled,
                        onCheckedChange = { enabled ->
                            val updated = draft.copy(touchSoundsEnabled = enabled)
                            draft = updated
                            feedback?.updatePreferences(
                                updated.touchSoundsEnabled,
                                updated.hapticFeedbackEnabled,
                                updated.soundVolume,
                                updated.hapticIntensity
                            )
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            onSave(updated)
                        },
                        modifier = Modifier.testTag("touch_sounds_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Vibration, contentDescription = null, tint = EmeraldGlow)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.enableHapticFeedback, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(s.enableHapticFeedbackSub, color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = draft.hapticFeedbackEnabled,
                        onCheckedChange = { enabled ->
                            val updated = draft.copy(hapticFeedbackEnabled = enabled)
                            draft = updated
                            feedback?.updatePreferences(
                                updated.touchSoundsEnabled,
                                updated.hapticFeedbackEnabled,
                                updated.soundVolume,
                                updated.hapticIntensity
                            )
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            onSave(updated)
                        },
                        modifier = Modifier.testTag("haptic_feedback_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.soundVolume, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        text = "${(draft.soundVolume * 100).toInt()}%",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = draft.soundVolume,
                    onValueChange = { vol ->
                        draft = draft.copy(soundVolume = vol)
                        feedback?.updatePreferences(
                            draft.touchSoundsEnabled,
                            draft.hapticFeedbackEnabled,
                            vol,
                            draft.hapticIntensity
                        )
                    },
                    onValueChangeFinished = {
                        onSave(draft)
                        feedback?.play(FeedbackAction.HOME_CLICK)
                    },
                    enabled = draft.touchSoundsEnabled,
                    modifier = Modifier.testTag("sound_volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = BorderEmeraldGlow
                    )
                )

                Spacer(Modifier.height(8.dp))

                Text(s.hapticIntensity, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HapticIntensity.entries.forEach { level ->
                        val selected = draft.hapticIntensity == level
                        FilterChip(
                            selected = selected,
                            enabled = draft.hapticFeedbackEnabled,
                            onClick = {
                                val updated = draft.copy(hapticIntensity = level)
                                draft = updated
                                feedback?.updatePreferences(
                                    updated.touchSoundsEnabled,
                                    updated.hapticFeedbackEnabled,
                                    updated.soundVolume,
                                    level
                                )
                                feedback?.play(
                                    action = FeedbackAction.NAMAZ_ON,
                                    overrideSoundEnabled = false,
                                    overrideHapticEnabled = true,
                                    overrideIntensity = level
                                )
                                onSave(updated)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("haptic_intensity_${level.name.lowercase()}"),
                            label = {
                                Text(
                                    text = if (draft.appLanguage == AppLanguage.BENGALI) {
                                        level.titleBn
                                    } else {
                                        level.titleEn
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = CardElevated,
                                labelColor = TextSecondary,
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.24f),
                                selectedLabelColor = GoldAccent
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = draft.hapticFeedbackEnabled,
                                selected = selected,
                                borderColor = BorderEmeraldGlow,
                                selectedBorderColor = GoldAccent
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            feedback?.play(
                                action = FeedbackAction.NAMAZ_ON,
                                overrideSoundEnabled = true,
                                overrideHapticEnabled = draft.hapticFeedbackEnabled,
                                overrideVolume = draft.soundVolume.coerceAtLeast(0.2f),
                                overrideIntensity = draft.hapticIntensity,
                                ignoreRingerModeForPreview = true
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("preview_sound_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldAccent),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = GoldAccent
                        )
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s.previewSound, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            val defaults = NamazSettingsState()
                            val updated = draft.copy(
                                touchSoundsEnabled = defaults.touchSoundsEnabled,
                                hapticFeedbackEnabled = defaults.hapticFeedbackEnabled,
                                soundVolume = defaults.soundVolume,
                                hapticIntensity = defaults.hapticIntensity
                            )
                            draft = updated
                            feedback?.updatePreferences(
                                updated.touchSoundsEnabled,
                                updated.hapticFeedbackEnabled,
                                updated.soundVolume,
                                updated.hapticIntensity
                            )
                            onRestoreTouchSoundDefaults()
                            feedback?.play(FeedbackAction.SAVE_SUCCESS)
                            Toast.makeText(context, s.defaultsRestoredToast, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("restore_touch_sound_defaults_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderEmeraldGlow),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s.restoreDefaults, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    feedback?.play(FeedbackAction.SAVE_SUCCESS)
                    onSave(draft)
                    Toast.makeText(context, s.savedToast, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(s.saveChanges, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, BorderEmeraldGlow)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun SettingsDropdownCard(
    icon: ImageVector,
    title: String,
    value: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, BorderEmeraldGlow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = EmeraldGlow)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(value, color = GoldAccent, fontSize = 13.sp)
            }
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = TextSecondary)
        }
    }
}
