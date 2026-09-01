## 1. Preparación
- [x] 1.1 Crear change uniformizar-1024-scroll-editor
- [x] 1.2 Redactar proposal, design

## 2. VentanaConfig y Main
- [x] 2.1 VentanaConfig 7 vistas a 1024×768
- [x] 2.2 Main constantes y mins a 1024×768, entrarEnMenu fuerza 1024×768

## 3. FXML
- [x] 3.1 MenuPrincipal, Configuracion, Historico, Clientes, Versiones, Backup a 1024×768
- [x] 3.2 Editor: envolver en ScrollPane general, ajustar tabla

## 4. Tests
- [x] 4.1 EditorTamanoMinimoTest a 1024×768 + check ScrollPane
- [x] 4.2 UiSmokeTest resize a 1024×768
- [x] 4.3 mvn test

## 5. Cierre Fase 1
- [x] 5.1 Actualizar CONTINUAR_MAÑANA.md
- [x] 5.2 Commit y push (sin archivar, sin spec)

## 6. Fase 2 — robustez y spec
- [x] 6.1 Eliminar el mínimo global 1024×768 de Main.configurarVentana (choca con Arranque 760×520)
- [x] 6.2 Actualizar spec: Requisito "Ventana" y "Tamaños de ventana por vista" a 1024×768, Arranque 760×520, diálogo 800×600, escenario Editor con scroll
- [x] 6.3 mvn test
- [x] 6.4 sync-specs + archive
- [x] 6.5 Actualizar CONTINUAR_MAÑANA.md, commit y push
