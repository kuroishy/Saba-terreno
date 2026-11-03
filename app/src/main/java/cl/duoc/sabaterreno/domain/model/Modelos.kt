package cl.duoc.sabaterreno.domain.model

/**
 * Modelos del dominio. Kotlin puro: no conocen Android, Room ni Compose,
 * de modo que las reglas de negocio se pueden probar con tests unitarios.
 */

enum class TipoEquipo(val etiqueta: String) {
    BARRERA("Barrera"),
    LPR("Lectura de patentes"),
    POS("Terminal de pago"),
    CAJERO("Cajero automático")
}

/** Severidad del servicio. El orden del enum define la gravedad. */
enum class Severidad(val etiqueta: String) {
    OPERATIVO("Operativo"),
    DEGRADADO("Degradado"),
    FUERA_DE_SERVICIO("Fuera de servicio")
}

enum class EstadoTicket(val etiqueta: String) {
    ESCALADO("Escalado al SAU"),
    EN_INTERVENCION("En intervención"),
    CERRADO("Cerrado")
}

enum class Rol(val etiqueta: String) {
    OPERADOR("Operación terreno"),
    IT("Tecnología e IT"),
    GERENCIA("Gestión y dirección"),
    PROVEEDOR("Proveedor externo")
}

data class Parking(
    val id: String,
    val nombre: String,
    val comuna: String
)

data class Equipo(
    val id: String,
    val nombre: String,
    val tipo: TipoEquipo
)

data class Usuario(
    val rut: String,
    val nombre: String,
    val rol: Rol,
    /** Lista vacía significa acceso a toda la red. */
    val parkings: List<String>
) {
    val accedeATodos: Boolean get() = parkings.isEmpty()
}

data class EventoTicket(
    val id: Long = 0,
    val ticketId: String,
    val instante: Long,
    val autor: String,
    val detalle: String
)

data class Ticket(
    val id: String,
    val parkingId: String,
    val equipoId: String,
    val falla: String,
    val severidad: Severidad,
    val estado: EstadoTicket,
    val autor: String,
    val observacion: String,
    val creado: Long,
    val cerrado: Long? = null,
    val fotoUri: String? = null,
    val eventos: List<EventoTicket> = emptyList()
) {
    val abierto: Boolean get() = estado != EstadoTicket.CERRADO
}

data class ItemPauta(
    val id: String,
    val etiqueta: String,
    val titulo: String
)

enum class RespuestaPauta { CONFORME, OBSERVADO }

data class Mantencion(
    val id: Long = 0,
    val parkingId: String,
    val semana: String,
    val etiquetaSemana: String,
    val autor: String,
    val instante: Long,
    val respuestas: Map<String, RespuestaPauta>,
    val observacion: String,
    val fotoUri: String? = null,
    val firmadaConBiometria: Boolean = false
) {
    val observados: Int get() = respuestas.values.count { it == RespuestaPauta.OBSERVADO }
}

/** Catálogo de fallas por tipo de equipo. */
object CatalogoFallas {
    private val porTipo = mapOf(
        TipoEquipo.BARRERA to listOf(
            "Brazo no sube", "Brazo no baja", "Goma o brazo dañado", "No lee ticket", "Otra falla"
        ),
        TipoEquipo.LPR to listOf(
            "Lectura de patentes intermitente", "Cámara sin imagen",
            "Lente sucio o desalineado", "Sin conexión", "Otra falla"
        ),
        TipoEquipo.POS to listOf(
            "Sin conexión", "Rechaza tarjetas", "Sin papel", "Pantalla apagada", "Otra falla"
        ),
        TipoEquipo.CAJERO to listOf(
            "Atasco de billetes / Sin vuelto", "Pantalla congelada",
            "Sin papel de boleta", "No acepta pago", "Otra falla"
        )
    )

    fun para(tipo: TipoEquipo): List<String> = porTipo[tipo].orEmpty()
}

/** Resultado de revisar un equipo durante la ronda de turno. */
enum class ResultadoRonda(val etiqueta: String) {
    CONFORME("Conforme"),
    OBSERVADO("Observado")
}

/**
 * Recorrido de turno: la persona revisa equipo por equipo al empezar su
 * jornada y deja constancia de cuáles quedaron observados.
 */
data class Ronda(
    val id: Long = 0,
    val parkingId: String,
    val autor: String,
    val instante: Long,
    val resultados: Map<String, ResultadoRonda>
) {
    val conformes: Int get() = resultados.values.count { it == ResultadoRonda.CONFORME }
    val observados: Int get() = resultados.values.count { it == ResultadoRonda.OBSERVADO }
    val equiposObservados: List<String>
        get() = resultados.filterValues { it == ResultadoRonda.OBSERVADO }.keys.toList()
}
