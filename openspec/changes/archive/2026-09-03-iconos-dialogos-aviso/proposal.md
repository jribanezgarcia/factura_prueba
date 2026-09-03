# Iconos en los diálogos de aviso

## Why

Las ventanas emergentes de aviso (información, error y confirmación) aparecen sin ningún icono: sale una ventana "random" sin nada. Los diálogos se visten con el tema mediante `Dialogos.aplicarTema`, que hace `pane.getStylesheets().setAll(ThemeManager.hojas())`. Ese `setAll` sustituye la hoja Modena por defecto de JavaFX, que es la que suministra los iconos gráficos de cada tipo de `Alert` (información, error/aviso, confirmación). Al quitarla, los diálogos se quedan sin el icono que indica el tipo de aviso.

## What Changes

- Añadir un icono gráfico explícito a cada diálogo de aviso, elegido según su tipo:
  - **Información** (`info`): icono de información.
  - **Error** (`error`): icono de alerta/error.
  - **Confirmación** (`confirmar`, `confirmarCambiosSinGuardar`, `modoGuardarVersion`): icono de pregunta/confirmación.
- El icono SHALL dibujarse con el color de acento del tema activo (estilo SVG similar a los glifos `nav-icon` / `icono` ya existentes) para que se integre visualmente en cualquier tema.
- Los diálogos SHALL seguir mostrando el fondo de tarjeta y el texto actuales; solo se añade el icono en la zona gráfica, sin romper el título/encabezado actuales.
- Además del glifo interno, cada ventana de diálogo `Alert` SHALL mostrar el icono de aplicación de la marca en su barra de título y en la barra de tareas de Windows (igual que el resto de ventanas de la app), reutilizando `Ventanas.aplicarIcono(Stage)`.
- No se altera el comportamiento de los diálogos (títulos, mensajes, botones, flujo de confirmación/cancelación).

## Capabilities

### New Capabilities

(ninguna)

### Modified Capabilities

- `invoicing`: nuevo requisito «Iconos en los diálogos de aviso» dentro de la interfaz de la aplicación. Los diálogos de información, error y confirmación muestran el icono gráfico correspondiente a su tipo (usando el color de acento del tema) y, además, la ventana del diálogo muestra el icono de aplicación de la marca en su barra de título y en la barra de tareas.

## Impact

- `src/main/java/com/alcazaba/facturacion/ui/Dialogos.java`: aplicar el glifo por tipo en cada constructor de `Alert` y llamar a `Ventanas.aplicarIcono(Stage)` sobre la ventana del diálogo.
- Reutiliza `Ventanas.aplicarIcono(Stage)` (ya existente e idempotente); no se crean recursos nuevos de icono.
- Una pequeña lógica de glifos (`SVGPath`) para el icono por tipo, con `styleClass "dialog-icon"` coloreado por CSS.
- `base.css`: una sola regla `.dialog-icon { -fx-fill: -fx-accent; }`; sin tocar ningún `tema-*.css` ni los 7 temas.
- Tests existentes de diálogos siguen en verde (los tests usan `setImpl`, no la implementación real de `Alert`).
