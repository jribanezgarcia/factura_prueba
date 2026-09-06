## D1. Decisión: quitar la caja, no rehacer los botones

El aspecto que se busca ya existe en la aplicación: `.nav-button` en `base.css:93`.

```css
.nav-button {
    -fx-background-color: transparent;
    -fx-background-insets: 0;
    -fx-border-width: 0 0 3 0;
    -fx-border-color: transparent;
    ...
}
.nav-button:hover { -fx-background-color: rgba(128,128,128,0.10); }
```

El fondo lo pone la barra (`.nav-bar`, que sí tiene color en cada tema); el botón no pinta nada hasta que se pasa el ratón por encima. Este change traslada ese mismo mecanismo a `.btn-ribbon`: transparencia en reposo, velo translúcido en *hover*.

No se crean clases nuevas ni se toca ningún FXML. Los 14 botones ya llevan `btn-ribbon` y esa clase **solo la usan ellos** (`Editor.fxml`: 8, `Historico.fxml`: 6; ningún otro FXML la menciona). Por tanto todo lo que se escriba contra `btn-ribbon` alcanza exactamente al ámbito del change y a nada más.

## D2. El problema real: quién gana, `base.css` o el tema

`ThemeManager.hojas()` mete en la escena `base.css` y, después, `tema-<activo>.css`. Las dos son hojas de autor sobre el mismo `Scene`, así que entre reglas de **igual** especificidad gana la última: el tema.

Hoy el fondo blanco lo pone el tema:

```css
/* tema-biblioteca8.css:28 */
.action-button { -fx-background-color: #FFFFFF; -fx-text-fill: #1F2937; -fx-border-color: #D8DBDF; }
```

Una regla `.btn-ribbon { -fx-background-color: transparent; }` en `base.css` **no serviría**: misma especificidad (una clase), y el tema va después. Por eso las reglas del change son **compuestas**:

```css
.action-button.btn-ribbon { -fx-background-color: transparent; }
```

Dos clases contra una: gana `base.css` con independencia del orden de las hojas. Lo mismo con `:hover`, donde la pseudoclase cuenta como una clase más: `.action-button:hover` (2) pierde contra `.action-button.btn-ribbon:hover` (3).

**Palanca de reserva** si por lo que sea la transparencia no se aplicara: mover el bloque completo al final de cada uno de los siete `tema-*.css`, donde gana por orden. Es una duplicación fea de 7 x 20 líneas y solo debe hacerse si se comprueba que la vía compuesta falla; antes de recurrir a ella, verificar que el `styleClass` del FXML lleva realmente las dos clases separadas por coma.

## D3. La variante principal es la única que necesita colores nuevos

Los colores de icono y texto de cada variante se eligieron en su día contra el fondo del propio botón:

| Variante | Fondo hoy | Texto hoy | Icono hoy | ¿Sirve sin fondo? |
|---|---|---|---|---|
| `primary-button` | acento del tema | blanco / casi negro | blanco / casi negro | **No** |
| `default-button` / `action-button` | blanco o panel oscuro | texto del tema | acento del tema | Sí |
| `action-danger-button` | blanco o panel oscuro | rojo o crema | rojo de peligro | Sí |

En `Guardar` y `Buscar` el texto está pensado para leerse *encima* del acento: blanco en biblioteca8, esmeralda, sakura y terracota; casi negro en negro-dorado, neón y omarchy. Al quitar el fondo quedaría blanco sobre gris claro (invisible) o negro sobre panel oscuro (invisible). Hay que remapearlos.

Las otras dos variantes ya se leen sobre el fondo de la barra, porque el blanco del botón y el gris de la barra tienen luminancia parecida en los cuatro temas claros, y en los tres oscuros el fondo del botón (`#1B1B1E`, `#1D2347`, `#1B2350`) es prácticamente el de la barra. No se tocan.

## D4. Por qué no hay que editar los siete temas

El remapeo de la variante principal necesita el color de acento de cada tema. En lugar de escribir siete pares de líneas, se usa `-fx-accent`, que los siete declaran en su `.root` y que coincide **exactamente** con el fondo que hoy usa `.primary-button`:

| Tema | `-fx-accent` | fondo de `.primary-button` |
|---|---|---|
| biblioteca8 | `#296796` | `#296796` |
| esmeralda | `#10B981` | `#10B981` |
| negro-dorado | `#D4AF37` | `#D4AF37` |
| neon | `#8B5CF6` | `#8B5CF6` |
| omarchy | `#7D82D9` | `#7D82D9` |
| sakura | `#B5567E` | `#B5567E` |
| terracota | `#C2542C` | `#C2542C` |

Es decir: el botón principal deja de ser *acento de fondo con texto neutro* y pasa a ser *acento en el trazo*, con el mismo color, en los siete temas y sin tocar ninguno. Se le añade `-fx-font-weight: bold` porque, perdido el fondo, la negrita es lo único que lo separa de un botón secundario.

## D5. Foco de teclado

En reposo el borde pasa a transparente, y el anillo de foco por defecto de JavaFX se dibuja sobre las capas de fondo del botón, que aquí desaparecen. El Editor se navega con teclado (`EditorFlujoTecladoTest`), así que el foco no puede quedarse sin señal: `:focused` repinta el borde —que sigue teniendo `-fx-border-width: 1` de la regla «Botones base»— en color de acento.

Los botones deshabilitados (`Versiones` y `Rectificativa` en una factura sin guardar, `Exportar PDF` sin selección) siguen atenuándose con la opacidad 0.4 por defecto de JavaFX. Sobre un botón plano eso se lee como icono y texto desvaídos, que es el comportamiento normal de una barra plana. No hace falta regla propia.

## D6. La banda de la `.action-bar` se queda

`.action-bar` tiene fondo propio en los siete temas (`#E4E7EA` en biblioteca8, `#1B1B1E` en negro-dorado...) mientras que la tarjeta que la envuelve es `#F6F6F6`. Se mantiene tal cual, porque es el paralelo exacto de la barra de navegación: banda con color, botones transparentes encima. En el Histórico no hay banda —los botones cuelgan directamente de la tarjeta `.panel-busqueda`— y ahí los botones adoptarán el `#F6F6F6` de la tarjeta.

Si al ver el resultado se prefiere que la barra del Editor desaparezca del todo, la tarea 5 (opcional, **no ejecutar sin decisión expresa del usuario**) lo consigue con una regla compuesta que gana por el mismo mecanismo de D2:

```css
.panel-busqueda .action-bar { -fx-background-color: transparent; -fx-border-color: transparent; }
```

## D7. Valores exactos

Usar este bloque tal cual. No reinterpretarlo ni añadir variantes.

### D7-A. Bloque nuevo en `themes/base.css`

Insertar **después** de la regla `.ribbon-sep` y **antes** del comentario `/* Tablas */`, sin modificar ninguna regla existente:

```css
/* Variante plana: el boton no pinta caja, solo icono y texto sobre el fondo de la barra */
.primary-button.btn-ribbon,
.default-button.btn-ribbon,
.danger-button.btn-ribbon,
.action-button.btn-ribbon,
.action-danger-button.btn-ribbon {
    -fx-background-color: transparent;
    -fx-background-insets: 0;
    -fx-border-color: transparent;
}

.primary-button.btn-ribbon {
    -fx-text-fill: -fx-accent;
    -fx-font-weight: bold;
}

.primary-button.btn-ribbon .icono-boton {
    -fx-fill: -fx-accent;
}

.primary-button.btn-ribbon:hover,
.default-button.btn-ribbon:hover,
.danger-button.btn-ribbon:hover,
.action-button.btn-ribbon:hover,
.action-danger-button.btn-ribbon:hover {
    -fx-background-color: rgba(128, 128, 128, 0.18);
    -fx-border-color: transparent;
}

.primary-button.btn-ribbon:pressed,
.default-button.btn-ribbon:pressed,
.danger-button.btn-ribbon:pressed,
.action-button.btn-ribbon:pressed,
.action-danger-button.btn-ribbon:pressed {
    -fx-background-color: rgba(128, 128, 128, 0.28);
}

.primary-button.btn-ribbon:focused,
.default-button.btn-ribbon:focused,
.danger-button.btn-ribbon:focused,
.action-button.btn-ribbon:focused,
.action-danger-button.btn-ribbon:focused {
    -fx-border-color: -fx-accent;
}
```

`rgba(128, 128, 128, ...)` es gris neutro a propósito: aclara sobre fondo oscuro y oscurece sobre fondo claro, así que el mismo valor funciona en los siete temas. Es el mismo recurso que ya usa `.nav-button:hover` en `base.css`, con algo más de opacidad porque aquí no hay borde que ayude a delimitar el botón.

### D7-B. Lo que NO se toca

- Ningún `tema-*.css`.
- Ningún `.fxml`.
- Ningún `.java`.
- Las reglas `.btn-ribbon`, `.btn-ribbon .icono-boton`, `.ribbon-sep`, `.action-bar` y «Botones base» de `base.css`.
- Los botones `Añadir línea` y `Eliminar línea` de `Editor.fxml`, que son `.action-button` **sin** `btn-ribbon` y por tanto quedan fuera del alcance de las reglas compuestas por construcción.
