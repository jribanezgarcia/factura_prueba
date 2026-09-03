## Context

`CalculoService.resumen(List<LineaFactura>, int descuento, TipoRetencion)` calcula dos magnitudes derivadas de la base:

- `baseTotalDescontada = round2(baseTotalSinDescuento × factor)` (línea 106), que alimenta el desglose por tipo de IVA (línea 119) y las cuotas (línea 141).
- `importeRetencion = round2(baseTotalSinDescuento × pct / 100)` (línea 149).

La primera usa la base descontada; la segunda, la bruta. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que la retención use la misma base imponible que el IVA.
- Que las facturas sin descuento global den exactamente el mismo importe que hoy.
- Que el total deje de poder salir negativo por un descuento del 100 %.
- Que el test que fijaba el comportamiento incorrecto se convierta en un test del comportamiento correcto, y que se compruebe que falla con el código anterior.

**Non-Goals:**

- No se toca el esquema de base de datos ni se migran importes ya guardados.
- No se recalculan las versiones históricas.
- No se aborda que `PdfService` recalcule los totales en vez de leer los guardados (ver proposal.md - Riesgo conocido).
- No se cambia el rango admitido de descuento en `FacturaService.validar`: el caso del 100 % deja de dar un total negativo como consecuencia del propio cambio de base, sin necesidad de una regla nueva.

## Decisions

### D1. Cambiar la base del cálculo, no añadir una opción configurable

Se descarta hacer la base de la retención configurable por empresa. No hay dos comportamientos legítimos entre los que elegir: la retención se practica sobre la base imponible, punto. Una opción sólo serviría para conservar un cálculo incorrecto y añadiría una dimensión más a los tests y a la interfaz.

El cambio es sustituir `baseTotalSinDescuento` por `baseTotalDescontada` en la línea 149.

### D2. Un único punto de cálculo

`CalculoService.resumen(...)` es el único sitio del proyecto donde se calcula el importe de retención. El editor (`EditorController:824`), el versionado (`VersionadoService:53` y `:109`) y el PDF (`PdfService:106`) lo llaman, así que la corrección se propaga sola a la interfaz, a lo que se persiste y a lo que se imprime. No hace falta tocar ninguno de esos tres.

### D3. La retención se calcula sobre el total descontado, no sobre la suma de grupos

`baseTotalDescontada` se redondea una sola vez a partir de la base bruta total (línea 106), mientras que las bases por tipo de IVA se redondean grupo a grupo y luego se ajusta el céntimo de diferencia en el grupo mayor (líneas 131-137). Para la retención se usa `baseTotalDescontada` directamente, que es el valor que la factura presenta como base imponible total y con el que el usuario puede cuadrar la resta a mano.

### D4. Coherencia con lo ya guardado

Las versiones existentes conservan su `importe_retencion` y su `total`: no se migra nada. La consecuencia es que una factura antigua con descuento y retención mostrará en el histórico el importe con el que se emitió, mientras que si se reabre y se vuelve a guardar pasará a usar la base correcta. Es el comportamiento deseado — una factura reemitida se recalcula con las reglas vigentes — pero conviene tenerlo presente al verificar.

## Verificación

- `CalculoServiceTest`: el caso con descuento pasa de esperar `190,00 / 899,00` a esperar `171,00 / 918,00`. Comprobar que el test nuevo **falla** con el código anterior antes de aplicar el cambio.
- Caso nuevo de descuento del 100 %: base 0,00, IVA 0,00, retención 0,00, total 0,00.
- Los tests de retención de `FacturaServiceTest` (`:177`) y `FacturacionMensualServiceTest` (`:212`) usan descuento 0, así que sus valores no deben cambiar. Si cambian, algo se ha roto.
- Suite completa: 156 tests.
- Comprobación manual: factura con descuento y retención, verificando el resumen del editor y el PDF.
