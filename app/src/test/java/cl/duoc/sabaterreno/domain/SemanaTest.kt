package cl.duoc.sabaterreno.domain

import cl.duoc.sabaterreno.domain.rules.SemanaOperacional
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SemanaTest {

    private fun instante(fechaHora: String): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("America/Santiago") }
            .parse(fechaHora)!!
            .time

    @Test
    fun `el lunes abre su propia semana`() {
        // 2026-10-26 es lunes.
        assertEquals("2026-10-26", SemanaOperacional.de(instante("2026-10-26 08:00")).clave)
    }

    @Test
    fun `el domingo sigue perteneciendo a la semana que abrio el lunes`() {
        assertEquals("2026-10-26", SemanaOperacional.de(instante("2026-11-01 23:30")).clave)
    }

    @Test
    fun `un dia intermedio cae en la misma semana`() {
        assertEquals("2026-10-26", SemanaOperacional.de(instante("2026-10-30 15:00")).clave)
    }

    @Test
    fun `el lunes siguiente abre una semana distinta`() {
        val estaSemana = SemanaOperacional.de(instante("2026-11-01 23:59")).clave
        val siguiente = SemanaOperacional.de(instante("2026-11-02 00:01")).clave
        assertNotEquals(estaSemana, siguiente)
        assertEquals("2026-11-02", siguiente)
    }

    @Test
    fun `la etiqueta cubre de lunes a domingo`() {
        assertEquals("26 oct al 1 nov", SemanaOperacional.de(instante("2026-10-30 12:00")).etiqueta)
    }
}
