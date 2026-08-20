## MODIFIED Requirements

### Requirement: Menú y navegación

La aplicación SHALL tener un menú principal con las opciones Nueva factura, Histórico, Configuración, Copia de seguridad y Salir. Dentro de una factura SHALL existir una barra superior con Guardar, Exportar PDF, Versiones, Crear rectificativa, Nueva factura y Volver. En todas las pantallas salvo el menú principal SHALL existir una barra de navegación superior con iconos que permita acceder a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir. Solo SHALL tenerse una factura abierta a la vez. No SHALL existir la opción "Nueva rectificativa" en el menú principal. Al cerrar la ventana, la aplicación SHALL pedir confirmación antes de salir.

#### Scenario: Crear rectificativa desde la factura
- **WHEN** el usuario pulsa "Crear rectificativa" en la barra de una factura abierta
- **THEN** se crea una rectificativa a partir de esa factura

#### Scenario: Navegar desde la barra de navegación
- **WHEN** el usuario pulsa un icono de la barra de navegación superior en una pantalla distinta del menú principal
- **THEN** la aplicación abre la pantalla correspondiente

#### Scenario: Cerrar la ventana con confirmación
- **WHEN** el usuario cierra la ventana
- **THEN** la aplicación pide confirmación antes de salir

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo (el logo se selecciona desde un archivo y permite ajustar tamaño y posición, y los datos de empresa se guardan siempre aunque la cabecera visible use solo el logo); el pie con texto legal libre configurable por el usuario, sin contenido obligatorio (el texto del Excel existente solo sirve como referencia inicial opcional); el tema de apariencia de la interfaz; los tipos de IVA; las series (crear/configurar, ver y modificar el siguiente número, y configurar la reutilización de números anulados); y las carpetas de PDF (carpeta automática de almacenamiento y última carpeta utilizada). La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, tamaño/posición de ventana, última carpeta de exportación y tema de apariencia.

#### Scenario: Configurar empresa
- **WHEN** el usuario guarda los datos de la empresa en Configuración
- **THEN** esos datos se usan en las nuevas exportaciones a PDF

#### Scenario: Elegir modo de cabecera
- **WHEN** el usuario selecciona un logo y lo ajusta en tamaño y posición
- **THEN** el PDF usa el logo como cabecera y los datos de empresa permanecen guardados

#### Scenario: Pie legal configurable
- **WHEN** el usuario modifica el texto legal del pie
- **THEN** el nuevo texto se repite en las páginas de los PDF generados

#### Scenario: Modificar siguiente número
- **WHEN** el usuario modifica el siguiente número de una serie en Configuración
- **THEN** la próxima factura de esa serie se propone a partir del nuevo valor

#### Scenario: Cambiar el tema de la interfaz
- **WHEN** el usuario selecciona un tema en Configuración y guarda
- **THEN** el tema se aplica de inmediato y queda guardado para las siguientes sesiones

### Requirement: IVA

La aplicación SHALL permitir configurar tipos de IVA: tipos porcentuales e IVA exento. Para IVA exento SHALL poder indicarse un motivo o texto de exención. Cada línea SHALL poder tener un tipo de IVA diferente. Los tipos de IVA SHALL poder crearse, modificarse mientras sea seguro, marcarse como inactivos si ya se han utilizado y SHALL NOT eliminarse físicamente si forman parte del histórico. En la exportación a PDF, el resumen de la factura SHALL desglosar cada tipo de IVA por separado (base y cuota), y en el editor la aplicación SHALL mostrar la base total, el IVA total y el total general como valores separados. Los cálculos SHALL usar BigDecimal; no se permite usar double/float para importes monetarios.

#### Scenario: Líneas con distintos tipos de IVA
- **WHEN** una factura tiene líneas con tipos de IVA diferentes, incluida una exenta
- **THEN** el resumen muestra cada tipo por separado: base 21% e IVA 21%, base 10% e IVA 10%, y base exenta con IVA 0% y su motivo de exención

#### Scenario: Inactivar tipo de IVA usado
- **WHEN** el usuario intenta inactivar un tipo de IVA que ya aparece en facturas del histórico
- **THEN** el tipo pasa a inactivo, no se ofrece para nuevas facturas y el histórico se conserva intacto

## ADDED Requirements

### Requirement: Temas y apariencia

La aplicación SHALL disponer de un sistema de temas de apariencia de la interfaz compuesto por una hoja de estilos base común y temas de colores. La aplicación SHALL ofrecer los temas biblioteca8 (predeterminado), omarchy, esmeralda, terracota, negro-dorado, sakura y neon. El tema activo SHALL aplicarse a cada pantalla al cargarla, SHALL poder cambiarse desde Configuración y SHALL recordarse entre sesiones.

#### Scenario: Cambiar el tema al vuelo
- **WHEN** el usuario selecciona un tema distinto en Configuración
- **THEN** la interfaz cambia de tema de inmediato

#### Scenario: Tema recordado entre sesiones
- **WHEN** el usuario reinicia la aplicación después de guardar un tema en Configuración
- **THEN** el tema guardado se aplica en todas las pantallas

#### Scenario: Tema predeterminado
- **WHEN** el usuario abre la aplicación sin un tema guardado
- **THEN** se aplica el tema biblioteca8

### Requirement: Identidad de empresa en la interfaz

El menú principal SHALL mostrar el nombre, el NIF y el logo de la empresa configurados. El editor de factura SHALL mostrar el logo de la empresa en su cabecera. Los datos mostrados SHALL tomarse de la configuración de empresa.

#### Scenario: Mostrar datos de empresa en el menú principal
- **WHEN** el usuario abre el menú principal con datos de empresa configurados
- **THEN** se muestran el nombre, el NIF y el logo de la empresa

#### Scenario: Mostrar logo en el editor
- **WHEN** el usuario abre una factura con un logo configurado
- **THEN** el logo de la empresa aparece en la cabecera del editor