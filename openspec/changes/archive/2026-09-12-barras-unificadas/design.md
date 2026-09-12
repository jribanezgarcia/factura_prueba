## Context

Las tres pantallas montan hoy la misma estructura en su `top`: `VBox` con la barra de navegación y, debajo, una tarjeta `card, panel-busqueda`.

- **Editor**: dentro de la tarjeta, una `HBox styleClass="action-bar"` con logo, título y los ocho botones. `action-bar` es la que pinta la franja: en `base.css` es transparente con borde redondeado de 10px, y cada tema le da su color (en Biblioteca8, `#E4E7EA` sobre el `#F6F6F6` de la tarjeta).
- **Clientes**: una sola `HBox` con el buscador a la izquierda, un `Region` que empuja y los cuatro botones a la derecha. Sin franja.
- **Histórico**: `GridPane` de filtros arriba y, debajo, una `HBox` de botones alineada a la derecha. Sin franja.

## Goals / Non-Goals

**Goals:** que Clientes y el Histórico tengan la franja de acciones del Editor, con los iconos en la primera fila y los campos debajo.

**Non-Goals:** cambiar botones, iconos, textos, acciones o el orden de los filtros; tocar el Editor; tocar los siete temas.

## Decisions

### D1. Reutilizar `action-bar`, no crear una clase nueva

La franja del Editor ya existe y los siete temas ya la colorean. Añadir `action-bar` a la `HBox` de botones de Clientes y del Histórico da la misma franja en todos los temas sin escribir una sola línea de CSS de tema. Una clase nueva obligaría a tocar los siete.

`action-bar` trae además `-fx-padding: 8px` y `-fx-spacing: 8px`, que es exactamente el espaciado que esas dos `HBox` declaran hoy a mano en el FXML. Al añadir la clase se quitan sus `spacing="8"`.

### D2. Los botones a la izquierda, no a la derecha

En el Editor los botones arrancan después del título y ocupan la franja de izquierda a derecha. En Clientes van hoy pegados a la derecha con un `Region` de por medio, y en el Histórico con `alignment="CENTER_RIGHT"`. Al pasar a fila propia se quedan sin nada a su izquierda, así que el `Region` desaparece en Clientes y la alineación pasa a `CENTER_LEFT` en ambas: es lo que hace el Editor y lo que pidió el usuario para los filtros.

### D3. El conteo de clientes baja con el buscador

`lblConteo` está hoy entre el buscador y los botones. Es información sobre la lista, no una acción: baja a la fila de abajo, junto al campo de búsqueda, en el mismo sitio relativo.

### D4. Los filtros del Histórico no se reordenan

El requisito «Distribución estable al redimensionar en Editor e Histórico» fija que los filtros mantengan siempre las mismas filas y posiciones. El `GridPane` sigue igual, con su `maxWidth="-Infinity"` que ya lo deja alineado a la izquierda; solo pasa a ir después de la fila de botones en vez de antes. Esto cubre lo que se quería del change de filtros del Histórico, que el usuario decidió fundir aquí.

### D5. La franja ocupa el ancho de la tarjeta

Las `HBox` dentro de una `VBox` crecen a lo ancho por defecto, de modo que la franja llegará de borde a borde de la tarjeta, como en el Editor. No hace falta CSS nuevo. Si al verlo la franja quedara ceñida al contenido, se corrige con `maxWidth="Infinity"` en el FXML antes que con una regla nueva.

### D6. El requisito de tema por defecto se ajusta, no se contradice

Sus escenarios dicen hoy que la tarjeta «que contiene los campos de búsqueda y la fila de botones» es `#F6F6F6`. Tras el cambio la tarjeta sigue siendo `#F6F6F6` y la fila de botones pasa a ir sobre la franja `#E4E7EA` de `action-bar`, igual que ya ocurre en el Editor —cuyo escenario, redactado más tarde, habla solo de la tarjeta—. Se reescriben los dos escenarios de Histórico y Clientes para decir eso, sin tocar los colores.
