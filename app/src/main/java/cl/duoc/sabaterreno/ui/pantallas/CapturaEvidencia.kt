package cl.duoc.sabaterreno.ui.pantallas

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import cl.duoc.sabaterreno.nativo.CamaraEvidencia

/**
 * Captura de evidencia con CameraX, embebida en el formulario.
 *
 * El permiso se pide en el momento en que la persona quiere la foto, no al
 * abrir la app, y si lo rechaza el formulario sigue siendo usable: sólo se le
 * explica por qué no puede continuar si la falla es crítica.
 */
@Composable
fun CapturaEvidencia(
    obligatoria: Boolean,
    rutaActual: String?,
    alCapturar: (String) -> Unit,
    alQuitar: () -> Unit,
    alAvisar: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var camaraAbierta by remember { mutableStateOf(false) }
    var captura by remember { mutableStateOf<ImageCapture?>(null) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        camaraAbierta = concedido
        if (!concedido) {
            alAvisar("Sin permiso de cámara no se puede adjuntar evidencia fotográfica.")
        }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (obligatoria) "Foto de evidencia (obligatoria)" else "Foto de evidencia (opcional)",
            style = MaterialTheme.typography.titleMedium
        )

        when {
            rutaActual != null -> {
                Text(
                    "Evidencia adjunta y guardada en el dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = alQuitar, modifier = Modifier.fillMaxWidth()) {
                    Text("Tomar otra foto")
                }
            }

            camaraAbierta && permisoConcedido -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { vista ->
                                CamaraEvidencia.iniciar(
                                    context = ctx,
                                    lifecycleOwner = lifecycleOwner,
                                    previewView = vista,
                                    alQuedarLista = { captura = it },
                                    alFallar = alAvisar
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(
                    onClick = {
                        val actual = captura ?: return@Button alAvisar("La cámara aún no está lista.")
                        CamaraEvidencia.capturar(
                            context = context,
                            captura = actual,
                            alGuardar = { ruta -> camaraAbierta = false; alCapturar(ruta) },
                            alFallar = alAvisar
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Text("  Capturar", style = MaterialTheme.typography.labelLarge)
                }
            }

            else -> {
                OutlinedButton(
                    onClick = {
                        if (permisoConcedido) camaraAbierta = true
                        else pedirPermiso.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Text("  Abrir cámara", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
