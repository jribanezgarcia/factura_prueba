CREATE TABLE IF NOT EXISTS numero_disponible (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  serie_id INTEGER NOT NULL REFERENCES serie(id),
  anio INTEGER NOT NULL,
  correlativo INTEGER NOT NULL,
  UNIQUE(serie_id, anio, correlativo)
);

CREATE INDEX IF NOT EXISTS idx_numero_disp ON numero_disponible(serie_id, anio, correlativo);
