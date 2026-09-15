> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. Mensajes, títulos y firmas exactos en `design.md`. Seguir `AGENTS.md` en todo el código nuevo o modificado: sin ternarios, sin `var`, sin streams ni `::`, sin lambdas guardadas en variables ni `boolean[]`, siempre `import`, Javadoc corto en clases y métodos públicos nuevos (primera persona del plural). No arreglar de paso construcciones antiguas fuera de las líneas tocadas.

## 1. Validador del NIF

- [ ] 1.1 En `utilidades/ValidadorDocumentoFiscal.java`, añadir `formatoCorrecto(String)` y `letraCorrecta(String)` y hacer que `esValido(String)` los use sin cambiar su resultado. Ver `design.md - D1`.

## 2. Regla del cliente en el negocio

- [ ] 2.1 Crear `modelo/negocio/ValidacionCliente.java` con los métodos y mensajes exactos de `design.md - D2`.
- [ ] 2.2 En `modelo/negocio/Clientes.java`, `insertar` y `actualizar` llaman primero a `ValidacionCliente.comprobar` y declaran `throws ValidacionException`. Ver `design.md - D3`.
- [ ] 2.3 En `modelo/negocio/Facturas.java`, `ValidacionCliente.comprobar(cliente)` como primera instrucción de `crearFacturaSinTransaccion` y de la sobrecarga de `guardarEditada` que hace el trabajo. Ver `design.md - D4`.
- [ ] 2.4 En `modelo/negocio/FacturacionMensual.java`, añadir `ValidacionCliente.comprobar(cliente)` en `validar`, tras la comprobación de cliente existente. Ver `design.md - D5`.
- [ ] 2.5 Adaptar las llamadas que ya no compilen por el nuevo `throws ValidacionException`.
- [ ] 2.6 `mvn -q compile` sin errores.

## 3. Ficha de cliente

- [ ] 3.1 En `vista/controlador/ClientesController.java`, poner asterisco en las etiquetas obligatorias y pasar los `TextField` de la ficha a campos privados, conservando sus `setId`. Ver `design.md - D6`.
- [ ] 3.2 Quitar de la ficha los `BooleanSupplier`, los `boolean[] avisando…`, los `Runnable avisar…Invalido`, los `Platform.runLater(…requestFocus)` y los tres `addEventFilter` de Guardar.
- [ ] 3.3 Añadir `marcarCampo`, `marcarCamposFicha`, `avisarPrimerErrorFicha` y el único filtro de Guardar, con el comportamiento de `design.md - D6` (rojo al salir sin aviso; aviso con Enter en NIF, CP y email; un único aviso al Guardar).
- [ ] 3.4 En `nuevo()` y `editar()`, añadir `catch (ValidacionException e)` con `Dialogos.error("Datos del cliente", e.getMessage())` antes del `catch (Exception e)`.

## 4. Editor

- [ ] 4.1 En `src/main/resources/cabofactu/vista/recursos/Editor.fxml`, asterisco en `Nombre*`, `NIF*`, `Dirección*`, `CP*`, `Localidad*` y `Provincia*`. Ver `design.md - D7`.
- [ ] 4.2 En `vista/controlador/EditorController.java`, quitar `validarNifCliente`, el campo `corrigiendoNif` y su `runLater`; añadir `marcarCampo`, `marcarCamposCliente` y `avisarPrimerErrorCliente`, con rojo al salir sin aviso y aviso con Enter en `cliNif`. Ver `design.md - D7`.
- [ ] 4.3 En `guardar()`, sustituir la validación del NIF y del nombre por los pasos 1 a 3 de `design.md - D7`.
- [ ] 4.4 `mvn -q compile` sin errores.

## 5. Datos de demostración

- [ ] 5.1 En `src/main/resources/db/seed_demo.sql`, cambiar las tres apariciones de `B77777777` por `B77777779`. Ver `design.md - D8`.

## 6. Tests

- [ ] 6.1 Añadir a `src/test/java/cabofactu/utilidades/ValidadorDocumentoFiscalTest.java` los casos de `formatoCorrecto` y `letraCorrecta` de `design.md - D1`.
- [ ] 6.2 Crear `src/test/java/cabofactu/modelo/negocio/ValidacionClienteTest.java` con los casos de `design.md - D8`.
- [ ] 6.3 Adaptar `FacturasTest`, `FacturacionMensualTest`, `EstadosTest`, `HistorialTest`, `CopiaSeguridadTest` y `EditorIvaInactivoTest` según la tabla de `design.md - D8`, y añadir los tests nuevos de `FacturasTest` y `FacturacionMensualTest`.
- [ ] 6.4 Adaptar `ClientesValidacionNifTest` y `EditorValidacionNifTest` según `design.md - D8` (sin aviso al salir, un aviso al guardar con su mensaje exacto, caso de NIF vacío). El ayudante falso de `Dialogos` guarda el último mensaje de error.
- [ ] 6.5 No cambiar el NIF de los tests que lo usan no válido a propósito: `ValidadorDocumentoFiscalTest`, `ClientesValidacionNifTest`, `EditorValidacionNifTest` y `ConfiguracionTest.nifNoValidoDevuelveNif`.

## 7. Comprobaciones

- [ ] 7.1 `git grep -n "Revise el DNI, NIE o NIF/CIF introducido" -- src` no devuelve nada.
- [ ] 7.2 `git grep -nE "avisandoNif|avisandoCp|avisandoEmail|corrigiendoNif|validarNifCliente" -- src` no devuelve nada.
- [ ] 7.3 `git grep -n "runLater" -- src/main/java/cabofactu/vista/controlador/ClientesController.java` no devuelve nada, y en `EditorController.java` ya no aparece `cliNif::requestFocus`.
- [ ] 7.4 `git grep -n "B77777777" -- src` no devuelve nada.
- [ ] 7.5 En los ficheros tocados, las líneas añadidas no contienen `record`, operador ternario, `var`, `::`, `.stream()`, clases anónimas (`new X() {`), lambdas guardadas en variables, `boolean[]` ni nombres completos de clase fuera de los `import`.

## 8. Verificación automática

- [ ] 8.1 `mvn test` en verde, con al menos los tests de antes más los nuevos.

## 9. Verificación manual

- [ ] 9.1 Borrar o renombrar la carpeta de datos (`%APPDATA%\Facturacion`) para empezar desde cero y arrancar con `mvn javafx:run`: se carga la empresa de demostración sin errores.
- [ ] 9.2 Clientes → Nuevo: las etiquetas obligatorias llevan asterisco. Escribir NIF `123` y pasar al siguiente campo con Tab: el NIF se pone en rojo, no sale aviso y el foco pasa al campo siguiente.
- [ ] 9.3 Con ese NIF mal, pulsar Cancelar: la ficha se cierra al primer clic.
- [ ] 9.4 Abrir otra vez Nuevo cliente, rellenar solo el nombre y pulsar Guardar: sale **un** aviso «El NIF/NIE es obligatorio.» y los campos obligatorios vacíos quedan en rojo.
- [ ] 9.5 NIF `123` y Guardar: aviso de formato con los tres ejemplos. NIF `12345678A` y Guardar: «La letra no es correcta.». NIF `12345678A` y Enter en el campo: sale el mismo aviso de letra.
- [ ] 9.6 CP vacío y Guardar (con NIF bien): «El código postal es obligatorio.». Email `pepe@` y Guardar: «Revise el formato del correo electrónico.». Email vacío con el resto bien: se guarda.
- [ ] 9.7 Editor → Nueva factura: etiquetas con asterisco en el bloque Cliente y nada descolocado. Sin datos de cliente y Guardar: «Indique los datos del cliente.». Con NIF mal, pulsar Volver: sale del Editor (con el aviso de cambios sin guardar si los hay).
- [ ] 9.8 Generar facturas mensuales para «Otro Cliente S.L.» (ya con `B77777779`): se generan sin avisos.
- [ ] 9.9 Crear una rectificativa de una factura de la demo: se crea sin avisos.
