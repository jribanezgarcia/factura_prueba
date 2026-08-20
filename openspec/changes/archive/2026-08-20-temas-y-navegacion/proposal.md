## Why

La interfaz de la aplicación fue rediseñada (sistema de temas, barra de navegación superior con iconos, cabecera de empresa y resumen desglosado) fuera del flujo OpenSpec. La spec principal (`openspec/specs/invoicing/spec.md`) no refleja ese estado real, por lo que cualquier trabajo posterior o traspaso a otra IA parte de una especificación desactualizada. Este cambio formaliza lo ya implementado para que la spec principal sea fiel a la aplicación.

## What Changes

- Sistema de temas: se añade un sistema de temas visuales compuesto por un CSS base (`base.css`) y siete temas (`biblioteca8`, `omarchy`, `esmeralda`, `terracota`, `negro-dorado`, `sakura`, `neon`). El tema activo se aplica al cargar cada vista y se recuerda en la tabla de preferencias (clave `tema`, valor por defecto `biblioteca8`).
- Selector de tema en Configuración: un `ComboBox` permite cambiar el tema al vuelo y guardarlo con el botón Guardar.
- Barra de navegación superior: nueva barra con iconos SVG presente en todas las pantallas salvo el menú principal, con accesos a Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad y Salir.
- Cabecera de empresa en la interfaz: el menú principal muestra el nombre, el NIF y el logo configurados de la empresa, y el editor muestra el logo en su cabecera.
- Resumen del editor: el resumen de la factura pasa de un texto en bloque a etiquetas separadas para base, IVA y total.
- Confirmación de salida: al cerrar la ventana, la aplicación pide confirmación "¿Seguro que deseas salir?".

## Capabilities

### New Capabilities

No se introducen capacidades nuevas: todos los cambios pertenecen a la capacidad existente `invoicing`.

### Modified Capabilities

- `invoicing` (`openspec/specs/invoicing/spec.md`): se modifican los requisitos "Menú y navegación" (barra de navegación superior con iconos y confirmación de salida) y "Configuración" (selector de tema), y se añaden los requisitos "Temas y apariencia" e "Identidad de empresa en la interfaz".

## Impact

- Nuevos archivos: `src/main/java/com/alcazaba/facturacion/ui/ThemeManager.java`, `src/main/java/com/alcazaba/facturacion/ui/BarraNavegacion.java`, `src/main/resources/com/alcazaba/facturacion/themes/` (base.css + 7 temas).
- Modificados: `Main.java` (confirmación de salida), `Navegador.java` (aplica el tema al crear cada escena), `MenuController.java` (logo/nombre/NIF en el menú principal), `EditorController.java` (barra de navegación, logo y resumen desglosado en etiquetas), `ConfiguracionController.java` (selector de tema), y los controladores y FXML de las vistas restantes (barra de navegación).
- La preferencia de tema se persiste en la tabla de preferencias de SQLite existente (clave `tema`).
- No hay cambios de APIs, dependencias ni modelos de datos.