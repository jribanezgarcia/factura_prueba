## Contexto

La ventana principal se titula siempre «Facturación» (`Main.configurarVentana`) y no hay ningún icono de aplicación: cada `Stage` usa el icono por defecto de JavaFX/Windows. Las vistas se cargan por `Navegador.mostrar(fxml)`, que aplica `VentanaConfig.para(fxml)` (enum que mapea cada FXML a un tamaño de ventana) pero no toca el título ni el icono. Solo existe un `Stage` secundario: el diálogo «Generar facturas mensuales» (`GenerarFacturasMensualesController` crea `new Stage()`).

## Decisiones de diseño

- **D1 — Recurso del icono**: se copia `logos/logo1.png` a `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png`. Se carga por classpath (`getResourceAsStream`), así queda dentro del JAR y se commitea (mientras `logos/` no está en `.gitignore`). Decisión del usuario validada en planning: ruta resources JavaFX y nombre `icono-aplicacion.png`.
- **D2 — Helper de icono**: clase pequeña `ui/Ventanas` (o método estático reutilizable) con `aplicarIcono(Stage)` (constante `ICONO = "/com/alcazaba/facturacion/images/icono-aplicacion.png"`) que hace `stage.getIcons().add(new Image(...))`; defensivo (si no se encuentra el recurso, no hace nada) e idempotente (una sola vez por ventana). Se aplica de forma central en `Navegador.mostrar` (todas las vistas navegadas), en la ventana principal (`Main.configurarVentana`, antes del arranque) y en el `Stage` secundario («Generar facturas mensuales»).
- **D3 — Marca en el título**: constante única `PREFIJO` = `"CaboFactu® "` compartida por todas las ventanas.
- **D4 — Título por pantalla en la ventana principal**: `VentanaConfig` gana un campo `titulo` (nombre de pantalla, p. ej. `MENU → "Menu Principal"`). `Navegador.mostrar` fija `stage.setTitle(PREFIJO + cfg.titulo())` cuando existe config para el FXML; si no, mantiene el título por defecto de la ventana. El título inicial en `Main.configurarVentana` pasa a ser `PREFIJO + "Arranque"` (o el del FXML de arranque).
  - Mapeo de títulos propuesto (se afina en apply, siguiendo el ejemplo del usuario «CaboFactu® Menu Principal»): `ARRANQUE → "Seleccion de empresa"`, `MENU → "Menu Principal"`, `EDITOR → "Editor de factura"`, `CONFIGURACION → "Configuracion"`, `HISTORICO → "Historico"`, `CLIENTES → "Clientes"`, `VERSIONES → "Versiones"`, `BACKUP → "Copias"`, `GENERAR_MENSUAL → "Generar facturas mensuales"`.
- **D5 — Ventana secundaria**: en `GenerarFacturasMensualesController`, al crear `Stage dialog`, se aplica `Ventanas.aplicarIcono(dialog)` y se antepone la marca a su título (`dialog.setTitle(PREFIJO + dialog.getTitle())` tras el setTitle original). Es la única ventana `Stage` secundaria de la app, por lo que con esto se cubre «vistas + ventanas secundarias».
- **D6 — Prueba**: test (ampliar `VentanaTransicionTest` o un `VentanaIconoTituloTest` nuevo) que, con un `Stage` real, cargue `MenuPrincipal` y verifique `stage.getTitle()` == `"CaboFactu® Menu Principal"` y `!stage.getIcons().isEmpty()`. Si montar un Stage de prueba del diálogo secundario resulta forzado, se documenta en vez de testearlo.

## Archivos afectados

- `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png` (nuevo, copia de `logos/logo1.png`).
- `src/main/java/com/alcazaba/facturacion/Main.java` (título inicial + icono de la ventana principal).
- `src/main/java/com/alcazaba/facturacion/ui/VentanaConfig.java` (campo `titulo`).
- `src/main/java/com/alcazaba/facturacion/ui/Navegador.java` (título por pantalla).
- `src/main/java/com/alcazaba/facturacion/ui/Ventanas.java` (nuevo helper de icono/marca).
- `src/main/java/com/alcazaba/facturacion/ui/GenerarFacturasMensualesController.java` (título e icono del `Stage` secundario).
- Tests de UI/ventana.
