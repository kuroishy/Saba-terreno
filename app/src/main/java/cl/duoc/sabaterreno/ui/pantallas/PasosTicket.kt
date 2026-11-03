package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.ui.theme.VerdeOperativo

/**
 * Línea de pasos del ciclo de vida de la incidencia. El paso actual se destaca
 * con un halo y la barra de avance crece animada al cambiar de estado.
 */
@Composable
fun PasosTicket(estado: EstadoTicket, modifier: Modifier = Modifier) {
    val etiquetas = listOf(
        "Detección",
        "Reporte",
        if (estado == EstadoTicket.EN_INTERVENCION) "Intervención" else "Escalado",
        "Cierre"
    )
    // Detección y Reporte siempre ocurrieron; el tercero depende del estado.
    val pasoActual = when (estado) {
        EstadoTicket.ESCALADO -> 3
        EstadoTicket.EN_INTERVENCION -> 3
        EstadoTicket.CERRADO -> 4
    }
    val avance by animateFloatAsState(
        targetValue = (pasoActual - 1).toFloat() / (etiquetas.size - 1),
        animationSpec = tween(500),
        label = "avancePasos"
    )

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Box(
                Modifier
                    .fillMaxWidth(avance)
                    .height(3.dp)
                    .align(Alignment.CenterStart)
                    .background(VerdeOperativo)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                etiquetas.forEachIndexed { indice, _ ->
                    val numero = indice + 1
                    val completado = numero < pasoActual || estado == EstadoTicket.CERRADO
                    val actual = numero == pasoActual && estado != EstadoTicket.CERRADO
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    completado -> VerdeOperativo
                                    actual -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(
                                2.dp,
                                if (actual) MaterialTheme.colorScheme.primary
                                else if (completado) VerdeOperativo
                                else MaterialTheme.colorScheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (completado) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "$numero",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (actual) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            etiquetas.forEach { etiqueta ->
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}
