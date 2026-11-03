package cl.duoc.sabaterreno.domain

import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.model.Severidad
import cl.duoc.sabaterreno.domain.validation.Campo
import cl.duoc.sabaterreno.domain.validation.CierreValidator
import cl.duoc.sabaterreno.domain.validation.LoginValidator
import cl.duoc.sabaterreno.domain.validation.PautaValidator
import cl.duoc.sabaterreno.domain.validation.TicketValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresTest {

    @Test
    fun `login marca el rut invalido en su propio campo`() {
        val r = LoginValidator.validar(rut = "15.420.895-9", clave = "terreno2026")
        assertFalse(r.valido)
        assertTrue(r.tieneError(Campo.RUT))
        assertFalse(r.tieneError(Campo.CLAVE))
    }

    @Test
    fun `login exige clave minima`() {
        val r = LoginValidator.validar(rut = "15.420.895-K", clave = "123")
        assertTrue(r.tieneError(Campo.CLAVE))
    }

    @Test
    fun `login valido no devuelve errores`() {
        assertTrue(LoginValidator.validar("15.420.895-K", "terreno2026").valido)
    }

    @Test
    fun `ticket degradado no exige foto`() {
        val r = TicketValidator.validar(
            equipoId = "cajero1",
            falla = "Pantalla congelada",
            severidad = Severidad.DEGRADADO,
            observacion = "",
            fotoUri = null
        )
        assertTrue(r.valido)
    }

    @Test
    fun `ticket fuera de servicio exige foto y observacion`() {
        val r = TicketValidator.validar(
            equipoId = "barrera-out",
            falla = "Brazo no sube",
            severidad = Severidad.FUERA_DE_SERVICIO,
            observacion = "corta",
            fotoUri = null
        )
        assertFalse(r.valido)
        assertNotNull(r.mensajeDe(Campo.EVIDENCIA))
        assertNotNull(r.mensajeDe(Campo.OBSERVACION))
    }

    @Test
    fun `ticket fuera de servicio pasa con evidencia completa`() {
        val r = TicketValidator.validar(
            equipoId = "barrera-out",
            falla = "Brazo no sube",
            severidad = Severidad.FUERA_DE_SERVICIO,
            observacion = "Salida bloqueada, se cobra de forma manual.",
            fotoUri = "content://evidencia/1"
        )
        assertTrue(r.valido)
    }

    @Test
    fun `pauta exige firma`() {
        val r = PautaValidator.validar(
            totalItems = 2,
            respuestas = mapOf("a" to RespuestaPauta.CONFORME, "b" to RespuestaPauta.CONFORME),
            observacion = "",
            firmada = false
        )
        assertTrue(r.tieneError(Campo.FIRMA))
    }

    @Test
    fun `pauta con observados exige detalle`() {
        val r = PautaValidator.validar(
            totalItems = 2,
            respuestas = mapOf("a" to RespuestaPauta.OBSERVADO, "b" to RespuestaPauta.CONFORME),
            observacion = "",
            firmada = true
        )
        assertTrue(r.tieneError(Campo.OBSERVACION))
    }

    @Test
    fun `cierre exige una resolucion no vacia`() {
        val r = CierreValidator.validar("")
        assertFalse(r.valido)
        assertTrue(r.tieneError(Campo.RESOLUCION))
    }

    @Test
    fun `cierre rechaza una resolucion demasiado breve`() {
        assertFalse(CierreValidator.validar("listo").valido)
    }

    @Test
    fun `cierre acepta una resolucion con detalle`() {
        assertTrue(CierreValidator.validar("Se reemplazó el cable de red del terminal.").valido)
    }
}
