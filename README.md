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