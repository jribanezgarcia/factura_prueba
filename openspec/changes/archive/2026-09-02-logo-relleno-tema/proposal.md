## Why

El recuadro que envuelve el logo de la empresa en el menú principal y en el editor se pinta hoy con el color del tema: blanco en los temas claros y prácticamente negro en los oscuros. Un logo con fondo blanco sobre un tema oscuro se ve como un rectángulo blanco flotando en una caja negra, con el borde del recuadro marcando la costura. El hueco a ambos lados del logo queda siempre en un color que no pega con la imagen.

## What Changes

- Rellenar el hueco del recuadro del logo con los colores del propio logo, para que imagen y recuadro se vean como una sola pieza sea cual sea el tema.
- Tres casos según la imagen:
  1. Logo con fondo plano y opaco (p. ej. blanco): el recuadro adopta ese color exacto en fondo y borde.
  2. Imagen opaca sin fondo plano (fotografía o degradado): el recuadro se rellena con una copia ampliada y desenfocada de la propia imagen, recortada a la forma del recuadro, con el borde en transparente.
  3. Imagen mayormente transparente (PNG con alfa): no se hace nada, manda el tema.
- En el editor, el logo pasa de estar suelto en el ToolBar a quedar contenido en un StackPane de tamaño fijo (110x40) con la clase `menu-logo-box`, con `fitHeight` además del `fitWidth` actual.
- Clasificación de la imagen muestreando solo el marco exterior (bandas de ~6% del lado), agrupando los píxeles opacos en cubos de color (5 bits por canal) y eligiendo el más frecuente, sin penalizar la carga con imágenes grandes (muestreo con paso).

## Capabilities

### New Capabilities

(ninguna)

### Modified Capabilities

- `invoicing`: se amplía el requisito «Identidad de empresa en la interfaz» para especificar cómo se rellena el recuadro del logo según el tipo de imagen (plano opaco, opaca sin fondo plano, o transparente) y que el logo del editor queda contenido en una caja de tamaño fijo.

## Impact

- `MenuPrincipal.fxml` / `MenuController.java`: recuadro del logo del menú.
- `Editor.fxml` / `EditorController.java`: envolver el logo en un StackPane de tamaño fijo y añadir `fitHeight`.
- Nuevo helper de clasificación de imagen y de aplicación/limpieza del recuadro (estilo inline, clip y respaldo difuminado), reutilizable por menú y editor.
- Tests nuevos con `WritableImage` (marco blanco, marco de color, transparente, ruido aleatorio, nula y 2x2) y de aplicación al StackPane.
- No se tocan `base.css` ni `tema-*.css`. Suite actual (126 tests) debe seguir en verde.
