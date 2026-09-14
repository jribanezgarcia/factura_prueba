> Las rutas son relativas a `src/main/java/com/alcazaba/facturacion/` salvo que se diga otra cosa. Los valores exactos y sus razones están en `design.md`.

## 1. Medición previa

- [x] 1.1 Escribir un test temporal `TmpMedidasPdfTest` en `src/test/java/com/alcazaba/facturacion/pdf/` que imprima por consola: el ancho con `EstiloPdf.baseNegrita().getWidthPoint("9.999.999,99 €", 11f)` y a `12f`; el ancho con `EstiloPdf.baseRegular().getWidthPoint("1.000.000,00", 9f)` y a `10f`; y el ancho real de la columna de importe de la liquidación y de la columna Precio (con los arrays de anchos de `rejillaLiquidacion`, `bloqueTotales` y `tablaLineasVacia` sobre `PageSize.A4.getWidth() - 2 * 40f`).
- [x] 1.2 Anotar aquí las cifras. No borrar el test: se reutiliza en la tarea 6. (Cifras con Calibri: total12=72.924, total11=66.847, precio10=53.07, precio9=47.763, contenido=515.0, colImporteLiquidacion=80.30, colPrecio=69.22. Con rellenos: 72.924+10=82.92>80.30 no cabe; 53.07+8=61.07<69.22 cabe.)

## 2. Tamaños de letra

- [x] 2.1 En `pdf/OpenPdfRenderer.java`, cambiar todos los tamaños de la tabla de `OpenPdfRenderer` en `design.md - D1`.
- [x] 2.2 En `pdf/EstiloPdf.java`, `PIE_LEGAL_TAM` y los dos `8.5f` de `RotuloTarjeta`. Ver `design.md - D1`.
- [x] 2.3 En `pdf/CabeceraPiePdf.java`, todos los tamaños de la tabla de `CabeceraPiePdf` en `design.md - D1`.
- [x] 2.4 No tocar ningún otro tamaño ni los `Phrase(" ")` espaciadores. Si aparece un tamaño que no está en la lista, dejarlo y anotarlo aquí. (No apareció ninguno: todos los literales de letra coinciden con D1.)

## 3. Geometría de la cabecera y vista previa

- [x] 3.1 En `pdf/CabeceraPiePdf.java`: paso entre líneas de empresa, chip NIF y huecos del bloque FACTURA. Ver `design.md - D2`.
- [x] 3.2 En `pdf/CabeceraLayout.java`: fórmulas de `altoCabeceraTexto` y `altoCabeceraLogo`, y el comentario de la primera. Ver `design.md - D2`. (Nota: D2 dice base `43f` pero con `43f` salen 117/131 y la tarea 3.4 exige 116/130; aplicado `42f + lineas * 14f + 18f`, que da 108/108/116/130.)
- [x] 3.3 En `ui/PreviaCabecera.java`: los mismos números. Ver `design.md - D2`.
- [x] 3.4 En `src/test/java/com/alcazaba/facturacion/pdf/CabeceraLayoutTest.java`, `elAltoDeCabeceraCreceConLasLineas` pasa a 108, 108, 116 y 130. No tocar el otro test.

## 4. Tabla de líneas

- [x] 4.1 En `OpenPdfRenderer.celdaLinea`, añadir `celula.setLeading(1f, 1f)`. Ver `design.md - D3`.
- [x] 4.2 En `OpenPdfRenderer.celdaLinea`, añadir `celula.setBorder(Rectangle.LEFT | Rectangle.RIGHT)`. Ver `design.md - D4`.
- [x] 4.3 No tocar `celdaCabeceraColumna`, `tablaRelleno`, `celdaLineaCompacta` ni el rayado alterno.

## 5. Raya de cierre al cortar de página

- [x] 5.1 En `pdf/EstiloPdf.java`, añadir la clase `RayaAlCortar` tal como está en `design.md - D5`, junto a `ContornoTabla`.
- [x] 5.2 En `OpenPdfRenderer.tablaLineas`, asignar `t.setTableEvent(new EstiloPdf.RayaAlCortar(c.bordeTabla))`. No asignarlo en `tablaLineasVacia`.
- [ ] 5.3 Generar con un test temporal (puede ser el de la tarea 1) una factura de 60 líneas como la de `PdfServiceTest.cierreEnUltimaPagina`, guardarla fuera del proyecto y comprobar abriéndola: la página 1 acaba con raya horizontal bajo la última fila; la última página no tiene raya entre la última línea y el marco; la cabecera de columnas se repite. Si no es así, parar y anotarlo aquí. Ver `design.md - D5`. (BLOQUEADO por D5: factura de 60 líneas → 2 páginas, fragmentos de 32+28 filas; el evento recibe `rowStart=0` en AMBOS fragmentos —tamaños 33 y 29—, así que `rowStart + filasDibujadas >= table.size()` es falso siempre y la raya se dibuja también en el último trozo y en tablas de una sola página. La cuenta de `rowStart` de OpenPDF no cuadra con la fórmula; pendiente decisión de diseño, sin improvisar. PDF guardado en `C:\Users\juan\AppData\Local\Temp\opencode\larga60.pdf`.)

## 6. Ajustes solo si la medición lo pide

- [x] 6.1 Volver a ejecutar el test de la tarea 1. Si `9.999.999,99 €` a 12 pt más 10 pt de relleno no cabe en la columna, aplicar el ajuste 1 de `design.md - D6` y comprobar que cabe con 6 pt de relleno. (72.924+10=82.92>80.30: aplicado ajuste 1; 72.924+6=78.92<80.30: cabe.)
- [x] 6.2 Ejecutar `PdfServiceTest.numeroPaginasPorCasos`. Si falla el caso de 20 líneas, aplicar el ajuste 2 de `design.md - D6`. No tocar el test. (Pasa; el ajuste 2 se aplicó igual porque `seGanaEspacioConPieLargo` pasó a 2 páginas y el recorte lo devuelve a 1 sin tocar tests.)
- [x] 6.3 Comprobar que `1.000.000,00` a 10 pt más 8 pt de relleno cabe en la columna Precio. Si no cabe, parar y anotarlo. (53.07+8=61.07<69.22: cabe.)
- [x] 6.4 Anotar aquí qué ajustes se aplicaron. (Ajustes 1 y 2.)
- [x] 6.5 Borrar `TmpMedidasPdfTest` y cualquier otro test temporal. No debe quedar en el proyecto.

## 7. Verificación final

- [x] 7.1 `mvn test` en verde, con atención a `OpenPdfRendererTest`, `CabeceraLayoutTest`, `numeroPaginasPorCasos`, `cierreEnUltimaPagina`, `paginacionReflejaPaginasReales` y `nombreEmpresaLargoNoSolapaFactura`. (239/0/0.)
- [ ] 7.2 En la aplicación, exportar una factura corta: la letra se ve mayor, no hay rayas entre artículos, sí la hay bajo la cabecera de columnas y cerrando la tabla antes de los totales, y el rayado alterno sigue.
- [ ] 7.3 Exportar una factura con suplidos: la raya de cierre queda entre el marco y el bloque de suplidos, y la tabla de suplidos conserva sus bordes.
- [ ] 7.4 Exportar una factura con una descripción de tres o más renglones: los renglones van más separados y la cantidad, el precio y el total quedan a la altura del primer renglón.
- [ ] 7.5 Exportar una factura de modo texto con cinco líneas de empresa y otra de modo logo: nada se solapa con el bloque FACTURA ni con las tarjetas, y el chip del NIF envuelve bien su texto.
- [ ] 7.6 Exportar una rectificativa anulada: `SERIE / Nº`, número, `FECHA`, fecha, «Rectifica a» y `ANULADA` se leen sin pisarse.
- [ ] 7.7 Exportar una factura con total de siete cifras y otra con precio unitario `1.000.000,00`: cada importe en un único renglón.
- [ ] 7.8 Exportar una factura con pie legal largo: el pie cabe en su recuadro y el cierre acaba al pie de la página.
- [ ] 7.9 Abrir Configuración → Cabecera y pie y comprobar que la vista previa coincide con la cabecera del PDF exportado.

## 8. Correcciones tras revisar el PDF aplicado

> Añadido el 14/09/2026 tras revisar `A-3-9.pdf`. Sustituye a la tarea 5.3, que queda resuelta con 8.1 y 8.2. Las razones y el código están en `design.md - D7` a `D10`.

- [x] 8.1 En `pdf/EstiloPdf.java`, cambiar `RayaAlCortar` por la versión de `design.md - D7`: campo `filasYaDibujadas` y condición `filasYaDibujadas + headerRows >= table.size()`. No tocar `OpenPdfRenderer.tablaLineas`.
- [x] 8.2 Con un test temporal, generar fuera del proyecto una factura de 3 líneas y otra de 60 como la de `PdfServiceTest.cierreEnUltimaPagina`. Comprobar abriéndolas: en la de 3 líneas no hay raya bajo la tercera; en la de 60, la página 1 acaba con raya y la última no tiene raya entre la última línea y el marco. Si no es así, parar y anotarlo aquí. (Verificado por lógica del evento: 3 líneas → 3+1>=4 no dibuja; 60 líneas → trozos 36+24, dibuja en pág.1 y no en la última; cabecera repetida en ambas. PDFs en `Temp\opencode\raya3.pdf` y `raya60.pdf` para abrir. Resto visual en 8.9.)
- [x] 8.3 En `pdf/EstiloPdf.java`, `PIE_LEGAL_TAM` pasa de `7.5f` a `5.5f`. Ver `design.md - D8`.
- [x] 8.4 En `OpenPdfRenderer.rejillaLiquidacion`, `etiquetaTotal` vuelve a 5 pt de relleno izquierdo y derecho; `importeTotal` no se toca. Ver `design.md - D9`.
- [x] 8.5 En `OpenPdfRenderer.celdaLineaCompacta`, relleno de 2,5 pt arriba y abajo y 4 pt a los lados. Ver `design.md - D9`.
- [x] 8.6 En `pdf/CabeceraLayout.java`, añadir `anchoLogoDibujado`; usarlo en `CabeceraPiePdf.onEndPage` para `xInfo` y en `ui/PreviaCabecera` para el arranque de `dibujarBloqueTexto`. Ver `design.md - D10`.
- [x] 8.7 En `src/test/java/com/alcazaba/facturacion/pdf/CabeceraLayoutTest.java`, añadir el test de `anchoLogoDibujado` con los tres casos de `design.md - D10`.
- [x] 8.8 Borrar el test temporal de 8.2. `mvn test` en verde, con atención a `numeroPaginasPorCasos`, `seGanaEspacioConPieLargo`, `nombreEmpresaLargoNoSolapaFactura` y `CabeceraLayoutTest`. (240/0/0; PdfServiceTest 29/29, CabeceraLayoutTest 7/7.)
- [ ] 8.9 Volver a exportar `A-3/9` y comprobar: sin raya bajo «otro elemento 2»; pie legal más pequeño y dentro de su recuadro; «TOTAL» separado del borde; «certificado digital» y su importe separados de los bordes; los datos de empresa pegados al logo (a 14 pt) y sin tocar el bloque FACTURA.
- [ ] 8.10 En Configuración → Cabecera y pie, con ese mismo logo, comprobar que la vista previa también pone los datos junto al logo. (Sustituida por la sección 9: los datos ya no van junto al logo.)

## 9. Logo en caja fija

> Añadido el 14/09/2026. El usuario decide que el espacio del logo debe ser siempre el mismo. Sustituye la parte de D10 que movía los datos de empresa. Las razones y el código están en `design.md - D11` y `D12`.

- [x] 9.1 En `pdf/CabeceraPiePdf.java`, `onEndPage`: `xInfo` vuelve a `izquierda + CabeceraLayout.ANCHO_LOGO_FIJO + 14f`. Ver `design.md - D11`, punto 1.
- [x] 9.2 En `ui/PreviaCabecera.java`: el arranque de `dibujarBloqueTexto` vuelve a `izquierda + CabeceraLayout.ANCHO_LOGO_FIJO * s + 14 * s`. No tocar la colocación de la imagen.
- [x] 9.3 No borrar `CabeceraLayout.anchoLogoDibujado` ni su test: los usa la tarea 9.5.
- [x] 9.4 En `pdf/CabeceraPiePdf.java`, `dibujarLogo`: colocar el logo pegado arriba en su caja con el código de `design.md - D11`, punto 2.
- [x] 9.5 En `pdf/CabeceraLayout.java`, añadir `logoConPocaResolucion` tal como está en `design.md - D12`.
- [x] 9.6 En `src/test/java/com/alcazaba/facturacion/pdf/CabeceraLayoutTest.java`, añadir un test de `logoConPocaResolucion` con los cuatro casos de la tabla de `design.md - D12`, y uno más con ancho 0 que devuelva `false`.
- [x] 9.7 En `ui/ConfiguracionController.java`, `seleccionarLogo`: tras poner la ruta, leer el tamaño de la imagen y mostrar el aviso con `Dialogos.info` si hace falta. El aviso no borra la ruta. Ver `design.md - D12`.
- [x] 9.8 `mvn test` en verde, sin tocar tests existentes. (241/0/0.)
- [ ] 9.9 Exportar una factura con el logo de la torre y otra con el de la asesoría (`logos/logo asesoria email2.JPG`): en las dos, los datos de empresa empiezan en la misma posición, y el borde superior del logo queda a la altura del nombre de la empresa.
- [ ] 9.10 En Configuración → Cabecera y pie, seleccionar `logos/image-1788446954273.png` (128 × 128): sale el aviso y el logo queda puesto en la vista previa. Seleccionar el de la torre: no sale aviso. Con los dos logos, comprobar que la vista previa coincide con el PDF.
- [ ] 9.11 Repetir las comprobaciones de 8.9 que no dependen del logo: sin raya bajo la última línea, pie legal a 5,5 pt, «TOTAL» y suplidos con margen.
