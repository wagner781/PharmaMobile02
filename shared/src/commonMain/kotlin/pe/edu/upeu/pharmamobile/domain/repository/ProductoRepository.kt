package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto

interface ProductRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>
}