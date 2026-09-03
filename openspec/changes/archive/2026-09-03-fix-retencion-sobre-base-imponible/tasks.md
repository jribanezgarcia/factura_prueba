## 1. Test que reproduce el fallo

- [x] 1.1 Reescribir `CalculoServiceTest.retencionConDescuentoUsaBaseBruta` como `retencionUsaLaBaseImponibleDescontada`: base 1.000,00, descuento 10 %, IVA 21 %, retención 19 % → base imponible 900,00, IVA 189,00, retención **171,00**, total **918,00**.
- [x] 1.2 Ejecutar y confirmar que **falla** con el código actual (esperado 171,00, obtenido 190,00).
- [x] 1.3 Añadir `descuentoDelCienPorCienNoDaTotalNegativo`: base 1.000,00, descuento 100 %, retención 15 % → base 0,00, IVA 0,00, retención 0,00, total 0,00. Confirmar que falla con el código actual (total −150,00).

## 2. Cálculo

- [x] 2.1 En `CalculoService.resumen(...)` línea 149, calcular `importeRetencion` sobre `baseTotalDescontada` en lugar de `baseTotalSinDescuento`.
- [x] 2.2 Actualizar el Javadoc de la clase (línea 23) y del método (línea 88), que hoy documentan la base bruta.

## 3. Tests de regresión

- [x] 3.1 Confirmar que `retencionSobreBaseBrutaRestaDelTotal` (sin descuento) sigue en verde con 150,00 y 1.060,00; renombrarlo a `retencionSinDescuentoRestaDelTotal` para que el nombre deje de mentir.
- [x] 3.2 Confirmar sin tocarlos que `FacturaServiceTest.crearFacturaConRetencionGuardaImporteYNombre` y `FacturacionMensualServiceTest.aplicaIvaYRetencionEnTotales` siguen en verde: ambos usan descuento 0 y sus importes no deben variar.

## 4. Especificación

- [x] 4.1 MODIFIED «Retención de IRPF»: la base pasa a ser la base imponible; escenario «Factura con descuento y retención» con los valores nuevos (900,00 / 189,00 / 135,00 / 954,00); escenario nuevo de descuento del 100 %.
- [x] 4.2 MODIFIED «Retención en rectificativas»: sustituir «sobre la base bruta» por «sobre la base imponible».

## 5. Verificación final

- [x] 5.1 Suite completa en verde (156 tests) con `mvn clean test`.
- [x] 5.2 Verificación manual con el usuario: factura con descuento global y retención; comprobar el resumen del editor y el PDF exportado.
- [x] 5.3 Anotar como trabajo aparte que `PdfService` recalcula los totales en lugar de leer los guardados, con el efecto que eso tiene al reexportar facturas antiguas (ver proposal.md).
