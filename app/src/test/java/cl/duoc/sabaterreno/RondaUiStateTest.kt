package cl.duoc.sabaterreno

import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import cl.duoc.sabaterreno.domain.model.TipoEquipo
import cl.duoc.sabaterreno.ui.pantallas.RondaUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El avance y el deshacer de la ronda son reglas del estado, no de la vista,
 * así que se pueden comprobar sin dibujar nada.
 */
class RondaUiStateTest {

    private val equipos = listOf(
        Equipo("barrera-in", "Barreras Entrada", TipoEquipo.BARRERA),
        Equipo("lpr", "Lectura LPR", TipoEquipo.LPR)
    )

    private fun responder(estado: RondaUiState, id: String, r: ResultadoRonda) = estado.copy(
        resultados = estado.resultados + (id to r),
        historial = estado.historial + id,
        indice = estado.indice + 1
    )

    private fun deshacer(estado: RondaUiState): RondaUiState {
        val ultimo = estado.historial.lastOrNull() ?: return estado
        return estado.copy(
            resultados = estado.resultados - ultimo,
            historial = estado.historial.dropLast(1),
            indice = (estado.indice - 1).coerceAtLeast(0)
        )
    }

    @Test
    fun `la ronda recien iniciada no esta terminada`() {
        val estado = RondaUiState(equipos = equipos)
        assertFalse(estado.terminada)
        assertFalse(estado.puedeDeshacer)
        assertEquals(0f, estado.progreso, 0.001f)
    }

    @Test
    fun `termina al responder todos los equipos`() {
        var estado = RondaUiState(equipos = equipos)
        estado = responder(estado, "barrera-in", ResultadoRonda.CONFORME)
        assertFalse(estado.terminada)
        estado = responder(estado, "lpr", ResultadoRonda.OBSERVADO)
        assertTrue(estado.terminada)
        assertEquals(1, estado.conformes)
        assertEquals(listOf(equipos[1]), estado.observados)
    }

    @Test
    fun `deshacer devuelve la tarjeta y olvida la respuesta`() {
        var estado = RondaUiState(equipos = equipos)
        estado = responder(estado, "barrera-in", ResultadoRonda.CONFORME)
        assertTrue(estado.puedeDeshacer)

        estado = deshacer(estado)
        assertEquals(0, estado.indice)
        assertTrue(estado.resultados.isEmpty())
        assertFalse(estado.puedeDeshacer)
    }

    @Test
    fun `deshacer en la primera tarjeta no rompe el indice`() {
        val estado = deshacer(RondaUiState(equipos = equipos))
        assertEquals(0, estado.indice)
    }
}
