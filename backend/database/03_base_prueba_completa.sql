-- Base completa para el backend actual de Parqueadero.
-- ADVERTENCIA: este script borra y vuelve a crear la base parqueadero.
-- Ejecútalo solo si aceptas perder los datos actuales de esa base.

DROP DATABASE IF EXISTS parqueadero;
CREATE DATABASE parqueadero
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE parqueadero;

-- 1. USUARIO
CREATE TABLE USUARIO (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  documento VARCHAR(20) NOT NULL UNIQUE,
  correo VARCHAR(100) NOT NULL UNIQUE,
  telefono VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL,
  rol VARCHAR(20) NOT NULL DEFAULT 'USER'
) ENGINE = InnoDB;

-- 2. CENTRO_COMERCIAL
CREATE TABLE CENTRO_COMERCIAL (
  id_centro INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  direccion VARCHAR(120),
  estado VARCHAR(20) NOT NULL
) ENGINE = InnoDB;

-- 3. COMERCIO (establecimientos administrados por el API)
CREATE TABLE COMERCIO (
  id_comercio INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  direccion VARCHAR(120),
  estado VARCHAR(20) NOT NULL
) ENGINE = InnoDB;

-- 4. VEHICULO
CREATE TABLE VEHICULO (
  id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  placa VARCHAR(10) NOT NULL UNIQUE,
  marca VARCHAR(30),
  modelo VARCHAR(30),
  color VARCHAR(20),
  CONSTRAINT fk_vehiculo_usuario
    FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
) ENGINE = InnoDB;

-- 5. TARIFA
CREATE TABLE TARIFA (
  id_tarifa INT AUTO_INCREMENT PRIMARY KEY,
  id_centro INT NOT NULL,
  valor_hora DECIMAL(10,2) NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NULL,
  estado VARCHAR(20) NOT NULL,
  CONSTRAINT fk_tarifa_centro
    FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro)
) ENGINE = InnoDB;

-- 6. SESION_PARQUEADERO
CREATE TABLE SESION_PARQUEADERO (
  id_sesion INT AUTO_INCREMENT PRIMARY KEY,
  id_vehiculo INT NOT NULL,
  id_centro INT NOT NULL,
  id_tarifa INT NOT NULL,
  fecha_entrada DATETIME NOT NULL,
  fecha_salida DATETIME NULL,
  horas_facturables DECIMAL(5,2),
  horas_gratuitas DECIMAL(8,6),
  beneficios_qr INT NOT NULL DEFAULT 0,
  monto_total DECIMAL(10,2),
  estado VARCHAR(20) NOT NULL,
  CONSTRAINT fk_sesion_vehiculo
    FOREIGN KEY (id_vehiculo) REFERENCES VEHICULO (id_vehiculo),
  CONSTRAINT fk_sesion_centro
    FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro),
  CONSTRAINT fk_sesion_tarifa
    FOREIGN KEY (id_tarifa) REFERENCES TARIFA (id_tarifa)
) ENGINE = InnoDB;

-- 7. REGISTRO_ACCESO
CREATE TABLE REGISTRO_ACCESO (
  id_acceso INT AUTO_INCREMENT PRIMARY KEY,
  id_centro INT NOT NULL,
  id_sesion INT NULL,
  placa_detectada VARCHAR(10),
  fecha_hora DATETIME NOT NULL,
  tipo_evento VARCHAR(10) NOT NULL,
  resultado_deteccion VARCHAR(20) NOT NULL,
  CONSTRAINT fk_acceso_centro
    FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro),
  CONSTRAINT fk_acceso_sesion
    FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
) ENGINE = InnoDB;

-- 8. CUENTA_AHORRO
CREATE TABLE CUENTA_AHORRO (
  id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL UNIQUE,
  saldo_horas DECIMAL(12,6) NOT NULL DEFAULT 0,
  CONSTRAINT chk_saldo_horas_no_negativo CHECK (saldo_horas >= 0),
  CONSTRAINT fk_cuenta_ahorro_usuario
    FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
) ENGINE = InnoDB;

-- 9. MOVIMIENTO_AHORRO
CREATE TABLE MOVIMIENTO_AHORRO (
  id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
  id_cuenta INT NOT NULL,
  fecha_hora DATETIME NOT NULL,
  cantidad_horas DECIMAL(12,6) NOT NULL,
  motivo VARCHAR(160) NOT NULL,
  CONSTRAINT fk_movimiento_cuenta
    FOREIGN KEY (id_cuenta) REFERENCES CUENTA_AHORRO (id_cuenta)
) ENGINE = InnoDB;

-- 10. RECUPERACION_CONTRASENA
CREATE TABLE RECUPERACION_CONTRASENA (
  id_recuperacion INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  fecha_creacion DATETIME NOT NULL,
  fecha_expiracion DATETIME NOT NULL,
  fecha_uso DATETIME NULL,
  INDEX idx_recuperacion_usuario (id_usuario),
  CONSTRAINT fk_recuperacion_usuario
    FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
) ENGINE = InnoDB;

-- 11. BENEFICIO_QR_APLICADO
CREATE TABLE BENEFICIO_QR_APLICADO (
  id_beneficio_qr INT AUTO_INCREMENT PRIMARY KEY,
  clave_compra_hash CHAR(64) NOT NULL,
  id_sesion INT NOT NULL,
  fecha_aplicacion DATETIME NOT NULL,
  CONSTRAINT uk_beneficio_qr_clave_hash UNIQUE (clave_compra_hash),
  CONSTRAINT fk_beneficio_qr_sesion
    FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
) ENGINE = InnoDB;

-- Tablas del esquema anterior que se conservan para QR y pagos.
-- El backend actual no tiene entidades/controladores para estas tablas.
CREATE TABLE QR_BENEFICIO (
  id_beneficio INT AUTO_INCREMENT PRIMARY KEY,
  id_comercio INT NOT NULL,
  codigo VARCHAR(60) NOT NULL UNIQUE,
  tipo_beneficio VARCHAR(40) NOT NULL,
  horas_otorgadas DECIMAL(5,2),
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NOT NULL,
  estado VARCHAR(20) NOT NULL,
  CONSTRAINT fk_qr_beneficio_comercio
    FOREIGN KEY (id_comercio) REFERENCES COMERCIO (id_comercio)
) ENGINE = InnoDB;

CREATE TABLE VALIDACION_QR (
  id_validacion INT AUTO_INCREMENT PRIMARY KEY,
  id_beneficio INT NOT NULL,
  id_sesion INT NOT NULL,
  fecha_validacion DATETIME NOT NULL,
  resultado VARCHAR(20) NOT NULL,
  horas_aplicadas DECIMAL(5,2),
  CONSTRAINT fk_validacion_beneficio
    FOREIGN KEY (id_beneficio) REFERENCES QR_BENEFICIO (id_beneficio),
  CONSTRAINT fk_validacion_sesion
    FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
) ENGINE = InnoDB;

CREATE TABLE PAGO (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_sesion INT NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  fecha_pago DATETIME NOT NULL,
  metodo_pago VARCHAR(30),
  estado VARCHAR(20) NOT NULL,
  referencia_transaccion VARCHAR(80),
  CONSTRAINT fk_pago_sesion
    FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
) ENGINE = InnoDB;

-- USUARIO DE PRUEBA
-- Correo: admin@parqueadero.com
-- Contraseña de prueba: Admin123
INSERT INTO USUARIO
  (nombre, apellido, documento, correo, telefono, password_hash, rol)
VALUES
  ('Juan', 'Administrador', '1234567890', 'admin@parqueadero.com',
   '3001234567',
   '$2a$10$zZfWoscnqSK5npBmaIHKlu3xMXiMjtIBFT7PqGN1bAIoXngnj2ZoS',
   'ADMIN');

SET @id_usuario_prueba = LAST_INSERT_ID();

-- VEHICULO DE PRUEBA
INSERT INTO VEHICULO (id_usuario, placa, marca, modelo, color)
VALUES (@id_usuario_prueba, 'ABC123', 'Mazda', '3', 'Rojo');

-- CENTRO Y TARIFA DE PRUEBA PARA EL FORMULARIO DE ENTRADA
INSERT INTO CENTRO_COMERCIAL (nombre, direccion, estado)
VALUES ('Centro Comercial Demo', 'Calle 1 # 2-3', 'ACTIVO');

SET @id_centro_prueba = LAST_INSERT_ID();

INSERT INTO TARIFA (id_centro, valor_hora, fecha_inicio, fecha_fin, estado)
VALUES (@id_centro_prueba, 5000.00, '2026-01-01', NULL, 'ACTIVA');

-- CUENTA DE AHORRO INICIAL PARA CONSULTAR EL SALDO
INSERT INTO CUENTA_AHORRO (id_usuario, saldo_horas)
VALUES (@id_usuario_prueba, 0.000000);

-- Comprobaciones al terminar
SHOW TABLES;
SELECT id_usuario, nombre, apellido, correo, rol
FROM USUARIO
WHERE id_usuario = @id_usuario_prueba;
SELECT id_centro, nombre, estado
FROM CENTRO_COMERCIAL
WHERE id_centro = @id_centro_prueba;
