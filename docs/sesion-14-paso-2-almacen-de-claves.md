# Sesión 14 · Paso 2. Almacén de claves de Android

El 11 de noviembre de 2026 se creó el almacén de entrega de PharmaMobile
fuera del repositorio, en el perfil de Windows del responsable de la entrega.

| Dato | Valor verificado |
| --- | --- |
| Almacén | `pharmamobile-release.jks` |
| Tipo de entrada | `PrivateKeyEntry` |
| Alias | `pharmamobile-release` |
| Titular del certificado | Wagner Qquerar Mayta |
| Algoritmo | RSA de 2048 bits |
| Vigencia | 10 000 días, del 11/11/2026 al 29/03/2054 |
| Huella SHA-256 del certificado | `91:B3:90:27:63:FE:88:6C:DA:BC:CE:A5:1A:49:1F:7E:29:9D:B1:71:56:28:93:8F:34:D1:97:77:64:53:E3:32` |

`keytool` confirmó el alias y el certificado. También se comprobó el acceso
a la clave privada firmando una solicitud de certificado de prueba.
Las contraseñas se guardaron con protección de Windows (DPAPI) para el
usuario actual, en un archivo local junto al almacén.

**Respaldo pendiente.** Falta guardar una copia del JKS fuera de esta PC
y conservar sus contraseñas en un gestor seguro. El archivo protegido por
DPAPI está ligado al usuario y al equipo actual y no sirve por sí solo
para recuperar las contraseñas en otra PC.
