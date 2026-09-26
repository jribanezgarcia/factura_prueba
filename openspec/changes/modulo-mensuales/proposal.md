## Why

La facturación mensual es el siguiente módulo en `main`. `GenerarFacturasMensualesController` tiene 557 líneas y `FacturacionMensual` 230, y no cumplen las normas:

| Qué | Hoy |
|---|---|
| Clases anónimas | 7 `StringConverter`; seis sobran porque `Cliente`, `Serie`, `TipoIva` y `TipoRetencion` ya tienen `toString()` |
| Clases dentro de otras | `LineaDialogo` en el controlador (una copia de la línea de factura con `Property` y constructor vacío); `ModoDia`, `LineaPlantilla` (otra copia, con constructor vacío) y `Resultado` en `FacturacionMensual` |
| Estilo | ~10 ternarios, un nombre de clase completo, `generar()` de 121 líneas, `StringBuilder`, lambdas con bloque en `setOnEditCommit` |
| Ventana | Se abre con su propio `FXMLLoader` y una ruta escrita entera, no con `Vista.crearVentanaModal` como las fichas |
| Negocio | `FacturacionMensual` no es singleton; tiene dos `generar`, una comprobación repetida que nunca se cumple y la opción «omitir los meses repetidos», que la ventana no usa |
| Código muerto | `generarDuplicados ? cantidadMeses : cantidadMeses` |

Y dos cosas que no encajan:

- **El año sale del reloj del ordenador**, no del año de trabajo elegido al arrancar.
- **El aviso de números libres engaña**: «Cancelar» no cancela, genera sin usarlos.

Además, la decisión F13 (19/09) pedía **una sola casilla «Añadir mes»** para todas las líneas, en lugar de una por línea.

## What Changes

- **Siempre en el año de trabajo**: el diálogo no deja elegir el año; lo enseña.
- **Una casilla «Añadir mes»** para todas las líneas. Las líneas son `LineaFactura` (cantidad, descripción y precio): fuera `LineaDialogo` y `LineaPlantilla`.
- **`PlantillaMensual`**: una clase de datos que se valida sola y reúne todo lo que se elige en el diálogo. `generar` recibe un objeto en lugar de doce parámetros.
- **`ModoDia`** en su propio fichero; **`FacturacionMensual`** como singleton, sin `Resultado` ni la opción de omitir meses.
- **Números libres con dos botones**, como en el editor: `Usar los números libres` y `Continuar sin ellos`.
- **Meses con factura, como hoy**: aviso con Aceptar (genera todos, también los repetidos) y Cancelar (no genera nada).
- **La ventana** se abre con `Vista.crearVentanaModal`, sus desplegables usan `toString()`, la tabla de líneas usa las celdas de JavaFX con `onEditCommit` en el FXML, y el botón del histórico pasa a decir «Facturar mes», como el menú y la especificación.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Facturación mensual por cliente» (año de trabajo, casilla única, números libres con dos botones y tres escenarios nuevos).

## A qué afecta

- **Nuevo**: `modelo/dominio/PlantillaMensual.java`, `modelo/dominio/ModoDia.java`, `Dialogos.mostrarDialogoNumerosLibres` y `Formatos.nombreMes`.
- **Se rehacen**: `modelo/negocio/FacturacionMensual.java`, `vista/controlador/GenerarFacturasMensualesController.java` y `vista/recursos/GenerarFacturasMensuales.fxml`.
- **Cambian**: `Controlador`, `Modelo`, `MenuPrincipalController` y `HistoricoController` (cómo se abre), `Historico.fxml` (texto del botón) y `ConfiguracionVentana` (si su entrada deja de usarse).
- **Tests**: `PlantillaMensualTest` (nuevo), `FacturacionMensualTest` y `PantallaMensualesTest`.
- **Queda fuera**: avisar de cambios sin guardar al cerrar el diálogo (no guarda un registro: genera facturas) y el PDF, que irá con Jasper.
