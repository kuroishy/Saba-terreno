package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.ui.componentes.Pastilla
import cl.duoc.sabaterreno.ui.theme.AmarilloDegradado
import cl.duoc.sabaterreno.ui.theme.VerdeOperativo

@Composable
fun MantencionScreen(
    vm: MantencionViewModel,
    autor: String,
    alPedirFirma: () -> Unit
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val items = Catalogo.itemsPauta
    val cumplida = estado.cumplidaEstaSemana
    val progreso by animateFloatAsState(
        targetValue = estado.completados.toFloat() / items.size,
        animationSpec = tween(400),
        label = "progresoPauta"
    )

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Mantención semanal", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Semana del ${estado.semana.etiqueta}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (cumplida == null && estado.puedeRegistrar) {
                    Text(
                        "${estado.completados} de ${items.size} ítems revisados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                }
            }
        }


        cumplida?.let { m ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = VerdeOperativo)
                        Column(Modifier.weight(1f)) {
                            Text("Pauta de esta semana cumplida", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${m.autor} · ${fechaCorta(m.instante)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (m.observados > 0) {
                            Pastilla(cantidad(m.observados, "observado", "observados"), AmarilloDegradado)
                        } else {
                            Pastilla("Conforme", VerdeOperativo)
                        }
                    }
                }
            }
            if (m.observacion.isNotBlank()) {
                item {
                    Text(m.observacion, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (cumplida == null && !estado.puedeRegistrar) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "Tu perfil puede consultar el cumplimiento, pero no registrar pautas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (estado.anteriores.isNotEmpty()) {
            item {
                Text(
                    "Mantenciones anteriores",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(estado.anteriores, key = { it.id }) { m ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Semana del ${m.etiquetaSemana}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (m.observados > 0) Pastilla(cantidad(m.observados, "observado", "observados"), AmarilloDegradado)
                            else Pastilla("Conforme", VerdeOperativo)
                        }
                        Text(
                            "${m.autor} · ${fechaCorta(m.instante)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (m.observacion.isNotBlank()) {
                            Text(m.observacion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (cumplida == null && estado.puedeRegistrar) items(items, key = { it.id }) { item ->
            val respuesta = estado.respuestas[item.id]
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        item.etiqueta.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(item.titulo, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val conforme = respuesta == RespuestaPauta.CONFORME
                        val observado = respuesta == RespuestaPauta.OBSERVADO
                        if (conforme) {
                            FilledTonalButton(
                                onClick = { vm.responder(item.id, RespuestaPauta.CONFORME) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, null, tint = VerdeOperativo)
                                Text("  Conforme")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { vm.responder(item.id, RespuestaPauta.CONFORME) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Conforme") }
                        }
                        if (observado) {
                            FilledTonalButton(
                                onClick = { vm.responder(item.id, RespuestaPauta.OBSERVADO) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.WarningAmber, null, tint = AmarilloDegradado)
                                Text("  Observado")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { vm.responder(item.id, RespuestaPauta.OBSERVADO) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Observado") }
                        }
                    }
                }
            }
        }

        if (cumplida == null && estado.puedeRegistrar) estado.errorDe(Campo.ITEMS)?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }

        if (cumplida == null && estado.puedeRegistrar && estado.hayObservados) {
            item {
                OutlinedTextField(
                    value = estado.observacion,
                    onValueChange = vm::cambiarObservacion,
                    label = { Text("¿Qué observaste?") },
                    minLines = 3,
                    isError = estado.errorDe(Campo.OBSERVACION) != null,
                    supportingText = estado.errorDe(Campo.OBSERVACION)?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (cumplida == null && estado.puedeRegistrar) item {
            // La firma es biométrica: no basta con marcar una casilla.
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = if (estado.firmada) VerdeOperativo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (estado.firmada) "Pauta firmada por $autor" else "Firma la pauta",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (estado.firmada) "Identidad confirmada por el sistema."
                            else "Se pedirá tu huella o el PIN del teléfono.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!estado.firmada) {
                        Button(onClick = alPedirFirma) { Text("Firmar") }
                    }
                }
            }
            estado.errorDe(Campo.FIRMA)?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            AnimatedVisibility(estado.avisoFirma != null) {
                Text(
                    estado.avisoFirma.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (cumplida == null && estado.puedeRegistrar) item {
            Button(
                onClick = { vm.guardar(autor) },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 8.dp)
            ) {
                Text("Guardar mantención", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
