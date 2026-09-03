## Why

La aplicación (con la marca «CaboFactu®») no muestra ningún icono propio en la barra de tareas ni identificativo en el título de las ventanas: todas se titulan «Facturación». Se quiere dar una identidad visual unificada: un icono de aplicación en la ventana principal y en las secundarias, y un título compuesto «CaboFactu® + pantalla» en todas las pantallas.

## What Changes

- Copiar `logos/logo1.png` a `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png` (commiteado y empaquetado en el JAR) y usarlo como icono de la aplicación.
- Aplicar ese icono al `Stage` principal y a los `Stage` secundarios que abre la aplicación (p. ej. el diálogo «Generar facturas mensuales»), de modo que se vea en la barra de tareas, en la esquina de la ventana y en la vista minimizada.
- Titular la ventana principal como «CaboFactu® + nombre de pantalla» para cada vista (Arranque, Menú Principal, Histórico, Configuración, Editor, Clientes, Versiones, Copias).
- Titular las ventanas secundarias (`Stage` propios) también con el prefijo «CaboFactu®» (p. ej. «CaboFactu® Generar facturas mensuales»).

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: se añade un requisito que define la identidad de la aplicación en la interfaz (icono de aplicación en la ventana principal y secundarias, y título de ventana con el prefijo de marca «CaboFactu®» en todas las pantallas).

## Impact

- `src/main/java/com/alcazaba/facturacion/Main.java`: título inicial de la ventana y establecimiento del icono.
- `src/main/java/com/alcazaba/facturacion/ui/Navegador.java`: título por pantalla al cambiar de vista.
- `src/main/java/com/alcazaba/facturacion/ui/GenerarFacturasMensualesController.java` (y cualquier otro `Stage` secundario): título e icono.
- Recurso nuevo `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png` (copia de `logos/logo1.png`).
- Tests de UI/layout que comprueben título e icono donde proceda.
