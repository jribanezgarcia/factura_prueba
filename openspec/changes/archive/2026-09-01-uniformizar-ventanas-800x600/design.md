## Componentes Afectados

- `VentanaConfig.java`: constantes de tamaño de vistas; lógica de `aplicar` para no redimensionar la ventana principal al navegar.
- `Main.java`: transición `Arranque` → `Menú` para poner la ventana en 800×600 sin maximizar.
- FXML/CSS de:
  - `Configuracion.fxml`
  - `Historico.fxml`
  - `Clientes.fxml`
  - `Versiones.fxml`
  - `Backup.fxml`
  - `GenerarFacturasMensuales.fxml`
- `VentanaConfigTest` (si existe) u otros tests que validen tamaños anteriores.

## Lógica de navegación

- En `Main.entrarEnMenu`:
  - Si la ventana está maximizada, desmaximizar.
  - Establecer ancho y alto a 800×600 (la altura de Arranque es menor).
  - Aplicar `VentanaConfig` del Menú.
  - NO forzar centrado; el SO mantiene la posición actual.
- En `VentanaConfig.aplicar`:
  - Si es un `Stage` nuevo/diálogo: aplicar `width` y `height` predefinidos.
  - Si es la ventana principal y ya estamos dentro de la app: solo aplicar `minWidth`, `minHeight`, `resizable`, `maximized`; NO modificar `width`, `height`, ni centrado.

## Ajustes de layout por pantalla

### Configuración
- Pestañas con `TabPane` y formularios: reducir padding general y espaciado de grids.
- Tabla de series/tipos de retención ajustar columnas a anchos menores o habilitar `columnResizePolicy` con scroll horizontal.

### Histórico
- Filtros en una sola línea o en dos líneas compactas.
- Tabla de facturas con `columnResizePolicy="CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN"` y scroll horizontal.

### Clientes
- Similar a Histórico: tabla con scroll horizontal; barra de búsqueda compacta.

### Versiones
- Tabla con pocas columnas; reducir padding.

### Backup
- Formulario compacto con `VBox` spacing reducido; botones alineados abajo.

### Generar facturas mensuales
- Diálogo con `ScrollPane` interno por si las fechas y el resumen no caben en 800×600.

## Testing

- Verificar que `mvn test` sigue pasando (105 tests).
- Navegar manualmente entre pantallas y comprobar que la ventana no salta.
- Comprobar que Arranque sigue 760×520 y el resto 800×600.
