package cl.duoc.sabaterreno.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        ParkingEntity::class,
        TicketEntity::class,
        EventoEntity::class,
        MantencionEntity::class,
        RondaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Convertidores::class)
abstract class SabaDatabase : RoomDatabase() {

    abstract fun usuarios(): UsuarioDao
    abstract fun parkings(): ParkingDao
    abstract fun tickets(): TicketDao
    abstract fun mantenciones(): MantencionDao
    abstract fun rondas(): RondaDao

    companion object {
        /**
         * La versión 2 suma la tabla de rondas de turno. Se migra creándola en
         * vez de recrear la base, para no perder los tickets ya registrados en
         * el dispositivo.
         */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS rondas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        parkingId TEXT NOT NULL,
                        autor TEXT NOT NULL,
                        instante INTEGER NOT NULL,
                        resultados TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_rondas_parkingId ON rondas (parkingId)")
            }
        }

        @Volatile private var instancia: SabaDatabase? = null

        fun obtener(context: Context): SabaDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    SabaDatabase::class.java,
                    "saba-terreno.db"
                ).addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instancia = it }
            }
    }
}
