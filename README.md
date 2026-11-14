# proyecto

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

## API de historial de sesiones

`GET /usuarios/{idUsuario}/sesiones` devuelve las sesiones del usuario, ordenadas
de la más reciente a la más antigua. Cada elemento incluye placa, centro, fecha
de entrada, fecha de salida (nula si sigue abierta), estado y monto pagado (nulo
si aún no se ha cerrado).

`GET /usuarios/{idUsuario}/sesiones/{idSesion}` consulta el detalle de una sesión
del usuario e incluye además el valor por hora y las horas facturables y gratuitas.
Ambos endpoints responden `404 Not Found` si el usuario no existe; el detalle
también devuelve `404` si la sesión no existe o no pertenece a ese usuario.

## Consulta de tiempo y costo de una sesión

`GET /sesiones/consulta?placa=ABC123` consulta la sesión abierta de la placa y
redirige al detalle de la sesión. El detalle informa la hora de entrada, el tiempo
transcurrido, la tarifa horaria aplicada y el costo acumulado, calculado de forma
proporcional por minuto y descontando las horas gratuitas. Cada consulta actualiza
el monto acumulado de la sesión abierta; en sesiones cerradas se conserva el monto
final registrado.