package com.example.core.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.core.agent.AgentEngine

/**
 * Manager handling overlay permissions and service lifecycle.
 */
object OverlayManager {

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun startOverlayService(context: Context) {
        if (!canDrawOverlays(context)) {
            return
        }
        AgentEngine.setFloatingAssistantEnabled(true)
        val serviceIntent = Intent(context, OverlayService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopOverlayService(context: Context) {
        AgentEngine.setFloatingAssistantEnabled(false)
        val serviceIntent = Intent(context, OverlayService::class.java)
        context.stopService(serviceIntent)
    }

    fun toggleOverlay(context: Context) {
        if (AgentEngine.state.value.isFloatingAssistantEnabled) {
            stopOverlayService(context)
        } else {
            if (canDrawOverlays(context)) {
                startOverlayService(context)
            } else {
                requestOverlayPermission(context)
            }
        }
    }
}
