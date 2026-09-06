UPDATE empresa SET nombre = 'Empresa Demo S.L.', nif = 'B99999999', direccion = 'Calle Ficticia 123',
  cp = '99999', localidad = 'Ciudad Demo', provincia = 'Demo', actividad = 'Demostración',
  email = 'demo@irreal.es', telefono = '900000000', pie_legal = 'Datos ficticios de demostración.' WHERE id = 1;

INSERT INTO cliente (nombre, nif, direccion, cp, localidad, provincia, activo, email) VALUES
  ('Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 1, 'ejemplo@irreal.es'),
  ('Otro Cliente S.L.', 'B77777777', 'Avenida Ficticia 5', '08000', 'Barcelona', 'Barcelona', 1, NULL);

INSERT INTO serie (codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados, sufijo_fecha) VALUES
  ('A', 'Serie general', 0, 6, 0, 'MES'),
  ('R', 'Rectificativas', 1, 2, 0, 'NINGUNO');

INSERT INTO serie_siguiente (serie_id, anio, siguiente) VALUES (1, 2026, 6);
INSERT INTO serie_siguiente (serie_id, anio, siguiente) VALUES (2, 2026, 2);

INSERT INTO tipo_retencion (nombre, porcentaje, activo) VALUES ('IRPF profesional', 15, 1);

INSERT INTO factura (serie_id, correlativo, cliente_id) VALUES
  (1, 1, 1),
  (1, 2, 1),
  (1, 3, 2),
  (1, 4, 2),
  (1, 5, 1),
  (2, 1, 1);

INSERT INTO factura_version (factura_id, version_num, numero, fecha_factura, fecha_guardado, estado,
  descuento_porcentaje, observaciones, referencia_rectifica,
  cli_nombre, cli_nif, cli_direccion, cli_cp, cli_localidad, cli_provincia, cli_email,
  forma_pago, vencimiento, realizada_por,
  base_total, iva_total, total, tipo_retencion_id, tipo_retencion_nombre,
  tipo_retencion_porcentaje, importe_retencion, total_suplidos) VALUES
  (1, 1, 'A-1/9', '2026-09-01', '2026-09-01 12:00:00', 'EMITIDA',
   0, 'Factura de demostración.', NULL,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   'Transferencia', '2026-10-01', 'Demo',
   '1500.00', '260.00', '1760.00', NULL, NULL, NULL, '0.00', '0.00'),
  (2, 1, 'A-2/9', '2026-09-02', '2026-09-02 12:00:00', 'EMITIDA',
   10, NULL, NULL,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   'Transferencia', NULL, 'Demo',
   '900.00', '189.00', '1089.00', NULL, NULL, NULL, '0.00', '0.00'),
  (3, 1, 'A-3/9', '2026-09-03', '2026-09-03 12:00:00', 'EMITIDA',
   0, NULL, NULL,
   'Otro Cliente S.L.', 'B77777777', 'Avenida Ficticia 5', '08000', 'Barcelona', 'Barcelona', NULL,
   'Transferencia', NULL, 'Demo',
   '1000.00', '210.00', '1060.00', 1, 'IRPF profesional', 15, '150.00', '0.00'),
  (4, 1, 'A-4/9', '2026-09-04', '2026-09-04 12:00:00', 'EMITIDA',
   0, NULL, NULL,
   'Otro Cliente S.L.', 'B77777777', 'Avenida Ficticia 5', '08000', 'Barcelona', 'Barcelona', NULL,
   'Transferencia', NULL, 'Demo',
   '1000.00', '210.00', '1310.00', 1, 'IRPF profesional', 15, '150.00', '250.00'),
  (5, 1, 'A-5/9', '2026-09-05', '2026-09-05 12:00:00', 'ANULADA',
   0, NULL, NULL,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   'Transferencia', NULL, 'Demo',
   '500.00', '50.00', '550.00', NULL, NULL, NULL, '0.00', '0.00'),
  (6, 1, 'R-1', '2026-09-06', '2026-09-06 12:00:00', 'EMITIDA',
   0, NULL, 'A-1/9',
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   'Transferencia', NULL, 'Demo',
   '200.00', '42.00', '242.00', NULL, NULL, NULL, '0.00', '0.00');

INSERT INTO factura_linea (factura_version_id, orden, cantidad, descripcion, precio_unitario,
  total_base, tipo_iva_id, iva_nombre, iva_porcentaje, iva_motivo_exencion, iva_importe, es_suplido) VALUES
  (1, 1, 1, 'Montaje de cocina', '1000.00', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, '210.00', 0),
  (1, 2, 1, 'Transporte y puesta en obra', '500.00', '500.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 10%'), 'IVA 10%', 10, NULL, '50.00', 0),
  (2, 1, 1, 'Montaje de cocina', '1000.00', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, '210.00', 0),
  (3, 1, 1, 'Montaje de cocina', '1000.00', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, '210.00', 0),
  (4, 1, 1, 'Montaje de cocina', '1000.00', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, '210.00', 0),
  (4, 2, 1, 'Tasas municipales', '250.00', '250.00', (SELECT id FROM tipo_iva WHERE nombre = 'Suplido'), 'Suplido', NULL, NULL, '0.00', 1),
  (5, 1, 1, 'Transporte y puesta en obra', '500.00', '500.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 10%'), 'IVA 10%', 10, NULL, '50.00', 0),
  (6, 1, 1, 'Abono parcial', '200.00', '200.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, '42.00', 0);
