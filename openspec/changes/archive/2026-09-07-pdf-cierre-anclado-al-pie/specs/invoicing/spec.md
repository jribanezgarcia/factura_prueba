## MODIFIED Requirements

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical con el diseño aprobado inspirado en el documento Excel de la empresa:

- Cabecera en todas las páginas: logo a un tamaño fijo que la aplicación no permite alterar (modo logo) o datos de empresa (modo texto), con el NIF de la empresa destacado en línea propia con esquinas redondeadas; a la derecha la palabra FACTURA y debajo, como pares rótulo→valor, `SERIE / Nº` sobre el número completo y `FECHA` sobre la fecha. Los datos de empresa SHALL ocupar una columna propia que SHALL NOT solaparse nunca con el bloque FACTURA: si el nombre o alguna línea excede el ancho disponible, se reduce su tamaño hasta caber. El número completo y la fecha SHALL ser siempre legibles. El rótulo y el valor de `SERIE / Nº` y de `FECHA` SHALL usar el color de acento configurado.
- Dos tarjetas bajo la cabecera con esquinas redondeadas: «FACTURAR A» con los datos del cliente presentados como pares etiqueta→valor, cada dato en su propia fila — Nombre (destacado en negrita), NIF, Dirección, Código postal, Población (la localidad), Provincia y Email; las filas con campo vacío no aparecen. «DATOS DE PAGO» con forma de pago, vencimiento y realizada por (solo las filas rellenas); cuando los tres campos estén vacíos, la tarjeta «Datos de pago» SHALL NOT aparecer y «Facturar A» SHALL conservar su anchura con el espacio restante en blanco. La cabecera de «FACTURAR A» SHALL ir con fondo del color de acento y texto blanco; la cabecera de «DATOS DE PAGO» SHALL ir en blanco con un borde fino inferior del color de acento y texto en gris neutro. Ambos cuerpos SHALL ir en blanco. Las etiquetas de la tarjeta SHALL ir en gris neutro y los valores de la tarjeta SHALL ir en negro.
- En las facturas de **más de una página**, la tarjeta «FACTURAR A» SHALL repetirse en la página 2 y siguientes, con los mismos campos, el mismo ancho y el mismo alto que en la primera. La tarjeta «DATOS DE PAGO» SHALL aparecer solo en la primera página, y el hueco que deja SHALL quedar en blanco, de modo que el espacio reservado a las tarjetas sea idéntico en todas las páginas.
- Tabla de líneas con celdas bordeadas estilo hoja de cálculo: Cant / Descripción / Precio / IVA % / Total. El Total por línea SHALL incluir el IVA (base × (1 + IVA%)); las líneas exentas SHALL mostrar su importe sin IVA. La descripción SHALL mostrarse siempre en un único estilo, aunque ocupe varias líneas. El texto de la tabla SHALL ir en negro.
- Bloque de totales en **dos rejillas hermanas** que arrancan a la misma altura, ambas con cabecera en banda del color de acento y rótulos blancos en mayúsculas. La rejilla izquierda es el desglose de IVA, con columnas `TIPO | BASE IMPONIBLE | CUOTA IVA`, una fila por tipo de IVA de la factura —las exentas incluidas— y una última fila `Totales` que suma las bases y las cuotas. La columna `TIPO` SHALL mostrar el porcentaje como número (`21,00`) y la palabra `Exento` en los grupos sin porcentaje, cuya cuota SHALL imprimirse como `—`. La rejilla derecha es la liquidación, con las filas `Base imponible` y `Total IVA repercutido` siempre presentes, la retención si la hay (en rojo suave y restando) y los suplidos si los hay (sumando), rematada por la banda `TOTAL` con fondo del color de acento y texto blanco. Cuando el descuento global sea mayor que cero, bajo la rejilla izquierda SHALL aparecer una nota en cuerpo menor con el porcentaje, el importe descontado y la base bruta; las bases de la rejilla SHALL ser siempre las netas. Las cifras SHALL cuadrar: base imponible + total IVA − retención + suplidos = TOTAL.
- **Cierre anclado al pie.** El bloque de totales SHALL situarse al pie de la última página, y las observaciones SHALL ir inmediatamente debajo de él, de modo que el cierre acabe justo encima del pie legal con independencia de cuántas líneas tenga la factura. La tabla de líneas SHALL completarse con **filas vacías del mismo estilo** —mismos anchos de columna, mismo alto de fila y continuando el rayado alterno— hasta alcanzar el cierre. El bloque de suplidos, cuando exista, SHALL situarse entre esas filas vacías y el bloque de totales. Cuando el espacio restante no baste para el cierre, las filas vacías SHALL completar la página en curso hasta su borde inferior y el cierre SHALL pasar íntegro a una página nueva, anclado también a su pie y **sin** filas vacías, por haber terminado ya la tabla de líneas.
- Observaciones en caja clara con esquinas redondeadas, presentes solo en la última página; pie legal configurable dentro de un recuadro con borde de color, repetido en todas las páginas y compuesto a 6,5 pt; `Página X de Y` en cada página reflejando el número real de páginas, con el dígito total dibujado sin solapar la palabra «de». El alto reservado al pie SHALL calcularse a partir del número de líneas que ocupe el texto legal, de modo que un pie largo reduzca el área útil de la factura y el cierre suba con él.
- Tipografía Calibri embebida en el documento cuando esté disponible en el sistema; en caso contrario Helvetica. Los tonos de acento SHALL derivarse del color de acento configurado siguiendo el prototipo. El texto por defecto del documento (datos de empresa, datos del cliente, líneas de la tabla, observaciones, totales y pie) SHALL ir en negro o gris neutro, sin tinte de color, y SHALL NOT verse afectado por el color de acento salvo en los elementos marcados expresamente.

El resto se mantiene como estaba: descripciones largas ajustadas automáticamente, importes en formato español y sin símbolo de moneda, que aparece una sola vez en la banda `TOTAL`, fechas formato español (`11/08/2026`), marca `ANULADA` destacada en facturas anuladas, correspondencia exacta con la versión exportada, uso de la configuración actual de empresa/logo/cabecera/pie legal, documentos independientes, estructura `Facturas/AAAA/SERIE/` y nombre `CODIGO-CORRELATIVO-MES.pdf` sin indicar versión. El logo SHALL dibujarse siempre dentro de una caja fija de 240 × 120 pt respetando su proporción, sin que su tamaño ni su posición sean configurables, de modo que nunca invada el bloque FACTURA ni comprima la columna de datos de empresa. El color de acento SHALL tomarse de la preferencia `color_pdf`, con valor por defecto arena Alcazaba (`#B08D57`) si no está configurada.

#### Scenario: Exportar factura de varias páginas
- **WHEN** el usuario exporta una factura con descripciones largas que ocupa varias páginas
- **THEN** el PDF repite cabecera y pie en cada página e indica `Página X de Y`
- **AND** la tarjeta «Facturar a» aparece también en la página 2 y siguientes

#### Scenario: Cliente repetido a partir de la segunda página
- **WHEN** el usuario exporta una factura de dos páginas de un cliente con todos sus datos rellenos
- **THEN** la página 2 muestra la tarjeta «Facturar a» completa, con el mismo ancho y el mismo alto que en la página 1
- **AND** la página 2 no muestra la tarjeta «Datos de pago», y su hueco queda en blanco

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

#### Scenario: Rótulos y valores Serie/Nº y Fecha en color de acento
- **WHEN** el usuario exporta una factura con un color de acento configurado
- **THEN** el rótulo y el valor de `SERIE / Nº` y de `FECHA` se muestran en ese color de acento

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
- **AND** la cabecera de «Datos de pago» va en blanco con borde fino inferior y texto en gris neutro

#### Scenario: Datos de pago vacíos ocultan la tarjeta
- **WHEN** el usuario exporta una factura sin forma de pago, sin vencimiento y sin realizada por
- **THEN** la tarjeta «Datos de pago» no aparece en el PDF
- **AND** «Facturar A» conserva su anchura con el espacio restante en blanco

#### Scenario: Tarjetas bicolor con email y datos de pago opcionales

- **WHEN** el usuario exporta una factura cuyo cliente tiene email y con forma de pago, vencimiento y realizada por rellenados
- **THEN** la tarjeta «Facturar a» muestra el email del cliente y la tarjeta «Datos de pago» muestra los tres valores
- **AND** si el cliente no tiene email esa fila no aparece en el PDF

#### Scenario: Total por línea con IVA incluido

- **WHEN** el PDF contiene una línea con base 100,00 e IVA 21 %
- **THEN** la columna Total de esa línea muestra 121,00
- **AND** las líneas exentas muestran su importe sin IVA añadido

#### Scenario: Totales sin filas duplicadas
- **WHEN** el usuario exporta una factura sin descuento con base 100,00 al 21 %
- **THEN** la rejilla izquierda muestra la fila `21,00 | 100,00 | 21,00` y la fila `Totales | 100,00 | 21,00`
- **AND** la rejilla derecha muestra `Base imponible 100,00`, `Total IVA repercutido 21,00` y la banda `TOTAL 121,00 €`
- **AND** no aparece ninguna nota de descuento bajo la rejilla izquierda

#### Scenario: Totales con descuento cuadrando
- **WHEN** el usuario exporta una factura con descuento global del 10 % sobre base 1.000,00 al 21 %
- **THEN** la rejilla izquierda muestra la base ya descontada, `21,00 | 900,00 | 189,00`, y la fila `Totales | 900,00 | 189,00`
- **AND** bajo esa rejilla aparece una nota en cuerpo menor que indica el 10 %, el importe descontado 100,00 y la base bruta 1.000,00
- **AND** la banda muestra `TOTAL 1.089,00 €`
- **AND** el bloque no repite las bases con dos rótulos distintos

#### Scenario: Varios tipos de IVA con un solo descuento
- **WHEN** el usuario exporta una factura con una línea de 1.000,00 al 21 %, otra de 500,00 al 10 % y descuento global del 10 %
- **THEN** la rejilla izquierda muestra una fila por tipo, `21,00 | 900,00 | 189,00` y `10,00 | 450,00 | 45,00`, y la fila `Totales | 1.350,00 | 234,00`
- **AND** la rejilla derecha muestra `Base imponible 1.350,00` y `Total IVA repercutido 234,00`
- **AND** la banda muestra `TOTAL 1.584,00 €`

#### Scenario: Tipo exento en la rejilla
- **WHEN** el usuario exporta una factura que incluye una línea exenta de 350,00
- **THEN** la rejilla izquierda incluye una fila con `Exento` en la columna de tipo, su base en la columna de base imponible y un guion en la columna de cuota
- **AND** esa base se suma en la fila `Totales`

#### Scenario: Retención y suplidos en la liquidación
- **WHEN** el usuario exporta una factura con retención del 15 % y suplidos por 200,00
- **THEN** la rejilla derecha muestra la retención restando en rojo suave y los suplidos sumando, en ese orden, entre `Total IVA repercutido` y la banda `TOTAL`
- **AND** se cumple base imponible + total IVA − retención + suplidos = TOTAL

#### Scenario: Liquidación sin retención ni suplidos
- **WHEN** la factura no tiene retención ni suplidos
- **THEN** la rejilla derecha muestra sólo `Base imponible`, `Total IVA repercutido` y la banda `TOTAL`

#### Scenario: El cierre cae a la misma altura sea cual sea el número de líneas
- **WHEN** el usuario exporta dos facturas del mismo cliente, una de 2 líneas y otra de 20, ninguna con observaciones y con el mismo pie legal
- **THEN** el bloque de totales arranca a la misma altura en las dos
- **AND** en ambas queda inmediatamente encima del pie legal

#### Scenario: Filas vacías hasta el cierre
- **WHEN** el usuario exporta una factura de 2 líneas
- **THEN** la tabla de líneas continúa con filas vacías hasta el bloque de totales
- **AND** esas filas conservan los anchos de columna, el alto de fila y la alternancia de color de las filas con contenido, sin repetir la cabecera

#### Scenario: Los suplidos quedan entre el relleno y los totales
- **WHEN** el usuario exporta una factura corta que tiene suplidos
- **THEN** las filas vacías van detrás de la tabla de líneas
- **AND** el bloque «SUPLIDOS» con su nota legal aparece después de esas filas y antes del bloque de totales

#### Scenario: Factura de solo suplidos
- **WHEN** todas las líneas de la factura son suplidos y por tanto no hay tabla de líneas
- **THEN** no se imprime ninguna fila vacía
- **AND** el bloque de totales sigue anclado al pie de la página

#### Scenario: Los totales no quedan solos en una página
- **WHEN** el usuario exporta una factura larga cuya tabla de líneas llega hasta el borde inferior de la última página
- **THEN** el cierre pasa íntegro a una página nueva, anclado a su pie
- **AND** esa página nueva no lleva filas vacías, porque la tabla de líneas ya ha terminado
- **AND** `Página X de Y` cuenta esa página como página real

#### Scenario: Nada se solapa
- **WHEN** el usuario exporta cualquier factura
- **THEN** la tarjeta del cliente queda íntegramente por encima de la primera fila de la tabla de líneas, sin invadirla
- **AND** ninguna página queda vacía de contenido, con solo la cabecera y el pie

#### Scenario: La factura no gana páginas
- **WHEN** el usuario exporta una factura de hasta veinte líneas, con o sin suplidos
- **THEN** el documento ocupa una sola página

#### Scenario: Tipografía y pie sin solape
- **WHEN** el usuario exporta una factura con Calibri disponible en el sistema
- **THEN** el documento embebe la fuente Calibri
- **AND** el pie muestra `Página X de Y` con el número total separado correctamente de la palabra «de»

#### Scenario: Pie legal largo
- **WHEN** el usuario exporta una factura cuyo pie legal ocupa muchas líneas, como el aviso completo de protección de datos
- **THEN** el pie se compone a 6,5 pt dentro de su recuadro, sin que el texto se salga
- **AND** el área útil de la factura se reduce y el cierre sube con el pie, sin solaparse con él

#### Scenario: El símbolo de moneda aparece una sola vez
- **WHEN** el usuario exporta cualquier factura
- **THEN** el texto del PDF contiene exactamente una aparición de `€`, la del importe de la banda `TOTAL`
- **AND** las rejillas, la tabla de líneas y la tabla de suplidos muestran los importes sin símbolo de moneda

#### Scenario: Texto por defecto en negro y gris neutro
- **WHEN** el usuario exporta una factura con un color de acento cualquiera
- **THEN** los datos de empresa, los datos del cliente, las líneas de la tabla, las observaciones, los totales y el pie se muestran en negro o gris neutro, sin tinte del color de acento ni de ningún tono fijo de color
