## 1. Planificación OpenSpec

- [x] 1.1 Crear change `window-sizing`.
- [x] 1.2 Redactar `proposal.md`.
- [x] 1.3 Redactar `spec.md` delta.
- [x] 1.4 Redactar `design.md`.

## 2. Configuración centralizada de tamaños

- [x] 2.1 Crear `VentanaConfig.java` con los datos de cada vista.
- [x] 2.2 Mapear cada FXML a su configuración.

## 3. Aplicación de tamaños en navegación

- [x] 3.1 Modificar `Navegador.mostrar(...)` para aplicar `VentanaConfig` al `Stage`.
- [x] 3.2 Asegurar que `Arranque` queda fijo (no redimensionable, min=max=760×520).
- [x] 3.3 Ajustar `Main` para que las preferencias de ventana no interfieran con la configuración por vista.

## 4. Diálogos

- [x] 4.1 Aplicar `VentanaConfig` al diálogo de `GenerarFacturasMensualesController`.

## 5. Tests

- [x] 5.1 Crear `EditorTamanoMinimoTest.java` que verifique que el Editor cabe en 1000×760.
- [x] 5.2 Ejecutar `mvn test` y corregir fallos.

## 6. Cierre OpenSpec

- [x] 6.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 6.2 Archivar el change `window-sizing`.
- [x] 6.3 Actualizar `CONTINUAR_MAÑANA.md`.
- [x] 6.4 Hacer commit y push.
