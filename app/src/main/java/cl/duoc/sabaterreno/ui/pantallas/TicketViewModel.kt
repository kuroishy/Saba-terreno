package cl.duoc.sabaterreno.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.domain.validation.CierreValidator
import cl.duoc.sabaterreno.domain.validation.ResultadoValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketUiState(
    val ticket: Ticket? = null,
    val cargando: Boolean = true,
    /** La caja de cierre sólo aparece cuando la persona la pide. */
    val cerrando: Boolean = false,
    val resolucion: String = "",
    val validacionCierre: ResultadoValidacion = ResultadoValidacion.OK,
    val mostrarErrores: Boolean = false,
    val escribiendoNota: Boolean = false,
    val nota: String = "",
    val aviso: String? = null
) {
    fun errorDe(campo: Campo): String? =
        if (mostrarErrores) validacionCierre.mensajeDe(campo) else null
}

/**
 * Detalle de una incidencia. El ticket se observa desde Room, así que
 * cualquier cambio (una nota, el cierre) se refleja solo en la pantalla sin
 * tener que recargar nada a mano.
 */
class TicketViewModel(
    private val repo: SabaRepository,
    private val ticketId: String
) : ViewModel() {

    private val _estado = MutableStateFlow(TicketUiState())
    val estado: StateFlow<TicketUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ticketConHistorial(ticketId).collect { t ->
                _estado.update { it.copy(ticket = t, cargando = false) }
            }
        }
    }

    fun registrarIntervencion(autor: String) {
        viewModelScope.launch {
            repo.registrarIntervencion(ticketId, autor)
            _estado.update { it.copy(aviso = "Intervención registrada.") }
        }
    }

    fun abrirCierre() = _estado.update { it.copy(cerrando = true, resolucion = "", mostrarErrores = false) }

    fun cancelarCierre() = _estado.update {
        it.copy(cerrando = false, resolucion = "", mostrarErrores = false)
    }

    fun cambiarResolucion(texto: String) = _estado.update {
        it.copy(resolucion = texto, validacionCierre = CierreValidator.validar(texto))
    }

    fun confirmarCierre(autor: String) {
        val actual = _estado.value
        val validacion = CierreValidator.validar(actual.resolucion)
        _estado.value = actual.copy(validacionCierre = validacion, mostrarErrores = true)
        if (!validacion.valido) return

        viewModelScope.launch {
            repo.cerrarTicket(ticketId, actual.resolucion.trim(), autor)
            _estado.update {
                it.copy(cerrando = false, resolucion = "", mostrarErrores = false, aviso = "Incidencia cerrada.")
            }
        }
    }

    fun mostrarCampoNota() = _estado.update { it.copy(escribiendoNota = true, nota = "") }

    fun cambiarNota(texto: String) = _estado.update { it.copy(nota = texto) }

    fun guardarNota(autor: String) {
        val texto = _estado.value.nota.trim()
        if (texto.isEmpty()) return
        viewModelScope.launch {
            repo.agregarNota(ticketId, texto, autor)
            _estado.update { it.copy(escribiendoNota = false, nota = "", aviso = "Nota agregada.") }
        }
    }

    fun avisoMostrado() = _estado.update { it.copy(aviso = null) }

    companion object {
        fun factory(repo: SabaRepository, ticketId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(clase: Class<T>): T =
                TicketViewModel(repo, ticketId) as T
        }
    }
}
