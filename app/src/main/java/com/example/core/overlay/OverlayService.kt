package com.example.core.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.core.agent.AgentEngine
import com.example.core.designsystem.JsAgentTheme
import com.example.ui.overlay.FloatingAgentGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Foreground Service that manages the unified Floating Agent Group (Bubble + Panel).
 *
 * Requirements enforced:
 * 1. Single Window Layout: Bubble and Panel are hosted inside one unified WindowManager window.
 * 2. Unified Coordinates: Bubble and Panel move together as one group; they cannot drift apart.
 * 3. Bubble on Top: Bubble is rendered above and in front of the top-left edge of the Panel (zIndex).
 * 4. Single Source of Truth: isPanelOpen is tracked reliably without double-toggle race conditions.
 * 5. Clean Dragging: Drag gestures originate exclusively from designated drag handles.
 * 6. Edge Clamping: The entire group is constrained inside visible screen boundaries.
 */
class OverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var windowManager: WindowManager? = null

    private var groupView: ComposeView? = null
    private var groupParams: WindowManager.LayoutParams? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private var isPanelOpen by mutableStateOf(false)

    companion object {
        const val CHANNEL_ID = "js_agent_overlay_channel"
        const val NOTIFICATION_ID = 2001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        createNotificationChannel()
        startForegroundWithNotification()

        setupFloatingGroupView()

        // Sync service lifecycle with AgentEngine state
        AgentEngine.state
            .onEach { state ->
                if (!state.isFloatingAssistantEnabled) {
                    stopSelf()
                }
            }
            .launchIn(serviceScope)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_service_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupFloatingGroupView() {
        val wm = windowManager ?: return

        lifecycleOwner = OverlayLifecycleOwner().apply { onCreate() }

        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val initialBubbleSizePx = (72 * density).toInt()

        val params = WindowManager.LayoutParams(
            initialBubbleSizePx,
            initialBubbleSizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (16 * density).toInt()
            y = (displayMetrics.heightPixels * 0.32f).toInt()
        }
        groupParams = params

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            lifecycleOwner?.let { owner ->
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
            }
            setContent {
                JsAgentTheme {
                    val agentState by AgentEngine.state.collectAsState()
                    FloatingAgentGroup(
                        agentState = agentState,
                        isPanelOpen = isPanelOpen,
                        onTogglePanel = { togglePanel() },
                        onGroupDrag = { dx, dy -> handleGroupDrag(dx, dy) },
                        onToggleAgentClick = { AgentEngine.toggleAgent() },
                        onToggleFloatingClick = { enabled ->
                            AgentEngine.setFloatingAssistantEnabled(enabled)
                            if (!enabled) {
                                isPanelOpen = false
                            }
                        }
                    )
                }
            }
        }

        groupView = composeView
        try {
            wm.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun togglePanel() {
        isPanelOpen = !isPanelOpen
        updateGroupWindowLayout()
    }

    private fun updateGroupWindowLayout() {
        val wm = windowManager ?: return
        val gv = groupView ?: return
        val gp = groupParams ?: return
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenSize = getScreenDimensions()

        if (isPanelOpen) {
            // Expanded to fit the Panel card and overlapping Bubble
            gp.width = (342 * density).toInt()
            gp.height = WindowManager.LayoutParams.WRAP_CONTENT
        } else {
            // Collapsed to fit just the Bubble and its glow
            val bubbleSizePx = (72 * density).toInt()
            gp.width = bubbleSizePx
            gp.height = bubbleSizePx
        }

        // Clamp inside screen bounds
        val maxX = (screenSize.x - gp.width).coerceAtLeast(0)
        val approxPanelHeight = (480 * density).toInt()
        val maxY = (screenSize.y - if (isPanelOpen) approxPanelHeight else gp.height).coerceAtLeast(0)

        gp.x = gp.x.coerceIn(0, maxX)
        gp.y = gp.y.coerceIn((36 * density).toInt(), maxY)

        try {
            wm.updateViewLayout(gv, gp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleGroupDrag(dx: Float, dy: Float) {
        val wm = windowManager ?: return
        val gv = groupView ?: return
        val gp = groupParams ?: return
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val screenSize = getScreenDimensions()

        val approxHeight = if (isPanelOpen) (480 * density).toInt() else gp.height
        val maxX = (screenSize.x - gp.width).coerceAtLeast(0)
        val maxY = (screenSize.y - approxHeight).coerceAtLeast(0)

        gp.x = (gp.x + dx.toInt()).coerceIn(0, maxX)
        gp.y = (gp.y + dy.toInt()).coerceIn((36 * density).toInt(), maxY)

        try {
            wm.updateViewLayout(gv, gp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getScreenDimensions(): Point {
        val displayMetrics = resources.displayMetrics
        return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        windowManager?.let { wm ->
            groupView?.let { gv ->
                if (gv.parent != null) {
                    try {
                        wm.removeView(gv)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        lifecycleOwner?.onDestroy()
        groupView = null
        groupParams = null
        lifecycleOwner = null

        AgentEngine.setFloatingAssistantEnabled(false)
    }
}
