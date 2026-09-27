package com.example.dfmtoy

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design tokens derived from DESIGN.md (Slacc-inspired, Slack-like analysis).
 * Deep aubergine primary, cream-lavender canvases, pastel-mesh gradient backdrop,
 * blue inline links, and over-padded pill CTAs at a 90px radius.
 */
object DesignTokens {

    object Colors {
        val primary = Color(0xFF4A154B)          // Aubergine — the single brand accent
        val primaryPress = Color(0xFF611F69)     // Pressed-state lift
        val onPrimary = Color(0xFFFFFFFF)
        val ink = Color(0xFF1D1D1D)              // primary body text
        val inkMute = Color(0xFF696969)          // secondary text / captions
        val linkBlue = Color(0xFF1264A3)         // inline links — the only non-aubergine accent
        val canvas = Color(0xFFFFFFFF)
        val canvasCream = Color(0xFFF4EDE4)      // warm off-white
        val canvasLavender = Color(0xFFF9F0FF)   // pale lavender — secondary button surface
        val surfaceAubergine = Color(0xFF4A154B) // aubergine reused as a surface (band, footer)
        val hairline = Color(0xFFE6E6E6)
        val onAubergineMute = Color(0xFFD9BDDE)  // muted-light text on aubergine

        // Pastel-mesh gradient stops (the brand's "depth without shadows")
        val meshPeach = Color(0xFFFFF0E6)
        val meshLavender = Color(0xFFE9D8FF)
        val meshGreen = Color(0xFFDCEBDD)
    }

    object Spacing {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
        val huge = 28.dp
    }

    object Radius {
        val sm = 4.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val pill = 90.dp
    }

    // Typography — tight negative tracking on display, relaxed 1.55 body leading.
    object Type {
        val displayHeroSize = 48.sp            // country name hero (display-lg scale)
        val displayHeroTracking = (-0.6).sp
        val displayWeight = FontWeight.Bold    // display tier is 700

        val headingLgSize = 24.sp
        val headingTracking = (-0.096).sp

        val bodyLgSize = 18.sp                 // marketing lead
        val bodyLgTracking = (-0.0216).sp

        val bodyMdSize = 16.sp                 // default UI body

        val buttonMdSize = 16.sp
        val buttonTracking = 0.2.sp
        val buttonWeight = FontWeight.Bold

        val microCapSize = 12.sp               // all-caps eyebrow
        val microCapTracking = 0.96.sp

        val captionSize = 14.sp
        val captionTracking = 0.1.sp
    }

    val centerText = TextAlign.Center
}
