# Sesión 13 · Paso 7. Pruebas sin red y sin dispositivo

Desde la raíz del proyecto, ejecuta en PowerShell:

```powershell
.\gradlew.bat --offline :shared:testAndroidHostTest
```

Gradle usa las dependencias que ya están en caché. Las pruebas se ejecutan
en la JVM con SQLite en memoria, `MockEngine` para HTTP y un gestor de
permisos falso. Pueden ejecutarse con el emulador y PharmaSoft apagados.

**Resultado:** 22 pruebas aprobadas, sin fallos ni pruebas omitidas.
También pasó `:androidApp:assembleDebug` con Gradle en modo offline.

| Caso | Comprobación |
| --- | --- |
| HTTP 503 antes de enviar | Suma intentos por fila, aplica las esperas con tiempo virtual y deja la fila fallida al llegar al límite. |
| HTTP 409 al enviar | Conserva la fila y el motivo, excluye el conflicto del reenvío y procesa las demás altas antes de descargar. |
| HTTP 503 después de un POST | Conserva el envío incierto para revisión y comprueba que una segunda sincronización no lo reenvía. |
| Alta aceptada con fallo en la descarga | Conserva el producto con su ID definitivo y la fecha de última descarga sigue vacía. |
| Resumen observable | Una sola suscripción recibe el pendiente, el fallo con su motivo y la fecha de la descarga. |
| Permiso de notificaciones | Verifica la primera denegación, el reintento, el acceso a Ajustes, el envío con permiso y la revocación antes de enviar. |
| Telemetría | Verifica los eventos y las categorías de error mediante un adaptador falso. La recepción de informes reales sigue pendiente de configurar el proyecto Firebase. |

La prueba de permisos cubre las decisiones del código común. El diálogo y
los Ajustes de Android se verificaron en el [paso 5](sesion-13-paso-5.md).
