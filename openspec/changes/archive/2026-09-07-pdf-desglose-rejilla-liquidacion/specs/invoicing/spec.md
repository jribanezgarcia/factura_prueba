## MODIFIED Requirements

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical con el diseño aprobado inspirado en el documento Excel de la empresa:

- Cabecera en todas las páginas: logo a un tamaño fijo que la aplicación no permite alterar (modo logo) o datos de empresa (modo texto), con el NIF de la empresa destacado en línea propia con esquinas redondeadas; a la derecha la palabra FACTURA y debajo, como pares rótulo→valor, `SERIE / Nº` sobre el número completo y `FECHA` sobre la fecha. Los datos de empresa SHALL ocupar una columna propia que SHALL NOT solaparse nunca con el bloque FACTURA: si el nombre o alguna línea excede el ancho disponible, se reduce su tamaño hasta caber. El número completo y la fecha SHALL ser siempre legibles. El rótulo y el valor de `SERIE / Nº` y de `FECHA` SHALL usar el color de acento configurado.
- Dos tarjetas bajo la cabecera con esquinas redondeadas: «FACTURAR A» con los datos del cliente presentados como pares etiqueta→valor, cada dato en su propia fila — Nombre (destacado en negrita), NIF, Dirección, Código postal, Población (la localidad), Provincia y Email; las filas con campo vacío no aparecen. «DATOS DE PAGO» con forma de pago, vencimiento y realizada por (solo las filas rellenas); cuando los tres campos estén vacíos, la tarjeta «Datos de pago» SHALL NOT aparecer y «Facturar A» SHALL conservar su anchura con el espacio restante en blanco. La cabecera de «FACTURAR A» SHALL ir con fondo del color de acento y texto blanco; la cabecera de «DATOS DE PAGO» SHALL ir en blanco con un borde fino inferior del color de acento y texto en gris neutro. Ambos cuerpos SHALL ir en blanco. Las etiquetas de la tarjeta SHALL ir en gris neutro y los valores de la tarjeta SHALL ir en negro.
- Tabla de líneas con celdas bordeadas estilo hoja de cálculo: Cant / Descripción / Precio / IVA % / Total. El Total por línea SHALL incluir el IVA (base × (1 + IVA%)); las líneas exentas SHALL mostrar su importe sin IVA. La descripción SHALL mostrarse siempre en un único estilo, aunque ocupe varias líneas. El texto de la tabla SHALL ir en negro.
- Bloque de totales en **dos rejillas hermanas** que arrancan a la misma altura, ambas con cabecera en banda del color de acento y rótulos blancos en mayúsculas. La rejilla izquierda es el desglose de IVA, con columnas `TIPO | BASE IMPONIBLE | CUOTA IVA`, una fila por tipo de IVA de la factura —las exentas incluidas— y una última fila `Totales` que suma las bases y las cuotas. La columna `TIPO` SHALL mostrar el porcentaje como número (`21,00`) y la palabra `Exento` en los grupos sin porcentaje, cuya cuota SHALL imprimirse como `—`. La rejilla derecha es la liquidación, con las filas `Base imponible` y `Total IVA repercutido` siempre presentes, la retención si la hay (en rojo suave y restando) y los suplidos si los hay (sumando), rematada por la banda `TOTAL` con fondo del color de acento y texto blanco. Cuando el descuento global sea mayor que cero, bajo la rejilla izquierda SHALL aparecer una nota en cuerpo menor con el porcentaje, el importe descontado y la base bruta; las bases de la rejilla SHALL ser siempre las netas. Las cifras SHALL cuadrar: base imponible + total IVA − retención + suplidos = TOTAL.
- Observaciones en caja clara con esquinas redondeadas; pie legal configurable dentro de un recuadro con borde de color, repetido en todas las páginas; `Página X de Y` en cada página reflejando el número real de páginas, con el dígito total dibujado sin solapar la palabra «de». El cierre del documento SHALL mantenerse compacto (totales estrechos y tablas capaces de repartir sus filas entre páginas) para evitar una página que contenga únicamente el bloque de totales cuando el contenido cabe repartiéndose.
- Tipografía Calibri embebida en el documento cuando esté disponible en el sistema; en caso contrario Helvetica. Los tonos de acento SHALL derivarse del color de acento configurado siguiendo el prototipo. El texto por defecto del documento (datos de empresa, datos del cliente, líneas de la tabla, observaciones, totales y pie) SHALL ir en negro o gris neutro, sin tinte de color, y SHALL NOT verse afectado por el color de acento salvo en los elementos marcados expresamente.

El resto se mantiene como estaba: descripciones largas ajustadas automáticamente, importes en formato español y sin símbolo de moneda, que aparece una sola vez en la banda `TOTAL`, fechas formato español (`11/08/2026`), marca `ANULADA` destacada en facturas anuladas, correspondencia exacta con la versión exportada, uso de la configuración actual de empresa/logo/cabecera/pie legal, documentos independientes, estructura `Facturas/AAAA/SERIE/` y nombre `CODIGO-CORRELATIVO-MES.pdf` sin indicar versión. El logo SHALL dibujarse siempre dentro de una caja fija de 240 × 120 pt respetando su proporción, sin que su tamaño ni su posición sean configurables, de modo que nunca invada el bloque FACTURA ni comprima la columna de datos de empresa. El color de acento SHALL tomarse de la preferencia `color_pdf`, con valor por defecto arena Alcazaba (`#B08D57`) si no está configurada.

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

#### Scenario: Los totales no quedan solos en una página
- **WHEN** el usuario exporta una factura larga cuyo bloque de totales cabe en el espacio restante de la última página una vez compactado
- **THEN** no se genera una página adicional que contenga únicamente los totales
- **AND** si el bloque realmente no cabe, pasa íntegro a la página siguiente y `Página X de Y` la cuenta como página real

#### Scenario: Tipografía y pie sin solape
- **WHEN** el usuario exporta una factura con Calibri disponible en el sistema
- **THEN** el documento embebe la fuente Calibri
- **AND** el pie muestra `Página X de Y` con el número total separado correctamente de la palabra «de»

#### Scenario: El símbolo de moneda aparece una sola vez
- **WHEN** el usuario exporta cualquier factura
- **THEN** el texto del PDF contiene exactamente una aparición de `€`, la del importe de la banda `TOTAL`
- **AND** las rejillas, la tabla de líneas y la tabla de suplidos muestran los importes sin símbolo de moneda

#### Scenario: Texto por defecto en negro y gris neutro
- **WHEN** el usuario exporta una factura con un color de acento cualquiera
- **THEN** los datos de empresa, los datos del cliente, las líneas de la tabla, las observaciones, los totales y el pie se muestran en negro o gris neutro, sin tinte del color de acento ni de ningún tono fijo de color

### Requirement: Orden del desglose de totales

Cuando la factura tenga **varios tipos de IVA**, el desglose SHALL mostrar la base imponible de **cada tipo** junto a su cuota, de modo que cada cuota impresa sea comprobable a partir de una base impresa. SHALL NOT mostrarse una única base imponible agregada en lugar de las bases por tipo. El desglose SHALL incluir además una suma de las bases y una suma de las cuotas.

En el **editor**, ese desglose por tipo SHALL presentarse como una matriz con una fila por tipo de IVA y columnas de base imponible y cuota, más una fila de totales. Junto a la matriz SHALL mostrarse la escalera hasta el total, con las filas de subtotal, descuento, base imponible, IVA total, retención, suplidos y TOTAL FACTURA, cada una sujeta a su condición de aparición. Las filas de subtotal y descuento SHALL aparecer solo cuando el descuento global sea mayor que 0.

En el **PDF**, el desglose SHALL presentarse como las dos rejillas hermanas descritas en el requisito «Exportación a PDF»: el desglose de IVA a la izquierda, con una fila por tipo y una última fila `Totales`, y la liquidación a la derecha, terminada en la banda `TOTAL`. El descuento global SHALL resolverse como nota bajo la rejilla izquierda, sin duplicar el bloque de bases. El PDF SHALL NOT imprimir los rótulos `Subtotal`, `Subtotal N %`, `Subtotal exento`, `Base imponible N %` ni `Base exenta`.

El editor y el PDF SHALL partir del mismo cálculo y SHALL mostrar los mismos importes para la misma factura. No están obligados a compartir disposición ni rótulos.

#### Scenario: Desglose por tipo en el editor
- **WHEN** el usuario edita una factura con una línea de 1.000,00 € al 21 %, otra de 500,00 € al 10 % y un descuento global del 10 %
- **THEN** la matriz muestra una fila de 900,00 € con cuota 189,00 €, otra de 450,00 € con cuota 45,00 €, y una fila de totales con 1.350,00 € y 234,00 €
- **AND** cada cuota coincide con aplicar su tipo a la base imponible de su misma fila

#### Scenario: Factura con descuento en el editor
- **WHEN** el usuario edita una factura con base 200,00 €, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la escalera muestra, en este orden: subtotal 200,00 €, descuento −20,00 €, base imponible 180,00 €, IVA total 37,80 €, retención −27,00 € y TOTAL FACTURA 190,80 €

#### Scenario: Factura sin descuento
- **WHEN** la factura no tiene descuento global
- **THEN** la escalera del editor no muestra las filas de subtotal ni de descuento, y su primera fila visible es la base imponible
- **AND** bajo la rejilla de IVA del PDF no aparece ninguna nota de descuento

#### Scenario: Factura con descuento en el PDF
- **WHEN** el usuario exporta el PDF de una factura con base 200,00, descuento global del 10 %, IVA 21 % y retención del 15 %
- **THEN** la rejilla de IVA muestra la fila `21,00 | 180,00 | 37,80` y la fila `Totales | 180,00 | 37,80`, con la nota del descuento debajo
- **AND** la liquidación muestra `Base imponible 180,00`, `Total IVA repercutido 37,80`, la retención −27,00 y la banda `TOTAL 190,80 €`

#### Scenario: Factura con varios tipos de IVA y descuento
- **WHEN** el usuario exporta el PDF de una factura con una línea de 1.000,00 al 21 %, otra de 500,00 al 10 % y un descuento global del 10 %
- **THEN** la rejilla de IVA muestra una fila por tipo con su base y su cuota, y la fila `Totales` con 1.350,00 y 234,00
- **AND** los importes son los mismos que muestra el editor para esa factura

#### Scenario: El PDF no usa los rótulos de la escalera
- **WHEN** el usuario exporta cualquier factura, con descuento o sin él
- **THEN** el PDF no contiene los textos `Subtotal`, `Subtotal exento`, `Base imponible 21%` ni `Base exenta`
- **AND** el descuento, si lo hay, aparece una sola vez, en la nota bajo la rejilla de IVA

#### Scenario: Los importes no cambian
- **WHEN** se presenta el desglose de cualquier factura
- **THEN** todos los importes son idénticos a los calculados antes del cambio
