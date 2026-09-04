## MODIFIED Requirements

### Requirement: Menú y navegación

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Histórico, Configuración, Copia de seguridad y Salir. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar PDF, Versiones, Rectificativa, Anular o Restaurar según el estado, Nueva y Volver. En todas las pantallas salvo el menú principal SHALL existir una barra de navegación superior con iconos que permita acceder a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Solo SHALL tenerse una factura abierta a la vez. No SHALL existir la opción "Nueva rectificativa" en el menú principal. Al cerrar la ventana o al pulsar Salir en la barra de navegación, la aplicación SHALL pedir confirmación antes de salir y SHALL seguir el mismo proceso de cierre (comprobación de cambios sin guardar, preferencias de ventana y lock).

#### Scenario: Crear rectificativa desde la factura
- **WHEN** el usuario pulsa "Rectificativa" en la barra de una factura abierta
- **THEN** se crea una rectificativa a partir de esa factura

#### Scenario: Navegar desde la barra de navegación
- **WHEN** el usuario pulsa un icono de la barra de navegación superior en una pantalla distinta del menú principal
- **THEN** la aplicación abre la pantalla correspondiente

#### Scenario: Cerrar la ventana con confirmación
- **WHEN** el usuario cierra la ventana
- **THEN** la aplicación pide confirmación antes de salir

#### Scenario: Salir desde la barra de navegación con confirmación
- **WHEN** el usuario pulsa Salir en la barra de navegación
- **THEN** la aplicación pide confirmación «¿Seguro que deseas salir de la aplicación?» y solo cierra si se acepta, siguiendo el mismo proceso de cierre que al cerrar la ventana

## ADDED Requirements

### Requirement: Barra de acciones del editor sin desbordamiento

La barra de acciones del editor de facturas SHALL mostrar todos sus botones visibles a la vez en el tamaño mínimo de ventana (1024×768), sin recurrir a un menú de desbordamiento. En particular, la aparición del botón de anular al guardar una factura SHALL NOT ocultar ningún otro botón.

El título de la factura SHALL tener una anchura máxima y recortarse con elipsis, de modo que un número de factura largo no reduzca el espacio disponible para los botones.

Los botones que requieren una factura ya guardada SHALL mostrarse deshabilitados mientras no la haya, en lugar de responder con un aviso al pulsarlos.

#### Scenario: Guardar una factura no esconde botones
- **WHEN** el usuario guarda una factura nueva y aparece el botón de anular
- **THEN** todos los botones de la barra siguen visibles y no aparece ningún menú de desbordamiento

#### Scenario: Número de factura largo
- **WHEN** se abre una factura cuyo número hace el título especialmente largo
- **THEN** el título se recorta con elipsis y los botones de la barra conservan su posición y visibilidad

#### Scenario: Botones que necesitan una factura guardada
- **WHEN** el usuario está en una factura nueva todavía sin guardar
- **THEN** los botones de Versiones y Rectificativa se muestran deshabilitados
