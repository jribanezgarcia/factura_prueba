> Las decisiones y medidas están en `design.md`.

## 1. Barra de navegación

- [x] 1.1 En `src/main/java/com/alcazaba/facturacion/ui/BarraNavegacion.java`, renombrar el parámetro `offsetX` de `boton()` e `icono()` a `escala`. Ver `design.md - D3`.
- [x] 1.2 En `icono()`, borrar `caja.setTranslateX(offsetX)` y fijar `p.setScaleX(escala)` y `p.setScaleY(escala)` sobre el `SVGPath`. La caja de 26x26 se queda igual.
- [x] 1.3 En `crear()`, pasar las escalas: `1.25` en Inicio, Nueva, Configuración, Copias y Salir; `1.15` en Histórico; `1.4` en Clientes. Ver `design.md - D2`.
- [x] 1.4 No tocar trazados, etiquetas, tooltips, acciones ni el espaciado de la barra.

## 2. Hoja de estilos

- [x] 2.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, borrar entero el bloque `.nav-button .nav-icon` con su escala. Ver `design.md - D1`.
- [x] 2.2 No tocar las reglas `.nav-button .nav-icon` de los siete temas: solo fijan el relleno y deben seguir haciéndolo.
- [x] 2.3 Comprobar con una búsqueda que no queda ninguna `-fx-scale` aplicada a `.nav-icon` en ninguna hoja.

## 3. Verificación final

- [x] 3.1 `mvn test` en verde.
- [x] 3.2 Abrir cualquier pantalla con barra de navegación y comprobar que los siete iconos se ven de un tamaño parecido, que Clientes ya no parece más pequeño e Histórico ya no parece más grande.
- [x] 3.3 Comprobar que cada icono queda centrado sobre su etiqueta, Histórico y Salir incluidos.
- [x] 3.4 Comprobar que ningún icono toca su etiqueta ni se sale de su botón, y que la barra no ha cambiado de alto.
- [x] 3.5 Cambiar a un tema oscuro y a uno claro y comprobar que los iconos conservan su color.
