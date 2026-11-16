package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.CardDarkEmerald
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
fun ActiveFocusScreen(
    state: NamazUiState,
    onTurnOff: () -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onBack()
    }
    val s = AppStrings.get(state.settings.appLanguage)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(
            showMoon = true,
            showMosqueArt = true,
            showPrayingSilhouette = true
        )

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        feedback?.play(FeedbackAction.BACK_TAP)
                        onBack()
                    },
                    modifier = Modifier.testTag("focus_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = s.modeActiveHeadline,
                style = MaterialTheme.typography.headlineMedium,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = s.stayFocused,
                color = TextPrimary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CardDarkEmerald.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 18.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = s.remainingTime, color = TextSecondary, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formatDurationHms(state.remainingMillis),
                        color = GoldAccent,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    feedback?.play(FeedbackAction.NAMAZ_OFF)
                    onTurnOff()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("turn_off_focus_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = null
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = s.turnOff,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}
