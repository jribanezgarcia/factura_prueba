## Context

La aplicación actual gestiona una sola empresa con una única BD SQLite en `%APPDATA%\Facturacion\facturas.db` (ver `db/Database.java`, directorio `dataDir` estático). La tabla `preferencias` vive dentro de esa BD, por lo que las preferencias no pueden ser globales sin un mecanismo fuera de ella. El correlativo de las series se guarda como un contador único por serie (`serie.siguiente_correlativo`) sin distinguir años. Motivación en `proposal.md`.

## Goals / Non-Goals

**Goals:**
- Cada empresa con su propia BD SQLite en una carpeta dedicada bajo la carpeta de datos de la aplicación.
- Pantalla de arranque que pida empresa + fecha de trabajo, con la última empresa preseleccionada.
- Correlativo de numeración independiente por año de ejercicio.
- Gestión de empresas (listar, crear, cambiar, eliminar) desde Configuración.
- Preferencias globales (ventana, tema, última empresa) fuera de la BD de empresa.

**Non-Goals:**
- No filtrar el Histórico por año de trabajo (el histórico sigue mostrando todas las facturas).
- No crear una capacidad OpenSpec nueva: el cambio vive dentro de `invoicing`.
- No compartir datos entre empresas (clientes, series, facturas quedan aislados por BD).
- No es un cambio de instalador: la ubicación sigue siendo `%APPDATA%\Facturacion`.

## Decisions

### Casa en BD por empresa y cambio de directorio activo

Se mantiene `Database.java` como gestor de una única conexión, pero el `dataDir` pasa a ser la subcarpeta de la empresa activa. `Database` gana una raíz fija `BASE_DATA_DIR = %APPDATA%\Facturacion` y un método para fijar la subcarpeta activa (`setEmpresaActiva(slug)` → `dataDir = BASE_DATA_DIR/<slug>`), reutilizando `setDataDir`/`resetConnection` existentes. Como los repositorios piden la conexión a `Database.getConnection()` en cada operación y `getConnection()` ya invoca `Migrations.migrate()`, cambiar de empresa solo exige `setEmpresaActiva` + `resetConnection`; los repositorios no cachean conexión.

Alternativa descartada: una única BD con columna `empresa_id` en todas las tablas. Se descarta porque acoplaria modelo y repositorios, obligaría a reescribir cada query y elimina el aislamiento físico deseado.

### Preferencias globales en un archivo de propiedades

Se introduce un archivo `%APPDATA%\Facturacion\preferencias.properties` con las preferencias compartidas entre empresas: `ultima_empresa`, datos de ventana (`ventana_x`, `ventana_y`, `ventana_w`, `ventana_h`) y `tema`. El resto de preferencias (`color_pdf`, `ultima_carpeta_export`, `ultima_serie`) permanece en la tabla `preferencias` de cada BD de empresa, como hoy.

Alternativa descartada: BD SQLite maestra global. Añade complejidad sin beneficio para unas pocas claves; un `.properties` es suficiente y legible.

### Catálogo de empresas (slug → nombre)

Un segundo archivo `%APPDATA%\Facturacion\empresas.properties` mapea `slug.nombre=Nombre visible` y sirve de catálogo para listar empresas sin abrir cada BD. El `slug` se genera normalizando el nombre (minúsculas, espacios y acentos a `_`/letras base, caracteres no ASCII eliminados). `Database.getEmpresasDisponibles()` lista las subcarpetas con `facturas.db`; las que no tengan entrada en el catálogo se muestran igualmente con el slug como nombre provisional.

### Correlativo por año: tabla `serie_siguiente`

Nueva migración SQL (004):
```sql
ALTER TABLE serie DROP COLUMN siguiente_correlativo; -- no; SQLite antiguo no permite DROP COLUMN en todas las versiones
```
Decisión: **no se elimina** `serie.siguiente_correlativo` (compatibilidad SQLite). Se crea una tabla auxiliar:

```sql
CREATE TABLE serie_siguiente (
  serie_id INTEGER NOT NULL REFERENCES serie(id),
  anio INTEGER NOT NULL,
  siguiente INTEGER NOT NULL DEFAULT 1,
  PRIMARY KEY (serie_id, anio)
);

-- Poblar el año actual con el valor configurado de cada serie
INSERT INTO serie_siguiente (serie_id, anio, siguiente)
SELECT id, CAST(strftime('%Y', 'now') AS INTEGER), COALESCE(NULLIF(siguiente_correlativo, 0), 1)
FROM serie;
```

- `SerieRepository` gana `getSiguiente(serieId, anio)`, `actualizarSiguiente(serieId, anio, siguiente)` y filtra `correlativosAnuladas`/`correlativosActivos` por año (extraído de `factura_version.fecha_factura`).
- `NumeroService` pasa a recibir el año (o la fecha) en `siguienteCorrelativo(serie, fecha)` y usa la tabla; la reutilización de anulados consulta solo los correlativos anulados/activos de ese año.
- En Configuración → Series, el «siguiente número» mostrado y editable corresponde al año de trabajo de la sesión.

Alternativa considerada: derivar el siguiente correlativo como `MAX(correlativo)+1` de las facturas activas del año. Se descarta porque complica la edición manual del siguiente número (requisito ya existente) y la reutilización de anulados.

### Sesión de trabajo (empresa + fecha)

Nueva clase `service/Sesion` (estado por sesión) que guarda `empresaSlug` y `fechaTrabajo`. Se inicializa al confirmar la pantalla de arranque y se usa para:
- Fecha por defecto de las nuevas facturas (el editor la toma de `Sesion.fechaTrabajo()`).
- Año del correlativo propuesto al crear una factura nueva.
- Mostrar/editar el «siguiente número» en Configuración.

El año que se usa para reservar correlativos se toma de la **fecha de la factura** al guardar/crear (no de la sesión), porque si el usuario cambia la fecha dentro del editor a otro año, el número debe recalcularse con el correlativo de ese año (escenario existente «El año sigue a la fecha»). La sesión solo aporta el **valor inicial**.

### Pantalla de arranque y flujo de apertura

Nueva vista `Arranque.fxml` + `ArranqueController`, cargada por `Main` antes del menú principal:
1. `EmpresaManager.listarEmpresas()` → lista; `PreferenciasGlobales.get("ultima_empresa")` → preselección.
2. El usuario elige el **año del ejercicio fiscal** (un `ComboBox<Integer>` con años alrededor del actual; por defecto el año en curso).
3. La **fecha de trabajo**:
   - Si el ejercicio es el año en curso → se fija automáticamente a la fecha del sistema y el `DatePicker` queda deshabilitado.
   - Si el ejercicio es otro año → el `DatePicker` se habilita para elegir a mano y se restringe su calendario a las fechas de ese ejercicio.
4. Un botón **«Nueva...»** permite crear una empresa nueva desde el propio arranque sin salir de la pantalla; la empresa recién creada queda seleccionada.
5. Al confirmar: `EmpresaManager.conectar(slug, fecha)` → `Database.setEmpresaActiva(slug)` → `resetConnection()` → (migraciones automáticas) → `Sesion` se inicializa → se carga el menú principal.
6. Navegación posterior (menú → pantallas) no vuelve a mostrar el selector.

El año de trabajo de la sesión coincide siempre con el año de la `fechaTrabajo` (el arranque impide elegir una fecha fuera del ejercicio), por lo que los consumidores del año (`ConfiguracionController.anioTrabajo`, correlativo por año) derivan el ejercicio de `Sesion.fechaTrabajo().getYear()` sin necesidad de un campo adicional.

En el menú principal, al abrirse, se solicita el foco del fondo de la escena (`Scene.getRoot().requestFocus()` en `Platform.runLater`) para que el primer botón de menú no quede resaltado por el foco inicial de JavaFX.

La comprobación de instancia única pasa a usar un lock global en `BASE_DATA_DIR` (una instancia de la aplicación a la vez, independiente de la empresa elegida), porque antes de elegir empresa aún no existe un `dataDir` de empresa que consultar.

### Gestión de empresas en Configuración

Nueva pestaña «Empresas» en `ConfiguracionController`/`Configuracion.fxml`:
- Tabla con el listado (nombre visible + slug).
- «Nueva empresa»: diálogo para el nombre → genera slug → crea la BD (migraciones desde 0) → la añade al catálogo → pasa a ser la empresa activa.
- «Cambiar a esta»: `EmpresaManager.conectar(slug, Sesion.fechaTrabajo())` y recarga la vista principal (los controladores ya cargan sus datos en `alIniciar`/método equivalente).
- «Eliminar» (solo si no es la empresa activa): confirmación explícita; se borra la entrada del catálogo. Se conserva una advertencia de que es preferible una copia de seguridad antes de eliminar.

### Migración de la instalación de un solo archivo a carpetas por empresa

En el arranque, una vez por migración (código, no SQL): si `BASE_DATA_DIR/facturas.db` existe y `BASE_DATA_DIR` no tiene ninguna subcarpeta de empresa, se crea `BASE_DATA_DIR/comercial_alcazaba/`, se mueven `facturas.db` y `facturas.lock`, se registra `comercial_alcazaba.nombre=Comercial Alcazaba` y se fija `ultima_empresa=comercial_alcazaba`. A partir de ahí la app arranca con ese directorio activo.

## Risks / Trade-offs

- **El correlativo por año cambia el contador existente** → la migración 004 siembra `serie_siguiente` con el valor de `siguiente_correlativo` para el año en curso y `NumeroService` respeta siempre los correlativos ocupados del año (consulta `correlativosActivos`), evitando colisiones.
- **Preferencias anteriores (ventana/tema) dejan de leerse de la BD de empresa** → en la primera ejecución tras el cambio se usan valores por defecto si el `.properties` global no existe; el coste es una pérdida puntual de posición/tema, mitigada por ser valores no críticos. Anotado en la sesión de usuario.
- **Eliminar una empresa borra su catálogo** → la BD puede conservarse a mano; la UI advierte y pide confirmación.
- **SQLite en versiones muy antiguas sin `DROP COLUMN`** → no se elimina `serie.siguiente_correlativo`; se deja como columna muerta para compatibilidad.

## Migration Plan

1. Implementar `Database.setEmpresaActiva` + `getEmpresasDisponibles` + catálogo/preferencias `.properties`.
2. Migración SQL 004 (`serie_siguiente`).
3. Migración de datos en arranque (v1 → carpetas por empresa).
4. Verificación manual: abrir la app con la BD existente ya reubicada, crear facturas en 2026 y en 2025, comprobar correlativos independientes.
5. Rollback: `git reset --hard` sobre el commit previo (los `.properties` y la reubicación de la BD son pasos idempotentes y no rompen la instalación previa si se conserva el archivo).

## Open Questions

- Ninguna que afecte a specs, enfoque o tareas. Las decisiones de implementación menores (formato exacto de fecha en SQL, normalización de slug) se resuelven durante la codificación sin cambiar comportamiento observable.