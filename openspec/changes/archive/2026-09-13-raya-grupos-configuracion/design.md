## Context

La lista lateral de Configuración se llena en `ConfiguracionController.configurarSecciones()` (líneas 368-408). Los nueve elementos son de dos tipos: siete secciones navegables y dos rótulos de grupo. La fábrica de celdas (380-401) los distingue por el campo `grupo`: para un grupo crea un `Label`, le pone la clase `grupo-secciones`, lo cuelga como `graphic` de la celda y deshabilita la celda para que no pueda seleccionarse.

El estilo vive en `base.css:471-475`, con el selector descendente `.lista-secciones .list-cell .grupo-secciones`: tamaño 12 px, opacidad 0,70 y un padding de `12px 4px 2px 4px`.

La línea 384 ya hace `getStyleClass().remove("grupo-secciones")` sobre la **celda**, no sobre el `Label`. Hoy no tiene efecto, porque la clase nunca llega a la celda.

## Goals / Non-Goals

**Goals:** que los dos rótulos de grupo se lean como cabeceras de bloque y no como una entrada más.

**Non-Goals:** añadir títulos a los paneles de sección; cambiar los grupos, su texto o su orden; tocar la selección, el hover o los colores de las entradas navegables; tocar ningún tema.

## Decisions

### D1. La clase pasa del rótulo a la celda

Un `Label` se dimensiona a su texto, así que una raya puesta sobre él llegaría solo hasta donde llega la palabra: acabaría bajo «CATÁLOGOS» y dejaría el resto de la fila sin rayar. La celda, en cambio, ocupa todo el ancho de la lista.

La rama de grupo deja de crear el `Label` y pinta el texto en la propia celda, con la clase puesta en la celda:

```java
} else if (item.grupo) {
    setText(item.texto);
    setGraphic(null);
    getStyleClass().add("grupo-secciones");
    setDisable(true);
}
```

El `remove` de la línea 384 pasa así a cumplir su función: limpiar la clase cuando la celda se recicla para una sección navegable. Sin él, al desplazar la lista una sección heredaría la raya del grupo.

### D2. Raya con borde asimétrico, como el resto de la aplicación

El selector deja de ser descendente y pasa a ser de la propia celda, con la raya como borde inferior:

```css
.lista-secciones .list-cell.grupo-secciones {
    -fx-font-size: 12px;
    -fx-padding: 12px 4px 4px 4px;
    -fx-opacity: 0.70;
    -fx-border-color: transparent transparent -fx-text-background-color transparent;
    -fx-border-width: 0 0 1 0;
}
```

Es el idioma que ya usa la casa para las rayas: borde de un solo lado con los otros tres transparentes, igual que `.nav-button.activo` (`base.css:110`) y la cabecera de los diálogos (`base.css:419`). El color sale de `-fx-text-background-color`, que definen los siete temas, así que la raya se adapta sola a cada uno y ningún `tema-*.css` se toca. La opacidad de 0,70 de la celda alcanza también a la raya, que queda suave en lugar de marcada.

El padding inferior sube de 2 a 4 px para que el texto no quede pegado a la raya. Son dos píxeles por grupo, en la lista lateral, que no es el elemento crítico de alto de la pantalla.

### D3. Los paneles de sección se quedan sin título

Los siete `VBox` de `pilaSecciones` no llevan título dentro y no se les añade. El escenario «Cada sección cabe sin scroll» exige que el contenido de cada sección se vea completo a 1024×768, y un título con su raya y su separación consumiría alto justo en la pantalla que ya va más justa. El nombre de la sección está siempre visible en la lista lateral, resaltado como entrada seleccionada.

## Risks / Trade-offs

- **Riesgo bajo:** el cambio es un rótulo que pasa de nodo hijo a texto de la celda y una regla de CSS.
- **Descuido posible:** olvidar el `setGraphic(null)` dejaría el `Label` viejo colgando junto al texto nuevo y saldría el rótulo duplicado al reciclarse la celda. Lo cubre la comprobación de la tarea 3.2, recorriendo la lista arriba y abajo.
- **Descuido posible:** poner la raya sobre el `Label` en lugar de sobre la celda da una raya corta, del ancho del texto. Lo cubre la tarea 3.1.
