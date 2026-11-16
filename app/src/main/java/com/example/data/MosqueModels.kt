package com.example.data

enum class MosqueDataSource {
    GOOGLE_PLACES,
    OPENSTREETMAP
}

data class MosqueInfo(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float,
    val address: String,
    val rating: Double? = null,
    val userRatingCount: Int? = null,
    val isOpenNow: Boolean? = null,
    val openingHoursText: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val source: MosqueDataSource = MosqueDataSource.GOOGLE_PLACES
)

sealed interface MosqueUiStatus {
    data object Idle : MosqueUiStatus
    data object LoadingLocation : MosqueUiStatus
    data object LoadingMosques : MosqueUiStatus
    data object PermissionRequired : MosqueUiStatus
    data object GpsDisabled : MosqueUiStatus
    data class Success(val mosques: List<MosqueInfo>) : MosqueUiStatus
    data class Empty(val radiusKm: Int, val query: String) : MosqueUiStatus
    data class Error(val message: String) : MosqueUiStatus
}
