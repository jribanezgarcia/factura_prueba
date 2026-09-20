## Why

El esqueleto de la aplicación no se parece al de Biblioteca8 y usa piezas difíciles de defender en 1º de DAM:

- `Launcher` y `Main extends Application` se reparten el arranque; `Main` crea **dos** `Navegador` (uno sin modelo) y recibe avisos con `Consumer` (`setOnEntrar`, `setOnVistaCambio`).
- Cada pantalla recibe el modelo y el navegador por `setModelo` / `setNavegador` y carga sus datos en `alIniciar()`, un método inventado en lugar del `initialize()` de JavaFX.
- `Navegador.mostrar` es un método **genérico** (`<T extends Vista> T`), y la interfaz de las pantallas se llama `Vista`, que en Biblioteca8 es la clase singleton.
- Del arranque (760x520) al menú (1024x768) se **oculta y se vuelve a mostrar la misma ventana**, y por eso hace falta `Platform.setImplicitExit(false)`.
- Los FXML se cargan con la ruta completa escrita a mano; la barra de navegación se construye en Java; y `Dialogos` tiene un mecanismo de sustitución para los tests (`MostradorDialogos`, `DialogosReales`, `setImpl`) con nombres distintos a los de clase.

Este es el primer módulo del replanteo: deja el esqueleto tal como manda `AGENTS.md`, para que los módulos siguientes (Clientes, Empresas, Facturas…) se apoyen en él.

## What Changes

- **Arranque como Biblioteca8**: `AppCaboFactu.main` crea `Modelo`, `Vista.getInstancia()` y `Controlador(modelo, vista)` y llama a `controlador.comenzar()`. `LanzadorVentanaPrincipal extends Application` abre la ventana de arranque.
- **`Controlador`** (paquete nuevo `cabofactu.controlador`): `comenzar()`, `terminar()`, `prepararDatos()`, `cargarDemostracion()` y, de momento, `getModelo()`.
- **`Vista` pasa a ser la clase singleton** que guarda el controlador, la ventana y la pantalla actual, y cambia de pantalla con `mostrar("Clientes.fxml")`. **`Navegador` desaparece.**
- **`Pantalla`**: la interfaz de las pantallas (antes `vista.Vista`), con `alMostrar()`, `puedeCerrar()` y `alCerrar()`. Desaparecen `setModelo`, `setNavegador` y `alIniciar`.
- **Las pantallas** pasan a `initialize()` de JavaFX y dejan en `alMostrar()` lo que necesita la ventana (atajos de teclado y foco).
- **El arranque tiene su propia ventana**: al entrar se abre la ventana principal y se cierra la del arranque. Desaparecen `stage.hide()`, `stage.show()` y `Platform.setImplicitExit(false)`.
- **FXML con `LocalizadorRecursos`**: se escribe solo el nombre del fichero.
- **Barra de navegación en FXML** (`BarraNavegacion.fxml` + su controlador), incluida con `<fx:include>`.
- **`Dialogos` vuelve a ser la clase de clase**: `mostrarDialogoError`, `mostrarDialogoInformacion`, `mostrarDialogoAdvertencia`, `mostrarDialogoConfirmacion` (Aceptar / Cancelar) y `mostrarDialogoCambiosSinGuardar`, conservando el tema, el icono de la ventana y el arreglo de los mensajes que se cortaban. Desaparecen `MostradorDialogos`, `DialogosReales` y `setImpl`.
- **Tests**: se quedan los del negocio y uno solo de pantallas, el que carga cada FXML.
- **`ConfiguracionVentana.para`** devuelve `null` en lugar de `Optional`, y `Microinteracciones` se sustituye por CSS.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Ventana» pasa a abrir la ventana principal al entrar y cerrar la del arranque; «Identidad de la aplicación en la interfaz» saca el arranque de las pantallas de la ventana principal.

## A qué afecta

- **Nuevo**: `AppCaboFactu`, `controlador/Controlador`, `vista/Vista` (clase), `vista/Pantalla`, `vista/LanzadorVentanaPrincipal`, `vista/recursos/LocalizadorRecursos`, `vista/controlador/BarraNavegacionController`, `vista/recursos/BarraNavegacion.fxml`.
- **Se borran**: `Launcher`, `Main`, `vista/Navegador`, la interfaz `vista/Vista`, `vista/utilidades/BarraNavegacion`, `vista/utilidades/Microinteracciones`, `vista/utilidades/MostradorDialogos`, `vista/utilidades/DialogosReales`.
- **Cambian**: `Modelo`, `vista/ConfiguracionVentana`, `vista/utilidades/Dialogos`, las 9 pantallas de `vista/controlador`, los 6 FXML con barra, `temas/base.css`, `pom.xml` y `AGENTS.md`.
- **Tests**: se borran los 11 de pantalla y `VistaPrueba`; se adaptan `PruebasJavaFx` y `CargaPantallasTest`.
- **Queda fuera** (módulos siguientes): el negocio sigue con sus DAO y sus excepciones propias; las clases de datos siguen sin validar; sigue habiendo versiones de factura; `Botones.igualarGrupos` se mantiene hasta que cada pantalla fije los anchos en su FXML.
