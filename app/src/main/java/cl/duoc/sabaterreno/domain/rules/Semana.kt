package cl.duoc.sabaterreno.domain.rules

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Semana operacional, de lunes a domingo.
 *
 * La pauta de mantención se cumple una vez por semana, así que necesitamos una
 * clave estable para saber si la de esta semana ya se hizo. El cálculo es una
 * función pura sobre un instante, de modo que se puede probar con fechas fijas
 * en vez de depender de cuándo se ejecute el test.
 */
data class Semana(val clave: String, val etiqueta: String)

object SemanaOperacional {

    private val ZONA: TimeZone = TimeZone.getTimeZone("America/Santiago")
    private val ESPANOL = Locale("es", "CL")

    /** Semana a la que pertenece [instante]. */
    fun de(instante: Long): Semana {
        val cal = Calendar.getInstance(ZONA, ESPANOL).apply {
            timeInMillis = instante
            firstDayOfWeek = Calendar.MONDAY
        }
        // Calendar.SUNDAY es 1, así que esta cuenta deja el lunes en 0.
        val diasDesdeLunes = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        cal.add(Calendar.DAY_OF_MONTH, -diasDesdeLunes)
        val lunes = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 6)
        val domingo = cal.time

        val clave = formato("yyyy-MM-dd", Locale.US).format(lunes)
        val dia = formato("d MMM", ESPANOL)
        return Semana(clave, "${dia.format(lunes)} al ${dia.format(domingo)}")
    }

    fun actual(): Semana = de(System.currentTimeMillis())

    private fun formato(patron: String, locale: Locale) =
        SimpleDateFormat(patron, locale).apply { timeZone = ZONA }
}
