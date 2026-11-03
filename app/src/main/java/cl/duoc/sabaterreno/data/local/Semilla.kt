package cl.duoc.sabaterreno.data.local

import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.model.Rol
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.rules.SemanaOperacional
import java.security.MessageDigest

/**
 * Datos iniciales del caso práctico. Son ficticios.
 *
 * Las contraseñas se guardan como hash SHA-256 con sal fija: para un prototipo
 * académico basta y evita dejar credenciales en texto plano en el dispositivo.
 * En producción correspondería un esquema con sal por usuario (bcrypt/argon2)
 * y validación contra el servidor, no local.
 */
object Semilla {

    private const val SAL = "saba-terreno-2026"

    fun hash(clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((SAL + clave).toByteArray())
            .joinToString("") { "%02x".format(it) }

    val parkings = listOf(
        ParkingEntity("plaza-italia", "Parking Plaza Italia", "Santiago"),
        ParkingEntity("bellavista", "Parking Bellavista", "Providencia"),
        ParkingEntity("apumanque", "Parking Apumanque", "Las Condes"),
        ParkingEntity("vina-centro", "Parking Viña Centro", "Viña del Mar"),
        ParkingEntity("concepcion", "Parking Concepción Centro", "Concepción"),
        ParkingEntity("antofagasta", "Parking Antofagasta Centro", "Antofagasta")
    )

    /** RUT válidos según módulo 11, para que pasen la validación real de la app. */
    val usuarios = listOf(
        UsuarioEntity(
            rut = "15420895K",
            claveHash = hash("terreno2026"),
            nombre = "Camila Rojas",
            rol = Rol.OPERADOR,
            parkings = listOf("plaza-italia", "bellavista", "apumanque")
        ),
        UsuarioEntity(
            rut = "102345770",
            claveHash = hash("tisaba2026"),
            nombre = "Diego Fuentes",
            rol = Rol.IT,
            parkings = emptyList()
        ),
        UsuarioEntity(
            rut = "987654325",
            claveHash = hash("gestion2026"),
            nombre = "Marcela Ibáñez",
            rol = Rol.GERENCIA,
            parkings = emptyList()
        ),
        UsuarioEntity(
            rut = "134567899",
            claveHash = hash("soporte2026"),
            nombre = "Andrés Soto",
            rol = Rol.PROVEEDOR,
            parkings = listOf("plaza-italia", "apumanque", "vina-centro")
        )
    )

    fun tickets(ahora: Long): List<Pair<TicketEntity, List<EventoEntity>>> {
        val hora = 3_600_000L
        return listOf(
            TicketEntity(
                id = "SAU-2026-0412",
                parkingId = "plaza-italia",
                equipoId = "cajero2",
                falla = "Atasco de billetes / Sin vuelto",
                severidad = Severidad.DEGRADADO,
                estado = EstadoTicket.EN_INTERVENCION,
                autor = "Camila Rojas",
                observacion = "Falla recurrente desde las 10:15. Pantalla congelada.",
                creado = ahora - 3 * hora,
                cerrado = null,
                fotoUri = null
            ) to listOf(
                EventoEntity(ticketId = "SAU-2026-0412", instante = ahora - 3 * hora, autor = "Camila Rojas", detalle = "Ticket creado desde la app (Degradado)."),
                EventoEntity(ticketId = "SAU-2026-0412", instante = ahora - 2 * hora, autor = "Diego Fuentes", detalle = "Técnico en camino. Se mantiene cobro en cajero N° 1.")
            ),
            TicketEntity(
                id = "SAU-2026-0413",
                parkingId = "apumanque",
                equipoId = "lpr",
                falla = "Lectura de patentes intermitente",
                severidad = Severidad.DEGRADADO,
                estado = EstadoTicket.ESCALADO,
                autor = "Camila Rojas",
                observacion = "Falla 4 de cada 10 vehículos en la entrada.",
                creado = ahora - 5 * hora,
                cerrado = null,
                fotoUri = null
            ) to listOf(
                EventoEntity(ticketId = "SAU-2026-0413", instante = ahora - 5 * hora, autor = "Camila Rojas", detalle = "Ticket creado desde la app (Degradado).")
            ),
            TicketEntity(
                id = "SAU-2026-0414",
                parkingId = "antofagasta",
                equipoId = "barrera-out",
                falla = "Brazo no sube",
                severidad = Severidad.FUERA_DE_SERVICIO,
                estado = EstadoTicket.ESCALADO,
                autor = "Operador de turno",
                observacion = "Salida con contingencia manual, se cobra con POS portátil.",
                creado = ahora - hora,
                cerrado = null,
                fotoUri = null
            ) to listOf(
                EventoEntity(ticketId = "SAU-2026-0414", instante = ahora - hora, autor = "Operador de turno", detalle = "Ticket creado desde la app (Fuera de servicio).")
            ),
            TicketEntity(
                id = "SAU-2026-0409",
                parkingId = "bellavista",
                equipoId = "pos",
                falla = "Sin conexión",
                severidad = Severidad.DEGRADADO,
                estado = EstadoTicket.CERRADO,
                autor = "Camila Rojas",
                observacion = "Terminal sin red.",
                creado = ahora - 30 * hora,
                cerrado = ahora - 26 * hora,
                fotoUri = null
            ) to listOf(
                EventoEntity(ticketId = "SAU-2026-0409", instante = ahora - 30 * hora, autor = "Camila Rojas", detalle = "Ticket creado desde la app (Degradado)."),
                EventoEntity(ticketId = "SAU-2026-0409", instante = ahora - 26 * hora, autor = "Diego Fuentes", detalle = "Cierre: se reemplazó el cable de red del terminal.")
            )
        )
    }

    /** Pauta de la semana pasada, para que el historial no nazca vacío. */
    fun mantencionAnterior(ahora: Long): MantencionEntity {
        val haceUnaSemana = ahora - 7 * 24 * 3_600_000L
        val semana = SemanaOperacional.de(haceUnaSemana)
        return MantencionEntity(
            parkingId = "apumanque",
            semana = semana.clave,
            etiquetaSemana = semana.etiqueta,
            autor = "Camila Rojas",
            instante = haceUnaSemana,
            respuestas = mapOf(
                "lpr" to RespuestaPauta.CONFORME,
                "goma" to RespuestaPauta.CONFORME,
                "pos" to RespuestaPauta.CONFORME,
                "caj" to RespuestaPauta.OBSERVADO,
                "sen" to RespuestaPauta.CONFORME
            ),
            observacion = "Rollo de boletas del cajero N° 2 al 20%.",
            fotoUri = null,
            firmadaConBiometria = true
        )
    }
}
