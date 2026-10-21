# PharmaMobile

Aplicación móvil multiplataforma para la gestión integral de inventarios, pedidos y clientes en el sector farmacéutico.

**Curso:** Desarrollo de Aplicaciones Móviles  
**Institución:** Universidad Peruana Unión - Facultad de Ingeniería y Arquitectura  
**Ciclo:** VIII - Semestre 2026-2  

---

## Tecnologías utilizadas

- **Kotlin Multiplatform (KMP):** Lógica de negocio compartida entre Android e iOS.
- **Compose Multiplatform:** UI declarativa reactiva compartida entre plataformas.
- **Ktor Client 3.6.0:** Cliente HTTP multiplataforma con plugins de negociación de contenido, autenticación y logging.
- **Koin 4.2.2:** Inyección de dependencias modular (`platformModule`, `dataModule`, `domainModule`, `presentationModule`).
- **Arquitectura:** Clean Architecture + MVVM con StateFlow y sealed interfaces para estados de UI.
- **Android Keystore & Jetpack Security:** Cifrado de credenciales con `EncryptedSharedPreferences` y claves AES256-GCM.
- **Control de Versiones:** Git & GitHub con flujo de ramas por feature.

---

## Estructura del Proyecto

- **`/shared/src/commonMain`**: Modelos de dominio (`Producto`, `Sesion`, `ErrorSesion`), contratos de repositorio (`AuthRepository`, `ProductoRepository`), casos de uso, cliente Ktor configurado y componentes UI compartidos (`LoginScreen`, `MainScreen`, `ProductoScreen`).
- **`/shared/src/androidMain`**: Implementaciones nativas de Android: motor HTTP Android, almacenamiento cifrado `AlmacenSeguroAndroid` (`EncryptedSharedPreferences`), formateo de moneda y compartidor nativo.
- **`/shared/src/iosMain`**: Implementaciones nativas de Apple (iOS SDK): motor HTTP Darwin, almacenamiento seguro `AlmacenSeguroIos`, formateo de moneda y `UIActivityViewController`.
- **`/androidApp`**: Aplicación Android (Entry Point con Compose Activity).
- **`/iosApp`**: Aplicación iOS (Entry Point para Xcode).

---

## Cómo ejecutar el proyecto

1. **Levantar el Backend PharmaSoft:**
   Asegúrate de que el backend Spring Boot esté en ejecución en el puerto 8080 con Flyway aplicado (migraciones V2 y V3 de seguridad).
   - Documentación Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   
2. **Ejecutar la App Android:**
   Usa el botón "Run" en Android Studio o ejecuta desde terminal:
   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

---

## Autenticación JWT y Almacenamiento Seguro (Laboratorio 10)

La aplicación integra autenticación JWT completa contra el backend PharmaSoft, garantizando la seguridad en el transporte y en el almacenamiento local de credenciales:

### 1. Endpoints de Autenticación
- **Inicio de sesión:** `POST /api/v1/auth/login` (recibe `{ "username", "password" }`, devuelve token JWT, roles y permisos).
- **Usuario actual:** `GET /api/v1/auth/me` (consulta los datos del usuario logueado usando el token Bearer).

### 2. Almacén Seguro Multiplataforma (`AlmacenSeguro`)
El token sobrevive al cierre de la app sin almacenarse en texto plano:
- **Contrato:** Interfaz `AlmacenSeguro` en `commonMain/data/local`.
- **Android:** Implementado en `AlmacenSeguroAndroid` utilizando `EncryptedSharedPreferences` y llave maestra `MasterKey` con esquema AES256_GCM gestionada por Android Keystore.
- **iOS:** Implementado en `AlmacenSeguroIos` usando almacenamiento nativo con soporte del paquete `platform.Security`.
- **Resolución:** Registrado en Koin a través del `platformModule` de cada plataforma.

### 3. Automatización con Plugins de Ktor
- **Plugin Auth (Bearer):**
  - `loadTokens`: Lee el token JWT automáticamente desde `AlmacenSeguro` e inyecta la cabecera `Authorization: Bearer <token>`.
  - `sendWithoutRequest`: Envía el token en la primera petición para todas las rutas excepto `/auth/login`, evitando peticiones duplicadas y ciclos 401.
  - `clearToken()`: Limpia la caché interna del proveedor Bearer inmediatamente tras el login y el logout.
- **Plugin Logging con Sanitización:**
  - Nivel configurado en `LogLevel.HEADERS`.
  - Regla `sanitizeHeader { it == HttpHeaders.Authorization }`: Oculta el token en Logcat mostrándolo como `Bearer ***` para prevenir fugas de credenciales en los logs.

### 4. Manejo de Errores y Estados de Sesión (`ErrorSesion`)
- **401 Unauthorized:** Si el mensaje indica expiración, se mapea a `ErrorSesion.SesionExpirada`, cerrando la sesión, limpiando el almacén y redirigiendo al login con un aviso. Si no, se mapea a `ErrorSesion.CredencialesInvalidas`.
- **403 Forbidden (`ErrorSesion.SinPermiso`):** Mantiene la sesión del usuario activa y muestra un mensaje informativo en pantalla sin enviarlo al login.
- **Sin Conexión (`ErrorSesion.SinConexion`):** Manejo controlado ante interrupciones de red.

### 5. Control de Acceso basado en Permisos (RBAC) en la UI
- **Ocultamiento de Acciones en Catálogo:** Mediante `sesion.puede("CATALOGO_ESCRIBIR")` se muestra u oculta el formulario y el botón de registrar/actualizar productos en `ProductoScreen`.
- **Navegación Dinámica en Menú Lateral:** Opciones como *Clientes* solo se muestran si el usuario cuenta con el permiso correspondiente (`CLIENTES_LEER`).
- **Cierre de Sesión:** Opción accesible desde el Drawer que borra el token del almacenamiento seguro, limpia la caché de Ktor y devuelve al usuario a `LoginScreen`.

---

## Usuarios de Prueba (PharmaSoft)

Cargados automáticamente mediante migraciones de Flyway:

| Usuario | Contraseña | Rol | Permisos asignados | Comportamiento en PharmaMobile |
| :--- | :--- | :--- | :--- | :--- |
| **`admin`** | `Admin123` | **ADMIN** | Los 8 permisos del sistema | Acceso completo: registro, edición y eliminación de productos, acceso a clientes y ventas. |
| **`vendedor`** | `Vendedor123` | **VENDEDOR** | Catálogo solo lectura; clientes y ventas completos | Catálogo en modo solo lectura (formulario y botones de registrar/editar ocultos); secciones de clientes y ventas habilitadas. |
| **`almacen`** | `Almacen123` | **ALMACENERO** | Solo catálogo: lectura y escritura | Puede ver y registrar productos en el catálogo; la sección de clientes no se ofrece en el menú. |

---

## Consumo del API REST (Ktor)

- **URL Base:** `http://10.0.2.2:8080` (emulador Android) / `http://localhost:8080` (máquina local).
- **Endpoints del CRUD:**
  - `GET /api/v1/productos` (Protegido con JWT)
  - `POST /api/v1/productos` (Requiere permiso `CATALOGO_ESCRIBIR`)
  - `PUT /api/v1/productos/{id}` (Requiere permiso `CATALOGO_ESCRIBIR`)
  - `DELETE /api/v1/productos/{id}` (Requiere permiso `CATALOGO_ESCRIBIR`)
  - `GET /api/v1/clientes` (Requiere permiso `CLIENTES_LEER`)

---

## Capacidades Nativas (Mecanismo Expect / Actual e Interfaces)

1. **Formateo de Moneda (expect/actual):**
   - Contrato: `expect fun formatearSoles`
   - Android: `NumberFormat.getCurrencyInstance` (`Formato.android.kt`).
   - iOS: `NSNumberFormatter` (`Formato.ios.kt`).

2. **Compartir Producto (Interfaz + Inyección Koin):**
   - Contrato: Interfaz `Compartidor` en `commonMain`.
   - Android: `Intent.ACTION_SEND` e `Intent.createChooser` (`CompartidorAndroid.kt`).
   - iOS: `UIActivityViewController` nativo (`CompartidorIos.kt`).

3. **Copiado al Portapapeles (expect/actual):**
   - Contrato: `expect class Portapapeles` en `commonMain`.
   - Android: `ClipboardManager` inyectando contexto (`Portapapeles.android.kt`).
   - iOS: `UIPasteboard` y `UIAlertController` (`Portapapeles.ios.kt`).

---

## Persistencia Local con SQLDelight y Estrategia Offline-First (Laboratorio 11)

La aplicación implementa una arquitectura **offline-first** donde la base de datos local SQLite pasa a ser la **única fuente de verdad** (*Single Source of Truth*), garantizando que la app continúe funcionando de forma transparente aún sin conexión a internet.

### 1. Principios de la Arquitectura
- **Única Fuente de Verdad:** La interfaz gráfica nunca consulta directamente a la red al abrirse; se suscribe mediante un `Flow` reactivo a la base de datos local. La red opera como un proceso en segundo plano que mantiene sincronizada dicha base.
- **Eliminación de Recargas Manuales:** Se eliminan todas las llamadas manuales de recarga (`cargarProductos()`); cualquier inserción o actualización local dispara automáticamente la emisión del `Flow` hacia el ViewModel y la UI.
- **Identificadores Locales Negativos:** Los registros creados sin red reciben identificadores negativos (`minOf(menor, 0L) - 1L`, ej. `-1`, `-2`) para evitar colisiones con los IDs autoincrementales asignados por PharmaSoft y reconocer de un vistazo los registros locales aún no sincronizados.

### 2. Esquema SQLDelight (`Producto.sq`)
Ubicado en `shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobil/db/Producto.sq`:
```sql
CREATE TABLE producto (
    id INTEGER NOT NULL PRIMARY KEY,
    nombre TEXT NOT NULL,
    precio REAL NOT NULL,
    stock INTEGER NOT NULL,
    pendiente INTEGER NOT NULL DEFAULT 0
);

listarTodos:
SELECT * FROM producto ORDER BY nombre ASC;

listarPendientes:
SELECT * FROM producto WHERE pendiente = 1;

insertar:
INSERT OR REPLACE INTO producto
VALUES (?, ?, ?, ?, ?);

borrarSincronizados:
DELETE FROM producto WHERE pendiente = 0;

siguienteIdLocal:
SELECT COALESCE(MIN(id), 0) FROM producto;

borrarPorId:
DELETE FROM producto WHERE id = ?;
```

### 3. Drivers de Base de Datos Multiplataforma
- **Contrato Común:** `CreadorDeDriver` en `commonMain/data/local/` con `fun crear(): SqlDriver`.
- **Android:** `CreadorDeDriverAndroid` utilizando `AndroidSqliteDriver(PharmaDb.Schema, context, "pharma.db")`.
- **iOS:** `CreadorDeDriverIos` utilizando `NativeSqliteDriver(PharmaDb.Schema, "pharma.db")`.
- **Pruebas Unitarias:** `JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)` con creación aislada de esquema por cada test.
- **Inyección con Koin:** Registrado en los módulos de plataforma (`platformModule`) y expuesta la instancia `PharmaDb` en el módulo común `dataModule`.

### 4. Regla Crítica de Sincronización: Subir y Luego Bajar
En `ProductoRepositorioOffline`:
```kotlin
override suspend fun sincronizar() {
    subirPendientes()
    local.reemplazarSincronizados(remoto.listar())
}
```
1. **Primero subir (`subirPendientes`):** Recorre los elementos con `pendiente = 1`, los envía a PharmaSoft (`remoto.registrar`) y los limpia de la cola local una vez aceptados por el backend.
2. **Luego bajar (`reemplazarSincronizados`):** Descarga la lista actualizada desde el servidor y reemplaza dentro de una transacción atómica únicamente las filas sincronizadas (`borrarSincronizados()`), preservando cualquier trabajo offline en progreso.
3. **Resiliencia de Red:** La operación se protege mediante `intentar { ... }` capturando `ErrorApi.SinConexion`, lo que garantiza que una pérdida de señal no interrumpa el funcionamiento local.

### 5. Verificación del Ciclo Offline Completo
Validación en 6 puntos sobre el emulador Android y el servidor PharmaSoft:
1. **Con red:** Apertura de la aplicación, inicio de sesión y sincronización inicial de productos.
2. **Activación de Modo Avión:** Cierre y reapertura de la aplicación en modo avión.
3. **Persistencia offline:** Los productos se muestran inmediatamente desde la base local con el aviso *"Sin conexión: mostrando datos guardados"*.
4. **Registro sin conexión:** Creación de un producto en modo avión con ID local negativo (`-1`) y etiqueta *"⏳ Pendiente de envío"*.
5. **Reconexión y sincronización:** Desactivación de modo avión y pulsación del botón sincronizar; el ID temporal es reemplazado por el ID positivo real del servidor y se retira el aviso de red.
6. **Verificación en Swagger:** Comprobación en Swagger (`GET /api/v1/productos`) de que el producto existe en PharmaSoft con su ID asignado.

### 6. Pruebas Unitarias en `commonTest`
Implementadas en `ProductoRepositorioOfflineTest.kt` con base en memoria (`JdbcSqliteDriver`):
1. `sin red el producto queda pendiente y la pantalla lo ve`: Valida la persistencia local con ID negativo y emisión por Flow sin red.
2. `con red se sincroniza y actualiza la lista local`: Valida la descarga e inserción de datos remotos en la base local.
3. `sincronizar sube pendientes antes de bajar del servidor`: Valida el flujo completo de subida previa y posterior recepción de identificadores reales.
- **Resultado:** 100% de pruebas aprobadas (`BUILD SUCCESSFUL`).