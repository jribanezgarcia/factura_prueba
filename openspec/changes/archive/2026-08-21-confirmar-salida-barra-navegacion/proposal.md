## Why

El botón Salir de la barra de navegación (común a todas las pantallas salvo el menú principal) llama a `stage.close()` directamente: cierra sin pedir confirmación y, además, se salta el proceso de cierre definido en `Main.java` (`onCloseRequest`), que es quien comprueba cambios sin guardar, guarda las preferencias de ventana y libera el lock de instancia única. Al cerrar con la X de la ventana sí se ejecuta todo ese proceso; desde la barra no.

## What Changes

- El botón Salir de la barra de navegación deja de cerrar la ventana directamente y pasa a lanzar el mismo evento de cierre (`WINDOW_CLOSE_REQUEST`) que un cierre externo, de forma que:
  - se pida confirmación «¿Seguro que deseas salir de la aplicación?» igual que al cerrar con la X;
  - si hay una factura abierta con cambios sin guardar, se muestre primero el diálogo de Guardar/Descartar/Cancelar;
  - se guarden las preferencias de ventana y se libere el lock al confirmar la salida.
- No cambian navegación, vistas ni lógica de negocio.

## Capabilities

### New Capabilities

No se introducen capacidades nuevas.

### Modified Capabilities

- `invoicing` / "Menú y navegación": la confirmación de salida pasa a aplicarse también al pulsar Salir en la barra de navegación, no solo al cerrar la ventana.

## Impact

- Modificado: `src/main/java/com/alcazaba/facturacion/ui/BarraNavegacion.java` (acción del botón Salir).
- Sin cambios en FXML, servicios, repositorios ni modelo de datos.
- Corrige de paso que salir desde la barra no guardara preferencias ni liberara el lock.
