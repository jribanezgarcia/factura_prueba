CREATE TABLE IF NOT EXISTS cliente (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  nif TEXT,
  direccion TEXT,
  cp TEXT,
  localidad TEXT,
  provincia TEXT,
  activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS serie (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  codigo TEXT NOT NULL UNIQUE,
  descripcion TEXT,
  es_rectificativa INTEGER NOT NULL DEFAULT 0,
  siguiente_correlativo INTEGER NOT NULL DEFAULT 1,
  reutilizar_anulados INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tipo_iva (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  porcentaje INTEGER,
  motivo_exencion TEXT,
  activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS factura (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  serie_id INTEGER NOT NULL REFERENCES serie(id),
  correlativo INTEGER NOT NULL,
  cliente_id INTEGER REFERENCES cliente(id)
);

CREATE TABLE IF NOT EXISTS factura_version (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  factura_id INTEGER NOT NULL REFERENCES factura(id),
  version_num INTEGER NOT NULL,
  numero TEXT NOT NULL,
  fecha_factura TEXT NOT NULL,
  fecha_guardado TEXT NOT NULL,
  estado TEXT NOT NULL,
  descuento_porcentaje INTEGER NOT NULL DEFAULT 0,
  observaciones TEXT,
  referencia_rectifica TEXT,
  cli_nombre TEXT,
  cli_nif TEXT,
  cli_direccion TEXT,
  cli_cp TEXT,
  cli_localidad TEXT,
  cli_provincia TEXT,
  base_total TEXT NOT NULL DEFAULT '0.00',
  iva_total TEXT NOT NULL DEFAULT '0.00',
  total TEXT NOT NULL DEFAULT '0.00'
);

CREATE INDEX IF NOT EXISTS idx_version_factura ON factura_version(factura_id, version_num);

CREATE TABLE IF NOT EXISTS factura_linea (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  factura_version_id INTEGER NOT NULL REFERENCES factura_version(id),
  orden INTEGER NOT NULL,
  cantidad INTEGER NOT NULL DEFAULT 1,
  descripcion TEXT,
  precio_unitario TEXT NOT NULL DEFAULT '0',
  total_base TEXT NOT NULL DEFAULT '0.00',
  tipo_iva_id INTEGER REFERENCES tipo_iva(id),
  iva_nombre TEXT,
  iva_porcentaje INTEGER,
  iva_motivo_exencion TEXT,
  iva_importe TEXT NOT NULL DEFAULT '0.00'
);

CREATE INDEX IF NOT EXISTS idx_linea_version ON factura_linea(factura_version_id, orden);

CREATE TABLE IF NOT EXISTS empresa (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  nombre TEXT,
  nif TEXT,
  direccion TEXT,
  cp TEXT,
  localidad TEXT,
  provincia TEXT,
  actividad TEXT,
  email TEXT,
  telefono TEXT,
  cabecera_modo TEXT NOT NULL DEFAULT 'TEXTO',
  logo_path TEXT,
  logo_x INTEGER NOT NULL DEFAULT 0,
  logo_y INTEGER NOT NULL DEFAULT 0,
  logo_ancho INTEGER,
  logo_alto INTEGER,
  pie_legal TEXT
);

CREATE TABLE IF NOT EXISTS preferencias (
  clave TEXT PRIMARY KEY,
  valor TEXT
);

INSERT OR IGNORE INTO tipo_iva (id, nombre, porcentaje, motivo_exencion, activo) VALUES (1, 'IVA 21%', 21, NULL, 1);
INSERT OR IGNORE INTO tipo_iva (id, nombre, porcentaje, motivo_exencion, activo) VALUES (2, 'IVA 10%', 10, NULL, 1);
INSERT OR IGNORE INTO tipo_iva (id, nombre, porcentaje, motivo_exencion, activo) VALUES (3, 'Exento', NULL, NULL, 1);

INSERT OR IGNORE INTO empresa (id) VALUES (1);
