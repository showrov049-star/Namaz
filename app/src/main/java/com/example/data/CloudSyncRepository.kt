package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class UserPreferencesDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val appLanguage: String = AppLanguage.ENGLISH.name,
    val voiceLanguage: String = VoiceLanguage.BENGALI.name,
    val durationMinutes: Long = 15L,
    val autoTurnOff: Boolean = true,
    val autoCallResponseEnabled: Boolean = false,
    val calculationMethod: String = PrayerCalculationMethod.KARACHI.name,
    val asrSchool: String = AsrJuristicSchool.HANAFI.name,
    val touchSoundsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundVolume: Double = 0.75,
    val hapticIntensity: String = HapticIntensity.MEDIUM.name,
    val tasbihZikrId: String = "subhanallah",
    val tasbihDailyCount: Long = 0L,
    val tasbihLifetimeCount: Long = 0L,
    val tasbihDailyGoal: Long = 100L,
    val tasbihDateKey: String = "",
    val duaBookmarks: List<String> = emptyList(),
    val notifEnabled: Boolean = false,
    val notifMinutesBefore: Long = 0L,
    val notifPlaySound: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PrayerLogDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val prayersCompleted: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = FirebaseAuth.getInstance()
    )

    private fun requireUserId(): String =
        checkNotNull(auth.currentUser?.uid) { "User must be signed in before accessing cloud sync" }

    fun observeUserPreferences(userId: String = requireUserId()): Flow<UserPreferencesDoc?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots().map { snapshot ->
            if (!snapshot.exists()) {
                null
            } else {
                snapshot.toObject(
                    UserPreferencesDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        }
    }

    suspend fun getUserPreferences(userId: String = requireUserId()): Result<UserPreferencesDoc?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val doc = if (snapshot.exists()) {
                snapshot.toObject(
                    UserPreferencesDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else null
            Result.success(doc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun saveUserPreferences(
        settings: NamazSettingsState,
        tasbih: TasbihState,
        bookmarks: Set<String>,
        notifConfig: PrayerNotificationConfig,
        userId: String = requireUserId()
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val existingSnap = docRef.get().await()
            val existingCreatedAt = if (existingSnap.exists()) {
                existingSnap.getTimestamp("createdAt")
            } else null

            val payload = UserPreferencesDoc(
                id = userId,
                userId = userId,
                appLanguage = settings.appLanguage.name,
                voiceLanguage = settings.voiceLanguage.name,
                durationMinutes = settings.durationMinutes.toLong(),
                autoTurnOff = settings.autoTurnOff,
                autoCallResponseEnabled = settings.autoCallResponseEnabled,
                calculationMethod = settings.calculationMethod.name,
                asrSchool = settings.asrSchool.name,
                touchSoundsEnabled = settings.touchSoundsEnabled,
                hapticFeedbackEnabled = settings.hapticFeedbackEnabled,
                soundVolume = settings.soundVolume.coerceIn(0f, 1f).toDouble(),
                hapticIntensity = settings.hapticIntensity.name,
                tasbihZikrId = tasbih.selectedZikrId.take(64),
                tasbihDailyCount = tasbih.dailyCount.coerceAtLeast(0).toLong(),
                tasbihLifetimeCount = tasbih.lifetimeCount.coerceAtLeast(0L),
                tasbihDailyGoal = tasbih.dailyGoal.coerceIn(1, 100_000).toLong(),
                tasbihDateKey = tasbih.lastDateKey.take(32),
                duaBookmarks = bookmarks.take(50),
                notifEnabled = notifConfig.enabled,
                notifMinutesBefore = notifConfig.minutesBefore.coerceIn(0, 120).toLong(),
                notifPlaySound = notifConfig.playSound,
                createdAt = existingCreatedAt,
                updatedAt = null
            )

            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val op = OperationType.WRITE
            handleFirestoreError(e, op, docRef.path, auth)
            Result.failure(e)
        }
    }

    fun observePrayerLogs(userId: String = requireUserId()): Flow<List<PrayerLogDoc>> {
        val colRef = db.collection("users").document(userId).collection("prayerLogs")
        return colRef.snapshots().map { querySnapshot ->
            querySnapshot.toObjects(
                PrayerLogDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
        }
    }

    suspend fun getPrayerLogs(userId: String = requireUserId()): Result<List<PrayerLogDoc>> {
        val colRef = db.collection("users").document(userId).collection("prayerLogs")
        return try {
            val snapshot = colRef.get().await()
            Result.success(
                snapshot.toObjects(
                    PrayerLogDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, colRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun savePrayerLog(
        dateKey: String,
        completed: Set<PrayerName>,
        userId: String = requireUserId()
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
            .collection("prayerLogs").document(dateKey)
        return try {
            val existingSnap = docRef.get().await()
            val existingCreatedAt = if (existingSnap.exists()) {
                existingSnap.getTimestamp("createdAt")
            } else null

            val doc = PrayerLogDoc(
                id = dateKey,
                userId = userId,
                prayersCompleted = completed.map { it.name }.take(5),
                createdAt = existingCreatedAt,
                updatedAt = null
            )
            docRef.set(doc).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }
}
