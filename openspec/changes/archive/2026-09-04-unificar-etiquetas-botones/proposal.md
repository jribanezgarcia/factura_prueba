## Why

El change `fix-barra-acciones-editor` acorta las etiquetas de la barra del editor para que los botones dejen de esconderse. El usuario puso una condición explícita al aprobarlo: **si se acortan aquí, tienen que acortarse igual en el resto de pantallas**, para que la aplicación hable con una sola voz.

Al repasar los nueve FXML aparecen inconsistencias reales que hoy conviven:

| Inconsistencia | Dónde |
|---|---|
| «Eliminar» frente a «Borrar» para la misma acción destructiva | Clientes dice «Eliminar»; Histórico dice «Borrar» |
| «Nuevo» frente a «Nueva factura» para crear | Clientes y Configuración dicen «Nuevo»; el editor decía «Nueva factura» |
| «Generar mensual» frente a «Generar facturas mensuales» | Histórico frente a Menú principal, misma función |
| El atajo metido dentro de la etiqueta | «Eliminar línea (Supr)» en el editor; «Eliminar línea» en Generar mensuales |
| «Cancelar» frente a «Volver» | Generar mensuales frente a todas las demás |
| Etiquetas largas en Copias | «Crear copia de seguridad», «Restaurar copia» |

No es solo estética: «Eliminar» y «Borrar» son la misma acción irreversible con dos nombres, y eso es exactamente lo que hace dudar antes de pulsar.

## What Changes

Se fija un criterio de etiquetado y se aplica a todas las pantallas:

- **El botón nombra la acción; el objeto lo da la pantalla.** En Clientes, «Eliminar» ya se entiende como eliminar el cliente seleccionado.
- **«Eliminar» siempre**, nunca «Borrar», para la acción destructiva.
- **«Volver» siempre** para salir de una pantalla. **«Cancelar» solo en diálogos modales**, donde el gesto es distinto: descartar lo que se estaba montando.
- **Los atajos van al tooltip**, nunca dentro de la etiqueta.
- **Una función, un nombre**: la generación mensual se llama igual desde el Histórico que desde el Menú principal.

No cambia ninguna acción, ningún destino de navegación ni ningún manejador: es texto de interfaz.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se añade un requisito de criterio de etiquetado de botones, aplicable a toda la interfaz.

## Impact

- `ui/Historico.fxml`, `ui/Clientes.fxml`, `ui/Backup.fxml`, `ui/GenerarFacturasMensuales.fxml`, `ui/Configuracion.fxml`: textos de botones.
- Tests que localicen botones por su texto: hay que revisarlos. Los de UI existentes buscan por `fx:id` o por clase CSS, así que en principio no se ven afectados, pero conviene comprobarlo antes de dar el change por cerrado.
- No se toca lógica, ni servicios, ni persistencia, ni PDF.

### Dependencia

Va **después** de `fix-barra-acciones-editor`, que es el que fija el criterio con el caso real. Hacerlo antes obligaría a redefinir las etiquetas dos veces.
