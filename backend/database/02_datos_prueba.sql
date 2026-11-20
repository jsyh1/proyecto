-- Datos mínimos para probar la HU. El hash es solo de ejemplo.
USE parqueadero;

INSERT INTO USUARIO (nombre, apellido, documento, correo, telefono, password_hash)
VALUES ('Ana', 'Pérez', '1000000001', 'ana@correo.com', '3000000000', 'hash-de-ejemplo');

INSERT INTO VEHICULO (id_usuario, placa, marca, modelo, color)
VALUES (LAST_INSERT_ID(), 'ABC123', 'Mazda', '3', 'Rojo');

INSERT INTO CENTRO_COMERCIAL (nombre, direccion, estado)
VALUES ('Centro Comercial Demo', 'Calle 1 # 2-3', 'ACTIVO');

INSERT INTO TARIFA (id_centro, valor_hora, fecha_inicio, fecha_fin, estado)
VALUES (LAST_INSERT_ID(), 5000.00, '2026-01-01', NULL, 'ACTIVA');
