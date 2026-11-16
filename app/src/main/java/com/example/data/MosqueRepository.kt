package com.example.data

import android.location.Location
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

data class MosqueSearchResult(
    val mosques: List<MosqueInfo>,
    val source: MosqueDataSource,
    val usedApiKeyFallback: Boolean
)

class MosqueRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val formMediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()

    companion object {
        fun hasConfiguredGoogleMapsKey(): Boolean {
            val key = BuildConfig.MAPS_API_KEY.trim()
            return key.isNotEmpty() && key != "MY_MAPS_API_KEY"
        }
    }

    private fun hasConfiguredGoogleMapsKey(): Boolean =
        Companion.hasConfiguredGoogleMapsKey()

    suspend fun findNearbyMosques(
        latitude: Double,
        longitude: Double,
        radiusKm: Int,
        query: String = ""
    ): Result<MosqueSearchResult> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (hasConfiguredGoogleMapsKey()) {
            val placesAttempt = runCatching {
                queryGooglePlaces(
                    apiKey = BuildConfig.MAPS_API_KEY.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = radiusKm * 1000.0,
                    query = trimmedQuery
                )
            }
            if (placesAttempt.isSuccess) {
                return@withContext Result.success(
                    MosqueSearchResult(
                        mosques = placesAttempt.getOrThrow(),
                        source = MosqueDataSource.GOOGLE_PLACES,
                        usedApiKeyFallback = false
                    )
                )
            }
        }

        // Real OpenStreetMap Overpass API fallback when MAPS_API_KEY is not configured or unavailable
        runCatching {
            val osmMosques = queryOverpassMosques(
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusKm * 1000,
                query = trimmedQuery
            )
            MosqueSearchResult(
                mosques = osmMosques,
                source = MosqueDataSource.OPENSTREETMAP,
                usedApiKeyFallback = !hasConfiguredGoogleMapsKey()
            )
        }
    }

    private fun queryGooglePlaces(
        apiKey: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        query: String
    ): List<MosqueInfo> {
        val isTextSearch = query.isNotBlank()
        val url = if (isTextSearch) {
            "https://places.googleapis.com/v1/places:searchText"
        } else {
            "https://places.googleapis.com/v1/places:searchNearby"
        }

        val centerJson = JSONObject()
            .put("latitude", latitude)
            .put("longitude", longitude)
        val circleJson = JSONObject()
            .put("center", centerJson)
            .put("radius", radiusMeters.coerceAtMost(50000.0))

        val bodyJson = if (isTextSearch) {
            JSONObject()
                .put("textQuery", if (query.contains("mosque", ignoreCase = true) || query.contains("masjid", ignoreCase = true)) query else "$query mosque")
                .put("maxResultCount", 20)
                .put("locationBias", JSONObject().put("circle", circleJson))
        } else {
            JSONObject()
                .put("includedTypes", JSONArray().put("mosque"))
                .put("maxResultCount", 20)
                .put("rankPreference", "DISTANCE")
                .put("locationRestriction", JSONObject().put("circle", circleJson))
        }

        val fieldMask = listOf(
            "places.id",
            "places.displayName",
            "places.formattedAddress",
            "places.location",
            "places.rating",
            "places.userRatingCount",
            "places.regularOpeningHours",
            "places.nationalPhoneNumber",
            "places.internationalPhoneNumber",
            "places.photos"
        ).joinToString(",")

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", apiKey)
            .addHeader("X-Goog-FieldMask", fieldMask)
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Places API error ${response.code}: $raw")
            }
            val root = JSONObject(raw)
            val places = root.optJSONArray("places") ?: return emptyList()
            val results = mutableListOf<MosqueInfo>()

            for (i in 0 until places.length()) {
                val item = places.optJSONObject(i) ?: continue
                val id = item.optString("id", "place_$i")
                val name = item.optJSONObject("displayName")?.optString("text")?.takeIf { it.isNotBlank() }
                    ?: continue
                val locObj = item.optJSONObject("location") ?: continue
                val pLat = locObj.optDouble("latitude", Double.NaN)
                val pLng = locObj.optDouble("longitude", Double.NaN)
                if (pLat.isNaN() || pLng.isNaN()) continue

                val distance = computeDistanceMeters(latitude, longitude, pLat, pLng)
                if (distance > radiusMeters) continue

                val address = item.optString("formattedAddress").takeIf { it.isNotBlank() }
                    ?: formatCoordsAddress(pLat, pLng)
                val rating = if (item.has("rating")) item.optDouble("rating") else null
                val ratingCount = if (item.has("userRatingCount")) item.optInt("userRatingCount") else null

                val hoursObj = item.optJSONObject("regularOpeningHours")
                val isOpenNow = if (hoursObj != null && hoursObj.has("openNow")) {
                    hoursObj.optBoolean("openNow")
                } else null
                val weekdayDescs = hoursObj?.optJSONArray("weekdayDescriptions")
                val hoursSummary = if (weekdayDescs != null && weekdayDescs.length() > 0) {
                    (0 until weekdayDescs.length())
                        .mapNotNull { weekdayDescs.optString(it).takeIf { s -> s.isNotBlank() } }
                        .joinToString("\n")
                } else null

                val phone = item.optString("internationalPhoneNumber").takeIf { it.isNotBlank() }
                    ?: item.optString("nationalPhoneNumber").takeIf { it.isNotBlank() }

                val photoName = item.optJSONArray("photos")
                    ?.optJSONObject(0)
                    ?.optString("name")
                    ?.takeIf { it.isNotBlank() }
                val photoUrl = photoName?.let {
                    "https://places.googleapis.com/v1/$it/media?maxWidthPx=800&key=$apiKey"
                }

                results.add(
                    MosqueInfo(
                        id = id,
                        name = name,
                        latitude = pLat,
                        longitude = pLng,
                        distanceMeters = distance,
                        address = address,
                        rating = rating,
                        userRatingCount = ratingCount,
                        isOpenNow = isOpenNow,
                        openingHoursText = hoursSummary,
                        phoneNumber = phone,
                        photoUrl = photoUrl,
                        source = MosqueDataSource.GOOGLE_PLACES
                    )
                )
            }
            return results.sortedBy { it.distanceMeters }
        }
    }

    private fun queryOverpassMosques(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        query: String
    ): List<MosqueInfo> {
        val overpassQuery = """
            [out:json][timeout:18];
            (
              node["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
              relation["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$latitude,$longitude);
            );
            out center tags;
        """.trimIndent()

        val formBody = ("data=" + URLEncoder.encode(overpassQuery, "UTF-8")).toRequestBody(formMediaType)
        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter")
            .post(formBody)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Overpass API error ${response.code}")
            }
            val root = JSONObject(raw)
            val elements = root.optJSONArray("elements") ?: return emptyList()
            val results = mutableListOf<MosqueInfo>()

            for (i in 0 until elements.length()) {
                val el = elements.optJSONObject(i) ?: continue
                val tags = el.optJSONObject("tags") ?: continue

                val center = el.optJSONObject("center")
                val mLat = if (el.has("lat")) el.optDouble("lat") else center?.optDouble("lat") ?: Double.NaN
                val mLng = if (el.has("lon")) el.optDouble("lon") else center?.optDouble("lon") ?: Double.NaN
                if (mLat.isNaN() || mLng.isNaN()) continue

                val name = tags.optString("name").takeIf { it.isNotBlank() }
                    ?: tags.optString("name:en").takeIf { it.isNotBlank() }
                    ?: tags.optString("name:bn").takeIf { it.isNotBlank() }
                    ?: continue

                if (query.isNotBlank() && !name.contains(query, ignoreCase = true)) {
                    val streetMatch = tags.optString("addr:street").contains(query, ignoreCase = true) ||
                        tags.optString("addr:suburb").contains(query, ignoreCase = true)
                    if (!streetMatch) continue
                }

                val distance = computeDistanceMeters(latitude, longitude, mLat, mLng)
                val addressParts = listOfNotNull(
                    tags.optString("addr:housenumber").takeIf { it.isNotBlank() },
                    tags.optString("addr:street").takeIf { it.isNotBlank() },
                    tags.optString("addr:suburb").takeIf { it.isNotBlank() },
                    tags.optString("addr:city").takeIf { it.isNotBlank() }
                )
                val fullAddress = if (addressParts.isNotEmpty()) {
                    addressParts.joinToString(", ")
                } else {
                    tags.optString("addr:full").takeIf { it.isNotBlank() }
                        ?: formatCoordsAddress(mLat, mLng)
                }

                val openingHours = tags.optString("opening_hours").takeIf { it.isNotBlank() }
                val phone = tags.optString("phone").takeIf { it.isNotBlank() }
                    ?: tags.optString("contact:phone").takeIf { it.isNotBlank() }

                results.add(
                    MosqueInfo(
                        id = "${el.optString("type")}_${el.optLong("id")}",
                        name = name,
                        latitude = mLat,
                        longitude = mLng,
                        distanceMeters = distance,
                        address = fullAddress,
                        rating = null,
                        userRatingCount = null,
                        isOpenNow = null,
                        openingHoursText = openingHours,
                        phoneNumber = phone,
                        photoUrl = null,
                        source = MosqueDataSource.OPENSTREETMAP
                    )
                )
            }
            return results.sortedBy { it.distanceMeters }
        }
    }

    private fun computeDistanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, result)
        return result[0]
    }

    private fun formatCoordsAddress(lat: Double, lng: Double): String =
        String.format(Locale.US, "%.4f°, %.4f°", lat, lng)
}
