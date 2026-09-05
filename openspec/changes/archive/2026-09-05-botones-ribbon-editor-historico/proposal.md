## Why

Las barras de acciones del Editor y del Histórico son filas de botones rectangulares con solo texto (`.action-button`, `.default-button`, `.primary-button`). Sin icono ni agrupación, las ocho acciones del Editor se leen como una única tira indiferenciada de etiquetas: el usuario tiene que leer palabra por palabra para localizar la que busca, y no hay ninguna pista visual de que `Anular` y `Restaurar` sean acciones de otra naturaleza que `Guardar` y `Nueva`.

El software de facturación profesional de referencia resuelve esto con una barra tipo *ribbon*: botones cuadrados, delimitados por borde, con un icono identificativo arriba y el texto debajo, y grupos separados por una línea vertical. El icono se reconoce antes que la palabra y el separador convierte una tira de ocho elementos en cuatro bloques de dos o tres.

La aplicación ya tiene todas las piezas para hacerlo: la barra de navegación (`BarraNavegacion.java`) ya usa `SVGPath` monocromo con `-fx-content-display: top`, y `MenuPrincipal.fxml` ya declara iconos `SVGPath` inline dentro del `<graphic>` de un botón. Falta llevar ese patrón a las barras de acciones.

## What Changes

- Los 8 botones de la barra de acciones del Editor y los 6 del Histórico pasan a ser cuadrados de anchura fija, con icono monocromo arriba y texto debajo, envuelto a dos líneas cuando la etiqueta es larga.
- Se añaden separadores verticales entre grupos: en el Editor `Guardar · Nueva` ┃ `Exportar PDF · Versiones · Rectificativa` ┃ `Anular · Restaurar` ┃ `Volver`; en el Histórico `Buscar · Generar mensuales` ┃ `Exportar PDF · Anular · Eliminar` ┃ `Volver`.
- En el Histórico cambia el orden de los botones para que respondan a esa agrupación: `Buscar` pasa al principio, junto a `Generar mensuales`.
- Los iconos son paths de Material Symbols (Apache-2.0), monocromo de un solo color, coloreados desde el tema activo igual que los de la barra de navegación y los del menú principal. Funcionan en los siete temas, incluidos los oscuros.
- Cada botón **conserva su clase actual** (`primary-button`, `action-button`, `default-button`, `action-danger-button`) y **suma** una clase nueva `btn-ribbon`. Los colores siguen viniendo de las reglas que ya existen en los siete temas; `btn-ribbon` aporta solo geometría.
- No cambia ningún texto de botón, ningún `onAction`, ningún `fx:id`, ningún controlador, ninguna lógica de negocio ni ninguna consulta.
- Quedan **fuera** de este change: los botones de formulario y de diálogo (`Elegir imagen...`, `Elegir carpeta...`, `Volver` de Backup/Versiones, el modal de generación mensual, los `ButtonType` de `Dialogos.java`), y las pantallas Clientes, Configuración, Backup, Versiones y Arranque. Se decidirán en un change posterior a la vista del resultado de este.
- Los botones `Añadir línea` / `Eliminar línea` del Editor (`Editor.fxml:95-96`) tampoco se tocan: son botones de tabla, no de barra de acciones.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía «Barra de acciones del editor sin desbordamiento» para recoger que los botones son cuadrados con icono y siguen sin desbordarse a 1024×768 con el distintivo de anulada visible, y se añade el requisito «Botones de acción con icono identificativo».

## Impact

- `themes/base.css`: reglas nuevas `.btn-ribbon`, `.btn-ribbon .icono-boton` y `.ribbon-sep`. Ninguna regla existente se modifica.
- Los 7 `themes/tema-*.css`: tres líneas nuevas por fichero con el `-fx-fill` del icono para las variantes primary, default/action y danger.
- `ui/Editor.fxml`: `<graphic>` con `SVGPath` en los 8 botones de la `action-bar`, tres `Separator` verticales, y el import de `javafx.scene.shape.*`.
- `ui/Historico.fxml`: lo mismo en sus 6 botones, dos separadores, reordenación de `Buscar`, y el import de `javafx.scene.shape.*`.
- `ui/EditorBarraAccionesTest`: recorre los hijos de `.action-bar`; hay que filtrar los `Separator` nuevos y revisar que las aserciones de no-desbordamiento y no-compresión siguen pasando con los botones más anchos y más altos.
- Riesgo de layout en `EditorTamanoMinimoTest` (la barra crece ~20 px de alto) y en `StyleClassSeparadorTest` (los `styleClass` múltiples nuevos deben llevar coma).
- No se tocan controladores, servicios, persistencia ni generación de PDF.
