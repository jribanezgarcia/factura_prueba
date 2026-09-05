## Why

En el Editor, bloque CLIENTE, los campos Nombre, Email y Localidad quedaron en ~122 px tras el reparto del change anterior: los nombres de empresa y de persona, que suelen ser largos, apenas se leen. Además, como las columnas de etiqueta están fijadas a 105 px (medida que exige FACTURA por «Forma de pago»), las etiquetas cortas «NIF» y «CP» quedan a ~93 px de sus campos, visualmente desconectadas de ellos.

## What Changes

- En la rejilla CLIENTE, las columnas de etiqueta pasan de 105 a 75 px (sus etiquetas más largas —«Dirección», «Localidad»— rondan los 65 px) y las de campo a 240/90: Nombre, Email y Localidad casi duplican su ancho y NIF/CP/Provincia conservan el suyo, con sus etiquetas mucho más próximas.
- El bloque CLIENTE crece de 485 a 505 y FACTURA cede de 440 a 420 (Serie/Fecha quedan en ~93 px, siguen legibles).
- No cambia ningún comportamiento, ningún texto, ningún controlador ni el resto de pantallas.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se precisa «Distribución estable al redimensionar en Editor e Histórico» — los campos Nombre, Email y Localidad del bloque CLIENTE SHALL tener anchura para nombres largos, y las etiquetas NIF/CP/Provincia SHALL quedar próximas a sus campos.

## Impact

- `ui/Editor.fxml`: `ColumnConstraints` de la rejilla CLIENTE, `prefWidth`/`maxWidth` de los dos `VBox` de cabecera.
- No se tocan controladores, lógica, servicios, PDF ni otros temas.

### De dónde sale el hueco

No hay hueco libre: a 1024 todo el ancho está repartido. El hueco sale de dos sitios: (1) las columnas de etiqueta de CLIENTE no necesitan los 105 px de FACTURA —con 75 basta— y (2) el bloque FACTURA cede 20 px (Serie/Fecha pasan de ~103 a ~93 px, aún legibles). La contrapartida aceptada es que las etiquetas de ambos bloques dejan de estar alineadas entre sí; cada bloque alinea las suyas.
