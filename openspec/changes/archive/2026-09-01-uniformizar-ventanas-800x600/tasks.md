## 1. Preparación

- [x] 1.1 Crear change `uniformizar-ventanas-800x600`.
- [x] 1.2 Redactar `proposal.md`, `design.md` y delta de spec.

## 2. Actualizar `VentanaConfig`

- [x] 2.1 Cambiar constantes de Configuración, Histórico, Clientes, Versiones, Backup y GenerarMensual a 800×600.
- [x] 2.2 Modificar `aplicar` para no redimensionar la ventana principal ya inicializada; solo aplicar mínimos y propiedades.

## 3. Actualizar `Main.java`

- [x] 3.1 En `entrarEnMenu`: forzar tamaño 800×600 al salir de Arranque y asegurar que no queda maximizado.

## 4. Ajustar layouts

- [x] 4.1 `Configuracion.fxml`: reducir padding, compactar pestañas, tabla scrollable.
- [x] 4.2 `Historico.fxml`: filtros compactos, tabla scroll horizontal.
- [x] 4.3 `Clientes.fxml`: búsqueda compacta, tabla scroll horizontal.
- [x] 4.4 `Versiones.fxml`: reducir padding, tabla adaptada.
- [x] 4.5 `Backup.fxml`: compactar formulario y botones.
- [x] 4.6 `GenerarFacturasMensuales.fxml`: diálogo 800×600 con `ScrollPane` si es necesario.

## 5. Tests

- [x] 5.1 Actualizar tests que validen tamaños anteriores.
- [x] 5.2 Ejecutar `mvn test`.

## 6. Cierre OpenSpec

- [x] 6.1 Sincronizar spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 6.2 Archivar el change `uniformizar-ventanas-800x600`.
- [x] 6.3 Actualizar `CONTINUAR_MAÑANA.md`.
- [x] 6.4 Hacer commit y push.
