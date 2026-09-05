## D1. Decisión: componer clases, no sustituirlas

La alternativa obvia —crear clases nuevas `.btn-ribbon-primary` / `.btn-ribbon-default` / `.btn-ribbon-danger`— obligaría a redefinir fondo, borde, texto y `:hover` en los **siete** temas, 21 reglas nuevas duplicando colores que ya están escritos.

En su lugar, cada botón conserva su clase actual y **añade** `btn-ribbon`:

```xml
<Button fx:id="btnGuardar" onAction="#guardar" styleClass="primary-button, btn-ribbon" text="Guardar">
```

`base.css` define en `.btn-ribbon` **solo geometría** (forma cuadrada, icono arriba, texto envuelto, anchura fija). Los colores siguen resolviéndose por `.primary-button` / `.action-button` / `.default-button` / `.action-danger-button`, que ya existen en los siete temas. Cada tema solo necesita decir de qué color va el icono.

`StyleClassSeparadorTest` parsea todos los FXML y exige coma en los `styleClass` múltiples: **siempre `"primary-button, btn-ribbon"`, nunca `"primary-button btn-ribbon"`**.

## D2. Decisión: iconos inline en FXML

Los iconos se declaran como `SVGPath` dentro del `<graphic>` de cada botón, siguiendo el precedente ya existente en `MenuPrincipal.fxml`:

```xml
<graphic><SVGPath styleClass="icono-boton" content="..."/></graphic>
```

Motivo: cero cambios en controladores y cero `fx:id` nuevos —varios botones de Editor e Histórico no tienen `fx:id` y asignar el icono desde Java obligaría a añadirlos—. El color se resuelve por CSS con `-fx-fill`, como ya ocurre con `.nav-icon` y `.menu-item .icono`.

Coste asumido: los paths de `Exportar PDF`, `Anular` y `Volver` aparecen literalmente dos veces, una por FXML. Es la misma duplicación que ya existe hoy entre `MenuPrincipal.fxml` y `BarraNavegacion.java`. No se introduce una clase catálogo en Java porque FXML no puede referenciar constantes Java desde un atributo.

## D3. Anchura fija y texto envuelto

`-fx-wrap-text: true` con `-fx-pref-width` fijo de 64 px es lo que hace que todos los botones midan igual pese a etiquetas de longitud muy distinta: `Rectificativa`, `Exportar PDF` y `Generar mensuales` caen a dos líneas. Es el mismo recurso que usa el ribbon de referencia con «Guardar y cerrar» o «Historial del documento». La anchura fija además hace trivial el cálculo de espacio de D4. Como la anchura fija obliga a que algunas etiquetas se partan en dos líneas, hace falta una altura mínima común (`-fx-min-height: 68`): sin ella los botones de dos líneas sobresalen sobre los de una.

## D4. Presupuesto de espacio (el riesgo real del change)

**Ancho, Editor a 1024 px** (el caso que verifica `EditorBarraAccionesTest`, con `btnAnular` visible y título largo):

| Concepto | px |
|---|---|
| 8 botones × 64 | 512 |
| 3 separadores ≈ 9 | 27 |
| logoBox + lblTitulo + chip ANULADA (110 + 130 + 80) | 320 |
| spacing 8 × 11 huecos | 88 |
| **Total** | **≈ 947** |

Ancho útil dentro de la `card` a 1024 px: ~980. Entra, con ~33 px de margen.

**Palancas si no entrase**, aplicar en este orden y parar en cuanto pase el test:

1. `-fx-pref-width` / `-fx-min-width` / `-fx-max-width` de `.btn-ribbon` de 64 a 60 (−32 px).
2. `-fx-spacing` de `.action-bar` de 8 a 6 (−22 px).
3. `maxWidth` de `lblTitulo` en `Editor.fxml` de 130 a 110 (−20 px).

**Alto**: la barra pasa de ~56 px a ~76 px. `EditorTamanoMinimoTest` exige que `#lblTotal` y `#txtObservaciones` quepan sin scroll a 768 px. **Palancas si no cabe**, en este orden:

1. Icono a 22×22 en lugar de 24×24 (bajar `-fx-scale` de `.btn-ribbon .icono-boton` de 1.05 a 0.95).
2. `-fx-padding` de `.btn-ribbon` de `6px 2px` a `4px 2px`.
3. `logoBox` en `Editor.fxml` de 40 a 36 px de alto (los tres atributos: `prefHeight`, `maxHeight`, `minHeight`).

## D5. Valores exactos

Usar estos valores tal cual. No reinterpretarlos.

### D5-A. Bloque nuevo en `themes/base.css`

Insertar **después** del bloque «Botones base» (tras la regla que agrupa `.primary-button, .default-button, ...`), sin modificar ninguna regla existente:

```css
/* Botones de barra de acciones estilo ribbon: icono arriba, texto debajo */
.btn-ribbon {
    -fx-content-display: top;
    -fx-graphic-text-gap: 4px;
    -fx-font-size: 10px;
    -fx-alignment: CENTER;
    -fx-text-alignment: center;
    -fx-wrap-text: true;
    -fx-min-width: 64;
    -fx-pref-width: 64;
    -fx-max-width: 64;
    -fx-min-height: 68;
    -fx-padding: 6px 2px;
    -fx-background-radius: 4px;
    -fx-border-radius: 4px;
}

.btn-ribbon .icono-boton {
    -fx-scale-x: 1.05;
    -fx-scale-y: 1.05;
}

.ribbon-sep {
    -fx-padding: 0 2px;
}
```

### D5-B. Tres líneas por tema

En cada uno de los siete `themes/tema-*.css`, añadir junto a la línea que ya define `.menu-item .icono`. El color de la primera línea es el mismo que ese tema ya usa en `.menu-item .icono`; el de la tercera es el mismo que ya usa en `.danger-button` como `-fx-text-fill`.

| tema | acento (default/action) | primary | danger |
|---|---|---|---|
| `tema-biblioteca8.css` | `#296796` | `#FFFFFF` | `#C0392B` |
| `tema-esmeralda.css` | `#10B981` | leer de su `.primary-button` | leer de su `.danger-button` |
| `tema-negro-dorado.css` | `#D4AF37` | leer de su `.primary-button` | leer de su `.danger-button` |
| `tema-neon.css` | `#8B5CF6` | leer de su `.primary-button` | leer de su `.danger-button` |
| `tema-omarchy.css` | `#7D82D9` | leer de su `.primary-button` | leer de su `.danger-button` |
| `tema-sakura.css` | `#B5567E` | leer de su `.primary-button` | leer de su `.danger-button` |
| `tema-terracota.css` | `#C2542C` | leer de su `.primary-button` | leer de su `.danger-button` |

Donde pone «leer de»: tomar el `-fx-text-fill` que ese fichero ya declara en esa regla y usar ese mismo hex. No inventar colores nuevos ni reutilizar los de biblioteca8.

Plantilla (ejemplo con los valores de `tema-biblioteca8.css`):

```css
.default-button .icono-boton, .action-button .icono-boton { -fx-fill: #296796; }
.primary-button .icono-boton { -fx-fill: #FFFFFF; }
.danger-button .icono-boton, .action-danger-button .icono-boton { -fx-fill: #C0392B; }
```

### D5-C. Catálogo de iconos (Material Symbols, viewBox 0 0 24 24)

Mismo sistema de coordenadas que los paths que ya hay en `BarraNavegacion.java`.

- **GUARDAR** (`save`)

  `M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z`

- **NUEVA** (`note_add`)

  `M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 14h-3v3h-2v-3H8v-2h3v-3h2v3h3v2zm-3-7V3.5L18.5 9H13z`

- **PDF** (`picture_as_pdf`)

  `M20 2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-8.5 7.5c0 .83-.67 1.5-1.5 1.5H9v2H7.5V7H10c.83 0 1.5.67 1.5 1.5v1zm5 2c0 .83-.67 1.5-1.5 1.5h-2.5V7H15c.83 0 1.5.67 1.5 1.5v3zm4-3H19v1h1.5V11H19v2h-1.5V7h3v1.5zM9 9.5h1v-1H9v1zM4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm10 5.5h1v-3h-1v3z`

- **VERSIONES** (`history`)

  `M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z`

- **RECTIFICATIVA** (`assignment_return` — documento con flecha de devolución, el equivalente semántico de la factura de abono)

  `M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm5 12h-3v3l-5-5 5-5v3h3v4z`

- **ANULAR** (`block`)

  `M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zM4 12c0-4.42 3.58-8 8-8 1.85 0 3.55.63 4.9 1.69L5.69 16.9C4.63 15.55 4 13.85 4 12zm8 8c-1.85 0-3.55-.63-4.9-1.69L18.31 7.1C19.37 8.45 20 10.15 20 12c0 4.42-3.58 8-8 8z`

- **RESTAURAR** (`unarchive` — la única silueta de la barra que no se repite y con la altura de la mediana, 18)

  `M20.55 5.22l-1.39-1.68C18.88 3.21 18.47 3 18 3H6c-.47 0-.88.21-1.15.55L3.46 5.22C3.17 5.57 3 6.01 3 6.5V19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6.5c0-.49-.17-.93-.45-1.28zM12 9.5l5.5 5.5H14v2h-4v-2H6.5L12 9.5zM5.12 5l.82-1h12l.93 1H5.12z`

- **VOLVER** (`arrow_back`)

  `M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z`

- **BUSCAR** (`search`)

  `M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z`

- **MENSUALES** (`date_range`)

  `M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z`

- **ELIMINAR** (`delete`)

  `M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z`

Ninguno de estos paths procede de `capturas_pantalla/Iconos.zip`: ese zip contiene iconos propietarios de terceros y se ha usado solo como referencia de qué icono corresponde a qué acción.

### D5-D. Barra del Editor (`ui/Editor.fxml`)

Añadir el import `<?import javafx.scene.shape.*?>` junto a los demás. `Separator` ya está cubierto por `javafx.scene.control.*`.

Orden final de los hijos de la `HBox styleClass="action-bar"`, conservando intactos `logoBox`, `lblTitulo`, `lblEstado` y el `Region` de empuje:

```
logoBox · lblTitulo · lblEstado · Region(hgrow=ALWAYS)
btnGuardar · Nueva
| Exportar PDF · Versiones · Rectificativa
| Anular · Restaurar
| Volver
```

Los tres `|` son `<Separator orientation="VERTICAL" styleClass="ribbon-sep"/>`.

Clases e iconos, uno por línea. Todo lo demás —`fx:id`, `onAction`, `text`, tooltips— se conserva literalmente:

| botón | styleClass | icono |
|---|---|---|
| `btnGuardar` "Guardar" | `primary-button, btn-ribbon` | GUARDAR |
| "Nueva" | `action-button, btn-ribbon` | NUEVA |
| `btnExportar` "Exportar PDF" | `action-button, btn-ribbon` | PDF |
| `btnVersiones` "Versiones" | `action-button, btn-ribbon` | VERSIONES |
| `btnRectificativa` "Rectificativa" | `action-button, btn-ribbon` | RECTIFICATIVA |
| `btnAnular` "Anular" | `action-danger-button, btn-ribbon` | ANULAR |
| `btnRestaurar` "Restaurar" | `action-button, btn-ribbon` | RESTAURAR |
| "Volver" | `action-button, btn-ribbon` | VOLVER |

Ejemplo literal del primero:

```xml
<Button fx:id="btnGuardar" onAction="#guardar" styleClass="primary-button, btn-ribbon" text="Guardar">
    <graphic>
        <SVGPath styleClass="icono-boton" content="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/>
    </graphic>
</Button>
```

`btnGuardar` mantiene delante su `Region HBox.hgrow="ALWAYS"`.

### D5-E. Barra del Histórico (`ui/Historico.fxml`)

Añadir el import `<?import javafx.scene.shape.*?>`.

La `HBox spacing="8" alignment="CENTER_RIGHT"` pasa a este orden. `Buscar` sube al principio porque es la acción que cierra el bloque de filtros que tiene justo encima:

```
Buscar · Generar mensuales
| Exportar PDF · Anular · Eliminar
| Volver
```

| botón | styleClass | icono |
|---|---|---|
| "Buscar" (`#buscar`) | `primary-button, btn-ribbon` | BUSCAR |
| "Generar mensuales" (`#generarMensual`) | `default-button, btn-ribbon` | MENSUALES |
| `btnExportarPdf` "Exportar PDF" | `default-button, btn-ribbon` | PDF |
| `btnAnular` "Anular" | `default-button, btn-ribbon` | ANULAR |
| `btnBorrar` "Eliminar" | `default-button, btn-ribbon` | ELIMINAR |
| "Volver" (`#volver`) | `default-button, btn-ribbon` | VOLVER |

`btnAnular` y `btnBorrar` conservan `default-button`: en el Histórico operan sobre selección múltiple y hoy no son botones de peligro; este change no cambia esa decisión.

## D6. Fuera de alcance

Clientes, Configuración, Backup, Versiones, Arranque, `GenerarFacturasMensuales.fxml`, los `ButtonType` de `Dialogos.java` y `ClientesController`, y los botones `Añadir línea` / `Eliminar línea` del Editor. Se abordarán en un change posterior una vez visto el resultado de este.
