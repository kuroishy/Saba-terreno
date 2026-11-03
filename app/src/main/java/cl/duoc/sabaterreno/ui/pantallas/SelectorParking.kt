package cl.duoc.sabaterreno.ui.pantallas

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.Parking
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.rules.Semaforo
import cl.duoc.sabaterreno.ui.componentes.PuntoEstado

/**
 * Elección del parking. El punto de color usa los equipos **visibles** para el
 * rol, de modo que la lista y la pantalla de estado nunca se contradigan.
 */
@Composable
fun SelectorParking(
    nombre: String,
    parkings: List<Parking>,
    tickets: List<Ticket>,
    equiposVisibles: List<Equipo>,
    alElegir: (Parking) -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(Modifier.padding(top = 28.dp, bottom = 8.dp)) {
                Text("Hola, $nombre", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Elige dónde vas a trabajar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(parkings, key = { it.id }) { parking ->
            val estado = Semaforo.estadoParking(tickets, parking.id, equiposVisibles)
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { alElegir(parking) }
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PuntoEstado(estado)
                    Column {
                        Text(parking.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${parking.comuna} · ${estado.etiqueta}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
