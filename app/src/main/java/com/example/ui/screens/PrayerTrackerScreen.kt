package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.NamazRepository
import com.example.data.PrayerName
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BgDeepForest
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PrayerTrackerScreen(
    history: Map<String, Set<PrayerName>>,
    appLanguage: AppLanguage,
    onTogglePrayer: (String, PrayerName) -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }

    val s = AppStrings.get(appLanguage)
    val todayKey = remember { NamazRepository.todayDateKey() }
    var selectedDateKey by remember { mutableStateOf(todayKey) }

    // Last 7 days keys (oldest -> today)
    val last7Days = remember {
        val dayFmt = SimpleDateFormat("EEE", Locale.US)
        (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -daysAgo) }
            val key = NamazRepository.todayDateKey(cal.timeInMillis)
            val label = if (daysAgo == 0) "Today" else dayFmt.format(cal.time)
            key to label
        }
    }

    // Last 30 days stats
    val last30Keys = remember {
        (29 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -daysAgo) }
            NamazRepository.todayDateKey(cal.timeInMillis)
        }
    }

    val selectedCompleted = history[selectedDateKey] ?: emptySet()
    val monthlyCompletedTotal = last30Keys.sumOf { (history[it] ?: emptySet()).size }
    val monthlyPossible = 30 * 5
    val monthlyPct = ((monthlyCompletedTotal * 100f) / monthlyPossible).toInt()

    // Streak calculation (consecutive days ending today/yesterday with all 5 prayers)
    val streakDays = remember(history) {
        var streak = 0
        for (daysAgo in 0..180) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -daysAgo) }
            val key = NamazRepository.todayDateKey(cal.timeInMillis)
            val count = history[key]?.size ?: 0
            if (count == 5) {
                streak++
            } else if (daysAgo == 0 && count > 0) {
                // Today still in progress, check previous days
                continue
            } else {
                break
            }
        }
        streak
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
                    modifier = Modifier.testTag("tracker_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(s.prayerTrackerTitle, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(s.prayerTrackerSub, color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Top KPI Row: Streak & 30-Day Consistency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(s.currentStreak, color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$streakDays ${s.daysUnit}",
                            color = GoldAccent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, BorderEmeraldGlow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(s.monthlyCompletion, color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$monthlyPct%",
                            color = EmeraldGlow,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Weekly 7-Day Selector & Bar Chart Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.weeklyOverview, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        last7Days.forEach { (dateKey, dayLabel) ->
                            val count = history[dateKey]?.size ?: 0
                            val isSelected = dateKey == selectedDateKey
                            val barHeight = (14 + count * 14).dp
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        feedback?.play(FeedbackAction.HOME_CLICK)
                                        selectedDateKey = dateKey
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$count/5",
                                    color = if (count == 5) GoldAccent else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgDeepForest),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (count == 5) GoldAccent
                                                else if (count > 0) EmeraldPrimary
                                                else BorderEmeraldGlow.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    color = if (isSelected) GoldAccent else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Daily Checklist Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${s.todaysChecklist} ($selectedDateKey)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${selectedCompleted.size}/5",
                            color = if (selectedCompleted.size == 5) GoldAccent else EmeraldGlow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    PrayerName.entries.forEach { prayer ->
                        val done = selectedCompleted.contains(prayer)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("tracker_prayer_${prayer.name.lowercase()}")
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    feedback?.play(
                                        if (!done) FeedbackAction.SAVE_SUCCESS else FeedbackAction.TOGGLE_SWITCH
                                    )
                                    onTogglePrayer(selectedDateKey, prayer)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (done) CardElevated else BgDeepForest.copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.dp,
                                if (done) EmeraldGlow.copy(alpha = 0.7f) else BorderEmeraldGlow.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (done) EmeraldGlow else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = s.prayerLabel(prayer),
                                    color = if (done) GoldAccent else TextPrimary,
                                    fontWeight = if (done) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 30-Day Per-Prayer Consistency Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, BorderEmeraldGlow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.perPrayer30Days, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))

                    PrayerName.entries.forEach { prayer ->
                        val count30 = last30Keys.count { (history[it] ?: emptySet()).contains(prayer) }
                        val ratio = (count30 / 30f).coerceIn(0f, 1f)
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.prayerLabel(prayer), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("$count30 / 30", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = EmeraldGlow,
                                trackColor = BgDeepForest
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
