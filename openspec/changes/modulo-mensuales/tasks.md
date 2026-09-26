> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código y los mensajes exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y reescrito, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, sin clases anónimas, sin `record`, **sin clases, `enum` ni `record` dentro de otra clase**, sin `instanceof` con variable, sin `Optional` (salvo el de `showAndWait()`), siempre `import`, lambdas que solo llaman a un método con nombre y Javadoc corto en primera persona del plural. **No tocar** el paquete `pdf`, las tablas, el editor ni el `.root` de los `temas/tema-*.css`. Commits **sin líneas de coautoría**.

## 1. Datos

- [x] 1.1 Nuevo `modelo/dominio/ModoDia.java`. Ver `design.md - D1`.
- [x] 1.2 Nuevo `modelo/dominio/PlantillaMensual.java` con su constructor, sus setters que validan y `getCantidadMeses()`. Ver `design.md - D1`.
- [x] 1.3 `utilidades/Formatos.java`: `nombreMes(int mes)`. Ver `design.md - D1`.

## 2. Negocio

- [x] 2.1 Rehacer `modelo/negocio/FacturacionMensual.java` como singleton con `mesesConFactura` y `generar(PlantillaMensual, boolean)`. Ver `design.md - D2`.
- [x] 2.2 `modelo/Modelo.java` y `controlador/Controlador.java`: las dos operaciones nuevas; fuera el campo `facturacionMensual` y las firmas viejas. Ver `design.md - D2`.
- [x] 2.3 `mvn -q compile` sin errores (el diálogo, con lo justo para compilar hasta la sección 3).

## 3. La ventana

- [x] 3.1 `vista/utilidades/Dialogos.java`: `mostrarDialogoNumerosLibres`. Ver `design.md - D3`.
- [x] 3.2 `vista/recursos/GenerarFacturasMensuales.fxml`: imports explícitos, `lblAnio`, el `ToggleGroup` en el FXML, tres columnas con `onEditCommit` y la casilla `chkAnadirMes`. Ver `design.md - D4`.
- [x] 3.3 Rehacer `vista/controlador/GenerarFacturasMensualesController.java`: `abrir()` con `Vista.crearVentanaModal`, `initialize` como índice, desplegables sin conversor, la tabla con `LineaFactura`, `actualizarInfo()` y `generar()` en pasos. Ver `design.md - D4`.
- [x] 3.4 `vista/recursos/Historico.fxml`: el botón «Mensual» pasa a «Facturar mes», sin *tooltip*. Si `ConfiguracionVentana.GENERAR_MENSUAL` se queda sin uso, se borra. Ver `design.md - D4`.
- [x] 3.5 `mvn -q compile` sin errores.
- [ ] 3.6 `TextosCompletosTest.comprobarTextos` detecta también el texto con «…» (`design.md - D6`). Comprobar que, con el botón todavía en «Facturar mes», `TextosCompletosTest` falla en el histórico, y apuntarlo aquí.
- [ ] 3.7 `Historico.fxml`: el botón pasa a «Fact. mes» con el tooltip «Facturar mes». `TextosCompletosTest` vuelve a pasar. Ver `design.md - D6`.
- [ ] 3.8 `base.css`: los dos `#F6F6F6` de `.panel-neutro` pasan a `-fx-control-inner-background`, y nada más (comprobar con `git diff` que no cambia ninguna otra línea ni el `.root` de ningún tema). Ver `design.md - D6`.

## 4. Tests

- [x] 4.1 Nuevo `PlantillaMensualTest` con los casos de `design.md - D5`.
- [x] 4.2 Rehacer `FacturacionMensualTest` con los casos de `design.md - D5`.
- [x] 4.3 `PantallaMensualesTest`: los cuatro de hoy adaptados y los cuatro nuevos de `design.md - D5`. `TextosCompletosTest` y `PantallaHistoricoTest` siguen pasando con el texto nuevo del botón.

## 5. Repaso

- [x] 5.1 `grep -rn "LineaDialogo\|LineaPlantilla\|FacturacionMensual.Resultado\|FacturacionMensual.ModoDia\|detectarDuplicados\|spinnerAnio\|colAnadirMes" src/`: sin resultados (el único acierto es la comprobación de `PantallaMensualesTest` de que `#spinnerAnio` ya no existe).
- [x] 5.2 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: |new [A-Za-z<>]+\([^)]*\) *\{|instanceof [A-Za-z<>?]+ [a-z]|javafx\.[a-z]+\.[A-Z]|java\.[a-z]+\.[a-z]+\.[A-Z]|new Thread|Task<|StringBuilder|static class|enum [A-Z]"` sin contar las líneas `import` (en `ModoDia.java` el `enum` es el propio fichero): nada nuevo (los dos ternarios de `Formatos.java` son de antes de este change).
- [x] 5.3 `GenerarFacturasMensualesController.java`: 389 líneas. `FacturacionMensual.java`: 112 líneas.
- [x] 5.4 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde, sin `ClassCastException`. 451 pruebas, 17:34 min.
- [x] 5.5 `openspec validate modulo-mensuales --strict` sin errores.

## 6. Estado

- [x] 6.1 Añadir este change a «En curso» en `ESTADO.md`.
- [x] 6.2 En «Qué toca ahora» de `ESTADO.md`, quitar `modulo-pdf` (no se hace: el PDF irá con JasperReports) y dejar el orden: `modulo-mensuales` → `modulo-copias` → `documentacion-final` → rama `pdf-jasper` (plan en `borrador_changes/plan-pdf-jasper.md`).

## 7. Pruebas manuales

> La base de datos no cambia: no hace falta borrar `%APPDATA%\Facturacion`.

- [ ] 7.1 Abrir «Facturar mes» desde el menú y desde el histórico (el botón dice ahora «Facturar mes»): se abre el diálogo con el tema de la empresa.
- [ ] 7.2 El diálogo enseña el año de trabajo y no deja cambiarlo.
- [ ] 7.3 Dos líneas («cuota» y «gestoría») con «Añadir mes» marcada, de enero a marzo: salen tres facturas y las dos líneas de cada una terminan en «- mes de enero», «febrero» y «marzo». Sin la casilla, las descripciones no cambian.
- [ ] 7.4 Día fijo 31: la factura de febrero lleva el último día de febrero. Primer día y último día del mes: las fechas cuadran.
- [ ] 7.5 IVA del 21 % y retención del 15 %: los totales de las facturas generadas cuadran.
- [ ] 7.6 Generar para un cliente que ya tiene factura en algún mes: avisa con los meses. Cancelar no genera nada; Aceptar genera todos.
- [ ] 7.7 Borrar una factura que no sea la última de la serie y generar: el aviso ofrece «Usar los números libres» y «Continuar sin ellos», y cada botón hace lo que dice.
- [ ] 7.8 Escribir una cantidad o un precio que no valen en la tabla de líneas: la línea se queda como estaba.
- [ ] 7.9 Cancelar cierra sin generar nada, y el diálogo se ve bien.
- [ ] 7.10 Tras la 3.7: el botón del histórico dice «Fact. mes» completo, también con omarchy, y al pasar el ratón sale «Facturar mes».
- [ ] 7.11 Tras la 3.8: con omarchy (y neon o negro-dorado), la ventana de facturar mes es oscura y todos sus textos se leen. Con biblioteca8 se ve como antes.
