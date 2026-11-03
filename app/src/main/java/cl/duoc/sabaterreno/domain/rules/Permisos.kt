package cl.duoc.sabaterreno.domain.rules

import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.Rol
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.model.TipoEquipo

/** Capacidades que habilita cada rol. */
data class Capacidades(
    val crearTicket: Boolean,
    val gestionarCierre: Boolean,
    val registrarIntervencion: Boolean,
    val agregarNota: Boolean,
    val registrarMantencion: Boolean
)

object Permisos {

    fun de(rol: Rol): Capacidades = when (rol) {
        Rol.OPERADOR -> Capacidades(true, false, false, true, true)
        Rol.IT -> Capacidades(true, true, true, true, true)
        Rol.GERENCIA -> Capacidades(false, false, false, false, false)
        Rol.PROVEEDOR -> Capacidades(false, false, true, true, false)
    }

    /** El proveedor externo no tiene visibilidad sobre los cajeros (manejan dinero). */
    fun equiposVisibles(rol: Rol, equipos: List<Equipo>): List<Equipo> =
        if (rol == Rol.PROVEEDOR) equipos.filter { it.tipo != TipoEquipo.CAJERO } else equipos

    fun puedeVer(rol: Rol, ticket: Ticket, equipos: List<Equipo>): Boolean {
        val tipo = equipos.firstOrNull { it.id == ticket.equipoId }?.tipo ?: return true
        return !(rol == Rol.PROVEEDOR && tipo == TipoEquipo.CAJERO)
    }
}
