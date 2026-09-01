## MODIFIED Requirements

### Requirement: Tamaños de ventana por vista

La aplicación SHALL garantizar que cada pantalla principal tenga un tamaño mínimo suficiente para que todo su contenido sea legible y usable. El tamaño predefinido de todas las pantallas principales SHALL ser 800×600, salvo la pantalla de selección de empresa (`Arranque`), que SHALL ser de tamaño fijo y no redimensionable. La ventana principal SHALL tener un tamaño mínimo de 800×600 y, una vez dentro de la aplicación, al cambiar entre vistas SHALL conservar su tamaño actual en lugar de redimensionarse o recentrarse. Los diálogos modales SHALL respetar su propia configuración de tamaño; en este change también se ajustan a 800×600.

#### Scenario: Arranque fijo
- **WHEN** la aplicación muestra la pantalla de selección de empresa
- **THEN** la ventana tiene un tamaño fijo de 760×520, no es redimensionable y no se puede maximizar

#### Scenario: Editor con tamaño mínimo legible
- **WHEN** el usuario abre el Editor de facturas
- **THEN** la ventana tiene un tamaño predefinido de 800×600, no se abre maximizada y todos los controles principales (barra de navegación, cabecera, tabla de líneas, botones de acción y resumen de totales) son visibles sin cortarse
- **AND** la tabla de líneas reduce su altura en 800×600 y muestra scroll vertical cuando hay muchas líneas
- **AND** al maximizar la ventana la tabla de líneas crece para aprovechar el espacio disponible

#### Scenario: Configuración con tamaño mínimo
- **WHEN** el usuario abre Configuración
- **THEN** la ventana tiene un tamaño predefinido de 800×600 y todos los controles de las pestañas son accesibles

#### Scenario: Histórico con tamaño mínimo
- **WHEN** el usuario abre el Histórico
- **THEN** la ventana tiene un tamaño predefinido de 800×600 y la tabla de facturas se adapta al ancho mostrando scroll horizontal si es necesario

#### Scenario: Clientes con tamaño mínimo
- **WHEN** el usuario abre Clientes
- **THEN** la ventana tiene un tamaño predefinido de 800×600 y la tabla de clientes se adapta al ancho mostrando scroll horizontal si es necesario

#### Scenario: Menú principal con tamaño mínimo
- **WHEN** el usuario abre el Menú principal
- **THEN** la ventana tiene un tamaño predefinido de 800×600 y las tarjetas dejan un margen visible respecto al borde inferior de la ventana

#### Scenario: Versiones con tamaño mínimo
- **WHEN** el usuario abre Versiones
- **THEN** la ventana tiene un tamaño predefinido de 800×600 y la tabla de versiones se adapta al ancho

#### Scenario: Backup con tamaño mínimo
- **WHEN** el usuario abre Backup
- **THEN** la ventana tiene un tamaño predefinido de 800×600

#### Scenario: Diálogo de facturación mensual
- **WHEN** se abre el diálogo de Generar facturas mensuales
- **THEN** el diálogo tiene un tamaño predefinido de 800×600 y muestra scroll si el contenido no cabe

#### Scenario: Centrado al cambiar de vista
- **WHEN** el usuario navega entre el Menú principal, el Editor, Configuración, Histórico, Clientes, Versiones o Backup
- **THEN** la ventana principal mantiene su tamaño actual de 800×600 y no se produce ningún salto ni recentrado
