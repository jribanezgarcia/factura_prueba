## Context

El PDF ya es fiel al prototipo en tipografía, redondeos, colores y pie; faltan los rótulos de identificación del prototipo: bajo FACTURA el par rótulo→valor (`Serie / Nº`, `Fecha`) y en «Facturar a» los campos etiquetados (Nombre/NIF/Dirección/Población/Email), con el código postal visible dentro de Población.

## Goals / Non-Goals

**Goals:**

- Bloque FACTURA con pares rótulo (pequeño, marrón claro) → valor (negrita).
- «Facturar a» como tabla etiqueta→valor igual que «Datos de pago».

**Non-Goals:**

- No se toca nada más del PDF ni modelo/servicio.

## Decisions

- **Rótulos del bloque FACTURA**: dibujados por evento de página a la derecha; rótulo 6.5pt negrita en `GRIS_CLARO` y valor debajo en negrita 10pt tinta. Espaciado vertical: −22 tras el título, −9 rótulo→valor, −13 valor→rótulo siguiente.
- **«Facturar a»**: misma estructura interna que `tarjetaPago` (tabla 30/70 con etiqueta `GRIS_CLARO` 8.5pt); Nombre en negrita 10.5pt, resto regular 9.5pt tinta. Población = CP + localidad (+ provincia entre paréntesis si existe). Filas vacías se omiten; si no hay ningún dato, guion como hoy.
