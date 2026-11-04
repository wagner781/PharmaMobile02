# Sesión 13 · Paso 5. Permiso de notificaciones

`GestorPermisos` y los tres estados se declaran en el código común. En Android,
Koin crea el gestor para la actividad que lanza el diálogo del sistema. Desde
Android 13 se solicita `POST_NOTIFICATIONS`. En versiones anteriores se consulta
si las notificaciones están habilitadas para la aplicación. [Documentación de Android](https://developer.android.com/develop/ui/compose/notifications/notification-permission)

La sección **Notificaciones** de **Inicio** explica para qué se solicita el
permiso. Si la persona lo deniega de forma permanente, puede abrir los ajustes
de notificaciones y la pantalla actualiza el estado al regresar. Con el
permiso concedido, se puede enviar una notificación local de prueba.

## Verificación en Android

Se usó el emulador Pixel_10a (API 37) con un perfil temporal. La compilación
de Android y las 17 pruebas del módulo compartido pasaron.

| Caso | Resultado observado |
| --- | --- |
| Cerrar el diálogo del sistema sin elegir una opción. | El botón **Permitir notificaciones** siguió disponible y volvió a abrir el diálogo nativo. |
| Pulsar **Don’t allow** por primera vez. | La app mostró «Permiso denegado» y permitió solicitarlo otra vez. [Captura](evidencias/sesion-13-paso-5/01-denegado.png). |
| Pulsar **Don’t allow** por segunda vez. | La app mostró «Permiso denegado permanentemente» y **Abrir Ajustes** llevó a la configuración de notificaciones de PharmaMobile. [Captura](evidencias/sesion-13-paso-5/02-denegado-permanentemente.png). |
| Activar las notificaciones en Ajustes y regresar. | La app mostró «Permiso concedido». [Captura](evidencias/sesion-13-paso-5/03-concedido.png). |
| Pulsar **Enviar notificación de prueba**. | Android registró la notificación de PharmaMobile en el canal `notificaciones_prueba`. |
