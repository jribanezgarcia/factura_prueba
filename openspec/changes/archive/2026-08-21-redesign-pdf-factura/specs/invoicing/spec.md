## DELTA Spec: invoicing

### MODIFIED Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical con el diseño aprobado inspirado en el documento Excel de la empresa:

- Cabecera en todas las páginas: logo al doble del tamaño configurado (modo logo) o datos de empresa (modo texto), con el NIF de la empresa destacado en línea propia; a la derecha la palabra FACTURA, el número completo (Serie/Nº) y la fecha.
- Dos tarjetas bicolor bajo la cabecera: «FACTURAR A» con nombre, NIF, dirección, población y email del cliente (esta última fila solo si existe email); «DATOS DE PAGO» con forma de pago, vencimiento y realizada por (solo las filas rellenas). La cabecera de cada tarjeta SHALL ir con fondo del color de acento y texto blanco, y el cuerpo SHALL ir en blanco.
- Tabla de líneas con celdas bordeadas estilo hoja de cálculo: Cant / Descripción / Precio / IVA % / Total. El Total por línea SHALL incluir el IVA (base × (1 + IVA%)); las líneas exentas SHALL mostrar su importe sin IVA. La descripción SHALL mostrarse siempre en un único estilo, aunque ocupe varias líneas.
- Resumen de totales alineado a la derecha con desglose por tipo de IVA (base y cuota), descuento global si es mayor que cero y fila TOTAL destacada con fondo del color de acento.
- Observaciones en caja clara; pie legal configurable dentro de un recuadro con borde de color, repetido en todas las páginas; `Página X de Y` en cada página.

El resto se mantiene como estaba: descripciones largas ajustadas automáticamente, importes formato español (`1.250,50 €`), fechas formato español (`11/08/2026`), marca `ANULADA` destacada en facturas anuladas, correspondencia exacta con la versión exportada, uso de la configuración actual de empresa/logo/cabecera/pie legal, documentos independientes, estructura `Facturas/AAAA/SERIE/` y nombre `CODIGO-CORRELATIVO-MES.pdf` sin indicar versión. El color de acento SHALL tomarse de la preferencia `color_pdf`, con valor por defecto arena Alcazaba (`#B08D57`) si no está configurada.

#### Scenario: Exportar factura de varias páginas

- **WHEN** el usuario exporta una factura con descripciones largas que ocupa varias páginas
- **THEN** el PDF repite cabecera y pie en cada página e indica `Página X de Y`

#### Scenario: Exportar factura anulada

- **WHEN** el usuario exporta una factura anulada
- **THEN** el PDF muestra la marca `ANULADA` de forma destacada

#### Scenario: Exportar versión concreta

- **WHEN** el usuario exporta una versión concreta del histórico
- **THEN** el PDF refleja exactamente los datos de esa versión

#### Scenario: Nombres de archivo

- **WHEN** el usuario exporta la factura C-59/8
- **THEN** se genera el archivo `Facturas/2026/C/C-59-8.pdf`

#### Scenario: Tarjetas bicolor con email y datos de pago opcionales

- **WHEN** el usuario exporta una factura cuyo cliente tiene email y con forma de pago, vencimiento y realizada por rellenados
- **THEN** la tarjeta «Facturar a» muestra el email del cliente y la tarjeta «Datos de pago» muestra los tres valores
- **AND** si el cliente no tiene email o los datos de pago están vacíos, esas filas no aparecen en el PDF

#### Scenario: Total por línea con IVA incluido

- **WHEN** el PDF contiene una línea con base 100,00 € e IVA 21 %
- **THEN** la columna Total de esa línea muestra 121,00 €
- **AND** las líneas exentas muestran su importe sin IVA añadido

### MODIFIED Requirement: Configuración

La pantalla de Configuración SHALL permitir además elegir el color de acento usado en los PDF exportados mediante un selector de color. El valor SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`). Del color elegido SHALL derivarse el resto de tonos del documento (cabeceras de tarjetas y tabla, fila TOTAL, recuadro del pie legal). El resto de capacidades se mantienen: datos de empresa, cabecera texto/logo, pie legal libre, tema, tipos de IVA, series y carpetas de PDF, y las preferencias recordadas.

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

#### Scenario: Cambiar el color del PDF

- **WHEN** el usuario elige un color en Configuración y guarda
- **THEN** los nuevos PDF usan ese color de acento y sus tonos derivados
- **AND** si se restablece el valor por defecto o la preferencia no existe, se usa `#B08D57`

### MODIFIED Requirement: Búsqueda de clientes al crear factura

Al crear o editar una factura, el usuario SHALL poder buscar un cliente por nombre/razón social o NIF con búsqueda incremental mientras escribe. Al seleccionar un cliente, sus datos —incluido el email— SHALL cargarse en la factura. Los datos del cliente (email incluido) SHALL poder modificarse desde la factura, y esos cambios SHALL actualizar también la ficha general del cliente. La ficha general de clientes SHALL incluir el campo email.

#### Scenario: Búsqueda incremental

- **WHEN** el usuario escribe caracteres en el campo de búsqueda de cliente
- **THEN** la lista de clientes coincidentes por nombre o NIF se actualiza con cada carácter

#### Scenario: Desplegable de clientes

- **WHEN** el usuario pulsa la flecha del selector de cliente sin texto de búsqueda
- **THEN** la aplicación muestra los clientes activos disponibles

#### Scenario: Selección de cliente

- **WHEN** el usuario selecciona un cliente de la lista
- **THEN** nombre/razón social, NIF, dirección, código postal, localidad, provincia y email se cargan en la factura

#### Scenario: Modificación del cliente desde la factura

- **WHEN** el usuario modifica un dato de cliente (email incluido) dentro de la factura y guarda
- **THEN** la ficha general del cliente queda actualizada con ese dato

### MODIFIED Requirement: Facturas normales

La aplicación SHALL permitir crear y editar facturas normales con número, fecha, cliente, líneas, descuento general, IVA, observaciones, totales y tres datos de pago opcionales: forma de pago, fecha de vencimiento y realizada por. Estos datos de pago SHALL quedar guardados en la versión de la factura y SHALL aparecer en el PDF solo cuando estén rellenos. La fecha de la factura SHALL ser editable mediante un selector/calendario. La introducción de líneas SHALL ser similar a trabajar con una hoja de cálculo.

#### Scenario: Crear factura con datos completos

- **WHEN** el usuario crea una factura con cliente, líneas, descuento, IVA y observaciones y la guarda
- **THEN** la factura se almacena con su número definitivo y aparece en el histórico

#### Scenario: Editar factura emitida

- **WHEN** el usuario modifica la versión actual de una factura en estado Emitida y guarda
- **THEN** tras la confirmación, la versión actual se sobrescribe con los cambios

#### Scenario: Datos de pago opcionales

- **WHEN** el usuario guarda una factura dejando vacíos forma de pago, vencimiento y realizada por
- **THEN** la factura se guarda igualmente y el PDF no incluye esas filas
