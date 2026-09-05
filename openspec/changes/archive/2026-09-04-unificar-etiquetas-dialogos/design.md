## Context

El criterio vigente dice: «la acción destructiva SHALL llamarse siempre "Eliminar" y SHALL NOT llamarse "Borrar" en ninguna pantalla». En la práctica se aplicó solo a los botones, porque el requisito habla de «etiquetas de los botones». Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que el botón y el diálogo que abre digan lo mismo.
- Que el criterio quede escrito de forma que la próxima pantalla no vuelva a separarse.

**Non-Goals:**

- No se renombran identificadores de código.
- No se cambia ningún comportamiento, ni el orden de los botones de los diálogos.
- No se tocan los diálogos de Copias, cuyos títulos («Restaurar copia») son descriptivos y correctos aunque el botón diga «Restaurar».

## Decisions

### D1. El criterio se aplica al par botón + diálogo

Un diálogo de confirmación es la continuación del botón que lo abrió: si el botón dice «Eliminar» y la ventana dice «Borrar», el usuario tiene que decidir si son la misma cosa justo antes de una acción irreversible.

La regla que se añade al requisito: el título y el cuerpo del diálogo SHALL usar el mismo verbo que el botón que lo abre.

### D2. Textos concretos

| Sitio | Antes | Después |
|---|---|---|
| `HistoricoController:234, 250, 253, 277` | Título `"Borrar"` | `"Eliminar"` |
| `HistoricoController:254` | «Se van a borrar físicamente N factura(s)» | «Se van a eliminar físicamente N factura(s)» |
| `ConfiguracionController:847` | `"Borrar serie"` / «¿Seguro que deseas borrar la serie…?» | `"Eliminar serie"` / «¿Seguro que deseas eliminar la serie…?» |
| `ConfiguracionController:837` | «no puede borrarse» | «no puede eliminarse» |
| `ConfiguracionController:854` | «No se pudo borrar la serie» | «No se pudo eliminar la serie» |
| `ConfiguracionController:938` | «Se borrará físicamente su carpeta de datos» | «Se eliminará físicamente su carpeta de datos» |

### D3. `"Borrar/Anular"` no es un título, son dos flujos

`HistoricoController:203-226` usa `"Borrar/Anular"` como título en el camino de **anulación**, que no elimina nada: crea una versión nueva en estado Anulada. Llamarlo «Borrar/Anular» sugiere que podría borrar, justo lo contrario de lo que hace.

Pasa a `"Anular"`, que es la acción real de ese flujo. El camino de eliminación tiene sus propios diálogos (`:234-277`) y se queda con `"Eliminar"`.

### D4. Los de Copias se quedan como están

`BackupController` titula sus diálogos `"Restaurar copia"` mientras el botón dice «Restaurar». No es incoherencia: el botón nombra la acción y el objeto lo da la pantalla, y el título del diálogo puede ser más explícito. El criterio pide el mismo **verbo**, y lo es.

## Verificación

- Buscar «borrar» en los literales de `src/main` y comprobar que solo quedan identificadores de código, nunca texto visible.
- A mano: en el Histórico, seleccionar facturas y pulsar Eliminar, comprobando que el diálogo dice «Eliminar» de principio a fin; repetir con Anular.
- A mano: en Configuración, intentar eliminar una serie con facturas y una sin ellas.
