> Los valores exactos están en `design.md`, sección «D5. Valores exactos».
> Usar esos números y esos paths SVG tal cual; no reinterpretarlos ni sustituirlos por otros iconos.
> Recordatorio: los `styleClass` múltiples van **con coma** (`"primary-button, btn-ribbon"`), o `StyleClassSeparadorTest` falla.

## 1. CSS base

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, insertar el bloque D5-A (`.btn-ribbon`, `.btn-ribbon .icono-boton`, `.ribbon-sep`) inmediatamente después del bloque «Botones base» que agrupa `.primary-button, .default-button, .danger-button, .action-button, .action-danger-button`.
- [x] 1.2 Verificar que no se ha modificado ninguna regla existente de `base.css`: el diff debe ser puramente aditivo.

## 2. Los siete temas

- [x] 2.1 En cada uno de los siete `themes/tema-*.css`, añadir las tres líneas de D5-B junto a la línea que ya declara `.menu-item .icono`.
- [x] 2.2 Para los temas distintos de `biblioteca8`, leer el hex real de `.primary-button { -fx-text-fill: ... }` y de `.danger-button { -fx-text-fill: ... }` de ese mismo fichero y usar esos valores. No copiar los de biblioteca8 ni inventar colores.
- [x] 2.3 Comprobar que los siete ficheros tienen las tres líneas y que ninguna regla previa se ha tocado.

## 3. Editor.fxml

- [x] 3.1 Añadir `<?import javafx.scene.shape.*?>` a la cabecera de `src/main/resources/com/alcazaba/facturacion/ui/Editor.fxml`.
- [x] 3.2 A cada uno de los 8 botones de la `HBox styleClass="action-bar"` (líneas ~24-31): sumar `btn-ribbon` a su `styleClass` y añadirle el `<graphic><SVGPath styleClass="icono-boton" content="..."/></graphic>` que le corresponde según la tabla D5-D. No cambiar `fx:id`, `onAction`, `text` ni tooltips.
- [x] 3.3 Insertar los tres `<Separator orientation="VERTICAL" styleClass="ribbon-sep"/>` en las posiciones de D5-D: tras `Nueva`, tras `Rectificativa` y tras `Restaurar`.
- [x] 3.4 Arrancar la app y comprobar que el Editor carga sin excepción de FXML y que los 8 iconos se ven.

## 4. Historico.fxml

- [x] 4.1 Añadir `<?import javafx.scene.shape.*?>` a la cabecera de `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`.
- [x] 4.2 Reordenar la `HBox spacing="8" alignment="CENTER_RIGHT"` (líneas ~42-49) al orden de D5-E: `Buscar` primero, luego `Generar mensuales`, `Exportar PDF`, `Anular`, `Eliminar`, `Volver`.
- [x] 4.3 A cada uno de los 6 botones: sumar `btn-ribbon` a su `styleClass` y añadir su `<graphic>` según la tabla D5-E.
- [x] 4.4 Insertar los dos `<Separator orientation="VERTICAL" styleClass="ribbon-sep"/>`: tras `Generar mensuales` y tras `Eliminar`.

## 5. Tests de layout

- [x] 5.1 En `src/test/java/com/alcazaba/facturacion/ui/EditorBarraAccionesTest.java`, al recorrer los hijos de `.action-bar`, filtrar los nodos que no son `Button` (los `Separator` nuevos) antes de aplicar las aserciones de contenencia y no-compresión.
- [x] 5.2 Ejecutar `mvn test` y revisar en concreto `EditorBarraAccionesTest`, `EditorTamanoMinimoTest`, `StyleClassSeparadorTest`, `BackupLayoutTest`, `ConfiguracionLayoutTest`, `VentanaTransicionTest` y `UiSmokeTest`.
- [x] 5.3 **Solo si `EditorBarraAccionesTest` falla por desbordamiento horizontal**: aplicar las palancas de ancho de `design.md` D4 en su orden (pref-width 64→60, luego spacing 8→6, luego `maxWidth` de `lblTitulo` 130→110) y parar en cuanto pase. Dejar anotado en el commit cuál se aplicó.
- [x] 5.4 **Solo si `EditorTamanoMinimoTest` falla por altura**: aplicar las palancas de alto de D4 en su orden (escala del icono 1.05→0.95, luego padding `6px 2px`→`4px 2px`, luego `logoBox` 40→36) y parar en cuanto pase.
- [x] 5.5 Suite completa en verde con `mvn test`.

## 6. Especificación

- [x] 6.1 MODIFIED «Barra de acciones del editor sin desbordamiento»: recoger que los botones son cuadrados con icono arriba y texto debajo, que el requisito de no-desbordamiento a 1024×768 con el distintivo de anulada visible se mantiene, y que los separadores no cuentan como botones.
- [x] 6.2 ADDED «Botones de acción con icono identificativo», con sus escenarios de icono monocromo, color por tema y agrupación con separadores.

## 7. Verificación manual

- [x] 7.1 Con `lanzar.bat`, Editor a 1024×768: los 8 botones se ven enteros, cuadrados y del mismo tamaño, ninguno recortado, las etiquetas largas envueltas a dos líneas.
- [x] 7.2 Editor con una factura **anulada** abierta (chip ANULADA visible) a 1024×768: sigue sin desbordarse.
- [x] 7.3 Editor maximizado: los botones no se estiran, el hueco queda a la izquierda de la barra.
- [x] 7.4 Histórico a 1024×768 y maximizado: los 6 botones alineados a la derecha con sus dos separadores, `Buscar` el primero del grupo.
- [x] 7.5 Recorrer los siete temas desde Configuración: en cada uno el icono se lee bien y toma el color correcto en las tres variantes (primary sobre fondo de acento, default/action en color de acento, danger en rojo). Prestar atención a `negro-dorado`, `neon` y `omarchy`, que son los oscuros.
