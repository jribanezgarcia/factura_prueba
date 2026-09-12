> Las razones están en `design.md`. Un solo archivo: `Historico.fxml`.

## 1. Etiqueta y tooltip

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`, en el botón `onAction="#generarMensual"`, cambiar `text="Facturar mes"` por `text="Mensual"`. Ver `design.md - D1`.
- [x] 1.2 Añadir a ese mismo botón `<tooltip><Tooltip text="Generar facturas mensuales"/></tooltip>` como hijo, junto al `<graphic>`. Ver `design.md - D2`.
- [x] 1.3 No tocar su icono, su `onAction` ni sus style-class.
- [x] 1.4 No tocar `MenuPrincipal.fxml`: la opción sigue llamándose «Facturar mes». Ver `design.md - D3`.

## 2. Verificación

- [x] 2.1 `mvn test` en verde. Buscar antes en `src/test` si algún test localiza el botón por el texto «Facturar mes»; si lo hace, actualizarlo.
- [x] 2.2 Abrir el Histórico a 1024×768 y comprobar que la etiqueta se lee «Mensual» entera, en una línea, sin puntos suspensivos, y a la misma altura que las de los demás botones.
- [x] 2.3 Posar el puntero sobre el botón y comprobar que aparece «Generar facturas mensuales».
- [x] 2.4 Comprobar que el botón sigue midiendo lo mismo que los demás de la barra y que la barra no ha cambiado de alto.
