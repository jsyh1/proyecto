-- Tablas que usa la HU "Registrar entrada por placa y consultar tiempo y costo".
-- Si ya ejecutaste el script completo del modelo relacional, no es necesario correr este.
USE parqueadero;

CREATE TABLE IF NOT EXISTS USUARIO (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL,
  apellido VARCHAR(50) NOT NULL,
  documento VARCHAR(20) NOT NULL UNIQUE,
  correo VARCHAR(100) NOT NULL UNIQUE,
  telefono VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL,
  rol VARCHAR(20) NOT NULL DEFAULT 'USER'
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
  horas_gratuitas DECIMAL(5,2),
  monto_total DECIMAL(10,2),
  estado VARCHAR(20) NOT NULL,
  FOREIGN KEY (id_vehiculo) REFERENCES VEHICULO (id_vehiculo),
  FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro),
  FOREIGN KEY (id_tarifa) REFERENCES TARIFA (id_tarifa)
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

CREATE TABLE IF NOT EXISTS BENEFICIO (
  id_beneficio INT AUTO_INCREMENT PRIMARY KEY,
  id_centro INT NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  tipo VARCHAR(30) NOT NULL,
  valor DECIMAL(10,2) NOT NULL,
  fecha_inicio DATE NOT NULL,
  fecha_fin DATE NULL,
  activo BOOLEAN NOT NULL,
  FOREIGN KEY (id_centro) REFERENCES CENTRO_COMERCIAL (id_centro)
);

CREATE TABLE IF NOT EXISTS QR_BENEFICIO (
  id_qr INT AUTO_INCREMENT PRIMARY KEY,
  codigo_hash CHAR(64) NOT NULL UNIQUE,
  id_beneficio INT NOT NULL,
  fecha_vencimiento DATETIME NOT NULL,
  consumido BOOLEAN NOT NULL,
  fecha_consumo DATETIME NULL,
  FOREIGN KEY (id_beneficio) REFERENCES BENEFICIO (id_beneficio)
);

CREATE TABLE IF NOT EXISTS VALIDACION_QR (
  id_validacion INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  id_sesion INT NOT NULL,
  id_qr INT NULL,
  resultado VARCHAR(20) NOT NULL,
  motivo VARCHAR(200),
  descuento_aplicado DECIMAL(10,2) NOT NULL,
  fecha_validacion DATETIME NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
  FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion),
  FOREIGN KEY (id_qr) REFERENCES QR_BENEFICIO (id_qr)
);

CREATE TABLE IF NOT EXISTS PAGO (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_sesion INT NOT NULL UNIQUE,
  metodo VARCHAR(30) NOT NULL,
  estado VARCHAR(20) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  fecha_pago DATETIME NOT NULL,
  FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
);

CREATE TABLE IF NOT EXISTS NOTIFICACION (
  id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  id_sesion INT NOT NULL,
  mensaje VARCHAR(300) NOT NULL,
  leida BOOLEAN NOT NULL,
  fecha_creacion DATETIME NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES USUARIO (id_usuario),
  FOREIGN KEY (id_sesion) REFERENCES SESION_PARQUEADERO (id_sesion)
);
