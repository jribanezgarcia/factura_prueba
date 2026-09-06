> El bloque CSS exacto está en `design.md`, sección «D7-A». Copiarlo tal cual, sin reinterpretarlo.
> Este change toca **un único fichero**: `src/main/resources/com/alcazaba/facturacion/themes/base.css`.
> Si en algún momento parece necesario tocar un `.fxml`, un `.java` o un `tema-*.css`, parar: no es lo que este change pide.

## 1. CSS base

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, localizar la regla `.ribbon-sep` (línea ~165, justo antes del comentario `/* Tablas */`).
- [x] 1.2 Insertar entre `.ribbon-sep` y `/* Tablas */` el bloque completo de D7-A: las cinco reglas (base plana, texto de la variante principal, icono de la variante principal, `:hover`, `:pressed`, `:focused`).
- [x] 1.3 Verificar que el diff de `base.css` es **puramente aditivo**: `git diff base.css` no debe mostrar ninguna línea eliminada ni modificada.
- [x] 1.4 Verificar que `git status` no muestra ningún otro fichero del change modificado.

## 2. Comprobación de que la regla gana al tema

- [x] 2.1 Arrancar con `lanzar.bat` y abrir el Editor con el tema por defecto (biblioteca8). Los ocho botones deben verse sin recuadro blanco y sin borde, fundidos con el gris de la barra.
- [x] 2.2 **Solo si siguen viéndose blancos**: no cambiar el enfoque a ciegas. Comprobar primero en `Editor.fxml` que el `styleClass` de cada botón lleva las dos clases separadas por coma (`"action-button, btn-ribbon"`). Si eso está bien, aplicar la palanca de reserva de `design.md` D2 (bloque replicado al final de los siete `tema-*.css`) y anotarlo en el commit. *(No aplica: 2.1 confirmado, sin palanca.)*

## 3. Verificación automática

- [x] 3.1 `mvn test`. Suite completa en verde.
- [x] 3.2 Revisar en concreto `EditorBarraAccionesTest`, `EditorTamanoMinimoTest`, `EditorFlujoTecladoTest`, `StyleClassSeparadorTest` y `UiSmokeTest`. La geometría no cambia, así que un fallo en cualquiera de ellos indica que se ha tocado algo que no tocaba: revisar el diff antes de ajustar el test.

## 4. Verificación manual

- [x] 4.1 Editor a 1024x768: los ocho botones sin caja, icono y etiqueta legibles, mismo tamaño y misma posición que antes del cambio.
- [x] 4.2 `Guardar` se distingue a simple vista de `Nueva` y de `Exportar PDF`: su icono y su texto van en el color de acento del tema y el texto en negrita.
- [x] 4.3 `Anular` mantiene su rojo y no se confunde con un botón deshabilitado. Comparar en la misma pantalla un `Anular` habilitado con un `Versiones` deshabilitado.
- [x] 4.4 Pasar el ratón por encima de cada botón: aparece un velo gris translúcido que delimita el botón. Mantener pulsado: el velo se oscurece.
- [x] 4.5 Recorrer la barra con `Tab`: el botón enfocado muestra un borde de color de acento. Sin ese borde el foco sería invisible.
- [x] 4.6 Histórico a 1024x768: los seis botones sin caja sobre el gris `#F6F6F6` de la tarjeta, con `Buscar` destacado en color de acento y los dos separadores en su sitio.
- [x] 4.7 Recorrer los siete temas desde Configuración con el Editor y el Histórico abiertos. Prestar atención especial a `negro-dorado`, `neon` y `omarchy`: comprobar que el icono y la etiqueta de las tres variantes se siguen leyendo sobre el fondo oscuro de la barra ahora que no hay caja detrás.
- [x] 4.8 Comprobar que `Añadir línea` y `Eliminar línea` del Editor **conservan** su caja blanca: son botones de tabla y quedan fuera del change.
- [x] 4.9 Comprobar que Clientes, Configuración, Copia de seguridad, Versiones y el diálogo de generación mensual no han cambiado en nada.

## 5. Barra completamente plana (OPCIONAL)

> **No ejecutar sin decisión expresa del usuario después de ver el resultado de las tareas 1 a 4.**
> Decisión del usuario (2026-09-06): **no se aplica**; la banda gris se mantiene. Tareas 5.1–5.3 descartadas.

- [ ] 5.1 ~~Si el usuario pide además que desaparezca la banda gris de la barra del Editor, añadir a `base.css`, junto al bloque de D7-A:~~
      ~~`.panel-busqueda .action-bar { -fx-background-color: transparent; -fx-border-color: transparent; }`~~ *(descartado)*
- [ ] 5.2 ~~Revisar los siete temas: sin la banda, los botones quedan sobre el fondo de la tarjeta `.panel-busqueda`, que en los temas oscuros no es el mismo color que la banda. Verificar contraste de icono y etiqueta en los tres oscuros.~~ *(descartado)*
- [ ] 5.3 ~~Si se aplica, actualizar el escenario correspondiente de la especificación, que hoy da por hecho que la barra tiene fondo propio.~~ *(descartado)*

## 6. Especificación

- [x] 6.1 MODIFIED «Botones de acción con icono identificativo»: el botón deja de tener recuadro y borde visible en reposo; se define el aspecto plano, el realce por *hover* y el borde de foco; y el color del icono de la variante principal pasa del color sobre acento al color de acento.
- [x] 6.2 MODIFIED «Estilo de zona de acciones en tema por defecto»: los botones de la barra de acciones del Editor dejan de mostrarse con fondo blanco; `Añadir línea` y `Eliminar línea` sí lo conservan; `Anular` sigue sin confundirse con un botón deshabilitado.
- [x] 6.3 Las dos requirements ya están escritas enteras en `specs/invoicing/spec.md` y `openspec validate botones-planos-editor-historico` pasa. No reescribirlas: los títulos de escenario existentes se conservan tal cual porque `archive` se niega a soltarlos, aunque su contenido haya cambiado (es el caso de «Botones del Editor en blanco y negro», que ahora habla solo de los botones de tabla).
