> Aplicar **después** de archivar `crear-tablas-sin-versiones`. Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. Código, mensajes y Javadoc exactos en `design.md`. Seguir `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: sin ternarios, sin `var`, sin streams ni `::`, sin `Optional`, siempre `import`, Javadoc corto en primera persona del plural. En las líneas donde solo se sustituye `modelo.` o `nav.` por la llamada completa no se arregla nada más.

## 1. Arranque como Biblioteca8

- [ ] 1.1 Renombrar con `git mv` la interfaz `vista/Vista.java` a `vista/Pantalla.java` y dejarla como en `design.md - D3`.
- [ ] 1.2 Crear `controlador/Controlador.java`. Ver `design.md - D2`.
- [ ] 1.3 Crear la clase `vista/Vista.java` con `getInstancia`, `setControlador`, `getControlador`, `getVentana`, `setVentana`, `getPantallaActual`, `comenzar`, `mostrarInicio`, `mostrar`, `abrirVentanaPrincipal`, `pedirCierre`, `puedeSalir` y `salir`. Ver `design.md - D4`, `D5` y `D6`.
- [ ] 1.4 Crear `vista/LanzadorVentanaPrincipal.java`. Ver `design.md - D6`.
- [ ] 1.5 Crear `AppCaboFactu.java` y cambiar `mainClass` en `pom.xml`. Ver `design.md - D1`.
- [ ] 1.6 Borrar `Launcher.java`, `Main.java` y `vista/Navegador.java`.
- [ ] 1.7 En `modelo/Modelo.java`, quitar la conexión del constructor y actualizar el Javadoc. Ver `design.md - D7`.
- [ ] 1.8 En `vista/ConfiguracionVentana.java`, `para` devuelve `null` en lugar de `Optional`. Ver `design.md - D8`.

## 2. Barra de navegación en FXML

- [ ] 2.1 Crear `src/main/resources/cabofactu/vista/recursos/BarraNavegacion.fxml` con los 7 botones de la tabla de `design.md - D10`, copiando las rutas SVG de `vista/utilidades/BarraNavegacion.java`.
- [ ] 2.2 Crear `vista/controlador/BarraNavegacionController.java` con `irInicio`, `irNueva`, `irHistorico`, `irClientes`, `irConfiguracion`, `irCopias`, `salir`, `marcarActivo` y `bloquearSalvoSalir`. Ver `design.md - D10`.
- [ ] 2.3 En `Clientes.fxml`, `Configuracion.fxml`, `CopiaSeguridad.fxml`, `Editor.fxml`, `Historico.fxml` y `Versiones.fxml`, sustituir el `HBox` `barraNavegacion` por `<fx:include fx:id="barra" source="BarraNavegacion.fxml"/>`.
- [ ] 2.4 En `temas/base.css`, `.nav-bar` con `-fx-padding: 8px 0 8px 0;`. Ver `design.md - D10`.
- [ ] 2.5 Borrar `vista/utilidades/BarraNavegacion.java`.

## 3. Pantallas

- [ ] 3.1 `ArranqueController`: `Pantalla` + `Initializable`, `initialize`, y `entrar()` abre la ventana principal y cierra la de arranque; quitar `onEntrar`/`setOnEntrar`. Ver `design.md - D6` y `D9`.
- [ ] 3.2 `MenuPrincipalController`: puntos 1-6 de `design.md - D9` (atajos y foco a `alMostrar`, `salir()` con `Vista.getInstancia().salir()`, `GenerarFacturasMensualesController.abrir()`).
- [ ] 3.3 `EditorController`: puntos 1-5, 7 y 8 de `design.md - D9` (`atajos()` desde `alMostrar`, `verVersiones` con cast a `VersionesController`).
- [ ] 3.4 `HistoricoController`: puntos 1-5 y 7 de `design.md - D9` (`abrirVersion` con cast a `EditorController`, `generarMensual` con `abrir()`).
- [ ] 3.5 `ClientesController` y `VersionesController`: puntos 1-5 y 7 de `design.md - D9`.
- [ ] 3.6 `ConfiguracionController`: puntos 1-4, 7 y 9 de `design.md - D9` (`barraController.bloquearSalvoSalir()`).
- [ ] 3.7 `CopiaSeguridadController`: puntos 1-4, 7 y 9 de `design.md - D9`.
- [ ] 3.8 `GenerarFacturasMensualesController`: `abrir()` sin parámetros, `initialize(URL, ResourceBundle)` con lo que hacía `alIniciar()`. Ver `design.md - D9`.
- [ ] 3.9 `grep -rn "Navegador\|setModelo\|setNavegador\|alIniciar\|BarraNavegacion\.crear\|implements Vista" src/main`: sin resultados.
- [ ] 3.10 `mvn -q compile` sin errores.

## 4. AGENTS.md

- [ ] 4.1 Actualizar la sección «Arquitectura (MVC como Biblioteca8)». Ver `design.md - D11`.
- [ ] 4.2 Actualizar `docs/tecnico.md` y `README.md`. Ver `design.md - D13`.

## 5. Tests

- [ ] 5.1 En `src/test/java/cabofactu/vista/PruebasJavaFx.java`, añadir `prepararVista(Modelo, Stage)`. Ver `design.md - D12`.
- [ ] 5.2 `VistaPrueba` implementa `Pantalla`; adaptar `NavegacionCambiosSinGuardarTest`.
- [ ] 5.3 Adaptar `CargaPantallasTest`. Ver `design.md - D12`.
- [ ] 5.4 Adaptar `VentanaTransicionTest`, con el test nuevo `arranqueYVentanaPrincipalConSuTamano`. Ver `design.md - D12`.
- [ ] 5.5 Adaptar los tests de `vista/controlador` y `BotonesTest` que usan `Navegador`. Ver `design.md - D12`.
- [ ] 5.6 `grep -rn "Navegador\|setModelo\|alIniciar\|implements Vista" src/test`: sin resultados.
- [ ] 5.7 Con la aplicación cerrada, borrar `target` y `mvn test`: todos en verde.
- [ ] 5.8 En los ficheros nuevos (`AppCaboFactu`, `Controlador`, `Vista`, `Pantalla`, `LanzadorVentanaPrincipal`, `BarraNavegacionController`) y en los métodos `alMostrar`, buscar construcciones prohibidas por `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, `Optional`, nombres completos de clase): ninguna.
- [ ] 5.9 `openspec validate mvc-como-biblioteca8 --strict` sin errores.

## 6. Pruebas manuales

- [ ] 6.1 `mvn javafx:run`: se abre la ventana de arranque de 760x520, centrada, con el aviso de bienvenida si se acaba de cargar la demostración.
- [ ] 6.2 Con la aplicación abierta, ejecutar otra vez `mvn javafx:run`: aviso «La aplicación ya está en ejecución» y la segunda no se abre.
- [ ] 6.3 Pulsar Entrar: se abre la ventana principal a 1024x768, centrada, con el menú (o Configuración si faltan datos), y la ventana de arranque desaparece.
- [ ] 6.4 Recorrer con la barra de arriba Nueva, Histórico, Clientes, Configuración y Copias: cada pantalla marca su botón con la raya de color y la barra mide y se ve igual que antes.
- [ ] 6.5 Atajos: en el menú Ctrl+N y Ctrl+F; en el Editor Ctrl+S, Ctrl+P y Esc; en el Histórico Ctrl+F; en Clientes Esc. El menú abre sin ningún botón resaltado.
- [ ] 6.6 Doble clic en una factura del Histórico: se abre en el Editor. Desde el Editor, «Versiones» y doble clic en una versión: se abre esa versión.
- [ ] 6.7 Abrir «Generar mensuales» desde el menú y desde el Histórico: se abre la ventana con sus datos cargados.
- [ ] 6.8 Crear una empresa nueva y entrar: Configuración con la barra bloqueada salvo Salir.
- [ ] 6.9 Editar una factura sin guardar y pulsar la X: pregunta por los cambios; al cancelar sigue abierta. Pulsar «Salir» en la barra y confirmar: la aplicación se cierra del todo (el proceso termina y se puede volver a abrir sin el aviso de instancia única).
- [ ] 6.10 Cerrar la ventana de arranque con la X sin entrar: la aplicación se cierra.
