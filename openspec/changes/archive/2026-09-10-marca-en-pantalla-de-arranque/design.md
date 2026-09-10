## Context

`Arranque.fxml` es un `BorderPane` de 760x520 cuyo `center` es un `VBox alignment="CENTER" spacing="24"` con tres hijos: el rótulo de la línea 14, la tarjeta de selección de 440 de ancho, y la etiqueta de error.

El icono de la aplicación ya existe como recurso, en `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png`. Es el único PNG dentro de `src/` y lo carga `Ventanas.java:15` para ponerlo en la barra de título de cada ventana. La marca como texto también existe ya, en `Ventanas.java:13`: `PREFIJO = "CaboFactu® "`.

La pantalla no tiene hoy ningún `ImageView` ni el import de `javafx.scene.image`.

## Goals / Non-Goals

**Goals:**

- Que la portada diga cómo se llama el programa y lo acompañe con su icono.

**Non-Goals:**

- No se cambia ningún control ni el flujo de la pantalla.
- No se crea ningún recurso gráfico nuevo.
- No se toca el logo de la empresa, que es otra cosa y aparece en el menú principal.
- No se toca el rótulo `empresa-nombre` de otras pantallas: la clase se comparte, pero aquí solo cambia el texto de esta instancia.

## Decisions

### D1. Decisión: el tamaño de la pantalla es un presupuesto cerrado

`VentanaConfig.java:13` declara `ARRANQUE` con ancho, alto, mínimo y máximo iguales a 760x520 y `redimensionable = false`, y el spec lo exige (`spec.md:983`). No hay ventana que crecer si el contenido no cabe.

Por eso el icono va **a la izquierda del texto y no encima**: en la misma línea no consume alto. Con `fitHeight="40"` y `preserveRatio="true"`, la fila de marca mide lo mismo que medía el rótulo solo, o muy poco más, y no empuja a la tarjeta.

### D2. Decisión: se reutiliza el icono de ventana, no se crea uno nuevo

`icono-aplicacion.png` es el icono con el que el usuario ya identifica el programa en la barra de tareas. Usar otro distinto en la portada sería introducir una segunda marca visual sin motivo.

Se carga desde el FXML con una ruta relativa al propio archivo, `@../images/icono-aplicacion.png`, que es la forma habitual en FXML y no requiere tocar el controlador.

### D3. Decisión: el texto se escribe literal en el FXML

`Ventanas.PREFIJO` contiene «CaboFactu® » con un espacio final, pensado para concatenar títulos de ventana. Aquí se necesita la marca a secas, sin espacio, y en un FXML. Referenciar la constante desde el FXML complicaría el archivo para ahorrar una cadena de once caracteres.

Queda como duplicación consciente: si algún día cambia la marca, hay dos sitios. Se anota aquí para que se encuentre.

## Riesgos

El rótulo usa `styleClass="empresa-nombre"`, una clase compartida con el menú principal, donde muestra el nombre de la empresa activa. No se toca la clase, solo el texto de esta instancia, así que el menú no se ve afectado. Conviene confirmarlo de un vistazo al verificar.

## Verificación

- Abrir la aplicación: la portada muestra el icono y «CaboFactu®» centrados, sin que la tarjeta de selección se desplace ni se corte nada dentro de los 760x520.
- Comprobar que el icono se ve nítido a 40px de alto y con su proporción original.
- Comprobar que el menú principal sigue mostrando el nombre de la empresa con el mismo aspecto de siempre.
- `mvn test` en verde. `UiSmokeTest` y `VentanaTransicionTest` cargan el arranque; un import olvidado o una ruta de recurso mal escrita harían fallar la carga del FXML.
