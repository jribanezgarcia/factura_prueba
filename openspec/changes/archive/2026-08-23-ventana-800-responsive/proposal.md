# Ventana base 800x600 y responsive minimo

## Why

La aplicacion abre hoy a 900x600 como minimo y no tiene tamano inicial propio: la primera vez hereda el prefWidth/prefHeight del FXML de cada pantalla (760x520 el menu, 1000x620 el resto). El usuario quiere un arranque compacto 800x600, poder redimensionar por debajo del minimo actual y que ninguna vista recorte controles con la ventana pequena.

## What Changes

- Primer arranque (sin preferencias de ventana guardadas): la ventana SHALL abrir a 800x600 centrada en pantalla.
- Tamano minimo de ventana SHALL bajar de 900x600 a 800x600.
- Se mantiene el comportamiento actual de recordar tamano/posicion de la ultima sesion.
- Historico: los filtros (serie, cliente/NIF, fechas, importes, estado) se reorganizan en varias lineas cuando el ancho no basta; los botones Exportar PDF / Buscar / Volver quedan en su propia fila abajo a la derecha.
- Configuracion: las filas de alta rapida de IVA y de Series se reorganizan cuando el ancho no basta, manteniendo agrupados sus botones al final.
- Editor: las columnas de valores de la cabecera (cliente, direccion, localidad...) se reparten el ancho disponible en lugar de anchos prefijados rigidos.

## Capabilities

### New Capabilities

- (ninguna)

### Modified Capabilities

- `invoicing`: requisito nuevo «Ventana» (tamano inicial, minimo y comportamiento responsive) dentro de la capacidad existente; sin cambios en requisitos ya aprobados.

## Impact

- `ui/Main.java` (arranque y minimos) — sin cambios de servicio ni modelo.
- `ui/Historico.fxml`, `ui/Configuracion.fxml`, `ui/Editor.fxml` (layout).
- Tests: ampliacion del smoke test de UI para cargar todas las vistas con la ventana a 800x600.
- Sin cambios de datos, PDF ni servicios.
