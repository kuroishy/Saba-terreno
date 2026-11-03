package cl.duoc.sabaterreno.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.model.Mantencion
import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.rules.Semana
import cl.duoc.sabaterreno.domain.rules.SemanaOperacional
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.domain.validation.PautaValidator
import cl.duoc.sabaterreno.domain.validation.ResultadoValidacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MantencionUiState(
    val semana: Semana = SemanaOperacional.actual(),
    val historial: List<Mantencion> = emptyList(),
    val respuestas: Map<String, RespuestaPauta> = emptyMap(),
    val observacion: String = "",
    val firmada: Boolean = false,
    val validacion: ResultadoValidacion = ResultadoValidacion.OK,
    val mostrarErrores: Boolean = false,
    val avisoFirma: String? = null,
    val puedeRegistrar: Boolean = true
) {
    /** La pauta de esta semana, si ya se registró. */
    val cumplidaEstaSemana: Mantencion?
        get() = historial.firstOrNull { it.semana == semana.clave }

    val anteriores: List<Mantencion>
        get() = historial.filter { it.semana != semana.clave }

    val completados: Int get() = respuestas.size
    val hayObservados: Boolean get() = respuestas.values.any { it == RespuestaPauta.OBSERVADO }
    fun errorDe(campo: Campo): String? = if (mostrarErrores) validacion.mensajeDe(campo) else null
}

class MantencionViewModel(
    private val repo: SabaRepository,
    private val parkingId: String,
    puedeRegistrar: Boolean
) : ViewModel() {

    private val _estado = MutableStateFlow(MantencionUiState(puedeRegistrar = puedeRegistrar))
    val estado: StateFlow<MantencionUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repo.mantenciones(parkingId).collect { lista ->
                _estado.update { it.copy(historial = lista) }
            }
        }
    }

    private fun MantencionUiState.revalidado() = copy(
        validacion = PautaValidator.validar(
            totalItems = Catalogo.itemsPauta.size,
            respuestas = respuestas,
            observacion = observacion,
            firmada = firmada
        )
    )

    fun responder(itemId: String, respuesta: RespuestaPauta) = _estado.update {
        it.copy(respuestas = it.respuestas + (itemId to respuesta)).revalidado()
    }

    fun cambiarObservacion(texto: String) = _estado.update {
        it.copy(observacion = texto).revalidado()
    }

    /** Sólo la confirmación biométrica marca la pauta como firmada. */
    fun firmaConfirmada() = _estado.update {
        it.copy(firmada = true, avisoFirma = null).revalidado()
    }

    fun firmaRechazada(motivo: String) = _estado.update {
        it.copy(firmada = false, avisoFirma = motivo).revalidado()
    }

    fun guardar(autor: String) {
        val actual = _estado.value.revalidado()
        _estado.value = actual.copy(mostrarErrores = true)
        if (!actual.validacion.valido) return
        // Una pauta por semana: si ya está cumplida no se vuelve a registrar.
        if (actual.cumplidaEstaSemana != null) return

        viewModelScope.launch {
            repo.guardarMantencion(
                parkingId = parkingId,
                semana = actual.semana.clave,
                etiquetaSemana = actual.semana.etiqueta,
                autor = autor,
                respuestas = actual.respuestas,
                observacion = actual.observacion.trim(),
                fotoUri = null,
                firmadaConBiometria = true
            )
            // El historial llega solo por el Flow; aquí sólo se limpia el borrador.
            _estado.update {
                it.copy(
                    respuestas = emptyMap(),
                    observacion = "",
                    firmada = false,
                    mostrarErrores = false,
                    validacion = ResultadoValidacion.OK
                )
            }
        }
    }

    companion object {
        fun factory(repo: SabaRepository, parkingId: String, puedeRegistrar: Boolean) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(clase: Class<T>): T =
                    MantencionViewModel(repo, parkingId, puedeRegistrar) as T
            }
    }
}
