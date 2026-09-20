# CaboFactu: estado del proyecto

Actualizado: **20/09/2026**

Por dónde va el proyecto y qué toca ahora. Las normas de cómo se escribe el código y cómo se trabaja están en [AGENTS.md](AGENTS.md).

## Dónde está cada cosa

| Qué | Dónde |
|---|---|
| Qué hace la aplicación (fuente de verdad) | `openspec/specs/` |
| Cambios en curso | `openspec/changes/` |
| Cambios terminados | `openspec/changes/archive/` |
| Normas de código y flujo de trabajo | `AGENTS.md` |
| Estado (este fichero) | `ESTADO.md` |
| Notas de trabajo y decisiones sin publicar | `borrador_changes/` (fuera de git; en `viejo/` la auditoría y las pruebas visuales de agosto) |
| Documentación técnica | `docs/tecnico.md`, `README.md` |

El historial de lo que hizo cada cambio no se escribe aquí: está en `openspec/changes/archive/` y en `git log`.

## Hecho

- Renombrado completo al estilo Biblioteca8: paquete `cabofactu`, clases y paquetes en español.
- NIF con un aviso distinto por caso y datos de cliente obligatorios, comprobados al guardar.
- Tema de apariencia por empresa.
- Tablas creadas con un único script (`db/crear_tablas.sql`), sin migraciones ni versiones de esquema.

Último cambio archivado: `2026-09-16-crear-tablas-sin-versiones`.

## En curso

**Replanteo para simplificar todo el proyecto** (19-20/09/2026). No hay ningún change aplicándose.

- El análisis y las decisiones están en `borrador_changes/analisis-desde-cero.md`.
- **Decisiones cerradas** (20/09): sin versiones de factura; VeriFactu más adelante en otra rama y hasta entonces todo lo que choca con él se queda igual; negocio con el SQL dentro (sin DAO); clases de datos que se validan en sus setters; solo `Exception`; `Dialogos` como en clase; pantallas de tabla con formulario modal reutilizable; `Factura` con `Serie`, `Cliente` y sus líneas dentro. Todas están en `AGENTS.md`.

Change escrito y pendiente de aplicar: **`especificacion-sin-pixeles`** — retira de la especificación los 20 requisitos que solo describen apariencia y los resume en uno, «Apariencia de la interfaz». No toca código.

Borrados por quedar obsoletos: `mvc-como-biblioteca8` y `negocio-dentro-del-modelo`.

## Qué toca ahora

1. Aplicar y archivar `especificacion-sin-pixeles`.
2. Escribir y aplicar los changes por módulos, en este orden: estructura → clientes → empresas y menú → configuración → facturas → editor → histórico → PDF → mensuales → copias → documentación.
3. VeriFactu, después, en otra rama.

## Trampas conocidas

- **Maven no está en el PATH.** Usar `C:\Users\juan\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd`.
- **No lanzar los tests con la aplicación abierta**: se mezclan las clases de `target` y salen errores falsos.
- **`mvn clean` puede fallar** al borrar `target`; borrar la carpeta a mano y ejecutar `mvn test`.
- **En OpenSpec, un requisito `MODIFIED` reemplaza el bloque entero**: hay que copiar todos sus escenarios, aunque solo cambie una frase.
- **No traduzcas las palabras clave de OpenSpec** (`ADDED`/`MODIFIED`/`REMOVED Requirements`, `Requirement:`, `Scenario:`, `WHEN`/`THEN`, y `## Why` y `## What Changes` del proposal): `validate` sigue diciendo «is valid» y el requisito desaparece al archivar.
- **Los datos se van a reiniciar** mientras el programa esté en desarrollo: no hace falta migrar nada.
