## Why

En el menú principal, el bloque formado por la tarjeta de información de la empresa y la tarjeta con las opciones del programa queda pegado a la parte superior de la ventana. Todo el espacio que sobra se acumula debajo, así que al agrandar la ventana el conjunto se lee descolgado en lugar de compuesto.

La causa está en una sola línea: el `StackPane` que envuelve el bloque (`MenuPrincipal.fxml:23`) declara `alignment="TOP_CENTER"`. Eso centra en horizontal y ancla arriba en vertical, que es exactamente lo que se ve.

Ningún requisito habla hoy de dónde queda ese bloque. Los requisitos de tamaño de ventana solo garantizan que nada se recorte, no cómo se compone la pantalla.

## What Changes

- El bloque de tarjetas del menú principal SHALL mostrarse centrado en los dos ejes dentro del espacio disponible, y SHALL seguir centrado al redimensionar la ventana.
- Las dos tarjetas SHALL conservar su tamaño natural, sin estirarse con la ventana, y SHALL seguir alineadas entre sí por su borde superior.
- No cambia ningún control, ningún texto ni ningún comportamiento de la pantalla.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se añade un requisito de composición del menú principal, que hasta ahora no estaba recogido.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/MenuPrincipal.fxml`: un atributo.
- `src/test/java/com/alcazaba/facturacion/ui/MenuLayoutTest.java` (nuevo).
- Ningún CSS, ningún `.java` de producción y ninguna otra pantalla.
