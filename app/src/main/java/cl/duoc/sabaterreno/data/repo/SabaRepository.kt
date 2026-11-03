package cl.duoc.sabaterreno.data.repo

import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.data.local.EventoEntity
import cl.duoc.sabaterreno.data.local.MantencionEntity
import cl.duoc.sabaterreno.data.local.RondaEntity
import cl.duoc.sabaterreno.data.local.SabaDatabase
import cl.duoc.sabaterreno.data.local.Semilla
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.model.Mantencion
import cl.duoc.sabaterreno.domain.model.Parking
import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import cl.duoc.sabaterreno.domain.model.Ronda
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Única puerta de entrada a los datos. Los ViewModel hablan con esta clase y
 * nunca con Room directamente, así se puede cambiar la fuente de datos (o
 * sustituirla por un doble en un test) sin tocar la interfaz.
 */
class SabaRepository(private val db: SabaDatabase) {

    suspend fun sembrarSiHaceFalta() {
        if (db.usuarios().total() > 0) return
        db.usuarios().insertar(Semilla.usuarios)
        db.parkings().insertar(Semilla.parkings)
        val ahora = System.currentTimeMillis()
        Semilla.tickets(ahora).forEach { (ticket, eventos) ->
            db.tickets().insertar(ticket)
            eventos.forEach { db.tickets().insertarEvento(it) }
        }
        db.mantenciones().insertar(Semilla.mantencionAnterior(ahora))
    }

    /** Autentica comparando el hash; nunca se guarda la clave en claro. */
    suspend fun autenticar(rutNormalizado: String, clave: String): Usuario? {
        val fila = db.usuarios().porRut(rutNormalizado) ?: return null
        return if (fila.claveHash == Semilla.hash(clave)) fila.aDominio() else null
    }

    /** Usuario por RUT, sin clave: se usa para restaurar una sesión recordada. */
    suspend fun usuarioPorRut(rut: String): Usuario? =
        db.usuarios().porRut(rut)?.aDominio()

    fun parkings(): Flow<List<Parking>> =
        db.parkings().todos().map { lista -> lista.map { it.aDominio() } }

    fun parkingsDe(usuario: Usuario): Flow<List<Parking>> =
        parkings().map { todos ->
            if (usuario.accedeATodos) todos else todos.filter { it.id in usuario.parkings }
        }

    fun tickets(): Flow<List<Ticket>> =
        db.tickets().todos().map { lista -> lista.map { it.aDominio() } }

    fun ticketsDe(parkingId: String): Flow<List<Ticket>> =
        db.tickets().porParking(parkingId).map { lista -> lista.map { it.aDominio() } }

    /** Ticket con su historial, combinando ambos flujos. */
    fun ticketConHistorial(id: String): Flow<Ticket?> =
        combine(db.tickets().porId(id), db.tickets().eventosDe(id)) { t, eventos ->
            t?.aDominio(eventos.map { it.aDominio() })
        }

    suspend fun crearTicket(
        parkingId: String,
        equipoId: String,
        falla: String,
        severidad: Severidad,
        observacion: String,
        fotoUri: String?,
        autor: String
    ): String {
        val correlativo = (db.tickets().ultimoCorrelativo() ?: 0) + 1
        val anio = java.time.Year.now().value
        val id = "SAU-$anio-%04d".format(correlativo)
        val ahora = System.currentTimeMillis()
        db.tickets().insertar(
            Ticket(
                id = id,
                parkingId = parkingId,
                equipoId = equipoId,
                falla = falla,
                severidad = severidad,
                estado = EstadoTicket.ESCALADO,
                autor = autor,
                observacion = observacion,
                creado = ahora,
                fotoUri = fotoUri
            ).aEntidad()
        )
        registrarEvento(id, autor, "Ticket creado desde la app (${severidad.etiqueta}).")
        registrarEvento(id, "Sistema", "Notificación enviada al SAU (simulada en este prototipo).")
        return id
    }

    suspend fun registrarIntervencion(ticketId: String, autor: String) {
        val actual = db.tickets().porId(ticketId).first() ?: return
        db.tickets().actualizar(actual.copy(estado = EstadoTicket.EN_INTERVENCION))
        registrarEvento(ticketId, autor, "Intervención registrada en el sitio.")
    }

    suspend fun cerrarTicket(ticketId: String, resolucion: String, autor: String) {
        val actual = db.tickets().porId(ticketId).first() ?: return
        db.tickets().actualizar(
            actual.copy(estado = EstadoTicket.CERRADO, cerrado = System.currentTimeMillis())
        )
        registrarEvento(ticketId, autor, "Cierre: $resolucion")
    }

    suspend fun agregarNota(ticketId: String, nota: String, autor: String) =
        registrarEvento(ticketId, autor, "Nota: $nota")

    private suspend fun registrarEvento(ticketId: String, autor: String, detalle: String) {
        db.tickets().insertarEvento(
            EventoEntity(
                ticketId = ticketId,
                instante = System.currentTimeMillis(),
                autor = autor,
                detalle = detalle
            )
        )
    }

    fun mantenciones(parkingId: String): Flow<List<Mantencion>> =
        db.mantenciones().porParking(parkingId).map { lista -> lista.map { it.aDominio() } }

    suspend fun guardarMantencion(
        parkingId: String,
        semana: String,
        etiquetaSemana: String,
        autor: String,
        respuestas: Map<String, RespuestaPauta>,
        observacion: String,
        fotoUri: String?,
        firmadaConBiometria: Boolean
    ) {
        db.mantenciones().insertar(
            MantencionEntity(
                parkingId = parkingId,
                semana = semana,
                etiquetaSemana = etiquetaSemana,
                autor = autor,
                instante = System.currentTimeMillis(),
                respuestas = respuestas,
                observacion = observacion,
                fotoUri = fotoUri,
                firmadaConBiometria = firmadaConBiometria
            )
        )
    }

    fun rondas(parkingId: String): Flow<List<Ronda>> =
        db.rondas().porParking(parkingId).map { lista -> lista.map { it.aDominio() } }

    fun ultimaRonda(parkingId: String): Flow<Ronda?> =
        db.rondas().ultima(parkingId).map { it?.aDominio() }

    suspend fun guardarRonda(
        parkingId: String,
        autor: String,
        resultados: Map<String, ResultadoRonda>
    ): Long = db.rondas().insertar(
        RondaEntity(
            parkingId = parkingId,
            autor = autor,
            instante = System.currentTimeMillis(),
            resultados = resultados
        )
    )

    fun equipos() = Catalogo.equipos

    fun itemsPauta() = Catalogo.itemsPauta
}
