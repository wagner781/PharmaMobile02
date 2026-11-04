# Actividad Autónoma 13 · Producto C. Bitácora de permisos

**Estudiante:** Wagner Qquerar Mayta · **Código:** 202210261

**Fecha:** 4 de noviembre de 2026 · **Plataforma:** Android, Pixel_10a (API 37)

## Permiso de notificaciones

`POST_NOTIFICATIONS` es el permiso que PharmaMobile solicita al usuario
desde **Inicio → Notificaciones**. La pantalla explica que sirve para recibir
avisos en el dispositivo. El diálogo nativo aparece al pulsar **Permitir
notificaciones**. [Captura del diálogo](evidencias/sesion-13-autonoma-b/01-dialogo-nativo.png).

| Estado | Qué ve el usuario | Cómo habilita o recupera la función |
| --- | --- | --- |
| Concedido | «Permiso concedido» y el botón **Enviar notificación de prueba**. [Captura](evidencias/sesion-13-paso-5/03-concedido.png). | Puede usar la función directamente. Al pulsar el botón, el aviso aparece en la bandeja de Android. [Notificación recibida](evidencias/sesion-13-autonoma-b/02-notificacion-recibida.png). |
| Denegado | «Permiso denegado», una explicación y el botón **Permitir notificaciones**. [Captura](evidencias/sesion-13-paso-5/01-denegado.png). | Pulsa **Permitir notificaciones** otra vez y elige **Allow** en el diálogo del sistema. |
| Denegado de forma definitiva | «Permiso denegado permanentemente» y el botón **Abrir Ajustes**. [Captura](evidencias/sesion-13-paso-5/02-denegado-permanentemente.png). | Sigue **Inicio → Notificaciones → Abrir Ajustes**, habilita las notificaciones de PharmaMobile y vuelve a la app. La pantalla vuelve a consultar el permiso y ofrece enviar la prueba. |

En el perfil de prueba se cerró el diálogo sin elegir y la solicitud siguió
disponible. La primera denegación permitió pedir el permiso de nuevo. Tras
la segunda denegación, se verificó la apertura de Ajustes y la recuperación
del estado **Concedido** al regresar.

## Alcance de las evidencias

- Las capturas de los tres estados provienen de la [práctica de permisos](sesion-13-paso-5.md), commit `b858d33`. El diálogo nativo y la entrega en la bandeja se confirmaron en el [Producto B](sesion-13-autonoma-producto-b.md) sobre la versión evaluada `8c0a3c6`.
- La bitácora cubre el permiso solicitado durante el uso de la app en Android. `INTERNET` se declara en el manifiesto y se concede sin diálogo al instalar.
- El flujo de autorización y entrega de notificaciones en iOS queda pendiente, según el estado documentado en el Producto B.
