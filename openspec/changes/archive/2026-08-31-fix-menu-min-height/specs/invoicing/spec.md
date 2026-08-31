## MODIFIED Requirements

### Requirement: Tamaños de ventana por vista

La aplicación SHALL garantizar que cada pantalla principal tenga un tamaño mínimo suficiente para que todo su contenido sea legible y usable. El tamaño mínimo y el tamaño predefinido de cada vista pueden ser iguales o diferentes, pero en ningún caso el usuario podrá reducir la ventana por debajo del mínimo definido para la vista activa. La pantalla de selección de empresa (`Arranque`) SHALL ser de tamaño fijo y no redimensionable. Al cambiar entre vistas, la ventana principal SHALL ajustarse automáticamente al tamaño predefinido de la nueva vista y SHALL centrarse en la pantalla. Los diálogos modales SHALL respetar su propia configuración de tamaño mínimo y predefinido.

#### Scenario: Arranque fijo
- **WHEN** la aplicación muestra la pantalla de selección de empresa
- **THEN** la ventana tiene un tamaño fijo de 760×520, no es redimensionable y no se puede maximizar

#### Scenario: Editor con tamaño mínimo legible
- **WHEN** el usuario abre el Editor de facturas
- **THEN** la ventana no puede ser menor de 1000×760 y todos los controles principales (barra de navegación, cabecera, tabla de líneas, botones de acción y resumen de totales) son visibles sin cortarse

#### Scenario: Configuración con tamaño mínimo
- **WHEN** el usuario abre Configuración
- **THEN** la ventana no puede ser menor de 1000×620

#### Scenario: Histórico con tamaño mínimo
- **WHEN** el usuario abre el Histórico
- **THEN** la ventana no puede ser menor de 1000×600

#### Scenario: Clientes con tamaño mínimo
- **WHEN** el usuario abre Clientes
- **THEN** la ventana no puede ser menor de 1000×600

#### Scenario: Menú principal con tamaño mínimo
- **WHEN** el usuario abre el Menú principal
- **THEN** la ventana no puede ser menor de 760×600

#### Scenario: Versiones con tamaño mínimo
- **WHEN** el usuario abre Versiones
- **THEN** la ventana no puede ser menor de 900×500

#### Scenario: Backup con tamaño mínimo
- **WHEN** el usuario abre Backup
- **THEN** la ventana no puede ser menor de 720×450

#### Scenario: Diálogo de facturación mensual
- **WHEN** se abre el diálogo de Generar facturas mensuales
- **THEN** el diálogo no puede ser menor de 920×680

#### Scenario: Centrado al cambiar de vista
- **WHEN** el usuario navega de una pantalla a otra
- **THEN** la ventana se redimensiona al tamaño predefinido de la nueva vista y se centra en la pantalla
