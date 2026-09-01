## Componentes Afectados
- `Editor.fxml`: reestructuracion completa del layout.
- `base.css`: clases nuevas acotadas al Editor.
- `EditorTamanoMinimoTest`: comprobaciones de layout real.

## Logica
- El presupuesto vertical a 1024x768 es de ~713 px utiles. El reparto nuevo es: navegacion 44, cabecera ~250, acciones de linea 44, tabla ~247 y franja inferior 116. La tabla es la unica zona con `VBox.vgrow="ALWAYS"`, asi que absorbe el sobrante al maximizar y se encoge hasta su minimo de 120 px antes de que nada quede fuera.
- La cabecera baja de seis filas a todo el ancho a dos bloques de tres y cinco filas: se aprovecha el ancho, que antes se desperdiciaba, para gastar menos alto. La referencia de rectificativa se queda en fila propia dentro del bloque de factura, porque el controlador la oculta con `managed=false` y la fila solo colapsa si no la comparte con otro campo.
- Dentro del bloque de cliente las dos columnas de campos no miden lo mismo: la ancha (`prefWidth` 260) recoge nombre y email, y cliente y direccion ocupan las tres columnas. GridPane reparte el sobrante a partes iguales entre columnas `hgrow`, asi que la diferencia de `prefWidth` se mantiene al ensanchar la ventana.
- Los totales dejan de ir dentro del scroll. Al colocarlos al lado de Observaciones en vez de apilados, la franja inferior mide lo mismo que median Observaciones y una barra horizontal, y el desglose queda agrupado en columna.
- Los ajustes de espaciado van en clases nuevas y no en `.card` ni `.zona-contenido`, que son globales y estan afinadas para Historico, Clientes, Configuracion, Versiones y Backup.
- Los totales reutilizan `.totales`, `.total-fila` y `.total-grande` para no tener que tocar los siete temas de color; `totales-compacta` solo recorta sus paddings y redondea los extremos.

## Alternativas consideradas
- Plegar los datos del cliente en un panel desplegable: ahorraba mas alto, pero obliga a desplegar para ver a quien se factura.
- Mantener la tarjeta de totales a la derecha dentro del scroll: no resuelve el problema, que es que los totales se pierden de vista.
- Barra horizontal de totales al pie: cabe igual, pero el desglose queda repartido en una linea en vez de agrupado.

## Testing
- `EditorTamanoMinimoTest`: layout real a 1024x768 y comprobacion de que `#lblTotal` y `#txtObservaciones` terminan dentro del alto de la escena y de que la tabla mide al menos 200 px. Con el `Editor.fxml` anterior falla: el total termina en 737 y el alto util es 729.
- `mvn test` (108 tests).
- Comprobacion visual: factura nueva sin scroll, factura larga con scroll solo en la tabla, rectificativa con la fila de referencia y ventana maximizada.
