package com.example.dfmtoy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * The whole trick lives in a single line:
 *
 *     splitInstallManager.installedModules
 *
 * We detect the country purely by reading WHICH Dynamic Feature Module is installed.
 * If countrycodekr is present -> Korea, if countrycodejp -> Japan.
 *
 * The modules contain zero code. "The fact that it is installed" is itself the data.
 *
 * UI follows DESIGN.md (Slacc-inspired): a pastel-mesh gradient backdrop, a deep
 * aubergine display headline, a floating white card, and over-padded pill CTAs.
 */
class MainActivity : ComponentActivity() {

    private lateinit var manager: SplitInstallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = SplitInstallManagerFactory.create(this)

        setContent {
            PastelMeshBackdrop {
                CountryScreen(manager)
            }
        }
    }
}

/** The brand's signature depth: peach + lavender + dusty-green wash over cream. */
@Composable
private fun PastelMeshBackdrop(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.Colors.canvasCream)
    ) {
        // Lavender wash from the top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(DesignTokens.Colors.meshLavender, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 1400f,
                    )
                )
        )
        // Peach wash from the top-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(DesignTokens.Colors.meshPeach, Color.Transparent),
                        center = Offset(1200f, 200f),
                        radius = 1100f,
                    )
                )
        )
        // Dusty-green wash from the bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(DesignTokens.Colors.meshGreen, Color.Transparent),
                        center = Offset(300f, 2400f),
                        radius = 1300f,
                    )
                )
        )
        content()
    }
}

@Composable
private fun CountryScreen(manager: SplitInstallManager) {
    // Screen state
    var installed by remember { mutableStateOf(manager.installedModules) }
    var statusText by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) } // 0f..1f, hidden when 0f

    fun refresh() {
        installed = manager.installedModules
    }

    // Register/unregister the install progress listener
    DisposableEffect(manager) {
        val listener = SplitInstallStateUpdatedListener { state ->
            statusText = when (state.status()) {
                SplitInstallSessionStatus.PENDING -> "Pending"
                SplitInstallSessionStatus.DOWNLOADING -> {
                    val total = state.totalBytesToDownload()
                    progress = if (total > 0) state.bytesDownloaded().toFloat() / total else 0f
                    "Downloading ${(progress * 100).toInt()}%"
                }
                SplitInstallSessionStatus.INSTALLING -> "Installing"
                SplitInstallSessionStatus.INSTALLED -> {
                    progress = 0f
                    refresh()
                    "Installed"
                }
                SplitInstallSessionStatus.FAILED -> {
                    progress = 0f
                    "Failed (code ${state.errorCode()})"
                }
                SplitInstallSessionStatus.CANCELED -> {
                    progress = 0f
                    "Canceled"
                }
                else -> "Status: ${state.status()}"
            }
        }
        manager.registerListener(listener)
        onDispose { manager.unregisterListener(listener) }
    }

    // Detect the country from the installed modules
    val country = when {
        "countrycodekr" in installed -> "🇰🇷  Korea"
        "countrycodejp" in installed -> "🇯🇵  Japan"
        else -> "Unknown"
    }
    val loginHint = when {
        "countrycodekr" in installed -> "Phone number login"
        "countrycodejp" in installed -> "LINE login"
        else -> "No country module installed yet"
    }

    fun install(module: String) {
        if (module in manager.installedModules) {
            refresh(); return
        }
        statusText = "Requesting…"
        val request = SplitInstallRequest.newBuilder().addModule(module).build()
        manager.startInstall(request)
            .addOnSuccessListener { refresh() }
            .addOnFailureListener { statusText = "Install failed: ${it.message}" }
    }

    // Note: DFM has no immediate uninstall API (deferredUninstall is handled later by Play).
    // To reset locally, run: adb uninstall com.example.dfmtoy

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignTokens.Spacing.xxl, vertical = DesignTokens.Spacing.huge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // All-caps eyebrow
        Text(
            text = "DETECTED COUNTRY",
            color = DesignTokens.Colors.primary,
            fontSize = DesignTokens.Type.microCapSize,
            fontWeight = DesignTokens.Type.buttonWeight,
            letterSpacing = DesignTokens.Type.microCapTracking,
        )

        // Aubergine display hero — the country name
        Text(
            text = country,
            color = DesignTokens.Colors.primary,
            fontSize = DesignTokens.Type.displayHeroSize,
            fontWeight = DesignTokens.Type.displayWeight,
            letterSpacing = DesignTokens.Type.displayHeroTracking,
            textAlign = DesignTokens.centerText,
            modifier = Modifier.padding(top = DesignTokens.Spacing.md),
        )

        // Body lead tagline
        Text(
            text = loginHint,
            color = DesignTokens.Colors.inkMute,
            fontSize = DesignTokens.Type.bodyLgSize,
            letterSpacing = DesignTokens.Type.bodyLgTracking,
            textAlign = DesignTokens.centerText,
            modifier = Modifier.padding(top = DesignTokens.Spacing.sm),
        )

        // Floating white card — installed modules + progress
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DesignTokens.Spacing.huge),
            shape = RoundedCornerShape(DesignTokens.Radius.xl),
            colors = CardDefaults.cardColors(containerColor = DesignTokens.Colors.canvas),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.xxl),
            ) {
                Text(
                    text = "Installed modules",
                    color = DesignTokens.Colors.ink,
                    fontSize = DesignTokens.Type.captionSize,
                    fontWeight = DesignTokens.Type.buttonWeight,
                    letterSpacing = DesignTokens.Type.captionTracking,
                )
                Text(
                    text = if (installed.isEmpty()) "[ ]" else installed.toList().toString(),
                    color = DesignTokens.Colors.inkMute,
                    fontSize = DesignTokens.Type.bodyMdSize,
                    modifier = Modifier.padding(top = DesignTokens.Spacing.xs),
                )
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        color = DesignTokens.Colors.primary,
                        fontSize = DesignTokens.Type.captionSize,
                        fontWeight = DesignTokens.Type.buttonWeight,
                        modifier = Modifier.padding(top = DesignTokens.Spacing.md),
                    )
                }
                if (progress > 0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = DesignTokens.Colors.primary,
                        trackColor = DesignTokens.Colors.hairline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DesignTokens.Spacing.sm),
                    )
                }
            }
        }

        // Primary aubergine pill (the one filled CTA)
        AuberginePill(
            text = "Install countrycodekr (act as Korea)",
            onClick = { install("countrycodekr") },
            modifier = Modifier.padding(top = DesignTokens.Spacing.xl),
        )
        // Secondary lavender pill
        LavenderPill(
            text = "Install countrycodejp (act as Japan)",
            onClick = { install("countrycodejp") },
            modifier = Modifier.padding(top = DesignTokens.Spacing.md),
        )
        // Outline aubergine pill
        OutlinePill(
            text = "Refresh (re-detect country)",
            onClick = { refresh() },
            modifier = Modifier.padding(top = DesignTokens.Spacing.md),
        )
    }
    }
}

@Composable
private fun AuberginePill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(DesignTokens.Radius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignTokens.Colors.primary,
            contentColor = DesignTokens.Colors.onPrimary,
        ),
    ) {
        Text(
            text = text,
            fontSize = DesignTokens.Type.buttonMdSize,
            fontWeight = DesignTokens.Type.buttonWeight,
            letterSpacing = DesignTokens.Type.buttonTracking,
        )
    }
}

@Composable
private fun LavenderPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(DesignTokens.Radius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = DesignTokens.Colors.canvasLavender,
            contentColor = DesignTokens.Colors.ink,
        ),
    ) {
        Text(
            text = text,
            fontSize = DesignTokens.Type.buttonMdSize,
            fontWeight = DesignTokens.Type.buttonWeight,
            letterSpacing = DesignTokens.Type.buttonTracking,
        )
    }
}

@Composable
private fun OutlinePill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(DesignTokens.Radius.pill),
        border = BorderStroke(2.dp, DesignTokens.Colors.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DesignTokens.Colors.primary,
        ),
    ) {
        Text(
            text = text,
            fontSize = DesignTokens.Type.buttonMdSize,
            fontWeight = DesignTokens.Type.buttonWeight,
            letterSpacing = DesignTokens.Type.buttonTracking,
        )
    }
}
