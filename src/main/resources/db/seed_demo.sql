UPDATE empresa SET nombre = 'Empresa Demo S.L.', nif = 'B99999997', direccion = 'Calle Ficticia 123',
  cp = '52999', localidad = 'Ciudad Demo', provincia = 'Demo', actividad = 'Demostración',
  email = 'demo@irreal.es', telefono = '900000000', pie_legal = 'Datos ficticios de demostración.' WHERE id = 1;

INSERT INTO cliente (nombre, nif, direccion, cp, localidad, provincia, activo, email) VALUES
  ('Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 1, 'ejemplo@irreal.es'),
  ('Otro Cliente S.L.', 'B77777779', 'Avenida Ficticia 5', '08000', 'Barcelona', 'Barcelona', 1, NULL);

INSERT INTO serie (codigo, descripcion, es_rectificativa, sufijo_fecha) VALUES
  ('A', 'Serie general', 0, 'MES'),
  ('R', 'Rectificativas', 1, 'NINGUNO');

INSERT INTO tipo_retencion (nombre, porcentaje, activo) VALUES ('IRPF profesional', 15, 1);

INSERT INTO factura (serie_id, anio, correlativo, numero, fecha, estado, cliente_id,
  cli_nombre, cli_nif, cli_direccion, cli_cp, cli_localidad, cli_provincia, cli_email,
  descuento, observaciones, rectifica_id, forma_pago, vencimiento, realizada_por,
  retencion_id, retencion_nombre, retencion_porcentaje,
  base_total, iva_total, importe_retencion, total_suplidos, total) VALUES
  (1, 2026, 1, 'A-1/9', '2026-09-01', 'EMITIDA', 1,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   0, 'Factura de demostración.', NULL, 'Transferencia', '2026-10-01', 'Demo',
   NULL, NULL, NULL, '1500.00', '260.00', '0.00', '0.00', '1760.00'),
  (1, 2026, 2, 'A-2/9', '2026-09-02', 'EMITIDA', 1,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   10, NULL, NULL, 'Transferencia', NULL, 'Demo',
   NULL, NULL, NULL, '900.00', '189.00', '0.00', '0.00', '1089.00'),
  (1, 2026, 3, 'A-3/9', '2026-09-03', 'EMITIDA', 2,
   'Otro Cliente S.L.', 'B77777779', 'Avenida Ficticia 5', '08000', 'Barcelona', 'Barcelona', NULL,
   0, NULL, NULL, 'Transferencia', NULL, 'Demo',
   1, 'IRPF profesional', 15, '1000.00', '210.00', '150.00', '0.00', '1060.00'),
  (1, 2026, 4, 'A-4/9', '2026-09-04', 'EMITIDA', 2,
   'Otro Cliente S.L.', 'B77777779', 'Avenida Ficticia 5', '08000', 'Barcelona', 'Barcelona', NULL,
   0, NULL, NULL, 'Transferencia', NULL, 'Demo',
   1, 'IRPF profesional', 15, '1000.00', '210.00', '150.00', '250.00', '1310.00'),
  (1, 2026, 5, 'A-5/9', '2026-09-05', 'ANULADA', 1,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   0, NULL, NULL, 'Transferencia', NULL, 'Demo',
   NULL, NULL, NULL, '500.00', '50.00', '0.00', '0.00', '550.00'),
  (2, 2026, 1, 'R-1', '2026-09-06', 'EMITIDA', 1,
   'Cliente Ejemplo S.L.', 'B88888888', 'Calle Irreal 10', '28000', 'Madrid', 'Madrid', 'ejemplo@irreal.es',
   0, NULL, 1, 'Transferencia', NULL, 'Demo',
   NULL, NULL, NULL, '200.00', '42.00', '0.00', '0.00', '242.00');

INSERT INTO factura_linea (factura_id, orden, cantidad, descripcion, precio_unitario,
  tipo_iva_id, iva_nombre, iva_porcentaje, iva_motivo_exencion, es_suplido) VALUES
  (1, 1, 1, 'Montaje de cocina', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, 0),
  (1, 2, 1, 'Transporte y puesta en obra', '500.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 10%'), 'IVA 10%', 10, NULL, 0),
  (2, 1, 1, 'Montaje de cocina', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, 0),
  (3, 1, 1, 'Montaje de cocina', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, 0),
  (4, 1, 1, 'Montaje de cocina', '1000.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, 0),
  (4, 2, 1, 'Tasas municipales', '250.00', (SELECT id FROM tipo_iva WHERE nombre = 'Suplido'), 'Suplido', NULL, NULL, 1),
  (5, 1, 1, 'Transporte y puesta en obra', '500.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 10%'), 'IVA 10%', 10, NULL, 0),
  (6, 1, 1, 'Abono parcial', '200.00', (SELECT id FROM tipo_iva WHERE nombre = 'IVA 21%'), 'IVA 21%', 21, NULL, 0);
