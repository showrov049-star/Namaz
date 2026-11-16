package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.MosqueInfo
import com.example.data.MosqueRepository
import com.example.data.MosqueUiStatus
import kotlin.math.cos
import kotlin.math.roundToInt
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
import com.example.ui.viewmodel.NearbyMosqueUiState
import com.example.ui.viewmodel.formatDistance
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private const val DARK_EMERALD_MAP_STYLE = """
[
  {"elementType":"geometry","stylers":[{"color":"#0b2217"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#9ab8a8"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#06140d"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#163827"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#1f543a"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#05100b"}]},
  {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#0e2b1d"}]}
]
"""

@Composable
fun NearbyMosqueScreen(
    uiState: NearbyMosqueUiState,
    appLanguage: AppLanguage,
    onRequestLocationPermission: () -> Unit,
    onSelectRadius: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectMosque: (MosqueInfo?) -> Unit,
    onToggleListMode: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val feedback = LocalFeedbackManager.current
    BackHandler {
        feedback?.play(FeedbackAction.BACK_TAP)
        if (uiState.selectedMosque != null) {
            onSelectMosque(null)
        } else {
            onBack()
        }
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val s = AppStrings.get(appLanguage)
    val radii = listOf(1, 3, 5, 10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeepForest),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    feedback?.play(FeedbackAction.BACK_TAP)
                    onBack()
                },
                modifier = Modifier.testTag("mosque_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Icon(
                imageVector = Icons.Rounded.Mosque,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = s.nearbyMosques,
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                onClick = {
                    feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                    onToggleListMode(!uiState.showListMode)
                },
                modifier = Modifier.testTag("mosque_view_toggle_button")
            ) {
                Icon(
                    imageVector = if (uiState.showListMode) {
                        Icons.Rounded.Map
                    } else {
                        Icons.AutoMirrored.Rounded.FormatListBulleted
                    },
                    contentDescription = if (uiState.showListMode) s.mapView else s.listView,
                    tint = GoldAccent
                )
            }

            IconButton(
                onClick = {
                    feedback?.play(FeedbackAction.HOME_CLICK)
                    onRefresh()
                },
                modifier = Modifier.testTag("mosque_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = s.refreshLocationDesc,
                    tint = EmeraldGlow
                )
            }
        }

        // Search Bar & Radius Filter Chips
        Column(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { q ->
                    if (q != uiState.searchQuery) {
                        feedback?.play(FeedbackAction.KEYBOARD_TAP)
                    }
                    onSearchQueryChange(q)
                },
                placeholder = { Text(s.searchMosqueHint, color = TextSecondary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldAccent)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
                    .testTag("mosque_search_input")
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                radii.forEach { km ->
                    val selected = uiState.radiusKm == km
                    FilterChip(
                        selected = selected,
                        onClick = {
                            feedback?.play(FeedbackAction.TOGGLE_SWITCH)
                            onSelectRadius(km)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("radius_chip_$km"),
                        label = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$km KM",
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
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

            if (uiState.usedFallbackNotice) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = s.apiKeyFallbackNotice,
                    color = GoldSoft,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Main Map / Status Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val status = uiState.status) {
                MosqueUiStatus.PermissionRequired -> {
                    MosqueStatusStateCard(
                        icon = Icons.Rounded.MyLocation,
                        title = s.permissionDeniedTitle,
                        description = s.permissionDeniedDesc,
                        primaryLabel = s.detectLocation,
                        onPrimaryClick = onRequestLocationPermission,
                        secondaryLabel = s.openAppSettings,
                        onSecondaryClick = { openAppSettings(context) }
                    )
                }

                MosqueUiStatus.GpsDisabled -> {
                    MosqueStatusStateCard(
                        icon = Icons.Rounded.LocationOff,
                        title = s.gpsDisabledTitle,
                        description = s.gpsDisabledDesc,
                        primaryLabel = s.turnOnGps,
                        onPrimaryClick = { openLocationSettings(context) },
                        secondaryLabel = s.retry,
                        onSecondaryClick = onRefresh
                    )
                }

                MosqueUiStatus.Idle, MosqueUiStatus.LoadingLocation -> {
                    LoadingMosqueBanner(text = s.detectingLocation)
                }

                MosqueUiStatus.LoadingMosques -> {
                    MosqueMapAndListLayer(
                        uiState = uiState,
                        mosques = emptyList(),
                        onSelectMosque = onSelectMosque
                    )
                    LoadingMosqueBanner(text = s.searchingMosques)
                }

                is MosqueUiStatus.Error -> {
                    MosqueStatusStateCard(
                        icon = Icons.Rounded.LocationOff,
                        title = "Error",
                        description = status.message,
                        primaryLabel = s.retry,
                        onPrimaryClick = onRefresh
                    )
                }

                is MosqueUiStatus.Empty -> {
                    MosqueMapAndListLayer(
                        uiState = uiState,
                        mosques = emptyList(),
                        onSelectMosque = onSelectMosque
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .widthIn(max = 560.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CardDarkEmerald.copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = s.noMosquesFound,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(10.dp))
                                if (uiState.radiusKm < 10) {
                                    Button(
                                        onClick = { onSelectRadius(10) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(s.expandRadius)
                                    }
                                }
                            }
                        }
                    }
                }

                is MosqueUiStatus.Success -> {
                    MosqueMapAndListLayer(
                        uiState = uiState,
                        mosques = status.mosques,
                        onSelectMosque = onSelectMosque
                    )
                }
            }

            // Bottom Selected Mosque Details Card
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.selectedMosque != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 600.dp)
            ) {
                uiState.selectedMosque?.let { mosque ->
                    MosqueDetailsBottomCard(
                        mosque = mosque,
                        appLanguage = appLanguage,
                        onClose = {
                            feedback?.play(FeedbackAction.BACK_TAP)
                            onSelectMosque(null)
                        },
                        onGetDirections = {
                            feedback?.play(FeedbackAction.NAVIGATION_CONFIRM)
                            openGoogleMapsDirections(context, mosque)
                        },
                        onCall = { phone ->
                            feedback?.play(FeedbackAction.NAVIGATION_CONFIRM)
                            dialPhoneNumber(context, phone)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MosqueMapAndListLayer(
    uiState: NearbyMosqueUiState,
    mosques: List<MosqueInfo>,
    onSelectMosque: (MosqueInfo?) -> Unit
) {
    val lat = uiState.userLat ?: 0.0
    val lng = uiState.userLng ?: 0.0
    val userLatLng = remember(lat, lng) { LatLng(lat, lng) }

    val targetZoom = when (uiState.radiusKm) {
        1 -> 14.5f
        3 -> 13.0f
        5 -> 12.2f
        else -> 11.2f
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, targetZoom)
    }

    LaunchedEffect(userLatLng, uiState.radiusKm) {
        if (uiState.userLat != null && uiState.userLng != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(userLatLng, targetZoom),
                durationMs = 550
            )
        }
    }

    LaunchedEffect(uiState.selectedMosque) {
        uiState.selectedMosque?.let { selected ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(selected.latitude, selected.longitude),
                    15.2f
                ),
                durationMs = 450
            )
        }
    }

    val mapStyle = remember {
        runCatching { MapStyleOptions(DARK_EMERALD_MAP_STYLE) }.getOrNull()
    }

    val feedback = LocalFeedbackManager.current
    val hasMapsKey = remember { MosqueRepository.hasConfiguredGoogleMapsKey() }
    if (uiState.showListMode) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mosques, key = { it.id }) { mosque ->
                MosqueListRowCard(
                    mosque = mosque,
                    isSelected = uiState.selectedMosque?.id == mosque.id,
                    onClick = {
                        feedback?.play(FeedbackAction.MARKER_POP)
                        onSelectMosque(mosque)
                    }
                )
            }
        }
    } else if (!hasMapsKey) {
        OsmMosqueRadarMap(
            userLat = lat,
            userLng = lng,
            radiusKm = uiState.radiusKm,
            mosques = mosques,
            selectedMosque = uiState.selectedMosque,
            onSelectMosque = { mosque ->
                if (mosque != null) {
                    feedback?.play(FeedbackAction.MARKER_POP)
                }
                onSelectMosque(mosque)
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("nearby_mosque_google_map"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = uiState.hasLocationPermission,
                    mapStyleOptions = mapStyle
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false,
                    compassEnabled = true
                ),
                onMapClick = { onSelectMosque(null) }
            ) {
                if (uiState.userLat != null && uiState.userLng != null) {
                    Circle(
                        center = userLatLng,
                        radius = uiState.radiusKm * 1000.0,
                        fillColor = EmeraldPrimary.copy(alpha = 0.08f),
                        strokeColor = EmeraldGlow.copy(alpha = 0.65f),
                        strokeWidth = 3f
                    )
                }

                mosques.forEach { mosque ->
                    val isSelected = uiState.selectedMosque?.id == mosque.id
                    val markerState = rememberMarkerState(
                        key = mosque.id,
                        position = LatLng(mosque.latitude, mosque.longitude)
                    )
                    Marker(
                        state = markerState,
                        title = mosque.name,
                        snippet = "${formatDistance(mosque.distanceMeters)} • ${mosque.address}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isSelected) BitmapDescriptorFactory.HUE_YELLOW
                            else BitmapDescriptorFactory.HUE_GREEN
                        ),
                        onClick = {
                            feedback?.play(FeedbackAction.MARKER_POP)
                            onSelectMosque(mosque)
                            true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OsmMosqueRadarMap(
    userLat: Double,
    userLng: Double,
    radiusKm: Int,
    mosques: List<MosqueInfo>,
    selectedMosque: MosqueInfo?,
    onSelectMosque: (MosqueInfo?) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDarkEmerald.copy(alpha = 0.72f))
            .clickable { onSelectMosque(null) }
            .testTag("nearby_mosque_osm_radar")
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val centerX = widthPx / 2f
        val centerY = heightPx / 2f
        val maxRadiusPx = (minOf(widthPx, heightPx) * 0.43f).coerceAtLeast(60f)

        val latMetersPerDeg = 111_320.0
        val lngMetersPerDeg = (111_320.0 * cos(Math.toRadians(userLat))).coerceAtLeast(10_000.0)
        val radiusMeters = (radiusKm * 1000.0).coerceAtLeast(1000.0)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = (minOf(size.width, size.height) * 0.43f)
            // Concentric distance rings
            listOf(0.33f, 0.66f, 1.0f).forEach { fraction ->
                drawCircle(
                    color = EmeraldGlow.copy(alpha = if (fraction == 1f) 0.45f else 0.18f),
                    radius = r * fraction,
                    center = c,
                    style = Stroke(width = if (fraction == 1f) 2.5f else 1.5f)
                )
            }
            // Crosshair grid lines
            drawLine(
                color = BorderEmeraldGlow.copy(alpha = 0.4f),
                start = Offset(c.x - r, c.y),
                end = Offset(c.x + r, c.y),
                strokeWidth = 1.2f
            )
            drawLine(
                color = BorderEmeraldGlow.copy(alpha = 0.4f),
                start = Offset(c.x, c.y - r),
                end = Offset(c.x, c.y + r),
                strokeWidth = 1.2f
            )
            // User GPS position dot
            drawCircle(
                color = EmeraldPrimary.copy(alpha = 0.28f),
                radius = 18f,
                center = c
            )
            drawCircle(
                color = GoldAccent,
                radius = 8f,
                center = c
            )
        }

        val pinHalfPx = with(density) { 24.dp.toPx() }
        mosques.forEach { mosque ->
            val dxMeters = (mosque.longitude - userLng) * lngMetersPerDeg
            val dyMeters = (mosque.latitude - userLat) * latMetersPerDeg
            val normX = (dxMeters / radiusMeters).coerceIn(-0.98, 0.98).toFloat()
            val normY = (-dyMeters / radiusMeters).coerceIn(-0.98, 0.98).toFloat()

            val px = centerX + normX * maxRadiusPx - pinHalfPx
            val py = centerY + normY * maxRadiusPx - pinHalfPx
            val isSelected = selectedMosque?.id == mosque.id

            Box(
                modifier = Modifier
                    .offset { IntOffset(px.roundToInt(), py.roundToInt()) }
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onSelectMosque(mosque) }
                    .testTag("osm_mosque_pin_${mosque.id}"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) GoldAccent else EmeraldPrimary,
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.85f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Mosque,
                            contentDescription = mosque.name,
                            tint = if (isSelected) BgDeepForest else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueListRowCard(
    mosque: MosqueInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CardElevated else CardDarkEmerald,
        border = BorderStroke(
            1.dp,
            if (isSelected) GoldAccent else BorderEmeraldGlow
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Mosque, contentDescription = null, tint = GoldAccent)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mosque.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mosque.address,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDistance(mosque.distanceMeters),
                    color = EmeraldGlow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (mosque.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${mosque.rating}",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueDetailsBottomCard(
    mosque: MosqueInfo,
    appLanguage: AppLanguage,
    onClose: () -> Unit,
    onGetDirections: () -> Unit,
    onCall: (String) -> Unit
) {
    val s = AppStrings.get(appLanguage)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("mosque_details_card"),
        shape = RoundedCornerShape(22.dp),
        color = CardDarkEmerald.copy(alpha = 0.98f),
        border = BorderStroke(1.2.dp, GoldSoft.copy(alpha = 0.65f)),
        shadowElevation = 10.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!mosque.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mosque.photoUrl,
                    contentDescription = mosque.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mosque.name,
                        color = GoldAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${formatDistance(mosque.distanceMeters)} ${s.away}",
                                color = EmeraldGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (mosque.rating != null) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            val countText = mosque.userRatingCount?.let { " ($it)" } ?: ""
                            Text(
                                text = "${mosque.rating}$countText",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (mosque.isOpenNow != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (mosque.isOpenNow) s.openNow else s.closedNow,
                                color = if (mosque.isOpenNow) EmeraldGlow else GoldSoft,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = EmeraldGlow,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = mosque.address,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            if (!mosque.openingHoursText.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = mosque.openingHoursText,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGetDirections,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("mosque_directions_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Rounded.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.getDirections, fontWeight = FontWeight.SemiBold)
                }

                if (!mosque.phoneNumber.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onCall(mosque.phoneNumber) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("mosque_call_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.callMosque, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueStatusStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 480.dp),
            shape = RoundedCornerShape(22.dp),
            color = CardDarkEmerald,
            border = BorderStroke(1.dp, BorderEmeraldGlow)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = title,
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(primaryLabel, fontWeight = FontWeight.SemiBold)
                }
                if (secondaryLabel != null && onSecondaryClick != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSecondaryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GoldSoft),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
                    ) {
                        Text(secondaryLabel, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingMosqueBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardDarkEmerald.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = GoldAccent
                )
                Spacer(Modifier.width(10.dp))
                Text(text = text, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun openGoogleMapsDirections(context: Context, mosque: MosqueInfo) {
    val navUri = "google.navigation:q=${mosque.latitude},${mosque.longitude}".toUri()
    val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (navIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(navIntent)
    } else {
        val webUri =
            "https://www.google.com/maps/dir/?api=1&destination=${mosque.latitude},${mosque.longitude}".toUri()
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

private fun dialPhoneNumber(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, "tel:${Uri.encode(phone)}".toUri())
    runCatching { context.startActivity(intent) }
}

private fun openLocationSettings(context: Context) {
    runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    runCatching { context.startActivity(intent) }
}
