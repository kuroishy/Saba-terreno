package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.ui.componentes.Pastilla
import cl.duoc.sabaterreno.ui.componentes.PuntoEstado
import cl.duoc.sabaterreno.ui.componentes.colorDe
import cl.duoc.sabaterreno.ui.theme.VerdeOperativo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "1 observado" / "3 observados": concuerda el sustantivo con la cantidad. */
fun cantidad(n: Int, singular: String, plural: String): String =
    "$n ${if (n == 1) singular else plural}"

fun fechaCorta(instante: Long): String =
    SimpleDateFormat("dd MMM, HH:mm", Locale("es", "CL")).format(Date(instante))

@Composable
fun IncidenciasScreen(tickets: List<Ticket>, alAbrir: (Ticket) -> Unit) {
    var verCerradas by remember { mutableStateOf(false) }
    val abiertas = tickets.filter { it.abierto }
    val cerradas = tickets.filter { !it.abierto }
    val lista = if (verCerradas) cerradas else abiertas

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Incidencias",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !verCerradas,
                    onClick = { verCerradas = false },
                    label = { Text("Abiertas · ${abiertas.size}") }
                )
                FilterChip(
                    selected = verCerradas,
                    onClick = { verCerradas = true },
                    label = { Text("Cerradas · ${cerradas.size}") }
                )
            }
        }
        if (lista.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        if (verCerradas) "Aún no hay incidencias cerradas."
                        else "No hay incidencias abiertas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
        items(lista, key = { it.id }) { ticket ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { alAbrir(ticket) }
            ) {
                Row(
                    Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    PuntoEstado(ticket.severidad, Modifier.padding(top = 4.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            Catalogo.equipo(ticket.equipoId)?.nombre ?: ticket.equipoId,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            ticket.falla,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val color = if (ticket.estado == EstadoTicket.CERRADO) VerdeOperativo
                            else colorDe(ticket.severidad)
                            Pastilla(ticket.estado.etiqueta, color)
                            Text(
                                fechaCorta(ticket.creado),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
