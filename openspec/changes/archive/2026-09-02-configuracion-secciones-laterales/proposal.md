## Why

La pantalla de Configuracion es un `TabPane` de ocho pestanas heterogeneas y arrastra tres problemas concretos:

1. **Dos botones "Guardar" con significados distintos.** El boton inferior (`guardar()` en `ConfiguracionController`) persiste datos de empresa, cabecera, pie, carpeta de PDFs, color y tema. En cambio IVA, Retenciones, Series y Empresas se guardan fila a fila con sus propios botones. Un usuario que edita un tipo de IVA y pulsa "Guardar configuracion" pierde el cambio sin ningun aviso. Es una trampa de comportamiento, no un detalle estetico.
2. **El ancho se desperdicia.** Los formularios se apinan a la izquierda en columnas estrechas mientras la mitad derecha de la ventana queda vacia, y aun asi hay pestanas que van justas de alto.

Ademas, el logo de cabecera se configura a ciegas: el usuario ajusta posicion y tamano y tiene que exportar una factura para ver el resultado.

## What Changes

- Las ocho pestanas se sustituyen por **navegacion lateral**: una lista de secciones a la izquierda (~200 px) y el contenido a la derecha. Siete secciones agrupadas por naturaleza: Empresa, Cabecera y pie, PDF y apariencia (las tres que guarda el boton) e IVA, Retenciones, Series, Empresas (las que se administran por filas).
- El boton **"Guardar configuracion" solo aparece en las secciones que guarda**. En las cuatro de tabla se oculta con `visible`/`managed`, y queda unicamente el boton de la propia seccion.
- El **tema de la aplicacion se mueve** de la seccion Empresa a "PDF y apariencia": el spec establece que el tema se guarda de forma global y compartida entre empresas, asi que junto a los datos de la empresa esta fuera de sitio.
- Nueva **vista previa de la cabecera del PDF** en la seccion "Cabecera y pie": un panel que dibuja a escala la banda superior de un A4 con el logo en su posicion y tamano efectivos, o el bloque de texto con el NIF destacado, y el color de acento aplicado. Reacciona a los cambios de los campos.
- Para que la vista previa no mienta, la geometria de cabecera se **extrae de `PdfService` a una clase compartida** `CabeceraLayout` en el paquete `pdf`, usada por el PDF y por la previsualizacion.
- El FXML nuevo mantiene los `styleClass` con COMAS. El separador ya se corrigio en el change anterior y `StyleClassSeparadorTest` lo vigila, asi que reescribir esta pantalla no debe reintroducir espacios.
- Estilos nuevos acotados a esta pantalla en `base.css`. No se tocan `.card` ni `.zona-contenido`, que son globales y usan las demas pantallas.

## Capabilities

### New Capabilities
- Vista previa de la cabecera del PDF dentro de Configuracion.

### Modified Capabilities
- Configuracion: navegacion por secciones en lugar de pestanas, y alcance explicito del boton de guardado.

## Impact

- src/main/resources/com/alcazaba/facturacion/ui/Configuracion.fxml
- src/main/java/com/alcazaba/facturacion/ui/ConfiguracionController.java
- src/main/java/com/alcazaba/facturacion/pdf/PdfService.java
- src/main/java/com/alcazaba/facturacion/pdf/CabeceraLayout.java (nuevo)
- src/main/java/com/alcazaba/facturacion/ui/PreviaCabecera.java (nuevo)
- src/main/resources/com/alcazaba/facturacion/themes/base.css
- src/test/java/com/alcazaba/facturacion/ui/ConfiguracionLayoutTest.java (nuevo)
- src/test/java/com/alcazaba/facturacion/pdf/CabeceraLayoutTest.java (nuevo)
