## MODIFIED Requirements

### Requirement: Ventana

La aplicación SHALL abrir su ventana con las siguientes medidas: en la primera ejecución (sin preferencias de ventana guardadas) SHALL medir 1024x768 y SHALL quedar centrada en la pantalla principal; en ejecuciones posteriores SHALL restaurar la posición y, como máximo, el tamaño guardados de la última sesión, sin bajar nunca de 1024x768 en las vistas principales. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo. Ningún tamaño de ventana guardado inferior a 1024x768 SHALL hacer que una vista principal se muestre recortada: la aplicación SHALL corregirlo al entrar en el menú. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico y las filas de alta rápida de IVA y Series en Configuración SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible.

#### Scenario: Primera ejecución abre a 1024x768 centrada
- **WHEN** el usuario inicia la aplicación sin preferencias de ventana guardadas
- **THEN** la ventana mide 1024x768 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la última sesión
- **WHEN** el usuario cierra la aplicación tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana recupera la posición y el tamaño (nunca inferior a 1024x768 en las vistas principales) de la sesión anterior

#### Scenario: Tamaño guardado inferior al mínimo
- **WHEN** la aplicación encuentra un tamaño de ventana guardado inferior a 1024x768
- **THEN** al entrar en el menú la ventana se corrige a 1024x768 y no se muestra recortada

#### Scenario: Mínimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla más pequeña
- **THEN** la ventana no puede bajar de 1024x768

#### Scenario: Filtros del Histórico con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre el Histórico
- **THEN** todos los filtros siguen visibles reorganizados en varias líneas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rápidas de IVA y Series con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abren las pestañas IVA o Series de Configuración
- **THEN** los campos de alta se reorganizan sin cortarse y los botones Nuevo, Guardar e Inactivar/Activar (o Nuevo y Guardar en Series) permanecen visibles y agrupados

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

### Requirement: Tamaños de ventana por vista

La aplicación SHALL garantizar que cada pantalla principal tenga un tamaño mínimo suficiente para que todo su contenido sea legible y usable. El tamaño predefinido y mínimo de todas las pantallas principales SHALL ser 1024×768, salvo la pantalla de selección de empresa (`Arranque`), que SHALL ser de tamaño fijo y no redimensionable, y el diálogo de Generar facturas mensuales, que mantiene 800×600. La ventana principal SHALL tener un tamaño mínimo de 1024×768 y, una vez dentro de la aplicación, al cambiar entre vistas SHALL conservar su tamaño actual (nunca inferior a 1024×768) en lugar de redimensionarse o recentrarse. Ninguna vista principal SHALL fijar un mínimo global de ventana superior que impida a `Arranque` mostrarse a su tamaño fijo; cada vista SHALL controlar su tamaño mediante su propia configuración.

#### Scenario: Arranque fijo
- **WHEN** la aplicación muestra la pantalla de selección de empresa
- **THEN** la ventana tiene un tamaño fijo de 760×520, no es redimensionable y no se puede maximizar

#### Scenario: Editor con tamaño mínimo legible
- **WHEN** el usuario abre el Editor de facturas
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768, no se abre maximizada y todo su contenido (barra de navegación, cabecera, tabla de líneas, botones de acción y resumen de totales) es accesible
- **AND** el contenido completo del Editor tiene scroll general vertical cuando no cabe en el alto de la ventana
- **AND** la tabla de líneas muestra scroll vertical interno cuando hay muchas líneas

#### Scenario: Configuración con tamaño mínimo
- **WHEN** el usuario abre Configuración
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y todos los controles de las pestañas son accesibles

#### Scenario: Histórico con tamaño mínimo
- **WHEN** el usuario abre el Histórico
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y la tabla de facturas se adapta al ancho mostrando scroll horizontal si es necesario

#### Scenario: Clientes con tamaño mínimo
- **WHEN** el usuario abre Clientes
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y la tabla de clientes se adapta al ancho mostrando scroll horizontal si es necesario

#### Scenario: Menú principal con tamaño mínimo
- **WHEN** el usuario abre el Menú principal
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y las tarjetas dejan un margen visible respecto al borde inferior de la ventana

#### Scenario: Versiones con tamaño mínimo
- **WHEN** el usuario abre Versiones
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768 y la tabla de versiones se adapta al ancho

#### Scenario: Backup con tamaño mínimo
- **WHEN** el usuario abre Backup
- **THEN** la ventana tiene un tamaño predefinido y mínimo de 1024×768

#### Scenario: Diálogo de facturación mensual
- **WHEN** se abre el diálogo de Generar facturas mensuales
- **THEN** el diálogo tiene un tamaño predefinido de 800×600 y muestra scroll si el contenido no cabe

#### Scenario: Centrado al cambiar de vista
- **WHEN** el usuario navega entre el Menú principal, el Editor, Configuración, Histórico, Clientes, Versiones o Backup
- **THEN** la ventana principal mantiene su tamaño actual (nunca inferior a 1024×768) y no se produce ningún salto ni recentrado

### Requirement: Menú principal adaptado a 1024×768

La pantalla del Menú principal SHALL tener un tamaño de 1024×768 y SHALL ajustar sus elementos para que la información de la empresa sea visible sin cortarse horizontalmente y para que la lista de opciones deje un margen inferior visible respecto al borde de la ventana. Las tarjetas de la pantalla SHALL mantener el estilo visual del sistema de diseño (fondo de tarjeta, esquinas redondeadas, sombra) y SHALL adaptarse al ancho disponible sin superponerse.

#### Scenario: Información de empresa visible
- **WHEN** el usuario abre el Menú principal
- **THEN** la tarjeta de empresa muestra el nombre, la información adicional y el logo sin cortarse horizontalmente

#### Scenario: Margen inferior en el Menú principal
- **WHEN** el usuario abre el Menú principal
- **THEN** debajo de la última opción del menú queda un espacio visible antes del borde inferior de la ventana

#### Scenario: Opciones del menú dentro de 1024×768
- **WHEN** el usuario abre el Menú principal
- **THEN** todas las opciones del menú y la tarjeta de empresa caben dentro de la ventana sin scroll
