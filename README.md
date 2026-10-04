# PharmaMobile

Aplicación móvil multiplataforma para la gestión integral de inventarios, pedidos y clientes en el sector farmacéutico.

**Curso:** Desarrollo de Aplicaciones Móviles  
**Institución:** Universidad Peruana Unión - Facultad de Ingeniería y Arquitectura  
**Ciclo:** VIII - Semestre 2026-2

---

## Tecnologías utilizadas

- **Kotlin Multiplatform (KMP):** Lógica de negocio compartida entre Android e iOS.
- **Compose Multiplatform:** UI declarativa compartida.
- **Arquitectura:** Clean Architecture + MVVM (a implementar en fases posteriores).
- **Control de Versiones:** Git & GitHub.

---

## Estructura del Proyecto

- **`/shared/src/commonMain`**: Contiene modelos de datos (Cliente, Producto, Pedido), validaciones y reglas de negocio compartidas.
- **`/shared/src/androidMain`**: Implementaciones específicas para el ecosistema Android (SDK, permisos).
- **`/shared/src/iosMain`**: Implementaciones específicas para el ecosistema Apple (iOS SDK).
- **`/androidApp`**: Aplicación Android (entry point).
- **`/iosApp`**: Aplicación iOS (entry point para Xcode).

---

## Cómo ejecutar el proyecto

- **Android App:** Usa el botón "Run" en Android Studio o ejecuta:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```

---

## Consumo de API (Ktor)

- **URL Base:** `https://api.escuelajs.co/api/v1/`
- **Endpoint consumido:** `GET /products?limit={limite}`
- **DTOs Implementados:** `ProductoDto`, `CategoriaDto`

---

## Manejo de errores

El proyecto implementa un manejo de errores fuertemente tipado utilizando `sealed classes` (tipo `ErrorApi`), permitiendo que cada tipo de fallo tenga una representación explícita en la UI:
- **Validacion:** Mapeo automático de los errores enviados por el servidor (ej: 400 Bad Request) mostrados debajo del campo de texto correspondiente.
- **NoEncontrado:** Manejo de error 404 (ej. cuando se intenta actualizar o borrar un producto que ya no existe o fue eliminado).
- **Conflicto:** Manejo de reglas de negocio del backend, mostrando mensajes al intentar romper restricciones de base de datos (ej: error 409).
- **TiempoAgotado y SinConexion:** Manejo resiliente y controlado ante fallas de conectividad a través de capturas de excepciones de Ktor, comunicando el estado al usuario sin cerrar la aplicación.