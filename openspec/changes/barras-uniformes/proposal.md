## Why

Clientes y el Histórico ya tienen la franja de acciones del Editor, pero al cambiar de pantalla se nota el salto:

- Los botones del Editor van **pegados a la derecha** de la franja, porque a su izquierda están el logo y el título. En Clientes y en el Histórico arrancan por la izquierda, así que los mismos iconos aparecen en un sitio completamente distinto según la pantalla.
- Nada garantiza que la franja siga midiendo lo mismo en las tres: hoy su alto lo marca el contenido de cada pantalla. Medido a 1024×768, hoy coincide —69px de alto, 966 de ancho, arranca en y=103 en las tres—, pero por casualidad, no porque nada lo fije.
- Las tres tarjetas están declaradas distinto: el Editor separa la barra de navegación con 8px y rellena su tarjeta con 12px, mientras Clientes y el Histórico usan 12 y 16. Hoy no produce desplazamiento visible, pero las deja divergiendo en el FXML.

El usuario quiere que al pasar de una pantalla a otra, aunque haya parpadeo, los iconos se queden donde estaban y todo dé sensación de uniformidad. Lo que hoy salta a la vista es la posición de los botones; lo demás es asegurar que siga cuadrando.

## What Changes

- En Clientes y en el Histórico, los botones de acción SHALL ir pegados a la derecha de la franja, como en el Editor.
- La franja de acciones SHALL tener el mismo alto en las tres pantallas, y ese alto SHALL NOT depender de lo que contenga cada una.
- Las tres tarjetas SHALL declarar el mismo espaciado bajo la barra de navegación y el mismo relleno, para que no vuelvan a divergir.
- Los iconos de las tres barras SHALL seguir compartiendo caja y escala, que ya es el caso; se añade la comprobación.
- No cambia ningún botón, ningún icono, ningún texto ni ninguna acción.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía el requisito «Barra de acciones sobre franja propia en Editor, Clientes e Histórico» con la posición de los botones, el alto común y el encaje de la franja en la tarjeta.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml`: un `Region` antes de los botones; espaciado y clase de relleno de la tarjeta igualados al Editor.
- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`: lo mismo.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: alto fijo en `.action-bar`.
- Ningún `.java`, ningún tema, ningún `Editor.fxml`.
