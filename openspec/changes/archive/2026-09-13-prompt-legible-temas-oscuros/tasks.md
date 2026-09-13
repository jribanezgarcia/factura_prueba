> La decisión y el valor de partida están en `design.md - D1`.

## 1. Regla de prompt

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, junto a la regla de `.text-field` (línea 399), fijar `-fx-prompt-text-fill: derive(-fx-text-background-color, -40%)` para `.text-field`, `.text-area` y `.combo-box`. Verificar que la aplicación arranca y los campos muestran su ayuda.
- [x] 1.2 No tocar ningún `tema-*.css`, ningún FXML ni ningún `.java`.

## 2. Verificación final

- [x] 2.1 `mvn test` en verde.
- [x] 2.2 Abrir Clientes y el Histórico con un tema oscuro y comprobar que el texto de ayuda de los campos de búsqueda se lee con claridad; repetir con un tema claro. Si el contraste no llega, mover el porcentaje de `derive` y anotarlo aquí.
