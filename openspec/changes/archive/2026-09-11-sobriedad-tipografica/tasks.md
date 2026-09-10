> El inventario de las dieciséis declaraciones está en `design.md - Context`, y la trampa del icono en `design.md - D4`.

## 1. Retirar la negrita

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, eliminar la declaración de negrita de estos quince bloques: `.section-title`, `.titulo`, `.chip-anulada`, `.nav-button`, `.btn-ribbon`, `.primary-button.btn-ribbon`, el bloque de `btn-suave` que la declara, `.table-view .column-header .label`, `.bloque-titulo`, `.empresa-nombre`, `.menu-item .nombre`, la cabecera de `.dialog-card`, `.lista-secciones .list-cell`, su variante seleccionada y `.grupo-secciones`.
- [x] 1.2 **No** tocar `.total-fila .valor`: es el único de `base.css` que conserva la negrita.
- [x] 1.3 **No** tocar `.total-grande .label` en ninguno de los siete temas.
- [x] 1.4 Comprobar que en `base.css` queda exactamente una aparición de negrita, y una en cada tema.

## 2. Icono y etiqueta del mismo color

- [x] 2.1 En `base.css`, añadir `-fx-text-fill: -fx-accent` a la regla que agrupa `.primary-button.btn-ribbon`, `.default-button.btn-ribbon` y `.action-button.btn-ribbon`. Si esas tres no comparten hoy un selector, crearlo.
- [x] 2.2 **No** incluir en ese selector `.danger-button.btn-ribbon` ni `.action-danger-button.btn-ribbon`: su rojo lo pone el tema y debe conservarse en el icono y en la etiqueta.
- [x] 2.3 **No** tocar `.nav-button`: su texto va sobre el fondo de acento de la barra y cada tema le da su color.

## 3. Retirar la acción principal

- [x] 3.1 En `base.css`, eliminar entera la regla `.primary-button.btn-suave`, la que fija `-fx-boton-lavado-fuerte` y el texto de acento.
- [x] 3.2 Eliminar las dos apariciones restantes de `-fx-boton-lavado-fuerte`, en las reglas de puntero encima y pulsado de `btn-suave`, dejando `-fx-boton-lavado` como capa de abajo, igual que tienen los demás botones.
- [x] 3.3 En `.primary-button.btn-ribbon`, quitar también el texto de acento propio, de modo que quede con el mismo tratamiento que `.action-button.btn-ribbon`. Si el bloque queda vacío, eliminarlo.
- [x] 3.4 **No** tocar la regla `.primary-button.btn-ribbon .icono-boton`. Ver `design.md - D4`: sin ella el tema pintaría ese icono del color que contrasta sobre el acento, y quedaría invisible sobre la barra.
- [x] 3.5 En los siete temas, eliminar la declaración `-fx-boton-lavado-fuerte` del bloque `.root`. **No** tocar `-fx-boton-lavado`.
- [x] 3.6 Comprobar que no queda ninguna referencia a `-fx-boton-lavado-fuerte` en todo el proyecto.
- [x] 3.7 **No** tocar ningún FXML: la clase `primary-button` sigue declarada donde está y solo deja de pintar distinto.

## 4. Verificación

- [x] 4.1 `mvn test` en verde.
- [x] 4.2 Recorrer menú principal, Editor, Histórico, Clientes, Configuración, Copias y Versiones comprobando que no queda ninguna negrita fuera del bloque de totales de la factura.
- [x] 4.3 Comprobar que el importe del total sigue en negrita.
- [x] 4.4 Comprobar en el Editor que icono y etiqueta comparten el color de acento, y que Anular conserva el rojo en los dos.
- [x] 4.5 Comprobar que Guardar se ve exactamente igual que Nueva, sin sombreado ni color propios.
- [x] 4.6 Comprobar en Configuración que los rótulos CONFIGURACIÓN GENERAL y CATÁLOGOS se siguen leyendo. Si quedan demasiado tenues, subir su opacidad por encima de 0.55 y anotarlo al reportar; nunca devolverles la negrita. Se aplicó `-fx-font-size` 12px y `-fx-opacity` 0.70 en `.grupo-secciones`.
- [x] 4.7 Anular una factura y comprobar que el distintivo ANULADA se sigue identificando de un vistazo solo por su rojo.
- [x] 4.8 Repetir el recorrido en un tema claro y en uno oscuro.
