## Why

`EditorController` tiene 1.797 líneas y es lo último de las pantallas de facturas que no se ha rehecho. Funciona, pero no se puede defender en clase:

| Qué | Hoy |
|---|---|
| Estilo | Unos 45 ternarios, tres clases anónimas (dos `StringConverter` y un `Task`), un `Thread` propio para el PDF, `instanceof` con variable de patrón y un nombre de clase completo en medio del código |
| Restos | Código de depuración del foco (`DIAGNOSTICO_FOCO` con `System.out`), un comentario que cita una tarea y separadores de sección |
| Celda de la descripción | ~180 líneas que buscan piezas internas del `TextArea` y atan su altura con `Bindings` para que crezca renglón a renglón |
| Aviso del hueco | Un `ChoiceDialog` construido en Java, fuera de `Dialogos`, que además **pisa el número escrito a mano** |
| Cliente | Si se elige un cliente y luego se le cambia el NIF en la factura, el editor sigue creyendo que es el mismo: al guardar puede convertir su ficha en la de otro cliente. Un cliente nuevo escrito a mano entra en la lista sin avisar |

Y usa cuatro piezas que tampoco cumplen las normas: `ResumenFactura.IvaGrupo` (una clase dentro de otra), `new LineaFactura()` (constructor vacío), `ValidacionCliente` (repite los `Cliente.errorX()` que ya usa la ficha) y `Reloj` (decidido quitarlo en E5).

## What Changes

- **`EditorController` rehecho** con `Editor.fxml`, en un único controlador ordenado como un índice: métodos cortos, sin nada de lo que prohíbe `AGENTS.md`, y las celdas de la tabla de líneas dentro, explicadas con su «Cómo funciona».
- **La descripción, más sencilla**: al escribir, un cuadro de tres renglones; al terminar, la fila crece y enseña todo el texto.
- **El NIF identifica al cliente de la factura.** Mismo NIF que el cliente elegido: si cambian otros datos, pregunta si actualizar su ficha, como hoy. Otro NIF: es otro cliente; si ya está en la lista se enlaza con él, y si no, **avisa de que se guardará en la lista**, con Aceptar y Cancelar.
- **El número libre se ofrece al guardar con dos botones** (`Usar A-2` / `Continuar con A-6`), desde un método nuevo de `Dialogos`, y solo si el número es el que propuso la aplicación: el escrito a mano se respeta.
- **Exportar a PDF sin hilos**: en el propio botón, con cursor de espera.
- **Las cuatro piezas de fuera**: `GrupoIva` en su propio fichero, `LineaFactura` con constructor de datos y setters que validan, fuera `ValidacionCliente` y fuera `Reloj` (la fecha de trabajo se pide al `Controlador`).
- **Las dos pruebas de pantalla pendientes** (número ocupado escrito a mano y el aviso del número libre) y las del comportamiento nuevo.
- **Limpieza de la especificación**: fuera cuatro notas de trabajo que se colaron en cuatro requisitos, las barras invertidas de «Numeración» pasan a acentos graves, y una referencia al requisito del PDF con su nombre de antes.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Búsqueda de clientes al crear factura» (el NIF identifica al cliente y aviso del cliente nuevo), «Líneas de factura» (escenario de la descripción larga), «Numeración de facturas por series» (el número libre al guardar y el número a mano), y «Desglose de totales por tipo de IVA» y «Suplidos en la facturación» (solo se quita la nota).
- `pdf-rendering`: «Orden del desglose en el PDF» y «Suplidos en el PDF» (solo se quita la nota y se corrige el nombre de un requisito citado).

## A qué afecta

- **Se rehace**: `vista/controlador/EditorController.java`.
- **Nuevo**: `modelo/dominio/GrupoIva.java`, `vista/utilidades/ConversorCliente.java` y `Dialogos.mostrarDialogoNumeroLibre`.
- **Cambian**: `LineaFactura`, `ResumenFactura`, `Calculos`, `Facturas`, `FacturacionMensual`, `Sesion`, `Modelo`, `Controlador`, el PDF (`ConstructorDocumentoFactura`, donde usa `IvaGrupo`) y, solo por la fecha de trabajo, `ConfiguracionController`, `MenuPrincipalController` y `GenerarFacturasMensualesController`.
- **Se borran**: `ValidacionCliente` y `Reloj`.
- **Tests**: `PruebaDePantalla` (dos ayudas nuevas), `PantallaEditorTest` y los que construyen líneas.
- **Queda fuera**: el `Clock` de `CopiaSeguridad` (va con `modulo-copias`), el resto de pantallas y el aspecto del editor, que no cambia.
