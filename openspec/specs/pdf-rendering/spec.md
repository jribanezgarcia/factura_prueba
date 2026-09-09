# pdf-rendering Specification

## Purpose
Composicion y dibujo del documento PDF de factura a partir de los datos ya resueltos por la capa de servicio: maquetacion, cabecera, tarjetas, tablas, totales, suplidos y pie legal.

## Requirements

### Requirement: Vista previa de la cabecera del PDF

La sección Cabecera y pie SHALL mostrar una previsualización de la cabecera del PDF que refleje el modo elegido, el logo con su posición y tamaño efectivos y el color de acento configurado, actualizándose al modificar cualquiera de esos valores. La previsualización SHALL usar las mismas reglas de geometría que emplea la generación del PDF, de modo que el tamaño efectivo del logo mostrado coincida con el impreso. La aplicación SHALL indicar junto a los campos de tamaño el tamaño efectivo resultante, y SHALL advertir de que la previsualización es aproximada.

#### Scenario: La previsualización refleja el tamaño real del logo
- **WHEN** el usuario configura un logo de 120 × 60 pt
- **THEN** la previsualización lo muestra al tamaño efectivo con el que se imprime, que es el doble del configurado, y la pantalla indica ese tamaño efectivo

#### Scenario: La previsualización reacciona a los cambios
- **WHEN** el usuario cambia la imagen del logo, su posición, su tamaño o el color de acento
- **THEN** la previsualización se actualiza sin necesidad de guardar ni de exportar una factura

#### Scenario: Modo texto
- **WHEN** el usuario elige el modo de cabecera con datos de empresa
- **THEN** la previsualización muestra las líneas de la empresa con el NIF destacado, como aparecen en el PDF

### Requirement: Retención en PDF

La exportación a PDF SHALL incluir una fila de retención en el resumen de totales cuando la factura tenga un tipo de retención seleccionado. La fila SHALL mostrar el nombre del tipo de retención y el importe retenido, y el total SHALL reflejar la resta de la retención.

#### Scenario: PDF con retención
- **WHEN** el usuario exporta a PDF una factura con base 1.000,00 €, IVA 21 % y retención del 15 %
- **THEN** el PDF muestra la retención como una fila propia y el total es 1.060,00 €

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical con el diseño aprobado inspirado en el documento Excel de la empresa:

- Cabecera en todas las páginas: logo a un tamaño fijo que la aplicación no permite alterar (modo logo) o datos de empresa (modo texto), con el NIF de la empresa destacado en línea propia con esquinas redondeadas; a la derecha la palabra FACTURA y debajo, como pares rótulo→valor, `SERIE / Nº` sobre el número completo y `FECHA` sobre la fecha. Los datos de empresa SHALL ocupar una columna propia que SHALL NOT solaparse nunca con el bloque FACTURA: si el nombre o alguna línea excede el ancho disponible, se reduce su tamaño hasta caber. El número completo y la fecha SHALL ser siempre legibles. El rótulo y el valor de `SERIE / Nº` y de `FECHA` SHALL usar el color de acento configurado.
- Dos tarjetas bajo la cabecera con esquinas redondeadas: «FACTURAR A» con los datos del cliente presentados como pares etiqueta→valor, cada dato en su propia fila — Nombre (destacado en negrita), NIF, Dirección, Código postal, Población (la localidad), Provincia y Email; las filas con campo vacío no aparecen. «DATOS DE PAGO» con forma de pago, vencimiento y realizada por (solo las filas rellenas); cuando los tres campos estén vacíos, la tarjeta «Datos de pago» SHALL NOT aparecer y «Facturar A» SHALL conservar su anchura con el espacio restante en blanco. La cabecera de «FACTURAR A» SHALL ir con fondo del color de acento y texto blanco; la cabecera de «DATOS DE PAGO» SHALL ir en blanco con un borde fino inferior del color de acento y texto en gris neutro. Ambos cuerpos SHALL ir en blanco. Las etiquetas de la tarjeta SHALL ir en gris neutro y los valores de la tarjeta SHALL ir en negro. Cada tarjeta SHALL ocupar el alto que necesite su contenido, sin estirarse hasta igualar a la otra. La columna de etiquetas SHALL ajustarse al ancho de la etiqueta más larga, de modo que el valor arranque junto a su rótulo y no quede un pasillo vacío entre ambos.
- En las facturas de **más de una página**, la tarjeta «FACTURAR A» SHALL repetirse en la página 2 y siguientes, con los mismos campos, el mismo ancho y el mismo alto que en la primera. La tarjeta «DATOS DE PAGO» SHALL aparecer solo en la primera página, y el hueco que deja SHALL quedar en blanco, de modo que el espacio reservado a las tarjetas sea idéntico en todas las páginas.
- Tabla de líneas con celdas bordeadas estilo hoja de cálculo: Cant / Descripción / Precio / IVA % / Total. El Total por línea SHALL incluir el IVA (base × (1 + IVA%)); las líneas exentas SHALL mostrar su importe sin IVA. La descripción SHALL mostrarse siempre en un único estilo, aunque ocupe varias líneas. El texto de la tabla SHALL ir en negro. La fila de cabecera de columnas SHALL repetirse al principio de cada página que continúe la tabla.
- Bloque de totales en **dos rejillas hermanas** que arrancan a la misma altura, ambas con cabecera en banda del color de acento y rótulos blancos en mayúsculas. La rejilla izquierda es el desglose de IVA, con columnas `TIPO | BASE IMPONIBLE | CUOTA IVA`, una fila por tipo de IVA de la factura —las exentas incluidas— y una última fila `Totales` que suma las bases y las cuotas. La columna `TIPO` SHALL mostrar el porcentaje como número (`21,00`) y la palabra `Exento` en los grupos sin porcentaje, cuya cuota SHALL imprimirse como `—`. La rejilla derecha es la liquidación, con las filas `Base imponible` y `Total IVA repercutido` siempre presentes, la retención si la hay (en rojo suave y restando) y los suplidos si los hay (sumando), rematada por la banda `TOTAL` con fondo del color de acento y texto blanco. Cuando el descuento global sea mayor que cero, bajo la rejilla izquierda SHALL aparecer una nota en cuerpo menor con el porcentaje, el importe descontado y la base bruta; las bases de la rejilla SHALL ser siempre las netas. Las cifras SHALL cuadrar: base imponible + total IVA − retención + suplidos = TOTAL.
- **Cierre anclado al pie.** El bloque de totales SHALL situarse al pie de la última página, y las observaciones SHALL ir inmediatamente debajo de él, de modo que el cierre acabe justo encima del pie legal con independencia de cuántas líneas tenga la factura. El hueco entre la última línea y el cierre SHALL dibujarse como **marco de columnas**: los bordes laterales de la tabla y sus divisiones verticales SHALL prolongarse hasta el cierre, cerrados por abajo, **sin renglones horizontales ni rayado alterno**. El bloque de suplidos, cuando exista, SHALL situarse entre ese marco y el bloque de totales. Cuando el espacio restante no baste para el cierre, el marco SHALL completar la página en curso hasta su borde inferior y el cierre SHALL pasar íntegro a una página nueva, anclado también a su pie. Esa página nueva SHALL llevar igualmente la cabecera de columnas y el marco hasta el cierre, de modo que no quede media hoja en blanco.
- Observaciones en caja clara con esquinas redondeadas, presentes solo en la última página. El pie legal configurable SHALL imprimirse dentro de un recuadro con borde de color, compuesto a 6,5 pt, **una sola vez y solo en la última página**, como parte del cierre y por debajo de las observaciones. Su alto SHALL contar dentro del alto del cierre, de modo que el conjunto siga acabando al pie de la página. Las páginas anteriores SHALL NOT reservar espacio para el texto legal. `Página X de Y` SHALL seguir apareciendo en todas las páginas, reflejando el número real de páginas y con el dígito total dibujado sin solapar la palabra «de».
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

#### Scenario: Cada tarjeta mide lo que necesita
- **WHEN** el usuario exporta una factura cuyo cliente tiene siete campos rellenos y cuyos datos de pago solo tienen tres
- **THEN** la tarjeta «Datos de pago» acaba justo debajo de su última fila, sin estirarse hasta la altura de «Facturar a»
- **AND** en las páginas siguientes el espacio reservado a las tarjetas sigue siendo el mismo que en la primera, de modo que la tarjeta repetida no invade la tabla de líneas

#### Scenario: La etiqueta va junto a su valor
- **WHEN** el usuario exporta cualquier factura
- **THEN** en las dos tarjetas el valor arranca inmediatamente después de la etiqueta más larga, sin dejar un pasillo vacío entre el rótulo y el dato

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
- **THEN** el hueco entre la última línea y el bloque de totales SHALL NOT contener ningún renglón vacío
- **AND** los bordes laterales y las divisiones verticales de las columnas se prolongan hasta el cierre, cerrados por abajo

#### Scenario: Los suplidos quedan entre el relleno y los totales
- **WHEN** el usuario exporta una factura corta que tiene suplidos
- **THEN** el marco de columnas va detrás de la tabla de líneas
- **AND** el bloque «SUPLIDOS» con su nota legal aparece después del marco y antes del bloque de totales

#### Scenario: Factura de solo suplidos
- **WHEN** todas las líneas de la factura son suplidos
- **THEN** la tabla de líneas se imprime igualmente, con su cabecera de columnas y sin ninguna fila de datos, y el marco baja hasta el bloque de suplidos
- **AND** el bloque de totales sigue anclado al pie de la página

#### Scenario: Los totales no quedan solos en una página
- **WHEN** el usuario exporta una factura larga cuya tabla de líneas llega hasta el borde inferior de la última página
- **THEN** el cierre pasa íntegro a una página nueva, anclado a su pie
- **AND** esa página nueva muestra la cabecera de columnas y el marco bajando hasta el cierre, sin que quede media hoja en blanco
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
- **WHEN** el usuario exporta una factura de una página cuyo pie legal ocupa muchas líneas, como el aviso completo de protección de datos
- **THEN** el pie se compone a 6,5 pt dentro de su recuadro, sin que el texto se salga
- **AND** el conjunto de totales, observaciones y pie legal acaba al pie de la página, sin solapes

#### Scenario: El pie legal sale una sola vez
- **WHEN** el usuario exporta una factura de varias páginas con pie legal configurado
- **THEN** el texto legal aparece únicamente en la última página, debajo del bloque de totales y de las observaciones
- **AND** las páginas anteriores no reservan espacio para él, por lo que caben más líneas de factura que antes
- **AND** `Página X de Y` sigue apareciendo en todas las páginas

#### Scenario: El cierre con pie legal no cabe
- **WHEN** el pie legal es tan largo que el bloque de totales, las observaciones y el propio pie no caben en el espacio que queda
- **THEN** el conjunto pasa íntegro a una página nueva, anclado a su pie
- **AND** no se parte el recuadro del pie legal entre dos páginas

#### Scenario: Cabecera de columnas en todas las páginas
- **WHEN** el usuario exporta una factura cuya tabla de líneas ocupa más de una página
- **THEN** cada página que continúa la tabla arranca con la fila de cabecera `CANT. / DESCRIPCIÓN / PRECIO / IVA % / TOTAL`

#### Scenario: Factura con logo
- **WHEN** el usuario exporta una factura de una empresa configurada en modo logo
- **THEN** el logo, los datos de empresa, el bloque FACTURA y la tarjeta del cliente no se solapan entre sí ni con la tabla de líneas
- **AND** el cierre queda anclado al pie de la última página igual que en modo texto

#### Scenario: Factura anulada de varias páginas
- **WHEN** el usuario exporta una factura anulada que ocupa más de una página
- **THEN** la marca `ANULADA` aparece en todas las páginas sin ocultar la tarjeta del cliente ni las líneas de la factura

#### Scenario: El símbolo de moneda aparece una sola vez
- **WHEN** el usuario exporta cualquier factura
- **THEN** el texto del PDF contiene exactamente una aparición de `€`, la del importe de la banda `TOTAL`
- **AND** las rejillas, la tabla de líneas y la tabla de suplidos muestran los importes sin símbolo de moneda

#### Scenario: Texto por defecto en negro y gris neutro
- **WHEN** el usuario exporta una factura con un color de acento cualquiera
- **THEN** los datos de empresa, los datos del cliente, las líneas de la tabla, las observaciones, los totales y el pie se muestran en negro o gris neutro, sin tinte del color de acento ni de ningún tono fijo de color

### Requirement: Exportación múltiple a PDF desde el histórico

La aplicación SHALL permitir exportar a PDF varias facturas seleccionadas en el histórico. Cuando se seleccionen varias facturas, el sistema SHALL preguntar si se desea generar un PDF por factura o un único PDF agrupado. Para la opción "un PDF por factura", el sistema SHALL guardar un archivo por cada factura en la carpeta elegida. Para la opción "único PDF agrupado", el sistema SHALL guardar un solo archivo que contenga todas las facturas seleccionadas.

#### Scenario: Exportar varias facturas a PDF
- **WHEN** el usuario selecciona varias facturas y pulsa "Exportar a PDF"
- **THEN** el sistema pregunta si quiere un PDF por factura o un PDF agrupado
- **AND** genera el resultado elegido

### Requirement: Orden del desglose en el PDF

> Procede de «Orden del desglose de totales» de `invoicing`. Alli se quedan la
> regla general del desglose y su presentacion en el editor, que son negocio y
> pantalla; aqui viene el parrafo del PDF con su escenario.

En el **PDF**, el desglose SHALL presentarse como las dos rejillas hermanas descritas en el requisito «Exportación a PDF»: el desglose de IVA a la izquierda, con una fila por tipo y una última fila `Totales`, y la liquidación a la derecha, terminada en la banda `TOTAL`. El descuento global SHALL resolverse como nota bajo la rejilla izquierda, sin duplicar el bloque de bases. El PDF SHALL NOT imprimir los rótulos `Subtotal`, `Subtotal N %`, `Subtotal exento`, `Base imponible N %` ni `Base exenta`.

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

### Requirement: Suplidos en el PDF

> Procede de «Suplidos» de `invoicing`. Que un suplido no forme parte de la base
> imponible, no genere cuota, no entre en la base de retencion y no se vea
> afectado por el descuento son reglas de negocio y se quedan alli.

En el PDF, las líneas de suplido SHALL NOT aparecer en la tabla de líneas junto a las operaciones facturadas, y SHALL NOT rotularse como exentas: un suplido no es una operación exenta de IVA. SHALL presentarse en un bloque propio, situado tras la tabla de líneas y antes del bloque de totales, con la descripción y el importe de cada suplido. Ese bloque SHALL llevar una nota que deje constancia de que se han pagado en nombre y por cuenta del cliente, facturados a su nombre, y de que no están sujetos a IVA ni a retención. El bloque SHALL aparecer solo cuando la factura tenga al menos un suplido.

Cuando todas las líneas de la factura sean suplidos, la tabla de líneas SHALL imprimirse igualmente con su cabecera de columnas y sin filas de datos, de modo que la hoja no quede vacía entre las tarjetas y el bloque de suplidos.

#### Scenario: El suplido tiene su propio bloque en el PDF
- **WHEN** el usuario exporta el PDF de una factura con una línea al 21 % y un suplido de 250,00 €
- **THEN** la tabla de líneas muestra solo la línea al 21 %
- **AND** un bloque titulado «SUPLIDOS» muestra la descripción del suplido y su importe de 250,00 €
- **AND** ese bloque lleva la nota de que se han pagado en nombre y por cuenta del cliente y no están sujetos a IVA ni a retención
- **AND** la fila «Suplidos» del bloque de totales sigue mostrando 250,00 €

#### Scenario: Un suplido nunca se rotula como exento
- **WHEN** el usuario exporta el PDF de una factura que contiene un suplido
- **THEN** ninguna fila del documento describe ese suplido como una operación exenta de IVA

#### Scenario: Factura de solo suplidos
- **WHEN** todas las líneas de la factura son suplidos
- **THEN** el PDF imprime la tabla de líneas con su cabecera de columnas y sin filas de datos
- **AND** el bloque de suplidos recoge todas las líneas

### Requirement: Separación de responsabilidades en la exportación

La capa de exportación a PDF SHALL limitarse a componer y dibujar el documento.

SHALL NOT calcular ningún importe ni derivar dato alguno del modelo de negocio: los importes, el desglose por tipo de IVA, la retención aplicable y la clasificación de una línea como suplido SHALL llegarle ya resueltos desde la capa de servicio.

Toda consulta que hoy resuelva la capa de exportación por su cuenta SHALL trasladarse a la capa de servicio, sin cambiar su resultado. El PDF producido SHALL ser idéntico al anterior al cambio.

#### Scenario: La exportación no deriva la retención
- **WHEN** se exporta una factura con retención
- **THEN** el importe y el porcentaje de la retención provienen de la versión de factura ya resuelta por la capa de servicio
- **AND** la capa de exportación no vuelve a buscar el tipo de retención por su cuenta

#### Scenario: La exportación no calcula el total de una línea
- **WHEN** se exporta una factura con líneas a distintos tipos de IVA
- **THEN** el total con IVA de cada línea llega ya calculado
- **AND** el PDF resultante es idéntico al que se generaba antes del cambio

### Requirement: Composición del documento verificable sin PDF

La composición del documento de factura SHALL ser verificable sin generar ningún PDF: a partir de los mismos datos de entrada (versión de factura, empresa y color) SHALL poder obtenerse el contenido ya compuesto —textos, rótulos y filas— como datos, antes de dibujarlo.

Ese contenido SHALL NOT incluir paginación, alturas ni coordenadas: en qué página cae cada cosa depende de cómo se reparta el texto al dibujar, y se decide al componer el PDF, no antes.

Los rótulos fijos del documento («BASE IMPONIBLE», «TOTAL», «SUPLIDOS», encabezados de columna y demás textos que no dependen de los datos) SHALL formar parte del modelo del documento, no del código de dibujo: el mismo rótulo SHALL aparecer idéntico se dibuje como se dibuje.

La extracción SHALL NOT cambiar el documento: el PDF generado a partir del modelo SHALL ser idéntico al que se generaba antes —mismos importes, mismas coordenadas, mismo número de páginas.

#### Scenario: El documento se compone sin generar PDF
- **WHEN** se pide el documento compuesto de una factura con descuento global y retención
- **THEN** el resultado contiene los textos, los rótulos y los importes ya formateados sin haberse generado ningún fichero
- **AND** no contiene número de páginas, alturas ni coordenadas

#### Scenario: Los rótulos viven en el modelo
- **WHEN** se compone el documento de cualquier factura
- **THEN** los rótulos fijos vienen en el documento compuesto y el dibujo los reproduce tal cual

#### Scenario: El PDF generado desde el modelo no cambia
- **WHEN** se genera el PDF a partir del documento compuesto
- **THEN** es idéntico al que se generaba antes del cambio en importes, coordenadas y páginas
