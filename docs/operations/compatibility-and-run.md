# Compatibilidad e instalación

## Entorno soportado

- **Java:** JDK 21, fijado por `java.version` en `pom.xml`.
- **Framework:** Spring Boot 4.1.1, fijado como parent Maven.
- **Interfaz:** web con Spring MVC y Thymeleaf. El proyecto no tiene dependencia,
  clases ni runtime de JavaFX; JavaFX no aplica.
- **Base de datos:** MySQL con Connector/J en runtime. La versión del servidor
  MySQL no está fijada por el proyecto; debe ser compatible con el esquema de
  `database/01_esquema_entrada.sql`.
- **Maven:** usar el Maven Wrapper del repositorio para obtener la versión
  controlada por el proyecto.

## Preparar un entorno limpio en Windows

1. Instalar JDK 21 y confirmar `java -version`.
2. Instalar e iniciar una instancia MySQL compatible.
3. Crear la base `parqueadero` y ejecutar
   `backend/database/01_esquema_entrada.sql` desde la raíz del repositorio con una cuenta
   administrativa. No almacenar credenciales en el repositorio ni incluirlas en
   argumentos de comandos.
4. Completar las variables `SPRING_DATASOURCE_URL`,
   `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` en `.env` para
   desarrollo local. La URL inicial apunta a `localhost:3306/parqueadero`.
   Spring Boot carga este archivo automáticamente. Para otros entornos, usar el
   administrador de secretos y variables de entorno en lugar de guardar secretos
   en archivos.
5. Desde PowerShell, ejecutar la validación y luego iniciar el backend:

   ```powershell
   cd backend
   .\mvnw.cmd clean verify
   .\mvnw.cmd spring-boot:run
   ```

`.env` está excluido de Git. No guardar credenciales reales en el repositorio ni
reemplazar esta configuración con secretos dentro del código.

## Cuentas y roles

Registrar una cuenta mediante `POST /usuarios/registro`; todas las cuentas nuevas
reciben rol `USER` y una contraseña BCrypt. Las páginas usan `/login`. Las API
REST reciben autenticación HTTP Basic con el correo y la contraseña.

Para habilitar administración, crear primero una cuenta y asignar `ADMIN` con una
cuenta de base de datos autorizada:

```sql
UPDATE USUARIO SET rol = 'ADMIN' WHERE correo = '<correo-de-la-cuenta>';
```

La migración de `database/01_esquema_entrada.sql` conserva los usuarios existentes
y les asigna `USER`. No hay una contraseña ni una cuenta `ADMIN` predeterminada.

Las pruebas configuran H2 mediante propiedades de test, lo que permite validar el
contexto y los casos automatizados sin una instancia MySQL. Esa configuración
existe solo durante las pruebas y no se usa al ejecutar la aplicación.

## Estado de validación

La suite debe ejecutarse con JDK 21 como en los pasos anteriores. La ejecución
con MySQL real requiere una instancia que no forma parte del workspace; las
pruebas con H2 no sustituyen esa comprobación específica del motor.
