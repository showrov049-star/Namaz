package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class QuranRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var cachedSurahList: List<QuranSurahSummary>? = null
    private val cachedSurahDetails = mutableMapOf<Int, QuranSurahDetail>()

    suspend fun getSurahList(): List<QuranSurahSummary> = withContext(Dispatchers.IO) {
        cachedSurahList?.let { return@withContext it }

        val offlineSummaries = IslamicContentData.offlineSurahs.map { it.summary }

        val remote = runCatching {
            val req = Request.Builder()
                .url("https://api.alquran.cloud/v1/surah")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@runCatching null
                val body = resp.body?.string() ?: return@runCatching null
                val root = JSONObject(body)
                val data = root.optJSONArray("data") ?: return@runCatching null
                val list = mutableListOf<QuranSurahSummary>()
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    val num = obj.optInt("number")
                    val offlineMatch = offlineSummaries.firstOrNull { it.number == num }
                    list.add(
                        QuranSurahSummary(
                            number = num,
                            arabicName = obj.optString("name"),
                            englishName = obj.optString("englishName"),
                            englishTranslation = obj.optString("englishNameTranslation"),
                            banglaName = offlineMatch?.banglaName ?: "সূরা ${obj.optString("englishName")}",
                            ayahCount = obj.optInt("numberOfAyahs"),
                            revelationType = obj.optString("revelationType", "Meccan")
                        )
                    )
                }
                list.takeIf { it.isNotEmpty() }
            }
        }.getOrNull()

        val result = remote ?: offlineSummaries
        if (remote != null) {
            cachedSurahList = remote
        }
        result
    }

    suspend fun getSurahDetail(surahNumber: Int): Result<QuranSurahDetail> = withContext(Dispatchers.IO) {
        cachedSurahDetails[surahNumber]?.let { return@withContext Result.success(it) }

        val offline = IslamicContentData.offlineSurahs.firstOrNull { it.summary.number == surahNumber }

        val remote = runCatching {
            val url =
                "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,bn.bengali,ar.alafasy"
            val req = Request.Builder().url(url).get().build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) error("HTTP ${resp.code}")
                val body = resp.body?.string() ?: error("Empty response")
                val root = JSONObject(body)
                val editions = root.optJSONArray("data") ?: error("Missing data")
                if (editions.length() < 3) error("Incomplete Quran editions")

                val arObj = editions.getJSONObject(0)
                val bnObj = editions.getJSONObject(1)
                val audioObj = editions.getJSONObject(2)

                val arAyahs = arObj.getJSONArray("ayahs")
                val bnAyahs = bnObj.getJSONArray("ayahs")
                val audioAyahs = audioObj.getJSONArray("ayahs")

                val summary = QuranSurahSummary(
                    number = arObj.optInt("number", surahNumber),
                    arabicName = arObj.optString("name"),
                    englishName = arObj.optString("englishName"),
                    englishTranslation = arObj.optString("englishNameTranslation"),
                    banglaName = offline?.summary?.banglaName ?: "সূরা ${arObj.optString("englishName")}",
                    ayahCount = arObj.optInt("numberOfAyahs", arAyahs.length()),
                    revelationType = arObj.optString("revelationType", "Meccan")
                )

                val ayahs = mutableListOf<QuranAyah>()
                for (i in 0 until arAyahs.length()) {
                    val aAr = arAyahs.getJSONObject(i)
                    val aBn = bnAyahs.optJSONObject(i)
                    val aAu = audioAyahs.optJSONObject(i)
                    val globalNum = aAr.optInt("number")
                    val audioUrl = aAu?.optString("audio")?.takeIf { it.isNotBlank() }
                        ?: "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$globalNum.mp3"
                    val offlineAyah = offline?.ayahs?.getOrNull(i)
                    ayahs.add(
                        QuranAyah(
                            numberInSurah = aAr.optInt("numberInSurah", i + 1),
                            globalNumber = globalNum,
                            arabicText = aAr.optString("text"),
                            banglaTranslation = aBn?.optString("text")
                                ?: offlineAyah?.banglaTranslation.orEmpty(),
                            transliteration = offlineAyah?.transliteration.orEmpty(),
                            audioUrl = audioUrl
                        )
                    )
                }

                QuranSurahDetail(
                    summary = summary,
                    ayahs = ayahs,
                    isOfflineVerified = offline != null
                )
            }
        }

        if (remote.isSuccess) {
            val detail = remote.getOrThrow()
            cachedSurahDetails[surahNumber] = detail
            Result.success(detail)
        } else if (offline != null) {
            Result.success(offline)
        } else {
            Result.failure(remote.exceptionOrNull() ?: IllegalStateException("Unable to load Surah"))
        }
    }
}
