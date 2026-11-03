package cl.duoc.sabaterreno.nativo

import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Recurso nativo 1: autenticación biométrica.
 *
 * Se usa para firmar la pauta de mantención. La firma deja constancia de quién
 * la realizó, así que exigir huella (o el patrón del dispositivo como
 * alternativa) es más fiable que una casilla que cualquiera puede marcar.
 */
object Biometria {

    private const val AUTENTICADORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    enum class Disponibilidad { DISPONIBLE, SIN_HARDWARE, NO_INSCRITA, NO_DISPONIBLE }

    fun disponibilidad(activity: FragmentActivity): Disponibilidad =
        when (BiometricManager.from(activity).canAuthenticate(AUTENTICADORES)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Disponibilidad.DISPONIBLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Disponibilidad.SIN_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Disponibilidad.NO_INSCRITA
            else -> Disponibilidad.NO_DISPONIBLE
        }

    /**
     * Pide la huella. [alExito] sólo se invoca si el sistema confirma la
     * identidad; cualquier error o cancelación va a [alFallar] con un mensaje
     * que la pantalla puede mostrar.
     */
    fun firmar(
        activity: FragmentActivity,
        titulo: String,
        subtitulo: String,
        alExito: () -> Unit,
        alFallar: (String) -> Unit
    ) {
        when (disponibilidad(activity)) {
            Disponibilidad.SIN_HARDWARE ->
                return alFallar("Este dispositivo no tiene lector biométrico.")
            Disponibilidad.NO_INSCRITA ->
                return alFallar("No hay huella inscrita. Configúrala en los ajustes del teléfono.")
            Disponibilidad.NO_DISPONIBLE ->
                return alFallar("La autenticación biométrica no está disponible ahora.")
            Disponibilidad.DISPONIBLE -> Unit
        }

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    alExito()
                }

                override fun onAuthenticationError(code: Int, mensaje: CharSequence) {
                    alFallar(mensaje.toString())
                }

                override fun onAuthenticationFailed() {
                    alFallar("No se reconoció la huella. Inténtalo otra vez.")
                }
            }
        )

        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(titulo)
                .setSubtitle(subtitulo)
                .setAllowedAuthenticators(AUTENTICADORES)
                .build()
        )
    }
}
