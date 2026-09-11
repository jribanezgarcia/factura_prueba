## Context

Todos los iconos son trazados Material sobre una rejilla de 24x24, pero cada dibujo ocupa un trozo distinto de esa rejilla. Un `SVGPath` no tiene viewBox: su tamaño de layout es el de su dibujo. Por eso, con el icono encima de la etiqueta, la etiqueta arranca donde termina cada dibujo.

El change de Clientes resolvió esto con una caja fija, `.caja-icono`, de 22x22 en `base.css`. Este change la extiende.

## Goals / Non-Goals

**Goals:** etiquetas alineadas en las barras del Editor y del Histórico; nombres y descripciones alineados en el menú principal.

**Non-Goals:** cambiar trazados, escalas o textos; tocar la barra de navegación; igualar el tamaño visual de los dibujos entre sí.

## Decisions

### D1. En el FXML, no en tiempo de ejecución

La prueba hecha en otro clon envolvía los iconos desde Java al mostrar cada pantalla. Aquí se hace en el FXML, igual que en Clientes: es lo que ya existe, se ve en el propio archivo y no añade código. Cada icono queda como:

```xml
<graphic><StackPane styleClass="caja-icono"><SVGPath styleClass="icono-boton" content="..."/></StackPane></graphic>
```

### D2. Barras del Editor y del Histórico: la misma caja de 22x22

Es la de Clientes. Las tres barras usan la misma escala, 1.05, y los mismos botones de 72. Reutilizarla deja las tres barras idénticas. La escala no cuenta para el layout, así que la caja solo tiene que cubrir el dibujo sin escalar, que no pasa de 21 en ningún icono.

### D3. Menú principal: caja propia de 28x28

En el menú el icono va a la izquierda del texto, no encima, y lleva escala 1.35. Lo que hay que igualar es el ancho. Se crea `.caja-icono-menu` con las seis propiedades de tamaño a `28` y `-fx-alignment: center`. 28 cubre el dibujo más ancho ya escalado (unos 27), de modo que ningún icono invade el hueco de 18 que lo separa del texto.

Va en `base.css`, justo después de `.menu-item .icono`.

### D4. Sin escalas por icono

La prueba del otro clon compensaba a mano algunos iconos con escalas propias. Aquí no: la norma iguala el espacio que ocupa cada icono, no el tamaño del dibujo, y el requisito prohíbe deformarlo. Si alguno se ve pequeño a ojo, será otro change.

### D5. Textos sin cambios

La prueba renombraba «Generar mensuales» a «Mensuales». Hoy el botón se llama «Facturar mes», que el spec ya fija y que cabe en una línea a 12px en un botón de 72. No se renombra nada.

## Risks / Trade-offs

- **Altura de las barras.** La caja de 22 puede ser uno o dos píxeles más alta que el icono suelto más bajo. La barra toma la altura del botón más alto, que ya era 22 o casi, así que no debería crecer. Se comprueba en 3.4.
- **Menú.** La tarjeta de opciones tiene ancho fijo de 430. Con la caja de 28 el texto arranca unos píxeles más a la derecha. Hay que comprobar que ninguna descripción se corta (3.5).
