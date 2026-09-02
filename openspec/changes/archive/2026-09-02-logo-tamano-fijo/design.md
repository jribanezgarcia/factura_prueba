## Componentes Afectados

- `CabeceraLayout`: pierde el x2, los topes y los offsets; expone el tamano fijo.
- `PdfService`: deja de aplicar offsets al colocar el logo.
- `PreviaCabecera`: deja de dibujar el logo desplazado; lo pinta en la caja fija.
- `Configuracion.fxml` y `ConfiguracionController`: fuera las cuatro filas y `lblTamanoEfectivo`.
- `CabeceraLayoutTest`: reescrito.

## ORDEN DE TRABAJO

Este change **borra parte del trabajo del change `configuracion-secciones-laterales`**: las filas de X/Y/ancho/alto, la etiqueta `lblTamanoEfectivo` y los topes que comprueba `CabeceraLayoutTest`. Ese change **ya esta commiteado y archivado** (`c75c55f`), asi que la via esta libre. Se deja escrito porque explica por que este change borra codigo recien anadido: no es un descuido, es la secuencia prevista.

## Logica

El tamano fijo es `240 x 120 pt`, que es el resultado de la configuracion por defecto de hoy (`120 x 60` duplicado). Se elige ese valor precisamente para que **los PDFs de quien nunca toco esos campos salgan identicos byte a byte**.

El dibujado ya usa `logo.scaleToFit(ancho, alto)`, que encaja la imagen dentro de la caja respetando su proporcion. Eso se mantiene: un logo apaisado y uno cuadrado caben los dos, cada uno con su forma, sin deformarse.

Al desaparecer los offsets, la posicion del logo pasa a ser el margen izquierdo, y el calculo del ancho de la columna de datos de empresa deja de depender de un valor que el usuario puede disparar. Con 240 pt de logo sobre 345 pt disponibles quedan 105 pt de aire antes del bloque FACTURA.

En `PreviaCabecera` hay que quitar tambien el uso de `offsetLogoX`/`offsetLogoY` al posicionar la imagen; si no, la previsualizacion mostraria algo que el PDF ya no hace.

## Alternativas consideradas

- **Poner topes correctos manteniendo los cuatro campos**: no se pierde funcionalidad, pero deja viva la parte que descuadra la cabecera y obliga a calcular el maximo en funcion del ancho del bloque FACTURA, que es justo la complejidad que no compensa.
- **Fijar el tamano pero mantener la posicion**: los offsets son la mitad del problema, y sin ancho configurable la posicion aporta muy poco.
- **Migrar las columnas de la base de datos**: innecesario. Dejar de leerlas no molesta a nadie y evita una migracion con riesgo.

## Testing

- `CabeceraLayoutTest` reescrito: el tamano efectivo es siempre 240 x 120 con independencia de lo que traiga la empresa, incluidos valores absurdos como 4000, y de que los campos vengan a null.
- **Comprobar que el test nuevo falla con el codigo anterior**: con el `CabeceraLayout` de hoy, pedir 240 x 120 para una empresa con `logoAncho = 400` devuelve 480, asi que debe fallar. Un test que pasa igual antes y despues no protege de nada.
- Verificacion manual: exportar un PDF con logo apaisado y otro cuadrado, comprobando que ninguno pisa el bloque FACTURA ni comprime los datos de empresa.
- **Comparar un PDF generado antes y despues con la configuracion por defecto: deben salir identicos.**
