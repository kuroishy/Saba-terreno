package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import cl.duoc.sabaterreno.ui.componentes.Lado
import cl.duoc.sabaterreno.ui.componentes.MazoDeslizable
import cl.duoc.sabaterreno.ui.theme.AmarilloDegradado
import cl.duoc.sabaterreno.ui.theme.VerdeOperativo

@Composable
fun RondaScreen(
    vm: RondaViewModel,
    parkingId: String,
    autor: String,
    alReportar: (Equipo) -> Unit,
    alTerminar: () -> Unit
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    // Descarte pedido desde los botones; el mazo lo anima igual que el gesto.
    var forzar by remember { mutableStateOf<Lado?>(null) }

    LaunchedEffect(estado.terminada) {
        if (estado.terminada) vm.guardar(parkingId, autor)
    }

    if (estado.terminada) {
        ResumenRonda(estado, alReportar, alTerminar)
        return
    }

    val progreso by animateFloatAsState(estado.progreso, tween(350), label = "progresoRonda")

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Ronda de turno", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Equipo ${estado.indice + 1} de ${estado.total}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )
        }

        Box(Modifier.fillMaxWidth().height(340.dp)) {
            MazoDeslizable(
                elementos = estado.equipos,
                indice = estado.indice,
                etiquetaIzquierda = "Observado",
                etiquetaDerecha = "Conforme",
                colorIzquierda = AmarilloDegradado,
                colorDerecha = VerdeOperativo,
                forzar = forzar,
                forzado = { forzar = null },
                alDescartar = { indice, lado ->
                    val equipo = estado.equipos.getOrNull(indice) ?: return@MazoDeslizable
                    vm.responder(
                        equipo.id,
                        if (lado == Lado.DERECHA) ResultadoRonda.CONFORME else ResultadoRonda.OBSERVADO
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) { equipo ->
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        equipo.tipo.etiqueta.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        equipo.nombre,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "¿Está operando con normalidad?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // El gesto no es la única vía: los mismos resultados por botón.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedIconButton(
                    onClick = { forzar = Lado.IZQUIERDA },
                    modifier = Modifier.size(66.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Marcar como observado", tint = AmarilloDegradado)
                }
                Text("Observado", style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = vm::deshacer, enabled = estado.puedeDeshacer) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                Text("  Deshacer")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedIconButton(
                    onClick = { forzar = Lado.DERECHA },
                    modifier = Modifier.size(66.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Marcar como conforme", tint = VerdeOperativo)
                }
                Text("Conforme", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (estado.indice == 0) {
            Text(
                "Desliza la tarjeta o usa los botones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResumenRonda(
    estado: RondaUiState,
    alReportar: (Equipo) -> Unit,
    alTerminar: () -> Unit
) {
    val observados = estado.observados
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (observados.isEmpty()) VerdeOperativo else AmarilloDegradado),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text("Ronda completa", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "${cantidad(estado.conformes, "conforme", "conformes")} · " +
                        cantidad(observados.size, "observado", "observados"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (estado.guardada) {
                    Text(
                        "Guardada en el dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (observados.isNotEmpty()) {
            Text("Equipos observados", style = MaterialTheme.typography.titleLarge)
            Text(
                "Reporta cada uno para que el SAU quede al tanto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            observados.forEach { equipo ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            equipo.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { alReportar(equipo) }) { Text("Reportar") }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = alTerminar,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Volver al estado del parking") }
    }
}
