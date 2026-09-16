## Why

La estructura de arranque y de pantallas no se parece a la de Biblioteca8, el proyecto de referencia del alumno, y usa piezas difíciles de explicar en 1º de DAM:

- `Launcher` y `Main extends Application` reparten el arranque; `Main` crea dos `Navegador` (uno sin modelo) y recibe avisos por `Consumer` (`setOnEntrar`, `setOnVistaCambio`).
- Cada pantalla recibe el modelo y el navegador por `setModelo` / `setNavegador` y carga sus datos en `alIniciar()`, un método propio en lugar del `initialize()` de JavaFX.
- `Navegador.mostrar` es un método genérico (`<T extends Vista> T`) y la interfaz de las pantallas se llama `Vista`, el mismo nombre que en Biblioteca8 tiene la clase singleton.
- El paso del arranque (760x520) al menú (1024x768) oculta y vuelve a mostrar la misma ventana, y para eso necesita `Platform.setImplicitExit(false)`.
- La barra de navegación se construye en Java (`BarraNavegacion.crear(nav, "x")`) en lugar de FXML.

## What Changes

- **Como Biblioteca8**: `AppCaboFactu.main` crea `Modelo`, `Vista.getInstancia()` y `Controlador(modelo, vista)` y llama a `controlador.comenzar()`. `LanzadorVentanaPrincipal extends Application` abre la ventana de arranque.
- **`Controlador`** (paquete `cabofactu.controlador`): `comenzar()`, `terminar()`, `prepararDatos()` y `getModelo()`. No repite los métodos del negocio.
- **`Vista`** pasa a ser una clase singleton que guarda el `Controlador`, la ventana y la pantalla actual, y cambia de pantalla con `mostrar(fxml)`. **`Navegador` desaparece.**
- **`Pantalla`**: la interfaz de las pantallas (antes `vista.Vista`) con `alMostrar()`, `puedeCerrar()` y `alCerrar()`. Desaparecen `setModelo`, `setNavegador` y `alIniciar`.
- **Pantallas**: cargan sus datos en `initialize()` y escriben la llamada completa `Vista.getInstancia().getControlador().getModelo()...`; los atajos de teclado y el foco inicial van en `alMostrar()`.
- **Arranque en ventana propia**, como el Login de Biblioteca8: al entrar se abre la ventana principal con la primera pantalla y se cierra la de arranque. Desaparecen `stage.hide()` / `stage.show()` y `Platform.setImplicitExit(false)`.
- **Barra de navegación en FXML**: `BarraNavegacion.fxml` + `BarraNavegacionController`, incluida con `<fx:include>`; la pantalla le pide `marcarActivo(...)` y Configuración `bloquearSalvoSalir()`.
- **Errores al arrancar** (carpeta de datos, instancia única, demostración) se comprueban en `LanzadorVentanaPrincipal.start`, cuando ya se pueden mostrar avisos.
- **`Modelo`** deja de abrir la conexión en su constructor: se crea una vez al arrancar, antes de elegir empresa.
- **`ConfiguracionVentana.para`** devuelve `null` en lugar de `Optional`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Ventana» pasa a abrir la ventana principal al entrar y cerrar la de arranque; el requisito «Identidad de la aplicación en la interfaz» saca el arranque de las pantallas de la ventana principal.

## Impact

- Nuevo: `cabofactu/AppCaboFactu`, `cabofactu/controlador/Controlador`, `cabofactu/vista/Vista` (clase), `cabofactu/vista/Pantalla`, `cabofactu/vista/LanzadorVentanaPrincipal`, `cabofactu/vista/controlador/BarraNavegacionController`, `vista/recursos/BarraNavegacion.fxml`.
- Se borran: `cabofactu/Launcher`, `cabofactu/Main`, `cabofactu/vista/Navegador`, la interfaz `cabofactu/vista/Vista`, `cabofactu/vista/utilidades/BarraNavegacion`.
- Cambian: `modelo/Modelo`, `vista/ConfiguracionVentana`, las 9 pantallas de `vista/controlador`, los FXML con barra (Clientes, Configuracion, CopiaSeguridad, Editor, Historico, Versiones), `temas/base.css`, `pom.xml` y `AGENTS.md`.
- Documentación: `docs/tecnico.md` (diagrama de capas y paquetes) y `README.md` (diagrama).
- Tests: `PruebasJavaFx`, `VistaPrueba`, `CargaPantallasTest`, `VentanaTransicionTest`, `NavegacionCambiosSinGuardarTest` y los 11 tests de `vista/controlador` que usan `Navegador`.
- Fuera (changes posteriores): `Empresas`, `Sesion`, `PreferenciasGlobales` y `Calculos` siguen siendo `static` (`negocio-dentro-del-modelo`); quitar la pantalla «Versiones» (apuntado para el futuro); ternarios, streams, clases anónimas, `Task` y `runLater` que ya existen fuera del código nuevo.
