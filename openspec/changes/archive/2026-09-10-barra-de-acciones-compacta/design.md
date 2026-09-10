## Context

Medidas reales con Segoe UI a 12px, que es el tamaño de las etiquetas de estos botones. La línea de texto mide 17px de alto.

| Etiqueta | Ancho | Cabe en 72 |
|---|---|---|
| Exportar | 44 | sí |
| Rectificar | 48 | sí |
| Versiones | 50 | sí |
| Restaurar | 50 | sí |
| Guardar | 43 | sí |
| Anular | 35 | sí |
| Volver | 33 | sí |
| Nueva | 34 | sí |
| Facturar mes | 67 | en dos líneas |

El ancho aprovechable de un botón es unos 15px menor que el declarado. Con 72 quedan unos 57 útiles, suficiente para todas salvo `Facturar mes`, que se parte por su espacio.

`.btn-ribbon` declara hoy `-fx-min-width`, `-fx-pref-width` y `-fx-max-width` a 86, y `-fx-min-height: 78`. `.action-bar`, que solo usa `Editor.fxml`, declara `-fx-spacing: 4px`. El contenedor del logo en `Editor.fxml` mide 80 de ancho.

## Goals / Non-Goals

**Goals:**

- Que la barra ocupe lo que necesita y no más, a lo ancho y a lo alto.
- Que el título de la factura se lea entero.

**Non-Goals:**

- No se cambia el tamaño de los iconos ni el de las etiquetas.
- No se toca el peso ni el color del texto: eso va en otro change.
- No se toca la barra de navegación superior.
- No se renombra ninguna acción del dominio ni ningún método: solo el texto visible de tres botones.

## Decisions

### D1. Decisión: primero las etiquetas, después las medidas

El orden importa para entender el change. Estrechar los botones sin acortar las etiquetas parte `Rectificativa` en dos líneas; acortar sin estrechar deja los botones sobrados. Son un solo movimiento y por eso van juntos.

`Exportar` en lugar de `Exportar PDF`: el icono es una hoja con las letras PDF, así que el formato ya está dicho. `Rectificar` en lugar de `Rectificativa`: nombra la acción, como pide el requisito de etiquetado. `Facturar mes` en lugar de `Generar mensuales`: más corto y más concreto sobre qué se genera.

### D2. Decisión: el ancho baja a 72 y sigue clavado

Los tres anchos —mínimo, preferido y máximo— siguen valiendo lo mismo, porque el requisito exige que todos los botones de una barra midan igual y que la etiqueta no ensanche el botón.

Presupuesto a 1024, con 944 útiles dentro de la barra: ocho botones de 72 son 576, más tres separadores de 5, más doce huecos de 8, más el logo de 110, suman 797. Al título le quedan 147, y envuelto en dos líneas necesita 131.

### D3. Decisión: fuera el alto mínimo

`-fx-min-height: 78` se puso cuando las etiquetas largas necesitaban dos líneas. Con las etiquetas cortas, el contenido del botón del Editor pide: 12 de relleno más 25 de icono más 4 de hueco más 17 de una línea, o sea 58. Se retira la declaración y el botón mide lo que pide.

La barra del Histórico, con `Facturar mes` en dos líneas, medirá 75. Cada barra queda coherente consigo misma aunque no coincidan entre ellas, que es lo que exige el requisito: los botones de **una misma** barra miden lo mismo.

### D4. Decisión: el título se envuelve, no se recorta

`Factura A-2026/123 (v2)` mide 194 a 17px, y el máximo del título son 130: hoy se recorta siempre que la factura tiene número real. Envuelto se parte en `Factura` (58), `A-2026/123` (95) y `(v2)` (32), y cabe.

Y no cuesta alto: la barra mide 58 porque lo marcan los botones, mientras que tres líneas de título son 51. El espacio vertical ya estaba pagado.

Alternativa descartada: acortar el título quitándole la palabra «Factura». Ahorra 58px pero empobrece el rótulo, y con la envoltura no hace falta.

## Riesgos

El ancho aprovechable de 15px menos que el declarado está deducido de lo observado en pantalla, no medido dentro de JavaFX. `Facturar mes` es el caso límite: si el margen fuera menor, `Facturar` podría partirse por dentro. La tarea 4.3 lo comprueba y dice qué hacer si ocurre.

## Verificación

- Abrir el Editor a 1024x768: los ocho botones en una fila, ninguna etiqueta partida, el título entero aunque sea en varias líneas, y un hueco visible entre el identificador de empresa y el título.
- Comprobar que la barra es visiblemente menos alta que antes y que la tabla de líneas ha ganado ese espacio.
- Abrir el Histórico y comprobar `Facturar mes` en dos líneas, partido por el espacio.
- Comprobar que el icono de `Exportar` sigue siendo el de PDF y el de `Rectificar` el de siempre.
