package cl.duoc.sabaterreno.domain

import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.model.Rol
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.model.TipoEquipo
import cl.duoc.sabaterreno.domain.rules.Permisos
import cl.duoc.sabaterreno.domain.rules.Semaforo
import org.junit.Assert.assertEquals
import org.junit.Test

class SemaforoTest {

    private val equipos = listOf(
        Equipo("barrera-in", "Barreras Entrada", TipoEquipo.BARRERA),
        Equipo("cajero1", "Cajero N° 1", TipoEquipo.CAJERO)
    )

    private fun ticket(equipo: String, sev: Severidad, estado: EstadoTicket = EstadoTicket.ESCALADO) =
        Ticket(
            id = "T-$equipo-${sev.name}",
            parkingId = "p1",
            equipoId = equipo,
            falla = "x",
            severidad = sev,
            estado = estado,
            autor = "test",
            observacion = "",
            creado = 0L
        )

    @Test
    fun `sin tickets el parking esta operativo`() {
        assertEquals(Severidad.OPERATIVO, Semaforo.estadoParking(emptyList(), "p1", equipos))
    }

    @Test
    fun `el parking toma la peor severidad abierta`() {
        val tickets = listOf(
            ticket("barrera-in", Severidad.DEGRADADO),
            ticket("cajero1", Severidad.FUERA_DE_SERVICIO)
        )
        assertEquals(Severidad.FUERA_DE_SERVICIO, Semaforo.estadoParking(tickets, "p1", equipos))
    }

    @Test
    fun `los tickets cerrados no afectan el semaforo`() {
        val tickets = listOf(ticket("cajero1", Severidad.FUERA_DE_SERVICIO, EstadoTicket.CERRADO))
        assertEquals(Severidad.OPERATIVO, Semaforo.estadoParking(tickets, "p1", equipos))
    }

    /**
     * Regresión del bug del prototipo: el proveedor no ve cajeros, así que un
     * ticket de cajero no debe pintarle el parking en rojo.
     */
    @Test
    fun `el proveedor no ve en rojo un parking por un ticket de cajero`() {
        val tickets = listOf(ticket("cajero1", Severidad.FUERA_DE_SERVICIO))
        val visibles = Permisos.equiposVisibles(Rol.PROVEEDOR, equipos)
        assertEquals(Severidad.OPERATIVO, Semaforo.estadoParking(tickets, "p1", visibles))
        assertEquals(Severidad.FUERA_DE_SERVICIO, Semaforo.estadoParking(tickets, "p1", equipos))
    }
}
