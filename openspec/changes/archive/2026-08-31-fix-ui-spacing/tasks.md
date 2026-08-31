## 1. Diagnóstico visual

- [x] 1.1 Confirmar que el problema afecta a Histórico, Configuración y otras pantallas principales.

## 2. Ajustes de márgenes en base.css

- [x] 2.1 Aumentar el padding de `.zona-contenido` para dar margen externo coherente.
- [x] 2.2 Añadir separación entre `.nav-bar` y el contenido siguiente.

## 3. Ajustes de FXML

- [x] 3.1 Añadir padding de ventana en `Historico.fxml` y separación entre barra de navegación y filtros.
- [x] 3.2 Ajustar márgenes en `Configuracion.fxml` para separar pestañas del borde y de la barra de menú.
- [x] 3.3 Revisar y ajustar `Clientes.fxml`, `Versiones.fxml`, `Backup.fxml`, `Editor.fxml`, `MenuPrincipal.fxml` y `Arranque.fxml` con el mismo criterio.

## 4. Tests y validación

- [x] 4.1 Ejecutar `mvn test` y corregir tests de UI si fallan.
- [x] 4.2 Verificar visualmente que los campos no toquen el borde ni se solapen con el menú.

## 5. Cierre OpenSpec

- [ ] 5.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [ ] 5.2 Archivar el change `fix-ui-spacing`.
- [ ] 5.3 Actualizar `CONTINUAR_MAÑANA.md`.
- [ ] 5.4 Hacer commit y push.
