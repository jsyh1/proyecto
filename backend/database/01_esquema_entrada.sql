-- Tablas que usa la HU "Registrar entrada por placa y consultar tiempo y costo".
-- Incluye además las cuentas y movimientos de horas gratuitas.
-- Puede ejecutarse sobre una base existente: crea las tablas que falten y amplía horas_gratuitas.
USE parqueadero;

CREATE TABLE IF NOT EXISTS USUARIO (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  documento VARCHAR(20) NOT NULL UNIQUE,
  correo VARCHAR(100) NOT NULL UNIQUE,
  telefono VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS RECUPERACION_CONTRASENA (
  id_recuperacion INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  fecha_creacion DATETIME NOT NULL,
  fecha_expiracion DATETIME NOT NULL,
  fecha_uso DATETIME NULL,
  INDEX idx_recuperacion_usuario (id_usuario),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
);

CREATE TABLE IF NOT EXISTS COMERCIO (
  id_comercio INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  direccion VARCHAR(120),
  estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS VEHICULO (
  id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  placa VARCHAR(10) NOT NULL UNIQUE,
  marca VARCHAR(30),
  modelo VARCHAR(30),
  color VARCHAR(20),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
);

CREATE TABLE IF NOT EXISTS CUENTA_AHORRO (
  id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL UNIQUE,
  saldo_horas DECIMAL(12,6) NOT NULL DEFAULT 0,
  CHECK (saldo_horas >= 0),
  FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario)
);

CREATE TABLE IF NOT EXISTS MOVIMIENTO_AHORRO (
  id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
  id_cuenta INT NOT NULL,
  fecha_hora DATETIME NOT NULL,
  cantidad_horas DECIMAL(12,6) NOT NULL,
  motivo VARCHAR(160) NOT NULL,
  FOREIGN KEY (id_cuenta) REFERENCES CUENTA_AHORRO (id_cuenta)
);

CREATE TABLE IF NOT EXISTS CENTRO_COMERCIAL (
  id_centro INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  direccion VARCHAR(120),
  estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS TARIFA (
  id_tarifa INT AUTO_INCREMENT PRIMARY KEY,
  id_centro INT NOT NULL,
  valor_hora DECIMAL(10,2) NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NULL,
  estado VARCHAR(20) NOT NULL,
  FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro)
);

CREATE TABLE IF NOT EXISTS SESION_PARQUEADERO (
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
  FOREIGN KEY (id_vehiculo) REFERENCES VEHICULO (id_vehiculo),
  FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro),
  FOREIGN KEY (id_tarifa) REFERENCES TARIFA (id_tarifa)
);

ALTER TABLE SESION_PARQUEADERO
  MODIFY COLUMN horas_gratuitas DECIMAL(8,6);

SET @beneficios_qr_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'SESION_PARQUEADERO'
    AND column_name = 'beneficios_qr'
);
SET @beneficios_qr_migration = IF(
  @beneficios_qr_column_exists = 0,
  'ALTER TABLE SESION_PARQUEADERO ADD COLUMN beneficios_qr INT NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE beneficios_qr_statement FROM @beneficios_qr_migration;
EXECUTE beneficios_qr_statement;
DEALLOCATE PREPARE beneficios_qr_statement;

CREATE TABLE IF NOT EXISTS BENEFICIO_QR_APLICADO (
  id_beneficio_qr INT AUTO_INCREMENT PRIMARY KEY,
  clave_compra_hash CHAR(64) NOT NULL,
  id_sesion INT NOT NULL,
  fecha_aplicacion DATETIME NOT NULL,
  UNIQUE KEY uk_beneficio_qr_clave_hash (clave_compra_hash),
  FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
);

CREATE TABLE IF NOT EXISTS REGISTRO_ACCESO (
  id_acceso INT AUTO_INCREMENT PRIMARY KEY,
  id_centro INT NOT NULL,
  id_sesion INT NULL,
  placa_detectada VARCHAR(10),
  fecha_hora DATETIME NOT NULL,
  tipo_evento VARCHAR(10) NOT NULL,
  resultado_deteccion VARCHAR(20) NOT NULL,
  FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro),
  FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
);
