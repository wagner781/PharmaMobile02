# Sesión 13 · Paso 1. Recorrido de Productos

Fecha de inicio: 4 de noviembre de 2026. Base evaluada: `c4c5a40`.
Entorno: AVD Pixel_10a, Android, y PharmaSoft en `localhost:8080`.
El endpoint `GET /api/health` respondió «Backend funcionando correctamente».
La compilación `:androidApp:assembleDebug` terminó correctamente y las 12 pruebas
existentes de `:shared:testAndroidHostTest` pasaron. Las primeras pruebas se
hicieron en la instalación existente. Para probar la primera apertura y el
cambio de cuenta se usó un perfil temporal del emulador, con datos separados.

## Registro de pruebas

| N.º | Prueba | Procedimiento y evidencia esperada | Resultado |
| --- | --- | --- | --- |
| 1 | Primera apertura | Abrir la app con datos limpios y verificar que se puede iniciar sesión. | En el perfil nuevo apareció directamente «Autenticación JWT». Se pudo ingresar con la cuenta de prueba y llegar a Inicio. |
| 2 | Credenciales incorrectas | Intentar entrar con una contraseña inválida y repetir sin red. Comparar los dos mensajes. | Con red: «Credenciales inválidas. Verifique usuario y contraseña». Sin red: «No se pudo conectar con el servidor». Se pudo volver a intentar. |
| 3 | Catálogo local vacío | Entrar con una cuenta de prueba y abrir Productos sin red con una caché vacía. Registrar el texto que aparece. | Mostró «No hay productos registrados» junto al aviso «Sin conexión: mostrando datos guardados». |
| 4 | Alta sin red | Con sesión iniciada, activar modo avión, apagar Wi-Fi y crear un producto de prueba con nombre único. | Con la app abierta se creó `S13-QA-20261104-01` y apareció «Pendiente de envío (ID: -1)». Al reabrir la app sin conexión en una prueba previa, el catálogo guardado era visible, pero el formulario desapareció y la app mostró «Modo solo lectura». |
| 5 | Recuperación de red | Restablecer la conexión. Observar si la lista cambia sola. Si hace falta, pulsar Sincronizar. Verificar el producto en el servidor. | La marca siguió visible tras recuperar la conexión. Al pulsar Sincronizar desapareció y el producto siguió en la lista. Una consulta de solo lectura en Oracle devolvió exactamente una fila para `S13-QA-20261104-01`. |
| 6 | Validación del servidor | Crear desde el formulario un producto llamado `ZQ`. PharmaSoft exige un mínimo de tres caracteres. | La app mostró «Producto registrado: ZQ» y dejó el producto pendiente sin indicar un error en Nombre. Oracle devolvió cero filas para `ZQ`. Se retiró la fila de prueba de la cola local. |
| 7 | Sesión vencida con formulario abierto | Iniciar sesión con un JWT de 60 segundos en una instancia temporal de PharmaSoft. Escribir `S13-QA-JWT-VENCIDO`, esperar más de 65 segundos con el formulario abierto, pulsar Sincronizar y luego Registrar. | El GET y el POST devolvieron 401. Tras el GET, el borrador seguía en el campo y la pantalla decía «Sin conexión: mostrando datos guardados». Tras el POST, mostró «Producto registrado» sin pedir autenticación. Al reabrir la app apareció «Autenticación JWT». |
| 8 | Cierre de sesión y cambio de cuenta | Cerrar sesión y reabrir. En el perfil temporal, crear un alta sin conexión como administrador, cerrar sesión, iniciar como vendedor y abrir Productos sin red. | La app volvió a pedir autenticación al reabrir. El vendedor vio `S13-QA-AISLAMIENTO` con «Pendiente de envío (ID: -1)», aunque su catálogo estaba en modo solo lectura. |

## Tabla de hallazgos

| Hallazgo | Evidencia | Gravedad | Decisión |
| --- | --- | --- | --- |
| Un conflicto al subir un alta pendiente borra la fila local. El usuario puede perder su versión del producto sin ver el motivo. | Revisión de [ProductoRepositorioOffline.kt](../shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/data/repository/ProductoRepositorioOffline.kt). Falta reproducirlo en Android. | Alta | Sesión 13. Conservar la fila y mostrar el conflicto como fallo permanente. |
| El primer fallo distinto de 409 interrumpe la subida de las demás filas y la descarga del catálogo. La cola tampoco registra intentos por fila. | Revisión del mismo repositorio y del [esquema de Producto](../shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobile/db/Producto.sq). | Alta | Sesión 13. Registrar intentos y errores por fila y distinguir fallos transitorios de permanentes. |
| Un alta que llega al servidor pero pierde la respuesta queda pendiente y se puede volver a enviar. Un conflicto posterior no confirma que el producto existente corresponda a esa operación. | El alta pendiente se envía de nuevo con un POST. El OpenAPI local no declara una clave de idempotencia para esa operación. | Alta | Sesión 13. Evitar reintentos automáticos de altas con resultado incierto mientras no exista confirmación segura. |
| La pantalla muestra la marca de pendiente por fila y la antigüedad de los datos, pero no resume cuántas filas esperan envío o fallaron. | Revisión de [ProductoScreen.kt](../shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/presentation/producto/ProductoScreen.kt). | Media | Sesión 13. Mostrar ambos conteos y ofrecer una acción cuando haya fallos. |
| Al recuperar la conexión, el alta siguió pendiente hasta que se pulsó Sincronizar. | Prueba 5 en el Pixel_10a. | Media | Sesión 13. Hacer visible qué queda por enviar y cómo iniciar el envío. Evaluar el disparo automático en la sesión 14. |
| El alta muestra un mensaje de éxito aunque el servidor rechace el nombre. El formulario no señala el campo Nombre. | Prueba 6 en el Pixel_10a y requisito de longitud del DTO de PharmaSoft. | Alta | Sesión 13. Mostrar el rechazo definitivo y distinguir un guardado local de una confirmación del servidor. |
| Al reabrir la app sin red, una sesión con permiso de escritura queda en solo lectura. | Prueba 4 en el Pixel_10a. La recuperación de sesión devuelve un conjunto vacío de permisos si no puede consultar `/auth/me`. | Alta | Sesión 14. Revisar cómo restaurar permisos de la última sesión autenticada y cuándo dejan de ser válidos. |
| Una sesión vencida se confunde con una caída de red. Guardar muestra un mensaje de éxito aunque el servidor respondió 401 y la pantalla no ofrece volver a autenticarse. | Prueba 7. Se verificaron las respuestas HTTP en Logcat y los mensajes en pantalla. | Alta | Sesión 13. Distinguir el 401 de la falta de red al sincronizar. Sesión 14. Resolver la reautenticación desde el formulario y conservar lo escrito. |
| El cierre de sesión borra el token, pero una segunda cuenta puede ver las altas pendientes de la anterior. | Prueba 8 con administrador y vendedor en el mismo perfil temporal. | Alta | Sesión 14. Aislar la cola y los datos locales por cuenta y conservar los cambios pendientes para su propietario. |

## Configuración de la prueba de vencimiento

La instancia temporal de PharmaSoft usó el puerto `18081` y
`APP_JWT_EXPIRACION_MINUTOS=1`. Se verificó la duración a partir de los
campos `iat` y `exp`. Se instaló un APK de prueba con identificador
`pe.edu.upeu.pharmamobile.qa` y URL `http://10.0.2.2:18081`, compilado
desde una copia de la base evaluada. El identificador y la URL se cambiaron
solo en esa copia.

La fila válida `S13-QA-20261104-01` quedó en PharmaSoft como evidencia de la prueba.
