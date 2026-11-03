package cl.duoc.sabaterreno.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import cl.duoc.sabaterreno.domain.model.Rol
import cl.duoc.sabaterreno.domain.model.Severidad

/**
 * Entidades de persistencia. Son distintas de los modelos de dominio a
 * propósito: la base de datos puede cambiar sin arrastrar al dominio.
 */

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val rut: String,
    val claveHash: String,
    val nombre: String,
    val rol: Rol,
    /** Vacío = acceso a toda la red. */
    val parkings: List<String>
)

@Entity(tableName = "parkings")
data class ParkingEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val comuna: String
)

@Entity(tableName = "tickets", indices = [Index("parkingId"), Index("equipoId")])
data class TicketEntity(
    @PrimaryKey val id: String,
    val parkingId: String,
    val equipoId: String,
    val falla: String,
    val severidad: Severidad,
    val estado: EstadoTicket,
    val autor: String,
    val observacion: String,
    val creado: Long,
    val cerrado: Long?,
    /** URI del archivo copiado al almacenamiento interno de la app. */
    val fotoUri: String?
)

@Entity(
    tableName = "eventos",
    indices = [Index("ticketId")],
    foreignKeys = [
        ForeignKey(
            entity = TicketEntity::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: String,
    val instante: Long,
    val autor: String,
    val detalle: String
)

@Entity(tableName = "mantenciones", indices = [Index(value = ["parkingId", "semana"], unique = true)])
data class MantencionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parkingId: String,
    val semana: String,
    val etiquetaSemana: String,
    val autor: String,
    val instante: Long,
    val respuestas: Map<String, RespuestaPauta>,
    val observacion: String,
    val fotoUri: String?,
    val firmadaConBiometria: Boolean
)

@Entity(tableName = "rondas", indices = [Index("parkingId")])
data class RondaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parkingId: String,
    val autor: String,
    val instante: Long,
    val resultados: Map<String, ResultadoRonda>
)
