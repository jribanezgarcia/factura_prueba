## Why

Clientes es el primer módulo de datos y el que fija el patrón: como salga `Cliente`, saldrán `Serie`, `TipoIva`, `TipoRetencion` y `Empresa`. Hoy no se parece a nada de lo que manda `AGENTS.md`:

- `Cliente` es una clase de datos **sin validar**: constructor vacío y setters que asignan lo que les den. Quien decide si un cliente vale es `ValidacionCliente`, una clase aparte con un método por campo.
- Hay **dos clases para una tabla**: `Clientes` (que solo reenvía) y `ClienteDAO` (que tiene el SQL), con `SELECT *` y una excepción propia, `DatosException`.
- `Clientes` **no es singleton** y se construye en `Modelo` pasándole el DAO; `Facturas` recibe además el mismo DAO por su constructor y lo usa por su cuenta.
- `Controlador` y `Modelo` **no tienen operaciones de cliente**: la pantalla llega hasta el negocio con `getControlador().getModelo().getClientes().insertar(c)`.
- La **ficha de cliente se construye en Java**: un `Dialog` con su `GridPane`, sus `ColumnConstraints`, un `addEventFilter` y un `setResultConverter`, dentro del mismo controlador de la pantalla. Son más de 200 líneas de interfaz escrita a mano, justo lo que `AGENTS.md` prohíbe.

## What Changes

- **`Cliente` se valida solo**: constructor con los datos obligatorios, setters que comprueban y lanzan `Exception` con el mensaje para el usuario, constructor copia, `equals`, `hashCode`, `toString` y getters de texto.
- **`Clientes` pasa a ser el singleton con el SQL dentro** (`Clientes.getClientes()`), con los métodos `alta`, `baja`, `modificar`, `buscar` y `listado`. **`ClienteDAO` desaparece.**
- **`Controlador` y `Modelo` repiten las operaciones**: `altaCliente`, `bajaCliente`, `modificarCliente`, `buscarCliente`, `listadoClientes`, `desactivarCliente` y `clienteTieneFacturas`.
- **La ficha de cliente pasa a FXML**: `FichaCliente.fxml` con su `FichaClienteController`, abierto en modo añadir con `setRegistro(null)` y en modo editar con `setRegistro(registro)`, y recogido con `getRegistro()`.
- **`ClientesController` se rehace** con el patrón de tabla + formulario: campo `registro`, `seleccionar`, `refrescarTabla` y un `catch (Exception e)` por botón.
- **Las columnas usan `PropertyValueFactory`** apoyadas en los getters de texto de `Cliente`.
- **Preguntar antes de tocar la ficha del cliente**: al guardar una factura cuyos datos de cliente difieran de los de su ficha, la aplicación pregunta si actualizarla. Hoy, una factura nueva no la toca y una factura editada la sobrescribe sin avisar.
- **Los demás sitios que usaban `ClienteDAO`** (`Facturas`, `Estados`, `Rectificativas`) pasan por `Clientes`, sin cambiar lo que hacen.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Clientes» añade que el NIF se guarda en mayúsculas; «Búsqueda de clientes al crear factura» deja de actualizar la ficha del cliente en silencio y pasa a preguntar.

## A qué afecta

- **Nuevo**: `vista/controlador/FichaClienteController`, `vista/recursos/FichaCliente.fxml`.
- **Se borra**: `modelo/negocio/sqlite/ClienteDAO`.
- **Se rehacen**: `modelo/dominio/Cliente`, `modelo/negocio/Clientes`, `vista/controlador/ClientesController`, `vista/recursos/Clientes.fxml`.
- **Cambian de plumbing, sin cambiar de comportamiento**: `modelo/Modelo`, `controlador/Controlador`, `modelo/negocio/Facturas`, `modelo/negocio/Estados`, `modelo/negocio/Rectificativas`, `vista/controlador/EditorController`, `vista/controlador/GenerarFacturasMensualesController`.
- **Tests**: se rehace `ValidacionClienteTest` como `ClienteTest`, se añade `ClientesTest`, y se adaptan `FacturasTest`, `EstadosTest`, `HistorialTest` y `FacturacionMensualTest` al constructor de `Facturas` sin DAO.
- **Queda fuera** (módulos siguientes): `ValidacionCliente` y `ValidacionException` siguen vivos porque los usan el Editor, la generación mensual y las rectificativas, que se rehacen en los módulos 5 y 6. El bloque Cliente del Editor sigue como está. Las demás tablas siguen con su DAO.
