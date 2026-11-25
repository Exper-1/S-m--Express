package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SemeLightColorScheme = lightColorScheme(
    primary = MidnightNavy,
    onPrimary = PureWhite,
    primaryContainer = Ivory,
    onPrimaryContainer = MidnightNavy,
    secondary = TurquoiseGreen,
    onSecondary = PureWhite,
    secondaryContainer = TurquoiseContainer,
    onSecondaryContainer = MidnightNavyDeep,
    tertiary = SoftGold,
    onTertiary = MidnightNavyDeep,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = MidnightNavyDeep,
    background = Ivory,
    onBackground = MidnightNavyDeep,
    surface = PureWhite,
    onSurface = MidnightNavyDeep,
    surfaceVariant = LightGray,
    onSurfaceVariant = SlateText,
    outline = BorderHairline,
    error = ErrorCrimson,
    onError = PureWhite,
    errorContainer = ErrorSoftContainer,
    onErrorContainer = ErrorCrimson
)

private val SemeDarkColorScheme = darkColorScheme(
    primary = SoftGold,
    onPrimary = MidnightNavyDeep,
    primaryContainer = MidnightNavySoft,
    onPrimaryContainer = Ivory,
    secondary = TurquoiseGreen,
    onSecondary = PureWhite,
    secondaryContainer = TurquoiseContainer,
    onSecondaryContainer = MidnightNavyDeep,
    tertiary = SoftGold,
    onTertiary = MidnightNavyDeep,
    background = MidnightNavyDeep,
    onBackground = Ivory,
    surface = MidnightNavy,
    onSurface = PureWhite,
    surfaceVariant = MidnightNavySoft,
    onSurfaceVariant = Ivory,
    outline = MidnightNavySoft,
    error = ErrorSoftContainer,
    onError = ErrorCrimson
)

val SemeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SemeExpressTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SemeDarkColorScheme else SemeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SemeShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SemeExpressTheme(darkTheme = darkTheme, content = content)
}
