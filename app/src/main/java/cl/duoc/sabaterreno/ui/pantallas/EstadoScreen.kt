package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.Ronda
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.rules.Semaforo
import cl.duoc.sabaterreno.ui.componentes.Pastilla
import cl.duoc.sabaterreno.ui.componentes.PuntoEstado
import cl.duoc.sabaterreno.ui.componentes.SemaforoVertical
import cl.duoc.sabaterreno.ui.componentes.colorDe

@Composable
fun EstadoScreen(
    tickets: List<Ticket>,
    parkingId: String,
    equiposVisibles: List<Equipo>,
    puedeReportar: Boolean,
    ultimaRonda: Ronda?,
    alReportar: () -> Unit,
    alIniciarRonda: () -> Unit,
    alAbrirEquipo: (Equipo) -> Unit
) {
    val estado = Semaforo.estadoParking(tickets, parkingId, equiposVisibles)
    val mensaje = when (estado) {
        Severidad.OPERATIVO -> "Todo funciona con normalidad."
        Severidad.DEGRADADO -> "Hay equipos con fallas parciales."
        Severidad.FUERA_DE_SERVICIO -> "Hay equipos que no funcionan."
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SemaforoVertical(estado)
                    // El titular cambia con un cruce suave al cambiar el estado.
                    AnimatedContent(
                        targetState = estado,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "estadoParking"
                    ) { actual ->
                        Column {
                            Text(
                                actual.etiqueta,
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorDe(actual)
                            )
                            Text(
                                mensaje,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (puedeReportar) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = alIniciarRonda,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null)
                        Text("  Iniciar ronda de turno", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = alReportar,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("  Reportar una falla")
                    }
                    ultimaRonda?.let { ronda ->
                        Text(
                            "Última ronda: ${fechaCorta(ronda.instante)} · ${ronda.autor} · " +
                                cantidad(ronda.conformes, "conforme", "conformes") + ", " +
                                cantidad(ronda.observados, "observado", "observados"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        items(equiposVisibles, key = { it.id }) { equipo ->
            val sev = Semaforo.estadoEquipo(tickets, parkingId, equipo.id)
            val hayTicket = tickets.any { it.equipoId == equipo.id && it.parkingId == parkingId && it.abierto }
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hayTicket || puedeReportar) { alAbrirEquipo(equipo) }
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PuntoEstado(sev)
                    Text(equipo.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (sev == Severidad.OPERATIVO) {
                        Text(
                            sev.etiqueta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Pastilla(sev.etiqueta, colorDe(sev))
                    }
                    if (hayTicket || puedeReportar) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
