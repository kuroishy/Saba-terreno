package cl.duoc.sabaterreno.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.ui.theme.AmarilloDegradado
import cl.duoc.sabaterreno.ui.theme.RojoFueraServicio
import cl.duoc.sabaterreno.ui.theme.VerdeOperativo

fun colorDe(severidad: Severidad): Color = when (severidad) {
    Severidad.OPERATIVO -> VerdeOperativo
    Severidad.DEGRADADO -> AmarilloDegradado
    Severidad.FUERA_DE_SERVICIO -> RojoFueraServicio
}

/** Punto de color que acompaña siempre a un texto, nunca solo (accesibilidad). */
@Composable
fun PuntoEstado(severidad: Severidad, modifier: Modifier = Modifier, tamano: Int = 14) {
    val color by animateColorAsState(colorDe(severidad), tween(400), label = "colorEstado")
    Column(
        modifier
            .size(tamano.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(color)
            .clearAndSetSemantics { }
    ) {}
}

/**
 * Semáforo de tres luces. La luz roja late para llamar la atención, que es
 * retroalimentación visual, no decoración.
 */
@Composable
fun SemaforoVertical(severidad: Severidad, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "latido")
    val latido by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alfaLatido"
    )
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B121D))
            .padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(
            Severidad.FUERA_DE_SERVICIO to RojoFueraServicio,
            Severidad.DEGRADADO to AmarilloDegradado,
            Severidad.OPERATIVO to VerdeOperativo
        ).forEach { (cual, color) ->
            val encendida = severidad == cual
            val alfa = when {
                encendida && cual == Severidad.FUERA_DE_SERVICIO -> latido
                encendida -> 1f
                else -> 0.14f
            }
            Column(
                Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .alpha(alfa)
                    .background(color)
            ) {}
        }
    }
}

/**
 * Campo de texto que muestra el error **bajo el campo**, con ícono, en vez de
 * un aviso global. El mensaje viene del validador del dominio.
 */
@Composable
fun CampoValidado(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    apoyo: String? = null,
    lineasMaximas: Int = 1,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    opcionesTeclado: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default
) {
    Column(modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            label = { Text(etiqueta) },
            isError = error != null,
            singleLine = lineasMaximas == 1,
            maxLines = lineasMaximas,
            visualTransformation = visualTransformation,
            keyboardOptions = opcionesTeclado,
            modifier = Modifier.padding(bottom = 2.dp),
            supportingText = {
                if (error != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "  $error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (apoyo != null) {
                    Text(apoyo, style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}

/** Etiqueta de estado con fondo, siempre con texto además del color. */
@Composable
fun Pastilla(texto: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}
