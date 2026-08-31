CREATE TABLE IF NOT EXISTS serie_siguiente (
  serie_id INTEGER NOT NULL REFERENCES serie(id),
  anio INTEGER NOT NULL,
  siguiente INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (serie_id, anio)
);

INSERT OR IGNORE INTO serie_siguiente (serie_id, anio, siguiente)
SELECT id, CAST(strftime('%Y', 'now') AS INTEGER), COALESCE(NULLIF(siguiente_correlativo, 0), 1)
FROM serie;