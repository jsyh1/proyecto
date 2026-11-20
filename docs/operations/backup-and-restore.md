# Copias de seguridad y recuperación de MySQL

## Alcance y valores operativos recomendados

Este procedimiento aplica a la base MySQL `parqueadero` del proyecto. Los valores
son una línea base operativa: una persona responsable de la base debe adaptarlos
al entorno antes de programar tareas de producción.

- Frecuencia: copia lógica completa diaria, a las 02:00 hora local.
- Retención: 30 copias diarias; eliminar únicamente archivos cifrados que hayan
  superado ese plazo.
- Prueba de restauración: mensual, en una instancia aislada y con un nombre de
  base diferente al de producción.
- Destino: almacenamiento externo al repositorio y al servidor de la aplicación,
  cifrado en tránsito y en reposo, con acceso limitado al DBA/operador de guardia.
- Temporal: volumen cifrado con ACL restringida; el volcado sin cifrar nunca debe
  quedar en el repositorio ni en un directorio compartido.
- Cifrado: GPG con la clave pública aprobada por el equipo; la clave privada se
  conserva fuera del servidor de copias.

No se deben guardar volcados, claves, archivos de credenciales ni datos de
producción en Git. No se deben incluir contraseñas en los argumentos de los
comandos.

## Responsabilidades

| Rol | Responsabilidad |
| --- | --- |
| DBA/operador de infraestructura | Mantener el cliente MySQL, la cuenta de respaldo, el destino cifrado, la tarea diaria, la retención y la revisión de resultados. |
| Responsable de recuperación | Declarar el incidente, elegir el punto de recuperación y coordinar la restauración aislada o de producción. |
| Mantenedor de la aplicación | Verificar el esquema y ejecutar pruebas de humo contra la instancia restaurada antes de autorizar el retorno del servicio. |
| Persona que ejecuta la prueba | Registrar fecha, entorno, versión, copia probada, resultado, duración, incidencias y seguimiento en el [registro de pruebas](./restore-test-log.md). |

## Preparación de acceso

Instalar las utilidades oficiales `mysql_config_editor`, `mysqldump`, `mysql` y
GPG en el equipo operador. Verificar que todas las tablas del esquema usan InnoDB
para que `--single-transaction` produzca una copia consistente. Crear un login
path interactivo para la cuenta de respaldo, evitando exponer la contraseña en
el historial o en la línea de comandos:

```powershell
mysql_config_editor set --login-path=parqueadero-backup --host=<host> --user=<usuario-respaldo> --password
```

La cuenta debe tener solo los permisos necesarios para leer el esquema y los
datos, vistas, disparadores, rutinas y eventos. Restringir mediante ACL el acceso
al archivo de login path y al directorio temporal de trabajo.

Configurar `PARQUEADERO_BACKUP_DIR` para que apunte al destino temporal protegido,
y `BACKUP_GPG_RECIPIENT` al identificador de la clave pública aprobada. El
directorio temporal debe estar fuera del repositorio. La ubicación final debe
ser externa al servidor de la base de datos. Crear también el login path
`parqueadero-restore` para una cuenta autorizada a crear una base aislada e
importar el respaldo:

```powershell
mysql_config_editor set --login-path=parqueadero-restore --host=<host-pruebas> --user=<usuario-restauracion> --password
```

## Crear y verificar una copia

Ejecutar diariamente o por medio de una tarea del sistema con una cuenta de
servicio restringida. No ejecutar cambios de esquema durante la copia.

```powershell
$mysqldump = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe'
if ([string]::IsNullOrWhiteSpace($env:PARQUEADERO_BACKUP_DIR) -or
    -not (Test-Path -LiteralPath $env:PARQUEADERO_BACKUP_DIR -PathType Container)) {
    throw 'PARQUEADERO_BACKUP_DIR debe apuntar a un directorio existente y protegido.'
}
if ([string]::IsNullOrWhiteSpace($env:BACKUP_GPG_RECIPIENT)) {
    throw 'BACKUP_GPG_RECIPIENT debe identificar la clave pública aprobada.'
}
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$plainDump = Join-Path $env:PARQUEADERO_BACKUP_DIR "parqueadero-$stamp.sql"
$encryptedDump = "$plainDump.gpg"

& $mysqldump --login-path=parqueadero-backup --single-transaction `
    --routines --events --triggers --hex-blob `
    --default-character-set=utf8mb4 --result-file="$plainDump" parqueadero
if ($LASTEXITCODE -ne 0) {
    throw "mysqldump falló con código $LASTEXITCODE."
}

gpg --batch --yes --recipient $env:BACKUP_GPG_RECIPIENT `
    --output $encryptedDump --encrypt $plainDump
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo cifrar el respaldo."
}

(Get-FileHash -Algorithm SHA256 -LiteralPath $encryptedDump).Hash |
    Set-Content -NoNewline "$encryptedDump.sha256"
Remove-Item -LiteralPath $plainDump
```

Si cualquier comando falla, conservar los logs del error, no marcar el respaldo
como exitoso y avisar al DBA/operador. Copiar el archivo `.gpg` y su `.sha256` al
almacenamiento externo aprobado. Verificar el hash después de la copia y confirmar
que el archivo cifrado se puede leer desde el destino. No copiar el SQL sin cifrar.
Si el cifrado falla, restringir el acceso al temporal y retirarlo de acuerdo con
el procedimiento de manejo de datos del entorno.

La tarea diaria debe alertar ante error, comprobar que el archivo del día existe y
registrar la hora, tamaño, hash y resultado. Retirar copias antiguas solo después
de confirmar la nueva copia, mantener al menos una copia válida y aplicar la
retención de 30 días.

## Prueba de restauración aislada

Realizarla mensualmente sin detener ni modificar la base de producción:

1. Seleccionar una copia cifrada y comprobar su SHA-256 contra el archivo
   acompañante.
2. Descifrarla en un directorio temporal con ACL restringida. No reutilizar ni
   sobrescribir una base de producción:

   ```powershell
   $encryptedDump = 'D:\copias\parqueadero-20261119-020000.sql.gpg'
   $expectedHash = (Get-Content -Raw "$encryptedDump.sha256").Trim()
   $actualHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $encryptedDump).Hash
   if ($actualHash -ne $expectedHash) { throw 'El hash de la copia no coincide.' }

   $plainDump = 'D:\restauracion-temporal\parqueadero.sql'
   gpg --output $plainDump --decrypt $encryptedDump
   if ($LASTEXITCODE -ne 0) { throw 'No se pudo descifrar la copia.' }
   ```

   La instancia aislada y el volumen temporal deben permanecer restringidos.
   Si el respaldo contiene datos personales, no exponerlos a usuarios de pruebas;
   aplicar anonimización antes de ampliar el acceso.
3. Crear una base vacía, por ejemplo `parqueadero_restore_202611`, en una
   instancia aislada y con una cuenta de restauración autorizada.
4. Importar el SQL en esa base. En Windows, se puede invocar el cliente desde
   PowerShell usando `cmd.exe` para redirigir el archivo:

   ```powershell
   $mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
   $restoreDb = 'parqueadero_restore_202611'
   $plainDump = 'D:\restauracion-temporal\parqueadero.sql'
   & $mysql --login-path=parqueadero-restore --execute="CREATE DATABASE $restoreDb"
   if ($LASTEXITCODE -ne 0) { throw "No se pudo crear $restoreDb." }

   $restoreCommand = '"{0}" --login-path=parqueadero-restore --database={1} < "{2}"' `
       -f $mysql, $restoreDb, $plainDump
   & $env:ComSpec /d /c $restoreCommand
   if ($LASTEXITCODE -ne 0) { throw "La importación falló." }
   ```

5. Confirmar que las tablas esperadas existen; consultar y registrar los conteos
   de `USUARIO`, `VEHICULO`, `SESION_PARQUEADERO`, `CUENTA_AHORRO` y
   `MOVIMIENTO_AHORRO`; validar una muestra conocida y la consistencia de las
   relaciones antes de ejecutar pruebas de aplicación.
6. Iniciar una instancia de aplicación de prueba apuntando solo a la base
   restaurada y ejecutar pruebas de humo de consulta de sesiones y saldo.
7. Registrar fecha, copia/hash, versiones, duración, verificaciones, resultado,
   incidencias y persona responsable en el [registro de pruebas](./restore-test-log.md).
8. Eliminar el directorio temporal y la base de prueba cuando el responsable
   confirme que ya no se necesitan. Conservar el registro de la prueba, no los
   datos restaurados.

Si se necesita recuperar producción, el responsable de recuperación coordina
primero la indisponibilidad de la aplicación y la protección de cualquier dato
posterior a la copia. Antes de reemplazar datos, obtener aprobación, preservar
una copia del estado actual si aún es legible y seguir los pasos de la prueba
aislada. Solo devolver el servicio después de la validación del DBA y del
mantenedor de la aplicación.

## Estado en este workspace

La ejecución de una restauración real requiere un cliente y una instancia MySQL
que no están disponibles en este entorno. El intento no se reporta como exitoso;
la evidencia actual está registrada como pendiente en el [registro de pruebas](./restore-test-log.md).
