## Context

El PDF actual (`pdf/PdfService.java`) ya implementa el diseño aprobado (tarjetas, tabla de líneas, resumen con TOTAL en color, pie con `Página X de Y` vía plantilla). La revisión sobre un PDF real detectó 3 desviaciones frente a `prototipos/pdf-fix-v2.html`: filas duplicadas en totales, cabecera marrón en «Datos de pago» y salto de página que deja los totales solos.

Dato clave del modelo: `CalculoService.resumen(...)` devuelve las bases de cada grupo **ya descontadas** (`base × factor`), y `getTotal()` = baseDescontada + iva. El prototipo aprobado pinta, cuando hay descuento: base SIN descontar, cuota IVA sobre la base descontada, fila «Descuento n%» restando y TOTAL. Con solo lo que expone hoy el resumen ese cuadre no se puede pintar.

## Goals / Non-Goals

**Goals:**

- Resumen del PDF sin filas agregadas repetidas y con el cuadre visible Base − Descuento + IVA = TOTAL.
- Cabecera clara en «DATOS DE PAGO» sin tocar «FACTURAR A».
- Cierre del documento compacto para minimizar páginas con solo totales; contador real intacto.
- Suite completa en verde tras el cambio.

**Non-Goals:**

- No cambia el cálculo fiscal ni lo que muestra el editor (sigue viendo base/IVA/totales como ahora).
- No cambia persistencia, nombres de archivo, marca ANULADA ni cabeceras/pies repetidos.
- No se toca el cambio activo `exportar-pdf-desde-historico`.

## Decisions

- **Exponer bases pre-descuento en el resumen, no recalcular a la inversa en el PDF.** Se añaden a `ResumenFactura` la suma de bases antes del descuento (`getBaseBruta()`) y el importe descontado (`getImporteDescuento()`), y a `IvaGrupo` la base bruta de cada grupo (`getBaseBruta()`); `CalculoService.resumen(...)` ya tiene esos valores en mano (`baseTotalSinDescuento`, base del grupo sin factor). Es necesaria la bruta por grupo para que con varios tipos de IVA + descuento cada par Base/IVA se muestre por separado y el conjunto cuadre (Σ brutas − importeDescuento = Σ descontadas). Alternativa descartada: reconstruirlos en `PdfService` dividiendo entre el factor (pérdida de precisión por redondeos y duplicación de lógica fiscal fuera del servicio de cálculo).
- **Fila de descuento única calculada desde los totales** (importe = bruta global − descontada global), no una por grupo: coincide con el prototipo («un solo descuento») y evita redondeos por grupo. Con descuento, cada fila «Base n%» muestra la bruta del grupo; sin descuento, la descontada (= bruta). El cuadre visible se mantiene: Σ bases − Descuento + Σ cuotas = TOTAL. Cuando hay un solo tipo de IVA la etiqueta es «Base» a secas (como el prototipo); con varios, «Base 21%», etc.; las exentas conservan «Base exenta (motivo)».
- **Cabecera de tarjeta parametrizada**: `cabeceraTarjeta(titulo, c)` gana una variante clara (fondo blanco, borde inferior fino `bordeTabla`, texto `oscuro`) usada solo por `tarjetaPago`. Alternativa descartada: dos métodos casi iguales (duplicación innecesaria).
- **`filaResumen` pasa a añadir ambas celdas**: el método original construía la celda de la etiqueta pero devolvía únicamente la del valor, así que las etiquetas del resumen («Base», «IVA n%», …) nunca llegaron a pintarse en ningún PDF anterior (bug latente descubierto al testear este cambio). Nueva firma `void filaResumen(PdfPTable t, String etiqueta, String valor)` que añade etiqueta y valor, igual que `filaDescuento`.
- **Paginación por composición, no por trucos del writer**: quitar 2 filas del bloque de totales ya lo acorta; además `setSplitLate(false)` en las tablas grandes (líneas) para que repartan filas en vez de saltar en bloque y espaciados (`espacio`, `spacingBefore`) revisados hacia valores menores al final del documento. El contador con plantilla (`totalPaginas`) no se toca: ya cuenta páginas reales.

## Risks / Trade-offs

- [Cambiar la cifra «Base» cuando hay descuento puede sorprender a quien comparaba con el editor] → El editor sigue mostrando base descontada; el PDF documenta el cuadre con la fila de descuento visible. Verificación manual conjunta tras implementar.
- [Redondeos: bruta − descontada ≠ Σ descuentos por grupo] → El importe mostrado sale de la resta de los dos totales ya redondeados por `CalculoService`; el TOTAL no varía.
- [`setSplitLate(false)` podría partir una fila de línea entre páginas] → OpenPDF parte entre filas, nunca dentro de una celda de una fila simple; se comprueba con la factura larga de la verificación manual.
