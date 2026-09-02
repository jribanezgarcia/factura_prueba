## Context

See proposal.md - Why. Hoy el recuadro del logo (`StackPane.menu-logo-box`, 280x100 en el menú; suelto sin contenedor en el `ToolBar` del editor) se pinta con el color de fondo y borde que define cada `tema-*.css`. Un intento anterior de rellenar el hueco hubo que revertirlo por el "respaldo difuminado desbordado": el respaldo, siendo más grande que la caja, hacía crecer el `StackPane` (al contar en el cálculo de tamaño mínimo) y el clip, ligado al tamaño de la caja, crecía con él y dejaba ver el desenfoque fuera del recuadro.

## Goals / Non-Goals

**Goals:**
- Recuadro del logo relleno con los colores del propio logo en menú y editor, sin costura visible.
- Respaldo difuminado que nunca desborde el recuadro ni altere el layout (sobre todo en el `ToolBar` del editor a 1024x768).
- Una única fuente de verdad para la clasificación de imagen y la aplicación/limpieza del relleno, reutilizable por menú y editor.

**Non-Goals:**
- No tocar `base.css` ni `tema-*.css` (el radio y grosor de borde siguen viniendo de la CSS).
- No cambiar el escalado actual del logo (proporción conservada) ni recortarlo para que llene la caja.
- No cargar la imagen entera en memoria para analizarla (muestreo con paso).

## Decisions

- **Clasificador de imagen por el marco exterior (util nuevo, p. ej. `LogoMarco`)**. Se muestrean solo las bandas del marco (~6% de cada lado), descartando píxeles con alfa < 0,9. Si los opacos no llegan a ~60% de las muestras → caso 3 (transparente). Los opacos se agrupan en cubos de color (5 bits por canal); el cubo más frecuente, si alcanza ~60% de los opacos, → caso 1 con color = media exacta de ese cubo (para que un blanco puro salga `#FFFFFF`); si no → caso 2 (difuminado).
  - Motivo del muestreo solo del marco y con paso: un logo de texto oscuro sobre blanco da un promedio global gris sucio, y un logo de 3000x1500 no debe penalizar la carga.
- **Aplicación del relleno sobre el `StackPane` (menú y editor)**. Un método `aplicar(StackPane, Image)` que, según el caso:
  - Caso 1 (plano): `setStyle` inline que pisa solo `-fx-background-color` y `-fx-border-color` con el color detectado. No uso `Region.setBackground` porque la CSS vuelve a ganar en el siguiente pulso. El radio y grosor siguen viniendo de la CSS.
  - Caso 2 (difuminado): añade un `ImageView` de respaldo con la imagen ampliada y desenfocada, recortado a la forma del recuadro (clip con esquinas redondeadas, radio 10 px ligado al width/height de la caja), borde en transparente, como hijo en el índice 0 (`mouseTransparent`) con `setManaged(false)` para que no cuente en el tamaño del padre, y posicionado con `layoutX`/`layoutY` negativos (mitad del desborde) y `fitWidth`/`fitHeight` = tamaño de la caja + el desborde (~60 px).
  - Caso 3 (transparente): no hace nada.
  - El `StackPane` debe llevar tamaño fijo (min = pref = max) para que ningún hijo lo estire.
- **Limpieza obligatoria con las tres piezas** (estilo inline, clip y respaldo) al principio de `aplicar` y en las tres salidas tempranas de la carga de logo (ruta vacía, fichero inexistente, imagen con error). Evita que el respaldo del logo anterior quede debajo del nuevo y que el recuadro quede pintado al cambiar a una empresa sin logo.
- **El bug del respaldo desbordado se evita con `setManaged(false)` + tamaño fijo de la caja + layout manual del respaldo.** Ese desborde (~60 px) da margen al desenfoque para no chupar transparencia de los bordes y crear un halo.
- **Editor**: envolver el `ImageView` del logo en un `StackPane` `menu-logo-box` de tamaño fijo 110x40 dentro del `ToolBar`, y añadir `fitHeight` (además del `fitWidth=92` existente) para que la imagen quede contenida.
- **Manejo de errores**: el `try/catch` que envuelve `cargarEmpresa` en `MenuController` se traga excepciones; si un `fx:id` nuevo no se inyecta, el fallo es silencioso. Conviene no depender de él para validar el cambio y mirar la consola/log.

## Risks / Trade-offs

- [El desbordamiento no se ve en tests unitarios (pasaban 135 y el bug estaba ahí)] → Verificación visual del editor u otro test temporal que haga `snapshot()` de la barra a PNG y se borre después.
- [Cambiar a un logo con otro tipo acumula el respaldo antiguo] → Limpieza al principio de `aplicar` y en las salidas tempranas.
- [Si el caso 2 se complica] → Entregar casos 1 y 3 y dejar la foto con el color del tema; resultado válido y no rompe nada.
