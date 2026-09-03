## Why

En el tema por defecto (Biblioteca8), algunos botones de la barra de acciones del Editor han pasado a un gris con texto casi blanco que dificulta su lectura, y las tarjetas superiores de Histórico y Clientes (donde están los campos de búsqueda y los botones) son de un blanco plano que no diferencia esa zona de trabajo del resto. Se quiere recuperar la legibilidad de esos botones y dar un fondo gris claro sutil a esas tarjetas para distinguir la zona de acciones sin que resalte.

## What Changes

- En el **Editor**, los **botones de acción** (Exportar PDF, Versiones, Crear rectificativa, Restaurar, Nueva factura, Volver, Añadir línea y Eliminar línea) pasan a fondo **blanco** con **texto negro** (quedan Guardar, estilo primario, y Anular, estilo de peligro, como están).
- En el **Histórico**, la tarjeta que contiene los campos de búsqueda y la fila de botones pasa a un fondo **gris claro `#F0F0F0`**.
- En **Clientes**, la tarjeta que contiene el campo de búsqueda y la fila de botones pasa al **mismo gris claro `#F0F0F0`**.
- Todo se aplica únicamente en el tema **Biblioteca8**; el resto de temas no se tocan.

## Capabilities

### New Capabilities
- _(ninguna)_

### Modified Capabilities
- `invoicing`: Se añade un requisito nuevo de identidad visual de la pantalla (estilo de los botones del Editor y fondo de las tarjetas de Histórico y Clientes para el tema Biblioteca8).

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/tema-biblioteca8.css` — `.action-button`/`:hover` pasan a blanco/negro; nueva clase `.panel-busqueda` con fondo `#F0F0F0`.
- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml` y `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml` — añadir la clase `panel-busqueda` a la tarjeta superior de acciones.
- Ningún cambio de lógica, repositorio, controladores, ni del resto de temas.
