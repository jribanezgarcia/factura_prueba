## Why

La barra de navegación superior, presente en todas las pantallas salvo el menú principal, es de **iconos sin texto**: `BarraNavegacion.boton()` (`:50-58`) crea un `Button` con un `SVGPath` como gráfico y un `Tooltip`, y nada más.

Eso tiene dos costes:

**Para cualquiera que la use**, siete siluetas —casa, lápiz, portapapeles, persona, engranaje, flecha hacia abajo, puerta— obligan a adivinar o a esperar el tooltip. El engranaje y el portapapeles son razonablemente universales; el lápiz para «Nueva factura» y la flecha para «Copia de seguridad» no lo son en absoluto. El usuario ha pedido expresamente texto bajo cada icono.

**Para un lector de pantalla**, esos siete botones **no tienen nombre**. Un `Button` cuyo único contenido es un `SVGPath` no expone texto accesible, y el `Tooltip` no lo suple: no se anuncia como nombre del control. Hoy la barra de navegación de toda la aplicación es, para ese usuario, siete botones sin identificar.

## What Changes

- Cada botón de la barra de navegación SHALL mostrar una etiqueta de texto **bajo** su icono.
- El texto visible es corto; el tooltip conserva el nombre completo («Menú principal», «Copia de seguridad»).
- Los siete temas reciben el color de ese texto, a juego con el del icono.
- El alto que gana la barra se compensa recortando su relleno vertical, para no comprometer el espacio del editor.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Menú y navegación» para que la barra de navegación lleve texto bajo cada icono.

## Impact

- `ui/BarraNavegacion`: el botón pasa a llevar texto con el gráfico encima.
- `themes/base.css`: tamaño de fuente y separación icono-texto, y relleno vertical de `.nav-bar`.
- `themes/tema-*.css`: **los siete**, una línea cada uno con el color del texto.
- Afecta visualmente a las seis pantallas que llevan la barra: Editor, Histórico, Clientes, Configuración, Versiones y Copias.
- No se toca lógica de navegación, ni destinos, ni servicios.

### El riesgo real: el alto del editor

`.nav-bar` mide hoy unos 67 px (`-fx-padding: 10px 0 16px 0`, botón `4px 8px` más 3 px de borde inferior, icono de 24 px escalado a 1,25). Añadir una etiqueta suma unos 16 px.

El editor va justo de alto: todo el trabajo del change `editor-sin-scroll-factura-corta` fue para que una factura corta quepa en 768 px con los totales visibles. `EditorTamanoMinimoTest` exige que `#lblTotal` y `#txtObservaciones` terminen dentro de la escena y que la tabla conserve al menos 200 px. Esos 16 px saldrían de la tabla, que es la que crece con `VBox.vgrow="ALWAYS"`.

Por eso el change incluye recortar el relleno de `.nav-bar` como parte del mismo trabajo, no como un extra opcional.
