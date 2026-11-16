package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CloudSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveUserPreferences_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = CloudSyncRepository(firestore, auth)

        val settings = NamazSettingsState(
            appLanguage = AppLanguage.BENGALI,
            durationMinutes = 20,
            calculationMethod = PrayerCalculationMethod.MWL
        )
        val tasbih = TasbihState(
            selectedZikrId = "alhamdulillah",
            dailyCount = 33,
            lifetimeCount = 150L,
            dailyGoal = 99,
            lastDateKey = "2026-11-16"
        )
        val notif = PrayerNotificationConfig(enabled = true, minutesBefore = 5, playSound = true)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveUserPreferences(settings, tasbih, setOf("dua_1"), notif, uid)
        }
        assertTrue("Expected saveUserPreferences to succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserPreferences(uid).first { it != null }
        }
        assertNotNull(observed)
        assertEquals("BENGALI", observed?.appLanguage)
        assertEquals(20L, observed?.durationMinutes)
        assertEquals("alhamdulillah", observed?.tasbihZikrId)

        // Update again to verify createdAt immutability passes
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveUserPreferences(
                settings.copy(appLanguage = AppLanguage.ENGLISH),
                tasbih.copy(dailyCount = 34),
                setOf("dua_1", "dua_2"),
                notif,
                uid
            )
        }
        assertTrue("Expected second saveUserPreferences update to succeed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)
    }

    @Test
    fun saveAndListPrayerLogs_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = CloudSyncRepository(firestore, auth)
        val dateKey = "2026-11-${UUID.randomUUID().toString().take(8)}"

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.savePrayerLog(dateKey, setOf(PrayerName.FAJR, PrayerName.MAGHRIB), uid)
        }
        assertTrue("Expected savePrayerLog to succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val logs = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePrayerLogs(uid).first { list -> list.any { it.id == dateKey } }
        }
        val doc = logs.first { it.id == dateKey }
        assertTrue(doc.prayersCompleted.contains("FAJR"))
        assertTrue(doc.prayersCompleted.contains("MAGHRIB"))
    }

    @Test
    fun getUserPreferences_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CloudSyncRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveUserPreferences(
                NamazSettingsState(),
                TasbihState(),
                emptySet(),
                PrayerNotificationConfig(),
                aliceUid
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = CloudSyncRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUserPreferences(aliceUid)
        }
        assertTrue(result.isFailure)
        val fsError = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException, got ${result.exceptionOrNull()}", fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun observePrayerLogs_unauthenticated_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = CloudSyncRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observePrayerLogs(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED exception for unauthenticated observer")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsError)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_namaz@test.com"
        const val BOB_EMAIL = "bob_namaz@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
