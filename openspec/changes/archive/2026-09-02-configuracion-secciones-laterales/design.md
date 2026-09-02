## Componentes Afectados

- `Configuracion.fxml`: se sustituye el `TabPane` por barra lateral + pila de secciones.
- `ConfiguracionController`: metodo nuevo para atar la seccion seleccionada a la visibilidad de los paneles y del boton de guardar; cableado de la vista previa.
- `PdfService`: se le extrae la geometria de cabecera.
- `CabeceraLayout` (nuevo, paquete `pdf`): unica fuente de verdad de esa geometria.
- `PreviaCabecera` (nuevo, paquete `ui`): panel que dibuja la previsualizacion.
- `base.css`: clases nuevas acotadas a esta pantalla.

## Dato de partida importante

**El controlador actual no referencia el `TabPane` ni los `Tab`**: solo usa los `fx:id` de los controles concretos. Verificado con grep sobre `ConfiguracionController.java`. Por eso el layout se puede reorganizar libremente sin romper la logica existente de IVA, retenciones, series ni empresas; lo unico que se anade es el manejo de secciones.

## Estructura nueva

`BorderPane`:

- `top`: barra de navegacion + titulo "Configuracion".
- `center`: `HBox` con
  - `ListView fx:id="listaSecciones"` de 200 px, con las siete secciones y dos encabezados de grupo;
  - `StackPane fx:id="pilaSecciones"` con un `VBox` por seccion, todos con `visible=false` y `managed=false` salvo el activo.
- `bottom`: `HBox fx:id="barraGuardar"` con "Guardar configuracion" y "Volver".

Reparto de secciones:

| Seccion | Contenido | Boton inferior |
|---|---|---|
| Empresa | nombre, NIF, actividad, direccion, CP, localidad, provincia, email, telefono | Si |
| Cabecera y pie | modo texto/logo, ruta del logo, X/Y/ancho/alto, pie legal, vista previa | Si |
| PDF y apariencia | carpeta automatica, ultima carpeta, color del PDF, tema de la aplicacion | Si |
| IVA | tabla + alta rapida | No |
| Retenciones | tabla + alta rapida | No |
| Series | tabla + alta rapida + formato | No |
| Empresas | tabla + nueva/cambiar/eliminar | No |

En el controlador, un `configurarSecciones()` que escuche la seleccion de la lista, muestre el `VBox` correspondiente y haga `barraGuardar.setVisible(...)` / `setManaged(...)` segun la seccion pertenezca al primer grupo o al segundo.

**Se descarta `TabPane side="LEFT"`**, que evitaria el codigo: rota las etiquetas 90 grados y obliga a contrarrestarlo con CSS fragil.

## Vista previa de la cabecera

El riesgo real de una previsualizacion es que se separe de lo que imprime el PDF. Hoy las reglas viven en metodos privados de `PdfService` (alrededor de la linea 599):

- `anchoLogoEfectivo` = ancho configurado multiplicado por 2, con tope de 480 pt (defecto 120)
- `altoLogoEfectivo` = alto configurado multiplicado por 2, con tope de 170 pt (defecto 60)
- `offsetLogoX` / `offsetLogoY`: desplazamientos en pt
- `lineasEmpresa(empresa)`: lineas del modo texto, con el NIF marcado como destacado
- margen lateral 40 pt sobre A4 (595 pt de ancho)
- alto de cabecera en modo texto: `42 + lineas * 13 + 18`, con minimo de 108 pt

`CabeceraLayout` recoge exactamente esas reglas y `PdfService` pasa a delegar en ella, sin cambiar ningun valor: el PDF resultante debe ser identico al actual. `PreviaCabecera` la usa para dibujar.

`PreviaCabecera` es un `Pane` que pinta a escala (A4 completo de ancho, recortado a la banda superior): rectangulo de pagina en blanco con borde, guia de margenes, el logo real cargado desde `logoPath` colocado en su posicion y tamano efectivos, o el bloque de lineas de empresa con el NIF destacado, mas la banda de acento con el color configurado. Debe repintarse al cambiar cualquiera de los campos implicados y al elegir un logo nuevo.

**Es una aproximacion, no el PDF real**, y la pantalla lo dice con una linea discreta bajo la previsualizacion. Rasterizar el PDF de verdad exigiria una dependencia nueva: el proyecto usa OpenPDF, que no rasteriza.

Aviso util que hoy no existe en ninguna parte: junto a los campos de ancho y alto conviene indicar el tamano efectivo, porque el usuario escribe 120 x 60 y el PDF dibuja 240 x 120.

## Estilos

Clases nuevas acotadas: `lista-secciones`, `seccion-config`, `previa-cabecera`, `alta-rapida`. Colores tomados de las variables del tema para que las siete paletas sigan funcionando; la seccion activa de la lista usa el mismo tratamiento que `.menu-item:hover` del menu principal.

**Trampa a evitar:** en FXML el separador de `styleClass` es la COMA. `styleClass="card zona-contenido"` produce una sola clase inservible. Ese fallo ya se corrigio en todo el proyecto y `StyleClassSeparadorTest` lo vigila; al reescribir esta pantalla hay que seguir usando comas para no reintroducirlo.

## Alternativas consideradas

- Mantener las ocho pestanas y solo ensanchar el contenido: no resuelve la ambiguedad del guardado ni la heterogeneidad de las pestanas.
- Reducir a cuatro pestanas agrupadas: mejora, pero deja los nombres apretados y no escala si se anaden opciones.
- Un boton unico que guarde tambien la fila en edicion de IVA, retenciones y series: mas comodo, pero toca logica de negocio y puede guardar cosas a medias sin que el usuario lo pida.

## Testing

- `ConfiguracionLayoutTest`: carga el FXML, `applyCss()` + `resize(1024, 768)` + `layout()`, y para cada una de las siete secciones comprueba que su ultimo control cae dentro del alto de la escena y que la barra de guardado esta visible solo en las tres primeras. El separador de `styleClass` ya lo cubre `StyleClassSeparadorTest`.
- `CabeceraLayoutTest`: la regla del doble y los topes de 480 y 170; los valores por defecto de 120 y 60; el alto de cabecera en modo texto segun el numero de lineas y su minimo de 108.
- La suite completa debe seguir en verde.
- **Comprobar que los tests nuevos fallan con el codigo anterior** antes de darlos por buenos. Un test que pasa igual antes y despues no protege de nada.
