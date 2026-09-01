## Why
Al abrir una factura nueva a 1024x768 el bloque de totales no se veia y habia que hacer scroll desde el primer momento, aunque la factura tuviera dos lineas. La ventana deja ~713 px utiles de contenido y el Editor pedia ~880: sobraban ~170 px y lo que caia fuera era el final del `bottom`, justo donde estaban los totales. Ademas todo el Editor vivia dentro de un `ScrollPane` general, asi que los totales scrolleaban con el resto y nunca quedaban fijos.

## What Changes
- El `ScrollPane` general desaparece: la raiz del Editor pasa a ser el `BorderPane`. La tabla de lineas es el unico elemento que crece y conserva su scroll interno.
- La cabecera se reorganiza en dos bloques lado a lado, FACTURA (serie, fecha, numero, forma de pago, vencimiento, realizada por y la referencia de rectificativa) y CLIENTE (cliente, nombre, NIF, direccion, email, CP, localidad, provincia), en lugar de una rejilla unica de seis filas a todo el ancho. La cabecera baja de ~340 px a ~250.
- Dentro del bloque de cliente las columnas son desiguales: cliente y direccion ocupan el ancho completo del bloque, y nombre y email la columna ancha, de modo que los campos con mas texto no quedan estrechos.
- El titulo de la pantalla y el distintivo de anulada se integran en la barra de acciones, que antes ocupaba una fila propia.
- Los totales pasan de tarjeta apilada de ~160 px a una columna compacta de 300 px anclada abajo a la derecha, fuera del scroll, con Observaciones a su izquierda ocupando el mismo alto.
- Clases nuevas en `base.css` acotadas al Editor (`card-editor`, `titulo-compacto`, `bloque-titulo`, `grid-editor`, `zona-editor`, `zona-editor-pie`, `totales-compacta`). No se tocan `.card` ni `.zona-contenido`, que usan el resto de pantallas.
- `EditorTamanoMinimoTest` comprueba ahora que el importe total y Observaciones caben dentro del alto de la ventana y que la tabla conserva al menos 200 px.

## Capabilities
### New Capabilities
- Ninguna.
### Modified Capabilities
- Tamanos de ventana por vista: se anade el requisito de que una factura corta quepa entera sin scroll y de que los totales permanezcan visibles.

## Impact
- src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml
- src/main/resources/com/alcazaba/facturacion/themes/base.css
- src/test/java/com/alcazaba/facturacion/ui/EditorTamanoMinimoTest.java
