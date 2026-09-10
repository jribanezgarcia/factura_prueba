## Why

La interfaz usa la negrita como recurso de jerarquía en dieciséis sitios distintos, y el resultado es que casi todo pesa. Al comparar la aplicación con MsConta de Cegid, que es el programa con el que el usuario trabaja a diario, la diferencia salta a la vista: allí no hay una sola negrita en toda la interfaz, y aun así se lee mejor. La jerarquía la dan el tamaño, el color y el orden.

Hay además una incoherencia que este change resuelve de paso. Los botones con icono llevan hoy el icono en el color del tema y la etiqueta en gris, de modo que el color se corta a mitad del botón.

Y la acción principal se distingue con dos señales —negrita más color, o negrita más un sombreado más intenso— que solo tienen sentido si el resto de la barra es más apagado. Cuando todo baja de peso, esas señales dejan de ser jerarquía y pasan a ser ruido.

## What Changes

- Ningún texto de la interfaz SHALL mostrarse en negrita, salvo los importes: el valor de las filas de totales y el total destacado de cada tema.
- La jerarquía entre títulos, rótulos y texto normal SHALL darse por tamaño y color, no por peso.
- En los botones que muestran icono y etiqueta, **ambos** SHALL ir en el color de acento del tema; en los destructivos, ambos en el color de peligro.
- Ningún botón SHALL destacarse como acción principal: dentro de una barra o de una pantalla todos SHALL presentarse igual, y el orden SHALL ser la única jerarquía.
- El sombreado suave de los botones de solo texto SHALL conservarse; solo desaparece el sombreado intenso de la acción principal, que queda sin uso.
- No cambia ninguna etiqueta, ningún icono, ninguna medida ni ningún flujo.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: seis requisitos apoyan la jerarquía en la negrita o en la distinción de la acción principal. Se corrigen todos y se añade la regla que reserva la negrita a los importes.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: quince declaraciones de negrita y las reglas de la acción principal.
- `src/main/resources/com/alcazaba/facturacion/themes/tema-*.css`: los siete, para retirar la variable de sombreado intenso que queda sin uso.
- Ningún FXML y ningún `.java`.
