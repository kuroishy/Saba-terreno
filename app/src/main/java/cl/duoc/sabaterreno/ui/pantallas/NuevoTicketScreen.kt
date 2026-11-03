package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.ui.componentes.PuntoEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoTicketScreen(
    vm: NuevoTicketViewModel,
    equiposVisibles: List<Equipo>,
    parkingId: String,
    autor: String,
    alCrear: (String) -> Unit
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var equipoAbierto by remember { mutableStateOf(false) }
    var fallaAbierta by remember { mutableStateOf(false) }

    LaunchedEffect(estado.ticketCreado) {
        estado.ticketCreado?.let(alCrear)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Reportar una falla", style = MaterialTheme.typography.headlineMedium)

        // ---- Equipo ----
        Column {
            Text("¿Qué equipo falló?", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = equipoAbierto,
                onExpandedChange = { equipoAbierto = !equipoAbierto }
            ) {
                OutlinedTextField(
                    value = equiposVisibles.firstOrNull { it.id == estado.equipoId }?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Elige un equipo") },
                    isError = estado.errorDe(Campo.EQUIPO) != null,
                    supportingText = estado.errorDe(Campo.EQUIPO)?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(equipoAbierto) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = equipoAbierto,
                    onDismissRequest = { equipoAbierto = false }
                ) {
                    equiposVisibles.forEach { equipo ->
                        DropdownMenuItem(
                            text = { Text(equipo.nombre) },
                            onClick = { vm.elegirEquipo(equipo.id); equipoAbierto = false }
                        )
                    }
                }
            }
        }

        // ---- Falla ----
        Column {
            Text("¿Qué pasó?", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = fallaAbierta,
                onExpandedChange = { if (estado.equipoId != null) fallaAbierta = !fallaAbierta }
            ) {
                OutlinedTextField(
                    value = estado.falla ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = estado.equipoId != null,
                    placeholder = {
                        Text(if (estado.equipoId == null) "Primero elige el equipo" else "Elige la falla")
                    },
                    isError = estado.errorDe(Campo.FALLA) != null,
                    supportingText = estado.errorDe(Campo.FALLA)?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fallaAbierta) },
                    modifier = Modifier
                        .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = fallaAbierta,
                    onDismissRequest = { fallaAbierta = false }
                ) {
                    estado.fallasDisponibles.forEach { falla ->
                        DropdownMenuItem(
                            text = { Text(falla) },
                            onClick = { vm.elegirFalla(falla); fallaAbierta = false }
                        )
                    }
                }
            }
        }

        // ---- Severidad ----
        Column {
            Text("¿Cómo está el servicio?", style = MaterialTheme.typography.titleMedium)
            listOf(
                Severidad.DEGRADADO to "Funciona con problemas",
                Severidad.FUERA_DE_SERVICIO to "No funciona"
            ).forEach { (sev, etiqueta) ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .selectable(
                            selected = estado.severidad == sev,
                            onClick = { vm.elegirSeveridad(sev) }
                        )
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioButton(selected = estado.severidad == sev, onClick = { vm.elegirSeveridad(sev) })
                        PuntoEstado(sev)
                        Text(etiqueta, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            estado.errorDe(Campo.SEVERIDAD)?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // ---- Evidencia ----
        Column {
            CapturaEvidencia(
                obligatoria = estado.exigeEvidencia,
                rutaActual = estado.fotoRuta,
                alCapturar = vm::fotoTomada,
                alQuitar = vm::quitarFoto,
                alAvisar = vm::avisoCamara
            )
            estado.errorDe(Campo.EVIDENCIA)?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            AnimatedVisibility(estado.avisoCamara != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    estado.avisoCamara.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---- Observación ----
        OutlinedTextField(
            value = estado.observacion,
            onValueChange = vm::cambiarObservacion,
            label = { Text("Comentario") },
            placeholder = { Text("Desde cuándo y qué hiciste") },
            minLines = 3,
            isError = estado.errorDe(Campo.OBSERVACION) != null,
            supportingText = {
                val error = estado.errorDe(Campo.OBSERVACION)
                Text(
                    error ?: if (estado.exigeEvidencia) "Obligatorio para fallas fuera de servicio." else "Opcional.",
                    color = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.enviar(parkingId, autor) },
            enabled = !estado.enviando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Enviar a soporte", style = MaterialTheme.typography.labelLarge)
        }
    }
}
