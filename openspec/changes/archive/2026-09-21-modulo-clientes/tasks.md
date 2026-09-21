> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código, los mensajes y los Javadoc exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: solo `Exception` en lo nuevo, sin ternarios, sin `var`, sin streams, sin `::`, sin `Optional`, siempre `import` y Javadoc corto en primera persona del plural. En los ficheros que solo cambian de plumbing (sección 6) no se arregla nada más.

## 1. La clase de datos

- [x] 1.1 Rehacer `modelo/dominio/Cliente.java`: constructor con los seis datos obligatorios, constructor copia, setters que comprueban y lanzan `Exception`, `setId` y `setActivo` sin comprobar. Ver `design.md - D1`.
- [x] 1.2 Añadir a `Cliente` los getters de texto `getEstadoTexto()` y `getNombreNif()`, `equals` y `hashCode` por el NIF y `toString()` que devuelve `getNombreNif()`. Quitar el método `nombreNif()`.
- [x] 1.4 Añadir a `Cliente` los siete comprobadores `static` (`errorNombre`, `errorNif`, `errorDireccion`, `errorCp`, `errorLocalidad`, `errorProvincia`, `errorEmail`) y hacer que **cada setter los use** en vez de repetir la comprobación. Ver `design.md - D1`.
- [x] 1.3 `grep -n "PATTERN" src/main/java/cabofactu/modelo/dominio/Cliente.java`: sin resultados (las comprobaciones con cálculo se quedan en `utilidades`).

## 2. El negocio

- [x] 2.1 Rehacer `modelo/negocio/Clientes.java` como singleton con `getClientes()` y el SQL dentro: `listado(boolean)`, `listado(String, boolean)`, `buscar(long)`, `alta`, `modificar`, `baja`, `desactivar` y `tieneFacturas`. Ver `design.md - D2`.
- [x] 2.2 En `Clientes`, el método privado `crearCliente(ResultSet)` que usan los tres métodos de consulta. Ver `design.md - D2`.
- [x] 2.3 Borrar `modelo/negocio/sqlite/ClienteDAO.java`.
- [x] 2.4 `grep -rn "SELECT \*" src/main/java/cabofactu/modelo/negocio/Clientes.java`: sin resultados.

## 3. Controlador y Modelo

- [x] 3.1 En `modelo/Modelo.java`: quitar el campo `clientes`, el `ClienteDAO` del constructor y el getter `getClientes()`; añadir las ocho operaciones. Ver `design.md - D3`.
- [x] 3.2 En `controlador/Controlador.java`: las mismas ocho operaciones, de una línea, llamando al modelo.
- [x] 3.3 `grep -rn "getModelo().getClientes()" src`: sin resultados.

## 4. La ficha en FXML

- [x] 4.1 Añadir `crearVentanaModal(Parent, String)` a `vista/Vista.java`. Ver `design.md - D4`.
- [x] 4.2 Crear `src/main/resources/cabofactu/vista/recursos/FichaCliente.fxml` con los siete campos, el `CheckBox`, el título y los botones Guardar y Cancelar de la tabla de `design.md - D5`.
- [x] 4.3 Crear `vista/controlador/FichaClienteController.java` con `setRegistro`, `getRegistro`, `initialize`, `guardar`, `cancelar`, `clienteDeLosCampos`, `actualizarRegistro`, `quitarMarcas` y `cerrarVentana`. Ver `design.md - D5`.
- [x] 4.5 En `FichaClienteController`, sustituir `marcarCampoDelError`/`marcar` por `marcarCamposMalos()` y `revisar(...)`, que marcan **todos** los campos incorrectos y devuelven el mensaje del primero, y adaptar `guardar`. Ver `design.md - D5`.
- [x] 4.6 En `crearVentanaModal`, cerrar la ventana con la X debe comportarse como Cancelar: la ficha no devuelve registro.
- [x] 4.7 En `FichaClienteController`, añadir `fotoDeLosCampos()`, el campo `fotoInicial` (que se guarda al final de `setRegistro`) y `confirmarDescartar()`, y llamarlo desde `cancelar`. Ver `design.md - D5`.
- [x] 4.8 `FichaClienteController implements Pantalla, Initializable` y contesta a `puedeCerrar()` con `confirmarDescartar()`. `crearVentanaModal` recibe un tercer parámetro `Pantalla` y consume el cierre si dice que no; `ClientesController.abrirFicha` le pasa la ficha. Ver `design.md - D5`.
- [x] 4.4 En `temas/base.css`, añadir `.campo-error` con el borde de aviso del tema. Ver `design.md - D5`.

## 5. La pantalla

- [x] 5.1 Rehacer `vista/controlador/ClientesController.java` con `registro`, `filtro`, `listaClientes`, `initialize`, `alMostrar`, `seleccionar`, `buscar`, `anadirCliente`, `editarCliente`, `borrarCliente`, `volver`, `editar`, `abrirFicha` y `refrescarTabla`. Ver `design.md - D6`.
- [x] 5.2 Quitar de `ClientesController` todo lo de la ficha: `construirFicha`, `fichaCliente`, `marcarCampo`, `marcarCamposFicha`, `avisarPrimerErrorFicha`, `comprobarAntesDeGuardar` y los siete campos `TextField`.
- [x] 5.3 En `Clientes.fxml`: `onMouseClicked="#seleccionar"` en la tabla, `onKeyReleased="#buscar"` en el buscador, los `onAction` con los nombres nuevos y el menú contextual con Editar y Borrar. No se tocan tarjetas, iconos ni anchos.
- [x] 5.4 Las columnas con `PropertyValueFactory<>("nombre")`, `("nif")`, `("localidad")` y `("estadoTexto")`. Ver `design.md - D6`.

## 6. Los que usaban el DAO

- [x] 6.1 `modelo/negocio/Facturas.java`: quitar el `ClienteDAO` del constructor y del campo, y cambiar las cinco llamadas por `Clientes.getClientes()`. Ver `design.md - D7`.
- [x] 6.2 En `Facturas`, pasar a `throws Exception` los métodos que lo necesiten (`crearFactura`, `guardarEditada`, `cliente`, `abrirVersion`) y arreglar en cascada quien los llama.
- [x] 6.3 `modelo/negocio/Estados.java`: `snapshotCliente` con el constructor y `throws Exception`. Ver `design.md - D7`.
- [x] 6.4 `modelo/negocio/Rectificativas.java`: `clienteDeVersion` con el constructor y `throws Exception`. Ver `design.md - D7`.
- [x] 6.5 `vista/controlador/EditorController.java`: las tres llamadas del desplegable a `listadoClientes`, y la factura con el cliente borrado sin fabricar un `Cliente` vacío. Ver `design.md - D7`.
- [x] 6.6 `vista/controlador/GenerarFacturasMensualesController.java`: `listar(true)` pasa a `listadoClientes(true)`.
- [x] 6.7 `grep -rn "ClienteDAO\|clienteDAO" src`: sin resultados.
- [x] 6.8 `mvn -q compile` sin errores.
- [x] 6.9 Añadir a `Cliente` el método `tieneLosMismosDatos(Cliente otro)`, que compara los siete datos sin mirar el id ni `activo`. Ver `design.md - D9`.
- [x] 6.10 En `Facturas.guardarEditada`, **quitar** el bloque que llama a `Clientes.getClientes().modificar(cliente)`. Se queda el `alta` de cuando el cliente es nuevo. Ver `design.md - D9`.
- [x] 6.11 En `EditorController.guardar()`, añadir `pedirActualizarFicha(cli)` antes de guardar la factura y, si el usuario acepta y la factura se guarda bien, llamar a `modificarCliente(cli)` y poner `clienteActual = cli`. Ver `design.md - D9`.
- [x] 6.12 `grep -rn "getClientes().modificar" src/main/java/cabofactu/modelo/negocio`: sin resultados.

## 7. Tests

- [x] 7.1 Borrar `src/test/java/cabofactu/modelo/negocio/ValidacionClienteTest.java`.
- [x] 7.2 Crear `src/test/java/cabofactu/modelo/dominio/ClienteTest.java` con los catorce casos de `design.md - D8`.
- [x] 7.10 Ampliar `ClienteTest` con `tieneLosMismosDatos`: true con una copia, false cambiando cualquiera de los siete datos, false con `null`, y true aunque cambien el id y el `activo`.
- [x] 7.9 Ampliar `ClienteTest`: que cada `errorX` devuelva el mismo mensaje que lanza su setter, y `null` con un dato correcto. `errorEmail` con vacío devuelve `null`.
- [x] 7.3 Crear `src/test/java/cabofactu/modelo/negocio/ClientesTest.java` contra una base temporal, con los ocho casos de `design.md - D8`.
- [x] 7.4 Adaptar `FacturasTest`, `EstadosTest`, `HistorialTest` y `FacturacionMensualTest` al constructor de `Facturas` sin DAO y al constructor de `Cliente`.
- [x] 7.5 Añadir `FichaCliente.fxml` a `CargaPantallasTest`.
- [x] 7.6 Con la aplicación cerrada, borrar `target` y ejecutar `mvn test`: todos en verde.
- [x] 7.7 En los ficheros nuevos o rehechos (`Cliente`, `Clientes`, `ClientesController`, `FichaClienteController`, `Controlador`, `Modelo`) buscar lo que prohíbe `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, `Optional`, `record`, clases anónimas, nombres completos de clase): ninguno.
- [x] 7.8 `openspec validate modulo-clientes --strict` sin errores.

## 8. Documentación y estado

- [x] 8.1 Añadir este change a «En curso» en `ESTADO.md`.

## 9. Pruebas manuales

- [x] 9.1 Abrir Clientes: salen los dos clientes de la demostración con su nombre, NIF, localidad y «Activo», y el conteo debajo.
- [x] 9.2 Escribir en el buscador parte de un nombre y parte de un NIF: la lista y el conteo se filtran con cada tecla.
- [x] 9.3 «Nuevo»: se abre la ficha en una ventana propia, con el tema y el icono de la aplicación, el título «Alta de cliente» y el botón «Añadir». No se puede usar la ventana de detrás mientras está abierta.
- [x] 9.4 Guardar un cliente nuevo correcto: se cierra la ficha, aparece en la tabla y el conteo sube.
- [x] 9.5 En la ficha, poner el NIF `123` y guardar: sale el aviso «Formato NIF/NIE incorrecto…», **el campo del NIF queda con el borde de aviso** y la ficha sigue abierta.
- [x] 9.6 Dejar el NIF y el código postal vacíos y guardar: **los dos campos quedan en rojo** y sale un único aviso, el del NIF. Rellenar el NIF y volver a guardar: el NIF se limpia, el CP sigue en rojo y ahora sale su aviso.
- [x] 9.7 Escribir un NIF en minúsculas (`12345678z`) y guardar: se guarda y en la tabla sale en mayúsculas.
- [x] 9.8 Doble clic en una fila: se abre la ficha en modo editar con los datos cargados y el botón «Guardar».
- [x] 9.9 Editar un cliente, cambiarle el **nombre** (que se aplica antes que el NIF) y ponerle el NIF `12345678A`. Pulsar **Guardar**: sale «La letra no es correcta.» y la ficha sigue abierta. Pulsar entonces **Cancelar**: la fila de la tabla **conserva su nombre original**.
- [x] 9.19 En una factura **nueva**, elegir un cliente, cambiarle la dirección y guardar: la aplicación pregunta si actualizar su ficha. Al **aceptar**, la factura lleva la dirección nueva y la ficha de Clientes también.
- [x] 9.20 Repetir en una factura **ya guardada** abierta desde el Histórico, y **cancelar** la pregunta: la factura queda con la dirección nueva y la ficha de Clientes **conserva la vieja**.
- [x] 9.21 Guardar una factura sin tocar ningún dato del cliente: **no** pregunta nada.
- [x] 9.22 Abrir la ficha con **Nuevo**, no escribir nada y cerrar con Cancelar y con la X: se cierra **sin preguntar** las dos veces.
- [x] 9.23 Abrir la ficha, escribir algo y pulsar **Cancelar**: pregunta «Hay cambios sin guardar en la ficha del cliente. ¿Quieres descartarlos?». Al cancelar la pregunta, la ficha sigue abierta con lo escrito; al aceptar, se cierra sin guardar.
- [x] 9.18 Repetir 9.9 cerrando la ficha con la **X** en vez de con Cancelar: **pregunta igual que Cancelar** y, al aceptar, tampoco se guarda nada.
- [x] 9.10 Desmarcar «Cliente activo» y guardar: la columna Estado pasa a «Inactivo».
- [x] 9.11 Intentar eliminar un cliente con facturas: no se borra y ofrece marcarlo como inactivo.
- [x] 9.12 Crear un cliente sin facturas y eliminarlo: pide confirmación con Aceptar/Cancelar y desaparece de la tabla.
- [x] 9.13 Botón derecho sobre una fila: salen Editar y Borrar y hacen lo mismo que los botones.
- [x] 9.14 Pulsar «Editar» o «Eliminar» sin seleccionar nada: sale el aviso de que hay que elegir un cliente.
- [x] 9.15 En el Editor de factura, desplegar el selector de cliente y escribir para buscar: siguen saliendo solo los activos y al elegir uno se cargan sus datos.
- [x] 9.16 «Facturar mes»: la lista de clientes sigue cargándose.
- [x] 9.17 Anular una factura de la demostración y crear una rectificativa: las dos siguen funcionando.
