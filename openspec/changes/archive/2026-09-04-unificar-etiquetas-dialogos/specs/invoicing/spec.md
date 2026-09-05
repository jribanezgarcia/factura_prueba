## MODIFIED Requirements

### Requirement: Criterio de etiquetado de botones

Las etiquetas de los botones de la aplicación SHALL seguir un criterio único en todas las pantallas, de modo que una misma acción se llame siempre igual.

El botón SHALL nombrar la **acción**, dejando que el objeto lo aporte la pantalla en la que está: en la pantalla de Clientes, «Eliminar» ya significa eliminar el cliente seleccionado.

Se SHALL usar un único verbo por concepto. En particular, la acción destructiva SHALL llamarse siempre «Eliminar» y SHALL NOT llamarse «Borrar» en ninguna pantalla.

El criterio SHALL alcanzar también al **texto de los diálogos** que abre cada botón: el título y el cuerpo del mensaje SHALL usar el mismo verbo que el botón desde el que se llega, de modo que el usuario no tenga que decidir si dos palabras distintas nombran la misma acción justo antes de confirmarla. El título de un diálogo SHALL NOT nombrar una acción que ese flujo no realiza.

«Volver» SHALL usarse para salir de una pantalla conservando lo realizado. «Cancelar» SHALL usarse únicamente en diálogos modales, donde el gesto descarta lo que se estaba componiendo.

Los atajos de teclado SHALL indicarse en el tooltip del botón y SHALL NOT formar parte del texto de la etiqueta.

Una misma función SHALL tener el mismo nombre desde cualquier punto de entrada.

Las etiquetas SHALL ser lo bastante cortas como para que las barras de acciones no necesiten menú de desbordamiento en el tamaño mínimo de ventana.

#### Scenario: La acción destructiva se llama igual en todas partes
- **WHEN** el usuario compara el botón de eliminar del Histórico con el de la pantalla de Clientes
- **THEN** ambos dicen «Eliminar», y ninguna pantalla usa «Borrar»

#### Scenario: El diálogo dice lo mismo que el botón
- **WHEN** el usuario pulsa «Eliminar» en el Histórico y aparece la confirmación
- **THEN** el título y el cuerpo del diálogo hablan de eliminar, no de borrar

#### Scenario: El título del diálogo no nombra acciones que no ocurren
- **WHEN** el usuario anula facturas desde el Histórico
- **THEN** el diálogo se titula «Anular» y no menciona borrar, porque la anulación crea una versión nueva y no elimina nada

#### Scenario: Volver frente a Cancelar
- **WHEN** el usuario está en una pantalla principal
- **THEN** el botón de salida dice «Volver»
- **AND** «Cancelar» solo aparece en diálogos modales como el de generación mensual

#### Scenario: Los atajos no van en la etiqueta
- **WHEN** un botón tiene un atajo de teclado asociado
- **THEN** el atajo se indica en su tooltip y la etiqueta contiene solo el nombre de la acción

#### Scenario: Una función, un nombre
- **WHEN** el usuario abre la generación de facturas mensuales desde el Menú principal y desde el Histórico
- **THEN** el botón se llama igual en los dos sitios
