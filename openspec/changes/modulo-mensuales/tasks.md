> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código y los mensajes exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y reescrito, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, sin clases anónimas, sin `record`, **sin clases, `enum` ni `record` dentro de otra clase**, sin `instanceof` con variable, sin `Optional` (salvo el de `showAndWait()`), siempre `import`, lambdas que solo llaman a un método con nombre y Javadoc corto en primera persona del plural. **No tocar** el paquete `pdf`, las tablas, el editor ni el `.root` de los `temas/tema-*.css`. Commits **sin líneas de coautoría**.

## 1. Datos

- [ ] 1.1 Nuevo `modelo/dominio/ModoDia.java`. Ver `design.md - D1`.
- [ ] 1.2 Nuevo `modelo/dominio/PlantillaMensual.java` con su constructor, sus setters que validan y `getCantidadMeses()`. Ver `design.md - D1`.
- [ ] 1.3 `utilidades/Formatos.java`: `nombreMes(int mes)`. Ver `design.md - D1`.

## 2. Negocio

- [ ] 2.1 Rehacer `modelo/negocio/FacturacionMensual.java` como singleton con `mesesConFactura` y `generar(PlantillaMensual, boolean)`. Ver `design.md - D2`.
- [ ] 2.2 `modelo/Modelo.java` y `controlador/Controlador.java`: las dos operaciones nuevas; fuera el campo `facturacionMensual` y las firmas viejas. Ver `design.md - D2`.
- [ ] 2.3 `mvn -q compile` sin errores (el diálogo, con lo justo para compilar hasta la sección 3).

## 3. La ventana

- [ ] 3.1 `vista/utilidades/Dialogos.java`: `mostrarDialogoNumerosLibres`. Ver `design.md - D3`.
- [ ] 3.2 `vista/recursos/GenerarFacturasMensuales.fxml`: imports explícitos, `lblAnio`, el `ToggleGroup` en el FXML, tres columnas con `onEditCommit` y la casilla `chkAnadirMes`. Ver `design.md - D4`.
- [ ] 3.3 Rehacer `vista/controlador/GenerarFacturasMensualesController.java`: `abrir()` con `Vista.crearVentanaModal`, `initialize` como índice, desplegables sin conversor, la tabla con `LineaFactura`, `actualizarInfo()` y `generar()` en pasos. Ver `design.md - D4`.
- [ ] 3.4 `vista/recursos/Historico.fxml`: el botón «Mensual» pasa a «Facturar mes», sin *tooltip*. Si `ConfiguracionVentana.GENERAR_MENSUAL` se queda sin uso, se borra. Ver `design.md - D4`.
- [ ] 3.5 `mvn -q compile` sin errores.

## 4. Tests

- [ ] 4.1 Nuevo `PlantillaMensualTest` con los casos de `design.md - D5`.
- [ ] 4.2 Rehacer `FacturacionMensualTest` con los casos de `design.md - D5`.
- [ ] 4.3 `PantallaMensualesTest`: los cuatro de hoy adaptados y los cuatro nuevos de `design.md - D5`. `TextosCompletosTest` y `PantallaHistoricoTest` siguen pasando con el texto nuevo del botón.

## 5. Repaso

- [ ] 5.1 `grep -rn "LineaDialogo\|LineaPlantilla\|FacturacionMensual.Resultado\|FacturacionMensual.ModoDia\|detectarDuplicados\|spinnerAnio\|colAnadirMes" src/`: sin resultados.
- [ ] 5.2 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: |new [A-Za-z<>]+\([^)]*\) *\{|instanceof [A-Za-z<>?]+ [a-z]|javafx\.[a-z]+\.[A-Z]|java\.[a-z]+\.[a-z]+\.[A-Z]|new Thread|Task<|StringBuilder|static class|enum [A-Z]"` sin contar las líneas `import` (en `ModoDia.java` el `enum` es el propio fichero): nada.
- [ ] 5.3 Apuntar aquí cuántas líneas tienen `GenerarFacturasMensualesController.java` y `FacturacionMensual.java` (`grep -c ""`).
- [ ] 5.4 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde y ningún `ClassCastException` en el log. Apuntar aquí cuántas pruebas son y cuánto tarda.
- [ ] 5.5 `openspec validate modulo-mensuales --strict` sin errores.

## 6. Estado

- [ ] 6.1 Añadir este change a «En curso» en `ESTADO.md`.
- [ ] 6.2 En «Qué toca ahora» de `ESTADO.md`, quitar `modulo-pdf` (no se hace: el PDF irá con JasperReports) y dejar el orden: `modulo-mensuales` → `modulo-copias` → `documentacion-final` → rama `pdf-jasper` (plan en `borrador_changes/plan-pdf-jasper.md`).

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
