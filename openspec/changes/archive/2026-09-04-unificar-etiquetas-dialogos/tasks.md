> Los textos exactos, sitio por sitio, están en `design.md`, sección «D2. Textos
> concretos». Usarlos tal cual.

## 1. Comprobación previa

- [x] 1.1 Buscar en `src/test` los literales «Borrar» y «Borrar/Anular» por si algún test los assertea.
- [x] 1.2 Anotar aquí los tests afectados, si los hay.

> **Resultado:** ningún test assertea texto de diálogo «Borrar» ni «Borrar/Anular». Las 5 coincidencias son datos/métodos internos: `EmpresaManagerTest:88,92` (nombre de empresa de prueba «Para Borrar»), `FacturacionMensualServiceTest:290` y `FacturaServiceTest:219,230` (llamadas a `facturaService.borrarFactura(...)`). Nada que tocar.

## 2. Histórico

- [x] 2.1 En `HistoricoController`, cambiar el título `"Borrar"` por `"Eliminar"` en los cuatro diálogos del flujo de eliminación (líneas 234, 250, 253 y 277).
- [x] 2.2 En el cuerpo del mensaje de confirmación, «Se van a borrar físicamente» pasa a «Se van a eliminar físicamente».
- [x] 2.3 El título `"Borrar/Anular"` del flujo de anulación (líneas 203, 210, 223 y 226) pasa a `"Anular"`: ese camino no elimina nada, crea una versión en estado Anulada, y el nombre actual sugiere lo contrario.

## 3. Configuración

- [x] 3.1 `"Borrar serie"` pasa a `"Eliminar serie"`, y «¿Seguro que deseas borrar la serie…?» a «¿Seguro que deseas eliminar la serie…?».
- [x] 3.2 «no puede borrarse» pasa a «no puede eliminarse» y «No se pudo borrar la serie» a «No se pudo eliminar la serie».
- [x] 3.3 En el diálogo de eliminar empresa, «Se borrará físicamente su carpeta de datos» pasa a «Se eliminará físicamente su carpeta de datos».

## 4. Lo que NO se toca

- [x] 4.1 Confirmar que no se han renombrado `btnBorrar`, `borrarSeleccionadas()` ni `itemBorrar`: son nombres internos, no texto visible.
- [x] 4.2 Confirmar que los diálogos de Copias siguen titulados «Restaurar copia»: el criterio pide el mismo verbo que el botón, y lo es.

> **4.1:** `btnBorrar` (`HistoricoController:60`), `itemBorrar` (`:155-157`), `borrarSeleccionadas()` (`:231`) intactos; solo cambió texto visible. **4.2:** `BackupController` titula sus 6 diálogos «Restaurar copia», mismo verbo que el botón «Restaurar».

## 5. Especificación

- [x] 5.1 MODIFIED «Criterio de etiquetado de botones»: el criterio SHALL alcanzar también al título y al cuerpo de los diálogos que abre cada botón, que SHALL usar el mismo verbo. Escenario nuevo del recorrido completo botón + confirmación en el Histórico.

## 6. Verificación final

- [x] 6.1 Buscar «borrar» en los literales de `src/main` y comprobar que solo quedan identificadores de código, nunca texto visible.
- [x] 6.2 Suite completa en verde con `mvn clean test`.
- [x] 6.3 A mano: eliminar facturas desde el Histórico y anular facturas, comprobando que cada diálogo dice lo mismo que su botón de principio a fin.

> **6.1:** quedan 17 «borrar» en `src/main`, todos identificadores (`borrarFactura`, `borrarFisico`, `borrarRecursivo`, `borrarDiario`, `btnBorrar`, `itemBorrar`, `borrarSeleccionadas`, `fx:id="btnBorrar"`, variable `borradas`, comentario en `FacturaRepository:54`) más un literal visible extra no listado en D2: el resumen «Borradas: N» (`HistoricoController:272`), que pasaba a «Eliminadas: N» por coherencia con el cuerpo del diálogo ya cambiado.
