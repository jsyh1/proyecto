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
   `database/01_esquema_entrada.sql` desde la raíz del repositorio con una cuenta
   administrativa. No almacenar credenciales en el repositorio ni incluirlas en
   argumentos de comandos.
4. Proporcionar estas variables al proceso de la aplicación mediante el
   administrador de secretos del entorno:
   - `SPRING_DATASOURCE_URL`, por ejemplo `jdbc:mysql://<host>:3306/parqueadero`.
   - `SPRING_DATASOURCE_USERNAME`.
   - `SPRING_DATASOURCE_PASSWORD`.
5. Desde PowerShell, ejecutar la validación y luego iniciar la aplicación:

   ```powershell
   .\mvnw.cmd clean verify
   .\mvnw.cmd spring-boot:run
   ```

Las variables de conexión no tienen valores predeterminados en
`application.properties`. Si falta una, Spring informa el nombre de la variable
no resuelta durante el inicio; no se imprime su valor ni una contraseña. No se
debe reemplazar esta configuración con credenciales dentro del código.

Las pruebas configuran H2 en `src/test/resources/application.properties`; esto
permite validar el contexto y los casos automatizados sin una instancia MySQL.
Esa configuración existe solo en el classpath de pruebas y no se usa al ejecutar
la aplicación.

## Estado de validación

La suite debe ejecutarse con JDK 21 como en los pasos anteriores. La ejecución
con MySQL real requiere una instancia que no forma parte del workspace; las
pruebas con H2 no sustituyen esa comprobación específica del motor.
