package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.IslamicContentData
import com.example.data.TasbihState
import com.example.data.ZikrOption
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BgDeepForest
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.CardElevated
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager

@Composable
fun TasbihScreen(
    tasbih: TasbihState,
    appLanguage: AppLanguage,
    onTapCount: () -> Boolean,
    onResetRound: () -> Unit,
    onSelectZikr: (ZikrOption) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }

    val s = AppStrings.get(appLanguage)
    val selectedZikr = remember(tasbih.selectedZikrId) {
        IslamicContentData.zikrOptions.firstOrNull { it.id == tasbih.selectedZikrId }
            ?: IslamicContentData.zikrOptions.first()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val orbScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tasbihOrbScale"
    )

    val roundProgress = (tasbih.currentCount.toFloat() / tasbih.roundTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedSweep by animateFloatAsState(
        targetValue = roundProgress * 360f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tasbihSweep"
    )
    val dailyProgress = (tasbih.dailyCount.toFloat() / tasbih.dailyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val goalReached = tasbih.dailyCount >= tasbih.dailyGoal

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(showMoon = true, showMosqueArt = false)

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    modifier = Modifier.testTag("tasbih_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.tasbihTitle, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(s.tasbihSub, color = TextSecondary, fontSize = 12.sp)
                }
                IconButton(
                    onClick = {
                        feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                        onResetRound()
                    },
                    modifier = Modifier.testTag("tasbih_reset_button")
                ) {
                    Icon(Icons.Rounded.Replay, contentDescription = s.resetRound, tint = GoldAccent)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Zikr Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IslamicContentData.zikrOptions.forEach { zikr ->
                    val selected = zikr.id == selectedZikr.id
                    FilterChip(
                        selected = selected,
                        onClick = {
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            onSelectZikr(zikr)
                        },
                        modifier = Modifier.testTag("zikr_chip_${zikr.id}"),
                        label = {
                            Text(
                                text = "${zikr.transliteration} (${zikr.defaultTarget})",
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

            Spacer(Modifier.height(14.dp))

            // Selected Zikr Arabic + Meaning Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedZikr.arabic,
                        color = GoldAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = selectedZikr.transliteration,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (appLanguage == AppLanguage.BENGALI) selectedZikr.meaningBn else selectedZikr.meaningEn,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Interactive Counter Orb
            Box(
                modifier = Modifier
                    .size(226.dp)
                    .scale(orbScale)
                    .testTag("tasbih_tap_orb")
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.material3.ripple(color = GoldAccent),
                        onClick = {
                            val completed = onTapCount()
                            feedback?.play(
                                if (completed) FeedbackAction.SAVE_SUCCESS else FeedbackAction.HOME_CLICK
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 14f
                    drawArc(
                        color = BorderEmeraldGlow,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = GoldAccent,
                        startAngle = -90f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(188.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(EmeraldGlow.copy(alpha = 0.25f), EmeraldDeep, CardDarkEmerald)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${tasbih.currentCount}",
                            color = TextPrimary,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("tasbih_current_count")
                        )
                        Text(
                            text = "/ ${tasbih.roundTarget} ${s.roundTarget}",
                            color = GoldAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.TouchApp,
                                contentDescription = null,
                                tint = EmeraldGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(s.tapToCount, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Daily Goal & Progress Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.dailyGoal, color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = "${tasbih.dailyCount} / ${tasbih.dailyGoal}",
                                color = if (goalReached) EmeraldGlow else GoldAccent,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (goalReached) {
                                Text(
                                    text = s.goalReached,
                                    color = EmeraldGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    feedback?.play(FeedbackAction.HOME_CLICK)
                                    onSetDailyGoal(tasbih.dailyGoal - 33)
                                },
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                border = BorderStroke(1.dp, BorderEmeraldGlow)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease goal", tint = TextPrimary)
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    feedback?.play(FeedbackAction.HOME_CLICK)
                                    onSetDailyGoal(tasbih.dailyGoal + 33)
                                },
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                border = BorderStroke(1.dp, GoldAccent)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase goal", tint = GoldAccent)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { dailyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (goalReached) EmeraldGlow else GoldAccent,
                        trackColor = BgDeepForest
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${s.todayTotal}: ${tasbih.dailyCount}",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${s.lifetimeTotal}: ${tasbih.lifetimeCount}",
                            color = EmeraldGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
