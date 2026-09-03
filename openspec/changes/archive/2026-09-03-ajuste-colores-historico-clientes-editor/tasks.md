## 1. Botones de acción del Editor en blanco y negro

- [x] 1.1 En `tema-biblioteca8.css`, cambiar `.action-button` (y su `:hover`) de gris a fondo blanco `#FFFFFF`, texto `#1F2937` y borde `#D8DBDF`, y verificar visualmente en el Editor que los botones de acción se ven blancos con texto negro (Guardar y Anular sin cambios)

## 2. Fondo gris claro en las tarjetas de Histórico y Clientes

- [x] 2.1 Añadir la clase `panel-busqueda` a la tarjeta superior de `Historico.fxml` (VBox `card, zona-contenido` que contiene los filtros y botones) y verificar que el FXML sigue cargando
- [x] 2.2 Añadir la clase `panel-busqueda` a la tarjeta superior de `Clientes.fxml` (VBox `card, zona-contenido` que contiene la búsqueda y botones) y verificar que el FXML sigue cargando
- [x] 2.3 En `tema-biblioteca8.css`, definir `.panel-busqueda { -fx-background-color: #F6F6F6; }` y verificar visualmente que Histórico y Clientes muestran el fondo gris claro en la tarjeta de acciones (la tabla sigue blanca)

## 3. Verificación

- [x] 3.1 Ejecutar la suite completa (`mvn -o test` desde el proyecto) y confirmar que sigue en verde (156 tests) con los cambios de FXML y CSS
- [x] 3.2 Comprobar visualmente que los temas distintos de Biblioteca8 no cambian de aspecto en Histórico, Clientes y Editor
