package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.AppLanguage
import com.example.ui.components.GlowingMosqueArchFrame
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.components.NamazAnimatedButton
import com.example.ui.components.NamazMotion
import com.example.ui.components.pressScaleClickable
import com.example.ui.components.rememberReduceMotionEnabled
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppStrings
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun AuthScreen(
    language: AppLanguage,
    onToggleLanguage: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val auth = remember { Firebase.auth }
    val reduceMotion = rememberReduceMotionEnabled()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var heroVisible by remember { mutableStateOf(reduceMotion) }

    val webClientId = remember(context) {
        val idRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (idRes != 0) context.getString(idRes) else ""
    }

    LaunchedEffect(Unit) {
        heroVisible = true
        if (auth.currentUser == null && webClientId.isNotBlank()) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                handleGoogleCredentialResult(auth, result.credential) { err ->
                    errorMessage = err
                }
            } catch (_: NoCredentialException) {
                // Expected on first run before an authorized Google account exists
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Auto Google Sign-In cancelled: ${e.message}", e)
            } catch (e: GetCredentialException) {
                Log.w("Auth", "Auto Google Sign-In unavailable: ${e.message}", e)
            }
        }
    }

    fun startInteractiveSignIn() {
        if (webClientId.isBlank()) {
            errorMessage = if (language == AppLanguage.BENGALI) {
                "Firebase Web Client ID কনফিগার করা নেই।"
            } else {
                "Firebase Web Client ID is not configured."
            }
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                handleGoogleCredentialResult(auth, result.credential) { err ->
                    errorMessage = err
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
                errorMessage = if (language == AppLanguage.BENGALI) {
                    "সাইন-ইন বাতিল করা হয়েছে। পুনরায় চেষ্টা করুন।"
                } else {
                    "Sign-in was cancelled. Tap below to try again."
                }
            } catch (e: GetCredentialException) {
                Log.w("Auth", "Google Sign-In failed: ${e.message}", e)
                errorMessage = e.localizedMessage ?: "Google Sign-In failed"
            } finally {
                isLoading = false
            }
        }
    }

    val s = AppStrings.get(language)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IslamicNightBackdrop(showMoon = true, showMosqueArt = true)

        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardDarkEmerald.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .pressScaleClickable { onToggleLanguage() }
                        .testTag("auth_language_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
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
                            text = if (language == AppLanguage.ENGLISH) "বাংলা / EN" else "EN / বাংলা",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = heroVisible,
                enter = fadeIn(tween(NamazMotion.LARGE_MS)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(NamazMotion.LARGE_MS)) +
                    slideInVertically(tween(NamazMotion.LARGE_MS)) { 28 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(168.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlowingMosqueArchFrame(isActive = true, modifier = Modifier.fillMaxSize())
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(EmeraldGlow.copy(alpha = 0.28f), EmeraldDeep)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mosque,
                                contentDescription = s.appTitle,
                                tint = GoldAccent,
                                modifier = Modifier.size(44.dp)
                            )
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(18.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardDarkEmerald.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, BorderEmeraldGlow)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val bullets = if (language == AppLanguage.BENGALI) {
                                listOf(
                                    "নামাজের সময়সূচি, তাসবিহ ও অগ্রগতি ক্লাউডে সিঙ্ক করুন",
                                    "দোয়া বুকমার্ক ও পছন্দের ভাষা সব ডিভাইসে সংরক্ষণ করুন",
                                    "কুইক সেটিংস টাইল, কিবলা কম্পাস ও স্মার্ট রিমাইন্ডার"
                                )
                            } else {
                                listOf(
                                    "Sync your Prayer Tracker streak, Tasbih goals & settings",
                                    "Save bookmarked Duas & bilingual preferences safely in Cloud",
                                    "Quick Settings Tile, Live Qibla Compass & Exact Prayer Alerts"
                                )
                            }
                            bullets.forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = item,
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = DangerRed.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                NamazAnimatedButton(
                    onClick = { startInteractiveSignIn() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = if (language == AppLanguage.BENGALI) {
                            "Google দিয়ে সাইন ইন করুন"
                        } else {
                            "Continue with Google"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (language == AppLanguage.BENGALI) {
                        "নিরাপদ Google অ্যাকাউন্ট দ্বারা আপনার ডেটা সুরক্ষিত"
                    } else {
                        "Secured with your personal Google Account"
                    },
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private suspend fun handleGoogleCredentialResult(
    auth: FirebaseAuth,
    credential: androidx.credentials.Credential,
    onError: (String) -> Unit
) {
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCred = GoogleAuthProvider.getCredential(googleIdCredential.idToken, null)
        try {
            auth.signInWithCredential(firebaseCred).await()
        } catch (e: Exception) {
            Log.e("Auth", "Firebase signInWithCredential failed", e)
            onError(e.localizedMessage ?: "Firebase authentication failed")
        }
    } else {
        onError("Unsupported credential type returned")
    }
}

suspend fun signOutWithCredentialClear(context: Context) {
    withContext(NonCancellable) {
        runCatching {
            CredentialManager.create(context.applicationContext)
                .clearCredentialState(ClearCredentialStateRequest())
        }
        Firebase.auth.signOut()
    }
}
