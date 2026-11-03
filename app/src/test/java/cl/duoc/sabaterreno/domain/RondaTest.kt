package cl.duoc.sabaterreno.domain

import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import cl.duoc.sabaterreno.domain.model.Ronda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RondaTest {

    private fun ronda(resultados: Map<String, ResultadoRonda>) = Ronda(
        parkingId = "p1",
        autor = "Camila Rojas",
        instante = 0L,
        resultados = resultados
    )

    @Test
    fun `cuenta conformes y observados por separado`() {
        val r = ronda(
            mapOf(
                "barrera-in" to ResultadoRonda.CONFORME,
                "lpr" to ResultadoRonda.OBSERVADO,
                "pos" to ResultadoRonda.CONFORME,
                "cajero1" to ResultadoRonda.OBSERVADO
            )
        )
        assertEquals(2, r.conformes)
        assertEquals(2, r.observados)
    }

    @Test
    fun `lista solo los equipos observados`() {
        val r = ronda(
            mapOf(
                "barrera-in" to ResultadoRonda.CONFORME,
                "lpr" to ResultadoRonda.OBSERVADO
            )
        )
        assertEquals(listOf("lpr"), r.equiposObservados)
    }

    @Test
    fun `una ronda sin observaciones no reporta equipos`() {
        val r = ronda(mapOf("barrera-in" to ResultadoRonda.CONFORME))
        assertEquals(0, r.observados)
        assertTrue(r.equiposObservados.isEmpty())
    }
}
