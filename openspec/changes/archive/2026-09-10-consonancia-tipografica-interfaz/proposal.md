## Why

Al poner los botones de solo texto a 13px en negrita, el resto del texto de la interfaz se quedó atrás. Hoy conviven tres pesos sin criterio: los botones de solo texto a 13px en negrita, los botones con icono a 10px sin negrita, y las entradas de la lista de secciones de Configuración a 13px sin negrita.

El resultado es que los elementos que más se usan —la barra de navegación, que está en todas las pantallas, y la barra de acciones del Editor y del Histórico— son los que menos peso visual tienen.

Subir el tamaño de esos botones tiene una consecuencia que hay que resolver en el mismo change: sus etiquetas se envuelven en más líneas. Medidas con la fuente real, `Rectificativa` ocupa 69px a 12px en negrita y `mensuales` 59px, contra un ancho aprovechable de unos 49px en un botón de 64. Sin más cambios, `Rectificativa` se parte en dos líneas y `Generar mensuales` en tres, que es peor de lo que había.

## What Changes

- El texto de los botones con icono SHALL mostrarse a 12px y en negrita, tanto en la barra de navegación como en las barras de acciones del Editor y del Histórico.
- Los botones de la barra de navegación SHALL ensancharse de 90 a 100 para que las etiquetas largas sigan cabiendo en una línea.
- Los botones de las barras de acciones SHALL ensancharse de 64 a 86, de modo que una etiqueta de una sola palabra quepa siempre en una línea y una de dos palabras se envuelva por su espacio, sin partir palabras.
- El espacio necesario para ensanchar SHALL recuperarse de la propia barra del Editor, reduciendo su separación entre elementos de 8 a 4 y el ancho del logo de empresa de 110 a 80, de modo que el título de la factura conserve prácticamente el espacio que tiene hoy.
- Los botones de las barras de acciones SHALL aumentar su alto mínimo de 68 a 78 para absorber la envoltura sin ensancharse más.
- Las entradas de la lista de secciones de Configuración SHALL mostrarse en negrita.
- No cambia ninguna etiqueta, ninguna acción ni ningún comportamiento.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se corrige el requisito de la barra de acciones del editor, que hoy fija en dos el número de líneas en las que puede envolverse una etiqueta. Ese número describía lo que ocurría con la letra a 10px, no una intención de diseño; lo que el requisito protege es que el botón no se ensanche y que todos midan lo mismo.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: `.nav-button`, `.btn-ribbon`, `.action-bar` y `.lista-secciones .list-cell`.
- `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`: el ancho del contenedor del logo.
- Ningún `.java` y ningún archivo de tema.
