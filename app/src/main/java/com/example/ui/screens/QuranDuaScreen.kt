package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.DuaCategory
import com.example.data.DuaEntry
import com.example.data.IslamicContentData
import com.example.data.QuranAyah
import com.example.data.QuranSurahDetail
import com.example.data.QuranSurahSummary
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
import com.example.ui.viewmodel.QuranUiState
import com.example.util.AppStrings
import com.example.util.AudioPlaybackState
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager

@Composable
fun QuranDuaScreen(
    quranState: QuranUiState,
    duaBookmarks: Set<String>,
    appLanguage: AppLanguage,
    onSearchQueryChange: (String) -> Unit,
    onOpenSurah: (Int) -> Unit,
    onCloseSurah: () -> Unit,
    onPlayAyah: (QuranAyah) -> Unit,
    onPlaySurahFromStart: () -> Unit,
    onStopAudio: () -> Unit,
    onSelectDuaCategory: (DuaCategory) -> Unit,
    onToggleDuaBookmark: (String) -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    var activeSegment by remember { mutableIntStateOf(0) } // 0 = Quran, 1 = Duas

    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        if (quranState.selectedSurah != null) {
            onCloseSurah()
        } else {
            onStopAudio()
            onBack()
        }
    }

    val s = AppStrings.get(appLanguage)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(showMoon = false, showMosqueArt = false)

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        feedback?.play(FeedbackAction.BACK_TAP)
                        if (quranState.selectedSurah != null) {
                            onCloseSurah()
                        } else {
                            onStopAudio()
                            onBack()
                        }
                    },
                    modifier = Modifier.testTag("quran_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quranState.selectedSurah?.summary?.let {
                            if (appLanguage == AppLanguage.BENGALI) it.banglaName else it.englishName
                        } ?: s.quranDuaTitle,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = quranState.selectedSurah?.summary?.arabicName ?: s.quranDuaSub,
                        color = GoldAccent,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (quranState.selectedSurah == null) {
                Spacer(Modifier.height(10.dp))

                // Segmented Toggle: Al-Quran vs Daily Duas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(s.quranTab, s.duaTab).forEachIndexed { idx, label ->
                        val selected = activeSegment == idx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("quran_dua_tab_$idx")
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                                    activeSegment = idx
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) EmeraldPrimary else CardDarkEmerald,
                            border = BorderStroke(1.dp, if (selected) GoldAccent else BorderEmeraldGlow)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (activeSegment == 0) {
                    SurahIndexContent(
                        quranState = quranState,
                        appLanguage = appLanguage,
                        onSearchQueryChange = onSearchQueryChange,
                        onOpenSurah = onOpenSurah
                    )
                } else {
                    DailyDuaListContent(
                        selectedCategory = quranState.selectedDuaCategory,
                        bookmarks = duaBookmarks,
                        appLanguage = appLanguage,
                        onSelectCategory = onSelectDuaCategory,
                        onToggleBookmark = onToggleDuaBookmark
                    )
                }
            } else {
                SurahReaderContent(
                    surah = quranState.selectedSurah,
                    isLoading = quranState.isLoadingSurah,
                    audioState = quranState.audioState,
                    appLanguage = appLanguage,
                    onPlayAyah = onPlayAyah,
                    onPlayAll = onPlaySurahFromStart,
                    onStopAudio = onStopAudio
                )
            }
        }
    }
}

@Composable
private fun SurahIndexContent(
    quranState: QuranUiState,
    appLanguage: AppLanguage,
    onSearchQueryChange: (String) -> Unit,
    onOpenSurah: (Int) -> Unit
) {
    val feedback = LocalFeedbackManager.current
    val s = AppStrings.get(appLanguage)
    val offlineIds = remember { IslamicContentData.offlineSurahs.map { it.summary.number }.toSet() }
    val filtered = remember(quranState.surahs, quranState.searchQuery) {
        val q = quranState.searchQuery.trim()
        if (q.isEmpty()) quranState.surahs
        else quranState.surahs.filter {
            it.englishName.contains(q, ignoreCase = true) ||
                it.banglaName.contains(q, ignoreCase = true) ||
                it.englishTranslation.contains(q, ignoreCase = true) ||
                it.number.toString() == q
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = quranState.searchQuery,
            onValueChange = { q ->
                if (q != quranState.searchQuery) feedback?.play(FeedbackAction.KEYBOARD_TAP)
                onSearchQueryChange(q)
            },
            placeholder = { Text(s.searchSurahHint, color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldAccent) },
            trailingIcon = {
                if (quranState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderEmeraldGlow,
                focusedContainerColor = CardDarkEmerald,
                unfocusedContainerColor = CardDarkEmerald,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("surah_search_input")
        )

        Spacer(Modifier.height(10.dp))

        if (quranState.isLoadingSurah) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GoldAccent)
                Spacer(Modifier.width(8.dp))
                Text("Loading Surah...", color = TextSecondary, fontSize = 13.sp)
            }
        }

        if (quranState.surahError != null) {
            Text(
                text = quranState.surahError,
                color = GoldSoft,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.number }) { surah ->
                SurahRowCard(
                    surah = surah,
                    isOfflineAvailable = offlineIds.contains(surah.number),
                    appLanguage = appLanguage,
                    onClick = {
                        feedback?.play(FeedbackAction.HOME_CLICK)
                        onOpenSurah(surah.number)
                    }
                )
            }
        }
    }
}

@Composable
private fun SurahRowCard(
    surah: QuranSurahSummary,
    isOfflineAvailable: Boolean,
    appLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val s = AppStrings.get(appLanguage)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("surah_card_${surah.number}")
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, BorderEmeraldGlow)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.number}",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (appLanguage == AppLanguage.BENGALI) surah.banglaName else surah.englishName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${surah.englishTranslation} • ${surah.ayahCount} ${s.ayahsLabel}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (isOfflineAvailable) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "• ${s.offlineVerifiedBadge}",
                            color = EmeraldGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Text(
                text = surah.arabicName,
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SurahReaderContent(
    surah: QuranSurahDetail,
    isLoading: Boolean,
    audioState: AudioPlaybackState,
    appLanguage: AppLanguage,
    onPlayAyah: (QuranAyah) -> Unit,
    onPlayAll: () -> Unit,
    onStopAudio: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    val s = AppStrings.get(appLanguage)

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(8.dp))

        // Recitation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardElevated,
            border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        feedback?.play(FeedbackAction.HOME_CLICK)
                        onPlayAll()
                    },
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (audioState.isBuffering) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(
                            imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (audioState.isPlaying) s.pauseAudio else s.playSurah,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (audioState.activeKey != null) {
                    IconButton(onClick = {
                        feedback?.play(FeedbackAction.BACK_TAP)
                        onStopAudio()
                    }) {
                        Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = GoldAccent)
                    }
                }

                Spacer(Modifier.weight(1f))
                Text(
                    text = "${surah.ayahs.size} ${s.ayahsLabel}",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (audioState.errorMessage != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = audioState.errorMessage, color = GoldSoft, fontSize = 12.sp)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldAccent)
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(surah.ayahs, key = { it.globalNumber }) { ayah ->
                val isCurrentAyah = audioState.activeKey == "ayah_${ayah.globalNumber}"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCurrentAyah) CardElevated else CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(
                        1.dp,
                        if (isCurrentAyah) GoldAccent else BorderEmeraldGlow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${ayah.numberInSurah}",
                                    color = EmeraldGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    feedback?.play(FeedbackAction.HOME_CLICK)
                                    onPlayAyah(ayah)
                                }
                            ) {
                                if (isCurrentAyah && audioState.isBuffering) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = GoldAccent)
                                } else {
                                    Icon(
                                        imageVector = if (isCurrentAyah && audioState.isPlaying) {
                                            Icons.Rounded.Pause
                                        } else {
                                            Icons.Rounded.PlayArrow
                                        },
                                        contentDescription = "Play Ayah",
                                        tint = GoldAccent
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = ayah.arabicText,
                            color = GoldAccent,
                            fontSize = 23.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (ayah.transliteration.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = ayah.transliteration,
                                color = EmeraldGlow,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = ayah.banglaTranslation,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDuaListContent(
    selectedCategory: DuaCategory,
    bookmarks: Set<String>,
    appLanguage: AppLanguage,
    onSelectCategory: (DuaCategory) -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    val feedback = LocalFeedbackManager.current
    val filtered = remember(selectedCategory) {
        if (selectedCategory == DuaCategory.ALL) IslamicContentData.dailyDuas
        else IslamicContentData.dailyDuas.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DuaCategory.entries.forEach { cat ->
                val selected = cat == selectedCategory
                FilterChip(
                    selected = selected,
                    onClick = {
                        feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                        onSelectCategory(cat)
                    },
                    label = {
                        Text(
                            text = if (appLanguage == AppLanguage.BENGALI) cat.titleBn else cat.titleEn,
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

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { dua ->
                DuaCard(
                    dua = dua,
                    isBookmarked = bookmarks.contains(dua.id),
                    appLanguage = appLanguage,
                    onToggleBookmark = {
                        feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                        onToggleBookmark(dua.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DuaCard(
    dua: DuaEntry,
    isBookmarked: Boolean,
    appLanguage: AppLanguage,
    onToggleBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardDarkEmerald.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, if (isBookmarked) GoldAccent.copy(alpha = 0.7f) else BorderEmeraldGlow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage == AppLanguage.BENGALI) dua.titleBn else dua.titleEn,
                    color = EmeraldGlow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) GoldAccent else TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = dua.arabic,
                color = GoldAccent,
                fontSize = 21.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = dua.transliteration,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (appLanguage == AppLanguage.BENGALI) dua.translationBn else "${dua.translationBn}\n\n${dua.translationEn}",
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = dua.reference,
                color = GoldSoft,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
