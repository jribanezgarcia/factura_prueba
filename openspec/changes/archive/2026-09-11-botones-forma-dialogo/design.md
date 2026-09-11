## Context

La regla base de `base.css` (línea 125) da forma a todos los botones con clase de estilo:

```css
.primary-button, .default-button, .danger-button,
.action-button, .action-danger-button {
    -fx-background-radius: 8px;
    -fx-border-radius: 8px;
    -fx-border-width: 1;
    -fx-padding: 8px 10px;
    -fx-font-size: 13px;
    -fx-cursor: hand;
}
```

Los botones de los diálogos no llevan ninguna de esas clases y usan el estilo de la plataforma (Modena): radio de 3 px y relleno de `4px 8px`, comprobados en el `modena.css` de `javafx-controls-21.0.6`. Su tamaño de letra también es 13 px.

Hoy los botones de solo texto son los de Configuración, Copia de seguridad, Versiones, la generación de facturas mensuales y los dos de línea del Editor. Los de Clientes ya llevan icono.

## Goals / Non-Goals

**Goals:** misma forma que los botones de los diálogos; ancho común dentro de cada grupo.

**Non-Goals:** cambiar colores, sombreado, tamaño de letra, textos o reacciones; tocar los botones con icono, la navegación, el menú principal, el arranque o los diálogos.

## Decisions

### D1. Forma, en la regla base

En la regla de la línea 125:

- `-fx-background-radius` y `-fx-border-radius`: `8px` → `3px`.
- `-fx-padding`: `8px 10px` → `4px 8px`.
- Nueva `-fx-min-width: 80px`.

No hace falta excluir los botones con icono: `.btn-ribbon` fija su propio radio, relleno y ancho en una regla posterior de la misma especificidad, y por eso gana. Las reglas de `btn-suave` no fijan forma, así que heredan la nueva. Ningún tema toca forma ni relleno.

### D2. Ancho común por grupo, en Java

CSS no puede igualar el ancho de varios botones al del más ancho. Se crea `Botones.java` en `com.alcazaba.facturacion.ui`, con un método estático `igualarGrupos(Parent raiz)`:

- Un grupo son los `Button` que son hijos directos de un mismo contenedor (`HBox`, `FlowPane`…), siempre que haya al menos dos.
- Se excluyen los que tienen la clase `btn-ribbon`, `nav-button` o `menu-item`, y cualquiera que esté dentro de un `DialogPane`.
- A cada botón del grupo se le fija `prefWidth` con el mayor `prefWidth(-1)` del grupo, calculado después de aplicar el CSS.
- Si cambia el texto de algún botón del grupo, se vuelve a igualar, con un listener sobre `textProperty`. Ocurre de verdad: `GenerarFacturasMensualesController` cambia «Generar» por «Generar N facturas». Antes de medir, cada botón vuelve a `Region.USE_COMPUTED_SIZE`, porque si no `prefWidth(-1)` devuelve el ancho ya fijado y el botón no crece con su texto nuevo.

Se llama desde `Navegador.mostrar`, justo antes de las microinteracciones de la línea 69, con `root.applyCss()` delante para que los anchos preferidos ya tengan el relleno nuevo.

Los diálogos no pasan por `Navegador.mostrar`, así que no les llega. La exclusión de `DialogPane` es por si algún día se incrusta uno.

### D3. El test de Configuración ignora regiones con un padre oculto

Con D1 aplicado, `ConfiguracionLayoutTest` falla en la sección Series: una región acaba en 772, por encima del límite de 768. Se ha comprobado cuál es: el `StackPane` `track-background` de una `VirtualScrollBar` **oculta** dentro de la lista interna del desplegable Formato. El test solo mira `n.isVisible()` del propio nodo, no si algún padre está oculto, así que da por visible algo que el usuario no ve. Pasa en cuanto los botones encogen: la tabla de Series crece con el hueco y empuja el `FlowPane` unos píxeles hacia abajo.

Se corrige el test, no la maquetación. En el bucle de la línea 104 se descartan los nodos con algún antecesor que no sea visible, recorriendo `getParent()` hasta la raíz. El resto del test no cambia y sigue vigilando todo lo que el usuario ve.

### D4. El botón Generar deja de llevar el número

En la revisión visual, el botón de la generación mensual, que cambiaba su texto a «Generar N facturas», quedaba más ancho que Cancelar y siempre decía 12. Lo segundo es un defecto previo: `actualizarInfoBoton()` solo se llama al abrir la pantalla y al tocar las líneas, y ningún listener la llama al cambiar el mes de inicio o el de fin.

Decidido por el usuario: el botón dice siempre «Generar», como en el FXML, y el número va en la etiqueta `lblInfo`, que ya está en la barra inferior a la izquierda de los botones y hoy no se usa. Se añaden listeners a los dos combos de meses para que se actualice al momento.

Con el texto fijo, el grupo Cancelar | Generar mide siempre lo mismo. El re-igualado por cambio de texto de D2 se queda: no molesta y cubre cualquier otro botón que cambie de texto en el futuro.

## Risks / Trade-offs

- **Esquinas sobre el sombreado.** La prueba original se hizo antes de que existiera `btn-suave`, así que nadie ha visto aún las esquinas de 3 px sobre el sombreado. Se revisa en 3.2.
- **Botones con texto largo.** «Seleccionar copia a restaurar...» e «Inactivar/Activar» son los más anchos. En su grupo arrastran a los demás hasta su ancho. Es lo que hacen los diálogos y lo que se busca.
