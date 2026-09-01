## Componentes Afectados
- VentanaConfig (constantes, aplicar con isShowing)
- Main (ANCHO_INICIAL, ALTO_INICIAL, configurarVentana min, aplicarPreferencias, entrarEnMenu)
- FXML: MenuPrincipal, Editor, Configuracion, Historico, Clientes, Versiones, Backup
- Tests: EditorTamanoMinimoTest, UiSmokeTest

## Lógica
- VentanaConfig: solo cambiar 7 constantes a 1024,768; mantener branching aplicarCompleto vs aplicarSinRedimensionar.
- Main: constantes 1024/768, minWidth/minHeight 1024/768, entrarEnMenu fuerza 1024×768.
- FXML: pref 1024×768, padding 12/16, sin ScrollPane salvo Editor.
- Editor: root ScrollPane fitToWidth true > BorderPane 1024×768. BorderPane conserva top/center/bottom. Tabla mantiene minHeight 120, prefHeight 220, sin vgrow (content no acotado dentro de ScrollPane). Scroll general aparece cuando totales empujan bajo viewport; tabla conserva su scroll interno.
- Tests: actualizar umbrales a 1024/768 y validar presencia de ScrollPane en Editor.

## Alternativas consideradas
- BorderPane > ScrollPane(center) dejaría cabecera fija, rechazada por requisito "todo" con scroll.
- Mantener VBox.vgrow ALWAYS en tabla dentro de ScrollPane sin efecto, se elimina.

## Testing
- mvn test 105/105
- Validación manual: abrir cada vista a 1024×768, redimensionar menor, comprobar scroll Editor.
