## Context

`Clientes.fxml:16-24` es un único `HBox` que contiene, en la misma fila, el campo de búsqueda, la etiqueta de conteo, un `Region` que empuja, y los cuatro botones con `styleClass="<tipo>-button, btn-suave"`.

El Histórico está montado distinto: los filtros viven en un `GridPane` y los botones ocupan un `HBox spacing="8" alignment="CENTER_RIGHT"` **propio**, con dos separadores verticales.

Los iconos del proyecto son `SVGPath` con trazados de Material Icons, coloreados por CSS a través de `.icono-boton`.

Inventario de iconos ya asignados, que ninguna propuesta puede repetir (el requisito «Botones de acción con icono identificativo»): lápiz = «Nueva factura» (menú y barra de navegación), persona = «Clientes» (menú y barra de navegación), papelera = «Eliminar», flecha izquierda = «Volver», y los del Editor y el Histórico.

## Goals / Non-Goals

**Goals:**

- Que Clientes se lea como el Editor y el Histórico, que son sus pantallas hermanas.

**Non-Goals:**

- No se fija una regla general sobre qué pantallas llevan iconos. Configuración, Copias, Versiones y Generar mensuales se quedan con botones de solo texto, y el requisito seguirá enumerando pantallas en vez de describiendo un criterio. Decisión explícita del usuario.
- No se reasigna el lápiz. Ver D3.
- No se cambia ninguna etiqueta ni ninguna acción.

## Decisions

### D1. Decisión: `Eliminar` y `Volver` se copian del Histórico

No es preferencia, es obligación: el requisito «Botones de acción con icono identificativo» dice que una misma acción lleva el mismo icono en todas las pantallas. Los trazados se copian literalmente de `Historico.fxml`:

- `Eliminar`: `M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z`
- `Volver`: `M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z`

### D2. Decisión: `Nuevo` usa persona con más

Es el icono estándar de alta de una persona. Comparte familia visual con el icono de «Clientes» de la barra de navegación, lo cual ayuda —ambos hablan de clientes— pero es un trazado distinto, así que no incumple la regla de un icono por acción.

Trazado (Material `person_add`):

`M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z`

Escrito de memoria de la librería Material y validado solo visualmente. La tarea 1.2 obliga a contrastarlo con la fuente original.

### D3. Decisión: `Editar` es un icono compuesto, porque el lápiz está ocupado

El icono canónico de editar es el lápiz, y en este proyecto el lápiz significa «Nueva factura»: aparece en `MenuPrincipal.fxml` y en `BarraNavegacion.java:25`. Como dos acciones distintas no pueden compartir icono, `Editar` no puede llevarlo tal cual.

Se compone uno nuevo: la silueta de persona que ya usa Clientes, reducida, con el lápiz encajado en la esquina inferior derecha. Se lee como «cliente» más «editar» y reutiliza una forma que el usuario ya asocia a esta pantalla.

Receta exacta, para que sea reproducible:

| Pieza | Origen | Escala | Desplazamiento |
|---|---|---|---|
| Persona | el trazado de «Clientes» tal cual | 0.80 | (-1.4, -1.0) |
| Lápiz | el trazado de «Nueva factura» tal cual | 0.48 | (13.2, 12.0) |

Resultado, que es lo que va al FXML:

`M 8.2 8.6 c 1.77 0 3.2 -1.43 3.2 -3.2 s -1.43 -3.2 -3.2 -3.2 -3.2 1.43 -3.2 3.2 1.43 3.2 3.2 3.2 Z m 0 1.6 c -2.14 0 -6.4 1.07 -6.4 3.2 v 1.6 h 12.8 v -1.6 c 0 -2.13 -4.26 -3.2 -6.4 -3.2 Z M 14.64 20.28 V 22.08 h 1.8 L 21.75 16.77 l -1.8 -1.8 L 14.64 20.28 Z M 23.14 15.38 c 0.19 -0.19 0.19 -0.49 0 -0.68 l -1.12 -1.12 c -0.19 -0.19 -0.49 -0.19 -0.68 0 l -0.88 0.88 1.8 1.8 0.88 -0.88 Z`

Ninguna curva original se ha modificado: solo se han transformado coordenadas. Es el primer icono del proyecto que no sale tal cual de Material, de ahí que la receta quede escrita aquí.

Alternativa descartada, y merece la pena anotarla: devolver el lápiz a su significado natural cambiando «Nueva factura» a un documento con más —que es justo el icono que ya usa el botón «Nueva» dentro del Editor—. Sería lo correcto, pero toca `MenuPrincipal.fxml` y `BarraNavegacion.java`, o sea otra pantalla y código Java. Si algún día se hace, este icono compuesto puede sustituirse por el lápiz a secas.

### D4. Decisión: buscador y botones en la misma fila

Buscador, conteo y botones comparten una sola fila, como estaban antes de este change: a la izquierda el campo de búsqueda y el conteo, un `Region` que empuja, y a la derecha los botones. Es decisión expresa del usuario tras ver la versión en dos filas.

Un botón con icono mide unos 58 de alto, frente a los unos 30 del campo de búsqueda, así que la fila crece a 58. El `HBox` conserva `alignment="CENTER_LEFT"`, que centra en vertical el campo y el conteo dentro de esa altura, de modo que no quedan pegados arriba.

Alternativa descartada: botones en fila propia bajo el buscador, como en el Histórico. Se aplicó y se retiró: separa dos cosas que en esta pantalla van juntas y cuesta una fila de alto a la tabla.

### D5. Decisión: tres grupos separados

El mismo requisito exige agrupar por afinidad con separadores verticales. Siguiendo el criterio del Editor: `Nuevo` y `Editar` juntas por ser de escritura, `Eliminar` sola por destructiva, `Volver` sola por navegación. Dos separadores, como en el Histórico.

### D6. Decisión: cada icono en una caja fija de 22×22

Medidas de los cuatro dibujos sobre la rejilla de 24×24 de Material:

| Icono | Ancho | Alto |
|---|---|---|
| Nuevo | 22.0 | 16.0 |
| Editar | 21.5 | 19.9 |
| Eliminar | 14.0 | 18.0 |
| Volver | 16.0 | 16.0 |

Un `SVGPath` de JavaFX no conserva la rejilla de 24×24: mide lo que ocupa su dibujo. Como en estos botones el icono va encima de la etiqueta, cada etiqueta arranca donde termina su icono. Con altos de 16 a 19.9, la de Editar quedaría unos 4 px más abajo que las de Nuevo y Volver.

Se envuelve cada `SVGPath` en un `StackPane` con una style-class nueva, `caja-icono`, que fija ancho y alto mínimo, preferido y máximo a 22. El `StackPane` centra el dibujo y todas las etiquetas arrancan a la misma altura. Con la escala de 1.05 de `.btn-ribbon .icono-boton`, el dibujo mayor, el de Editar, ocupa 20.9, así que cabe. Y 22 es el mismo tamaño que usó la prueba de iconos normalizados, de modo que el change que extienda la norma a las demás barras pueda reutilizar la clase sin tocarla.

El tamaño se declara en `base.css` y no en cada FXML, para que haya un solo sitio donde cambiarlo. Los selectores de color del icono son de descendiente, como `.default-button .icono-boton`, así que siguen aplicando con la caja de por medio.

No se aplican escalas por icono para igualar el tamaño aparente. Los iconos de Material están diseñados para verse equilibrados entre sí aunque sus dibujos midan distinto, y la caja respeta esa proporción. Si al verlos alguno pareciera desproporcionado, la corrección por escala se decidirá en el change que extienda la norma, con todas las barras medidas a la vez.

La norma se escribe como requisito propio con alcance solo en Clientes. Meterla en el requisito de iconos, que alcanza también al Editor y al Histórico, obligaría a declarar algo que esas dos barras todavía no cumplen. El change de iconos normalizados ampliará su alcance.

### D7. Decisión: Eliminar va en el color del tema

El `Eliminar` de Clientes era `danger-button`, en rojo. El mismo `Eliminar` del Histórico es `default-button` y va en color de acento, igual que los dos `Eliminar` de Configuración. Clientes era la excepción: la misma acción con dos colores según la pantalla.

Se cambia a `default-button`, de modo que los cuatro botones de la barra se presentan igual. El carácter destructivo lo comunica el icono de la papelera, no el color. Decisión expresa del usuario.

La misma decisión alcanza al `Anular` del Editor, el otro único botón rojo de la aplicación: pasa de `action-danger-button` a `action-button`, y se ve igual que el `Anular` del Histórico, que ya iba en el color del tema. Con eso ningún botón de barra queda en rojo.

El rojo del `Anular` se había exigido para que un `Anular` habilitado no se confundiera con un botón deshabilitado. Esa garantía no depende del rojo: los botones deshabilitados se atenúan, y un `Anular` a plena intensidad en el color del tema se distingue igual. El requisito se reescribe en esos términos.

Al corregir el spec aparece un error anterior: un escenario afirmaba que el `Eliminar` de Configuración «sigue siendo rojo», y nunca lo fue, porque es `default-button`. Queda corregido.

Las clases `danger-button` y `action-danger-button` se quedan sin ningún uso en los FXML. No se retiran de `base.css` ni de los temas en este change, para no mezclar una limpieza de CSS con un cambio de pantalla. Queda anotado aparte.

## Riesgos

La fila de búsqueda crece de unos 30 a unos 58 de alto por los botones con icono. Hay que comprobar que la tabla de clientes sigue teniendo altura suficiente con la ventana en 1024x768.

## Verificación

- Comparar Clientes con el Histórico lado a lado: los botones deben verse idénticos en tamaño, tratamiento y comportamiento.
- Comprobar que `Eliminar` y `Volver` muestran exactamente el mismo icono en las dos pantallas.
- Comprobar que las cuatro etiquetas de Clientes arrancan exactamente a la misma altura.
- Comprobar el icono compuesto de `Editar` a tamaño real: la persona debe reconocerse y el lápiz distinguirse.
- Comprobar el color por tema: los cuatro botones con icono y etiqueta en color de acento y sin negrita, `Eliminar` incluido. Ninguno se destaca sobre los demás.
