INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido)
SELECT 'Suplido', NULL, NULL, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM tipo_iva WHERE es_suplido = 1);
