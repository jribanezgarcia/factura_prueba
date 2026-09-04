## Context

Inventario de los textos de botón por pantalla, sacado de los nueve FXML. Ver proposal.md - Why para las inconsistencias detectadas.

## Goals / Non-Goals

**Goals:**

- Que la misma acción se llame igual en todas las pantallas.
- Que las etiquetas sean cortas, para que ninguna barra vuelva a desbordarse.
- Que quede escrito el criterio, para que la próxima pantalla no vuelva a inventarse un nombre.

**Non-Goals:**

- No se cambia ninguna acción ni ningún manejador.
- No se tocan etiquetas de campos de formulario, títulos de sección ni encabezados de tabla: solo botones.
- No se toca la barra de navegación: eso es el change `nav-bar-con-texto`.

## Decisions

### D1. El criterio

1. **La acción, no el objeto.** El objeto lo da la pantalla: en Clientes, «Eliminar» basta.
2. **Un verbo por concepto.** «Eliminar» para destruir, nunca «Borrar». «Nuevo» para crear. «Guardar» para persistir.
3. **«Volver» para salir de una pantalla; «Cancelar» solo en diálogos modales.** Son gestos distintos y conviene que se distingan: «Volver» deja lo hecho, «Cancelar» lo descarta.
4. **Los atajos, en el tooltip.** Una etiqueta no es sitio para «(Supr)».
5. **Una función, un nombre**, se invoque desde donde se invoque.

### D2. Cambios concretos

| Pantalla | Antes | Después |
|---|---|---|
| Histórico | Borrar | Eliminar |
| Histórico | Generar mensual | Generar mensuales |
| Menú principal | Generar facturas mensuales | Generar mensuales |
| Backup | Crear copia de seguridad | Crear copia |
| Backup | Restaurar copia | Restaurar |
| Generar mensuales | Eliminar línea | Eliminar línea *(se queda; el editor se alinea con esta en el change anterior)* |

«Cancelar» en Generar mensuales **se conserva**: es un diálogo modal, y por el criterio 3 es lo correcto. Es el único sitio donde debe aparecer.

En Backup, «Crear copia» y «Restaurar» se entienden por el contexto de la pantalla y de la tarjeta en la que están, que ya se titulan «Copia de seguridad» y «Restaurar una copia».

### D3. Riesgo: tests que busquen por texto

Los tests de UI del proyecto localizan nodos por `fx:id` (`#tablaLineas`, `#lblTotal`) o por clase CSS (`.nav-bar`, `.card-editor`), no por texto de botón. Aun así, antes de cerrar el change hay que hacer una búsqueda de los literales que se cambian por si alguno aparece en un assert, sobre todo en `UiSmokeTest`.

## Verificación

- Buscar en `src/test` cada literal modificado antes de tocar nada.
- Suite completa en verde.
- Repaso visual de las cinco pantallas afectadas, comprobando que ninguna etiqueta queda ambigua fuera de su contexto.
