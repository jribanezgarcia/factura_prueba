> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código, los mensajes y los Javadoc exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: solo `Exception` en lo nuevo, sin ternarios, sin `var`, sin streams, sin `::`, sin clases anónimas, sin `Optional` (salvo el de `showAndWait()` leído con `isPresent()` y `get()`), siempre `import` y Javadoc corto en primera persona del plural. En los ficheros que solo cambian de plumbing (sección 7) no se arregla nada más. **No tocar** el `.root` de los `temas/tema-*.css`.

## 1. La clase de datos

- [ ] 1.1 Crear `modelo/dominio/EmpresaDisponible.java`: carpeta y nombre, constructor que valida la carpeta, nombre que cae en la carpeta si viene vacío, `Comparable` por nombre sin distinguir mayúsculas, `toString()` con el nombre y `equals`/`hashCode` por la carpeta. Ver `design.md - D1`.

## 2. El negocio

- [ ] 2.1 Rehacer `modelo/negocio/Empresas.java` como singleton con `getEmpresas()`: `listado`, `alta`, `baja`, `abrir`, `cerrar`, `registrarNombre`, `recordarTema` y `carpetaDe`. Borrar el `record EmpresaInfo`. Ver `design.md - D2`.
- [ ] 2.2 En `Empresas`, `borrarCarpeta(File)` en lugar de `borrarRecursivo`, avisando si no puede borrar. Ver `design.md - D2`.
- [ ] 2.3 Rehacer `modelo/negocio/Sesion.java` como singleton con `getSesion()`, `iniciar`, `terminar`, `getCarpetaEmpresa` y `getFechaTrabajo`. Ver `design.md - D3`.
- [ ] 2.4 `modelo/negocio/Reloj.java`: `fechaTrabajo()` con `Sesion.getSesion().getFechaTrabajo()` y sin ternario.
- [ ] 2.5 `modelo/negocio/Configuracion.java`: añadir `comprobarEmpresaCompleta()`. Ver `design.md - D4`.
- [ ] 2.6 `grep -rn "EmpresaInfo\|Sesion\.empresaSlug\|Sesion\.fechaTrabajo\|Sesion\.inicializar\|Empresas\.listarEmpresas\|Empresas\.crearEmpresa\|Empresas\.conectar\|Empresas\.eliminarEmpresa\|slugDe" src/main`: sin resultados.

## 3. Controlador y Modelo

- [ ] 3.1 En `modelo/Modelo.java` y `controlador/Controlador.java`, las siete operaciones de `design.md - D4`, de una línea cada una.

## 4. Las ventanas

- [ ] 4.1 En `vista/Vista.java`: constante `ARRANQUE`, `prepararArranque(Stage)`, `volverAlArranque()` y `comprobarDatosEmpresa()`; `mostrarInicio()` muestra siempre el menú y se borra la constante `CONFIGURACION`. Ver `design.md - D5`.
- [ ] 4.2 `vista/LanzadorVentanaPrincipal.java`: usar `prepararArranque(stage)` y quitar su constante `ARRANQUE`.

## 5. El arranque

- [ ] 5.1 `Arranque.fxml`: botón «Eliminar» (`btnEliminarEmpresa`, `#eliminarEmpresa`) al lado de «Nueva...».
- [ ] 5.2 `ArranqueController`: `ComboBox<EmpresaDisponible>`, sin las dos `ListCell` anónimas; `cargarEmpresas` con un `for`; `actualizarBoton` para Entrar y Eliminar; `nuevaEmpresa` con `isPresent()`/`get()` y `altaEmpresa`; `entrar` con `abrirEmpresa`; `eliminarEmpresa` nuevo. Ver `design.md - D6`.
- [ ] 5.3 `ArranqueController.mostrarAvisoInicial`: los dos textos nuevos de `design.md - D6`.

## 6. Configuración y menú

- [ ] 6.1 `Configuracion.fxml` y `ConfiguracionController`: borrar la sección «Empresas» entera (FXML, entrada de `configurarSecciones`, campos y los cinco métodos). Ver `design.md - D7`.
- [ ] 6.2 `ConfiguracionController.initialize`: quitar `bloquearSalvoSalir()` y el `setDisable` de Volver; `nombreVisibleEmpresaActiva()` con `Sesion.getSesion()` y `listadoEmpresas()`; nuevo texto de `lblDatosPendientes` en el FXML. Ver `design.md - D7`.
- [ ] 6.3 Botón «Cambiar de empresa» en la barra de abajo de `Configuracion.fxml` y método `cambiarDeEmpresa()`. Ver `design.md - D7`.
- [ ] 6.4 Borrar `BarraNavegacionController.bloquearSalvoSalir()`.
- [ ] 6.5 `MenuPrincipal.fxml`: la franja `franjaDatosPendientes` con `lblDatosPendientes` y «Completar datos». `MenuPrincipalController`: `mostrarDatosPendientes()` desde `initialize` y `completarDatos(ActionEvent)`. Ver `design.md - D8`.
- [ ] 6.6 `temas/base.css`: la clase `.franja-aviso`. Ver `design.md - D8`.

## 7. Plumbing y comprobaciones de datos

- [ ] 7.1 Las cinco comprobaciones de `design.md - D9` en `EditorController` (`guardar`, `exportarPdf`, `crearRectificativa`), `HistoricoController.exportarPdf` y `GenerarFacturasMensualesController.generar`.
- [ ] 7.2 `fichero/CopiaSeguridad`, `CargarDemo` (`SLUG` → `CARPETA`), `PreparacionDatos` y `CopiaSeguridadController`, según la tabla de `design.md - D10`.
- [ ] 7.3 `grep -rn "bloquearSalvoSalir\|seccionEmpresas\|tablaEmpresas\|CargarDemo.SLUG" src`: sin resultados.
- [ ] 7.4 `mvn -q compile` sin errores.

## 8. Tests

- [ ] 8.1 Rehacer `src/test/java/cabofactu/modelo/negocio/EmpresasTest.java` con los casos de `design.md - D11`.
- [ ] 8.2 Crear `src/test/java/cabofactu/modelo/dominio/EmpresaDisponibleTest.java`. Ver `design.md - D11`.
- [ ] 8.3 Ampliar `ConfiguracionTest` con `comprobarEmpresaCompleta`.
- [ ] 8.4 Adaptar `CopiaSeguridadTest`, `CopiaSeguridadDAOTest` y `PreparacionDatosTest`.
- [ ] 8.5 Con la aplicación cerrada, borrar `target` y ejecutar `mvn test`: todos en verde y **ningún** `ClassCastException` en el log.
- [ ] 8.6 En los ficheros nuevos o rehechos (`EmpresaDisponible`, `Empresas`, `Sesion`, y en las líneas nuevas de `Vista`, `ArranqueController`, `ConfiguracionController` y `MenuPrincipalController`) buscar lo que prohíbe `AGENTS.md`: ninguno.
- [ ] 8.7 `openspec validate modulo-empresas-y-menu --strict` sin errores.

## 9. Documentación y estado

- [ ] 9.1 Añadir este change a «En curso» en `ESTADO.md`.

## 10. Pruebas manuales

- [ ] 10.1 Arrancar: el desplegable muestra los nombres de las empresas, con la última usada elegida, y los botones «Nueva...» y «Eliminar».
- [ ] 10.2 «Nueva...», escribir «Prueba Uno»: aparece en la lista y queda elegida sin salir del arranque.
- [ ] 10.3 Entrar en «Prueba Uno»: se abre el **menú** (no Configuración), con la franja «Faltan datos de tu empresa: …» y el botón «Completar datos».
- [ ] 10.4 Con la empresa incompleta, recorrer con la barra de arriba Nueva, Histórico, Clientes, Configuración y Copias: **ningún botón está bloqueado**.
- [ ] 10.5 En el Editor de «Prueba Uno», intentar guardar una factura: aviso «Faltan datos de tu empresa: …» y no se guarda. Igual con exportar a PDF y con «Facturar mes» → Generar.
- [ ] 10.6 «Completar datos»: se abre Configuración en la sección Empresa, con el texto de datos pendientes y el nombre propuesto. Rellenar todo y guardar: vuelve al menú **sin la franja**.
- [ ] 10.7 Configuración: ya no hay sección «Empresas» en la lista de la izquierda.
- [ ] 10.8 «Cambiar de empresa» y **Cancelar**: no pasa nada. Otra vez y **Aceptar**: se cierra la ventana grande, se abre la de arranque (pequeña, centrada) con «Prueba Uno» elegida, y **no** sale la pregunta de salir de la aplicación.
- [ ] 10.9 En el arranque, elegir «Prueba Uno», «Eliminar» y **Cancelar**: sigue en la lista. «Eliminar» y **Aceptar**: desaparece de la lista y su carpeta ya no está en `%APPDATA%\Facturacion`.
- [ ] 10.10 Entrar en la empresa de demostración: menú **sin franja**, y todo funciona como antes.
- [ ] 10.11 Desde el menú, «Salir» y confirmar: la aplicación se cierra del todo y se puede volver a abrir sin el aviso de instancia única.
- [ ] 10.12 Copias → restaurar una copia como empresa nueva y aceptar cambiar a ella: entra en el menú de la empresa restaurada.
