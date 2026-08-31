## Why

La interfaz actual de la aplicación funciona pero carece de coherencia visual y de la sensación de pulido esperada en una aplicación de escritorio moderna. Aplicar una capa de diseño inspirada en la estética de Ajustes de Apple mejorará la jerarquía visual, la legibilidad y la percepción de calidad, reduciendo la carga cognitiva al crear facturas.

## What Changes

- Rediseño visual global de la aplicación basado en los principios de Apple Design: jerarquía clara, espaciado generoso, tarjetas de sección, tipografía cuidada y estados de control con feedback inmediato.
- Ajustes de márgenes, alineaciones, tarjetas de sección, tipografía y clases CSS en los FXML principales: `MenuPrincipal`, `Editor`, `Historico`, `Configuracion`, `Clientes`, `GenerarFacturasMensuales`, `Versiones`, `Backup` y `Arranque`.
- Refactorizar `src/main/resources/com/alcazaba/facturacion/themes/base.css` para definir la estructura visual común (espaciado, radios, sombras, estados de foco) sin cambiar las paletas de color de los temas existentes.
- Introducir microinteracciones suaves (hover, foco, transiciones cortas) mediante pseudo-clases CSS y pequeñas animaciones JavaFX.
- Mantener los 7 temas de color actuales (`biblioteca8`, `omarchy`, `esmeralda`, `terracota`, `negro-dorado`, `sakura`, `neon`) intactos y adaptar el nuevo diseño a cada uno de ellos.
- Actualizar los tests de UI si dependen de clases CSS modificadas o de la estructura de nodos.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: se añaden requisitos de diseño de interfaz, modo claro/oscuro y microinteracciones visuales.

## Impact

- FXML y CSS en `src/main/resources/com/alcazaba/facturacion/ui` y `.../themes`.
- `ThemeService` y `ConfiguracionController` para gestionar y persistir el modo claro/oscuro.
- Tests de UI que verifiquen layout o clases CSS.
- No cambia la lógica de negocio, cálculos ni modelos de datos.
