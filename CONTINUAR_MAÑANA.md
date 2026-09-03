# Continuacion del proyecto de facturacion

Estado actualizado: 03/09/2026

NOTA: hasta aqui se ha hecho la app con modelos gratuitos de OPENCODE.

Este documento sirve como traspaso para continuar con cualquier IA. El proyecto se esta construyendo con OpenCode/OpenSpec. Antes de tocar codigo, leer:

- `facturacion_openspec_explore.md`
- `openspec/specs/invoicing/spec.md`
- este archivo

## REGLA OBLIGATORIA: usar OpenSpec siempre

Todo trabajo en este proyecto (nuevas funcionalidades, cambios, fixes, redisenos de interfaz, temas, etc.) se realiza SIEMPRE con el flujo OpenSpec a traves de las skills/commands de opencode (`/opsx-*`): primero `/opsx-propose`, despues `/opsx-apply-change`, luego `/opsx-sync-specs` y por ultimo `/opsx-archive-change`.

No se permite:

- tocar codigo ni spec fuera del flujo OpenSpec;
- modificar `openspec/specs/invoicing/spec.md` a mano sin pasar por `/opsx-sync-specs`;
- implementar cambios sin su cambio OpenSpec correspondiente (ni siquiera redisenos "rapidos");

El CLI `openspec` ya esta instalado (version 1.10.0). Cualquier IA que trabaje en este proyecto debe seguir este flujo en todas las sesiones.

## Estado general

Aplicacion JavaFX de facturacion local para Windows.

Stack actual:

- Java 21
- JavaFX/FXML
- Maven
- SQLite/JDBC
- OpenPDF
- JUnit 5
- Arquitectura por capas: `ui`, `service`, `repository`, `model`, `db`, `pdf`, `util`

La especificacion activa esta en:

- `openspec/specs/invoicing/spec.md`

Cambios OpenSpec archivados:

- `openspec/changes/archive/2026-08-21-redesign-pdf-factura`
- `openspec/changes/archive/2026-08-22-exportar-pdf-desde-historico`
- `openspec/changes/archive/2026-08-22-fix-pdf-totales-tarjeta-pago`
- `openspec/changes/archive/2026-08-22-pdf-fidelidad-prototipo`
- `openspec/changes/archive/2026-08-22-pdf-etiquetas-factura-cliente`
- `openspec/changes/archive/2026-08-22-pdf-cp-datos-pago-opcional`
- `openspec/changes/archive/2026-08-22-guardar-version-nueva-edicion`
- `openspec/changes/archive/2026-08-23-ventana-800-responsive`
- `openspec/changes/archive/2026-08-24-formato-numeracion-series`
- `openspec/changes/archive/2026-08-30-multi-empresa-ejercicio-fiscal`
- `openspec/changes/archive/2026-08-31-retencion-irpf`
- `openspec/changes/archive/2026-08-31-facturacion-mensual-cliente`
- `openspec/changes/archive/2026-08-31-redesign-ui-apple`
- `openspec/changes/archive/2026-08-31-fix-ui-spacing`
- `openspec/changes/archive/2026-08-31-window-sizing`
- `openspec/changes/archive/2026-08-31-fix-menu-min-height`
- `openspec/changes/archive/2026-08-31-adjust-menu-editor-sizing`
- `openspec/changes/archive/2026-09-01-fix-reutilizar-numeros-borrados`
- `openspec/changes/archive/2026-09-01-mejorar-menu-principal`
- `openspec/changes/archive/2026-09-01-editor-800x600-scroll`
- `openspec/changes/archive/2026-09-01-uniformizar-ventanas-800x600`
- `openspec/changes/archive/2026-09-01-uniformizar-1024-scroll-editor`
- `openspec/changes/archive/2026-09-01-fix-ventana-1024-transicion-arranque-menu`
- `openspec/changes/archive/2026-09-01-fix-ventana-1024-tras-pulse`
- `openspec/changes/archive/2026-09-01-reforzar-test-ventana`
- `openspec/changes/archive/2026-09-01-fix-ventana-max-heredado-arranque`
- `openspec/changes/archive/2026-09-01-editor-sin-scroll-factura-corta`
- `openspec/changes/archive/2026-09-01-fix-styleclass-separador-fxml`
- `openspec/changes/archive/2026-09-02-configuracion-secciones-laterales`
- `openspec/changes/archive/2026-09-02-ficha-cliente-validada`
- `openspec/changes/archive/2026-09-02-logo-tamano-fijo`
- `openspec/changes/archive/2026-09-02-fix-exportar-pdf-agrupado`
- `openspec/changes/archive/2026-09-02-fix-cancelar-salida`
- `openspec/changes/archive/2026-09-02-logo-relleno-tema`
- `openspec/changes/archive/2026-09-02-pdf-texto-neutro-color-acento`
- `openspec/changes/archive/2026-09-03-fix-crear-empresa-no-cambia-activa`
- `openspec/changes/archive/2026-09-03-restaurar-copia-seguridad`
- `openspec/changes/archive/2026-09-03-fix-restaurar-validacion-y-rollback`

Cambio OpenSpec activo: ninguno (`fix-crear-empresa-no-cambia-activa`, `restaurar-copia-seguridad` y `fix-restaurar-validacion-y-rollback` archivados el 03/09/2026 con delta de specs sincronizadas y validadas).

## Sesion del 31/08/2026 (cerrada y commiteada)

### Change `retencion-irpf` (archivado)

- Tipos de retencion configurables por empresa, con pestana en Configuracion similar a IVA.
- Selector de retencion en el editor de factura, con recalculo en caliente.
- Calculo sobre base bruta: `Total = Base − Descuento + IVA − Retencion`.
- Persistencia del tipo e importe de retencion en cada version de factura.
- Columna de retencion en el Historico.
- Fila de retencion en el resumen del PDF.
- Rectificativas heredan la retencion de la factura origen, editable antes de guardar.
- Snapshot de nombre y porcentaje de retencion en `factura_version` para conservar el historico aunque el tipo maestro cambie o se borre.
- Migraciones `005_retencion_irpf.sql` y `006_retencion_irpf_snapshot.sql`.
- Tests nuevos: `TipoRetencionRepositoryTest`, retencion en `CalculoServiceTest`, retencion en `FacturaServiceTest`. Suite **86/86** en verde.
- Commits `428958c` (implementacion) y `9d77eb5` (sync de spec y archivo en OpenSpec), push realizado.

### Change `facturacion-mensual-cliente` (archivado)

- Dialogo de facturacion mensual accesible desde el menu principal y desde el historico.
- Generacion de facturas mensuales para un unico cliente con seleccion de año, rango de meses, serie de numeracion y dia del mes (fijo, primer dia o ultimo dia).
- Lineas de concepto configurables con opcion de añadir el nombre del mes a la descripcion.
- Seleccion de tipo de IVA y tipo de retencion IRPF aplicados a todas las facturas generadas.
- Deteccion de facturas ya existentes para el mismo cliente, año y mes, con confirmacion antes de generar duplicados.
- Acciones de **Anular** y **Borrar** en el historico, con menu contextual y resumen de resultados.
- Borrado fisico de facturas que registra el numero como disponible en la tabla `numero_disponible` para reutilizacion posterior.
- Exportacion multiple a PDF desde el historico: un PDF por factura o un unico PDF agrupado.
- Migracion `007_numeros_disponibles.sql` y repositorio `NumeroDisponibleRepository`.
- Servicios nuevos/modificados: `FacturacionMensualService`, `NumeroService.proponerNumeros(...)`, `FacturaService.borrarFactura(...)`, `EstadoService.anularFacturas(...)`.
- Tests nuevos/actualizados: `FacturacionMensualServiceTest`, `FacturaServiceTest`, `EstadoServiceTest`, `NumeroServiceTest`, `HistorialServiceTest`. Suite **101/101** en verde.
- Commits `ebbc641` (implementacion) y `9156850` (sync de spec y archivo en OpenSpec), push realizado.

### Change `redesign-ui-apple` (archivado)

- Rediseño estructural de la interfaz inspirado en Ajustes de Apple: tarjetas de seccion, espaciado generoso, esquinas redondeadas y jerarquia tipografica.
- Se mantienen los 7 temas de color existentes (`biblioteca8`, `omarchy`, `esmeralda`, `terracota`, `negro-dorado`, `sakura`, `neon`); el nuevo diseño se adapta a cada paleta.
- Refactor de `base.css` con clases utilitarias `.card`, `.surface`, `.section-title`, `.form-label`, `.toolbar`, `.dialog-card` y estados de hover/focus.
- Ajustes en FXML de `MenuPrincipal`, `Editor`, `Historico`, `Configuracion`, `Clientes`, `GenerarFacturasMensuales`, `Versiones`, `Backup` y `Arranque` para usar las nuevas tarjetas.
- Microinteracciones CSS en botones, campos y navegacion; transiciones JavaFX de escala suave en botones primarios e items del menu (`Microinteracciones`).
- `Navegador` aplica las microinteracciones tras cargar cada vista.
- Se añade `minHeight` a la tabla de lineas del editor para garantizar que los tests de edicion siguen funcionando con el nuevo layout.
- Suite **101/101** en verde.
- Change archivado como `2026-08-31-redesign-ui-apple`.

### Change `fix-ui-spacing` (archivado)

- Correccion de espaciado en las pantallas redisenadas: contenido demasiado pegado al borde de la ventana y a la barra de navegacion.
- Se añade padding de ventana de 16px a los `BorderPane` raiz de `Historico`, `Configuracion`, `Clientes`, `Versiones`, `Backup`, `Editor`, `MenuPrincipal` y `Arranque`.
- Se añade separacion de 12px entre la barra de navegacion y la primera tarjeta de contenido en `Historico`, `Clientes`, `Editor`, `Versiones` y `Backup`.
- Se amplian los paddings de `.card` (20px) y `.zona-contenido` (16px) y se aumenta el margen inferior de `.nav-bar` en `base.css`.
- Se añaden escenarios de margen y separacion respecto al menu en la especificacion del sistema de diseño visual.
- Suite **101/101** en verde.
- Change archivado como `2026-08-31-fix-ui-spacing`.

### Change `window-sizing` (archivado)

- Configuracion de tamaño por vista para evitar que las pantallas se reduzcan mas de lo que su contenido permite.
- Nueva clase `VentanaConfig` con ancho, alto, minimo, maximo y redimensionabilidad por FXML.
- `Navegador` aplica la configuracion de la vista al `Stage` cada vez que cambia de pantalla, redimensionando y centrando la ventana.
- `Arranque` es fijo y no redimensionable (760×520).
- Editor: minimo 1000×760 para que se vean todos los controles.
- Resto de pantallas: `MenuPrincipal` 760×520, `Configuracion` 1000×620, `Historico` 1000×600, `Clientes` 1000×600, `Versiones` 900×500, `Backup` 720×450.
- Diálogo `Generar facturas mensuales`: minimo 920×680.
- Se mantiene el guardado de posicion y tamaño al cerrar; el tamaño se restaura pero las vistas lo sobreescriben al navegar.
- Nuevo test `EditorTamanoMinimoTest` que verifica que el Editor cabe completo en su tamaño minimo.
- Suite **102/102** en verde.
- Change archivado como `2026-08-31-window-sizing`.

### Change `fix-menu-min-height` (archivado)

- Correccion del tamaño minimo del Menu principal, que aparecia cortado tras los nuevos márgenes.
- Medida real del layout con test diagnostico: el contenido necesita al menos 589 px de alto.
- Se establece el tamaño predefinido/minimo del Menu principal en **760×600**.
- Se actualiza `VentanaConfig.MENU` y `MenuPrincipal.fxml`.
- Se actualiza la especificacion del sistema de tamaños de ventana.
- Suite **102/102** en verde.
- Change archivado como `2026-08-31-fix-menu-min-height`.

### Change `adjust-menu-editor-sizing` (archivado)

- Menu principal ajustado a **800×600** con margen inferior de 20 px y tarjetas alineadas arriba.
- Editor de facturas configurado para abrirse **maximizado** por defecto, aprovechando todo el alto de pantalla; la tabla de lineas ya permite scroll vertical.
- `VentanaConfig` añade soporte para flag `maximizado` y aplica `stage.setMaximized(...)` al cargar cada vista.
- Fix posterior: se reordena `VentanaConfig.aplicar` para llamar a `setMaximized` despues de fijar tamaño y centrar, garantizando que el Editor realmente se maximice.
- Test `EditorTamanoMinimoTest` actualizado para desmaximizar el Editor antes de validar su tamaño minimo.
- Se actualiza la especificacion del sistema de tamaños de ventana.
- Suite **102/102** en verde.
- Change archivado como `2026-08-31-adjust-menu-editor-sizing`.

### Change `fix-reutilizar-numeros-borrados` (archivado)

- Corregida la numeración de facturas para reutilizar los huecos libres dejados por facturas borradas.
- `NumeroService.siguienteCorrelativo(...)` consulta primero `numero_disponible` y propone el menor correlativo libre antes de continuar con el siguiente número.
- Se añaden tests para reutilización de números borrados, exclusión de huecos ocupados por activas y prioridad de huecos borrados sobre anulados.
- Se actualiza la especificacion de numeración por series.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-fix-reutilizar-numeros-borrados`.

### Change `mejorar-menu-principal` (archivado)

- Ajustada la pantalla del Menú principal a 800×600 para que la información de la empresa no se corte y se vea margen inferior.
- Tarjeta de empresa ampliada a 300 px de ancho; logo reducido a 100 px de alto; etiquetas de empresa con `wrapText`.
- Espaciado interno de la lista de opciones reducido y padding vertical de `.menu-item` bajado a 10 px.
- Padding inferior del `BorderPane` raíz aumentado a 24 px.
- Se añade requisito visual del Menú principal a la especificación.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-mejorar-menu-principal`.

### Change `editor-800x600-scroll` (archivado)

- Editor de facturas adaptado a **800×600** y ya no se abre maximizado.
- Tabla de líneas con `minHeight` reducido a 120 px y `VBox.vgrow="ALWAYS"` para que crezca al maximizar y tenga scroll interno en 800×600.
- Ajustes compactos en `base.css`: padding de `.card` a 16 px, `.action-bar` a 8 px, `.cabecera-linea` y `.grid-cabecera` más ajustados.
- Reducción de paddings y espaciados en `Editor.fxml` para que la cabecera, la tabla y los totales encajen en 600 px de alto.
- Test `EditorTamanoMinimoTest` actualizado a 800×600 y verifica que la tabla y los totales son visibles.
- Se actualiza la especificación de tamaños de ventana.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-editor-800x600-scroll`.

### Change `uniformizar-ventanas-800x600` (archivado)

- Todas las pantallas principales y el diálogo de Generar facturas mensuales ajustados a **800×600**.
- `VentanaConfig.aplicar` ahora no redimensiona la ventana principal cuando ya está visible; solo aplica mínimos, máximos y propiedades, evitando saltos entre vistas.
- `Main.entrarEnMenu` fuerza el tamaño a 800×600 al salir de `Arranque` y desmaximiza si fuera necesario.
- FXML ajustados: Configuración (tablas y formularios más compactos), Histórico y Clientes (tablas adaptadas a 800 de ancho), Versiones y Backup (paddings reducidos), Generar facturas mensuales (diálogo con `ScrollPane`).
- Especificación actualizada: se elimina el redimensionado/recentrado automático y se definen los tamaños uniformes.
- Suite **105/105** en verde.
- Change archivado como `2026-09-01-uniformizar-ventanas-800x600`.

### Change `fix-ventana-1024-transicion-arranque-menu` (archivado)

- Corregido que el menú y las vistas principales se quedaban a 760×520 (tamaño de Arranque) al navegar desde la pantalla de empresa hasta su carga en 1024×768 hasta manualmente redimensionar o maximizar.
- Causa raíz: `VentanaConfig.aplicar` enrutaba los Stages ya visibles a `aplicarSinRedimensionar`, que solo aplicaba min/max/resizable/maximized y NO volvía a fijar el tamaño tras `stage.setScene(...)`, por lo que la ventana conservaba el tamaño previo de Arranque.
- Fix: `aplicarSinRedimensionar` ahora eleva `width`/`height` hasta el mínimo de la vista si la ventana está por debajo (`if stage.getWidth()<minAncho → setWidth`, igual con alto). Se ejecuta después de `setScene`, así que cualquier vista con mayor mínimo crece al cargar (sin recentrar ni reducir).
- Nuevo test `VentanaTransicionTest`: Stage 760×520 → `nav.mostrar(MenuPrincipal)` verifica que sube a 1024×768.
- Suite **106/106** en verde.
- Change archivado como `2026-09-01-fix-ventana-1024-transicion-arranque-menu`.

### Change `fix-ventana-1024-tras-pulse` (archivado)

- El clamp del change anterior no bastaba: `entrarEnMenu` pide `setWidth(1024)` ANTES de `nav.mostrar`, y en el momento del clamp sincrono `stage.getWidth()` ya reporta 1024 aunque la ventana nativa siga en 760 (peticion asincrona de un Stage visible y no redimensionable). El layout de la escena nueva dejaba la ventana en 760×520.
- Fix definitivo en `VentanaConfig.aplicar`: cuando el Stage ya esta visible y la vista es redimensionable, tras `aplicarSinRedimensionar` se programa un `Platform.runLater` que re-sube width/height al minimo de la vista; se ejecuta en el siguiente pulse, despues del layout de la escena nueva, cuando `getWidth()` ya refleja el tamaño real.
- `VentanaTransicionTest` corregido para reproducir el timing real (Stage mostrado no redimensionable a 760×520 → `setWidth(1024)` → `nav.mostrar(Menu)` → layout → comprobacion tras dos pulses).
- Suite **106/106** en verde (`mvn clean test`).
- Change archivado como `2026-09-01-fix-ventana-1024-tras-pulse` (skip_specs).

### Change `fix-ventana-max-heredado-arranque` (implementado, pendiente de archivar)

- Corregido definitivamente que el menu y las vistas principales se quedaran a 760x520 (tamano de Arranque) al pulsar Entrar.
- Causa raiz real: `ARRANQUE` era la unica vista que fijaba maximos (`maxWidth=760`, `maxHeight=520`). Esos maximos quedaban puestos en el Stage y en la ventana nativa; `entrarEnMenu` pedia 1024x768 antes de que nadie los levantase y con `resizable=false` activo, asi que Windows recortaba la peticion. Los clamps de los dos intentos anteriores eran condicionales a `stage.getWidth()`, que devuelve la propiedad de JavaFX (ya 1024) y no el tamano real de la ventana, por lo que nunca llegaban a ejecutarse.
- `VentanaConfig`: `ARRANQUE` deja de fijar maximos (su caracter fijo lo da `redimensionable=false`) y `aplicar` pasa a un unico camino determinista: libera los maximos de la vista anterior, fija `resizable` antes de tocar el tamano, aplica minimos y maximos, y decide si redimensionar comparando la configuracion previa (guardada por Stage en `getProperties()`) con la nueva, sin leer `getWidth()`. Solo maximiza cuando la vista lo pide, asi que navegar ya no desmaximiza la ventana.
- `Main.entrarEnMenu` deja de dimensionar y hace la transicion con la ventana oculta (`hide()` -> cargar menu -> `show()`), de modo que la ventana nativa se recrea con las medidas de la vista destino. Requiere `Platform.setImplicitExit(false)`; el cierre real sigue siendo el `Platform.exit()` de `cerrarAplicacion`.
- `Main` recupera ademas el tamano guardado de la sesion anterior cuando supera el minimo de la vista.
- `VentanaTransicionTest` reescrito (transicion Arranque -> Menu, maximos liberados, redimensionabilidad) mas un caso nuevo de conservacion del tamano del usuario al navegar entre vistas del mismo tamano. Suite **107/107** en verde.
- IMPORTANTE: el test NO reproduce el bug (comprobado revirtiendo el codigo: el test pasaba igual). En un Stage creado en un test la ventana nativa si crece; el fallo solo se daba en el primary stage de la aplicacion real. La verificacion valida fue visual, con la app en marcha.

### Change `reforzar-test-ventana` (archivado)

- `VentanaTransicionTest` pasaba igual con el codigo anterior al fix de ventana, asi que no protegia contra una regresion. Comprobado revirtiendo `VentanaConfig` a `9ddd14f`.
- El sintoma en pantalla solo se daba en el primary stage de la aplicacion y no es reproducible con un Stage creado en un test, pero las dos causas de codigo si son observables como propiedades del Stage: que `ARRANQUE` no imponga maximos y que navegar no desmaximice.
- Con el codigo antiguo las dos aserciones nuevas fallan; con el actual pasan.
- Commit `78385cb`.

### Change `editor-sin-scroll-factura-corta` (implementado, pendiente de sync y archivo)

- El Editor no cabia a 1024x768: la ventana deja ~713 px utiles y el contenido pedia ~880, asi que el bloque de totales quedaba cortado y habia que scrollear desde el primer momento aunque la factura tuviera dos lineas.
- Se elimina el `ScrollPane` general: la raiz vuelve a ser el `BorderPane` y la tabla de lineas es lo unico que crece, con su scroll interno.
- Cabecera reorganizada en dos bloques contiguos, FACTURA y CLIENTE, en lugar de una rejilla de seis filas a todo el ancho: baja de ~340 px a ~250 aprovechando el ancho que se desperdiciaba. La referencia de rectificativa se queda en fila propia para que siga colapsando al ocultarse.
- Dentro del bloque de cliente las columnas son desiguales: cliente y direccion ocupan el ancho completo del bloque; nombre y email, la columna ancha.
- Titulo y distintivo de anulada integrados en la barra de acciones, que antes ocupaba fila propia.
- Totales en columna compacta de 300 px anclada al pie, fuera del scroll, con Observaciones al lado ocupando el mismo alto. La franja inferior mide lo mismo que antes y el desglose queda agrupado.
- Clases nuevas en `base.css` acotadas al Editor; no se tocan `.card` ni `.zona-contenido`, que son globales. Los totales reutilizan `.totales`, `.total-fila` y `.total-grande` para no tener que tocar los siete temas.
- `EditorTamanoMinimoTest` comprueba ahora, sobre el layout real, que `#lblTotal` y `#txtObservaciones` terminan dentro del alto de la escena y que la tabla conserva al menos 200 px. Con el FXML anterior falla: el total termina en 737 y el alto util es 729.
- Suite **108/108** en verde y comprobacion visual con la app hecha.

### BUG RESUELTO: styleClass con espacios en 5 FXML

Corregido el 01/09/2026 con el change `fix-styleclass-separador-fxml` (archivado): las 15 ocurrencias de `styleClass="card zona-contenido"` pasaron a `styleClass="card, zona-contenido"` en Backup, Clientes, Configuracion, Historico y Versiones. `StyleClassSeparadorTest` vigila que no reaparezcan espacios sin coma, y `Configuracion.fxml` reescrito en el change `configuracion-secciones-laterales` mantiene las comas.

## Sesion del 02/09/2026

### Change `configuracion-secciones-laterales` (archivado)

- La pantalla de Configuracion deja de usar pestanas: navegacion lateral con `ListView` (200 px) + `StackPane` con un `VBox` por seccion, dos encabezados de grupo ("Configuracion general" y "Catalogos").
- Siete secciones: Empresa, Cabecera y pie, PDF y apariencia (grupo con guardado global) e IVA, Retenciones, Series, Empresas (gestion fila a fila con sus propias acciones).
- El boton "Guardar configuracion" se muestra/oculta con `visible`/`managed` segun la seccion: visible solo en las tres primeras.
- El tema de la aplicacion se mueve de Empresa a la seccion "PDF y apariencia" (preferencia global compartida entre empresas).
- `CabeceraLayout` (nuevo, paquete `pdf`): unica fuente de verdad de la geometria de cabecera (doble del logo con topes 480/170 y defectos 120/60, offsets X/Y, lineas de empresa con NIF destacado, margen lateral 40 pt y alto de cabecera minimo 108, tanto en modo texto como logo). `PdfService` delega en el sin cambiar ningun valor: el PDF resultante es identico (`PdfServiceTest` en verde).
- `PreviaCabecera` (nuevo, paquete `ui`): previsualizacion aproximada a escala (A4 de ancho, banda superior) con el logo real cargado desde `logoPath` en posicion y tamano efectivos o el bloque de lineas de empresa con el NIF en chip, banda de acento, guias de margen y separador de banda. Repinta al cambiar modo, logo, X/Y, ancho/alto y color; muestra el tamano efectivo junto a los campos de ancho/alto y un aviso discreto de "Vista aproximada".
- `base.css`: clases nuevas acotadas a la pantalla (`lista-secciones`, `seccion-config`, `alta-rapida`, `previa-cabecera`, `vista-aviso`, `grupo-secciones`) con colores derivados de `-fx-accent`/`-fx-base`; no se tocan `.card` ni `.zona-contenido`, que son globales.
- Tests nuevos: `CabeceraLayoutTest` (7) y `ConfiguracionLayoutTest` (las 7 secciones caben a 1024×768 y la barra de guardado solo es visible en las 3 primeras; falla con el FXML anterior). Suite **119/119** en verde.
- Verificacion visual a 1024×768 por el usuario: recorrido de las 7 secciones sin scroll ok, previa reacciona a logo/posicion/tamano/color, guardado y reentrada conservan todo, IVA/Retenciones/Series funcionan, los 7 temas ok y Historico/Clientes/Versiones/Backup sin cambios.
- Commit `5c56ecf` (implementacion) y `bf4f564` (docs). Archivado el 02/09/2026 en `2026-09-02-configuracion-secciones-laterales`; specs sincronizadas y validadas (`openspec validate --specs` ok, 2 requisitos nuevos anadidos a `invoicing/spec.md`).

## Sesion del 02/09/2026 (continuacion)

### Change `ficha-cliente-validada` (archivado)

- Validadores nuevos en `util`: `CodigoPostalValidator` (cinco digitos con las dos primeras cifras entre 01 y 52; en blanco o null NO valido) y `EmailValidator` (en blanco SI valido; con contenido, patron razonable `algo@algo.algo`). TDD: `CodigoPostalValidatorTest` (4) y `EmailValidatorTest` (3), en rojo antes de implementar.
- Tema en todos los dialogos: `Dialogos.aplicarTema(DialogPane)` anade la clase `.dialog-card` y las hojas del tema activo (`ThemeManager.hojas()` nuevo, reutilizado por `seleccionar`); aplicado a error, info, confirmar, confirmarCambiosSinGuardar y modoGuardarVersion. `.dialog-card` completo con fondo `-fx-base` y borde `derive(-fx-base, -10%)`, y cabecera del dialogo con degradado gris y texto en el color de acento para que "Alta de cliente" parezca la primera fila de las tablas.
- Ficha de cliente a 375 px (primero fue 560 y el usuario pidio un tercio menos), `ColumnConstraints` con hgrow ALWAYS + `maxWidth` infinito en los campos, Direccion con etiqueta y campo en la misma fila (el usuario rechazo la version en dos filas), tema aplicado y `initOwner(nav.stage())`.
- Validaciones de CP y email calcando el patron del NIF con un ajuste pedido por el usuario: el CP NO avisa al salir del campo (solo al pulsar Guardar, para poder rellenar el resto) y limpia el borde rojo al enfocar o corregir. NIF intacto. `ClientesNifValidationTest` adaptado porque el CP ahora es obligatorio.
- Suite **126/126** en verde. Verificacion visual del usuario OK en los 5 puntos (tema claro/oscuro, altas con invalidos, email en blanco, Direccion larga, editar cliente antiguo sin CP). Archivado el 02/09/2026 en `2026-09-02-ficha-cliente-validada` (sin delta de specs por `skip_specs`).

### Change `logo-tamano-fijo` (archivado)

- El logo de cabecera pasa a un tamaño **fijo** de 240×120 pt trazado por `CabeceraLayout` (constantes `ANCHO_LOGO_FIJO`/`ALTO_LOGO_FIJO`); se eliminan los offsets X/Y y los topes, y su tamaño y posición ya no son configurables.
- Alto de cabecera en modo logo de 170 pt = `HUECO_LOGO_SUPERIOR` (26) + `ALTO_LOGO_FIJO` (120) + `HUECO_LOGO_INFERIOR` (24). OpenPDF ancla la imagen por abajo-izquierda (`cm [plainWidth 0 0 plainHeight ex ey]`), por eso el logo se coloca en `bordeSuperiorContenido + HUECO_LOGO_INFERIOR` para no tocar la línea separadora, y `xInfo = izquierda + ANCHO_LOGO_FIJO + 14` (verificado con `javap` sobre `openpdf-1.3.39.jar`).
- `ConfiguracionController`/`Configuracion.fxml`: se retiran los campos X, Y, ancho y alto del logo y la etiqueta de tamaño efectivo con su `actualizarTamanoEfectivo()`; `PreviaCabecera` se mantiene y repinta la caja fija.
- `MenuController` fija el logo del menú a 260×100 con `preserveRatio` para que no desborde la tarjeta de empresa.
- TDD: `CabeceraLayoutTest` reescrito (6 tests de caja fija, incluidos nulos y absurdos) y comprobado que falla con el layout anterior (`expected 240.0 but was 480.0`).
- Suite **125/125** en verde (126 − 1 test eliminado de offsets). Verificación visual del usuario OK en los 4 puntos: logos apaisado y cuadrado sin pisar FACTURA ni comprimir datos de empresa, PDF con la configuración por defecto idéntico al anterior, y la sección Cabecera y pie cabe a 1024×768.
- Commits `c25020f` (implementación) y `7d47d9f` (sync de specs + archivo en OpenSpec), push realizado. Spec sincronizada: requisitos «Exportación a PDF» (logo a tamaño fijo no configurable, caja 240×120) y «Configuración» (escenario «Elegir modo de cabecera» sin ajuste de tamaño/posición), validadas (`openspec validate --specs` ok). Archivado como `2026-09-02-logo-tamano-fijo`.

### Change `fix-exportar-pdf-agrupado` (archivado)

- Bug: la exportación de varias facturas como «Un único PDF agrupado» lanzaba `ExceptionConverter: Stream Closed`.
- Causa raíz: en `PdfService.concatenar()`, el `FileOutputStream` estaba en try-with-resources que lo cerraba antes de que `document.close()` hiciera flush de `PdfCopy` (necesita el stream abierto para escribir la tabla de cross-references y trailer).
- Fix: eliminar el try-with-resources del `FileOutputStream` y crear el stream inline en el constructor de `PdfCopy`; el `document.close()` del finally cierra `PdfCopy` → flush → stream.
- Test nuevo: `exportarAgrupadoUneDosFacturasEnUnSoloPdf` verifica que dos facturas se fusionan en un solo PDF con ≥2 páginas.
- Suite **126/126** en verde. `skip_specs` (fix puro, sin cambio de requisito). Archivado el 02/09/2026 en `2026-09-02-fix-exportar-pdf-agrupado`.

### Change `fix-cancelar-salida` (archivado)

- Al cerrar la app (X o Salir del menú) el diálogo «¿Seguro que deseas salir?» ahora se puede cancelar: si el usuario pulsa «No», la ventana sigue visible.
- `Main.cerrarAplicacion()` pasa a devolver `boolean` (true si se cierra); el manejador de `setOnCloseRequest` consume el evento (`e.consume()`) cuando se cancela.
- `MenuController.salir` dispara `WINDOW_CLOSE_REQUEST` en vez de `stage.close()`, de modo que el cierre con Salir pasa por el mismo manejador cancelable.
- En la pantalla de arranque el cierre sigue sin preguntar (se mantiene el comportamiento).
- Suite **126/126** en verde. `skip_specs`. Verificación visual del usuario OK en los 4 puntos. Archivado el 02/09/2026 en `2026-09-02-fix-cancelar-salida`.

### Change `logo-relleno-tema` (archivado)

- El recuadro que envuelve al logo (menú principal 280×100 y editor 110×40) se rellena con los colores del propio logo para que imagen y caja se vean como una sola pieza sea cual sea el tema. `util/LogoMarco` clasifica muestreando solo el marco exterior (~6 % por lado con paso, descartando píxeles con alfa < 0,9 y agrupando opacos en cubos de 5 bits por canal) y decide tres casos:
  - **Plano** (fondo opaco uniforme): el recuadro adopta el color exacto vía `setStyle` inline pisando solo `-fx-background-color`/`-fx-border-color`; conserva esquinas redondeadas (radius del tema `.menu-logo-box`).
  - **Difuminado** (foto/degradado): respaldo desenfocado (blur 25, desborde 60 px, `mouseTransparent`, no gestionado, tamaño fijo min=pref=max) como primer hijo + clip redondeado radio 10 ligado al tamaño de la caja, sin que el desenfoque se salga del recuadro.
  - **Transparente** (PNG con canal alfa): no toca nada, queda el color del tema.
- `LogoMarco.aplicar(StackPane, Image)` limpia antes (estilo inline + clip + respaldo marcado); `LogoMarco.limpiar(...)` deshace todo y se llama en las tres salidas tempranas de `MenuController.cargarLogo` y `EditorController.cargarLogo` (ruta vacía, fichero inexistente, imagen con error). No se tocan `base.css` ni `tema-*.css` ni el escalado del logo.
- Menú (`MenuPrincipal.fxml`): StackPane `logoBox` con `fx:id`, fijo 280×100. Editor (`Editor.fxml`): el `ImageView` del logo se envuelve en un StackPane fijo 110×40 en el ToolBar; `EditorController.cargarLogo` añade `fitHeight=38` además del `fitWidth=92`.
- Los tres logos reales del proyecto clasifican como **plano blanco** (`#FEFEFE`), así que quedan con caja blanca uniforme sin franjas ni borde visible.
- TDD: `LogoMarcoTest` (12 tests: plano blanco puro, plano de color exacto, transparente sin cambio, ruido→difuminado, imagen nula y 2×2→sin cambio, geometría del respaldo fit=caja+60 con `managed`/`mouseTransparent`, clip arqueado, y cambio foto⇄plano en los dos sentidos sin rastro). Suite **138/138** en verde (126 + 12). Verificación visual del usuario OK.
- Sync de specs: MODIFIED «Identidad de empresa en la interfaz» (relleno por tipo de imagen, esquinas y grosor conservados, transparente con tema, logo del editor en caja fija + 3 escenarios nuevos). Archivado el 02/09/2026 en `2026-09-02-logo-relleno-tema`.

### Change `pdf-texto-neutro-color-acento` (archivado)

- El texto por defecto del PDF salía con un tinte marrón-arena fijo (constantes `TINTA` `#3A332B`, `GRIS` `#5F5548`, `GRIS_CLARO` `#A2937F`, `VALOR_SUAVE` `#C4BAAC`) independiente del color de acento configurable.
- En `PdfService` se sustituyen esas constantes por una paleta neutra: valores en **negro** `#000000`, etiquetas/info secundaria y pie (`Página X de Y`) en **gris neutro** `#555555`, etiquetas pequeñas de la tarjeta FACTURAR A en **gris claro neutro** `#777777`. Se mantienen los rojos (anulada/descuento), el blanco y los tonos derivados del acento.
- El bloque **`SERIE / Nº` y `FECHA`** (rótulo y valor) pasa a usar el **color de acento oscurecido** `c.oscuro` (mezcla 35 % negro), en lugar de la tinta marrón, para garantizar contraste sobre blanco.
- Se añade un escenario nuevo; primera idea de campo de texto hex manual se **descartó a petición del usuario** (el `ColorPicker` ya permite elegir cualquier color de la paleta), quedando únicamente el selector.
- `Configuracion.fxml`: se actualiza solo el texto informativo del `ColorPicker` ("...; también SERIE/Nº y FECHA").
- Sync de specs: MODIFIED «Exportación a PDF» (texto por defecto en negro/gris neutro, SERIE/Nº–FECHA en color de acento, cabecera de «Datos de pago» y etiquetas/valores de tarjeta en gris neutro/negro, + 2 escenarios nuevos). Suite **138/138** en verde y verificación visual del usuario OK. Archivado el 02/09/2026 en `2026-09-02-pdf-texto-neutro-color-acento`.

## Sesion del 03/09/2026

### Change `fix-crear-empresa-no-cambia-activa` (archivado)

- Bug: `EmpresaManager.crearEmpresa()` activaba en silencio la empresa nueva. Desde Configuración con una empresa en uso, la UI seguía mostrando la empresa anterior pero todo lo que se guardaba escribía en la base de datos de la empresa nueva vacía, y además cambiaba la última empresa recordada.
- `Database`: nuevo accesor `dbPathDe(String slug)` que construye la ruta de la base de una empresa sin activarla.
- `EmpresaManager.crearEmpresa`: ahora solo crea carpeta, base de datos migrada y entrada en el catálogo, sin tocar el estado global (no activa la empresa, no cierra la conexión en curso, no cambia `ULTIMA_EMPRESA` ni la sesión). Crea la base sobre una conexión JDBC local y temporal y la cierra.
- `ConfiguracionController.nuevaEmpresa`: tras crear, ofrece al usuario cambiar a la empresa nueva («¿Quieres cambiar a ella ahora?»); si acepta, selecciona por slug y llama a `cambiarEmpresa()`; si no, se queda en Configuración con su empresa activa intacta.
- `Migrations`: se expone `ultimaVersion()` (número de migraciones) para el test de esquema completo.
- `Main` y `ArranqueController` no se tocan: el arranque ya selecciona explícitamente por slug y conecta al entrar; en `Main.prepararDatos()` se verifica que no se llama a `Database.getConnection()` en la ventana sin empresa.
- Tests (`EmpresaManagerTest`, de 6 a 9): `creaEmpresaYLaDejaActiva` renombrado a `crearCreaLaBaseSinActivarla`; `dosEmpresasNoCompartenDatos`, `eliminarEmpresaBorraCarpeta` y `noSePuedeEliminarLaActiva` intercalan `conectar(...)`. Nuevos: `crearNoCambiaLaEmpresaActiva`, `crearNoRompeLaConexionEnCurso` (reproduce el fallo real) y `laBaseNuevaTieneElEsquemaCompleto` (`PRAGMA user_version == Migrations.ultimaVersion()`).
- Suite **141/141** en verde. Sync de specs (MODIFIED «Gestión de empresas»: crear desde Configuración SHALL NOT cambiar la empresa activa/la conexión/la última empresa; cambiar de empresa SHALL ser acción explícita; escenario «Crear una nueva empresa» reescrito, nuevos «Crear y aceptar el cambio»), archivado el 03/09/2026 en `2026-09-03-fix-crear-empresa-no-cambia-activa`.

### Change `restaurar-copia-seguridad` (archivado)

- La pantalla de Copia de seguridad permite **restaurar** una copia del SQLite además de crearla. Al seleccionar un archivo `*.db` muestra un resumen (empresa, NIF, nº de facturas, última fecha y versión de esquema).
- `BackupService.leerResumen(Path)` valida antes de tocar nada: archivo legible, no ser la propia base activa, `PRAGMA quick_check` (rechaza no-SQLite), existencia de todas las tablas de la aplicación y `user_version > 0`; rechaza copias de esquema más nuevo solo si su estructura de tablas/columnas no coincide (permite las que coinciden avisando). `verificarEstructura` comprueba tablas y columnas conocidas (`COLUMNAS_APLICACION`).
- `restaurarEnEmpresaActiva(Path)`: guarda copia de rescate en `copias_previas`, `resetConnection`, `Files.copy`, limpia `-wal`/`-shm` y reconecta; si algo falla hace rollback.
- `restaurarComoEmpresaNueva(Path, nombre)`: `crearEmpresa` (ya no activa), copia sobre `dbPathDe(slug)`, limpia diario y migra con conexión local.
- `crearBackup` usa `rutaLibre` para evitar colisiones de timestamp dentro del mismo segundo.
- `Migrations.userVersion(Connection)` pasa a ser público.
- UI: `Backup.fxml` con una tarjeta «Restaurar una copia» (origen, resumen, `radio` Reemplazar/Crear nueva, nombre de nueva empresa, botón Restaurar), contenido en `ScrollPane`. `BackupController` con `seleccionarOrigen`, regla del NIF (deshabilita Reemplazar si el NIF de la copia no coincide con la empresa activa, salvo que la activa esté vacía y sin facturas) y `restaurar` en `Task` con navegación post-restauración.
- Fix menor de FXML: el `ToggleGroup` se declara dentro de `<fx:define>` (un `ToggleGroup` no es un `Node` y no puede ser hijo de layout; rompía el load con `Unable to coerce ToggleGroup to Node`).
- Tests nuevos: `BackupServiceTest` (11) y `BackupLayoutTest` (layout con las dos tarjetas sin desbordar). Suite **153/153** en verde.
- Sync de specs (MODIFIED «Copia de seguridad» con restauración, resumen, validación, copia de rescate, reemplazar/crear nueva, regla del NIF, esquema posterior y logo inexistente + 9 escenarios), archivado el 03/09/2026 en `2026-09-03-restaurar-copia-seguridad`.

### Change `fix-restaurar-validacion-y-rollback` (archivado)

- **Fallo 1 (validación demasiado estricta)**: `verificarEstructura` exigía TODAS las tablas de la aplicación, así que rechazaba una copia de un esquema anterior legítimo (p. ej. sin `numero_disponible`).
  - `BackupService`: nueva constante `TABLAS_NUCLEO` (cliente, serie, tipo_iva, factura, factura_version, factura_linea, empresa, preferencias). `verificarEstructura` se sustituye por `comprobarTablasNucleo(Connection)` (exige siempre las tablas núcleo; falta alguna → `ValidationException`) y `estructuraCompleta(Connection)` (todas las tablas y columnas conocidas, devuelve `boolean`). `leerResumen` llama siempre a `comprobarTablasNucleo` y, solo cuando `user_version > Migrations.ultimaVersion()`, a `estructuraCompleta`. Una copia de esquema anterior se acepta y se migra al restaurar (`Database.getConnection()` migra al reconectar; `restaurarComoEmpresaNueva` migra con conexión local).
- **Fallo 2 (rollback con conexión abierta)**: en `restaurarEnEmpresaActiva` el `catch` copiaba el rescate con la conexión SQLite aún abierta; Windows impedía sobrescribir y enmascaraba el error original.
  - El `catch` ahora empieza con `Database.resetConnection()` (cierra → copia rescate → `borrarDiario` → reabre), y propaga la causa original: firma `throws IOException, SQLException, ValidationException` (deja de ser `Exception` genérica).
- **Menores**: `restaurarComoEmpresaNueva` envuelve los pasos posteriores a `crearEmpresa` en `try/catch` que llama a `EmpresaManager.eliminarEmpresa(nueva.slug())` si algo falla (no deja basura de carpeta/catálogo); `borrarDiario` deriva `-wal`/`-shm` de `Database.dbPath().getFileName()` en vez de hardcodear `facturas.db`.
- **UI (`BackupController`)**: el mensaje de confirmación muestra el **nombre visible** de la empresa activa vía `EmpresaManager.listarEmpresas()` (slug de respaldo); `aplicarReglaNif` se aplana (empresa activa vacía sin NIF/facturas → permitir reemplazar; NIFs iguales → permitir; en cualquier otro caso solo crear nueva) eliminando la rama muerta; al crear empresa nueva y responder «NO» al cambio se **queda en la pantalla de Copias** (`Backup.fxml`) en lugar de ir al menú.
- Tests: `rechazaCopiaSinLasTablasDeLaAplicacion` ahora deja caer `factura` (tabla núcleo) en vez de `numero_disponible` (legítimo que falte en una copia antigua); nuevos `rechazaCopiaSinLasTablasNucleo` (user_version 1 sin `factura` → rechaza) y `restaurarCopiaDeEsquemaAnteriorSeMigra` (DROP `numero_disponible` + user_version 6 → restaura y la base activa recupera la tabla y queda en `ultimaVersion()`). `restaurarComoEmpresaNuevaNoDejaBasuraSiFalla` se documenta en vez de montar un test artificial. Suite **155/155** en verde.
- Sync de specs (MODIFIED «Copia de seguridad»: tablas fundamentales siempre exigidas, copia de esquema anterior aceptada y migrada, estructura completa solo si es posterior + 2 escenarios nuevos), archivado el 03/09/2026 en `2026-09-03-fix-restaurar-validacion-y-rollback`.

## Proximos pasos

- No hay changes activos. Esperar instrucciones del usuario para el siguiente change.
- Opciones conocidas pendientes en el spec principal:
  - Flujo de **clientes inactivos** (clientes con facturas no se borran, se marcan inactivos y no se ofrecen al crear facturas nuevas).
  - **Copia de seguridad** manual ya implementada (crear y restaurar) en `restaurar-copia-seguridad`.
- Idea futura anotada en el cambio y pendiente de un change propio: **buscador de codigos postales** en la ficha de cliente (buscar por localidad y que rellene el CP, o al reves). Es el dato que mas lata da al cumplimentar facturas.


## Git

- **Corregido el bug de 1024×768 en transición (01/09/2026)**: al pasar de Arranque (760×520) al menú, la ventana se quedaba en 760×520 hasta redimensionar o maximizar. Causa raíz: `VentanaConfig.aplicar` enviaba los Stages ya visibles a `aplicarSinRedimensionar`, que solo aplicaba min/max sin fijar el tamaño tras `setScene`. Fix: ahora `aplicarSinRedimensionar` eleva width/height hasta el mínimo de la vista (`VentanaTransicionTest` lo verifica, suite **106/106**).
- Commit y push de: fix `VentanaConfig` + `VentanaTransicionTest` + spec y archive OpenSpec.
- Despues, fix definitivo con clamp diferido al pulse (`fix-ventana-1024-tras-pulse`) commiteado y pusheado.
- **Editor sin scroll (01/09/2026)**: rediseno del Editor para que una factura corta quepa entera a 1024x768 con los totales siempre visibles. Maquetado previo revisado con el usuario antes de tocar codigo.
- **Fix real del tamano de ventana (01/09/2026)**: los dos fixes anteriores no bastaban. La causa era el `maxWidth`/`maxHeight` de Arranque heredado por el Stage. Corregido en `VentanaConfig` (sin maximos en Arranque, `aplicar` determinista) y `Main` (transicion con la ventana oculta). Verificado a la vista con la app en marcha; suite **107/107**.
- **Configuracion por secciones (02/09/2026)**: pantalla de Configuracion con lista lateral en vez de pestanas, `PdfService` delegando en `CabeceraLayout` y vista previa aproximada de la cabecera (`PreviaCabecera`). Suite **119/119**. Commits `5c56ecf` (implementacion), `bf4f564` (docs) y `c75c55f` (archivo OpenSpec + spec sincronizada).
- **Logo relleno tema (02/09/2026)**: util `LogoMarco` + aplicacion al menu (280×100) y editor (110×40). TDD con `LogoMarcoTest` (12 tests, suite **138/138**). Sync de spec «Identidad de empresa en la interfaz» + archive OpenSpec + update de `CONTINUAR_MAÑANA.md`, commit y push.
- **PDF texto neutro / color acento (02/09/2026)**: paleta de tinta del PDF sin tinte arena (negro/gris neutro) y `SERIE / Nº`-`FECHA` en color de acento. Suite **138/138**. Sync de spec «Exportación a PDF» + archive OpenSpec + update de `CONTINUAR_MAÑANA.md`, commit y push.
- **Crear empresa sin activar (03/09/2026)**: `crearEmpresa` deja de activar la empresa nueva (crea base con conexión local + catálogo, sin tocar estado global); desde Configuración se ofrece cambiar a ella. `Migrations.ultimaVersion()` nuevo. Suite **141/141**. Sync de spec «Gestión de empresas» + archive OpenSpec + update de `CONTINUAR_MAÑANA.md`, commit y push.
- **Restaurar copia de seguridad (03/09/2026)**: pantalla de Copia de seguridad con restauración (resumen, validación, copia de rescate automática, reemplazar la activa o crear empresa nueva, regla del NIF). `leerResumen`, `restaurarEnEmpresaActiva`, `restaurarComoEmpresaNueva`, `verificarEstructura`, `rutaLibre` en `BackupService`; `ToggleGroup` en `fx:define` en `Backup.fxml`. Suite **153/153**. Sync de spec «Copia de seguridad» + archive OpenSpec + update de `CONTINUAR_MAÑANA.md`, commit y push.
- **Fix restaurar validación y rollback (03/09/2026)**: `verificarEstructura` se separa en `comprobarTablasNucleo` (siempre exigidas, `TABLAS_NUCLEO`) y `estructuraCompleta` (solo exigida si la copia es de esquema posterior), aceptando y migrando copias de esquema anterior; rollback de `restaurarEnEmpresaActiva` que cierra la conexión antes de copiar el rescate y propaga el error original (`IOException`); limpieza en `restaurarComoEmpresaNueva`; `borrarDiario` derivado de `dbPath()`; UI con nombre visible de empresa y fin en pantalla Copias al responder «NO». Suite **155/155**. Sync de spec «Copia de seguridad» + archive OpenSpec + update de `CONTINUAR_MAÑANA.md`, commit y push.

## Notas tecnicas que evitan perder tiempo

- Comando Maven: `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` (suite completa: 155 tests, todos verdes). IMPORTANTE: lanzar maven siempre desde el directorio del proyecto; si se lanza desde otro workdir falla sin POM y los pasos siguientes usan clases viejas. IMPORTANTE: si la app sigue mostrando tamaños antiguos tras cambiar código, borrar `target\` y `mvn clean` (el compilador incremental puede dejar `.class` mezclados).
- Para inspeccionar PDFs visualmente: rasterizar pagina con `Windows.Data.Pdf` desde PowerShell 5.1 (`render.ps1` en %TEMP%\opencode\pdfcheck) y leer el PNG; el modelo no lee PDFs directamente.
- `PdfPCellEvent.cellLayout(PdfCell, Rectangle, PdfContentByte[])` dibuja DESPUES del contenido: usar `canvases[PdfPTable.TEXTCANVAS]` para contornos; para fondo+texto juntos, pintar ambos dentro del evento con celda de frase vacia. `PdfReader.getPageN(1).getAsDict(PdfName.RESOURCES)` + `PdfDictionary.getKeys()` para inspeccionar fuentes embebidas (no existe `getPageResources`).
- FXML: `maxWidth="USE_PREF_SIZE"` es invalido; usar `maxWidth="-Infinity"`. Para que un control CREZCA dentro de una celda de GridPane con `hgrow` hacen falta AMBAS cosas: `ColumnConstraints hgrow="ALWAYS" fillWidth="true"` y `maxWidth="Infinity"` en el control (los controles no crecen por defecto). Las filas que deben envolver usan `FlowPane` con cada grupo etiqueta+campo en su propio HBox (FlowPane no tiene hgrow).
- El smoke test de UI no muestra ventanas: para ejercitar layout real usa `root.applyCss(); root.resize(1024,768); root.layout();`.
- Cierre programatico de ventana: `nav.stage().fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))`.
- Constructor de `EstadoService` (7 parametros): `(FacturaRepository, SerieRepository, VersionRepository, LineaRepository, VersionadoService, NumeroService, FacturaService)`.
- En los PDF el `.xlsx` original es referencia de formato: la columna TOTAL lleva IVA incluido (base × 1,21).
- El texto extraible de un PDF no incluye lo dibujado via plantilla/XObject (p. ej. la cifra final de «Pagina X de Y»): para testear el pie solo se puede afirmar hasta «de »; la cifra completa se comprueba a la vista.
- No crear dentro del proyecto carpetas/archivos de metadatos de IA ni documentacion no pedida.
