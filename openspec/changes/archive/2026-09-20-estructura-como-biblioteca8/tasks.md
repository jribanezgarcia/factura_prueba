> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código, los mensajes y los Javadoc exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: solo `Exception` en lo nuevo, sin ternarios, sin `var`, sin streams, sin `::`, sin `Optional`, siempre `import` y Javadoc corto en primera persona del plural. En las líneas donde solo se sustituye `modelo.` o `nav.` no se arregla nada más.

## 1. El esqueleto

- [x] 1.1 Renombrar con `git mv` la interfaz `vista/Vista.java` a `vista/Pantalla.java` y dejarla como en `design.md - D4`.
- [x] 1.2 Crear `controlador/Controlador.java`. Ver `design.md - D2` y `D3`.
- [x] 1.3 Crear la clase `vista/Vista.java` con `getInstancia`, `setControlador`, `getControlador`, `getVentana`, `setVentana`, `getPantallaActual`, `comenzar`, `mostrarInicio`, `mostrar`, `abrirVentanaPrincipal`, `pedirCierre`, `puedeSalir` y `salir`. Ver `design.md - D5`, `D6` y `D7`.
- [x] 1.4 Crear `vista/recursos/LocalizadorRecursos.java` y `vista/LanzadorVentanaPrincipal.java`. Ver `design.md - D7`.
- [x] 1.5 Crear `AppCaboFactu.java` y cambiar `mainClass` en `pom.xml`. Ver `design.md - D1`.
- [x] 1.6 Borrar `Launcher.java`, `Main.java` y `vista/Navegador.java`.
- [x] 1.7 En `vista/ConfiguracionVentana.java`, guardar el **nombre** del FXML en lugar de la ruta completa y que `para(String)` devuelva `null`. Ver `design.md - D6`.
- [x] 1.8 En `modelo/Modelo.java`, quitar la conexión del constructor y actualizar su Javadoc. Ver `design.md - D11`.

## 2. Diálogos

- [x] 2.1 Reescribir `vista/utilidades/Dialogos.java` partiendo de la clase de Biblioteca8, con el tema, el icono y la etiqueta que evita que los mensajes se corten. Ver `design.md - D8`.
- [x] 2.2 Borrar `vista/utilidades/MostradorDialogos.java` y `vista/utilidades/DialogosReales.java`.
- [x] 2.3 Cambiar las llamadas en todo `src/main`: `Dialogos.error(` → `Dialogos.mostrarDialogoError(`, `info(` → `mostrarDialogoInformacion(`, `confirmar(` → `mostrarDialogoConfirmacion(`, `confirmarCambiosSinGuardar()` → `mostrarDialogoCambiosSinGuardar()`, `modoGuardarVersion()` → `mostrarDialogoModoGuardarVersion()`.
- [x] 2.4 `grep -rn "Dialogos\.\(error\|info\|confirmar\|confirmarCambios\|modoGuardar\)" src`: sin resultados.

## 3. Barra de navegación en FXML

- [x] 3.1 Crear `src/main/resources/cabofactu/vista/recursos/BarraNavegacion.fxml` con los 7 botones de la tabla de `design.md - D9`, copiando textos, tooltips, rutas SVG y escalas de `vista/utilidades/BarraNavegacion.java`.
- [x] 3.2 Crear `vista/controlador/BarraNavegacionController.java` con `irInicio`, `irNueva`, `irHistorico`, `irClientes`, `irConfiguracion`, `irCopias`, `salir`, `marcarActivo` y `bloquearSalvoSalir`. Ver `design.md - D9`.
- [x] 3.3 En `Clientes.fxml`, `Configuracion.fxml`, `CopiaSeguridad.fxml`, `Editor.fxml`, `Historico.fxml` y `Versiones.fxml`, sustituir el `HBox` `barraNavegacion` por `<fx:include fx:id="barra" source="BarraNavegacion.fxml"/>`.
- [x] 3.4 En `temas/base.css`, `.nav-bar` con `-fx-padding: 8px 0 8px 0;` y añadir el `:hover` que sustituye a `Microinteracciones`. Ver `design.md - D9` y `D11`.
- [x] 3.5 Borrar `vista/utilidades/BarraNavegacion.java` y `vista/utilidades/Microinteracciones.java`.

## 4. Pantallas

- [x] 4.1 `ArranqueController`: `Pantalla` + `Initializable`, `initialize`, y `entrar()` abre la ventana principal y cierra la de arranque; quitar `onEntrar` y `setOnEntrar`. Ver `design.md - D7` y `D10`.
- [x] 4.2 `MenuPrincipalController`: puntos 1 a 6 de `design.md - D10`.
- [x] 4.3 `EditorController`: puntos 1 a 5 y 7 de `design.md - D10`.
- [x] 4.4 `HistoricoController`: puntos 1 a 5 y 7, con el cast a `EditorController` y `GenerarFacturasMensualesController.abrir()`.
- [x] 4.5 `ClientesController` y `VersionesController`: puntos 1 a 5 y 7.
- [x] 4.6 `ConfiguracionController` y `CopiaSeguridadController`: puntos 1 a 4 y 7, con `barraController.bloquearSalvoSalir()`.
- [x] 4.7 `GenerarFacturasMensualesController`: punto 8 de `design.md - D10`.
- [x] 4.8 `grep -rn "Navegador\|setModelo\|setNavegador\|alIniciar\|BarraNavegacion\.crear\|implements Vista" src/main`: sin resultados.
- [x] 4.9 `mvn -q compile` sin errores.

## 5. Tests

- [x] 5.1 Borrar los 14 ficheros de test y el FXML de prueba que indica `design.md - D12`.
- [x] 5.2 Añadir `prepararVista(Modelo, Stage)` a `src/test/java/cabofactu/vista/PruebasJavaFx.java`. Ver `design.md - D12`.
- [x] 5.3 Adaptar `CargaPantallasTest`. Ver `design.md - D12`.
- [x] 5.4 `grep -rn "Navegador\|setModelo\|alIniciar\|Dialogos.setImpl" src/test`: sin resultados.
- [x] 5.5 Con la aplicación cerrada, borrar `target` y ejecutar `mvn test`: todos en verde.
- [x] 5.6 En los ficheros nuevos (`AppCaboFactu`, `Controlador`, `Vista`, `Pantalla`, `LanzadorVentanaPrincipal`, `LocalizadorRecursos`, `BarraNavegacionController`, `Dialogos`) y en los métodos `alMostrar`, buscar lo que prohíbe `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, `Optional`, nombres completos de clase): ninguno.
- [x] 5.7 `openspec validate estructura-como-biblioteca8 --strict` sin errores.

## 6. Documentación y estado

- [x] 6.1 Actualizar la sección «Transición» de `AGENTS.md`: el esqueleto ya cumple las normas. Ver `design.md - D13`.
- [x] 6.2 Añadir este change a «En curso» en `ESTADO.md`.

## 7. Pruebas manuales

- [x] 7.1 `mvn javafx:run`: se abre la ventana de arranque de 760x520, centrada, con el aviso de bienvenida si se acaba de cargar la demostración.
- [x] 7.2 Con la aplicación abierta, lanzarla otra vez desde otra terminal: sale «La aplicación ya está en ejecución» y la segunda no se abre.
- [x] 7.3 Pulsar Entrar: se abre la ventana principal a 1024x768, centrada, con el menú (o Configuración si faltan datos), y la ventana de arranque desaparece.
- [x] 7.4 Recorrer con la barra de arriba Nueva, Histórico, Clientes, Configuración y Copias: cada pantalla marca su botón con la raya de color, y la barra se ve y mide igual que antes.
- [x] 7.5 Atajos: en el menú Ctrl+N y Ctrl+F; en el Editor Ctrl+S, Ctrl+P y Esc; en el Histórico Ctrl+F; en Clientes Esc. El menú abre sin ningún botón resaltado.
- [x] 7.6 Guardar un cliente con el NIF mal: sale el aviso con su mensaje completo, sin cortarse, con el color del tema y el icono de la aplicación en la ventana.
- [x] 7.7 Borrar un cliente: la confirmación sale con los botones **Aceptar / Cancelar**.
- [x] 7.8 Doble clic en una factura del Histórico: se abre en el Editor. Desde el Editor, «Versiones» y doble clic en una versión: se abre esa versión.
- [x] 7.9 Abrir «Generar mensuales» desde el menú y desde el Histórico: se abre la ventana con sus datos cargados.
- [x] 7.10 Crear una empresa nueva y entrar: Configuración con la barra bloqueada salvo Salir.
- [x] 7.11 Editar una factura sin guardar y pulsar la X: pregunta por los cambios; al cancelar, la ventana sigue abierta. Después, «Salir» en la barra y confirmar: la aplicación se cierra del todo (se puede volver a abrir sin el aviso de instancia única).
- [x] 7.12 Cerrar la ventana de arranque con la X sin entrar: la aplicación se cierra.
