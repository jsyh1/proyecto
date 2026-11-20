# proyecto

## Compatibilidad e instalación

El proyecto requiere JDK 21 y usa Spring Boot 4.1.1. Es una aplicación web, no
usa JavaFX. La configuración de MySQL, la preparación de un entorno limpio y los
comandos de validación están en
[docs/operations/compatibility-and-run.md](./docs/operations/compatibility-and-run.md).

## API de vehículos

`POST /vehiculos` registra un vehículo y lo asocia a un usuario existente. La placa
se guarda en mayúsculas y sin espacios ni guiones; debe contener entre 5 y 7
caracteres alfanuméricos. Una placa solo puede estar asociada a un vehículo.

Solicitud:

```json
{
  "idUsuario": 1,
  "placa": "abc-123",
  "marca": "Mazda",
  "modelo": "3",
  "color": "Rojo"
}
```

La respuesta `201 Created` incluye los datos del vehículo registrado y el `idUsuario`.
Se responde `400 Bad Request` si el formato de la placa o los campos requeridos no
son válidos, `404 Not Found` si el usuario no existe y `409 Conflict` si la placa
ya está registrada.

`GET /vehiculos?idUsuario=1` consulta los vehículos registrados para ese usuario,
incluyendo placa, marca, modelo y color cuando estén disponibles. La aplicación
aún no implementa autenticación; por tanto, el usuario se identifica mediante
`idUsuario`, tanto al registrar como al consultar.

## Saldo de horas gratuitas

`GET /usuarios/{idUsuario}/saldo-horas-gratuitas` devuelve el saldo disponible y
los movimientos ordenados del más reciente al más antiguo. Las compras validadas
acreditan una hora; al iniciar una sesión se reserva el saldo existente y al
cerrarla se consume solo el tiempo gratuito realmente utilizado, devolviendo el
sobrante al saldo. El historial registra los abonos, las reservas, su liberación
y las horas finalmente utilizadas.
Como aún no hay autenticación, el usuario se identifica mediante `idUsuario`.

## Recuperación de contraseña

En `/recuperar-contrasena` se solicita un enlace de un solo uso, válido por 15
minutos. La respuesta es genérica independientemente de que exista la cuenta. El
enlace llega al correo registrado, tratado como verificado para esta HU; el token
se guarda únicamente como hash y la contraseña nueva se almacena con BCrypt.
Configura `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`,
`SPRING_MAIL_PASSWORD`, `SPRING_MAIL_SMTP_AUTH` y
`SPRING_MAIL_SMTP_STARTTLS_ENABLE` para el SMTP. `APP_PASSWORD_RECOVERY_FROM`
define el remitente y `APP_PASSWORD_RESET_URL` debe apuntar al formulario público
(en producción, usa HTTPS). No se guardan credenciales de correo en el repositorio.

## API de historial de sesiones

`GET /usuarios/{idUsuario}/sesiones` devuelve las sesiones del usuario, ordenadas
de la más reciente a la más antigua. Cada elemento incluye placa, centro, fecha
de entrada, fecha de salida (nula si sigue abierta), estado y monto pagado (nulo
si aún no se ha cerrado).

`GET /usuarios/{idUsuario}/sesiones/{idSesion}` consulta el detalle de una sesión
del usuario e incluye además el valor por hora, las horas facturables y gratuitas
y la cantidad de beneficios QR aplicados.
Ambos endpoints responden `404 Not Found` si el usuario no existe; el detalle
también devuelve `404` si la sesión no existe o no pertenece a ese usuario.

## Consulta de tiempo y costo de una sesión

`GET /sesiones/consulta?placa=ABC123` consulta la sesión abierta de la placa y
redirige al detalle de la sesión. El detalle informa las horas de entrada y salida
(cuando exista), el tiempo transcurrido, la tarifa horaria, las horas facturables,
las horas gratuitas, los beneficios QR aplicados por separado y el valor pendiente.
El costo se calcula de forma proporcional por minuto y descontando las horas
gratuitas. Cada consulta actualiza el monto de la sesión abierta; en sesiones
cerradas se conserva el monto final registrado.

## Beneficio por compra validada

El servicio interno `SesionService.aplicarBeneficioCompraValida(idSesion, claveCompraQr)`
aplica una hora gratuita por compra validada. La clave idempotente identifica una
compra y sus reintentos no vuelven a acreditar horas; solo se guarda su hash.
Recalcula y persiste el costo pendiente; el detalle indica cuántos beneficios QR
se aplicaron. Cada beneficio ya está incluido en el total de horas gratuitas y no
se descuenta otra vez. No se expone una ruta para otorgar el beneficio sin validar
la compra.

## Integridad transaccional

Las entradas serializan por vehículo; la aplicación de beneficios, el cierre y la
consulta que actualiza una sesión bloquean la sesión correspondiente. El saldo y
sus movimientos, junto con el registro del beneficio, se confirman dentro de la
misma transacción. Aún no existe un flujo de pagos: no se marca una sesión como
pagada ni se registra un pago aprobado.

## Copias de seguridad

El procedimiento recomendado de respaldo MySQL, recuperación y registro de
pruebas está en [docs/operations/backup-and-restore.md](./docs/operations/backup-and-restore.md).
La prueba de restauración real queda pendiente hasta disponer de una instancia
MySQL aislada.

## Comprobante de pago

El servicio interno `ComprobantePagoService.generarComprobantePagoExitoso(sesion,
valorPagado)` genera la evidencia para una sesión cerrada después de que el flujo
de pagos confirme la transacción. Incluye placa, fecha y hora del pago, duración
del estacionamiento, horas gratuitas aplicadas y valor pagado. No procesa pagos.

## Establecimientos autorizados

`ComercioService` permite registrar, consultar, modificar, activar y desactivar
establecimientos. Los nuevos se registran activos; el servicio
`obtenerEstablecimientoActivoParaValidacion(id)` rechaza los inactivos para nuevas
validaciones. Estas operaciones son internas y no tienen rutas públicas hasta que
se integre la autorización de administrador.

## Avisos de cambios en el costo

La pantalla de detalle de sesión compara el costo y las horas gratuitas con la
última consulta en la pestaña. Cuando cambian, muestra el nuevo valor pendiente
y avisa si se aplicó una bonificación. La pantalla se actualiza cada 30 segundos.

## Registrar salida de un vehículo

`POST /sesiones/salida` recibe la placa de un vehículo con sesión abierta. Cierra la
sesión, registra la hora de salida y el evento de acceso, calcula el tiempo
facturable y fija el costo final. La respuesta redirige al detalle de la sesión
cerrada; si no existe una sesión abierta para la placa, vuelve al formulario con
un mensaje de error.