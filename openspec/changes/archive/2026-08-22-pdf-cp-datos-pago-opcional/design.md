## Context

Ajuste decidido por el usuario tras ver el PDF: el CP necesita su propia fila etiquetada (hoy va fundido en Población con la provincia) y la tarjeta «Datos de pago» no debe pintarse cuando no hay ningún dato.

## Goals / Non-Goals

**Goals:**

- Filas independientes: Nombre / NIF / Dirección / Código postal / Población / Provincia / Email.
- «Datos de pago» oculta si sus tres campos están vacíos; «Facturar A» conserva anchura.

**Non-Goals:**

- No cambia modelo, servicio ni el resto del PDF.

## Decisions

- **`tarjetaCliente`**: lista de filas por campo individual; `Población` = `cliLocalidad`; `Código postal` = `cliCp`; `Provincia` = `cliProvincia`. Columna de etiquetas 30%→32% para que «Código postal» quepa en una línea.
- **`tarjetas()`**: las filas de pago se calculan antes (`filasDatosPago(vc)`); si están vacías, el hueco central gana `colspan=2` y no se añade la celda de la tarjeta de pago. La tabla exterior mantiene `{49, 2, 49}` para que «Facturar A» no cambie de ancho.
- **`tarjetaPago`**: pasa a recibir la lista de filas ya construida (se elimina el caso «—» interno porque solo se llama con filas).

## Risks / Trade-offs

- [Tests existentes asumen «DATOS DE PAGO» siempre visible] → se ajustan: la muestra del test sin datos de pago afirma ahora su ausencia; los assertions del contenido pasan al test con datos rellenos.
