package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.sabaterreno.domain.model.Usuario
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.ui.componentes.CampoValidado

/** Perfiles de ejemplo del caso práctico, con RUT válidos según módulo 11. */
private val perfilesDemo = listOf(
    Triple("Operador", "15.420.895-K", "terreno2026"),
    Triple("IT", "10.234.577-0", "tisaba2026"),
    Triple("Gerencia", "98.765.432-5", "gestion2026"),
    Triple("Proveedor", "13.456.789-9", "soporte2026")
)

@Composable
fun LoginScreen(vm: LoginViewModel, alEntrar: (Usuario) -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.usuarioAutenticado) {
        estado.usuarioAutenticado?.let(alEntrar)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera con el lenguaje visual del letrero de parking.
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("P", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
            Column(Modifier.padding(top = 16.dp)) {
                Text(
                    "Saba Terreno",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.background
                )
                Text(
                    "Continuidad operacional en el estacionamiento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.75f)
                )
            }
        }

        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

            CampoValidado(
                valor = estado.rut,
                alCambiar = vm::cambiarRut,
                etiqueta = "RUT",
                error = estado.errorDe(Campo.RUT),
                apoyo = "Con dígito verificador, por ejemplo 15.420.895-K",
                modifier = Modifier.fillMaxWidth(),
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            CampoValidado(
                valor = estado.clave,
                alCambiar = vm::cambiarClave,
                etiqueta = "Contraseña",
                error = estado.errorDe(Campo.CLAVE),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // El error de credenciales aparece y desaparece con animación.
            AnimatedVisibility(
                visible = estado.errorCredenciales != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    estado.errorCredenciales.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(12.dp)
                )
            }

            Button(
                onClick = { vm.formatearRut(); vm.ingresar() },
                enabled = !estado.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continuar", style = MaterialTheme.typography.labelLarge)
                }
            }

            Text(
                "Probar con un perfil de ejemplo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                perfilesDemo.forEach { (etiqueta, rut, clave) ->
                    AssistChip(
                        onClick = { vm.usarPerfilDemo(rut, clave) },
                        label = { Text(etiqueta) }
                    )
                }
            }
            Text(
                "Datos ficticios para el caso práctico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
