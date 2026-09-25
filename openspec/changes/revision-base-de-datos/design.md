## Situación de partida

| Pieza | Hoy |
|---|---|
| `cliente.nif` | `TEXT` a secas: admite vacíos y repetidos |
| `Clientes.alta` y `modificar` | No comprueban si el NIF ya existe |
| `Facturas.crearFactura` y `guardarEditada` | El mismo bloque de tres líneas en las dos: si el cliente no trae id, `Clientes.alta`. Nunca miran si ya existe |
| `tipo_iva.nombre` y `tipo_retencion.nombre` | Admiten repetidos, y el negocio no los comprueba |
| Columnas de sí o no | `INTEGER` sin límite: admiten un 7 |
| La ficha de serie | La única que ya comprueba repetidos (`Series.comprobarCodigo`) |

## Objetivos y lo que queda fuera

**Objetivo**: que en las tablas maestras no pueda haber dos clientes con el mismo NIF ni dos tipos con el mismo nombre, que lo impida la base de datos y no solo el código, y que facturar deje de crear clientes repetidos.

**Fuera**: las tablas de facturas (`factura`, `factura_version` y `factura_linea`), que se rehacen en `modulo-facturas`; la tabla `preferencias`, que se queda como está; el nombre de la columna `serie.sufijo_fecha`, que la especificación nombra en «Numeración por series».

---

## D1. El esquema

Las cuatro tablas maestras de `db/crear_tablas.sql` quedan así. Lo que cambia es el `NOT NULL UNIQUE` del NIF, los dos `UNIQUE` de los nombres y los `CHECK`:

```sql
CREATE TABLE IF NOT EXISTS cliente (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  nif TEXT NOT NULL UNIQUE,
  direccion TEXT,
  cp TEXT,
  localidad TEXT,
  provincia TEXT,
  activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
  email TEXT
);

CREATE TABLE IF NOT EXISTS serie (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  codigo TEXT NOT NULL UNIQUE,
  descripcion TEXT,
  es_rectificativa INTEGER NOT NULL DEFAULT 0 CHECK (es_rectificativa IN (0, 1)),
  sufijo_fecha TEXT NOT NULL DEFAULT 'MES' CHECK (sufijo_fecha IN ('MES', 'ANIO', 'NINGUNO'))
);

CREATE TABLE IF NOT EXISTS tipo_iva (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE,
  porcentaje INTEGER,
  motivo_exencion TEXT,
  activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
  es_suplido INTEGER NOT NULL DEFAULT 0 CHECK (es_suplido IN (0, 1))
);

CREATE TABLE IF NOT EXISTS tipo_retencion (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL UNIQUE,
  porcentaje INTEGER NOT NULL,
  activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
);
```

El `CHECK` de `sufijo_fecha` limita el formato a los tres valores de `FormatoNumero`: con otro texto, `Series.crearSerie` fallaría al hacer `FormatoNumero.valueOf`.

Los nombres se comparan **tal cual**: «IVA 21%» e «iva 21%» son distintos, igual que para la base de datos. El NIF no tiene ese problema porque `Cliente.setNif` lo guarda siempre en mayúsculas.

Las columnas no cambian, así que `CopiaSeguridadDAO` no se toca.

---

## D2. `Clientes`: buscar por NIF y comprobar

```java
/** El cliente que tiene ese NIF, esté activo o no, o null si no lo tiene ninguno. */
public Cliente buscarPorNif(String nif) throws Exception {
    String consulta = "SELECT id, nombre, nif, direccion, cp, localidad, provincia, email, activo "
            + "FROM cliente WHERE nif = ?";
    try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
        sentencia.setString(1, nif);
        try (ResultSet filas = sentencia.executeQuery()) {
            if (filas.next()) {
                return crearCliente(filas);
            }
            return null;
        }
    } catch (SQLException e) {
        throw new Exception("Error SQLite: " + e.getMessage());
    }
}

/** Ningún otro cliente puede tener este NIF. Al modificar, el propio cliente no cuenta. */
private void comprobarNif(Cliente cliente) throws Exception {
    Cliente otro = buscarPorNif(cliente.getNif());
    if (otro != null && !otro.getId().equals(cliente.getId())) {
        throw new Exception(String.format("Ya existe un cliente con el NIF %s: %s.",
                otro.getNif(), otro.getNombre()));
    }
}
```

`alta` y `modificar` llaman a `comprobarNif(cliente)` justo después de su guarda de `null`. En un alta el cliente aún no tiene id, así que `otro.getId().equals(null)` da `false` y cualquier otro cliente cuenta.

---

## D3. `Facturas`: facturar sin duplicar clientes

Los dos bloques iguales de `crearFactura` y `guardarEditada` se sustituyen por una llamada a un privado:

```java
/**
 * Si el cliente se ha escrito a mano, usamos el que ya tiene ese NIF o, si no
 * hay ninguno, lo damos de alta. Su ficha no se toca: la factura guarda su
 * propia copia de los datos.
 */
private void asegurarCliente(Cliente cliente) throws Exception {
    if (cliente == null || cliente.getId() != null || isVacio(cliente)) {
        return;
    }
    Cliente existente = Clientes.getClientes().buscarPorNif(cliente.getNif());
    if (existente != null) {
        cliente.setId(existente.getId());
        return;
    }
    cliente.setId(Clientes.getClientes().alta(cliente));
}
```

El objeto `cliente` conserva lo que se escribió: solo recibe el id. Por eso la factura guarda los datos tal como se escribieron y la ficha del cliente no cambia.

---

## D4. La ficha del cliente

La comprobación se hace **en la ficha, antes de cerrarla**, para poder marcar el NIF en rojo y no perder lo escrito. La ficha solo consulta: quien guarda sigue siendo la pantalla. Es lo mismo que ya hace `FichaTipoIvaController` al preguntar `tipoIvaEnUso`.

`guardar` construye el cliente en una variable local, lo comprueba y solo entonces lo pasa a `registro`:

```java
@FXML
void guardar(ActionEvent event) {
    String error = marcarCamposMalos();
    if (error != null) {
        Dialogos.mostrarDialogoError("Datos del cliente", error);
        return;
    }
    try {
        Cliente cliente;
        if (original == null) {
            cliente = clienteDeLosCampos();
        } else {
            cliente = actualizarRegistro();
        }
        if (!nifLibre(cliente)) {
            return;
        }
        registro = cliente;
        cerrarVentana(event);
    } catch (Exception e) {
        Dialogos.mostrarDialogoError("Datos del cliente", e.getMessage());
    }
}

/**
 * Miramos si otro cliente tiene ya este NIF. Si es uno dado de baja y estamos
 * dando de alta, ofrecemos recuperarlo: el cliente pasa a llevar su id y vuelve
 * a estar activo. Devolvemos false si no se puede guardar.
 */
private boolean nifLibre(Cliente cliente) throws Exception {
    Cliente otro = Vista.getInstancia().getControlador().buscarClientePorNif(cliente.getNif());
    if (otro == null || otro.getId().equals(cliente.getId())) {
        return true;
    }
    if (original == null && !otro.isActivo()) {
        boolean recuperar = Dialogos.mostrarDialogoConfirmacion("Cliente dado de baja", String.format(
                "El cliente %s, con el NIF %s, está dado de baja.%n%n"
                        + "¿Quieres volver a darlo de alta con los datos que acabas de escribir?",
                otro.getNombre(), otro.getNif()));
        if (recuperar) {
            cliente.setId(otro.getId());
            cliente.setActivo(true);
        }
        return recuperar;
    }
    txtNif.getStyleClass().add("campo-error");
    Dialogos.mostrarDialogoError("Datos del cliente", String.format(
            "Ya existe un cliente con el NIF %s: %s.", otro.getNif(), otro.getNombre()));
    return false;
}
```

Recuperar solo se ofrece **al dar de alta**. Al editar, poner el NIF de otro cliente, esté activo o no, es un error: fundir dos fichas no es algo que deba hacerse sin querer.

Si el usuario no acepta recuperarlo, la ficha sigue abierta con lo que había escrito.

En `ClientesController.anadirCliente`, un cliente recuperado ya trae id, así que se guarda como modificación:

```java
Cliente nuevo = ficha.getRegistro();
if (nuevo != null) {
    // Si la ficha ha recuperado un cliente dado de baja, ya trae su id.
    if (nuevo.getId() == null) {
        Vista.getInstancia().getControlador().altaCliente(nuevo);
    } else {
        Vista.getInstancia().getControlador().modificarCliente(nuevo);
    }
    refrescarTabla();
}
```

---

## D5. Los tipos de IVA y de retención

**Negocio**: `TiposIva` y `TiposRetencion` reciben el mismo par que `Clientes`, con sus columnas y su mapeador de siempre (`crearTipoIva`, `crearTipoRetencion`):

- `buscarPorNombre(String nombre)`: el tipo con ese nombre, o `null`.
- `comprobarNombre(tipo)`, privado, que llaman `alta` y `modificar` tras su guarda de `null`. Mensajes: «Ya existe un tipo de IVA con el nombre %s.» y «Ya existe un tipo de retención con el nombre %s.».

**Fichas**: `FichaTipoIvaController` y `FichaTipoRetencionController` cambian su `guardar` igual que la ficha del cliente (variable local, comprobar, y después `registro`), con un privado `nombreLibre`:

```java
/** Miramos si otro tipo de IVA tiene ya este nombre. Devolvemos false si no se puede guardar. */
private boolean nombreLibre(TipoIva tipo) throws Exception {
    TipoIva otro = Vista.getInstancia().getControlador().buscarTipoIvaPorNombre(tipo.getNombre());
    if (otro == null || otro.getId().equals(tipo.getId())) {
        return true;
    }
    txtNombre.getStyleClass().add("campo-error");
    Dialogos.mostrarDialogoError("Datos del tipo de IVA", String.format(
            "Ya existe un tipo de IVA con el nombre %s.", otro.getNombre()));
    return false;
}
```

En la de retención, lo mismo con `TipoRetencion`, `buscarTipoRetencionPorNombre` y el título «Datos del tipo de retención».

---

## D6. Controlador y Modelo

Tres operaciones nuevas, de una línea en cada uno, como todas:

| Operación | Llama a |
|---|---|
| `buscarClientePorNif(String nif)` | `Clientes.getClientes().buscarPorNif(nif)` |
| `buscarTipoIvaPorNombre(String nombre)` | `TiposIva.getTiposIva().buscarPorNombre(nombre)` |
| `buscarTipoRetencionPorNombre(String nombre)` | `TiposRetencion.getTiposRetencion().buscarPorNombre(nombre)` |

---

## D7. Tests

**Negocio**, contra base temporal:

- `ClientesTest`: `buscarPorNif` encuentra también a un inactivo; alta con NIF repetido falla con su mensaje; modificar poniendo el NIF de otro falla; modificar sin tocar el NIF funciona (el propio cliente no cuenta).
- `FacturasTest`: dos facturas con el mismo cliente escrito a mano dejan **una** fila en `cliente`, y las dos facturas apuntan a ella.
- `TiposIvaTest` y `TiposRetencionTest`: nombre repetido falla; modificar con su propio nombre funciona.

**Pantalla**:

- `PantallaClientesTest`: alta con `B88888888` (lo tiene «Cliente Ejemplo S.L.» en la demo) → aviso con su nombre, NIF en rojo y la ficha sigue abierta. Y recuperar: dar de baja a un cliente con facturas (queda inactivo), dar de alta otro con su NIF, aceptar → la tabla sigue con el mismo número de clientes y ese vuelve a estar activo.
- `PantallaIvaTest` y `PantallaRetencionesTest`: nombre repetido → aviso y nombre en rojo.

**Los tests que hoy dan de alta dos veces el mismo NIF** fallarán con el `UNIQUE`: hay que darles NIF distintos. `mvn test` dice cuáles.

---

## Decisiones

| Decisión | Por qué |
|---|---|
| La comprobación va en la ficha **y** en el negocio | La ficha, para marcar el campo en rojo sin cerrarse ni perder lo escrito. El negocio, porque `Facturas` y cualquier otro camino también dan de alta clientes. Y debajo de los dos, el `UNIQUE` de la base de datos |
| Un NIF de un cliente inactivo se ofrece recuperar, solo al dar de alta | Decisión del usuario (24/09): es el mismo cliente, y así conserva su historial. Al editar sería fundir dos fichas |
| Facturar reutiliza el cliente por su NIF y no toca su ficha | La regla de siempre: nunca se cambia la ficha de un cliente sin preguntar. La factura tiene su copia |
| Los nombres de los tipos se comparan tal cual | Es lo que hace la base de datos con `UNIQUE`; normalizar mayúsculas sería otra regla que explicar |
| `sufijo_fecha` no se renombra | La especificación nombra ese campo en «Numeración por series», que tiene veinte escenarios. Un `MODIFIED` tan grande por un nombre no compensa |

## Riesgos y renuncias

- **Las bases que ya existen no reciben las restricciones**: `crear_tablas.sql` usa `CREATE TABLE IF NOT EXISTS`. Hay que borrar `%APPDATA%\Facturacion` antes de probar; los datos de hoy son de prueba.
- **Una copia de seguridad con NIF repetidos no se podrá restaurar**: el `UNIQUE` la rechazará. Las copias de hoy son de datos de prueba.
- **La ficha de serie hace esta misma comprobación de otra forma**: cierra, avisa y se reabre con `reintentarCon`. Queda así en este change; unificarla con las otras tres sería otro change.
