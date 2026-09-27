## Why

Las copias de seguridad son el último módulo que queda por rehacer en `main`, y con él se termina la transición a las normas de `AGENTS.md`. Hoy el módulo tiene cuatro piezas que no las cumplen:

| Pieza | Hoy |
|---|---|
| `fichero/CopiaSeguridad` | Un `record` dentro (`ResumenCopia`), un `Clock` y el DAO y `Facturas` recibidos por el constructor. `Modelo` la crea con `new` |
| `CopiaSeguridadDAO` | El último DAO. Las tablas y columnas que se exigen a una copia están escritas a mano, en **dos** listas de tablas casi iguales y un mapa de ~80 columnas que hay que tocar con cada cambio del esquema |
| `DatosException` | La última excepción propia, usada por el DAO y por `Conexion` |
| `CopiaSeguridadController` | 306 líneas: tres `Task` con `new Thread`, clases anónimas, un `Object[]` para devolver dos cosas, ternarios y el `getModelo()` provisional del `Controlador` |

`Conexion` tampoco las cumple: cuatro métodos de transacción que no usa nadie, un stream con `::`, un ternario y una `IllegalStateException`.

Y tres cosas que no encajan con cómo se usa el programa:

- **Crear una copia son dos pasos** (elegir la carpeta y pulsar «Crear copia»), y cada vez hay que buscar la carpeta desde el principio.
- **Todas las copias se llaman `facturas_…db`**, sea cual sea la empresa: en una misma carpeta no se distingue de quién es cada una.
- **La regla «empresa activa vacía sin NIF» ya no puede darse**: desde que volvió el bloqueo de la empresa incompleta, sin NIF no se llega a la pantalla de copias.

Además, la decisión F12 (19/09) pedía que, al crear una empresa desde una copia, «¿Cambiar a ella?» **vuelva al arranque** con esa empresa elegida, en vez de abrirla por detrás.

## What Changes

- **Un solo botón «Crear copia…»**: abre el selector de carpetas en la última carpeta usada y crea la copia nada más elegirla. La carpeta se recuerda en las preferencias generales, la misma para todas las empresas.
- **El nombre lleva la empresa**: `demo_20260927_103015.db` (carpeta de la empresa, fecha y hora), con `_2`, `_3`… si ya existe.
- **La copia se comprueba contra el script de tablas**: se crea una base vacía en memoria con `crear_tablas.sql` y se compara con la copia. Fuera las listas escritas a mano.
- **Reemplazar solo con el mismo NIF**: fuera la excepción de la empresa vacía. La regla pasa al negocio y se prueba.
- **«¿Cambiar a ella?» vuelve al arranque** con la empresa nueva elegida; si no, se queda en Copias.
- **`CopiaSeguridad`** pasa a ser un singleton de `modelo/negocio` con el SQL dentro; **`ResumenCopia`**, una clase de datos de `modelo/dominio`. Desaparecen `CopiaSeguridadDAO`, `DatosException`, el `Clock`, el paquete `fichero` y `Controlador.getModelo()`.
- **La pantalla**, sin hilos: cada botón hace su trabajo con el cursor de espera, como el histórico.
- **`Conexion`** entera según las normas.
- **`AGENTS.md`**: se quita el paquete `fichero` y el apartado «Transición» deja de apuntar pendientes.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Copia de seguridad» (botón único con carpeta recordada, nombre con la empresa, estructura según el script, reemplazar solo con el mismo NIF, vuelta al arranque, y tres escenarios nuevos).

## A qué afecta

- **Nuevo**: `modelo/dominio/ResumenCopia.java` y `modelo/negocio/CopiaSeguridad.java` (sustituye a `fichero/CopiaSeguridad.java`).
- **Se borran**: `fichero/CopiaSeguridad.java`, `modelo/negocio/sqlite/CopiaSeguridadDAO.java` y `modelo/negocio/sqlite/DatosException.java`.
- **Se rehacen**: `modelo/negocio/sqlite/Conexion.java`, `vista/controlador/CopiaSeguridadController.java` y `vista/recursos/CopiaSeguridad.fxml`.
- **Cambian**: `Controlador`, `Modelo`, `Empresas` (recordar la última empresa), `PreferenciasGlobales` (la carpeta de las copias) y `AGENTS.md`.
- **Tests**: `CopiaSeguridadTest` (pasa a `modelo/negocio` y absorbe `CopiaSeguridadDAOTest`), `ResumenCopiaTest` (nuevo), `ConexionTest` y `PantallaCopiasTest`.
- **Queda fuera**: restaurar desde la pantalla de arranque, limpiar la carpeta `copias_previas` y copiar los PDF o el logo.
