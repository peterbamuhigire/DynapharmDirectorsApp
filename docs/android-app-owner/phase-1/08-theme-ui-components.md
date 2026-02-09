# Phase 1 -- Section 08: Theme & UI Components

**Parent:** [Phase 1 README](./README.md) | [All Docs](../README.md)

---

## 1. Overview

Complete Material 3 theme for DynapharmOwner plus reusable composables for
Phase 1 screens (login, dashboard, placeholder tabs).

| Token | Hex | Usage |
|-------|-----|-------|
| Primary | `#2E7D32` | App bars, FABs, buttons |
| Primary Light | `#60AD5E` | Highlights, selected states |
| Primary Dark | `#005005` | Status bar, pressed states |
| Accent | `#F9A825` | Badges, alerts, accent elements |
| Error | `#D32F2F` | Destructive actions, validation |
| Info | `#1565C0` | Informational banners |

---

## 2. Color.kt

```kotlin
package com.dynapharm.owner.presentation.theme

import androidx.compose.ui.graphics.Color

// Brand
val GreenPrimary = Color(0xFF2E7D32); val GreenLight = Color(0xFF60AD5E)
val GreenDark = Color(0xFF005005); val GoldAccent = Color(0xFFF9A825)
val GoldLight = Color(0xFFFFD95A); val GoldDark = Color(0xFFC17900)

// Semantic
val Success = Color(0xFF2E7D32); val Warning = Color(0xFFF9A825)
val Error = Color(0xFFD32F2F); val ErrorLight = Color(0xFFEF5350)
val Info = Color(0xFF1565C0); val InfoLight = Color(0xFF42A5F5)

// Payment / approval status
val StatusPaid = Color(0xFF2E7D32); val StatusPartial = Color(0xFFF9A825)
val StatusUnpaid = Color(0xFFD32F2F); val StatusPending = Color(0xFF757575)
val StatusApproved = Color(0xFF2E7D32); val StatusRejected = Color(0xFFD32F2F)

// Neutrals
val White  = Color(0xFFFFFFFF); val Black  = Color(0xFF000000)
val Gray50 = Color(0xFFFAFAFA); val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE); val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD); val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575); val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242); val Gray900 = Color(0xFF212121)

// Light surfaces
val SurfaceLight = Color(0xFFFFFFFF); val BackgroundLight = Color(0xFFF8FAF8)
val OnSurfaceLight = Color(0xFF1C1B1F); val OnBackgroundLight = Color(0xFF1C1B1F)
val OutlineLight = Color(0xFF79747E); val SurfaceVariantLight = Color(0xFFE7E0EC)

// Dark surfaces
val SurfaceDark = Color(0xFF1C1B1F); val BackgroundDark = Color(0xFF121212)
val OnSurfaceDark = Color(0xFFE6E1E5); val OnBackgroundDark = Color(0xFFE6E1E5)
val OutlineDark = Color(0xFF938F99); val SurfaceVariantDark = Color(0xFF49454F)
```

---

## 3. Type.kt

```kotlin
package com.dynapharm.owner.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DynapharmFontFamily = FontFamily.Default // swap for branded font later

val DynapharmTypography = Typography(
    displayLarge  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
    displaySmall  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
    headlineLarge  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
    headlineSmall  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
    titleLarge  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontFamily = DynapharmFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)
```

---

## 4. Shape.kt

```kotlin
package com.dynapharm.owner.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DynapharmShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
```

---

## 5. Theme.kt

```kotlin
package com.dynapharm.owner.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary, onPrimary = White, primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark, secondary = GreenLight, onSecondary = White,
    secondaryContainer = Color(0xFFE8F5E9), onSecondaryContainer = GreenDark,
    tertiary = GoldAccent, onTertiary = Black, tertiaryContainer = GoldLight,
    onTertiaryContainer = GoldDark, error = Error, onError = White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
    background = BackgroundLight, onBackground = OnBackgroundLight,
    surface = SurfaceLight, onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight, onSurfaceVariant = Gray700,
    outline = OutlineLight, outlineVariant = Gray300)

private val DarkColorScheme = darkColorScheme(
    primary = GreenLight, onPrimary = GreenDark, primaryContainer = GreenPrimary,
    onPrimaryContainer = Color(0xFFC8E6C9), secondary = GreenLight, onSecondary = GreenDark,
    secondaryContainer = Color(0xFF1B5E20), onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = GoldLight, onTertiary = GoldDark, tertiaryContainer = GoldDark,
    onTertiaryContainer = GoldLight, error = ErrorLight, onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    background = BackgroundDark, onBackground = OnBackgroundDark,
    surface = SurfaceDark, onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark, onSurfaceVariant = Gray400,
    outline = OutlineDark, outlineVariant = Gray700)

@Composable
fun DynapharmOwnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = DynapharmTypography,
        shapes = DynapharmShapes, content = content)
}
```

---

## 6. StatCard.kt

KPI widget: icon, value, label, optional subtitle.

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(
    title: String, value: String, icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null, modifier: Modifier = Modifier
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(value, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(icon, title, tint = color, modifier = Modifier.size(32.dp))
            }
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
```

---

## 7. LoadingState.kt -- Shimmer placeholder

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(cardCount: Int = 4, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(cardCount) { ShimmerCard() }
    }
}

@Composable
private fun ShimmerCard(modifier: Modifier = Modifier) {
    val anim by rememberInfiniteTransition("shimmer").animateFloat(
        0f, 1000f, infiniteRepeatable(tween(1200), RepeatMode.Restart), "shimmer_x")
    val sv = MaterialTheme.colorScheme.surfaceVariant
    val brush = Brush.linearGradient(listOf(sv.copy(0.6f), sv.copy(0.2f), sv.copy(0.6f)),
        start = Offset(anim - 200f, 0f), end = Offset(anim, 0f))
    Column(modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
        ShimmerBox(brush, 0.4f, 12.dp); Spacer(Modifier.height(8.dp))
        ShimmerBox(brush, 0.6f, 24.dp); Spacer(Modifier.height(8.dp))
        ShimmerBox(brush, 0.3f, 10.dp)
    }
}

@Composable
private fun ShimmerBox(brush: Brush, w: Float, h: Dp) {
    Box(Modifier.fillMaxWidth(w).height(h).clip(MaterialTheme.shapes.small).background(brush))
}
```

---

## 8. ErrorState.kt -- Error message with optional retry

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
        Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onRetry != null) { Spacer(Modifier.height(24.dp)); Button(onClick = onRetry) { Text("Retry") } }
    }
}
```

---

## 9. EmptyState.kt -- Zero-items placeholder

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(title: String, message: String,
    icon: ImageVector = Icons.Outlined.Inbox, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
            modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

---

## 10. PlaceholderScreen.kt -- "Coming Soon" for non-dashboard tabs

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dynapharm.owner.presentation.theme.GoldAccent

@Composable
fun PlaceholderScreen(tabName: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(32.dp), Alignment.CenterHorizontally, Arrangement.Center) {
        Icon(Icons.Outlined.Construction, null, tint = GoldAccent, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text(tabName, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text("This feature is coming soon.\nStay tuned for updates.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

---

## 11. DynapharmLogo.kt -- Brand logo for login screen

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynapharm.owner.presentation.theme.GoldAccent
import com.dynapharm.owner.presentation.theme.GreenPrimary

@Composable
fun DynapharmLogo(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(buildAnnotatedString {
            withStyle(SpanStyle(color = GreenPrimary, fontWeight = FontWeight.Bold)) { append("DYNA") }
            withStyle(SpanStyle(color = GoldAccent, fontWeight = FontWeight.Bold)) { append("PHARM") }
        }, fontSize = 36.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(4.dp))
        Text("Owner Portal", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

---

## 12. PullToRefreshWrapper.kt -- M3 pull-to-refresh for scrollable content

```kotlin
package com.dynapharm.owner.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshWrapper(
    isRefreshing: Boolean, onRefresh: () -> Unit,
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()
    if (state.isRefreshing) { LaunchedEffect(Unit) { onRefresh() } }
    LaunchedEffect(isRefreshing) { if (!isRefreshing) state.endRefresh() }
    Box(modifier.nestedScroll(state.nestedScrollConnection)) {
        content()
        PullToRefreshContainer(state, Modifier.align(Alignment.TopCenter))
    }
}
```

---

## 13. Dark Mode & Preview

All components use `MaterialTheme.colorScheme` tokens (never raw constants except
`DynapharmLogo` and `PlaceholderScreen` brand colors). Light/dark swap automatically.
`dynamicColor` is off by default to preserve brand identity.

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StatCardPreview() {
    DynapharmOwnerTheme {
        StatCard(title = "Sales MTD", value = "KES 150,000",
            icon = Icons.Outlined.TrendingUp, subtitle = "75% of target")
    }
}
```

---

## 14. Cross-References

- [Architecture (presentation layer)](../sds/01-architecture.md) | [Gradle Compose](../sds/02-gradle-config.md) | [Hilt](../sds/03-hilt-modules.md) | [Phase 1 README](./README.md)
