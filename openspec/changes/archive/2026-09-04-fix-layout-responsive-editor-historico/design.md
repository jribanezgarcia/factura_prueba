## Context

Tres defectos de layout puro, sin lógica implicada: los controladores no manipulan layout. El tamaño mínimo y predefinido de ventana es 1024×768 (`Requirement: Tamaños de ventana por vista`). Ver proposal.md - Why.

Anchos disponibles (medidos sobre el FXML+CSS actual):
- Editor, tarjeta de cabecera (`card, card-editor, panel-busqueda`): 1024 − 2×16 (padding del `BorderPane`) − 2×12 (padding de `.card-editor`) = **968 px**.
- Histórico, tarjeta de filtros (`card, zona-contenido, panel-busqueda`): 992 − 2×16 (padding de `.card`) = **960 px**.
- `.grid-editor`: `hgap 8, vgap 4` (`base.css:199-203`).

## Goals / Non-Goals

**Goals:**

- `Nueva` junto a `Guardar` en la barra del Editor.
- Etiquetas del Editor siempre enteras a 1024, sin «…».
- Misma distribución a 1024 y maximizado en Editor e Histórico; solo crecen las tablas.

**Non-Goals:**

- No se reordenan filtros ni se renombra ninguna etiqueta «Hasta» duplicada: solo se fija la distribución.
- No se cambia el mínimo de ventana ni se tocan controladores, tests de comportamiento o estilos visuales.

## Decisions

### D1. Columna de etiqueta fija en vez de elástica

Las columnas 0 y 2 de los dos grids del Editor pasan de `<ColumnConstraints/>` vacío a `hgrow="NEVER" minWidth="-Infinity" prefWidth="105.0"`. `minWidth="-Infinity"` equivale a `USE_PREF_SIZE`: la columna ni crece ni se deja recortar.

Se descarta limitarse a dar `prefWidth` sin fijar el mínimo: el `GridPane` seguiría pudiendo comprimir la etiqueta por debajo de su preferido, que es el defecto actual.

### D2. Campos con anchura fija, no elástica

Se quita `maxWidth="Infinity"` de los controles y `hgrow="ALWAYS"` de las columnas de campo; cada columna de campo conserva su `prefWidth` actual con `hgrow="NEVER"` y `fillWidth="true"`. Así el campo ocupa siempre lo mismo y el sobrante no se reparte.

Se descarta mantener el estiramiento solo al maximizar: la distribución dejaría de ser idéntica en ambos tamaños, que es justo lo que pide el requisito nuevo.

### D3. El sobrante lo absorbe un `Region`, no los campos

En el `HBox` de cabecera del Editor se quita `HBox.hgrow="ALWAYS"` de los dos `VBox`, se les pone `maxWidth` igual a su `prefWidth` (380 / 545) y se añade un `<Region HBox.hgrow="ALWAYS"/>` al final. En Histórico, la rejilla de filtros lleva `maxWidth="-Infinity"` para no estirarse dentro de su `VBox`. Solo `tablaLineas` y `tabla` conservan crecimiento (`vgrow`/`hgrow` `ALWAYS`).

### D4. Filtros del Histórico en rejilla fija 4+3

El `FlowPane` se sustituye por un `GridPane styleClass="grid-filtros"` de 8 columnas (4 pares etiqueta/campo) y 2 filas fijas, con los mismos `fx:id` y `prefWidth` de controles. Reparto: fila 0 `Serie` · `Cliente/NIF` · `Desde` · `Hasta`; fila 1 `Importe desde` · `Hasta` · `Estado`. Columnas de etiqueta con `hgrow="NEVER" minWidth="-Infinity"` (autoajustan a la etiqueta más ancha de la columna y no se recortan); columnas de campo con `hgrow="NEVER"` (autoajustan al `prefWidth` del control).

Se descarta 3 pares por fila: cabría pero dejaría la tercera fila casi vacía y separaría los dos «Hasta» de sus «Desde». Con 4+3 cada «Hasta» queda junto a su «Desde» para siempre.

### D5. Valores exactos

**A1. Orden de la barra** (`Editor.fxml:17-32`): mover el `Button text="Nueva"` (`Editor.fxml:30`) para que quede inmediatamente después de `btnGuardar` (`Editor.fxml:24`). Orden final: `logoBox`, `lblTitulo`, `lblEstado`, `Region`, **Guardar, Nueva**, Exportar PDF, Versiones, Rectificativa, Anular, Restaurar, Volver.

**A2. Columnas de etiqueta** (4 reemplazos, en `Editor.fxml:37-42` y `Editor.fxml:63-68`): cada `<ColumnConstraints/>` de columna 0 y 2 pasa a `<ColumnConstraints hgrow="NEVER" minWidth="-Infinity" prefWidth="105.0"/>`. 105 px cubre «Forma de pago» (13 caracteres) a 12 px en Segoe UI.

**A3. Columnas de campo**: en las 4 `ColumnConstraints` con `hgrow="ALWAYS"` se quita el `hgrow` (queda `fillWidth="true"` + su `prefWidth`: 120/120 en FACTURA, 200/210 en CLIENTE — la estrecha se ensancha para NIF/CP/Provincia a costa de la ancha). En los controles se quita `maxWidth="Infinity"` (`comboSerie`, `fecha`, `txtNumero`, `txtFormaPago`, `vencimiento`, `txtRealizadaPor`, `comboCliente`, `cliNombre`, `cliNif`, `cliDireccion`, `cliEmail`, `cliCp`, `cliLocalidad`, `cliProvincia`). Excepción: `txtReferencia` (`Editor.fxml:56`, `columnSpan=3`) lo conserva —su ancho lo determinan las columnas, ya fijas.

**A4. Tope de los bloques**: en el `HBox spacing="14"` (`Editor.fxml:33-87`), quitar `HBox.hgrow="ALWAYS"` de los dos `VBox`, fijarles `prefWidth` y `maxWidth` a **440** (FACTURA) y **485** (CLIENTE), y añadir `<Region HBox.hgrow="ALWAYS"/>` tras el segundo `VBox`. Cuenta: 440 + 14 + separador + 14 + 485 ≈ 958 ≤ 968 a 1024; al maximizar, el `Region` absorbe todo el extra.

**Anchos fijos resultantes**: rejilla FACTURA = 105+120+105+120+3×8 = **474 px**; rejilla CLIENTE = 105+200+105+210+3×8 = **644 px**. Dentro de su `VBox` topado, los campos absorben la diferencia hasta su mínimo —las etiquetas quedan intactas, que es lo exigido— y el `TextField` hace scroll interno si su texto no cabe. Reparto: FACTURA dispone de 440−210−24 = **206 px** para sus dos campos (~103 cada uno: Serie y Fecha legibles); CLIENTE dispone de 485−210−24 = **251 px**, repartidos ~122 en la columna ancha (Nombre/Dirección/Email/Localidad) y ~129 en la estrecha (NIF/CP/Provincia, un ~47 % más que los ~88 anteriores). El reparto prima los campos que se rellenan en cada factura e iguala las dos columnas de cliente.

**B1. Rejilla de filtros** (`Historico.fxml:16-45`): `GridPane styleClass="grid-filtros" maxWidth="-Infinity"`, 8 columnas × 2 filas. Parejas estimadas (etiqueta + 4 + control): Serie ~160, Cliente/NIF ~275, Desde ~190, Hasta ~190, Importe desde ~190, Hasta ~150, Estado ~170. Fila 0 ≈ 160+275+190+190+3×10 = **845 ≤ 960** ✓. Fila 1 ≈ 190+150+170+2×10 = **530** ✓. `Estado`/`comboEstado` va en la fila 1.

**C. CSS** (`base.css`, junto a `.grid-editor`): añadir `.grid-filtros { -fx-hgap: 10px; -fx-vgap: 8px; -fx-padding: 0; }` (mismo aire que el `FlowPane` actual: `hgap=10 vgap=8`). Ninguna regla existente se toca.

## Risks / Trade-offs

- [Los campos quedan por debajo de su `prefWidth` a 1024 (FACTURA ~103 px, CLIENTE ~122/~129 px)] → Mitigación: lo exigido son etiquetas intactas y distribución idéntica; los campos hacen scroll interno y sus mínimos dan para el contenido habitual (Serie, fechas, NIF, CP, provincias). La tarea de verificación visual a 1024 lo confirma.
- [`EditorBarraAccionesTest` itera los botones de la barra y podría asumir posiciones] → Mitigación: el test no afirma orden (solo contenencia y no-compresión), pero la tarea 1.2 lo re-ejecuta y lo ajusta si hace falta.
- [Un `DatePicker` o `ComboBox` con mínimo mayor que el hueco asignado desbordaría su `VBox`] → Mitigación: ni `VBox` ni tarjeta recortan por defecto; la verificación visual a 1024 y la suite completa lo detectarían.

## Migration Plan

No aplica: aplicación de escritorio sin despliegue ni datos que migrar. Reversión: revertir el commit.

## Verificación

- `mvn test` en verde, con atención a `EditorBarraAccionesTest`, `EditorTamanoMinimoTest` y `UiSmokeTest`.
- A mano a 1024×768 y maximizado: etiquetas del Editor enteras, campos con el mismo ancho en ambos tamaños, filtros del Histórico fijos, solo crecen las tablas, `Nueva` junto a `Guardar`.
