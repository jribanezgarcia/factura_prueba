## Why

La barra de acciones del Editor ocupa más de lo que necesita, en las dos dimensiones.

A lo ancho, `Rectificativa` mide 63 píxeles y `Exportar PDF` 68, contra unos 57 aprovechables. Para que cupieran se ensancharon los botones a 86, y esos 86 por ocho botones se comieron 688 de los 944 disponibles. De ahí salió todo lo demás: el logo de empresa encogido a 80, la separación de la barra bajada de 8 a 4 —que deja el título pegado al logo— y el título recortándose con puntos suspensivos en cuanto la factura tiene número real.

A lo alto, el botón reserva un mínimo de 78 píxeles cuando su contenido pide 58. Ese mínimo se puso para que cupieran dos líneas de etiqueta.

Las dos cosas tienen la misma raíz: etiquetas que no caben. Y una de ellas, además, incumple un requisito vigente: el criterio de etiquetado dice que el botón SHALL nombrar la **acción**, y `Rectificativa` es un sustantivo.

## What Changes

- Los botones de las barras de acciones SHALL llamarse `Exportar`, `Rectificar` y `Facturar mes`, en lugar de `Exportar PDF`, `Rectificativa` y `Generar mensuales`. El icono ya identifica el formato y el objeto.
- Los botones SHALL estrecharse de 86 a 72, anchura suficiente para que toda etiqueta de una palabra quepa en una línea.
- Los botones SHALL NOT reservar un alto mínimo por encima de su contenido.
- La separación de la barra SHALL volver de 4 a 8, y el identificador de empresa SHALL recuperar su anchura de 110.
- El título de la factura SHALL envolverse en varias líneas en lugar de recortarse con puntos suspensivos.
- No cambia ninguna acción, ningún icono ni ningún flujo.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: siete requisitos nombran estos botones por su etiqueta o dependen del tamaño de la barra. Se corrigen todos para que sigan describiendo la aplicación.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: `.btn-ribbon` y `.action-bar`.
- `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`: dos etiquetas, el ancho del logo y el título.
- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`: dos etiquetas.
- Ningún `.java`: las coincidencias de «Rectificativa» en el código son del dominio, no textos de botón.
