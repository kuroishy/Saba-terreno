package cl.duoc.sabaterreno.nativo

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Recurso nativo 2: cámara del dispositivo mediante CameraX.
 *
 * La foto se guarda en el almacenamiento **interno** de la app
 * (`filesDir/evidencias`), no en la galería: es evidencia operacional y no
 * tiene por qué quedar expuesta al resto del teléfono. La ruta se persiste en
 * Room junto al ticket, de modo que la evidencia sobrevive al cierre de la app.
 */
object CamaraEvidencia {

    private const val CARPETA = "evidencias"

    fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    private fun nuevoArchivo(context: Context): File {
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(carpeta(context), "EV_$marca.jpg")
    }

    /** Enlaza la vista previa y la captura al ciclo de vida de la pantalla. */
    fun iniciar(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        alQuedarLista: (ImageCapture) -> Unit,
        alFallar: (String) -> Unit
    ) {
        val futuro = ProcessCameraProvider.getInstance(context)
        futuro.addListener({
            try {
                val proveedor = futuro.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val captura = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                proveedor.unbindAll()
                proveedor.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    captura
                )
                alQuedarLista(captura)
            } catch (e: Exception) {
                alFallar(e.message ?: "No se pudo abrir la cámara.")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun capturar(
        context: Context,
        captura: ImageCapture,
        alGuardar: (String) -> Unit,
        alFallar: (String) -> Unit
    ) {
        val archivo = nuevoArchivo(context)
        captura.takePicture(
            ImageCapture.OutputFileOptions.Builder(archivo).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                    alGuardar(archivo.absolutePath)
                }

                override fun onError(e: ImageCaptureException) {
                    alFallar(e.message ?: "No se pudo guardar la foto.")
                }
            }
        )
    }
}
