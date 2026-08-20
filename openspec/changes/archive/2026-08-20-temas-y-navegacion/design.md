## Context

La aplicación JavaFX usa FXML por vistas, un `Navegador` que carga cada vista en la misma `Scene`/`Stage` y una capa de servicios con preferencias persistidas en SQLite (clave/valor). El cambio introduce apariencia configurable, navegación superior y cabecera de empresa sobre esa base existente, sin alterar el modelo de datos ni los servicios de negocio.

Ver `proposal.md` para la motivación y `specs/invoicing/spec.md` para los requisitos.

## Goals / Non-Goals

**Goals:**

- Sistema de temas CSS (base + color) aplicable a toda la interfaz y persistente entre sesiones.
- Barra de navegación superior con iconos en todas las vistas salvo el menú principal.
- Cabecera de empresa (logo/nombre/NIF) en menú principal y editor.
- Resumen del editor desglosado en etiquetas separadas (base, IVA, total).
- Confirmación al cerrar la ventana.

**Non-Goals:**

- Crear temas adicionales ni un editor de temas en tiempo de ejecución.
- Cambiar el modelo de datos, las series, el versionado o la exportación a PDF.
- El desglose por tipo de IVA del editor: se conserva solo en el PDF (ver spec `IVA`).

## Decisions

**Temas como CSS estático por archivo (`ThemeManager`).**
Cada tema es un fichero `tema-<nombre>.css` con variables de color, aplicado siempre junto a `base.css` (estructura común). `ThemeManager` guarda el catálogo de temas, aplica `base + tema` a la `Scene` (reemplazando las hojas anteriores) y recuerda el activo en la tabla de preferencias con la clave `tema` (valor por defecto `biblioteca8`).
- Alternativa descartada: temas por JavaFX Look and Feel o CSS dinámico generado. Un CSS por tema es más sencillo, mantenible y suficiente para la V1.

**Aplicación del tema centralizada en `Navegador`.**
Al cargar cada escena, `Navegador.mostrar(...)` invoca `ThemeManager.aplicar(scene, servicios)`, que lee la preferencia guardada y aplica el tema. Así no hace falta que cada controlador gestione el tema; solo Configuración, que permite cambiarlo al vuelo.

**Cambio de tema al vuelo desde Configuración.**
`ConfiguracionController` añade un `ComboBox` con los temas (etiquetas humanas vía `ThemeManager.etiqueta`). Al cambiar la selección se aplica `ThemeManager.seleccionar(scene, tema)` de inmediato y, al pulsar Guardar, se persiste con `ThemeManager.guardar(servicios)`.

**Barra de navegación reutilizable (`BarraNavegacion`).**
Clase estática que construye un `HBox` con botones de icono SVG (tooltip + clase CSS `nav-button`) y los accesos a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Cada controlador añade la barra a un contenedor `HBox fx:id="barraNavegacion"` del FXML marcando la pantalla actual (borde inferior activo). No aparece en el menú principal.
- Alternativa descartada: incluir la barra directamente en cada FXML. Hacerlo en Java evita repetir FXML y centraliza los iconos.

**Cabecera de empresa.**
`MenuController` carga la `Empresa` de configuración y muestra nombre, `NIF nnn...` y logo (si existe) en el menú principal. `EditorController` muestra el logo en la cabecera del editor. Ambos reutilizan `Empresa.getLogoPath()` ya persistido.

**Resumen del editor con etiquetas separadas.**
`EditorController.actualizarResumen()` deja de componer un texto en bloque y asigna tres etiquetas (`lblBaseTotal`, `lblIvaTotal`, `lblTotal`) con `Formatos.moneda(...)`, agrupadas en una caja de totales. El desglose por tipo de IVA sigue viviendo en el PDF (`PdfService.bloqueTotales`).

**Confirmación de salida en `Main`.**
El `setOnCloseRequest` de la ventana principal pide confirmación ("¿Seguro que deseas salir de la aplicación?") tras comprobar que la vista actual permite cerrarse (`Vista.puedeCerrar()`, que gestiona los cambios sin guardar), y antes de guardar preferencias de ventana y liberar el lock de instancia única.

## Risks / Trade-offs

- El CSS de tema depende de nombres de clase definidos en `base.css` y en los FXML; un tema nuevo que no cubra una clase hereda el estilo base → Mantener el catálogo y el `base.css` en un único sitio (`themes/`).
- Añadir una vista nueva exige recordar incluir la barra de navegación y un `HBox fx:id="barraNavegacion"` → Documentarlo en el código/traspaso; la barra es opcional en el menú principal.
- El resumen del editor ya no muestra el desglose por tipo de IVA; si un usuario lo necesita en pantalla habría que reincorporarlo en una vista/spec posterior → Registrado en la spec (desglose queda en PDF).
- Los estilos en línea antiguos de algunos FXML (p. ej. `style="..."`) coexisten con el CSS; al migrar más vistas conviene moverlos a `base.css` → Migración gradual sin cambios de comportamiento.