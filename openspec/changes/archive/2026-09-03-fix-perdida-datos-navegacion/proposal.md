## Why

`Navegador.mostrar()` carga el FXML y hace `stage.setScene(...)` **sin consultar nunca `Vista.puedeCerrar()`**. La comprobación de cambios sin guardar existe, pero solo la hacen `Main.cerrarAplicacion()` (al cerrar la ventana) y tres métodos del propio editor.

Todos los demás caminos de navegación se la saltan:

- los seis botones de `BarraNavegacion` (Menú principal, Nueva factura, Histórico, Clientes, Configuración, Copia de seguridad), que llaman a `nav.mostrar(...)` directamente;
- `MenuController`, con sus cinco entradas;
- `HistoricoController.abrirVersion()` y `VersionesController.abrirVersion()`;
- `BackupController` y `ClientesController` al volver al menú.

**Reproducción:** rellenar media factura en el editor y pulsar cualquier icono de la barra superior. La factura se pierde sin ningún aviso. El spec ya exige lo contrario: «Si hay cambios sin guardar y el usuario pulsa Volver o cierra la aplicación, la aplicación SHALL pedir confirmación».

La causa de que el agujero exista es que la guarda está repetida en los sitios que llaman, en lugar de estar en el único sitio por el que pasan todos. Hoy la tienen tres métodos de `EditorController` (`volver()`, `nuevaFactura()` y `verVersiones()`) y nadie más.

## What Changes

- `Navegador.mostrar()` SHALL consultar `puedeCerrar()` de la vista actual antes de cambiar de escena, y no navegar si devuelve `false`. Pasa a ser la única puerta de la navegación.
- `Navegador` guarda una referencia a la vista actual, que hoy solo conoce `Main` a través de `setOnVistaCambio`.
- `mostrar()` devuelve `null` cuando la navegación se cancela. Los tres sitios que usan el valor de retorno comprueban el `null`.
- Se **eliminan** las comprobaciones de `puedeCerrar()` de `EditorController.volver()`, `nuevaFactura()` y `verVersiones()`, que a partir de ahora serían redundantes y harían salir el diálogo de cambios sin guardar **dos veces**.
- `Main.cerrarAplicacion()` no se toca: el cierre de ventana no pasa por `mostrar()`, así que no hay doble pregunta.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía el requisito «Cambios sin guardar» para que la confirmación cubra cualquier navegación, no solo Volver y el cierre de la aplicación.

## Impact

- `ui/Navegador`: campo de vista actual y guarda en `mostrar()`.
- `ui/EditorController`: se quitan tres comprobaciones que pasan a estar centralizadas.
- `ui/HistoricoController:187`, `ui/VersionesController:98` y `ui/EditorController:1173`: comprobar `null` en el resultado de `mostrar()`.
- Tests de UI: los que dejan el editor con cambios y luego navegan pasarán por el diálogo. Se resuelve con `Dialogos.setImpl(...)`, que ya existe para eso.
- No se toca el esquema, ni los servicios, ni el PDF.

### Fuera de alcance

`ConfiguracionController` no implementa `puedeCerrar()` y no tiene ningún indicador de «modificado», así que sus datos de empresa editados y no guardados se siguen perdiendo al navegar. Añadirle seguimiento de cambios es trabajo aparte —hay que instrumentar todos los campos del formulario— y se deja para su propio change. Este cambio le da el punto de enganche: en cuanto implemente `puedeCerrar()`, la guarda nueva lo cubre sin tocar nada más.
