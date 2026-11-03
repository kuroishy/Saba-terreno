package cl.duoc.sabaterreno.domain.rules

import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.model.Ticket

/**
 * Reglas del semáforo operacional. Funciones puras: reciben los tickets ya
 * cargados y devuelven el estado, sin tocar base de datos ni interfaz.
 */
object Semaforo {

    /** La peor de dos severidades, según el orden del enum. */
    fun peor(a: Severidad, b: Severidad): Severidad =
        if (b.ordinal > a.ordinal) b else a

    /** Estado de un equipo: la peor severidad entre sus tickets abiertos. */
    fun estadoEquipo(tickets: List<Ticket>, parkingId: String, equipoId: String): Severidad =
        tickets
            .filter { it.parkingId == parkingId && it.equipoId == equipoId && it.abierto }
            .fold(Severidad.OPERATIVO) { acc, t -> peor(acc, t.severidad) }

    /**
     * Estado del parking: la peor severidad entre los equipos **visibles** para
     * quien consulta. Recibir la lista de equipos visibles evita la
     * inconsistencia de mostrar en rojo un parking por un equipo que el usuario
     * no tiene permiso de ver.
     */
    fun estadoParking(tickets: List<Ticket>, parkingId: String, equiposVisibles: List<Equipo>): Severidad =
        equiposVisibles.fold(Severidad.OPERATIVO) { acc, e ->
            peor(acc, estadoEquipo(tickets, parkingId, e.id))
        }

    /** Porcentaje de equipos operativos sobre el total visible, para los paneles de red. */
    fun porcentajeOperativo(tickets: List<Ticket>, parkings: List<String>, equiposVisibles: List<Equipo>): Int {
        val total = parkings.size * equiposVisibles.size
        if (total == 0) return 100
        val operativos = parkings.sumOf { p ->
            equiposVisibles.count { e -> estadoEquipo(tickets, p, e.id) == Severidad.OPERATIVO }
        }
        return (operativos * 100) / total
    }
}
