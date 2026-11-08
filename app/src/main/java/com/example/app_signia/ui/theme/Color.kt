package com.example.app_signia.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ================================================================
// Paleta de Colores Oficial SIGNIA
// ================================================================
val SigniaDarkPurple = Color(0xFF81638B)     // #81638b - Púrpura Oscuro
val SigniaSoftLilac = Color(0xFFB695C0)      // #b695c0 - Lila Medio
val SigniaLightLavender = Color(0xFFDAC9DF) // #dac9df - Lavanda Claro de Fondo
val SigniaBluePrimary = Color(0xFF2196F3)    // #2196f3 - Azul Eléctrico
val SigniaBlueSecondary = Color(0xFF81C9FA)  // #81c9fa - Azul Celeste Secundario

// Colores complementarios
val SigniaWhite = Color(0xFFFFFFFF)
val SigniaDarkText = SigniaDarkPurple
val SigniaLightBg = SigniaLightLavender
val SigniaError = Color(0xFF2196F3)

// Colores para Modo Oscuro
val SigniaDarkBg = Color(0xFF1B1522)         // Fondo oscuro elegante
val SigniaDarkCardBg = Color(0xFF292033)     // Tarjeta en modo oscuro
val SigniaDarkInputBg = Color(0xFF332A3E)    // Campo de entrada modo oscuro
val SigniaDarkTextLight = Color(0xFFF3EDF6)  // Texto claro para modo oscuro
val SigniaDarkSubtext = Color(0xFFC7B8CE)    // Subtexto claro en modo oscuro

// ================================================================
// GESTOR DE TEMA (MODO OSCURO Y CLARO)
// Código simple y principiante para controlar la apariencia en toda la app
// ================================================================
object ThemeManager {
    // Estado del modo oscuro
    var isDarkMode by mutableStateOf(false)

    // Colores dinámicos adaptados al modo actual (Claro u Oscuro)
    val backgroundColor: Color
        get() = if (isDarkMode) SigniaDarkBg else SigniaLightLavender

    val cardBackgroundColor: Color
        get() = if (isDarkMode) SigniaDarkCardBg else Color.White

    val inputBackgroundColor: Color
        get() = if (isDarkMode) SigniaDarkInputBg else Color(0xFFEBF3FA)

    val textColor: Color
        get() = if (isDarkMode) SigniaDarkTextLight else SigniaDarkPurple

    val subtextColor: Color
        get() = if (isDarkMode) SigniaDarkSubtext else SigniaDarkPurple.copy(alpha = 0.7f)

    val topBarColor: Color
        get() = if (isDarkMode) SigniaDarkBg else SigniaLightLavender

    val bottomBarColor: Color
        get() = if (isDarkMode) SigniaDarkBg else SigniaLightLavender

    val heroCardColor: Color
        get() = if (isDarkMode) Color(0xFF3C2D4A) else SigniaDarkPurple
}

// Aliases para compatibilidad completa en toda la app
val HomeDarkPurple = SigniaDarkPurple
val HomeSoftLilac = SigniaSoftLilac
val HomeLightLavender = SigniaLightLavender
val HomeBluePrimary = SigniaBluePrimary
val HomeBlueSecondary = SigniaBlueSecondary

val SigniaPrimary = SigniaBluePrimary
val SigniaGreen = SigniaBluePrimary
val SigniaSoftGreen = SigniaSoftLilac
val SigniaBackground = SigniaLightLavender
val SigniaHomePrimary = SigniaDarkPurple
val SigniaHomeSoftGreen = SigniaSoftLilac
val SigniaHomeWhite = SigniaWhite
val SigniaHomeDarkText = SigniaDarkPurple
val SigniaHomeLightBg = SigniaLightLavender
val SigniaHomeStreakYellow = SigniaBlueSecondary
val SigniaHomeGrayText = Color(0xFF6B5872)
val SigniaHomeBorder = SigniaSoftLilac
val SigniaHomeError = SigniaError
val SigniaHomePrimaryDark = SigniaDarkPurple
val SigniaStreakYellow = SigniaBlueSecondary
val SigniaGrayText = Color(0xFF6B5872)
val SigniaLightGray = SigniaSoftLilac

// Colores de compatibilidad de tema Material 3
val Purple80 = SigniaSoftLilac
val PurpleGrey80 = SigniaLightLavender
val Pink80 = SigniaBlueSecondary

val Purple40 = SigniaDarkPurple
val PurpleGrey40 = SigniaSoftLilac
val Pink40 = SigniaBluePrimary