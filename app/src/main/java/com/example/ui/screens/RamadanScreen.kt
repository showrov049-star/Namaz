package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerName
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
import com.example.ui.viewmodel.NamazUiState
import com.example.ui.viewmodel.formatDurationHms
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.HijriDateHelper
import com.example.util.LocalFeedbackManager
import com.example.util.PrayerTimeCalculator
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun RamadanScreen(
    state: NamazUiState,
    onRequestLocation: () -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }

    val s = AppStrings.get(state.settings.appLanguage)
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val hijriInfo = remember(nowMillis / 60_000L) { HijriDateHelper.fromCalendar() }

    val hasCoords = state.settings.hasSavedLocation
    val (_, scheduleRows) = remember(
        state.settings.latitude,
        state.settings.longitude,
        state.settings.calculationMethod,
        state.settings.asrSchool,
        hasCoords
    ) {
        if (hasCoords) {
            HijriDateHelper.generateRamadanCalendar(
                latitude = state.settings.latitude,
                longitude = state.settings.longitude,
                method = state.settings.calculationMethod,
                asrSchool = state.settings.asrSchool
            )
        } else {
            false to emptyList()
        }
    }

    // Compute live Sehri and Iftar epoch millis for today/tomorrow
    val (sehriCountdownMillis, iftarCountdownMillis, todaySehriLabel, todayIftarLabel) = remember(
        nowMillis,
        state.settings.latitude,
        state.settings.longitude,
        state.settings.calculationMethod,
        state.settings.asrSchool,
        hasCoords
    ) {
        if (!hasCoords) {
            Quadruple(0L, 0L, "--:--", "--:--")
        } else {
            val today = Calendar.getInstance()
            val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            val todayTimes = PrayerTimeCalculator.calculateForDate(
                state.settings.latitude,
                state.settings.longitude,
                "",
                state.settings.calculationMethod,
                state.settings.asrSchool,
                today
            )
            val tomorrowTimes = PrayerTimeCalculator.calculateForDate(
                state.settings.latitude,
                state.settings.longitude,
                "",
                state.settings.calculationMethod,
                state.settings.asrSchool,
                tomorrow
            )
            val todayFajr = todayTimes.entries.first { it.prayer == PrayerName.FAJR }
            val todayMaghrib = todayTimes.entries.first { it.prayer == PrayerName.MAGHRIB }
            val tmrwFajr = tomorrowTimes.entries.first { it.prayer == PrayerName.FAJR }
            val tmrwMaghrib = tomorrowTimes.entries.first { it.prayer == PrayerName.MAGHRIB }

            val sehriTodayMs = epochFor(today, todayFajr.hour24, todayFajr.minute)
            val iftarTodayMs = epochFor(today, todayMaghrib.hour24, todayMaghrib.minute)
            val sehriTmrwMs = epochFor(tomorrow, tmrwFajr.hour24, tmrwFajr.minute)
            val iftarTmrwMs = epochFor(tomorrow, tmrwMaghrib.hour24, tmrwMaghrib.minute)

            val nextSehriMs = if (sehriTodayMs > nowMillis) sehriTodayMs else sehriTmrwMs
            val nextIftarMs = if (iftarTodayMs > nowMillis) iftarTodayMs else iftarTmrwMs

            Quadruple(
                (nextSehriMs - nowMillis).coerceAtLeast(0L),
                (nextIftarMs - nowMillis).coerceAtLeast(0L),
                todayFajr.formattedTime,
                todayMaghrib.formattedTime
            )
        }
    }

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
                    modifier = Modifier.testTag("ramadan_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.ramadanModeTitle, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(hijriInfo.formatted(state.settings.appLanguage), color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            if (!hasCoords) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = s.locationPromptDesc,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                feedback?.play(FeedbackAction.HOME_CLICK)
                                onRequestLocation()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White)
                        ) {
                            Text(s.detectLocation, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Live Sehri & Iftar Countdown Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).testTag("sehri_countdown_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = CardDarkEmerald.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, BorderEmeraldGlow)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.WbTwilight, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.sehriCountdown, color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = formatDurationHms(sehriCountdownMillis),
                                color = EmeraldGlow,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${s.sehriEnds}: $todaySehriLabel",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f).testTag("iftar_countdown_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = CardDarkEmerald.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.NightsStay, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.iftarCountdown, color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = formatDurationHms(iftarCountdownMillis),
                                color = GoldAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${s.iftarTime}: $todayIftarLabel",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 30-Day Ramadan Schedule Table
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, BorderEmeraldGlow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.ramadanCalendarTitle,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        if (state.settings.locationName.isNotBlank()) {
                            Text(
                                text = state.settings.locationName,
                                color = EmeraldGlow,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 6.dp)
                        ) {
                            Text(s.dayCol, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                            Text(s.dateCol, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                            Text(s.sehriCol, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(s.iftarCol, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = BorderEmeraldGlow)

                        scheduleRows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (row.isToday) CardElevated else Color.Transparent)
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${row.ramadanDay}",
                                    color = if (row.isToday) GoldAccent else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(0.6f)
                                )
                                Text(
                                    text = row.gregorianLabel,
                                    color = if (row.isToday) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Text(
                                    text = row.sehriTime,
                                    color = EmeraldGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = row.iftarTime,
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = BorderEmeraldGlow.copy(alpha = 0.35f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun epochFor(dayCal: Calendar, hour24: Int, minute: Int): Long {
    return (dayCal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, hour24)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
