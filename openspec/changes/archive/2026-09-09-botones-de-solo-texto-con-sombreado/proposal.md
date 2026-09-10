## Why

Conviven dos aspectos de botón en la aplicación. Los que llevan icono son planos: no pintan caja ni borde y solo reaccionan al ratón. Los que llevan únicamente texto reciben fondo blanco y borde de cada tema, así que Clientes, Configuración, Copia de seguridad y Versiones se leen como una rejilla de cajas junto a un Editor y un Histórico que ya no lo son.

Quitarles la caja del todo y dejarlos transparentes se probó y no funciona: un botón sin ningún fondo deja de leerse como botón. Lo que sí funciona es un sombreado suave y uniforme, del que ya existe un ejemplo en la aplicación, y que da superficie al botón sin devolverle el recuadro blanco.

El requisito «Estilo de zona de acciones en tema por defecto» fija hoy lo contrario para dos de estos botones: dice que Añadir línea y Eliminar línea del Editor conservan fondo blanco y texto negro por no formar parte de la barra de acciones. Ese criterio deja de valer cuando el resto de botones de texto de la aplicación pasan a llevar sombreado.

## What Changes

- Los botones que muestran solo texto SHALL mostrarse sobre un sombreado suave y uniforme, sin borde, en lugar de sobre fondo blanco con recuadro.
- Ese sombreado SHALL ser un gris neutro teñido levemente con el acento de cada tema, de modo que acompañe al tema sin competir con él.
- Todos estos botones SHALL mostrar su texto en negrita.
- El botón de acción principal de cada pantalla SHALL distinguirse por un sombreado más intenso y por el color de acento en su texto.
- Los botones de tabla del Editor (Añadir línea y Eliminar línea) SHALL dejar de mostrarse con fondo blanco y texto negro, y SHALL adoptar el mismo sombreado.
- Los dos botones de línea de la pantalla de generación mensual, que hoy no declaran ningún estilo y muestran el gris por defecto de la plataforma, SHALL adoptarlo también.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se añade el requisito de sombreado de los botones de solo texto y se corrige el requisito de estilo de zona de acciones, que fijaba el fondo blanco de los dos botones de tabla del Editor.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: nueva variante `btn-suave`.
- `src/main/resources/com/alcazaba/facturacion/themes/tema-*.css`: los 7, una pareja de variables de lavado en cada uno.
- `src/main/resources/com/alcazaba/facturacion/ui/`: `Clientes.fxml`, `Configuracion.fxml`, `Backup.fxml`, `Editor.fxml`, `GenerarFacturasMensuales.fxml`, `Versiones.fxml`.
- Ningún `.java`.
