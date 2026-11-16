package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallEventInfo
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.AvatarIconTint
import com.example.ui.theme.AvatarSurface
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager

@Composable
fun CallMonitorScreen(
    title: String,
    endCallContentDescription: String,
    callEvent: CallEventInfo,
    onDismissOrEnd: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        onDismissOrEnd()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val barFactors = listOf(0.4f, 0.9f, 0.6f, 1.0f, 0.7f, 0.85f, 0.45f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(showMoon = false, showMosqueArt = false)

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text(text = title, color = TextSecondary, fontSize = 16.sp)
                Spacer(Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(AvatarSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = AvatarIconTint,
                        modifier = Modifier.size(58.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = callEvent.callerNumber,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = callEvent.statusText.ifBlank { "Auto Answering..." },
                    color = EmeraldGlow,
                    fontSize = 14.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.height(36.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    barFactors.forEachIndexed { index, factor ->
                        val anim by infiniteTransition.animateFloat(
                            initialValue = 8f,
                            targetValue = 34f * factor,
                            animationSpec = infiniteRepeatable(
                                animation = tween(420 + index * 70, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar$index"
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(anim.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldGlow)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = EmeraldGlow
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = callEvent.spokenMessage,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    feedback?.play(FeedbackAction.NAMAZ_OFF)
                    onDismissOrEnd()
                },
                modifier = Modifier
                    .padding(bottom = 18.dp)
                    .size(68.dp)
                    .testTag("end_call_button")
                    .clip(CircleShape)
                    .background(DangerRed)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CallEnd,
                    contentDescription = endCallContentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
