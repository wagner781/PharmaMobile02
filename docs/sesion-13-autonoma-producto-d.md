# Actividad Autónoma 13 · Producto D. Ficha de integración

**Estudiante:** Wagner Qquerar Mayta · **Código:** 202210261

**Fecha de revisión:** 4 de noviembre de 2026 · **Versión evaluada:** [8c0a3c6](https://github.com/wagner781/PharmaMobile02/commit/8c0a3c6e781f9848d7bf613d56d8367e0ae7b08b) · **Rama:** [feature/integrador-13](https://github.com/wagner781/PharmaMobile02/tree/feature/integrador-13)

La ficha parte de los [hallazgos de la práctica](sesion-13-paso-1.md) y los contrasta con el [recorrido Android de la actividad autónoma](sesion-13-autonoma-producto-a.md). Las fechas de los pendientes son metas de revisión para la sesión 14. El cierre requiere la verificación indicada en cada fila.

## Hallazgos cerrados

| N.º | Hueco detectado | Gravedad | Decisión y evidencia de cierre | Fecha de cierre |
| --- | --- | --- | --- | --- |
| 1 | Un conflicto HTTP 409 podía borrar el alta local. | Alta | **Cerrado en sesión 13.** La fila conserva el motivo y la cola sigue con las demás altas. Verificado con `MockEngine` en el [paso 7](sesion-13-paso-7.md). | 04/11/2026 |
| 2 | Los fallos interrumpían la subida y faltaban intentos y errores por fila. | Alta | **Cerrado en sesión 13.** La migración añadió los campos y la prueba HTTP 503 verificó la espera creciente y el límite de cinco intentos. [Pruebas offline](sesion-13-paso-7.md). | 04/11/2026 |
| 3 | Un POST sin respuesta podía volver a enviarse y duplicar el alta. | Alta | **Cerrado en sesión 13.** El envío incierto queda para revisión. La prueba simula un HTTP 503 tras el POST y comprueba que la sincronización siguiente no lo repite. [Pruebas offline](sesion-13-paso-7.md). | 04/11/2026 |
| 4 | El catálogo carecía de un resumen de pendientes y fallidos. | Media | **Cerrado en sesión 13.** El Pixel_10a mostró los dos conteos y el motivo al abrir **Revisar fallos**. [Capturas del paso 4](sesion-13-paso-4.md). | 04/11/2026 |
| 5 | El alta indicaba éxito aunque el servidor rechazara el nombre. | Alta | **Cerrado en sesión 13.** La app distingue el guardado local y conserva el rechazo para revisión. El caso 7 del [Producto A](sesion-13-autonoma-producto-a.md) muestra el HTTP 400 y su motivo. La ubicación del mensaje en el formulario se trata en el punto 7. | 04/11/2026 |
| 6 | El catálogo presentaba el JWT vencido como si se hubiera perdido la conexión. | Alta | **Cerrado en sesión 13.** Con un JWT vencido, la sincronización devolvió HTTP 401 y la app mostró «Sesión vencida. Inicia sesión nuevamente». [Caso 9 del Producto A](sesion-13-autonoma-producto-a.md). | 04/11/2026 |

## Hallazgos abiertos para la sesión 14

| N.º | Hueco detectado | Gravedad | Decisión y criterio de cierre | Fecha objetivo |
| --- | --- | --- | --- | --- |
| 7 | El HTTP 400 de Nombre aparece en **Revisar fallos** y falta mostrarlo junto al campo. | Media | **Sesión 14.** Repetir el alta inválida `ZQ` con conexión y verificar que el error se señale junto a **Nombre** cuando el formulario esté disponible. [Caso 7](sesion-13-autonoma-producto-a.md). | 10/11/2026 |
| 8 | Recuperar la conexión deja el alta pendiente hasta pulsar **Sincronizar**. | Media | **Sesión 14.** Evaluar la sincronización al reconectar. Si se incorpora, verificar que el alta pendiente llegue una sola vez. Si se mantiene la acción manual, registrar la decisión y su justificación. [Casos 5 y 6](sesion-13-autonoma-producto-a.md). | 10/11/2026 |
| 9 | Una cuenta con escritura queda en solo lectura al reabrir sin conexión. | Alta | **Sesión 14.** Revisar la recuperación local de los permisos. Probar que una sesión válida conserve los privilegios que correspondan al reabrir en modo avión, con reglas explícitas de caducidad. [Línea base](sesion-13-paso-1.md). | 10/11/2026 |
| 10 | El JWT vencido requiere reabrir la app para regresar al acceso. Falta recuperar el flujo cuando hay un formulario abierto. | Alta | **Sesión 14.** Probar un JWT de 60 segundos con un borrador abierto. Verificar que se pueda volver a autenticar y recuperar el borrador. [Caso 9](sesion-13-autonoma-producto-a.md). | 10/11/2026 |
| 11 | Las altas pendientes de una cuenta pueden quedar visibles al entrar con otra. | Alta | **Sesión 14.** Aislar la cola local por cuenta. Repetir el cambio de administrador a vendedor y verificar que el vendedor no vea el alta del administrador y que su propietario la recupere. [Línea base](sesion-13-paso-1.md) · [Cierre observado](sesion-13-autonoma-producto-a.md). | 10/11/2026 |
| 12 | Falta verificar el recorrido y la entrega de notificaciones en iOS. | Media | **Sesión 14.** Conseguir una Mac con Xcode, enlazar el framework y ejecutar el recorrido. La compilación Kotlin en Windows pasó y el enlace quedó `SKIPPED`. [Estado de iOS](sesion-13-autonoma-producto-b.md). | 10/11/2026, sujeto a acceso a una Mac |
| 13 | Falta confirmar la recepción de un informe real de Crashlytics. | Media | **Sesión 14.** Registrar la app Android en Firebase, añadir `google-services.json` y verificar en la consola un informe no fatal generado con un HTTP 503 controlado. [Integración y prueba](sesion-13-paso-6.md). | 10/11/2026, sujeto al registro en Firebase |

Para la revisión de la sesión 14, la prioridad es aislar los datos por cuenta y resolver la recuperación de sesión. La ficha conserva todos los hallazgos de la línea base.
