package cl.duoc.sabaterreno.data.repo

import cl.duoc.sabaterreno.data.local.EventoEntity
import cl.duoc.sabaterreno.data.local.MantencionEntity
import cl.duoc.sabaterreno.data.local.ParkingEntity
import cl.duoc.sabaterreno.data.local.RondaEntity
import cl.duoc.sabaterreno.data.local.TicketEntity
import cl.duoc.sabaterreno.data.local.UsuarioEntity
import cl.duoc.sabaterreno.domain.model.EventoTicket
import cl.duoc.sabaterreno.domain.model.Mantencion
import cl.duoc.sabaterreno.domain.model.Parking
import cl.duoc.sabaterreno.domain.model.Ronda
import cl.duoc.sabaterreno.domain.model.Ticket
import cl.duoc.sabaterreno.domain.model.Usuario

/** Traducción entre entidades de Room y modelos de dominio. */

fun ParkingEntity.aDominio() = Parking(id, nombre, comuna)

fun UsuarioEntity.aDominio() = Usuario(rut, nombre, rol, parkings)

fun EventoEntity.aDominio() = EventoTicket(id, ticketId, instante, autor, detalle)

fun TicketEntity.aDominio(eventos: List<EventoTicket> = emptyList()) = Ticket(
    id = id,
    parkingId = parkingId,
    equipoId = equipoId,
    falla = falla,
    severidad = severidad,
    estado = estado,
    autor = autor,
    observacion = observacion,
    creado = creado,
    cerrado = cerrado,
    fotoUri = fotoUri,
    eventos = eventos
)

fun Ticket.aEntidad() = TicketEntity(
    id = id,
    parkingId = parkingId,
    equipoId = equipoId,
    falla = falla,
    severidad = severidad,
    estado = estado,
    autor = autor,
    observacion = observacion,
    creado = creado,
    cerrado = cerrado,
    fotoUri = fotoUri
)

fun MantencionEntity.aDominio() = Mantencion(
    id, parkingId, semana, etiquetaSemana, autor, instante,
    respuestas, observacion, fotoUri, firmadaConBiometria
)

fun RondaEntity.aDominio() = Ronda(id, parkingId, autor, instante, resultados)
