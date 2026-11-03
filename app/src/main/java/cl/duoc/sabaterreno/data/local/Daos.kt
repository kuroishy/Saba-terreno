package cl.duoc.sabaterreno.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun porRut(rut: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuarios: List<UsuarioEntity>)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun total(): Int
}

@Dao
interface ParkingDao {
    @Query("SELECT * FROM parkings ORDER BY nombre")
    fun todos(): Flow<List<ParkingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(parkings: List<ParkingEntity>)
}

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY creado DESC")
    fun todos(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE parkingId = :parkingId ORDER BY creado DESC")
    fun porParking(parkingId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    fun porId(id: String): Flow<TicketEntity?>

    @Query("SELECT * FROM eventos WHERE ticketId = :id ORDER BY instante ASC")
    fun eventosDe(id: String): Flow<List<EventoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ticket: TicketEntity)

    @Update
    suspend fun actualizar(ticket: TicketEntity)

    @Insert
    suspend fun insertarEvento(evento: EventoEntity)

    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun total(): Int

    @Query("SELECT MAX(CAST(SUBSTR(id, 10) AS INTEGER)) FROM tickets")
    suspend fun ultimoCorrelativo(): Int?
}

@Dao
interface MantencionDao {
    @Query("SELECT * FROM mantenciones WHERE parkingId = :parkingId ORDER BY instante DESC")
    fun porParking(parkingId: String): Flow<List<MantencionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(mantencion: MantencionEntity)
}

@Dao
interface RondaDao {
    @Query("SELECT * FROM rondas WHERE parkingId = :parkingId ORDER BY instante DESC")
    fun porParking(parkingId: String): Flow<List<RondaEntity>>

    @Query("SELECT * FROM rondas WHERE parkingId = :parkingId ORDER BY instante DESC LIMIT 1")
    fun ultima(parkingId: String): Flow<RondaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ronda: RondaEntity): Long
}
