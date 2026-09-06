ALTER TABLE tipo_iva ADD COLUMN es_suplido INTEGER NOT NULL DEFAULT 0;
ALTER TABLE factura_linea ADD COLUMN es_suplido INTEGER NOT NULL DEFAULT 0;
ALTER TABLE factura_version ADD COLUMN total_suplidos TEXT;
INSERT OR IGNORE INTO tipo_iva (id, nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES (4, 'Suplido', NULL, NULL, 1, 1);
