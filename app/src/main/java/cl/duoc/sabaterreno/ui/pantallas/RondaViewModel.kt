package cl.duoc.sabaterreno.ui.pantallas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RondaUiState(
    val equipos: List<Equipo> = emptyList(),
    val indice: Int = 0,
    val resultados: Map<String, ResultadoRonda> = emptyMap(),
    /** Orden en que se respondió, para poder deshacer el último. */
    val historial: List<String> = emptyList(),
    val guardando: Boolean = false,
    val guardada: Boolean = false
) {
    val total: Int get() = equipos.size
    val terminada: Boolean get() = total > 0 && indice >= total
    val progreso: Float get() = if (total == 0) 0f else indice.toFloat() / total
    val conformes: Int get() = resultados.values.count { it == ResultadoRonda.CONFORME }
    val observados: List<Equipo>
        get() = equipos.filter { resultados[it.id] == ResultadoRonda.OBSERVADO }
    val puedeDeshacer: Boolean get() = historial.isNotEmpty()
}

/**
 * Ronda de turno: se recorre la lista de equipos uno por uno y se deja cada
 * uno como conforme u observado. Al terminar se guarda el recorrido completo,
 * no equipo por equipo, para que una ronda a medias no quede registrada.
 */
class RondaViewModel(
    private val repo: SabaRepository,
    equiposVisibles: List<Equipo>
) : ViewModel() {

    private val _estado = MutableStateFlow(RondaUiState(equipos = equiposVisibles))
    val estado: StateFlow<RondaUiState> = _estado.asStateFlow()

    fun responder(equipoId: String, resultado: ResultadoRonda) = _estado.update {
        if (it.terminada) it
        else it.copy(
            resultados = it.resultados + (equipoId to resultado),
            historial = it.historial + equipoId,
            indice = it.indice + 1
        )
    }

    fun deshacer() = _estado.update {
        val ultimo = it.historial.lastOrNull() ?: return@update it
        it.copy(
            resultados = it.resultados - ultimo,
            historial = it.historial.dropLast(1),
            indice = (it.indice - 1).coerceAtLeast(0)
        )
    }

    fun reiniciar() = _estado.update {
        RondaUiState(equipos = it.equipos)
    }

    fun guardar(parkingId: String, autor: String) {
        val actual = _estado.value
        if (!actual.terminada || actual.guardando || actual.guardada) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            repo.guardarRonda(parkingId, autor, actual.resultados)
            _estado.update { it.copy(guardando = false, guardada = true) }
        }
    }

    companion object {
        fun factory(repo: SabaRepository, equipos: List<Equipo>) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(clase: Class<T>): T =
                RondaViewModel(repo, equipos) as T
        }
    }
}
