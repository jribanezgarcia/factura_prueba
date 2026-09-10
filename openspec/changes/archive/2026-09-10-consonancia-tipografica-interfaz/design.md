## Context

Tres bloques de `base.css` fijan la tipografía en juego:

- `.nav-button`: `10px`, sin `-fx-font-weight`, `-fx-min-width` y `-fx-pref-width` a 90, `-fx-content-display: top`. **No** tiene `-fx-wrap-text`, así que una etiqueta que no quepa se recorta.
- `.btn-ribbon`: `10px`, sin `-fx-font-weight`, ancho clavado a 64 por `min`, `pref` y `max`, `-fx-min-height: 68`, y `-fx-wrap-text: true`.
- `.lista-secciones .list-cell`: `13px`, sin `-fx-font-weight`. Solo `:filled:selected` añade negrita.

La referencia nueva es `.btn-suave`, que aplica negrita sobre los 13px del bloque base de botones.

`.action-bar` la usa únicamente `Editor.fxml:18`. Su `-fx-spacing: 8px` no afecta a ninguna otra pantalla. El contenedor del logo de empresa dentro de esa barra (`Editor.fxml:19`) mide 110 de ancho fijo. La barra del Histórico es un `HBox` propio con su `spacing="8"` declarado en el FXML, en una fila sin nada más que compita por el ancho.

## Goals / Non-Goals

**Goals:**

- Que el texto de los elementos de navegación y acción pese al menos tanto como el de los botones secundarios.
- Que ninguna etiqueta de una sola palabra se parta.

**Non-Goals:**

- No se toca `.menu-item` ni sus sub-clases: el menú principal ya tiene su propia jerarquía.
- No se cambia ninguna etiqueta de botón para que quepa mejor.
- No se toca el espaciado de la barra del Histórico, que no tiene presión de espacio.

## Decisions

### D1. Decisión: el ancho de la navegación sube de 90 a 100

`.nav-button` no tiene envoltura de texto. Medido con Segoe UI, `Configuración` pasa de 66px a 10px en negrita a 80px a 12px, y no cabría en los 90 actuales.

Hay sitio: son 7 botones con 34 de separación. A 90 ocupan 834; a 100, 904. El mínimo de ventana es 1024.

### D2. Decisión: el ancho del ribbon sube de 64 a 86

Anchos reales de las etiquetas críticas, medidos con Segoe UI negrita:

| Etiqueta | 10px | 12px |
|---|---|---|
| Rectificativa | 57 | 69 |
| mensuales | 49 | 59 |
| Restaurar | 45 | 54 |

El ancho aprovechable de un botón es unos 15px menor que su ancho declarado: JavaFX reserva más de lo que sugiere el `-fx-padding: 6px 2px`. Con 64 de ancho quedan unos 49 útiles, que es justo lo que hace que `mensuales` se parta a 12px y `Rectificativa` a cualquier tamaño.

A 86 de ancho quedan unos 71 útiles, con margen sobre los 69 de `Rectificativa`. Es el ancho mínimo que cumple el objetivo.

### D3. Decisión: el espacio sale de la propia barra, no del título

Los ocho botones, el título de la factura y el distintivo de anulada comparten el ancho de 1024. Ensanchar los botones sin más se lo quita al título:

| Ancho botón | Ocupan los 8 | Queda para título más distintivo |
|---|---|---|
| 64 (hoy) | 512 | 210 |
| 86 | 688 | 34 |

Con 34 el título desaparece, y eso incumpliría el requisito que dice que conserva su texto completo mientras haya espacio.

Se recupera espacio de dos sitios de la misma barra:

- `-fx-spacing` de `.action-bar` de 8 a 4. Son catorce huecos: **56px**.
- Ancho del contenedor del logo de 110 a 80: **30px**.

Con esos 86 recuperados, al título le quedan 120 con el distintivo oculto, frente a los 130 de hoy. Con el distintivo visible se recorta con elipsis, que es lo que el requisito ya prevé para ese caso.

Alternativa descartada: acortar las etiquetas. Cambiar `Generar mensuales` por `Mensuales` o `Rectificativa` por `Rectificar` resolvería el ancho, pero toca el requisito de criterio de etiquetado y empobrece la interfaz para salvar un problema de maquetación.

### D4. Decisión: se corrige el requisito, no se deja el resultado al azar

El requisito dice hoy que una etiqueta que no quepa «SHALL envolverse a dos» líneas, y un escenario afirma que `Rectificativa` se ve en dos líneas. Ese «dos» describía lo que pasaba a 10px con el ancho viejo, no una intención.

Se reescribe para decir lo que importa: el botón no se ensancha, todos miden lo mismo, una palabra nunca se parte y una etiqueta de varias palabras se envuelve por sus espacios.

### D5. Decisión: las entradas de la lista de secciones van en negrita

`:filled:selected` pierde la negrita como señal distintiva, pero conserva el fondo de acento atenuado y el color de texto derivado del acento. Los encabezados de grupo no se tocan: ya van en negrita a 11px con opacidad 0.55.

## Riesgos

El ancho aprovechable de 15px menos que el declarado está deducido de dos observaciones, no medido dentro de JavaFX. Si resultara ser menor, `Rectificativa` seguiría partiéndose y habría que subir de 86 a 90, lo que dejaría al título en 88 en vez de 120.

Es la razón de que la tarea 4.2 obligue a mirarlo con la ventana en 1024x768 antes de cerrar.

## Verificación

- Abrir el Editor a 1024x768: los 8 botones visibles, todos del mismo ancho, `Rectificativa` en una línea y `Exportar PDF` en dos.
- Abrir el Histórico: `Generar mensuales` en dos líneas.
- Comprobar que el título de la factura sigue leyéndose y que solo se recorta al aparecer el distintivo de anulada.
- Comprobar que el logo de empresa a 80 de ancho sigue reconociéndose.
- Comprobar en la barra de navegación que ninguna etiqueta se recorta.
- Comprobar en Configuración que las entradas de la lista se leen en negrita y la seleccionada se distingue.
