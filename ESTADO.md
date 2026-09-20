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

Último cambio archivado: `2026-09-19-crear-tablas-sin-versiones`.

## En curso

**Replanteo para simplificar todo el proyecto** (19-20/09/2026). No hay ningún change aplicándose.

- El análisis y las decisiones están en `borrador_changes/analisis-desde-cero.md`.
- Decidido hasta ahora: se quitan las versiones de factura; VeriFactu se hará más adelante en otra rama y hasta entonces todo lo que choca con él se queda igual; el negocio pasa a llevar el SQL dentro (sin DAO); las clases de datos validan en sus setters; solo se usa `Exception`; `Dialogos` vuelve a ser la clase que se usa en clase; las pantallas de tabla usan formulario modal reutilizable.
- Quedan por decidir los últimos detalles de código (reparto de `Facturas` y `Series`, recorrer listas, textos, constantes, paquetes y comentarios).

**Changes escritos que ya no valen**, pendientes de borrar: `mvc-como-biblioteca8` y `negocio-dentro-del-modelo`. Se rehacen con el enfoque por módulos.

## Qué toca ahora

1. Terminar las preguntas de estructura y estilo del código.
2. Reescribir `AGENTS.md` con esas decisiones.
3. Escribir y aplicar los changes por módulos, en este orden: especificación sin píxeles → estructura → clientes → empresas y menú → configuración → facturas → editor → histórico → PDF → mensuales → copias → documentación.
4. VeriFactu, después, en otra rama.

## Trampas conocidas

- **Maven no está en el PATH.** Usar `C:\Users\juan\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd`.
- **No lanzar los tests con la aplicación abierta**: se mezclan las clases de `target` y salen errores falsos.
- **`mvn clean` puede fallar** al borrar `target`; borrar la carpeta a mano y ejecutar `mvn test`.
- **En OpenSpec, un requisito `MODIFIED` reemplaza el bloque entero**: hay que copiar todos sus escenarios, aunque solo cambie una frase.
- **Los datos se van a reiniciar** mientras el programa esté en desarrollo: no hace falta migrar nada.
