# Sesión 14 · Paso 1. Ficha de integración y alcance de entrega

**Responsable:** Wagner Qquerar Mayta · **Código:** 202210261 · **Modalidad:** individual

**Revisión:** 11 de noviembre de 2026 · **Rama:** `feature/builds-14`

**Base:** `e6a4496` de `feature/integrador-13`

Esta ficha cierra la revisión del [Producto D de la sesión 13](sesion-13-autonoma-producto-d.md). Los números conservan la referencia a esa ficha. Los casos 14 y 15 salieron de la revisión del CRUD antes de firmar.

| N.º | Hallazgo | Gravedad | Decisión | Evidencia o condición de cierre |
| --- | --- | --- | --- | --- |
| 1 | Un conflicto HTTP 409 podía borrar el alta local. | Alta | Cerrado el 04/11/2026. | La prueba simula el conflicto, conserva la fila y su motivo y procesa las demás altas. [Pruebas offline](sesion-13-paso-7.md). |
| 2 | La cola carecía de intentos y errores por fila. | Alta | Cerrado el 04/11/2026. | Migración verificada en SQLite y prueba de HTTP 503 con espera creciente y límite de cinco intentos. [Pruebas offline](sesion-13-paso-7.md). |
| 3 | Un alta con respuesta incierta podía volver a enviarse. | Alta | Cerrado el 04/11/2026. | La prueba verifica que el envío queda para revisión y que la próxima sincronización no repite el POST. [Pruebas offline](sesion-13-paso-7.md). |
| 4 | Faltaba un resumen de pendientes y fallidos. | Media | Cerrado el 04/11/2026. | La interfaz muestra conteos, antigüedad y motivos. [Capturas](sesion-13-paso-4.md). |
| 5 | Un alta rechazada parecía confirmada por el servidor. | Alta | Cerrado el 04/11/2026. | Se distingue el guardado local y el rechazo queda visible para revisión. [Caso 7](sesion-13-autonoma-producto-a.md). |
| 6 | El JWT vencido se mostraba como falta de conexión. | Alta | Cerrado el 04/11/2026. | El HTTP 401 de sincronización muestra «Sesión vencida». [Caso 9](sesion-13-autonoma-producto-a.md). |
| 7 | El rechazo de un alta aparece separado del campo Nombre. | Media | Limitación de la entrega. | Las altas se guardan localmente y el rechazo queda en **Revisar fallos**. Mostrarlo en el formulario de alta requeriría vincular ese formulario a un envío que puede terminar después de cerrarlo. En edición con red se conserva el formulario y se muestra el rechazo del servidor junto al campo cuando la respuesta lo identifica. |
| 8 | La reconexión requiere pulsar **Sincronizar** para subir el pendiente. | Media | Sincronización automática descartada para esta entrega. | El flujo manual se comprobó y permite terminar el recorrido. El disparo automático requeriría validar el comportamiento con la app en primer y segundo plano. [Casos 5 y 6](sesion-13-autonoma-producto-a.md). |
| 9 | Al reabrir sin red se pierde el acceso de escritura hasta comprobar los permisos. | Alta | Limitación de la entrega. | En modo avión, mantener abierta una sesión ya iniciada para probar altas. Tras reabrir, se puede consultar el catálogo guardado en solo lectura. Recuperar permisos offline exige una política de caducidad y revocación. [Línea base](sesion-13-paso-1.md). |
| 10 | Falta reautenticar desde el catálogo y recuperar el borrador. | Alta | Limitación de la entrega. | El catálogo avisa del 401 y se recupera el acceso al volver a iniciar sesión. Recuperar un borrador exige definir su persistencia y el comportamiento al cambiar de cuenta. [Caso 9](sesion-13-autonoma-producto-a.md). |
| 11 | Otra cuenta puede ver datos pendientes de la anterior en la misma instalación. | Alta | Limitación de la entrega. | Usar una cuenta por instalación o perfiles de Android separados para pruebas de roles. Evitar cambiar de cuenta con una cola local. Habilitar el uso compartido requiere aislar catálogo y cola por cuenta y comprobar la conservación de pendientes. [Línea base y cierre de sesión](sesion-13-autonoma-producto-a.md). |
| 12 | Falta generar y validar el build de iOS. | Media | Limitación de la entrega. | En Windows compiló Kotlin para el simulador y el enlace quedó `SKIPPED`. Para cerrar se necesita macOS con Xcode, un destino de prueba y acceso a PharmaSoft desde iOS. [Evidencia](sesion-13-autonoma-producto-b.md). |
| 13 | Falta comprobar la llegada de un informe real a Crashlytics. | Media | Limitación de la entrega. | La integración y la prueba simulada están disponibles. Falta registrar la app Android en Firebase y verificar un informe no fatal en la consola. [Paso 6](sesion-13-paso-6.md). |
| 14 | La edición no estaba implementada en el repositorio activo. | Alta | Cerrado el 11/11/2026. | En Android se editó el producto de prueba 87. El catálogo local y PharmaSoft mostraron el nombre editado con la misma categoría y estado. Una edición inválida mostró el rechazo junto a Nombre. [Validación](evidencias/sesion-14-paso-1/01-validacion-edicion.png) · [Edición](evidencias/sesion-14-paso-1/02-edicion-confirmada.png). |
| 15 | Una baja rechazada podía desaparecer del catálogo local y mostrarse como exitosa. | Alta | Cerrado el 11/11/2026. | Una prueba simuló HTTP 403 y comprobó que la fila local seguía visible. En el Pixel_10a, la baja del producto 87 lo retiró del catálogo activo en la app y en PharmaSoft. [Baja confirmada](evidencias/sesion-14-paso-1/03-baja-confirmada.png). |

## Alcance congelado

- **Plataforma de entrega.** Android con el identificador existente `pe.edu.upeu.pharmamobile`. El AAB firmado será el artefacto de entrega y el APK firmado se usará para instalar y recorrer la aplicación.
- **Flujo incluido.** Inicio y cierre de sesión, catálogo de Productos, roles existentes, CRUD con conexión, lectura desde SQLite, altas en modo avión durante una sesión activa, sincronización manual al reconectar y permiso de notificaciones con sus tres estados.
- **Límites operativos.** Las ediciones de productos confirmados y sus bajas requieren acceso a PharmaSoft. Para editar un alta local, primero debe sincronizarse. El build se valida en perfiles de prueba separados para cuentas distintas.
- **Cambios restantes.** Configurar firma, URL de prueba para teléfono, versión y empaquetado. Durante la validación de release se limitarán los cambios a defectos que bloqueen este recorrido y a los ajustes de minificación que requiera el build.
- **Evidencia de entrega.** Verificar el titular y la huella del certificado, recorrer el APK en dos Android físicos y registrar los defectos por equipo. La evidencia del emulador corresponde al cierre previo a firma.

## Verificación previa a firma

El 11 de noviembre se ejecutó `.\gradlew.bat --offline :shared:testAndroidHostTest :androidApp:assembleDebug`. Pasaron las 24 pruebas. En un perfil temporal del Pixel_10a se creó un producto de prueba, se verificó la validación del servidor, se editó y se dio de baja. La baja de PharmaSoft retiró el producto del listado activo y la consulta histórica por ID siguió respondiendo.
