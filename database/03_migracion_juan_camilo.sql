-- Ejecutar una sola vez sobre bases creadas antes de habilitar los roles.
USE parqueadero;

ALTER TABLE USUARIO
  ADD COLUMN rol VARCHAR(20) NOT NULL DEFAULT 'USER';
