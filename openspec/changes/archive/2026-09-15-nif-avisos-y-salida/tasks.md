> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. Mensajes, títulos y firmas exactos en `design.md`. Seguir `AGENTS.md` en todo el código nuevo o modificado: sin ternarios, sin `var`, sin streams ni `::`, sin lambdas guardadas en variables ni `boolean[]`, siempre `import`, Javadoc corto en clases y métodos públicos nuevos (primera persona del plural). No arreglar de paso construcciones antiguas fuera de las líneas tocadas.

## 1. Validador del NIF

- [x] 1.1 En `utilidades/ValidadorDocumentoFiscal.java`, añadir `formatoCorrecto(String)` y `letraCorrecta(String)` y hacer que `esValido(String)` los use sin cambiar su resultado. Ver `design.md - D1`.

## 2. Regla del cliente en el negocio

- [x] 2.1 Crear `modelo/negocio/ValidacionCliente.java` con los métodos y mensajes exactos de `design.md - D2`.
- [x] 2.2 En `modelo/negocio/Clientes.java`, `insertar` y `actualizar` llaman primero a `ValidacionCliente.comprobar` y declaran `throws ValidacionException`. Ver `design.md - D3`.
- [x] 2.3 En `modelo/negocio/Facturas.java`, `ValidacionCliente.comprobar(cliente)` como primera instrucción de `crearFacturaSinTransaccion` y de la sobrecarga de `guardarEditada` que hace el trabajo. Ver `design.md - D4`.
- [x] 2.4 En `modelo/negocio/FacturacionMensual.java`, añadir `ValidacionCliente.comprobar(cliente)` en `validar`, tras la comprobación de cliente existente. Ver `design.md - D5`.
- [x] 2.5 Adaptar las llamadas que ya no compilen por el nuevo `throws ValidacionException`.
- [x] 2.6 `mvn -q compile` sin errores.

## 3. Ficha de cliente

- [x] 3.1 En `vista/controlador/ClientesController.java`, poner asterisco en las etiquetas obligatorias y pasar los `TextField` de la ficha a campos privados, conservando sus `setId`. Ver `design.md - D6`.
- [x] 3.2 Quitar de la ficha los `BooleanSupplier`, los `boolean[] avisando…`, los `Runnable avisar…Invalido`, los `Platform.runLater(…requestFocus)` y los tres `addEventFilter` de Guardar.
- [x] 3.3 Añadir `marcarCampo`, `marcarCamposFicha`, `avisarPrimerErrorFicha` y el único filtro de Guardar, con el comportamiento de `design.md - D6` (rojo al salir sin aviso; aviso con Enter en NIF, CP y email; un único aviso al Guardar).
- [x] 3.4 En `nuevo()` y `editar()`, añadir `catch (ValidacionException e)` con `Dialogos.error("Datos del cliente", e.getMessage())` antes del `catch (Exception e)`.

## 4. Editor

- [x] 4.1 En `src/main/resources/cabofactu/vista/recursos/Editor.fxml`, asterisco en `Nombre*`, `NIF*`, `Dirección*`, `CP*`, `Localidad*` y `Provincia*`. Ver `design.md - D7`.
- [x] 4.2 En `vista/controlador/EditorController.java`, quitar `validarNifCliente`, el campo `corrigiendoNif` y su `runLater`; añadir `marcarCampo`, `marcarCamposCliente` y `avisarPrimerErrorCliente`, con rojo al salir sin aviso y aviso con Enter en `cliNif`. Ver `design.md - D7`.
- [x] 4.3 En `guardar()`, sustituir la validación del NIF y del nombre por los pasos 1 a 3 de `design.md - D7`.
- [x] 4.4 `mvn -q compile` sin errores.

## 5. Datos de demostración

- [x] 5.1 En `src/main/resources/db/seed_demo.sql`, cambiar las tres apariciones de `B77777777` por `B77777779`. Ver `design.md - D8`.

## 6. Tests

- [x] 6.1 Añadir a `src/test/java/cabofactu/utilidades/ValidadorDocumentoFiscalTest.java` los casos de `formatoCorrecto` y `letraCorrecta` de `design.md - D1`.
- [x] 6.2 Crear `src/test/java/cabofactu/modelo/negocio/ValidacionClienteTest.java` con los casos de `design.md - D8`.
- [x] 6.3 Adaptar `FacturasTest`, `FacturacionMensualTest`, `EstadosTest`, `HistorialTest`, `CopiaSeguridadTest` y `EditorIvaInactivoTest` según la tabla de `design.md - D8`, y añadir los tests nuevos de `FacturasTest` y `FacturacionMensualTest`.
- [x] 6.4 Adaptar `ClientesValidacionNifTest` y `EditorValidacionNifTest` según `design.md - D8` (sin aviso al salir, un aviso al guardar con su mensaje exacto, caso de NIF vacío). El ayudante falso de `Dialogos` guarda el último mensaje de error.
- [x] 6.5 No cambiar el NIF de los tests que lo usan no válido a propósito: `ValidadorDocumentoFiscalTest`, `ClientesValidacionNifTest`, `EditorValidacionNifTest` y `ConfiguracionTest.nifNoValidoDevuelveNif`.

## 7. Comprobaciones

- [x] 7.1 `git grep -n "Revise el DNI, NIE o NIF/CIF introducido" -- src` no devuelve nada.
- [x] 7.2 `git grep -nE "avisandoNif|avisandoCp|avisandoEmail|corrigiendoNif|validarNifCliente" -- src` no devuelve nada.
- [x] 7.3 `git grep -n "runLater" -- src/main/java/cabofactu/vista/controlador/ClientesController.java` no devuelve nada, y en `EditorController.java` ya no aparece `cliNif::requestFocus`.
- [x] 7.4 `git grep -n "B77777777" -- src` no devuelve nada.
- [x] 7.5 En los ficheros tocados, las líneas añadidas no contienen `record`, operador ternario, `var`, `::`, `.stream()`, clases anónimas (`new X() {`), lambdas guardadas en variables, `boolean[]` ni nombres completos de clase fuera de los `import`.

## 8. Verificación automática

- [x] 8.1 `mvn test` en verde, con al menos los tests de antes más los nuevos.

## 10. Ajuste tras la primera aplicación: avisar solo al guardar

> Añadido el 15/09/2026. El usuario decide que los datos del cliente se comprueban **solo al guardar**, como en Biblioteca8. Ver `design.md - D6` y `D7`.

- [x] 10.1 En `vista/controlador/ClientesController.java`, quitar los escuchadores de foco de los siete campos de la ficha, los `setOnAction` de NIF, CP y email, y los métodos que solo usaban (`alSalirDeCampo`, `avisarCampo`). Se quedan `marcarCampo`, `marcarCamposFicha`, `avisarPrimerErrorFicha` y `comprobarAntesDeGuardar`.
- [x] 10.2 En `vista/controlador/EditorController.java`, quitar los escuchadores de foco de los campos del cliente, el `setOnAction` de `cliNif` y los métodos que solo usaban (`alSalirDeCampoCliente`, `avisarNifCliente`). Se quedan `marcarCampo`, `marcarCamposCliente` y `avisarPrimerErrorCliente`.
- [x] 10.3 En `EditorController.cargarDatosCliente`, tras rellenar los campos, quitar el rojo de los siete campos del cliente.
- [x] 10.4 Adaptar `ClientesValidacionNifTest` y `EditorValidacionNifTest` a la tabla de `design.md - D8`: al salir del campo ni rojo ni aviso; al guardar, rojo y un aviso.
- [x] 10.5 Repetir las comprobaciones 7.1 a 7.5 y `mvn test` en verde.

## 11. Avisos que no se cortan

> Añadido el 15/09/2026 tras la prueba manual: el aviso del código postal se veía cortado. Causa y arreglo medidos en `design.md - D10`.

- [x] 11.1 En `vista/utilidades/Dialogos.java`, añadir `ponerMensaje(Alert, String)` y usarlo en `error`, `info` y `confirmar` en lugar de `setContentText`. Ver `design.md - D10`.
- [x] 11.2 Crear `src/test/java/cabofactu/vista/utilidades/DialogosTextoCompletoTest.java` con los dos mensajes de `design.md - D10`.
- [x] 11.3 Repetir las comprobaciones 7.5 (construcciones prohibidas en líneas añadidas) y `mvn test` en verde.
- [x] 11.4 Verificación manual: Clientes → Nuevo con el resto de datos bien y CP `99999` → Guardar: el aviso muestra el texto entero «El código postal debe tener cinco dígitos y comenzar entre 01 y 52.». Guardar con NIF `123`: el aviso de formato se lee entero. En el Editor, con cambios, pulsar Volver: el aviso de cambios sin guardar se lee entero.

## 12. Dialogos ordenado como Biblioteca8

> Añadido el 15/09/2026 a petición del usuario. Detalle en `design.md - D11`. No cambia nada visible.

- [x] 12.1 Crear en `vista/utilidades/` los ficheros `CambiosSinGuardar.java`, `ModoGuardarVersion.java`, `MostradorDialogos.java` y `DialogosReales.java`, moviendo el código desde `Dialogos.java` según `design.md - D11`.
- [x] 12.2 Dejar `Dialogos.java` solo con la parte `static` de `design.md - D11` (sin enum, sin interfaz, sin clase anónima).
- [x] 12.3 Aplicar al código movido la tabla «Construcciones que se cambian» de `design.md - D11` (ternario, `Optional.map`, lambda de `setOnShown` e `instanceof`).
- [x] 12.4 Actualizar los usos en `EditorController`, `ClientesValidacionNifTest`, `EditorIvaInactivoTest` y `EditorValidacionNifTest`, y sacar a un método privado el bloque de `Platform.runLater` de `DialogosTextoCompletoTest`.
- [x] 12.5 Comprobar que `git grep -nE "Dialogos.(Impl|CambiosSinGuardar|ModoGuardarVersion)|IMPLEMENTACION_POR_DEFECTO" -- src` no devuelve nada, repetir 7.5 y `mvn test` en verde.
- [x] 12.6 Verificación manual: en el Editor, con cambios, pulsar Volver (sale el aviso de Guardar / Descartar / Cancelar y cada botón hace lo de siempre); guardar una factura ya guardada (sale el aviso de sobrescribir o nueva versión); un aviso de error y uno de confirmación muestran su icono y el icono de la aplicación en la barra de la ventana.

## 9. Verificación manual (tras la sección 10)

- [x] 9.1 Borrar o renombrar la carpeta de datos (`%APPDATA%\Facturacion`) para empezar desde cero y arrancar con `mvn javafx:run`: se carga la empresa de demostración sin errores.
- [x] 9.2 Clientes → Nuevo: las etiquetas obligatorias llevan asterisco. Escribir el nombre y NIF `123` y pasar de campo con Tab y con Enter: no sale ningún aviso y ningún campo se pone en rojo.
- [x] 9.3 Con ese NIF mal, pulsar Cancelar: la ficha se cierra al primer clic.
- [x] 9.4 Abrir otra vez Nuevo cliente, rellenar solo el nombre y pulsar Guardar: sale **un** aviso «El NIF/NIE es obligatorio.» y los campos obligatorios vacíos quedan en rojo.
- [x] 9.5 NIF `123` y Guardar: aviso de formato con los tres ejemplos. NIF `12345678A` y Guardar: «La letra no es correcta.». Tras cada aviso, el NIF queda en rojo; al corregirlo y volver a Guardar, deja de estar en rojo.
- [x] 9.6 CP vacío y Guardar (con NIF bien): «El código postal es obligatorio.». Email `pepe@` y Guardar: «Revise el formato del correo electrónico.». Email vacío con el resto bien: se guarda.
- [x] 9.7 Editor → Nueva factura: etiquetas con asterisco en el bloque Cliente y nada descolocado. Sin datos de cliente y Guardar: «Indique los datos del cliente.». Con NIF mal y Guardar: aviso y NIF en rojo; después elegir otro cliente del desplegable: el rojo desaparece. Con NIF mal, pulsar Volver: sale del Editor (con el aviso de cambios sin guardar si los hay).
- [x] 9.8 Generar facturas mensuales para «Otro Cliente S.L.» (ya con `B77777779`): se generan sin avisos.
- [x] 9.9 Crear una rectificativa de una factura de la demo: se crea sin avisos.
