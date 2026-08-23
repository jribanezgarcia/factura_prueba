## 1. Ventana

- [x] 1.1 `Main`: minimo 800x600 y, sin preferencias guardadas, abrir a 800x600 centrada (verificar arrancando con datos limpios)

## 2. Layout responsive

- [x] 2.1 `Historico.fxml`: filtros en FlowPane (grupos etiqueta+campo juntos) y botones Exportar PDF/Buscar/Volver en fila propia abajo-derecha; verificar visualmente a 800x600
- [x] 2.2 `Configuracion.fxml`: filas de alta de IVA y Series en FlowPane con los botones agrupados al final; verificar visualmente a 800x600
- [x] 2.3 `Editor.fxml`: ColumnConstraints hgrow/fillWidth en la cabecera y retirar prefWidth rigidos (con minWidth en campos cortos); verificar visualmente a 800x600

## 3. Tests

- [x] 3.1 `UiSmokeTest`: cargar todas las vistas con el stage a 800x600 y suite completa `mvn test` en verde

## 4. Cierre

- [x] 4.1 Verificacion manual del usuario con la ventana al minimo (Historico, Configuracion IVA/Series, Editor)
