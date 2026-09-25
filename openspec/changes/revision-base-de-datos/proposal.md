## Why

Escribiendo las pruebas de pantalla de clientes salió que la aplicación **acepta dos clientes con el mismo NIF sin decir nada**. En la base de datos, `cliente.nif` es `TEXT` a secas, sin `NOT NULL` ni `UNIQUE`, y ni `Clientes.alta` ni `Clientes.modificar` comprueban nada. Y eso a pesar de que `Cliente.equals` compara por el NIF: para el modelo de datos, esos dos clientes son el mismo.

Tirando del hilo apareció algo peor. `Facturas.crearFactura` y `Facturas.guardarEditada` llaman a `Clientes.alta` **cada vez que el cliente no trae id**, sin mirar si ya existe. Facturar a un cliente escrito a mano y luego editar esa factura deja **dos filas** del mismo cliente. En `FacturasTest`, `clientePrueba()` acaba insertando el mismo cliente tres veces en un solo test.

Con los tipos de IVA y de retención pasa lo mismo con el nombre: se pueden repetir. Las series sí lo comprueban («Ya existe una serie con el código A»).

Este change es la primera mitad de la revisión de la base de datos: **las tablas maestras**. La segunda mitad, fundir `factura` y `factura_version` en una sola tabla, va en `modulo-facturas`, porque obliga a reescribir el negocio de las facturas.

## What Changes

- **El NIF del cliente, único**: `nif TEXT NOT NULL UNIQUE`. La ficha lo comprueba antes de cerrarse y, si lo tiene otro cliente, marca el NIF en rojo y dice de quién es. El negocio lo vuelve a comprobar al guardar.
- **Un cliente dado de baja se recupera en vez de duplicarse**: si se da de alta un NIF que tiene un cliente inactivo, la ficha ofrece volver a darlo de alta con los datos recién escritos. Se recupera la misma ficha, con sus facturas.
- **Facturar ya no duplica clientes**: si el cliente escrito a mano ya existe por su NIF, la factura se asocia a él. Su ficha no se toca: la factura guarda su propia copia de los datos.
- **Nombres únicos en los tipos de IVA y de retención**, con la misma comprobación en su ficha.
- **`CHECK (x IN (0, 1))`** en las columnas de sí o no de las tablas maestras.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Clientes» (NIF único, recuperar un cliente dado de baja, facturar sin duplicar) e «IVA» (nombres únicos en los tipos de IVA y de retención).

## A qué afecta

- **Base de datos**: `db/crear_tablas.sql` (tablas `cliente`, `serie`, `tipo_iva` y `tipo_retencion`).
- **Negocio**: `Clientes` (`buscarPorNif` y la comprobación en `alta` y `modificar`), `TiposIva` y `TiposRetencion` (`buscarPorNombre` y su comprobación), `Facturas` (un privado que reutiliza el cliente por su NIF).
- **Controlador y modelo**: tres operaciones nuevas de una línea.
- **Pantallas**: `FichaClienteController`, `ClientesController`, `FichaTipoIvaController` y `FichaTipoRetencionController`.
- **Tests**: `ClientesTest`, `FacturasTest`, `TiposIvaTest`, `TiposRetencionTest`, `PantallaClientesTest`, `PantallaIvaTest` y `PantallaRetencionesTest`, y los que den de alta dos veces el mismo NIF.
- **Queda fuera**: todo lo de las tablas de facturas (`factura`, `factura_version` y `factura_linea`), que va en `modulo-facturas`. La tabla `preferencias` se queda como está, por decisión del usuario. Y `serie.sufijo_fecha` no se renombra: la especificación nombra ese campo en «Numeración por series», y modificar un requisito de veinte escenarios por un nombre de columna no compensa.
