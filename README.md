# proyecto

## Inicio local

El proyecto usa Spring Boot, Java 21+, Thymeleaf y JPA. Para iniciar una instancia
de demostración con una base H2 en memoria:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

La base de demostración se reinicia al detener la aplicación. Para ejecutar las
pruebas automatizadas:

```powershell
.\mvnw.cmd test
```

Para MySQL, crea la base `parqueadero`, ejecuta `database/01_esquema_entrada.sql`
y configura `spring.datasource.url`, `spring.datasource.username` y
`spring.datasource.password` como propiedades externas. En una base antigua,
ejecuta además `database/03_migracion_juan_camilo.sql` una sola vez para agregar
el rol del usuario. No guardes credenciales en el repositorio.

Las cuentas nuevas se crean con rol `USER`. Las pantallas y APIs administrativas
requieren que un operador autorizado asigne el rol `ADMIN` en la base de datos,
por ejemplo con `UPDATE USUARIO SET rol = 'ADMIN' WHERE correo = 'admin@ejemplo.com';`.
En despliegues HTTPS configura `SESSION_COOKIE_SECURE=true`.

Los pagos de esta versión son únicamente simulados: no se conecta a una pasarela,
no se cobra dinero ni se conservan datos de tarjetas. Los QR emitidos contienen
un token aleatorio; la aplicación guarda solo su hash y permite aplicarlo una vez.
La lectura con cámara depende del soporte del navegador y necesita localhost o
HTTPS; siempre está disponible la entrada manual del código.

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

Las rutas de historial, perfil y vehículos requieren una sesión autenticada y solo
permiten acceder a los datos del usuario actual. Los formularios y clientes REST
deben obtener primero un token CSRF desde `GET /api/auth/csrf` y enviarlo en
`X-XSRF-TOKEN` en cada solicitud que cambie datos.

## Cuentas y perfil

- `POST /api/usuarios/registro`: crea una cuenta con correo y documento únicos.
- `POST /api/auth/login` y `POST /api/auth/logout`: inicia y cierra una sesión.
- `GET /api/usuarios/me` y `PUT /api/usuarios/me`: consulta y actualiza el perfil.
- `GET /api/vehiculos/me` y `POST /api/vehiculos`: consulta y registra vehículos propios.

Las contraseñas se codifican con BCrypt. La contraseña nunca forma parte de las
respuestas de la API.

## Tarifas, beneficios, QR y pagos

- `GET /api/centros` y `GET /api/tarifas/vigentes?idCentro={id}`: consulta centros y tarifas.
- `/api/admin/tarifas` y `/api/admin/beneficios`: administración autenticada con rol `ADMIN`.
- `POST /api/admin/beneficios/{id}/codigos`: emite códigos de un beneficio con vencimiento.
- `POST /api/sesiones/entradas`: registra una entrada asociada a un centro.
- `POST /api/sesiones/{id}/beneficios/validar`: verifica y consume el QR de la misma ubicación.
- `GET /api/validaciones`: historial de intentos del usuario autenticado.
- `GET /api/sesiones/{id}/pago`: consulta costo inicial, descuento y total.
- `POST /api/sesiones/{id}/pagos`: procesa el pago simulado y cierra la sesión al aprobarse.
- `GET /api/usuarios/me/notificaciones`: consulta las notificaciones internas de entrada.

Los tipos de beneficio son `HORAS_GRATIS`, `PORCENTAJE_DESCUENTO` y
`VALOR_FIJO_DESCUENTO`. Las vigencias activas de tarifas de un mismo centro no se
pueden superponer.

## Consulta de tiempo y costo de una sesión

`GET /sesiones/consulta?placa=ABC123` consulta la sesión abierta de la placa y
redirige al detalle de la sesión. El detalle informa la hora de entrada, el tiempo
transcurrido, la tarifa horaria aplicada y el costo acumulado, calculado de forma
proporcional por minuto y descontando las horas gratuitas. Cada consulta actualiza
el monto acumulado de la sesión abierta; en sesiones cerradas se conserva el monto
final registrado.