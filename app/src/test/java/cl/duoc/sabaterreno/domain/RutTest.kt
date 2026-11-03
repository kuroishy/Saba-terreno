package cl.duoc.sabaterreno.domain

import cl.duoc.sabaterreno.domain.validation.Rut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutTest {

    @Test
    fun `acepta un rut valido con dv numerico`() {
        assertTrue(Rut.esValido("98.765.432-5"))
    }

    @Test
    fun `acepta un rut valido con dv K`() {
        assertTrue(Rut.esValido("15.420.895-K"))
    }

    @Test
    fun `acepta el rut sin formato`() {
        assertTrue(Rut.esValido("15420895k"))
    }

    @Test
    fun `rechaza un dv incorrecto`() {
        assertFalse(Rut.esValido("15.420.895-9"))
    }

    @Test
    fun `rechaza un cuerpo demasiado corto`() {
        assertFalse(Rut.esValido("123-5"))
    }

    @Test
    fun `rechaza texto vacio o basura`() {
        assertFalse(Rut.esValido(""))
        assertFalse(Rut.esValido("no-soy-un-rut"))
    }

    @Test
    fun `acepta un rut cuyo dv es cero`() {
        assertTrue(Rut.esValido("10.234.577-0"))
    }

    @Test
    fun `formatea con puntos y guion`() {
        assertEquals("15.420.895-K", Rut.formatear("15420895K"))
    }
}
