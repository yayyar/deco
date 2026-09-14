package com.yayyar.deco.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.yayyar.deco.R

enum class ThemeSeed(
    val id: String,
    val titleRes: Int,
    val seedColor: Color,
    val previewColors: List<Color>
) {
    TEAL(
        id = "TEAL",
        titleRes = R.string.theme_seed_teal,
        seedColor = Color(0xFF0F766E),
        previewColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6), Color(0xFF0284C7), Color(0xFF10B981))
    ),
    BLUE(
        id = "BLUE",
        titleRes = R.string.theme_seed_blue,
        seedColor = Color(0xFF1D4ED8),
        previewColors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF06B6D4))
    ),
    INDIGO(
        id = "INDIGO",
        titleRes = R.string.theme_seed_indigo,
        seedColor = Color(0xFF4338CA),
        previewColors = listOf(Color(0xFF4338CA), Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))
    ),
    PURPLE(
        id = "PURPLE",
        titleRes = R.string.theme_seed_purple,
        seedColor = Color(0xFF6D28D9),
        previewColors = listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6), Color(0xFFD946EF), Color(0xFF3B82F6))
    ),
    ROSE(
        id = "ROSE",
        titleRes = R.string.theme_seed_rose,
        seedColor = Color(0xFFBE123C),
        previewColors = listOf(Color(0xFFBE123C), Color(0xFFF43F5E), Color(0xFFFB7185), Color(0xFFF59E0B))
    ),
    ORANGE(
        id = "ORANGE",
        titleRes = R.string.theme_seed_orange,
        seedColor = Color(0xFFC2410C),
        previewColors = listOf(Color(0xFFC2410C), Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFE11D48))
    ),
    EMERALD(
        id = "EMERALD",
        titleRes = R.string.theme_seed_emerald,
        seedColor = Color(0xFF047857),
        previewColors = listOf(Color(0xFF047857), Color(0xFF10B981), Color(0xFF34D399), Color(0xFF0D9488))
    ),
    SLATE(
        id = "SLATE",
        titleRes = R.string.theme_seed_slate,
        seedColor = Color(0xFF334155),
        previewColors = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFF0284C7))
    ),
    MONOCHROME(
        id = "MONOCHROME",
        titleRes = R.string.theme_seed_monochrome,
        seedColor = Color(0xFF18181B),
        previewColors = listOf(Color(0xFF18181B), Color(0xFF3F3F46), Color(0xFF71717A), Color(0xFFA1A1AA))
    );

    companion object {
        fun fromId(id: String): ThemeSeed {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: TEAL
        }
    }
}

enum class ThemeStyle(
    val id: String,
    val titleRes: Int,
    val descRes: Int
) {
    TONAL_SPOT(
        id = "TONAL_SPOT",
        titleRes = R.string.theme_style_tonal_spot,
        descRes = R.string.theme_style_tonal_spot_desc
    ),
    FIDELITY(
        id = "FIDELITY",
        titleRes = R.string.theme_style_fidelity,
        descRes = R.string.theme_style_fidelity_desc
    ),
    VIBRANT(
        id = "VIBRANT",
        titleRes = R.string.theme_style_vibrant,
        descRes = R.string.theme_style_vibrant_desc
    ),
    EXPRESSIVE(
        id = "EXPRESSIVE",
        titleRes = R.string.theme_style_expressive,
        descRes = R.string.theme_style_expressive_desc
    ),
    NEUTRAL(
        id = "NEUTRAL",
        titleRes = R.string.theme_style_neutral,
        descRes = R.string.theme_style_neutral_desc
    ),
    MONOCHROME(
        id = "MONOCHROME",
        titleRes = R.string.theme_style_monochrome,
        descRes = R.string.theme_style_monochrome_desc
    ),
    RAINBOW(
        id = "RAINBOW",
        titleRes = R.string.theme_style_rainbow,
        descRes = R.string.theme_style_rainbow_desc
    ),
    FRUIT_SALAD(
        id = "FRUIT_SALAD",
        titleRes = R.string.theme_style_fruit_salad,
        descRes = R.string.theme_style_fruit_salad_desc
    ),
    CONTENT(
        id = "CONTENT",
        titleRes = R.string.theme_style_content,
        descRes = R.string.theme_style_content_desc
    );

    companion object {
        fun fromId(id: String): ThemeStyle {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: TONAL_SPOT
        }
    }
}

object ThemePalette {

    fun generateColorScheme(seed: ThemeSeed, style: ThemeStyle, isDark: Boolean): ColorScheme {
        return if (isDark) {
            generateDarkScheme(seed, style)
        } else {
            generateLightScheme(seed, style)
        }
    }

    private fun generateLightScheme(seed: ThemeSeed, style: ThemeStyle): ColorScheme {
        val (primary, secondary, tertiary, container) = getLightPalette(seed, style)
        return lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = container,
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.copy(alpha = 0.15f),
            onSecondaryContainer = secondary,
            tertiary = tertiary,
            onTertiary = Color.White,
            background = BackgroundLight,
            surface = SurfaceLight,
            surfaceVariant = SurfaceCardLight,
            onSurface = Color(0xFF0F172A),
            onSurfaceVariant = Color(0xFF475569),
            error = AccentRed,
            onError = Color.White
        )
    }

    private fun generateDarkScheme(seed: ThemeSeed, style: ThemeStyle): ColorScheme {
        val (primary, secondary, tertiary, container) = getDarkPalette(seed, style)
        return darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF0B0F17),
            primaryContainer = container,
            onPrimaryContainer = Color.White,
            secondary = secondary,
            onSecondary = Color(0xFF0B0F17),
            secondaryContainer = secondary.copy(alpha = 0.2f),
            onSecondaryContainer = Color.White,
            tertiary = tertiary,
            onTertiary = Color(0xFF0B0F17),
            background = BackgroundDark,
            surface = SurfaceDark,
            surfaceVariant = SurfaceCardDark,
            onSurface = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF94A3B8),
            error = AccentRed,
            onError = Color.White
        )
    }

    private fun getLightPalette(seed: ThemeSeed, style: ThemeStyle): QuadColor {
        val base = when (seed) {
            ThemeSeed.TEAL -> QuadColor(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFFF59E0B), Color(0xFFCCFBF1))
            ThemeSeed.BLUE -> QuadColor(Color(0xFF1D4ED8), Color(0xFF0284C7), Color(0xFF7C3AED), Color(0xFFDBEAFE))
            ThemeSeed.INDIGO -> QuadColor(Color(0xFF4338CA), Color(0xFF6D28D9), Color(0xFFE11D48), Color(0xFFE0E7FF))
            ThemeSeed.PURPLE -> QuadColor(Color(0xFF6D28D9), Color(0xFFC026D3), Color(0xFF2563EB), Color(0xFFEDE9FE))
            ThemeSeed.ROSE -> QuadColor(Color(0xFFBE123C), Color(0xFFEA580C), Color(0xFF7C3AED), Color(0xFFFFE4E6))
            ThemeSeed.ORANGE -> QuadColor(Color(0xFFC2410C), Color(0xFFD97706), Color(0xFFBE123C), Color(0xFFFFEDD5))
            ThemeSeed.EMERALD -> QuadColor(Color(0xFF047857), Color(0xFF0F766E), Color(0xFFD97706), Color(0xFFD1FAE5))
            ThemeSeed.SLATE -> QuadColor(Color(0xFF334155), Color(0xFF0284C7), Color(0xFF64748B), Color(0xFFE2E8F0))
            ThemeSeed.MONOCHROME -> QuadColor(Color(0xFF18181B), Color(0xFF52525B), Color(0xFF71717A), Color(0xFFF4F4F5))
        }

        return when (style) {
            ThemeStyle.TONAL_SPOT -> base
            ThemeStyle.FIDELITY -> base.copy(
                primary = seed.seedColor,
                container = seed.seedColor.copy(alpha = 0.14f)
            )
            ThemeStyle.VIBRANT -> base.copy(
                primary = boostChroma(base.primary, 1.15f),
                secondary = Color(0xFF0284C7),
                tertiary = Color(0xFFEA580C)
            )
            ThemeStyle.EXPRESSIVE -> base.copy(
                secondary = Color(0xFFD946EF),
                tertiary = Color(0xFFF59E0B),
                container = base.primary.copy(alpha = 0.12f)
            )
            ThemeStyle.NEUTRAL -> base.copy(
                primary = muteColor(base.primary),
                secondary = Color(0xFF64748B),
                tertiary = Color(0xFF94A3B8),
                container = Color(0xFFF1F5F9)
            )
            ThemeStyle.MONOCHROME -> QuadColor(
                primary = Color(0xFF18181B),
                secondary = Color(0xFF3F3F46),
                tertiary = Color(0xFF71717A),
                container = Color(0xFFE4E4E7)
            )
            ThemeStyle.RAINBOW -> base.copy(
                secondary = Color(0xFF06B6D4),
                tertiary = Color(0xFFEC4899)
            )
            ThemeStyle.FRUIT_SALAD -> base.copy(
                secondary = Color(0xFFF43F5E),
                tertiary = Color(0xFF10B981)
            )
            ThemeStyle.CONTENT -> base.copy(
                container = base.primary.copy(alpha = 0.08f)
            )
        }
    }

    private fun getDarkPalette(seed: ThemeSeed, style: ThemeStyle): QuadColor {
        val base = when (seed) {
            ThemeSeed.TEAL -> QuadColor(Color(0xFF14B8A6), Color(0xFF38BDF8), Color(0xFFFBBF24), Color(0xFF115E59))
            ThemeSeed.BLUE -> QuadColor(Color(0xFF60A5FA), Color(0xFF38BDF8), Color(0xFFA78BFA), Color(0xFF1E40AF))
            ThemeSeed.INDIGO -> QuadColor(Color(0xFF818CF8), Color(0xFFA78BFA), Color(0xFFFB7185), Color(0xFF3730A3))
            ThemeSeed.PURPLE -> QuadColor(Color(0xFFA78BFA), Color(0xFFE879F9), Color(0xFF60A5FA), Color(0xFF5B21B6))
            ThemeSeed.ROSE -> QuadColor(Color(0xFFFB7185), Color(0xFFFB923C), Color(0xFFA78BFA), Color(0xFF9F1239))
            ThemeSeed.ORANGE -> QuadColor(Color(0xFFFB923C), Color(0xFFFBBF24), Color(0xFFFB7185), Color(0xFF9A3412))
            ThemeSeed.EMERALD -> QuadColor(Color(0xFF34D399), Color(0xFF2DD4BF), Color(0xFFFBBF24), Color(0xFF065F46))
            ThemeSeed.SLATE -> QuadColor(Color(0xFF94A3B8), Color(0xFF38BDF8), Color(0xFFCBD5E1), Color(0xFF1E293B))
            ThemeSeed.MONOCHROME -> QuadColor(Color(0xFFE4E4E7), Color(0xFFA1A1AA), Color(0xFF71717A), Color(0xFF27272A))
        }

        return when (style) {
            ThemeStyle.TONAL_SPOT -> base
            ThemeStyle.FIDELITY -> base.copy(
                primary = boostBrightness(seed.seedColor, 1.35f),
                container = seed.seedColor.copy(alpha = 0.35f)
            )
            ThemeStyle.VIBRANT -> base.copy(
                primary = boostBrightness(base.primary, 1.15f),
                secondary = Color(0xFF38BDF8),
                tertiary = Color(0xFFFB923C)
            )
            ThemeStyle.EXPRESSIVE -> base.copy(
                secondary = Color(0xFFE879F9),
                tertiary = Color(0xFFFBBF24),
                container = base.primary.copy(alpha = 0.25f)
            )
            ThemeStyle.NEUTRAL -> base.copy(
                primary = muteColor(base.primary),
                secondary = Color(0xFF94A3B8),
                tertiary = Color(0xFF64748B),
                container = Color(0xFF1E293B)
            )
            ThemeStyle.MONOCHROME -> QuadColor(
                primary = Color(0xFFF4F4F5),
                secondary = Color(0xFFA1A1AA),
                tertiary = Color(0xFF71717A),
                container = Color(0xFF27272A)
            )
            ThemeStyle.RAINBOW -> base.copy(
                secondary = Color(0xFF22D3EE),
                tertiary = Color(0xFFF472B6)
            )
            ThemeStyle.FRUIT_SALAD -> base.copy(
                secondary = Color(0xFFFB7185),
                tertiary = Color(0xFF34D399)
            )
            ThemeStyle.CONTENT -> base.copy(
                container = base.primary.copy(alpha = 0.2f)
            )
        }
    }

    private fun boostChroma(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    private fun boostBrightness(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    private fun muteColor(color: Color): Color {
        val avg = (color.red + color.green + color.blue) / 3f
        return Color(
            red = (color.red * 0.7f + avg * 0.3f).coerceIn(0f, 1f),
            green = (color.green * 0.7f + avg * 0.3f).coerceIn(0f, 1f),
            blue = (color.blue * 0.7f + avg * 0.3f).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    private data class QuadColor(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val container: Color
    )
}
