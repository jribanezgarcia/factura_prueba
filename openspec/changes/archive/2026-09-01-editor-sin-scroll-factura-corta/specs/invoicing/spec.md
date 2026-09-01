## ADDED Requirements

### Requirement: Editor legible sin scroll en facturas cortas

El Editor de facturas SHALL mostrar completa una factura de pocas lineas en el tamano minimo de ventana de 1024x768, sin que el usuario tenga que desplazarse. El resumen de totales SHALL permanecer visible en todo momento, con independencia del numero de lineas. La tabla de lineas SHALL ser el unico elemento que crece con la ventana y SHALL desplazarse internamente cuando las lineas no quepan.

#### Scenario: Factura corta sin scroll
- **WHEN** el usuario abre una factura nueva con la ventana en 1024x768
- **THEN** la cabecera, la tabla de lineas, las observaciones y el resumen de totales son visibles sin desplazarse

#### Scenario: Totales siempre visibles
- **WHEN** una factura tiene mas lineas de las que caben en la tabla
- **THEN** solo se desplaza la tabla, y el desglose de base imponible, IVA, retencion y total sigue visible al pie

#### Scenario: Cabecera repartida en dos bloques
- **WHEN** el usuario abre el Editor
- **THEN** los datos de la factura y los del cliente se muestran en dos bloques contiguos, y los campos de cliente, nombre y direccion disponen del ancho suficiente para su contenido habitual

#### Scenario: La tabla aprovecha el alto disponible
- **WHEN** el usuario amplia o maximiza la ventana del Editor
- **THEN** la tabla de lineas absorbe todo el alto adicional y el resto de zonas conserva su tamano
