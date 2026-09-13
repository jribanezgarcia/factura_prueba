## Why

En una instalación nueva la aplicación crea sola una empresa llamada «Comercial Alcazaba» (`Main.prepararDatos`), que es el nombre del primer cliente para el que se hizo. Cualquier otro usuario se la encuentra al abrir por primera vez, con los datos fiscales vacíos y una carpeta `comercial_alcazaba` que no puede renombrar. Además `Main` y `Database` conservan una migración de instalaciones antiguas de un único `facturas.db` que ya no usa nadie y que también bautiza la empresa con ese nombre.

Hoy tampoco hay nada que impida emitir facturas con una empresa sin NIF ni dirección: los datos se guardan en Configuración sin comprobar nada. Una factura sin datos del emisor no es válida, y cuando llegue VeriFactu el NIF de la empresa será imprescindible.

## What Changes

- La aplicación deja de crear «Comercial Alcazaba». En una instalación nueva (sin ninguna empresa) carga la **empresa de demostración** (`Empresa Demo S.L.`, la de `seed_demo.sql`) y la deja preseleccionada, para poder probar el programa nada más instalarlo.
- La pantalla de arranque muestra un texto de ayuda cuando no hay empresas o cuando la única empresa es la de demostración, invitando a crear la empresa propia con «Nueva…» y a completar después sus datos.
- `CargarDemo` separa la carga de la demostración en un método reutilizable; su `main` sigue funcionando igual.
- Se eliminan la migración de instalaciones de un solo archivo, la constante `SLUG_EMPRESA_INICIAL` y sus tests.
- Datos obligatorios de la empresa: nombre o razón social, NIF (válido), dirección, código postal (válido), localidad, provincia, email (válido) y teléfono. La comprobación vive en `ConfigService`.
- Al entrar en una empresa a la que le falten esos datos (desde el arranque, al cambiar de empresa o al restaurar una copia), la aplicación abre Configuración en la sección Empresa en lugar del Menú, con un texto explicativo, el nombre ya rellenado si estaba vacío y la barra de navegación y el botón Volver desactivados (salvo Salir). Las demás secciones de Configuración siguen disponibles.
- «Guardar configuración» exige siempre los datos obligatorios y dice cuáles faltan o no son válidos. Cuando se guardan completos estando en ese modo, la aplicación pasa al Menú.
- Los campos obligatorios de la sección Empresa llevan un asterisco.
- Los datos de demostración pasan a tener un NIF y un código postal que superan la validación, para que la empresa de demostración entre directamente al Menú.
- **BREAKING (datos):** una empresa existente con datos incompletos ya no llega al Menú hasta completarlos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se modifica «Gestión de empresas» (instalación nueva con la demostración, texto de ayuda en el arranque y ejemplo con un nombre genérico) y se añade «Datos obligatorios de la empresa».

## Impact

- `src/main/java/com/alcazaba/facturacion/Main.java` (`start`, `prepararDatos`, `entrarEnMenu`).
- `db/CargarDemo.java` (método `cargar()` reutilizable).
- `db/Database.java` (se quitan `SLUG_EMPRESA_INICIAL` y `migrarInstalacionUnArchivo`); `test/.../db/DatabaseTest.java`.
- `service/ConfigService.java` (datos pendientes).
- `ui/Navegador.java` (`mostrarInicio`), `ui/BarraNavegacion.java`, `ui/ArranqueController.java` + `Arranque.fxml`, `ui/ConfiguracionController.java` + `Configuracion.fxml`, `ui/BackupController.java`.
- `src/main/resources/db/seed_demo.sql` (NIF y CP de la empresa de demostración).
- Tests nuevos de `ConfigService` y ajustes de los tests de interfaz que dependan del flujo.
- Requiere que `excepcion-de-datos` esté archivado: los métodos nuevos se escriben sin `throws SQLException`.
