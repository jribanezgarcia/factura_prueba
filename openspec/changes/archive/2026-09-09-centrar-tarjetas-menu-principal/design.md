## Context

`MenuPrincipal.fxml` es un `BorderPane` de 1024x768 con la fila de la fecha de trabajo en el `top` y, en el `center`, un `StackPane` (línea 23) que envuelve un `HBox` (línea 24) con las dos tarjetas: la de empresa, de 300 de ancho fijo, y la del menú de opciones, de 430.

El `StackPane` recibe todo el alto disponible del `BorderPane`. El `HBox` declara `maxWidth="-Infinity"` y `maxHeight="-Infinity"`, así que se queda en su tamaño preferido y el `StackPane` se limita a colocarlo. Dónde lo coloca lo decide su `alignment`, hoy `TOP_CENTER`: centrado en horizontal, anclado arriba en vertical.

No hay ningún `ScrollPane` ni `AnchorPane` en la pantalla, ni ningún `vgrow`/`hgrow` en todo el archivo, ni CSS que toque la alineación: `.zona-contenido` solo aporta `-fx-padding: 16px`.

## Goals / Non-Goals

**Goals:**

- Que el bloque quede centrado en los dos ejes y siga centrado al redimensionar.

**Non-Goals:**

- No se cambia el tamaño de las tarjetas ni se las hace crecer con la ventana.
- No se cambia cómo se alinean las dos tarjetas entre sí.
- No se toca la fila de la fecha de trabajo del `top`.
- No se tocan las demás pantallas.

## Decisions

### D1. Decisión: cambiar el `alignment`, no reestructurar

Basta `alignment="CENTER"` en el `StackPane` de la línea 23. El horizontal ya funciona; `CENTER` solo cambia el reparto vertical.

Alternativa descartada: sustituir el `StackPane` por un `VBox alignment="CENTER"`. Haría lo mismo con más cambio y perdiendo la propiedad del `StackPane` de superponer hijos, que hoy no se usa pero es la razón de que esté ahí.

### D2. Decisión: el `HBox` conserva `TOP_LEFT`

Ese valor rige cómo se alinean las dos tarjetas **entre sí**, no dónde va el bloque. La tarjeta de menú es más alta que la de empresa; alinearlas por arriba es lo correcto. Centrarlas entre sí dejaría el logo de la empresa flotando a media altura respecto a la primera opción del menú, que es peor de lo que hay ahora.

### D3. Decisión: las tarjetas no crecen

No se añade `VBox.vgrow` ni `HBox.hgrow`. Los anchos de 300 y 430 son fijos a propósito y las alturas son las naturales de su contenido. Centrar es repartir el hueco sobrante, no rellenarlo.

## Verificación

- El test nuevo debe fallar si se repone `TOP_CENTER`. Conviene comprobarlo y reponer el valor bueno, como se hizo con `trocear`.
- A ojo: abrir el menú y arrastrar la ventana desde 1024x768 hasta maximizada, comprobando que el bloque se mantiene centrado sin saltos.
- Comprobar que la fila de la fecha de trabajo sigue donde estaba.
