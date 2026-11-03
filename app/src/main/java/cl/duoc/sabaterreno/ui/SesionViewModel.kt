package cl.duoc.sabaterreno.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.sabaterreno.data.local.SesionStore
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.Parking
import cl.duoc.sabaterreno.domain.model.Usuario
import cl.duoc.sabaterreno.domain.rules.Capacidades
import cl.duoc.sabaterreno.domain.rules.Permisos
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la sesión: quién entró y en qué parking está trabajando. */
data class SesionUiState(
    val usuario: Usuario? = null,
    val parking: Parking? = null,
    val parkingsDisponibles: List<Parking> = emptyList(),
    /** Mientras se intenta restaurar la sesión no se muestra el login. */
    val restaurando: Boolean = true
) {
    val capacidades: Capacidades? get() = usuario?.let { Permisos.de(it.rol) }
    fun equiposVisibles(todos: List<Equipo>): List<Equipo> =
        usuario?.let { Permisos.equiposVisibles(it.rol, todos) } ?: emptyList()
}

class SesionViewModel(
    private val repo: SabaRepository,
    private val store: SesionStore
) : ViewModel() {

    private val _estado = MutableStateFlow(SesionUiState())
    val estado: StateFlow<SesionUiState> = _estado.asStateFlow()

    /** Observador de los parkings del usuario activo; se cancela al cambiar de usuario. */
    private var observador: Job? = null

    init {
        viewModelScope.launch {
            repo.sembrarSiHaceFalta()
            restaurar()
        }
    }

    /**
     * Rearma la sesión recordada. El usuario se vuelve a leer de la base en vez
     * de guardarse entero, de modo que un cambio de rol o de parkings
     * asignados se refleja de inmediato.
     */
    private suspend fun restaurar() {
        val guardada = store.sesion.first()
        val usuario = guardada?.let { repo.usuarioPorRut(it.rut) }
        if (usuario == null) {
            _estado.update { it.copy(restaurando = false) }
            return
        }

        val parkings = repo.parkingsDe(usuario).first()
        // El parking recordado puede haber dejado de estar asignado.
        val parking = parkings.firstOrNull { it.id == guardada.parkingId }
        _estado.update {
            it.copy(
                usuario = usuario,
                parking = parking,
                parkingsDisponibles = parkings,
                restaurando = false
            )
        }
        observarParkings(usuario)
    }

    fun iniciarSesion(usuario: Usuario) {
        _estado.update { it.copy(usuario = usuario, restaurando = false) }
        viewModelScope.launch { store.recordarUsuario(usuario.rut) }
        observarParkings(usuario)
    }

    private fun observarParkings(usuario: Usuario) {
        observador?.cancel()
        observador = viewModelScope.launch {
            repo.parkingsDe(usuario).collect { lista ->
                _estado.update { it.copy(parkingsDisponibles = lista) }
            }
        }
    }

    fun elegirParking(parking: Parking) {
        _estado.update { it.copy(parking = parking) }
        viewModelScope.launch { store.recordarParking(parking.id) }
    }

    fun cambiarParking() {
        _estado.update { it.copy(parking = null) }
        viewModelScope.launch { store.olvidarParking() }
    }

    fun cerrarSesion() {
        observador?.cancel()
        observador = null
        _estado.value = SesionUiState(restaurando = false)
        viewModelScope.launch { store.olvidar() }
    }

    companion object {
        fun factory(repo: SabaRepository, store: SesionStore) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(clase: Class<T>): T =
                SesionViewModel(repo, store) as T
        }
    }
}
