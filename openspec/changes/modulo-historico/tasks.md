> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código y los mensajes exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y reescrito, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, sin clases anónimas, sin `record`, sin `instanceof` con variable, sin `Optional` (salvo el de `showAndWait()`), siempre `import`, lambdas que solo llaman a un método con nombre y Javadoc corto en primera persona del plural. **No tocar** el paquete `pdf`, las tablas, el editor ni el `.root` de los `temas/tema-*.css`. Commits **sin líneas de coautoría**.

## 1. Datos y negocio

- [x] 1.1 Rehacer `modelo/dominio/FiltrosHistorial.java` con el constructor, los setters que validan y `errorFechas`/`errorImportes`. Ver `design.md - D2`.
- [x] 1.2 `modelo/negocio/Facturas.java`: `listado(null)` devuelve todas, filtro de serie por `serie_id`, y el mensaje nuevo de la factura con rectificativa. Ver `design.md - D2` y `D3`.
- [x] 1.3 `mvn -q compile` sin errores (el histórico, con lo justo para compilar hasta la sección 3).

## 2. Utilidades de la vista

- [x] 2.1 Nuevo `vista/utilidades/ExportacionVarias.java` y `Dialogos.mostrarDialogoExportarVarias`. Ver `design.md - D4`.

## 3. La pantalla

- [x] 3.1 `vista/recursos/Historico.fxml`: imports explícitos, `onMouseClicked`, el texto de la tabla vacía, el menú del clic derecho y `#eliminarSeleccionadas`. Ver `design.md - D1`.
- [x] 3.2 Rehacer `vista/controlador/HistoricoController.java`: `initialize` como índice, columnas con `PropertyValueFactory`, `pulsarTabla`, el año de trabajo al entrar y el atajo. Ver `design.md - D1`.
- [x] 3.3 Los combos de serie y estado con su lista al lado, y `buscar()` con las marcas en rojo y un solo aviso. Ver `design.md - D2`.
- [x] 3.4 Anular y eliminar con la lista de facturas, los textos y el resumen con números. Ver `design.md - D3`.
- [x] 3.5 Exportar una, varias por separado y varias en un PDF, sin `Task` ni `Thread` y con cursor de espera. Ver `design.md - D4`.
- [x] 3.6 `mvn -q compile` sin errores.

## 4. Tests

- [x] 4.1 Nuevo `FiltrosHistorialTest` con los casos de `design.md - D5`.
- [x] 4.2 `FacturasTest`: los casos de `design.md - D5`. `SeriesTest` y `FacturacionMensualTest`: `listado(null)`.
- [x] 4.3 `PantallaHistoricoTest`: `quitarFechas()`, los seis casos nuevos de `design.md - D5` y los de hoy adaptados.

## 5. Repaso

- [x] 5.1 `grep -rn "serieCodigo\|new FiltrosHistorial()\|borrarSeleccionadas\|parseMonedaOpcional" src/`: nada en el histórico ni en sus tests. Se han borrado `parseMoneda` y `parseMonedaOpcional` de `Formatos`, que ya no usaba nadie, junto con su caso de test en `ValidadorDocumentoFiscalTest`.
- [x] 5.2 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: |new [A-Za-z<>]+\([^)]*\) *\{|instanceof [A-Za-z<>?]+ [a-z]|javafx\.[a-z]+\.[A-Z]|new Thread|Task<|ChoiceDialog|StringBuilder|int\[\]"`: solo salen falsos positivos (imports `javafx.*`, `FutureTask` de los tests, y el `StringBuilder` del SQL de `Facturas.listado`, que ya estaba antes de este change y no lo toca `design.md`); nada nuevo prohibido.
- [x] 5.3 Apuntar aquí cuántas líneas tiene `HistoricoController.java` (`grep -c ""`): 554 líneas.
- [x] 5.4 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde y ningún `ClassCastException` en el log. Apuntar aquí cuántas pruebas son y cuánto tarda: 436 pruebas, 6:46 min, `BUILD SUCCESS`, sin `ClassCastException`.
- [x] 5.5 `openspec validate modulo-historico --strict` sin errores.

## 6. Estado

- [x] 6.1 Añadir este change a «En curso» en `ESTADO.md`.

## 7. Pruebas manuales

> La base de datos no cambia: no hace falta borrar `%APPDATA%\Facturacion`.

- [ ] 7.1 Entrar en el histórico: las fechas vienen puestas en el año de trabajo y la tabla ya enseña sus facturas.
- [ ] 7.2 Escribir «12,5x» en «Importe desde» y Buscar: el campo sale en rojo y el aviso dice que no es un importe válido. Poner la fecha desde después de la hasta: las dos en rojo y su aviso.
- [ ] 7.3 Filtrar por serie, por estado y por cliente: los resultados cuadran.
- [ ] 7.4 Doble clic en una fila: abre la factura en el editor.
- [ ] 7.5 Clic derecho sobre la tabla: el menú dice «Exportar a PDF», «Anular facturas seleccionadas» y «Eliminar facturas seleccionadas».
- [ ] 7.6 Anular dos facturas, una ya anulada: el resumen dice 1 anulada y 1 ya anulada. Eliminar la A-1/9: el resumen dice «A-1/9: Tiene la rectificativa R-1 y no se puede eliminar.».
- [ ] 7.7 Exportar una factura: pide dónde guardarla y el PDF se ve bien.
- [ ] 7.8 Exportar tres facturas «Un PDF por factura»: pide la carpeta y salen tres PDF con sus nombres. Exportarlas «Todas en un PDF»: sale un único PDF con las tres. Mientras se generan, cursor de espera.
- [ ] 7.9 Ctrl+F busca, y el histórico se ve bien a 1024×768.
