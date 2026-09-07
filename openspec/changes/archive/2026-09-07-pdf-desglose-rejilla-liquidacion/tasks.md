> La geometría, los colores y los rótulos exactos están en `design.md - D3` y `design.md - D6`.
> La maqueta que hay que reproducir es `prototipos/pdf-totales-rejilla-hermanas.html`, con sus tres casos.

## 1. Contenedor del bloque

- [x] 1.1 En `src/main/java/com/alcazaba/facturacion/pdf/PdfService.java`, reescribir `bloqueTotales(ResumenFactura, int, Colores)` manteniendo la firma y la llamada de `PdfService.java:128`.
- [x] 1.2 Construir el contenedor: `PdfPTable` de dos columnas `{3.3f, 3.0f}` al 100 % del ancho, celdas sin borde y alineadas arriba, con `setPaddingRight(7f)` en la izquierda y `setPaddingLeft(7f)` en la derecha.
- [x] 1.3 Dejar el `setSpacingBefore` que hoy tiene el bloque, para no alterar la separación con la tabla de suplidos.

## 2. Rejilla de IVA (celda izquierda)

- [x] 2.1 `PdfPTable` de tres columnas `{1.0f, 2.0f, 1.7f}` al 100 %, con cabecera `TIPO` / `BASE IMPONIBLE` / `CUOTA IVA` sobre fondo `c.base`, texto blanco en negrita de 7 pt y `Chunk.setCharacterSpacing(0.5f)`.
- [x] 2.2 Una fila por elemento de `r.getGrupos()`: en `TIPO`, el porcentaje formateado como número español con dos decimales, o `Exento` si `isExento()`; en `BASE IMPONIBLE`, `grupo.getBase()`; en `CUOTA IVA`, `grupo.getCuota()`, o `—` si el grupo es exento.
- [x] 2.3 Fila final `Totales` con `r.getBaseTotal()` y `r.getIvaTotal()`, en negrita, fondo `c.claro` y borde superior `c.bordeTabla`.
- [x] 2.4 Cuerpo sobre `c.clarisimo`, texto de 8.5 pt en `TINTA`, padding 3.5 pt, cifras a la derecha y columna de tipo centrada.

## 3. Nota del descuento

- [x] 3.1 Añadir la nota sólo cuando `r.getImporteDescuento()` sea mayor que cero, bajo la rejilla de IVA, a 7 pt en `c.oscuro` y sin borde.
- [x] 3.2 Redactarla con el porcentaje recibido, `r.getImporteDescuento()` y `r.getBaseBruta()`, según el texto de `design.md - D2`.
- [x] 3.3 Si algún grupo exento tiene motivo, añadir una sola frase con ese motivo al final de la nota.

## 4. Rejilla de liquidación (celda derecha)

- [x] 4.1 `PdfPTable` de dos columnas `{2.2f, 1.4f}` al 100 %, con una única celda de cabecera `LIQUIDACIÓN` con `setColspan(2)` y el mismo estilo de banda que la rejilla de IVA.
- [x] 4.2 Filas fijas `Base imponible` (`r.getBaseTotal()`) y `Total IVA repercutido` (`r.getIvaTotal()`), siempre presentes.
- [x] 4.3 Fila de retención sólo si `r.getImporteRetencion()` es mayor que cero, en cursiva `ROJO_DESCUENTO`, con el rótulo de `design.md - D6` y el importe precedido de signo menos.
- [x] 4.4 Fila de suplidos sólo si `r.getTotalSuplidos()` es mayor que cero, con el importe precedido de `+`.
- [x] 4.5 Banda `TOTAL` como última fila: fondo `c.base`, rótulo blanco en negrita de 8 pt con `setCharacterSpacing(0.7f)` y el importe de `r.getTotal()` en negrita de 13 pt a la derecha.

## 5. Limpieza

- [x] 5.1 Eliminar `nombreBaseGrupo(...)` (`PdfService.java:588`) y `nombreBaseImponibleGrupo(...)` (`PdfService.java:602`), que ya no tienen llamantes.
- [x] 5.2 Eliminar `filaDescuento(...)` (`PdfService.java:607`) y `filaResumen(...)` (`PdfService.java:620`) si tras el cambio no los usa nadie más; comprobarlo antes de borrar.
- [x] 5.3 Actualizar el javadoc de cabecera de la clase (`PdfService.java:49`), que describe el resumen con «fila TOTAL».
- [x] 5.4 Comprobar que no queda ningún uso de `GRIS` ni de `ROJO_DESCUENTO` sin llamante.

## 6. Tests

- [x] 6.1 `exportaDisenoAprobadoConTotalConIvaYTarjetas`: sustituir `assertTrue(texto.contains("Base imponible"))` y `assertFalse(texto.contains("Subtotal"))` por las comprobaciones de la rejilla nueva.
- [x] 6.2 `totalesConDescuentoSeMuestranRestandoYCuadran`: reescribir sobre el escenario «Descuento como nota bajo la rejilla»; conservar la comprobación de que las cifras cuadran.
- [x] 6.3 `desgloseConVariosTiposYDescuentoMuestraBasePorTipo`: reescribir sobre el escenario «Varios tipos de IVA con un solo descuento», comprobando la fila `Totales`.
- [x] 6.4 `retencionApareceComoFilaPropiaEnElPdf` y `suplidosAparecenEntreRetencionYTotal`: adaptar el orden esperado al de la rejilla de liquidación.
- [x] 6.5 Añadir un test que compruebe que el PDF no contiene `Subtotal`, `Base imponible 21%` ni `Base exenta` en una factura con tres tipos y descuento.
- [x] 6.6 Añadir un test del caso exento: fila con `Exento` y guion en la cuota, y su base sumada en `Totales`.
- [x] 6.7 Comprobar que los tests de `CalculoServiceTest` siguen en verde sin tocarlos: los importes no cambian.

## 7. Verificación final

- [x] 7.1 Suite completa en verde con `mvn test`.
- [x] 7.2 Exportar a mano las tres facturas de la maqueta y comparar con `prototipos/pdf-totales-rejilla-hermanas.html`: caso completo, caso mínimo y caso intermedio. *(Verificado por el usuario el 2026-09-07.)*
- [x] 7.3 ~~Comprobar que `BASE IMPONIBLE` y `Total IVA repercutido` no se parten de línea con el acento por defecto y con Calibri ausente del sistema.~~ *(No aplicable: Calibri viene instalada de serie en Windows desde Vista dentro de la colección ClearType, así que en Windows 7, 10 y 11 está en `C:\Windows\Fonts`, que es donde la busca `cargarFuenteSistema`. El fallback de `baseRegular()` a Helvetica no se ejercita en la práctica. La tipografía no cambia.)*
- [x] 7.4 Exportar una factura larga y confirmar que el bloque no se queda solo en una página. *(Verificado por el usuario el 2026-09-07 con una factura de 34 líneas: 2 páginas y totales no solos.)*
- [x] 7.5 Comprobar que el pie del editor sigue igual que antes del change. *(Verificado por el usuario el 2026-09-07.)*

## 8. Correcciones de la revisión

- [x] 8.1 Cerrar por abajo las dos rejillas: la fila `Totales` de la rejilla de IVA y las dos celdas de la banda `TOTAL` llevan `BOTTOM`.
- [x] 8.2 El símbolo `€` aparece solo en la banda `TOTAL`. Las dos rejillas muestran el número sin símbolo; `importeSinSimbolo` se renombra a `importeRejilla` y se usa en todas esas celdas.
- [x] 8.3 La banda `TOTAL` pasa a rótulo e importe en negrita a 12 pt, con el mismo padding, centrados verticalmente y sin `setCharacterSpacing`.
- [x] 8.4 La nota del descuento fija el leading en 8,5 pt para que las dos líneas queden pegadas y sin tocar el borde derecho de su celda.
- [x] 8.5 La banda `TOTAL` elimina el trazo vertical interior entre etiqueta e importe: cada celda solo lleva el contorno exterior.
- [x] 8.6 La tabla `SUPLIDOS` superior usa variantes compactas (`celdaCabeceraColumnaCompacta`, `celdaLineaCompacta`) con cuerpo 8 pt y padding 2,5. No se tocan `celdaLinea` ni `celdaCabeceraColumna`; el contenido, el orden y la nota legal no cambian.
- [x] 8.7 El test `elPdfNoUsaRótulosDeLaEscalera` se renombra a `elPdfNoUsaRotulosDeLaEscalera` (ASCII).
- [x] 8.8 Retirada la tarea 7.3 (fallback a Helvetica) por la presencia garantizada de Calibri en Windows; motivo anotado en la propia tarea.
- [x] 8.9 El símbolo `€` aparece una sola vez en todo el PDF, en el importe de la banda `TOTAL`. Tabla de líneas, tabla SUPLIDOS y las dos rejillas usan `importePdf` (antes `importeRejilla`); `Formatos.moneda` queda solo en la banda. Delta de spec actualizado (exportación a PDF y orden del desglose) y test de aparición única añadido.
- [x] 8.10 La banda `TOTAL` baja a 11 pt (rótulo e importe), manteniendo negrita, mismo padding y centrado vertical. `design.md - D3` actualizado.
