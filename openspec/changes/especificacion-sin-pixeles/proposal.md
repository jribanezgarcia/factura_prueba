## Why

La especificación de `invoicing` tiene 54 requisitos y 1.672 líneas, y **19 de ellos solo describen el aspecto de la interfaz**: el sombreado de los botones, el peso de la tipografía, el tamaño de la caja de los iconos, la franja de fondo de las barras de acciones, la composición centrada del menú… Unas 700 líneas.

Son el poso de unos 58 changes de retoque visual. Cada uno dejó su requisito, y hoy:

- la especificación describe **píxeles en lugar de comportamiento**, que es para lo que sirve;
- varios requisitos se solapan («Tamaños de ventana por vista» con «Ventana», «Estilo de zona de acciones» con «Botones de acción con icono identificativo»);
- describen **cómo está hecho hoy el CSS**, así que cualquier retoque de apariencia obliga a cambiar la especificación;
- leerla entera cuesta ~29.000 tokens, y casi la mitad no habla de lo que hace la aplicación.

## What Changes

- Se **retiran 20 requisitos**: 19 que solo describen aspecto visual y «Temas y apariencia», cuyo contenido se conserva entero en el requisito nuevo.
- Se **añade un requisito, «Apariencia de la interfaz»**, que recoge lo que sí es una regla: los temas, la legibilidad con cualquiera de ellos, que ninguna pantalla recorte contenido en el tamaño mínimo, que una misma acción se llame siempre igual, los iconos de los diálogos y la negrita reservada a los importes.
- **El aspecto pasa a vivir en las hojas de estilo** (`base.css` y un fichero por tema), no en la especificación.
- **No cambia nada de lo que ve el usuario ni una línea de código**: es solo la especificación.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: 20 requisitos retirados y uno nuevo, «Apariencia de la interfaz», que los resume.

## Impact

- `openspec/specs/invoicing/spec.md`: de 54 requisitos a 35, y de ~1.670 a unas 1.000 líneas.
- Sin cambios en `src/`, ni en los tests, ni en los CSS.
- Fuera: `pdf-rendering` (describe el documento impreso, que sí es comportamiento) y los requisitos «Ventana», «Menú y navegación», «Identidad de empresa en la interfaz» e «Identidad de la aplicación en la interfaz», que dicen qué hay en cada pantalla, no cómo se pinta.
