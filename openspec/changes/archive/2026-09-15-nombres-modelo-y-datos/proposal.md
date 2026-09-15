## Why

Tras `paquetes-en-espanol`, los paquetes ya siguen la forma de Biblioteca8, pero las clases de reglas y de datos conservan nombres en inglés o mezclados: `FacturaService`, `ClienteRepository`, `EmpresaManager`, `Database`, `Migrations`, `ValidationException`, `Servicios`… En Biblioteca8 la clase de negocio se llama en plural (`Libros`), el contenedor se llama `Modelo` y la conexión `Conexion`, con métodos en español (`establecerConexion`, `cerrarConexion`).

Este es el segundo de los tres changes de renombrado: **capa de modelo, datos y copia de seguridad**.

## What Changes

- **Reglas** (`cabofactu.modelo.negocio`): en plural, como `Libros`. `FacturaService` → `Facturas`, `ClienteService` → `Clientes`, `EmpresaManager` → `Empresas`, `NumeroService` → `Numeracion`, `CalculoService` → `Calculos`, etc. `ValidationException` → `ValidacionException`.
- **Datos** (`cabofactu.modelo.negocio.sqlite`): sufijo `DAO`. `FacturaRepository` → `FacturaDAO`, `LineaRepository` → `LineaFacturaDAO`, `VersionRepository` → `VersionFacturaDAO`, `IvaRepository` → `TipoIvaDAO`, `ConfigRepository` → `ConfiguracionDAO`, `CopiaRepository` → `CopiaSeguridadDAO`, etc.
- `Database` → `Conexion` y `Migrations` → `Migraciones`, con sus métodos públicos en español (`getConnection` → `establecerConexion`, `resetConnection` → `cerrarConexion`, `beginTransaction` → `iniciarTransaccion`, `migrate` → `migrar`…).
- `Servicios` → `Modelo`. Sus campos pasan a `private` con getter y se llaman como la clase que guardan (`factura` → `getFacturas()`, `ivas` → `getTiposIva()`, `backup` → `getCopiaSeguridad()`…). En las pantallas, la variable `servicios` pasa a `modelo`, `Vista.setServicios` pasa a `setModelo` y `Navegador.servicios()` pasa a `modelo()`.
- `BackupService` → `fichero.CopiaSeguridad` y su `ResumenBackup` → `ResumenCopia`.
- `FacturacionMensualService.DiaMode` → `FacturacionMensual.ModoDia`.
- `model.FacturaVersion` → `VersionFactura` y `HistorialFila` → `FilaHistorial`.
- Los campos y parámetros que repiten el nombre viejo se renombran igual (`facturaRepository` → `facturaDAO`, `numeroService` → `numeracion`).
- Los tests siguen a su clase (`FacturaServiceTest` → `FacturasTest`, `DatabaseTest` → `ConexionTest`…).
- Ningún cambio visible: mismos textos, mismos mensajes de error, mismas tablas y ficheros.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. Solo cambian nombres internos.

## Impact

- Todas las clases de `cabofactu.modelo`, `cabofactu.modelo.dominio`, `cabofactu.modelo.negocio`, `cabofactu.modelo.negocio.sqlite` y `cabofactu.fichero`, con sus tests.
- Uso de esas clases desde `cabofactu` (`Main`, `PreparacionDatos`, `InstanciaUnica`), `cabofactu.vista.*` y `cabofactu.pdf`.
- `cargar_demo.bat` no cambia (`CargarDemo` conserva su nombre).
- Fuera: nombres de clases de `vista`, `pdf` y `utilidades` (tercer change); `docs/` y `README.md` (tercer change); textos que ve el usuario; esquema de la base; nombres de métodos de reglas y DAO que ya están en español.
- **Depende de** `paquetes-en-espanol` archivado.
