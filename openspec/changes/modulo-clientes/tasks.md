> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código, los mensajes y los Javadoc exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: solo `Exception` en lo nuevo, sin ternarios, sin `var`, sin streams, sin `::`, sin `Optional`, siempre `import` y Javadoc corto en primera persona del plural. En los ficheros que solo cambian de plumbing (sección 6) no se arregla nada más.

## 1. La clase de datos

- [ ] 1.1 Rehacer `modelo/dominio/Cliente.java`: constructor con los seis datos obligatorios, constructor copia, setters que comprueban y lanzan `Exception`, `setId` y `setActivo` sin comprobar. Ver `design.md - D1`.
- [ ] 1.2 Añadir a `Cliente` los getters de texto `getEstadoTexto()` y `getNombreNif()`, `equals` y `hashCode` por el NIF y `toString()` que devuelve `getNombreNif()`. Quitar el método `nombreNif()`.
- [ ] 1.3 `grep -n "PATTERN" src/main/java/cabofactu/modelo/dominio/Cliente.java`: sin resultados (las comprobaciones con cálculo se quedan en `utilidades`).

## 2. El negocio

- [ ] 2.1 Rehacer `modelo/negocio/Clientes.java` como singleton con `getClientes()` y el SQL dentro: `listado(boolean)`, `listado(String, boolean)`, `buscar(long)`, `alta`, `modificar`, `baja`, `desactivar` y `tieneFacturas`. Ver `design.md - D2`.
- [ ] 2.2 En `Clientes`, el método privado `crearCliente(ResultSet)` que usan los tres métodos de consulta. Ver `design.md - D2`.
- [ ] 2.3 Borrar `modelo/negocio/sqlite/ClienteDAO.java`.
- [ ] 2.4 `grep -rn "SELECT \*" src/main/java/cabofactu/modelo/negocio/Clientes.java`: sin resultados.

## 3. Controlador y Modelo

- [ ] 3.1 En `modelo/Modelo.java`: quitar el campo `clientes`, el `ClienteDAO` del constructor y el getter `getClientes()`; añadir las ocho operaciones. Ver `design.md - D3`.
- [ ] 3.2 En `controlador/Controlador.java`: las mismas ocho operaciones, de una línea, llamando al modelo.
- [ ] 3.3 `grep -rn "getModelo().getClientes()" src`: sin resultados.

## 4. La ficha en FXML

- [ ] 4.1 Añadir `crearVentanaModal(Parent, String)` a `vista/Vista.java`. Ver `design.md - D4`.
- [ ] 4.2 Crear `src/main/resources/cabofactu/vista/recursos/FichaCliente.fxml` con los siete campos, el `CheckBox`, el título y los botones Guardar y Cancelar de la tabla de `design.md - D5`.
- [ ] 4.3 Crear `vista/controlador/FichaClienteController.java` con `setRegistro`, `getRegistro`, `initialize`, `guardar`, `cancelar`, `clienteDeLosCampos`, `actualizarRegistro`, `marcarCampoDelError`, `marcar`, `quitarMarcas` y `cerrarVentana`. Ver `design.md - D5`.
- [ ] 4.4 En `temas/base.css`, añadir `.campo-error` con el borde de aviso del tema. Ver `design.md - D5`.

## 5. La pantalla

- [ ] 5.1 Rehacer `vista/controlador/ClientesController.java` con `registro`, `filtro`, `listaClientes`, `initialize`, `alMostrar`, `seleccionar`, `buscar`, `anadirCliente`, `editarCliente`, `borrarCliente`, `volver`, `editar`, `abrirFicha` y `refrescarTabla`. Ver `design.md - D6`.
- [ ] 5.2 Quitar de `ClientesController` todo lo de la ficha: `construirFicha`, `fichaCliente`, `marcarCampo`, `marcarCamposFicha`, `avisarPrimerErrorFicha`, `comprobarAntesDeGuardar` y los siete campos `TextField`.
- [ ] 5.3 En `Clientes.fxml`: `onMouseClicked="#seleccionar"` en la tabla, `onKeyReleased="#buscar"` en el buscador, los `onAction` con los nombres nuevos y el menú contextual con Editar y Borrar. No se tocan tarjetas, iconos ni anchos.
- [ ] 5.4 Las columnas con `PropertyValueFactory<>("nombre")`, `("nif")`, `("localidad")` y `("estadoTexto")`. Ver `design.md - D6`.

## 6. Los que usaban el DAO

- [ ] 6.1 `modelo/negocio/Facturas.java`: quitar el `ClienteDAO` del constructor y del campo, y cambiar las cinco llamadas por `Clientes.getClientes()`. Ver `design.md - D7`.
- [ ] 6.2 En `Facturas`, pasar a `throws Exception` los métodos que lo necesiten (`crearFactura`, `guardarEditada`, `cliente`, `abrirVersion`) y arreglar en cascada quien los llama.
- [ ] 6.3 `modelo/negocio/Estados.java`: `snapshotCliente` con el constructor y `throws Exception`. Ver `design.md - D7`.
- [ ] 6.4 `modelo/negocio/Rectificativas.java`: `clienteDeVersion` con el constructor y `throws Exception`. Ver `design.md - D7`.
- [ ] 6.5 `vista/controlador/EditorController.java`: las tres llamadas del desplegable a `listadoClientes`, y la factura con el cliente borrado sin fabricar un `Cliente` vacío. Ver `design.md - D7`.
- [ ] 6.6 `vista/controlador/GenerarFacturasMensualesController.java`: `listar(true)` pasa a `listadoClientes(true)`.
- [ ] 6.7 `grep -rn "ClienteDAO\|clienteDAO" src`: sin resultados.
- [ ] 6.8 `mvn -q compile` sin errores.

## 7. Tests

- [ ] 7.1 Borrar `src/test/java/cabofactu/modelo/negocio/ValidacionClienteTest.java`.
- [ ] 7.2 Crear `src/test/java/cabofactu/modelo/dominio/ClienteTest.java` con los catorce casos de `design.md - D8`.
- [ ] 7.3 Crear `src/test/java/cabofactu/modelo/negocio/ClientesTest.java` contra una base temporal, con los ocho casos de `design.md - D8`.
- [ ] 7.4 Adaptar `FacturasTest`, `EstadosTest`, `HistorialTest` y `FacturacionMensualTest` al constructor de `Facturas` sin DAO y al constructor de `Cliente`.
- [ ] 7.5 Añadir `FichaCliente.fxml` a `CargaPantallasTest`.
- [ ] 7.6 Con la aplicación cerrada, borrar `target` y ejecutar `mvn test`: todos en verde.
- [ ] 7.7 En los ficheros nuevos o rehechos (`Cliente`, `Clientes`, `ClientesController`, `FichaClienteController`, `Controlador`, `Modelo`) buscar lo que prohíbe `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, `Optional`, `record`, clases anónimas, nombres completos de clase): ninguno.
- [ ] 7.8 `openspec validate modulo-clientes --strict` sin errores.

## 8. Documentación y estado

- [ ] 8.1 Añadir este change a «En curso» en `ESTADO.md`.

## 9. Pruebas manuales

- [ ] 9.1 Abrir Clientes: salen los dos clientes de la demostración con su nombre, NIF, localidad y «Activo», y el conteo debajo.
- [ ] 9.2 Escribir en el buscador parte de un nombre y parte de un NIF: la lista y el conteo se filtran con cada tecla.
- [ ] 9.3 «Nuevo»: se abre la ficha en una ventana propia, con el tema y el icono de la aplicación, el título «Alta de cliente» y el botón «Añadir». No se puede usar la ventana de detrás mientras está abierta.
- [ ] 9.4 Guardar un cliente nuevo correcto: se cierra la ficha, aparece en la tabla y el conteo sube.
- [ ] 9.5 En la ficha, poner el NIF `123` y guardar: sale el aviso «Formato NIF/NIE incorrecto…», **el campo del NIF queda con el borde de aviso** y la ficha sigue abierta.
- [ ] 9.6 Dejar el NIF y el código postal vacíos y guardar: sale solo el aviso del NIF y solo se marca el NIF. Rellenar el NIF y volver a guardar: ahora sale el del código postal.
- [ ] 9.7 Escribir un NIF en minúsculas (`12345678z`) y guardar: se guarda y en la tabla sale en mayúsculas.
- [ ] 9.8 Doble clic en una fila: se abre la ficha en modo editar con los datos cargados y el botón «Guardar».
- [ ] 9.9 Editar un cliente, romperle el NIF, pulsar **Cancelar**: la ficha se cierra y **la fila de la tabla sigue con sus datos buenos**.
- [ ] 9.10 Desmarcar «Cliente activo» y guardar: la columna Estado pasa a «Inactivo».
- [ ] 9.11 Intentar eliminar un cliente con facturas: no se borra y ofrece marcarlo como inactivo.
- [ ] 9.12 Crear un cliente sin facturas y eliminarlo: pide confirmación con Aceptar/Cancelar y desaparece de la tabla.
- [ ] 9.13 Botón derecho sobre una fila: salen Editar y Borrar y hacen lo mismo que los botones.
- [ ] 9.14 Pulsar «Editar» o «Eliminar» sin seleccionar nada: sale el aviso de que hay que elegir un cliente.
- [ ] 9.15 En el Editor de factura, desplegar el selector de cliente y escribir para buscar: siguen saliendo solo los activos y al elegir uno se cargan sus datos.
- [ ] 9.16 «Facturar mes»: la lista de clientes sigue cargándose.
- [ ] 9.17 Anular una factura de la demostración y crear una rectificativa: las dos siguen funcionando.
