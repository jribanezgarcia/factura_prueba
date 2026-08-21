## Why

En el menú principal, la caja del logo (260×200) y la lista de botones se centran por separado; como el logo es apaisado (1847×851), queda hueco arriba/abajo y el bloque de empresa no "casa" con los botones. Además, los iconos de "Histórico" (una lupa de búsqueda) y "Copia de seguridad" (una caja genérica) no transmiten su significado, y se han propuesto y elegido sustitutos más claros con prototipos en `prototipos/ajustes-menu-iconos.html`.

## What Changes

- Alineación del menú principal: las dos columnas (bloque de empresa/logo y lista de botones) pasan a estar alineadas por su borde superior en lugar de centrarse cada una por separado.
- Icono de Histórico: se sustituye la lupa por un icono de lista/expediente de documentos, en el menú principal y en la barra de navegación.
- Icono de Copia de seguridad: se sustituye la caja genérica por un icono de disquete (guardar), en el menú principal y en la barra de navegación.
- Cambio puramente visual: no se alteran acciones, navegación, lógica de negocio ni el modelo de datos.

## Capabilities

### New Capabilities

No se introducen capacidades nuevas.

### Modified Capabilities

No se modifican requisitos de la spec: `skip_specs: true` (cambio puramente visual, sin cambios de comportamiento especificados). La spec "Menú y navegación" describe la barra de navegación "con iconos" pero no fija los glifos concretos, y la alineación del menú principal no está especificada.

## Impact

- Modificados: `src/main/resources/com/alcazaba/facturacion/ui/MenuPrincipal.fxml` (alineación del menú e iconos), `src/main/java/com/alcazaba/facturacion/ui/BarraNavegacion.java` (glifos de Histórico y Copia de seguridad).
- CSS: posible ajuste menor en `src/main/resources/com/alcazaba/facturacion/themes/base.css` (caja del logo para la alineación arriba) si se requiere.
- Sin cambios de APIs, dependencias ni modelo de datos.