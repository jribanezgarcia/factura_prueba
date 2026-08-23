## Context

`Main` no fija tamano inicial: la ventana hereda el pref del FXML raiz de cada vista y solo aplica el minimo 900x600 (`setMinWidth/setMinHeight`). Al cerrar guarda `ventana_x/y/w/h` como preferencias y al arrancar las reaplica si existen. Los FXML usan HBox con anchos fijos que suman mas de 800px en Historico (filtros y botones) y en Configuracion (altas de IVA y Series); el Editor reparte su cabecera con prefWidth rigidos. Las tablas ya son fluidas (`CONSTRAINED_RESIZE_POLICY`).

## Goals / Non-Goals

**Goals:**

- Primer arranque determinista a 800x600 centrado.
- Minimo de ventana 800x600 conservando el recordatorio de ultima sesion tal cual.
- Que filtros, altas rapidas y cabecera del Editor se reorganicen (wrap) sin recortes.

**Non-Goals:**

- Sin modo pantalla completa ni persistencia de maximizado.
- Sin rediseño visual: mismos estilos, controles y orden logico.
- Menu, Clientes, Versiones y Backup no se tocan (ya caben).

## Decisions

- **Tamano inicial**: en `aplicarPreferenciasVentana`, si no hay w/h guardados se hace `setWidth(800); setHeight(600)` + centrado con `centerOnScreen()` tras aplicar; alternativa descartada: cambiar los prefWidth de todos los FXML (depende de la vista abierta, no es determinista).
- **Wrap de filas**: sustituir los HBox problematicos por `FlowPane` (hgap/vgap 8) en Historico (dos filas de filtros) y Configuracion (alta IVA y alta Series). En FlowPane no existe hgrow, asi que los botones de accion pierden el empuje a la derecha: se agrupan al final del flujo y, para Historico, se mueven a una fila propia inferior alineada a la derecha con HBox normal. Alternativa descartada: GridPane con wrap dinamico programatico (mas codigo para el mismo efecto).
- **Cabecera del Editor**: anadir `ColumnConstraints` con `hgrow=ALWAYS`/`fillWidth` a las columnas de valores del GridPane y quitar prefWidth rigidos donde sobra (conservando minWidth en campos cortos como CP o descuento). Asi los campos grandes respiran y ninguno se sale.
- **Smoke test**: `UiSmokeTest` fija el stage a 800x600 antes de cargar cada vista; detecta excepciones de layout/cableado en el tamano minimo. La comprobacion visual fina sigue siendo manual.

## Risks / Trade-offs

- [FlowPane parte lineas en puntos distintos segun ancho] → aceptable: es el comportamiento wrap pedido; los grupos etiqueta+campo se envuelven juntos dentro de sub-HBox para no separar un control de su rotulo.
- [Quitar prefWidth puede estrechar campos en pantallas grandes] → se acota con maxWidth razonables donde importe (p. ej. comboCliente) para que no se estiren absurdamente.
- [Restaurar una sesion guardada en otra pantalla] → comportamiento ya existente, fuera de alcance.
