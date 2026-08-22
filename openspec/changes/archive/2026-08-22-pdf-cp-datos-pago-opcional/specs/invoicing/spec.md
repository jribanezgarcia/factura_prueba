## MODIFIED Requirements

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical con el diseño aprobado inspirado en el documento Excel de la empresa:

- Cabecera en todas las páginas: logo al doble del tamaño configurado (modo logo) o datos de empresa (modo texto), con el NIF de la empresa destacado en línea propia con esquinas redondeadas; a la derecha la palabra FACTURA y debajo, como pares rótulo→valor, `SERIE / Nº` sobre el número completo y `FECHA` sobre la fecha. Los datos de empresa SHALL ocupar una columna propia que SHALL NOT solaparse nunca con el bloque FACTURA: si el nombre o alguna línea excede el ancho disponible, se reduce su tamaño hasta caber. El número completo y la fecha SHALL ser siempre legibles.
- Dos tarjetas bajo la cabecera con esquinas redondeadas: «FACTURAR A» con los datos del cliente presentados como pares etiqueta→valor, cada dato en su propia fila — Nombre (destacado en negrita), NIF, Dirección, Código postal, Población (la localidad), Provincia y Email; las filas con campo vacío no aparecen. «DATOS DE PAGO» con forma de pago, vencimiento y realizada por (solo las filas rellenas); cuando los tres campos estén vacíos, la tarjeta «Datos de pago» SHALL NOT aparecer y «Facturar A» SHALL conservar su anchura con el espacio restante en blanco. La cabecera de «FACTURAR A» SHALL ir con fondo del color de acento y texto blanco; la cabecera de «DATOS DE PAGO» SHALL ir en blanco con un borde fino inferior del color de acento y texto marrón oscuro derivado del acento. Ambos cuerpos SHALL ir en blanco.
- Tabla de líneas con celdas bordeadas estilo hoja de cálculo: Cant / Descripción / Precio / IVA % / Total. El Total por línea SHALL incluir el IVA (base × (1 + IVA%)); las líneas exentas SHALL mostrar su importe sin IVA. La descripción SHALL mostrarse siempre en un único estilo, aunque ocupe varias líneas.
- Resumen de totales alineado a la derecha y compacto, sin filas de totales globales repetidas («Base total»/«IVA total» SHALL NOT aparecer): por cada tipo de IVA una fila `Base` (importes antes del descuento global) seguida de su fila `IVA n%` con la cuota calculada sobre la base ya descontada; si el descuento global es mayor que cero, una única fila `Descuento n%` con el importe restando y en rojo suave; después la fila TOTAL destacada con fondo del color de acento, separada de las filas anteriores por un espacio visible. Las cifras mostradas SHALL cuadrar: Base − Descuento + IVA = TOTAL.
- Observaciones en caja clara con esquinas redondeadas; pie legal configurable dentro de un recuadro con borde de color, repetido en todas las páginas; `Página X de Y` en cada página reflejando el número real de páginas, con el dígito total dibujado sin solapar la palabra «de». El cierre del documento SHALL mantenerse compacto (totales estrechos y tablas capaces de repartir sus filas entre páginas) para evitar una página que contenga únicamente el bloque de totales cuando el contenido cabe repartiéndose.
- Tipografía Calibri embebida en el documento cuando esté disponible en el sistema; en caso contrario Helvetica. Los tonos SHALL derivarse del color de acento configurado siguiendo el prototipo.

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

#### Scenario: Rótulos Serie/Nº y Fecha bajo FACTURA
- **WHEN** el usuario exporta cualquier factura
- **THEN** bajo la palabra FACTURA aparece el rótulo `SERIE / Nº` encima del número completo y el rótulo `FECHA` encima de la fecha

#### Scenario: Cabecera sin solapes con nombre largo
- **WHEN** el usuario exporta una factura de una empresa cuyo nombre o líneas son más anchos que la columna disponible
- **THEN** los datos de empresa no invaden el bloque FACTURA
- **AND** el número completo (Serie/Nº) y la fecha son legibles en su posición

#### Scenario: Tarjeta Facturar a con campos etiquetados
- **WHEN** el usuario exporta una factura cuyo cliente tiene nombre, NIF, dirección, código postal, localidad, provincia y email
- **THEN** la tarjeta «Facturar a» muestra cada dato precedido de su etiqueta: Nombre, NIF, Dirección, Código postal, Población, Provincia y Email
- **AND** el código postal aparece como fila propia, separada de Población

#### Scenario: Campos vacíos sin fila
- **WHEN** el cliente carece de alguno de esos campos
- **THEN** la fila correspondiente no aparece en la tarjeta

#### Scenario: Tarjeta Facturar a bicolor y tarjeta Datos de pago clara
- **WHEN** el usuario exporta una factura con datos de pago rellenados
- **THEN** la cabecera de «Facturar a» lleva fondo del color de acento con texto blanco
- **AND** la cabecera de «Datos de pago» va en blanco con borde fino inferior y texto marrón oscuro

#### Scenario: Datos de pago vacíos ocultan la tarjeta
- **WHEN** el usuario exporta una factura sin forma de pago, sin vencimiento y sin realizada por
- **THEN** la tarjeta «Datos de pago» no aparece en el PDF
- **AND** «Facturar A» conserva su anchura con el espacio restante en blanco

#### Scenario: Tarjetas bicolor con email y datos de pago opcionales

- **WHEN** el usuario exporta una factura cuyo cliente tiene email y con forma de pago, vencimiento y realizada por rellenados
- **THEN** la tarjeta «Facturar a» muestra el email del cliente y la tarjeta «Datos de pago» muestra los tres valores
- **AND** si el cliente no tiene email esa fila no aparece en el PDF

#### Scenario: Total por línea con IVA incluido

- **WHEN** el PDF contiene una línea con base 100,00 € e IVA 21 %
- **THEN** la columna Total de esa línea muestra 121,00 €
- **AND** las líneas exentas muestran su importe sin IVA añadido

#### Scenario: Totales sin filas duplicadas
- **WHEN** el usuario exporta una factura sin descuento con base 100,00 € al 21 %
- **THEN** el resumen muestra exactamente `Base 100,00 €`, `IVA 21 % 21,00 €` y `TOTAL 121,00 €`
- **AND** no aparecen las filas `Base total` ni `IVA total`

#### Scenario: Totales con descuento cuadrando
- **WHEN** el usuario exporta una factura con descuento global del 10 % sobre base 1.000,00 € al 21 %
- **THEN** el resumen muestra `Base 1.000,00 €`, `IVA 21 % 189,00 €` (cuota sobre la base descontada), `Descuento 10 % −100,00 €` restando y `TOTAL 1.089,00 €`
- **AND** se cumple Base − Descuento + IVA = TOTAL

#### Scenario: Varios tipos de IVA con un solo descuento
- **WHEN** el usuario exporta una factura con líneas al 21 % y al 10 % y descuento global mayor que cero
- **THEN** el resumen muestra cada par Base/IVA por separado y una única fila `Descuento n%`
- **AND** no aparecen filas de totales globales repetidas

#### Scenario: Los totales no quedan solos en una página
- **WHEN** el usuario exporta una factura larga cuyo bloque de totales cabe en el espacio restante de la última página una vez compactado
- **THEN** no se genera una página adicional que contenga únicamente los totales
- **AND** si el bloque realmente no cabe, pasa íntegro a la página siguiente y `Página X de Y` la cuenta como página real

#### Scenario: Tipografía y pie sin solape
- **WHEN** el usuario exporta una factura con Calibri disponible en el sistema
- **THEN** el documento embebe la fuente Calibri
- **AND** el pie muestra `Página X de Y` con el número total separado correctamente de la palabra «de»