package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.rules.Capacidades
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.ui.componentes.Pastilla
import cl.duoc.sabaterreno.ui.componentes.colorDe
import cl.duoc.sabaterreno.ui.theme.VerdeOperativo
import coil.compose.AsyncImage
import java.io.File

@Composable
fun TicketScreen(
    vm: TicketViewModel,
    capacidades: Capacidades,
    autor: String
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var historialAbierto by remember { mutableStateOf(false) }
    val ticket = estado.ticket

    if (estado.cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (ticket == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("La incidencia ya no existe.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val equipo = Catalogo.equipo(ticket.equipoId)?.nombre ?: ticket.equipoId
    val puedeIntervenir = capacidades.registrarIntervencion && ticket.estado == EstadoTicket.ESCALADO
    val puedeCerrar = capacidades.gestionarCierre && ticket.abierto
    val puedeNotar = capacidades.agregarNota && ticket.abierto

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(equipo, style = MaterialTheme.typography.headlineMedium)
                Text(
                    ticket.falla,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val color = if (ticket.estado == EstadoTicket.CERRADO) VerdeOperativo
                    else colorDe(ticket.severidad)
                    Pastilla(ticket.estado.etiqueta, color)
                    if (ticket.abierto) Pastilla(ticket.severidad.etiqueta, colorDe(ticket.severidad))
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                PasosTicket(ticket.estado, Modifier.padding(16.dp))
            }
        }

        if (ticket.observacion.isNotBlank()) {
            item { Text(ticket.observacion, style = MaterialTheme.typography.bodyLarge) }
        }

        // Evidencia fotográfica: se lee del almacenamiento interno de la app.
        ticket.fotoUri?.let { ruta ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Evidencia", style = MaterialTheme.typography.titleMedium)
                    AsyncImage(
                        model = File(ruta),
                        contentDescription = "Foto de evidencia de la incidencia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        item {
            Text(
                "Reportado por ${ticket.autor} · ${fechaCorta(ticket.creado)} · ${ticket.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ---- Acciones según el rol ----
        if (estado.cerrando) {
            item {
                Card(Modifier.fillMaxWidth().animateContentSize()) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("¿Cómo se resolvió?", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = estado.resolucion,
                            onValueChange = vm::cambiarResolucion,
                            placeholder = { Text("Qué se hizo para resolverlo") },
                            minLines = 3,
                            isError = estado.errorDe(Campo.RESOLUCION) != null,
                            supportingText = estado.errorDe(Campo.RESOLUCION)?.let {
                                { Text(it, color = MaterialTheme.colorScheme.error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { vm.confirmarCierre(autor) },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text("Confirmar cierre") }
                        TextButton(
                            onClick = vm::cancelarCierre,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cancelar") }
                    }
                }
            }
        } else {
            if (puedeIntervenir) {
                item {
                    Button(
                        onClick = { vm.registrarIntervencion(autor) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Text("  Registrar intervención")
                    }
                }
            }
            if (puedeCerrar) {
                item {
                    if (puedeIntervenir) {
                        TextButton(onClick = vm::abrirCierre, modifier = Modifier.fillMaxWidth()) {
                            Text("Cerrar sin intervención")
                        }
                    } else {
                        Button(
                            onClick = vm::abrirCierre,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text("Cerrar incidencia") }
                    }
                }
            }
            if (puedeNotar) {
                item {
                    AnimatedVisibility(
                        visible = estado.escribiendoNota,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = estado.nota,
                                onValueChange = vm::cambiarNota,
                                placeholder = { Text("Escribe la nota") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { vm.guardarNota(autor) },
                                enabled = estado.nota.isNotBlank()
                            ) { Text("Agregar") }
                        }
                    }
                    if (!estado.escribiendoNota) {
                        OutlinedButton(
                            onClick = vm::mostrarCampoNota,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("  Agregar una nota")
                        }
                    }
                }
            }
        }

        // ---- Historial ----
        item {
            Card(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { historialAbierto = !historialAbierto }
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (historialAbierto) "Ocultar historial"
                        else "Ver historial (${ticket.eventos.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    AnimatedVisibility(visible = historialAbierto) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ticket.eventos.sortedByDescending { it.instante }.forEach { evento ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        Modifier
                                            .padding(top = 6.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Column {
                                        Text(evento.detalle, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${evento.autor} · ${fechaCorta(evento.instante)}",
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
        }

        item { Box(Modifier.height(12.dp)) }
    }
}
