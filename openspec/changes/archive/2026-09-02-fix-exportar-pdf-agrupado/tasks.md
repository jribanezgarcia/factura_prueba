## 1. Fix en PdfService.concatenar

- [x] 1.1 Eliminar el try-with-resources del `FileOutputStream` en `concatenar()` y crear el stream inline en el constructor de `PdfCopy`, manteniendo el try/finally con `document.close()`. Verificar que el test nuevo `exportarAgrupadoUneDosFacturasEnUnSoloPdf` pasa.
- [x] 1.2 Ejecutar la suite completa (`mvn test`) y confirmar 125+ tests en verde sin regresiones.

## 2. Cierre

- [ ] 2.1 Actualizar CONTINUAR_MAÑANA.md con la sesión y el fix.
- [ ] 2.2 Archivar el change y commitear.
