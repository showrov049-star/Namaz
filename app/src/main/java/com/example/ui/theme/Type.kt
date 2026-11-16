package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PlayfairDisplayFont = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val PlusJakartaFont = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val NotoBengaliFont = FontFamily(
    Font(R.font.noto_sans_bengali, FontWeight.Normal),
    Font(R.font.noto_sans_bengali, FontWeight.Medium),
    Font(R.font.noto_sans_bengali, FontWeight.SemiBold),
    Font(R.font.noto_sans_bengali, FontWeight.Bold)
)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = PlayfairDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = GoldAccent
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    )
)
