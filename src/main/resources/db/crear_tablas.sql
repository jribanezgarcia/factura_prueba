CREATE TABLE IF NOT EXISTS cliente (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  nif TEXT NOT NULL UNIQUE,
  direccion TEXT,
  cp TEXT,
  localidad TEXT,
  provincia TEXT,
  activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
  email TEXT
);

CREATE TABLE IF NOT EXISTS serie (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  codigo TEXT NOT NULL UNIQUE,
  descripcion TEXT,
  es_rectificativa INTEGER NOT NULL DEFAULT 0 CHECK (es_rectificativa IN (0, 1)),
  sufijo_fecha TEXT NOT NULL DEFAULT 'MES' CHECK (sufijo_fecha IN ('MES', 'ANIO', 'NINGUNO'))
);

CREATE TABLE IF NOT EXISTS tipo_iva (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE,
  porcentaje INTEGER,
  motivo_exencion TEXT,
  activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
  es_suplido INTEGER NOT NULL DEFAULT 0 CHECK (es_suplido IN (0, 1))
);

CREATE TABLE IF NOT EXISTS tipo_retencion (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE,
  porcentaje INTEGER NOT NULL,
  activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
);

CREATE TABLE IF NOT EXISTS factura (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  serie_id INTEGER NOT NULL REFERENCES serie(id),
  anio INTEGER NOT NULL,
  correlativo INTEGER NOT NULL,
  numero TEXT NOT NULL,
  fecha TEXT NOT NULL,
  estado TEXT NOT NULL CHECK (estado IN ('EMITIDA', 'ANULADA')),
  cliente_id INTEGER REFERENCES cliente(id),
  cli_nombre TEXT,
  cli_nif TEXT,
  cli_direccion TEXT,
  cli_cp TEXT,
  cli_localidad TEXT,
  cli_provincia TEXT,
  cli_email TEXT,
  descuento INTEGER NOT NULL DEFAULT 0,
  observaciones TEXT,
  rectifica_id INTEGER REFERENCES factura(id),
  forma_pago TEXT,
  vencimiento TEXT,
  realizada_por TEXT,
  retencion_id INTEGER REFERENCES tipo_retencion(id),
  retencion_nombre TEXT,
  retencion_porcentaje INTEGER,
  base_total TEXT NOT NULL DEFAULT '0.00',
  iva_total TEXT NOT NULL DEFAULT '0.00',
  importe_retencion TEXT NOT NULL DEFAULT '0.00',
  total_suplidos TEXT NOT NULL DEFAULT '0.00',
  total TEXT NOT NULL DEFAULT '0.00',
  UNIQUE (serie_id, anio, correlativo)
);

CREATE TABLE IF NOT EXISTS factura_linea (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  factura_id INTEGER NOT NULL REFERENCES factura(id),
  orden INTEGER NOT NULL,
  cantidad INTEGER NOT NULL DEFAULT 1,
  descripcion TEXT,
  precio_unitario TEXT NOT NULL DEFAULT '0',
  tipo_iva_id INTEGER REFERENCES tipo_iva(id),
  iva_nombre TEXT,
  iva_porcentaje INTEGER,
  iva_motivo_exencion TEXT,
  es_suplido INTEGER NOT NULL DEFAULT 0 CHECK (es_suplido IN (0, 1))
);

CREATE INDEX IF NOT EXISTS idx_linea_factura ON factura_linea(factura_id, orden);

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
  pie_legal TEXT
);

CREATE TABLE IF NOT EXISTS preferencias (
  clave TEXT PRIMARY KEY,
  valor TEXT
);

INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES ('IVA 21%', 21, NULL, 1, 0);
INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES ('IVA 10%', 10, NULL, 1, 0);
INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES ('Exento', NULL, NULL, 1, 0);
INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES ('Suplido', NULL, NULL, 1, 1);

INSERT OR IGNORE INTO empresa (id) VALUES (1);
