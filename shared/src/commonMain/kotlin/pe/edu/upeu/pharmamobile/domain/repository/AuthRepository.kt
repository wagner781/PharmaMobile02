package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(credenciales: Credenciales): Sesion
    suspend fun sesionGuardada(): Sesion?
    suspend fun cerrarSesion()
}
