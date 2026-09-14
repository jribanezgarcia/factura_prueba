## Why

Los nombres del proyecto mezclan inglés y español: paquetes `db`, `model`, `repository`, `service`, `ui` y `util`, junto a clases como `Servicios`, `Sesion` o `Navegador`. Además, el paquete raíz `com.alcazaba.facturacion` lleva el nombre del primer cliente.

El objetivo es que el código se parezca a Biblioteca8, el proyecto de referencia del usuario: `biblioteca.modelo.dominio`, `modelo.negocio`, `modelo.negocio.mysql`, `vista`, `vista.controlador`, `vista.recursos`, `vista.utilidades`, `fichero` y `utilidades`.

El renombrado se hace en tres changes seguidos. Este es el primero: **solo mueve paquetes y recursos, sin cambiar el nombre de ninguna clase**. Así el diff es casi todo `package`/`import` y rutas, y es fácil revisar.

## What Changes

- El paquete raíz pasa de `com.alcazaba.facturacion` a `cabofactu`.
- Paquetes nuevos:
  - `model` → `modelo.dominio`
  - `service` → `modelo.negocio`
  - `repository` y `db` → `modelo.negocio.sqlite`
  - `ui` → `vista`, `vista.controlador` y `vista.utilidades`
  - `util` → `utilidades`
  - `pdf` se mantiene
- `Servicios` pasa a `cabofactu.modelo` y `BackupService` a `cabofactu.fichero`, porque en Biblioteca8 esos papeles los tienen `modelo.Modelo` y `fichero.CopiaSeguridadXML`.
- Recursos: FXML a `cabofactu/vista/recursos/`, temas CSS a `cabofactu/vista/recursos/temas/` e imágenes a `cabofactu/vista/recursos/imagenes/`. Se actualizan todas las rutas de texto y la referencia `@../images/…` de `Arranque.fxml`.
- Los tests se mueven al paquete de la clase que prueban.
- `Dialogos.setImpl` y `Dialogos.restoreDefault` pasan a `public`, porque los tests de controladores dejan de estar en el mismo paquete que `Dialogos`.
- `pom.xml` (`groupId` y `mainClass`), `cargar_demo.bat` y la ruta del logo en `README.md` apuntan a los paquetes nuevos.
- Ningún cambio visible ni de datos: la carpeta `%APPDATA%\Facturacion` y las bases no dependen del paquete.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. Es una reorganización interna sin cambio de comportamiento; los specs de `openspec/specs/` no citan paquetes ni clases.

## Impact

- Todos los ficheros de `src/main/java`, `src/test/java`, `src/main/resources/com/alcazaba/facturacion` y `src/test/resources/com/alcazaba/facturacion`.
- `pom.xml`, `cargar_demo.bat`, `README.md` (solo la ruta del logo).
- Fuera: el nombre de las clases (changes `nombres-modelo-y-datos` y `nombres-vista-pdf-utilidades`), `docs/tecnico.md` y `docs/metodologia.md` (se actualizan en el tercer change), `src/main/resources/db/` (se carga por ruta de classpath que no depende del paquete) y los changes archivados.
