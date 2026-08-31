## 1. Prototipado visual

- [x] 1.1 Generar prototipo HTML del Menú principal con estilo Ajustes Apple y verificar que incluye tarjetas, iconos y jerarquía clara.
- [x] 1.2 Generar prototipo HTML del Editor de factura con tarjetas de sección, estados de foco y tabla de líneas limpia.
- [x] 1.3 Generar prototipo HTML del Histórico con filtros reorganizables, tabla limpia y botones de acción con hover.
- [x] 1.4 Generar prototipo HTML de Configuración mostrando secciones tipo lista y selector de tema (sin modo claro/oscuro).
- [x] 1.5 Validar prototipos con el usuario antes de traducirlos a JavaFX.

## 2. Sistema visual y base CSS

- [x] 2.1 Definir variables estructurales en `base.css` (espaciado, radios, sombras, estados de foco) sin cambiar paletas de color.
- [x] 2.2 Renovar estilos base para botones, campos, tablas, etiquetas, títulos, tarjetas y navegación según el diseño Apple, manteniendo que los temas aporten los colores.
- [x] 2.3 Verificar que los 7 temas existentes (`biblioteca8`, `omarchy`, `esmeralda`, `terracota`, `negro-dorado`, `sakura`, `neon`) se siguen aplicando correctamente.
- [x] 2.4 Ajustar transparencias y estados genéricos en `base.css` para que funcionen sobre fondos claros y oscuros sin romper los temas.

## 3. Ajustes de FXML

- [x] 3.1 Ajustar `MenuPrincipal.fxml` para usar nuevas clases CSS de tarjetas, espaciado y estados hover.
- [x] 3.2 Ajustar `Editor.fxml` para agrupar cabecera, cliente, datos de pago y totales en secciones con el nuevo estilo.
- [x] 3.3 Ajustar `Historico.fxml` para filtros reorganizables a 800x600 y tabla con el nuevo estilo.
- [x] 3.4 Ajustar `Configuracion.fxml` para presentar pestañas como secciones tipo Ajustes.
- [x] 3.5 Ajustar `Clientes.fxml`, `GenerarFacturasMensuales.fxml`, `Versiones.fxml`, `Backup.fxml` y `Arranque.fxml` con el nuevo sistema de estilos.
- [x] 3.6 Verificar que ninguna pantalla recorta controles a 800x600.

## 4. Microinteracciones

- [x] 4.1 Implementar efectos `:hover` y `:focused` en botones y campos mediante CSS.
- [x] 4.2 Añadir transiciones JavaFX suaves en botones principales e items del menú (escala/opacidad), respetando preferencia de movimiento reducido.
- [x] 4.3 Verificar que las animaciones no bloquean la interacción y duran menos de 200 ms.

## 5. Tests y validación

- [x] 5.1 Ejecutar `mvn test` y corregir tests de UI que fallen por cambios de clases CSS o estructura de nodos.
- [x] 5.2 Validar visualmente las pantallas principales con al menos tres temas distintos (claro, oscuro y uno de acento vivo).

## 6. Cierre OpenSpec

- [x] 6.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 6.2 Archivar el change `redesign-ui-apple`.
- [x] 6.3 Actualizar `CONTINUAR_MAÑANA.md` con el cierre del change.
- [x] 6.4 Hacer commit y push de los cambios.
