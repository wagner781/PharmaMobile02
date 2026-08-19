package pe.edu.upeu.pharmamobile

import pe.edu.upeu.pharmamobile.domain.model.Cliente
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedLogicAndroidHostTest {

    @Test
    fun ClienteTelefono() {
        val cliente = Cliente(
            id=1L,
            nombre="Farmacia Central",
            correo="ventas@central.pe",
            telefono="998765432"
        )
        val resultado = cliente.obtenerTelefono()

        assertEquals(
            expected = "998765432",
            actual = resultado
        )
    }
}