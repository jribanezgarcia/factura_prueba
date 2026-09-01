## Why
Muchas pantallas aparecen cortadas a 800×600. El usuario requiere 1024×768 para todas las vistas principales salvo Arranque (760×520), y scroll general en el Editor de factura nueva. Los diálogos mantienen su tamaño actual.

## What Changes
- Pasar VentanaConfig y Main a 1024×768 para MENU, EDITOR, CONFIGURACION, HISTORICO, CLIENTES, VERSIONES, BACKUP. ARRANQUE 760×520 y GENERAR_MENSUAL 800×600 sin cambios.
- Ajustar prefWidth/prefHeight de los 7 FXML principales a 1024×768.
- Envolver el contenido completo del Editor en ScrollPane general (fitToWidth, hbar/vbar AS_NEEDED) para que toda la pantalla tenga scroll derecho.
- Actualizar tests (EditorTamanoMinimoTest, UiSmokeTest) a 1024×768.
- No modificar spec en esta fase (Fase 2 tras validación manual).

## Capabilities
### New Capabilities
- Ninguna.
### Modified Capabilities
- invoicing: tamaños de ventana y scroll del Editor (Fase 2 para spec).

## Impact
- VentanaConfig.java, Main.java
- MenuPrincipal.fxml, Editor.fxml, Configuracion.fxml, Historico.fxml, Clientes.fxml, Versiones.fxml, Backup.fxml
- EditorTamanoMinimoTest.java, UiSmokeTest.java
- No impact en spec Fase 1, ni en diálogos.
