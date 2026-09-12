## Context

Los botones `btn-ribbon` miden 72px fijos de ancho —`min`, `pref` y `max`— y la barra les da el alto que piden los demás, 55px. Con `-fx-wrap-text: true`, una etiqueta de dos palabras se parte por su espacio y pide 70px de alto; como no los tiene, JavaFX la recorta con elipsis.

Ya hay un precedente en la aplicación: la barra de navegación muestra una etiqueta breve y deja el nombre completo del destino en el tooltip. El spec lo recoge en «Menú y navegación».

## Goals / Non-Goals

**Goals:** que la etiqueta del botón se lea entera sin tocar medidas ni alturas.

**Non-Goals:** cambiar el ancho de los botones, el alto de la barra, el tamaño de los iconos o el nombre de la opción en el menú principal.

## Decisions

### D1. «Mensual», no «Gen. mensual»

El usuario pidió «Gen. mensual» si cabía y «Mensual» si no. Medido, no cabe: 71,5px de texto para 68 útiles, y además lleva un espacio, de modo que se partiría en dos líneas y volveríamos al recorte. «Gen.mensual» sin espacio tampoco entra, por 0,2px, y al no tener por dónde partirse se recortaría en seco a media palabra. «Mensual» mide 44,7 y entra con holgura.

Se descarta ensanchar los botones. Daría sitio para «Facturar mes» en una línea, pero mueve la geometría de las tres barras justo cuando se acaba de unificar, y el Editor, con ocho botones, es el que va justo de ancho.

Se descarta también subir el alto de la barra para admitir dos líneas: son 15px menos de alto útil en las tres pantallas, y el Editor tiene que caber sin scroll a 1024×768.

### D2. El tooltip lleva el nombre completo

«Mensual» solo no dice qué hace. El tooltip, «Generar facturas mensuales», lo aclara al posar el puntero, que es exactamente lo que ya hace cada botón de la barra de navegación con su destino.

Se escribe en el FXML, como un hijo `<tooltip><Tooltip text="..."/></tooltip>` del botón, sin tocar el controlador.

### D3. El menú principal conserva «Facturar mes»

En el menú la opción va en una tarjeta ancha con su descripción debajo: cabe de sobra y un nombre descriptivo ayuda más que uno corto. El requisito que exige que ambos se llamen igual se ajusta para admitir que la barra de iconos use una forma breve **con el nombre completo en el tooltip**, que es el criterio que ya rige en la barra de navegación. No se abre la puerta a nombres distintos: se admite abreviar, no rebautizar.

### D4. El escenario de las dos líneas desaparece

«Barra de acciones del editor sin desbordamiento» tiene un escenario que exige ver «Facturar mes» partido en dos líneas por su espacio. Es justo lo que ya no va a pasar. La regla de fondo —que una etiqueta de varias palabras se envuelva en vez de ensanchar el botón o recortarse— sigue siendo válida y se mantiene en el texto del requisito; lo que se reescribe es el escenario, que pasa a comprobar que la etiqueta del botón mensual se lee entera en una línea.
