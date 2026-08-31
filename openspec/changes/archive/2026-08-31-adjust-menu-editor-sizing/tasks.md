## 1. Planificación OpenSpec

- [x] 1.1 Crear change `adjust-menu-editor-sizing`.
- [x] 1.2 Redactar `proposal.md`, `spec.md`, `design.md`.

## 2. Menú principal 800×600 con margen inferior

- [x] 2.1 Actualizar `VentanaConfig.MENU` a 800×600.
- [x] 2.2 Actualizar `MenuPrincipal.fxml` a 800×600, aumentar padding inferior y alinear tarjetas arriba.

## 3. Editor maximizado por defecto

- [x] 3.1 Añadir flag `maximizado` a `VentanaConfig`.
- [x] 3.2 Activar maximizado para `EDITOR`.
- [x] 3.3 Aplicar `stage.setMaximized(...)` en `VentanaConfig.aplicar`.

## 4. Tests

- [x] 4.1 Ajustar `EditorTamanoMinimoTest` para desmaximizar antes de verificar tamaño.
- [x] 4.2 Ejecutar `mvn test` y corregir fallos.

## 5. Cierre OpenSpec

- [x] 5.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 5.2 Archivar el change `adjust-menu-editor-sizing`.
- [x] 5.3 Actualizar `CONTINUAR_MAÑANA.md`.
- [x] 5.4 Hacer commit y push.
