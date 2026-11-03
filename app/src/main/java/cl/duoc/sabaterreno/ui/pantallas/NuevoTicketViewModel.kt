package cl.duoc.sabaterreno.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.model.CatalogoFallas
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.domain.validation.ResultadoValidacion
import cl.duoc.sabaterreno.domain.validation.TicketValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevoTicketUiState(
    val equipoId: String? = null,
    val falla: String? = null,
    val severidad: Severidad? = null,
    val observacion: String = "",
    val fotoRuta: String? = null,
    val validacion: ResultadoValidacion = ResultadoValidacion.OK,
    val mostrarErrores: Boolean = false,
    val enviando: Boolean = false,
    val ticketCreado: String? = null,
    val avisoCamara: String? = null
) {
    val fallasDisponibles: List<String>
        get() = Catalogo.equipo(equipoId ?: "")?.let { CatalogoFallas.para(it.tipo) }.orEmpty()

    val exigeEvidencia: Boolean get() = severidad == Severidad.FUERA_DE_SERVICIO

    fun errorDe(campo: Campo): String? =
        if (mostrarErrores) validacion.mensajeDe(campo) else null
}

class NuevoTicketViewModel(private val repo: SabaRepository) : ViewModel() {

    private val _estado = MutableStateFlow(NuevoTicketUiState())
    val estado: StateFlow<NuevoTicketUiState> = _estado.asStateFlow()

    private fun NuevoTicketUiState.revalidado() = copy(
        validacion = TicketValidator.validar(equipoId, falla, severidad, observacion, fotoRuta)
    )

    fun elegirEquipo(id: String) = _estado.update {
        // Al cambiar de equipo la falla anterior deja de tener sentido.
        it.copy(equipoId = id, falla = null).revalidado()
    }

    fun elegirFalla(f: String) = _estado.update { it.copy(falla = f).revalidado() }

    fun elegirSeveridad(s: Severidad) = _estado.update { it.copy(severidad = s).revalidado() }

    fun cambiarObservacion(t: String) = _estado.update { it.copy(observacion = t).revalidado() }

    fun fotoTomada(ruta: String) = _estado.update {
        it.copy(fotoRuta = ruta, avisoCamara = null).revalidado()
    }

    fun quitarFoto() = _estado.update { it.copy(fotoRuta = null).revalidado() }

    fun avisoCamara(mensaje: String?) = _estado.update { it.copy(avisoCamara = mensaje) }

    fun enviar(parkingId: String, autor: String) {
        val actual = _estado.value.revalidado()
        _estado.value = actual.copy(mostrarErrores = true)
        if (!actual.validacion.valido) return

        viewModelScope.launch {
            _estado.update { it.copy(enviando = true) }
            val id = repo.crearTicket(
                parkingId = parkingId,
                equipoId = actual.equipoId!!,
                falla = actual.falla!!,
                severidad = actual.severidad!!,
                observacion = actual.observacion.trim(),
                fotoUri = actual.fotoRuta,
                autor = autor
            )
            _estado.update { it.copy(enviando = false, ticketCreado = id) }
        }
    }

    companion object {
        fun factory(repo: SabaRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(clase: Class<T>): T = NuevoTicketViewModel(repo) as T
        }
    }
}
