> Las etiquetas, los tooltips, los valores de CSS y el color por tema están
> fijados en `design.md`, sección «D6. Valores exactos». Usar esos números y
> esos colores tal cual; no reinterpretarlos.

## 1. Texto en los botones

- [x] 1.1 En `BarraNavegacion.boton(...)`, añadir un parámetro de etiqueta corta y llamar a `b.setText(...)`, conservando el `Tooltip` con el nombre completo. La disposición icono-encima-texto **no** se pone en Java: va como `-fx-content-display: top` en `base.css` (tarea 2.1).
- [x] 1.2 Etiquetas y tooltips exactos en la tabla de D6: Inicio, Nueva, Histórico, Clientes, Configuración, Copias y Salir, con «Menú principal» y «Copia de seguridad» completos en sus tooltips.
- [x] 1.3 Comprobar que el botón mantiene hover, cursor y el subrayado de `.activo`, que dependen de la clase `.nav-button` y no del contenido.

## 2. Estilos comunes

- [x] 2.1 En `base.css`, añadir a `.nav-button` el tamaño de fuente de la etiqueta (unos 10 px) y un `-fx-graphic-text-gap` pequeño.
- [x] 2.2 Cambiar el relleno de `.nav-bar` de `10px 0 16px 0` a `6px 0 8px 0` para compensar el alto que gana la etiqueta.
- [x] 2.3 Revisar el `-fx-spacing: 34px` de `.nav-bar`: con botones más anchos por el texto, es previsible que haya que bajarlo. Ajustar a ojo sobre la pantalla real.

## 3. Color del texto en los siete temas

- [x] 3.1 En cada `tema-*.css`, añadir una regla de `-fx-text-fill` para el texto del botón de navegación, con el **mismo color** que ya usa `.nav-button .nav-icon` en ese tema.
- [x] 3.2 Colores de referencia por tema: biblioteca8 `#FCF9F9`, esmeralda `#FFFFFF`, negro-dorado `#D4AF37`, neon `#A78BFA`, omarchy `#060B1E`, sakura `#FFFFFF`, terracota `#FFFFFF`.
- [x] 3.3 Confirmar tema por tema que el texto se lee sobre el fondo de la barra. **Este paso no se puede saltar**: sin él, varios temas dejan el texto ilegible.

## 4. Presupuesto vertical del editor

- [x] 4.1 Ejecutar `EditorTamanoMinimoTest`, que es la comprobación de que el editor sigue cabiendo: falla si el total o las observaciones se salen de la escena, o si la tabla baja de 200 px.
- [x] 4.2 Si no pasa, bajar la fuente de la etiqueta y, si sigue sin caber, reducir el escalado del icono de 1,25 a 1,1. En ese orden, y sin recortar más el relleno de la barra.

## 5. Especificación

- [x] 5.1 MODIFIED «Menú y navegación»: la barra de navegación SHALL mostrar, bajo cada icono, una etiqueta de texto con el nombre de su destino, y conservar el nombre completo en el tooltip.

## 6. Verificación final

- [x] 6.1 Suite completa en verde con `mvn clean test`.
- [x] 6.2 Recorrer las seis pantallas con barra de navegación (Editor, Histórico, Clientes, Configuración, Versiones y Copias) en **los siete temas** a 1024x768.
- [x] 6.3 Comprobar en el editor que los totales siguen visibles sin scroll con una factura corta.
