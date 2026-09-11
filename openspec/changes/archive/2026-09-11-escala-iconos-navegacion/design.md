## Context

`BarraNavegacion.icono(contenido, offsetX)` crea el `SVGPath`, lo mete en un `StackPane` de 26x26 y desplaza la caja entera con `setTranslateX(offsetX)`. Solo Histórico (`-1.3`) y Salir (`2.8`) usan un desplazamiento distinto de cero. La escala de todos sale de `base.css`:

```css
.nav-button .nav-icon {
    -fx-scale-x: 1.25;
    -fx-scale-y: 1.25;
}
```

Los temas solo cambian el relleno de `.nav-icon`, ninguno su escala.

## Goals / Non-Goals

**Goals:** iconos centrados en su caja, sin desplazamientos; tamaño visual parecido entre ellos.

**Non-Goals:** cambiar trazados, la caja de 26x26, la separación entre botones o las etiquetas.

## Decisions

### D1. La escala va en Java y sale del CSS

En JavaFX una regla de una hoja de estilos del autor tiene prioridad sobre un valor fijado con `setScaleX`. Si la regla de `base.css` se quedara, pisaría la escala de cada icono y todos seguirían a 1.25. Por eso se retira el bloque `.nav-button .nav-icon` de `base.css` entero (solo contiene la escala) y la escala se fija en `icono()` con `setScaleX` y `setScaleY` sobre el `SVGPath`.

La alternativa, un estilo en línea con `setStyle`, funcionaría, pero mezcla CSS dentro de una cadena en Java sin necesidad.

### D2. Escalas

Medidas en otro clon con el motor real de JavaFX: 1.25 para Inicio, Nueva, Configuración, Copias y Salir; 1.15 para Histórico, cuyo dibujo ocupa casi toda la rejilla; 1.4 para Clientes, cuyo dibujo es el más pequeño (16x16).

El mayor dibujo escalado no pasa de unos 23 píxeles, así que todos caben en la caja de 26 sin salirse.

### D3. Firma

`boton(etiqueta, tooltip, svg, escala, accion, activo)` e `icono(contenido, escala)`: el `double offsetX` cambia de nombre y de uso en el mismo sitio, sin añadir parámetros. Se borra `caja.setTranslateX(...)`.

## Risks / Trade-offs

- **Salir.** Su desplazamiento de 2,8 compensaba que la flecha del dibujo queda cargada a un lado. Sin él, centrado de caja, puede verse algo corrido. Se acepta: la caja lo centra por su contorno, que es lo que exige el requisito. Si a ojo molesta, es un ajuste de escala, no de posición.
- **Valores de otro clon.** Se midieron sobre un estado anterior de la barra. La caja y la escala general no han cambiado desde entonces, así que siguen valiendo; se confirman a ojo en 3.2.
