CREATE TABLE IF NOT EXISTS tipo_retencion (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  porcentaje INTEGER NOT NULL,
  activo INTEGER NOT NULL DEFAULT 1
);

ALTER TABLE factura_version ADD COLUMN tipo_retencion_id INTEGER REFERENCES tipo_retencion(id);
ALTER TABLE factura_version ADD COLUMN importe_retencion NUMERIC;
