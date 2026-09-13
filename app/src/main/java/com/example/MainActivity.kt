package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core.designsystem.JsAgentTheme
import com.example.ui.demo.DemoChallengeScreen
import com.example.ui.home.MainScreen
import com.example.ui.home.MainViewModel
import com.example.ui.settings.SettingsScreen

enum class AppDestination {
    HOME,
    SETTINGS,
    DEMO_PLAYGROUND
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDestination = when (intent?.getStringExtra("NAVIGATE_TO")) {
            "settings" -> AppDestination.SETTINGS
            "demo" -> AppDestination.DEMO_PLAYGROUND
            else -> AppDestination.HOME
        }

        setContent {
            JsAgentTheme {
                JsAgentApp(
                    viewModel = viewModel,
                    initialDestination = initialDestination,
                    onOpenOverlaySettings = { viewModel.requestOverlayPermission(this) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun JsAgentApp(
    viewModel: MainViewModel,
    initialDestination: AppDestination = AppDestination.HOME,
    onOpenOverlaySettings: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(initialDestination) }
    val agentState by viewModel.agentState.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibilityPermission by viewModel.hasAccessibilityPermission.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "screen_transition"
    ) { destination ->
        when (destination) {
            AppDestination.HOME -> {
                MainScreen(
                    agentState = agentState,
                    hasOverlayPermission = hasOverlayPermission,
                    hasAccessibilityPermission = hasAccessibilityPermission,
                    onToggleAgent = { viewModel.toggleAgent() },
                    onToggleFloatingAssistant = { enabled ->
                        viewModel.toggleFloatingAssistant(context, enabled)
                    },
                    onRequestOverlayPermission = onOpenOverlaySettings,
                    onRequestAccessibilityPermission = { viewModel.requestAccessibilityPermission(context) },
                    onTriggerInspection = { viewModel.triggerScreenInspection() },
                    onToggleDebugInspector = { enabled -> viewModel.toggleDebugInspector(enabled) },
                    onOpenSettings = { currentDestination = AppDestination.SETTINGS },
                    onOpenDemo = { currentDestination = AppDestination.DEMO_PLAYGROUND },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppDestination.SETTINGS -> {
                SettingsScreen(
                    isFloatingEnabled = agentState.isFloatingAssistantEnabled,
                    hasOverlayPermission = hasOverlayPermission,
                    hasAccessibilityPermission = hasAccessibilityPermission,
                    isDebugInspectorEnabled = agentState.isDebugInspectorEnabled,
                    onToggleFloating = { enabled ->
                        viewModel.toggleFloatingAssistant(context, enabled)
                    },
                    onRequestOverlayPermission = onOpenOverlaySettings,
                    onRequestAccessibilityPermission = { viewModel.requestAccessibilityPermission(context) },
                    onToggleDebugInspector = { enabled -> viewModel.toggleDebugInspector(enabled) },
                    onNavigateBack = { currentDestination = AppDestination.HOME },
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppDestination.DEMO_PLAYGROUND -> {
                DemoChallengeScreen(
                    agentState = agentState,
                    onToggleAgent = { viewModel.toggleAgent() },
                    onNavigateBack = { currentDestination = AppDestination.HOME },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
