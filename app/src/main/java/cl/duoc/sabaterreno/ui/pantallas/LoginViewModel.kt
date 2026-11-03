package cl.duoc.sabaterreno.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.model.Usuario
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.domain.validation.LoginValidator
import cl.duoc.sabaterreno.domain.validation.ResultadoValidacion
import cl.duoc.sabaterreno.domain.validation.Rut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val rut: String = "",
    val clave: String = "",
    val validacion: ResultadoValidacion = ResultadoValidacion.OK,
    val mostrarErrores: Boolean = false,
    val errorCredenciales: String? = null,
    val cargando: Boolean = false,
    val usuarioAutenticado: Usuario? = null
) {
    /** El error sólo se muestra después del primer intento, no mientras escribe. */
    fun errorDe(campo: Campo): String? =
        if (mostrarErrores) validacion.mensajeDe(campo) else null
}

/**
 * El ViewModel orquesta: pide la validación al dominio y expone el resultado.
 * No contiene ninguna regla de validación propia.
 */
class LoginViewModel(private val repo: SabaRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun cambiarRut(valor: String) = _estado.update {
        it.copy(rut = valor, errorCredenciales = null).revalidado()
    }

    fun cambiarClave(valor: String) = _estado.update {
        it.copy(clave = valor, errorCredenciales = null).revalidado()
    }

    /** Al salir del campo, formatea el RUT si ya es válido. */
    fun formatearRut() = _estado.update {
        if (Rut.esValido(it.rut)) it.copy(rut = Rut.formatear(it.rut)) else it
    }

    private fun LoginUiState.revalidado() =
        copy(validacion = LoginValidator.validar(rut, clave))

    fun usarPerfilDemo(rut: String, clave: String) = _estado.update {
        it.copy(rut = rut, clave = clave, errorCredenciales = null, mostrarErrores = false)
            .revalidado()
    }

    fun ingresar() {
        val actual = _estado.value.revalidado()
        _estado.value = actual.copy(mostrarErrores = true)
        if (!actual.validacion.valido) return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val usuario = repo.autenticar(Rut.normalizar(actual.rut), actual.clave)
            _estado.update {
                if (usuario == null) {
                    it.copy(cargando = false, errorCredenciales = "RUT o contraseña incorrectos.")
                } else {
                    it.copy(cargando = false, usuarioAutenticado = usuario)
                }
            }
        }
    }

    companion object {
        fun factory(repo: SabaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(clase: Class<T>): T = LoginViewModel(repo) as T
        }
    }
}
