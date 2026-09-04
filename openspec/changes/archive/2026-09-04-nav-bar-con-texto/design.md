## Context

`BarraNavegacion.crear(nav, actual)` monta un `HBox` con clase `.nav-bar` y siete botones `.nav-button`, cada uno con un `SVGPath` `.nav-icon` como gráfico y un `Tooltip`. Los temas pintan el fondo de la barra y el relleno del icono. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que cada icono lleve debajo el nombre de su destino.
- Que la barra siga siendo legible en los siete temas, que tienen fondos muy distintos.
- Que el editor siga cabiendo en 768 px con los totales visibles.
- Que los botones dejen de ser anónimos para un lector de pantalla.

**Non-Goals:**

- No se cambian los iconos ni los destinos.
- No se toca la barra de acciones del editor: eso es `fix-barra-acciones-editor`.
- No se añade la barra al menú principal, que sigue sin llevarla.

## Decisions

### D1. Un `Button` con `ContentDisplay.TOP`, no un `VBox`

La tentación es envolver icono y etiqueta en un `VBox`. Es peor: habría que replicar el hover, el foco y el subrayado de `.activo` sobre el contenedor, y el conjunto dejaría de ser un solo control.

Basta con que el botón siga siendo un `Button`, se le dé `setText(...)` y `setContentDisplay(ContentDisplay.TOP)`. JavaFX coloca el gráfico encima del texto de forma nativa. Se conserva `.nav-button` entera —hover, `:activo`, cursor— y, como el botón pasa a tener texto, **gana nombre accesible sin código adicional**. `-fx-graphic-text-gap` gobierna la separación.

### D2. Etiqueta corta visible, nombre completo en el tooltip

Siete etiquetas largas ensancharían la barra y la volverían pesada. El texto visible se queda en una palabra donde se pueda:

| Destino | Etiqueta | Tooltip |
|---|---|---|
| Menú principal | Inicio | Menú principal |
| Nueva factura | Nueva | Nueva factura |
| Histórico | Histórico | Histórico |
| Clientes | Clientes | Clientes |
| Configuración | Configuración | Configuración |
| Copia de seguridad | Copias | Copia de seguridad |
| Salir | Salir | Salir |

Es el mismo patrón que el resto del trabajo de etiquetas: corto a la vista, completo al posarse.

### D3. El color del texto hay que ponerlo en los siete temas

`.nav-bar` tiene fondo de color propio en cada tema (`#296796`, `#10B981`, `#121214`, `#151A3A`, `#7D82D9`, `#B5567E`, `#C2542C`) y el icono se pinta con `-fx-fill` a juego (`tema-*.css:16-17`).

El texto de un `Button` usa `-fx-text-fill`, que es **otra propiedad**: no hereda de `-fx-fill`. Si no se añade, el texto sale con el color por defecto de Modena sobre esos fondos, y en varios temas será ilegible —negro sobre `#121214`, por ejemplo—.

Son siete ediciones de una línea, con el mismo color que ya lleva `.nav-icon` en cada tema. Es mecánico pero **no es opcional**, y es la parte del change que más fácil se olvida.

Se descarta introducir una variable compartida: los temas hardcodean hex y unificarlos ahora sería un cambio de alcance mucho mayor, con riesgo de tocar todo lo demás.

### D4. Presupuesto vertical

Ganancia estimada de la etiqueta: ~16 px con fuente de 10 px.

Compensación: `.nav-bar` pasa de `-fx-padding: 10px 0 16px 0` a `6px 0 8px 0`, es decir −12 px. Crecimiento neto ~4 px, que la tabla del editor puede absorber.

La comprobación es barata y ya existe: `EditorTamanoMinimoTest` falla si `#lblTotal` o `#txtObservaciones` se salen de la escena o si la tabla baja de 200 px. **Si ese test pasa, cabe.** No hace falta medir a mano.

Si aun así no cupiera, la siguiente palanca es bajar la fuente de la etiqueta o reducir el escalado del icono de 1,25 a 1,1; conviene tocarlas en ese orden y no recortar más el relleno, que es lo que da respiro visual a la barra.

### D5. Ancho

Siete etiquetas suman aproximadamente 450 px de texto más el `-fx-spacing: 34px` entre botones (204 px), unos 650 px sobre los ~992 disponibles a 1024. Cabe, pero con las etiquetas los botones son mucho más anchos que antes y 34 px de separación quedarán excesivos: conviene bajar el espaciado al revisarlo visualmente.

### D6. Valores exactos

**Texto y tooltip de cada botón**, en el orden en que los monta `BarraNavegacion.crear(...)`:

| Destino | `setText(...)` | `Tooltip` |
|---|---|---|
| Menú principal | `Inicio` | Menú principal |
| Nueva factura | `Nueva` | Nueva factura |
| Histórico | `Histórico` | Histórico |
| Clientes | `Clientes` | Clientes |
| Configuración | `Configuración` | Configuración |
| Copia de seguridad | `Copias` | Copia de seguridad |
| Salir | `Salir` | Salir |

**En Java** solo el texto y el tooltip. La disposición va en CSS, que es donde vive el resto del aspecto de la barra:

```java
b.setText(etiqueta);
b.setTooltip(new Tooltip(nombreCompleto));
```

**En `base.css`**, sobre las reglas que ya existen:

```css
.nav-bar {
    -fx-padding: 6px 0 8px 0;   /* antes: 10px 0 16px 0 */
    -fx-spacing: 22px;          /* antes: 34px */
}

.nav-button {
    -fx-content-display: top;
    -fx-graphic-text-gap: 3px;
    -fx-font-size: 10px;
}
```

`-fx-content-display: top` en CSS evita tener que llamar a `setContentDisplay(...)` desde Java. El resto de `.nav-button` —fondo transparente, `-fx-padding: 4px 8px`, borde inferior de 3 px, radio y cursor— **no se toca**, ni tampoco el `-fx-scale-x/y: 1.25` de `.nav-icon`.

El espaciado baja de 34 a 22 px porque con rótulo los botones son bastante más anchos y 34 px separan de más. Es el valor a ajustar a ojo si no convence.

**Color del texto, uno por tema.** Cada `tema-*.css` recibe **una línea nueva**, junto a la de `.nav-button .nav-icon` que ya tiene, con el mismo color:

| Tema | Fondo de la barra | Línea a añadir |
|---|---|---|
| biblioteca8 | `#296796` | `.nav-button { -fx-text-fill: #FCF9F9; }` |
| esmeralda | `#10B981` | `.nav-button { -fx-text-fill: #FFFFFF; }` |
| negro-dorado | `#121214` | `.nav-button { -fx-text-fill: #D4AF37; }` |
| neon | `#151A3A` | `.nav-button { -fx-text-fill: #A78BFA; }` |
| omarchy | `#7D82D9` | `.nav-button { -fx-text-fill: #060B1E; }` |
| sakura | `#B5567E` | `.nav-button { -fx-text-fill: #FFFFFF; }` |
| terracota | `#C2542C` | `.nav-button { -fx-text-fill: #FFFFFF; }` |

Funciona sobre `.nav-button` directamente porque `Button` honra `-fx-text-fill`; no hace falta bajar a `.label`.

**Cuenta del alto.** Rótulo de 10 px ≈ 13 px de línea, más 3 px de `graphic-text-gap` = ~16 px. El recorte de relleno resta 12 px (4 arriba, 8 abajo). Neto ~+4 px sobre los ~67 px actuales.

Los dos temas con el contraste más ajustado son **omarchy** (texto `#060B1E` sobre `#7D82D9`) y **neon** (`#A78BFA` sobre `#151A3A`); son los primeros que hay que mirar en la comprobación visual.

## Verificación

- `EditorTamanoMinimoTest` es la prueba de que el editor sigue cabiendo; debe pasar sin tocarlo.
- Recorrer las seis pantallas con barra de navegación en **los siete temas** a 1024x768, comprobando el contraste del texto sobre el fondo de la barra.
- Comprobar que el subrayado de la pantalla activa (`.nav-button.activo`) sigue viéndose bien ahora que el botón es más alto.
