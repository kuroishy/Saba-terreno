package cl.duoc.sabaterreno.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/** Paleta tomada de la señalética de estacionamiento: azul de señal y semáforo. */
private val AzulSenal = Color(0xFF1B4FD8)
private val AzulClaro = Color(0xFF7B9DFF)
private val Tinta = Color(0xFF101B2D)
private val TintaOscura = Color(0xFF070C14)
private val Superficie = Color(0xFFFFFFFF)
private val SuperficieOscura = Color(0xFF131D2D)
private val FondoClaro = Color(0xFFEDF0F5)
private val FondoOscuro = Color(0xFF0A111C)

val VerdeOperativo = Color(0xFF1FA85F)
val AmarilloDegradado = Color(0xFFF2B01E)
val RojoFueraServicio = Color(0xFFE5392E)

private val ClaroScheme = lightColorScheme(
    primary = AzulSenal,
    onPrimary = Color.White,
    secondary = Tinta,
    background = FondoClaro,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = Color(0xFFE3EAFB),
    onSurfaceVariant = Color(0xFF5A6779),
    outline = Color(0xFFD8DEE8),
    error = Color(0xFFB3261E)
)

private val OscuroScheme = darkColorScheme(
    primary = AzulClaro,
    onPrimary = FondoOscuro,
    secondary = Color(0xFFE8EDF5),
    background = FondoOscuro,
    onBackground = Color(0xFFE8EDF5),
    surface = SuperficieOscura,
    onSurface = Color(0xFFE8EDF5),
    surfaceVariant = Color(0xFF1B2A4D),
    onSurfaceVariant = Color(0xFF94A1B5),
    outline = Color(0xFF26344A),
    error = Color(0xFFFF8F86)
)

private val TipografiaSaba = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
)

@Composable
fun SabaTerrenoTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (oscuro) OscuroScheme else ClaroScheme
    val vista = LocalContext.current as? Activity
    SideEffect {
        vista?.window?.let { w ->
            WindowCompat.getInsetsController(w, w.decorView).isAppearanceLightStatusBars = !oscuro
        }
    }
    MaterialTheme(colorScheme = scheme, typography = TipografiaSaba, content = content)
}
