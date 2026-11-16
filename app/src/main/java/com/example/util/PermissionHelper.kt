package com.example.util

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean {
        return hasFineLocationPermission(context) || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requiredLocationPermissions(): Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasCallPermissions(context: Context): Boolean {
        val readPhone = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        val answerCalls = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED
        } else false
        return readPhone && answerCalls
    }

    fun isCallScreeningRoleHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun createCallScreeningRoleIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return null
        return if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        } else null
    }

    fun requiredCallPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS
            )
        } else {
            arrayOf(Manifest.permission.READ_PHONE_STATE)
        }
}
