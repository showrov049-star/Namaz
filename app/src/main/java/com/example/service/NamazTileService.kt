package com.example.service

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.R
import com.example.data.NamazRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NamazTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        super.onStartListening()
        syncTileState()
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val repo = NamazRepository(applicationContext)
            val current = repo.getSnapshot()
            if (current.isNamazModeActive) {
                repo.setActive(false, 0L)
                NamazForegroundService.stop(applicationContext)
                updateTile(false)
            } else {
                val endTime = if (current.autoTurnOff) {
                    System.currentTimeMillis() + current.durationMinutes * 60_000L
                } else {
                    0L
                }
                repo.setActive(true, endTime)
                NamazForegroundService.start(applicationContext)
                updateTile(true)
            }
        }
    }

    private fun syncTileState() {
        scope.launch {
            val isActive = NamazRepository(applicationContext).getSnapshot().isNamazModeActive
            updateTile(isActive)
        }
    }

    private fun updateTile(isActive: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        val icon = Icon.createWithResource(this, R.drawable.ic_namaz_tile)
        if (isActive) {
            icon.setTint(0xFF00C853.toInt())
        } else {
            icon.setTint(0xFF8E9B94.toInt())
        }
        tile.icon = icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = getString(
                if (isActive) R.string.tile_subtitle_on else R.string.tile_subtitle_off
            )
        }
        tile.updateTile()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun requestTileRefresh(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, NamazTileService::class.java)
            )
        }
    }
}
