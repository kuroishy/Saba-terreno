package cl.duoc.sabaterreno.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Sesión recordada entre aperturas de la app. */
data class SesionGuardada(val rut: String, val parkingId: String?)

private val Context.sesionDataStore by preferencesDataStore(name = "sesion")

/**
 * Recuerda en qué parking quedó trabajando la persona, para que al volver a
 * abrir la app no tenga que elegir de nuevo a mitad de turno.
 *
 * Guarda sólo el RUT y el parking: **nunca la contraseña**. Los datos del
 * usuario se vuelven a leer de la base en cada arranque, así que si cambia su
 * rol o sus parkings asignados, la sesión restaurada ya refleja el cambio.
 */
class SesionStore(private val context: Context) {

    private object Claves {
        val RUT = stringPreferencesKey("rut")
        val PARKING = stringPreferencesKey("parking")
    }

    val sesion: Flow<SesionGuardada?> = context.sesionDataStore.data.map { prefs ->
        val rut = prefs[Claves.RUT] ?: return@map null
        SesionGuardada(rut, prefs[Claves.PARKING])
    }

    suspend fun recordarUsuario(rut: String) {
        context.sesionDataStore.edit { prefs ->
            prefs[Claves.RUT] = rut
            prefs.remove(Claves.PARKING)
        }
    }

    suspend fun recordarParking(parkingId: String) {
        context.sesionDataStore.edit { prefs -> prefs[Claves.PARKING] = parkingId }
    }

    suspend fun olvidarParking() {
        context.sesionDataStore.edit { prefs -> prefs.remove(Claves.PARKING) }
    }

    suspend fun olvidar() {
        context.sesionDataStore.edit { prefs -> prefs.clear() }
    }
}
