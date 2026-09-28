## Situación de partida

- **Menú principal** (`MenuPrincipal.fxml`):
  - cada opción es un `Button` con la clase `opcion-menu` y dentro, como `graphic`, un icono (`SVGPath` con clase `icono` en una `caja-icono-menu`), el nombre (`nombre`) y la descripción (`descripcion`);
  - en `base.css`, `.opcion-menu` solo fija radio, relleno, cursor, alineación y espacio, y `.opcion-menu:hover` escala el botón entero un 2 %;
  - cada tema pinta `.opcion-menu:hover` con un fondo;
  - como nada quita el fondo ni el borde de `Button`, se ve el botón estándar de JavaFX (borde y degradado). Pasa desde el cambio de `menu-item` a `opcion-menu` (commit `708ce66`, 26/09).
- **Configuración** (`Configuracion.fxml` y `ConfiguracionController`, unas 825 líneas):
  - barra lateral `lista-secciones` de 200 px con dos grupos (CONFIGURACIÓN GENERAL: Empresa, Cabecera y pie, PDF y apariencia; CATÁLOGOS: IVA, Retenciones, Series);
  - los `ToggleButton` se muestran con `mostrarSeccion(seccion, boton, conGuardar)`;
  - las tarjetas de sección no tienen título;
  - el logo (`txtLogoPath`, `seleccionarLogo`) está en Cabecera y pie; el color del PDF, las carpetas y el tema, en PDF y apariencia.
- **Propuesta de Figma** (`prototipos/figma/CaboFactu Desktop Billing App.make`, fuera de git, revisada por la sesión principal):
  - barra lateral de 200 px con icono de 16 px y nombre;
  - la sección activa con fondo azul claro, raya de 3 px a la izquierda y texto de acento;
  - cada tarjeta con su título en el color de acento.
  - Sus secciones no coinciden con las de la aplicación: solo se toma el estilo.

## Objetivos y lo que queda fuera

**Objetivo**:
- que el menú principal vuelva a verse limpio (solo icono y texto);
- que Configuración se entienda sola: cada sección con una sola cosa, un nombre profesional, un icono y una frase que diga para qué sirve.

**Fuera**:
- secciones de Figma que la aplicación no tiene;
- cambios en cómo se guarda la configuración;
- el paquete `pdf`;
- el editor, el histórico y las demás pantallas (salvo los «...» de sus botones).

---

## D1. Decisiones tomadas con el alumno

| # | Tema | Decisión |
|---|---|---|
| 1 | Menú principal, al pasar el ratón | Sin fondo ni borde nunca; el icono crece un poco y el nombre se resalta en el acento, subrayado |
| 2 | Estructura de Configuración | 4 grupos y 8 secciones (D3) |
| 3 | Nombres de grupos | DATOS DE LA EMPRESA · FISCALIDAD · FACTURA EN PDF · PREFERENCIAS |
| 4 | Cabecera de cada sección | Icono, título (= nombre de la lista) y una frase de ayuda |
| 5 | Iconos | 16 px, Material, en la barra lateral y junto al título; gris y de acento en la sección elegida |
| 6 | Sección elegida | Fondo suave, raya de acento a la izquierda y color de acento. **Sin negrita**: se respeta la norma de la especificación |
| 7 | Vista previa del logotipo | El mismo recuadro del menú principal (280 × 100, con `LogoMarco`) |
| 8 | Botón nuevo | «Quitar logotipo» |
| 9 | Textos | Los de D4, elegidos uno a uno |
| 10 | Puntos suspensivos | «…» (un carácter) en todos los botones y textos de ayuda que llevan «...» |

## D2. Menú principal

- **`base.css`**:
  - `.opcion-menu` suma `-fx-background-color: transparent; -fx-background-insets: 0; -fx-border-color: transparent; -fx-border-width: 0;`;
  - `.opcion-menu:focused` también sin fondo ni borde, para que el foco del teclado no dibuje un recuadro;
  - `.opcion-menu` sale de la regla `.opcion-menu:hover, .primary-button:hover` (que escala el botón entero); `.primary-button:hover` se queda;
  - `.opcion-menu:hover .icono, .opcion-menu:focused .icono`: escala 1,5 (hoy 1,35: un 10 % más);
  - `.opcion-menu:hover .nombre, .opcion-menu:focused .nombre { -fx-underline: true; }`.
- **Cada `tema-*.css`**:
  - fuera la regla `.opcion-menu:hover { -fx-background-color: ... }`;
  - regla nueva para `.opcion-menu:hover .nombre, .opcion-menu:focused .nombre` (`-fx-text-fill`) y `.opcion-menu:hover .icono, .opcion-menu:focused .icono` (`-fx-fill`) con el acento **más intenso**: en los temas claros (biblioteca8, esmeralda, terracota, sakura) `derive(-fx-accent, -25%)`; en los oscuros (omarchy, negro-dorado, neon) `derive(-fx-accent, 30%)`, para que gane contraste sobre el fondo oscuro. Se comprueba a mano en los siete.
- **Prueba nueva** en `PantallaMenuPrincipalTest`: cada botón con clase `opcion-menu` no pinta nada, es decir, su `getBackground()` es `null` o todos sus rellenos son transparentes, y su `getBorder()` es `null` o todos sus trazos son transparentes. Es la prueba que habría detectado el fallo del 26/09. Se comprueba que falla si se quita la regla nueva de `base.css`.

## D3. Configuración: estructura

Barra lateral, en este orden (los grupos son `Label` con clase `grupo-secciones`, como hoy):

| Grupo | Sección (`ToggleButton`) | `fx:id` botón / panel / método | Contenido | Guardar |
|---|---|---|---|---|
| DATOS DE LA EMPRESA | Datos fiscales | `btnDatosFiscales` / `seccionDatosFiscales` / `verDatosFiscales` | Los campos de la empresa de hoy y `lblDatosPendientes` | Sí |
| | Logotipo | `btnLogotipo` / `seccionLogotipo` / `verLogotipo` | Ruta del logo, «Elegir imagen…», «Quitar logotipo» y vista previa | Sí |
| FISCALIDAD | Tipos de IVA | `btnIva` / `seccionIva` / `verIva` | Tabla de IVA | No |
| | Retenciones IRPF | `btnRetenciones` / `seccionRetenciones` / `verRetenciones` | Tabla de retenciones | No |
| | Series de numeración | `btnSeries` / `seccionSeries` / `verSeries` | Tabla de series | No |
| FACTURA EN PDF | Diseño del PDF | `btnDisenoPdf` / `seccionDisenoPdf` / `verDisenoPdf` | Modo de cabecera, pie legal, color de acento y su explicación, vista previa de la cabecera | Sí |
| PREFERENCIAS | Apariencia | `btnApariencia` / `seccionApariencia` / `verApariencia` | Tema | Sí |
| | Carpetas | `btnCarpetas` / `seccionCarpetas` / `verCarpetas` | Carpeta de los PDF con «Elegir carpeta…» y última carpeta usada | Sí |

- Se reutiliza `mostrarSeccion(seccion, boton, conGuardar)`. Con la empresa bloqueada (datos pendientes) se abre **Datos fiscales**, como hoy Empresa.
- **Cabecera de cada sección**, primer hijo de su tarjeta: un `VBox` con clase `cabecera-seccion` que contiene:
  - un `HBox` con el icono (misma caja y trazado que en la barra lateral, clase `icono-seccion`) y un `Label` con clase `titulo-seccion`;
  - un `Label` con clase `ayuda-seccion` y `wrapText="true"`.
  - Una raya fina debajo (borde inferior de `cabecera-seccion`).
- **Vista previa del logotipo**: un `StackPane` `logoPrevia` de 280 × 100 fijo, con la clase `menu-logo-box` (el mismo recuadro del menú) y un `ImageView` dentro, como en `MenuPrincipal.fxml`.
  - Se repinta cuando cambia `txtLogoPath`: si hay ruta y la imagen carga, `ImageView` con la imagen y `LogoMarco.aplicar(logoPrevia, imagen)`; si no, `ImageView` vacío y `LogoMarco.limpiar(logoPrevia)`.
  - Un método privado en el controlador, como en el menú.
- **«Quitar logotipo»**: `@FXML void quitarLogotipo(ActionEvent event)` deja `txtLogoPath` vacío. No cambia el modo de cabecera: sin logo, el PDF ya usa la cabecera de texto (`GeneradorPdf.cargarLogo` devuelve `null`). La vista previa de la cabecera se repinta sola, porque ya escucha `txtLogoPath`.
- La **vista previa de la cabecera** (`PreviaCabecera`) sigue igual; solo cambia de sección.
- **Cabe a 1024×768**: Diseño del PDF junta lo que hoy son dos secciones. Si con la cabecera nueva no cabe sin desplazarse (lo dice `TextosCompletosTest` y se mira a mano), el pie legal pasa a `prefRowCount="2"` y, si aun así no cabe, se reduce el alto de la vista previa. Se apunta en `tasks.md` qué hizo falta.

## D4. Textos

**Grupos y secciones, con su frase de ayuda:**

| Grupo | Sección | Frase de ayuda |
|---|---|---|
| DATOS DE LA EMPRESA | Datos fiscales | Nombre, NIF, domicilio y contacto que figurarán en cada factura emitida. |
| | Logotipo | La imagen de tu empresa en el menú principal, el editor y la cabecera del PDF. |
| FISCALIDAD | Tipos de IVA | Tipos impositivos disponibles al facturar, incluidos los exentos y los suplidos. |
| | Retenciones IRPF | Porcentajes de retención para profesionales y actividades sujetas a IRPF. |
| | Series de numeración | Las series con las que numeras tus facturas: ordinarias, rectificativas y otras. |
| FACTURA EN PDF | Diseño del PDF | Personaliza la cabecera, el pie legal y el color de tus facturas en PDF. |
| PREFERENCIAS | Apariencia | El tema de colores de la aplicación. Cada empresa guarda el suyo. |
| | Carpetas | Ubicación de los documentos que genera la aplicación. |

**Etiquetas** (hoy → nuevo):

| Sección | Hoy | Nuevo |
|---|---|---|
| Datos fiscales | Nombre / razón social * | Nombre o razón social * |
| | Dirección * | Domicilio fiscal * |
| | CP * | Código postal * |
| | Email * | Correo electrónico * |
| | (NIF *, Actividad, Localidad *, Provincia *, Teléfono *) | sin cambios |
| | Aviso: «Para empezar a usar el programa completa los datos de tu empresa. Rellena los campos marcados con * y pulsa «Guardar configuración».» | «Completa los datos fiscales de tu empresa para empezar a facturar. Los campos con * son obligatorios.» |
| Logotipo | Logo | Imagen del logotipo |
| | Elegir imagen... | Elegir imagen… |
| | — | Quitar logotipo (botón nuevo) |
| Diseño del PDF | Modo de cabecera | Cabecera |
| | Texto con datos de empresa | Datos de la empresa en texto |
| | Logo / imagen | Logotipo |
| | Texto legal del pie | Pie legal |
| | Color del PDF | Color de acento |
| | Color de acento: cabeceras de tarjetas, totales y pie legal; también SERIE/Nº y FECHA | Se usa en las cabeceras de las tarjetas, los totales, el pie legal, el número y la fecha. |
| | Vista aproximada de la cabecera del PDF. | Vista previa de la cabecera |
| Apariencia | Tema de la aplicación | Tema |
| Carpetas | Carpeta automática de almacenamiento | Guardar los PDF en |
| | Elegir carpeta... | Elegir carpeta… |
| | Última carpeta de exportación | Última carpeta usada |
| Tipos de IVA | columna Tipo | Porcentaje |
| | columna Motivo exención | Motivo de exención |
| Series | columna «Siguiente (año)» (`ConfiguracionController`) | «Siguiente n.º (año)» |
| Barra inferior | Guardar configuración | Guardar cambios |

- **Se quedan**: «Cambiar de empresa», «Volver», los avisos «Configuración guardada.» y «Datos de la empresa completados.», y los mensajes de error de los campos, salvo que citen una etiqueta cambiada. Se buscan con `grep` en `ConfiguracionController` y en las clases de datos («Dirección», «CP», «Email»…); si un mensaje nombra el campo, se usa el nombre nuevo.
- **«...» → «…»** en: `Arranque.fxml` («Nueva…»), `Clientes.fxml` (texto de ayuda «Buscar por nombre o NIF…»), `CopiaSeguridad.fxml` («Crear copia…», «Elegir copia…») y los dos de Configuración. Después, `grep -rn '\.\.\."' src/main` no devuelve nada.

## D5. Iconos (Material, rejilla 24 × 24)

Comprobados dibujándolos por la sesión principal:

| Sección | Icono | `content` |
|---|---|---|
| Datos fiscales | business | `M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z` |
| Logotipo | image | `M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z` |
| Tipos de IVA | percent | `M7.5 11C9.43 11 11 9.43 11 7.5S9.43 4 7.5 4 4 5.57 4 7.5 5.57 11 7.5 11zm0-5C8.33 6 9 6.67 9 7.5S8.33 9 7.5 9 6 8.33 6 7.5 6.67 6 7.5 6zM4.0025 18.5832 18.5832 4.0025 19.9975 5.4168 5.4168 19.9975zM16.5 13c-1.93 0-3.5 1.57-3.5 3.5s1.57 3.5 3.5 3.5 3.5-1.57 3.5-3.5-1.57-3.5-3.5-3.5zm0 5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z` |
| Retenciones IRPF | account_balance | `M4 10v7h3v-7H4zm6 0v7h3v-7h-3zM2 22h19v-3H2v3zm14-12v7h3v-7h-3zm-4.5-9L2 6v2h19V6l-9.5-5z` |
| Series de numeración | format_list_numbered | `M2 17h2v.5H3v1h1v.5H2v1h3v-4H2v1zm1-9h1V4H2v1h1v3zm-1 3h1.8L2 13.1v.9h3v-1H3.2L5 10.9V10H2v1zm5-6v2h14V5H7zm0 14h14v-2H7v2zm0-6h14v-2H7v2z` |
| Diseño del PDF | picture_as_pdf | `M20 2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-8.5 7.5c0 .83-.67 1.5-1.5 1.5H9v2H7.5V7H10c.83 0 1.5.67 1.5 1.5v1zm5 2c0 .83-.67 1.5-1.5 1.5h-2.5V7H15c.83 0 1.5.67 1.5 1.5v3zm4-3H19v1h1.5V11H19v2h-1.5V7h3v1.5zM9 9.5h1v-1H9v1zM4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm10 5.5h1v-3h-1v3z` |
| Apariencia | palette | `M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c2.76 0 5-2.24 5-5 0-4.42-4.03-8-9-8zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 1.5-.67 1.5-1.5 1.5z` |
| Carpetas | folder | `M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z` |

- **En la barra lateral**: `graphic` del `ToggleButton` con una caja fija de 16 × 16 (`StackPane`, clase `caja-icono-seccion`) y dentro el `SVGPath` con clase `icono-seccion`. El trazado de 24 se escala a 16 con `-fx-scale-x/y: 0.67`, y la caja fija mantiene alineados los textos, como en la barra de navegación. `graphicTextGap` de unos 10 px.
- **En el título de la tarjeta**: la misma caja y el mismo trazado.
- **Colores**, en `base.css` y con las variables del tema, nunca con colores fijos (trampa de `ESTADO.md`):
  - `.icono-seccion { -fx-fill: derive(-fx-text-background-color, 35%); }` (gris del texto);
  - `.lista-secciones .toggle-button:selected .icono-seccion` y `.cabecera-seccion .icono-seccion { -fx-fill: -fx-accent; }`;
  - `.titulo-seccion`: tamaño 15 px, color `-fx-accent`, sin negrita;
  - `.ayuda-seccion`: clase `muted`, 12 px.
- **Sección elegida**, en `.lista-secciones .toggle-button:selected`: se conservan el fondo `derive(-fx-accent, 84%)` y el texto `derive(-fx-accent, -35%)`, y se añade la raya: `-fx-border-color: transparent transparent transparent -fx-accent; -fx-border-width: 0 0 0 3;`. Sin negrita.
- **Se comprueba a mano en los siete temas** que iconos, títulos y la sección elegida se leen bien.

## D6. Especificación

Deltas en `specs/` de este change:

- `invoicing`:
  - «Configuración»: sección Datos fiscales, Series de numeración y la etiqueta «Nombre o razón social»;
  - «Configuración organizada por secciones»: grupos, secciones, cometido de cada una, icono, título y ayuda, sin negrita, guardado. Escenarios nuevos «Cada sección se presenta sola» y «Quitar el logotipo»;
  - «Datos obligatorios de la empresa»: sección Datos fiscales y botón «Guardar cambios».
- `pdf-rendering`:
  - «Vista previa de la cabecera del PDF»: en la sección Diseño del PDF, sin los campos de tamaño (ya no existen) ni el aviso de «aproximada»;
  - «Aviso de logo de baja resolución»: en la sección Logotipo.

## D7. Pruebas

- Se adaptan a los textos nuevos: `PantallaConfiguracionTest`, `PantallaMenuPrincipalTest` («Guardar cambios»), `PantallaSeriesTest` («Siguiente n.º (año)»), `PantallaArranqueTest` («Nueva…») y las que salgan con `grep` de los textos viejos en `src/test`.
- `TextosCompletosTest` recorre las **ocho** secciones.
- **Nuevas en `PantallaConfiguracionTest`**:
  - la barra lateral muestra los cuatro grupos y las ocho secciones en orden, cada una con su icono (un `SVGPath` en su `graphic`);
  - al abrir cada sección se ven su título y su frase de ayuda (D4);
  - Diseño del PDF contiene la vista previa de la cabecera y no contiene el tema;
  - «Quitar logotipo» deja la ruta vacía y la vista previa del logotipo sin imagen.
- **Nueva en `PantallaMenuPrincipalTest`**: la de D2.

## D8. Documentación

- **`docs/diseno.md`**, «Las rondas»: **ronda 8**, «Configuración y menú principal (28/09)».
  - La propuesta hecha con **Figma** (Figma Make), en `prototipos/figma/`, fuera de git.
  - Lo que se tomó: iconos pequeños en la barra lateral, título en cada sección y la sección elegida con raya de acento.
  - Lo que no: sus secciones y la negrita, por la norma de la especificación.
  - La reorganización en cuatro grupos y ocho secciones, con los textos elegidos uno a uno.
  - El arreglo del menú principal y su causa (el cambio de `menu-item` a `opcion-menu`).
  - Sin imagen de la maqueta: enlace a la captura nueva `capturas/configuracion.png`.
- **Capturas** (sesión principal, con Computer use, tema biblioteca8 y la demo): rehacer `docs/capturas/menu.png` y añadir `docs/capturas/configuracion.png` (sección Datos fiscales). En el `README.md`, `configuracion.png` en la tabla de capturas.
- **`ESTADO.md`**:
  - al aplicar, «En curso»;
  - al archivar, la trampa: «Un botón con clase propia (`opcion-menu`) se ve como botón estándar si nadie le quita fondo y borde: antes lo hacía por casualidad el estilo de `menu-item` de JavaFX. Lo vigila `PantallaMenuPrincipalTest`».

## Riesgos y renuncias

- **Diseño del PDF junta dos secciones** y puede no caber a 1024×768: plan en D3.
- **Colores de hover y selección en los temas oscuros**: `derive` con porcentajes; si alguno no contrasta, se ajusta en su `tema-*.css` y se apunta.
- **Pruebas que buscan textos por su valor**: al cambiar textos fallarán varias; se adaptan sin cambiar lo que comprueban.
