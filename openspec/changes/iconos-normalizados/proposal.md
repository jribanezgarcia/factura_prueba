## Why

En Clientes cada icono ya va dentro de una caja fija y las cuatro etiquetas arrancan a la misma altura. En el Editor, el Histórico y el menú principal los iconos siguen sueltos: cada dibujo ocupa lo que mide, y como no todos miden igual, las etiquetas de una misma barra quedan a alturas distintas y los textos del menú arrancan en posiciones distintas según la fila.

La norma ya existe en el spec, pero su alcance se quedó en Clientes.

## What Changes

- En la barra de acciones del Editor y en la del Histórico, cada icono SHALL ir dentro de la misma caja fija que ya usa Clientes, de modo que todas las etiquetas de cada barra arranquen a la misma altura.
- En el menú principal, cada icono SHALL ir dentro de una caja fija propia, más grande, de modo que el nombre y la descripción de todas las opciones arranquen en la misma posición horizontal.
- No cambia ningún trazado, ningún texto, ninguna acción ni ninguna escala de icono.
- La barra de navegación queda fuera: tiene su propia caja y sus propios ajustes, que se tratan en un change aparte.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía el alcance del requisito «Iconos de tamaño uniforme dentro de una barra» de Clientes al Editor, al Histórico y al menú principal.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`: los ocho `SVGPath` de la barra, envueltos en `StackPane` con `caja-icono`.
- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`: los seis `SVGPath` de la barra, igual.
- `src/main/resources/com/alcazaba/facturacion/ui/MenuPrincipal.fxml`: los siete `SVGPath`, envueltos en `StackPane` con `caja-icono-menu`.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: una style-class nueva, `caja-icono-menu`.
- Ningún `.java`.
