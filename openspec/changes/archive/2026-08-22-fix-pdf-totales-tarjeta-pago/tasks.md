## 1. Resumen con bases pre-descuento

- [x] 1.1 Añadir a `ResumenFactura` los campos `baseBruta` (suma de bases antes del descuento) e `importeDescuento` (bruta − descontada) con sus getters; verificar que compila con `mvn -q compile`
- [x] 1.2 Asignarlos en `CalculoService.resumen(...)` desde `baseTotalSinDescuento` y la resta ya redondeada; ampliar `CalculoServiceTest` con caso sin descuento (importeDescuento = 0, bruta = descontada), caso 10 % sobre 1.000 (189 de cuota, −100, total 1.089) y caso varios tipos de IVA; ejecutar `mvn test -Dtest=CalculoServiceTest`

## 2. Bloque de totales del PDF

- [x] 2.1 Reescribir `bloqueTotales` en `pdf/PdfService.java`: quitar las filas «Base total»/«IVA total»; primera fila «Base» = baseBruta cuando hay descuento y baseTotal en caso contrario; una fila «Descuento n%» restando en rojo suave solo si descuento > 0; mantener pares Base/IVA por grupo y TOTAL en color; verificar compilación
- [x] 2.2 Compactar el bloque (paddings/spacingBefore reducidos) manteniéndolo alineado a la derecha

## 3. Tarjeta Datos de pago clara

- [x] 3.1 Añadir variante de cabecera clara (fondo blanco, borde fino inferior del color de acento, texto marrón oscuro) y usarla solo en `tarjetaPago`; «Facturar a» intacta; verificar compilación

## 4. Paginación del cierre del documento

- [x] 4.1 Aplicar reparto de filas entre páginas a la tabla de líneas (`setSplitLate(false)` o equivalente) y revisar espaciados finales para evitar página con solo totales; verificar compilación
- [x] 4.2 Comprobar con un PDF generado que el contador «Página X de Y» refleja las páginas reales tras el cambio

## 5. Verificación completa

- [x] 5.1 Pasar la suite completa: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (39 tests en verde + los nuevos)
- [x] 5.2 Verificación manual conjunta con el usuario: exportar desde Histórico una factura individual y un lote, comparando con `prototipos/pdf-fix-v2.html` (totales, tarjeta clara, salto de página); cierra también la tarea 4.2 del cambio `exportar-pdf-desde-historico`
