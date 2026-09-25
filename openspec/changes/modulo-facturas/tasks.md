> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código y los mensajes exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, **sin clases, `record` ni `enum` dentro de otra clase**, sin clases anónimas, sin `Optional` (salvo el de `showAndWait()`), siempre `import` y Javadoc corto en primera persona del plural. En las pantallas **solo plumbing**: lo que pida este change y lo justo para que compilen y sigan funcionando; se rehacen en sus módulos. **No tocar** las tablas maestras, `preferencias`, los `record` del paquete `pdf` ni de `CopiaSeguridad`, ni el `.root` de los `temas/tema-*.css`. Commits **sin líneas de coautoría**.

## 1. Base de datos

- [x] 1.1 `src/main/resources/db/crear_tablas.sql`: fuera `factura_version` y su índice; `factura` y `factura_linea` como en `design.md - D1`.
- [x] 1.2 `src/main/resources/db/seed_demo.sql`: la misma demostración en una sola tabla (A-1 a A-5 con la A-5 anulada, y la R-1 con `rectifica_id` de la A-1), con `anio` y las líneas por `factura_id` sin sus totales. Ver `design.md - D1`.

## 2. Clases de datos

- [x] 2.1 Rehacer `modelo/dominio/Factura.java` con los campos, el constructor, `getAnio()`, el constructor copia, `equals`/`hashCode`/`toString` y los getters de texto de `design.md - D2`.
- [x] 2.2 `modelo/dominio/LineaFactura.java`: fuera `totalBase` e `ivaImporte` con sus setters; `getTotalBase()` calculado; `copia()` pasa a constructor copia. Ver `design.md - D2`.
- [x] 2.3 Borrar `VersionFactura`, `DatosPago`, `FilaHistorial` y `ModoGuardarVersion`.

## 3. Negocio

- [x] 3.1 Rehacer `modelo/negocio/Facturas.java` como singleton con las operaciones de `design.md - D3`, sus transacciones en el propio método y sus mensajes. Sin `Clock`.
- [x] 3.2 `modelo/negocio/Series.java`: `correlativosUsados` de una sola tabla y con `anio = ?`, fuera `correlativosActivos`, `correlativoOcupado` sobre los usados, y el nuevo `rectificativa()`. Ver `design.md - D4`.
- [x] 3.3 `modelo/negocio/Calculos.java`: fuera `cuotasSinDescuento`, `ClaveIva` y `ResultadoConIva`; nuevo `baseDesdeTotalConIva`. Ver `design.md - D5`.
- [x] 3.4 `modelo/negocio/FacturacionMensual.java`: construye objetos `Factura`, llama a `Facturas.altaVarias` y `detectarDuplicados` usa `clienteTieneFacturaEnMes`. Sin DAO. Ver `design.md - D7`.
- [x] 3.5 Borrar `Versiones`, `Estados`, `Rectificativas`, `Historial`, `FacturaDAO`, `VersionFacturaDAO`, `LineaFacturaDAO`, `HistorialDAO` y `ValidacionException`. Donde se lanzaba `ValidacionException`, `Exception` con el mismo mensaje.

## 4. Controlador y Modelo

- [x] 4.1 Las once operaciones de `design.md - D6` en `modelo/Modelo.java` y `controlador/Controlador.java`, de una línea cada una. `Modelo` pierde los getters de facturas y los DAO y el `Clock` que ya no hagan falta.

## 5. Pantallas (solo plumbing)

- [x] 5.1 `vista/controlador/EditorController.java`: todo lo de `design.md - D7` (abrir, `txtNumero` desactivado con factura abierta, referencia no editable, guardar nueva, pregunta antes de sobrescribir una emitida, fuera versiones y «(vN)», las líneas sin `setTotalBase`/`setIvaImporte`, y las operaciones por el controlador).
- [x] 5.2 `vista/controlador/HistoricoController.java` y `vista/recursos/Historico.fxml`: la tabla con objetos `Factura`, fuera la columna «Versión», anular y borrar por el controlador. Ver `design.md - D7`.
- [x] 5.3 `vista/controlador/GenerarFacturasMensualesController.java` por las operaciones del controlador.
- [x] 5.4 `pdf/ConstructorDocumentoFactura.java` y `pdf/ExportadorPdf.java` reciben `Factura`. Ver `design.md - D7`.
- [x] 5.5 `CopiaSeguridadDAO` y `CopiaSeguridad` con el esquema nuevo. Ver `design.md - D7`.
- [x] 5.6 Borrar `vista/controlador/VersionesController.java`, `vista/recursos/Versiones.fxml` y `Dialogos.mostrarDialogoModoGuardarVersion`.
- [x] 5.7 `mvn -q compile` sin errores.

## 6. Tests

- [x] 6.1 `FacturasTest` con los casos de `design.md - D8`; borrar `EstadosTest` e `HistorialTest` (sus casos pasan a `FacturasTest`).
- [x] 6.2 `SeriesTest` y `CalculosTest`: los casos de `design.md - D8`.
- [x] 6.3 `PantallaHistoricoTest` y `PantallaEditorTest`: los casos de `design.md - D8`. `CargaPantallasTest` y `TextosCompletosTest` sin `Versiones.fxml`.
- [x] 6.4 Adaptar los del PDF, las copias, las mensuales, la demostración y cualquier otro que use lo que desaparece.

## 7. Repaso

- [x] 7.1 `grep -rn "VersionFactura\|factura_version\|ValidacionException\|DatosPago\|FilaHistorial\|getVersiones\|getEstados\|getRectificativas\|getHistorial\|ModoGuardarVersion" src/`: sin resultados.
- [x] 7.2 `grep -rn "getModelo()" src/main/java/cabofactu/vista`: ninguna llamada a facturas.
- [x] 7.3 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: "`: nada nuevo, salvo lo que ya había en las líneas del editor que no se reescriben.
- [x] 7.4 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde y ningún `ClassCastException` en el log. Apuntar aquí cuántas pruebas son y cuánto tarda.
- [x] 7.5 `openspec validate modulo-facturas --strict` sin errores.

7.4: **403 pruebas, 4:52 con `mvn test`** (aplicación cerrada, `target` borrado antes).

## 8. Estado

- [x] 8.1 Añadir este change a «En curso» en `ESTADO.md`.

## 9. Pruebas manuales

> Antes de empezar, cierra la aplicación y **borra la carpeta `%APPDATA%\Facturacion`**: cambia el esquema.

- [x] 9.1 Nueva factura con dos líneas, una escribiendo el precio y otra con «total con IVA» marcado escribiendo el total: los totales cuadran y, al reabrirla, las líneas y los totales siguen igual.
- [x] 9.2 Abrir una factura emitida, cambiar algo y Guardar: **pregunta** si sobrescribirla. Aceptar: en el histórico sigue habiendo **una sola** fila de esa factura, con el cambio.
- [x] 9.3 En una factura de la serie A, cambiar la fecha a otro mes y guardar: el número cambia de mes y conserva el correlativo. Cambiarla a otro año y guardar: avisa de que no puede cambiar de año y no guarda.
- [x] 9.4 Con una factura abierta, el campo del número está desactivado.
- [x] 9.5 Anular una factura desde el histórico: sigue siendo una fila, ahora anulada. Restaurarla: vuelve a emitida con su número.
- [x] 9.6 Rectificar la A-2: se crea la rectificativa en la serie R con la referencia A-2/9, que no se puede editar.
- [x] 9.7 Borrar la A-1 desde el histórico: avisa de que la rectifica la R-1 y no la borra.
- [x] 9.8 Exportar a PDF una factura normal y una rectificativa: salen bien.
- [x] 9.9 Facturar mes para un cliente, tres meses: salen tres facturas con números seguidos.
- [x] 9.10 Copia de seguridad: crear una y restaurarla como empresa nueva; en ella, el histórico enseña las mismas facturas.
