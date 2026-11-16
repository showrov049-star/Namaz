package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HapticIntensity
import com.example.data.NamazSettingsState
import com.example.util.FeedbackAction
import com.example.util.FeedbackManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NAMAZ", appName)
  }

  @Test
  fun `all ten sound raw resources are bundled`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val rawIds = listOf(
      R.raw.sfx_home_click,
      R.raw.sfx_namaz_on,
      R.raw.sfx_namaz_off,
      R.raw.sfx_settings_click,
      R.raw.sfx_toggle_switch,
      R.raw.sfx_mosque_finder,
      R.raw.sfx_marker_pop,
      R.raw.sfx_navigation_confirm,
      R.raw.sfx_save_success,
      R.raw.sfx_back_tap
    )
    assertEquals(10, rawIds.distinct().size)
    rawIds.forEach { resId ->
      context.resources.openRawResource(resId).use { stream ->
        assertTrue(stream.available() > 44)
      }
    }
  }

  @Test
  fun `feedback manager updates preferences and triggers safely`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = FeedbackManager(context)
    val defaults = NamazSettingsState()
    assertTrue(defaults.touchSoundsEnabled)
    assertTrue(defaults.hapticFeedbackEnabled)
    assertEquals(0.75f, defaults.soundVolume)
    assertEquals(HapticIntensity.MEDIUM, defaults.hapticIntensity)

    manager.updatePreferences(
      soundsEnabled = false,
      hapticsEnabled = true,
      volume = 0.4f,
      intensity = HapticIntensity.STRONG
    )
    assertEquals(false, manager.touchSoundsEnabled)
    assertEquals(0.4f, manager.soundVolume)
    assertEquals(HapticIntensity.STRONG, manager.hapticIntensity)

    FeedbackAction.entries.forEach { action ->
      manager.play(action)
    }
    manager.release()
  }
}
