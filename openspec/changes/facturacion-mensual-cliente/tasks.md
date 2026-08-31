## 1. UI del diálogo

- [x] 1.1 Crear `GenerarFacturasMensuales.fxml` y `GenerarFacturasMensualesController.java` con los campos de cliente, año, mes inicio/fin, serie, día, IVA, retención y tabla de líneas; verificar que la vista carga sin errores ejecutando `UiSmokeTest` o la aplicación.
- [x] 1.2 Añadir el acceso al diálogo desde `MenuPrincipal.fxml`/`MenuPrincipalController.java` y desde `Historico.fxml`/`HistoricoController.java`; verificar que ambos botones abren el diálogo.
- [x] 1.3 Rellenar los desplegables de cliente, serie, tipo de IVA, tipo de retención y meses con los datos de la empresa actual; verificar que las listas se cargan correctamente al abrir el diálogo.
- [x] 1.4 Permitir elegir el día del mes de tres formas: día fijo editable, primer día del mes o último día del mes; verificar que el spinner se desactiva al seleccionar primer/último día.
- [x] 1.5 Generar el prototipo HTML del diálogo en `prototipos/generar-facturas-mensuales.html` y validar el diseño con el usuario antes de seguir.

## 2. Lógica de negocio

- [x] 2.1 Crear `FacturacionMensualService.java` con el método `generar(...)` que, dado cliente, año, rango de meses, serie, modo de día, IVA, retención y líneas, cree una factura por mes; verificar con un test unitario que genera el número esperado de facturas.
- [x] 2.2 Implementar la detección de duplicados por cliente, mes y año y el modo `DiaMode` (FIJO, PRIMER_DIA, ULTIMO_DIA); verificar con tests que se detectan los meses duplicados y que los modos de día producen las fechas correctas.
- [x] 2.3 Implementar el ajuste del día fijo al último día válido del mes cuando no exista; verificar con un test para febrero con día 31.
- [x] 2.4 Aplicar el IVA y la retención seleccionados a cada línea de cada factura; verificar con un test de totales que el importe final cuadra.
- [x] 2.5 Implementar el checkbox "Añadir mes" para que se añada ` - mes de {nombre}` al final de la descripción; verificar con un test que enero y febrero producen descripciones distintas.

## 3. Transacción, confirmación de duplicados e integración

- [x] 3.1 Ejecutar la generación dentro de una transacción SQLite con rollback en caso de error; verificar con un test que, si la segunda factura falla, no queda ninguna factura guardada.
- [x] 3.2 Mostrar un diálogo de confirmación cuando haya meses con facturas existentes, permitiendo al usuario generar todas las facturas (incluidos los duplicados) o cancelar; verificar manualmente con un año que tenga algunos meses ya facturados.
- [x] 3.3 Conectar el controller con `FacturacionMensualService` y cerrar el diálogo tras generar; verificar que las facturas aparecen en el histórico y se pueden exportar a PDF.
- [x] 3.4 Ejecutar la suite completa con `mvn test` y confirmar que todos los tests siguen en verde.

## 4. Ajustes de UI, anulación/borrado, exportación múltiple y huecos de numeración

- [x] 4.1 Corregir el foco azul del botón "Generar facturas mensuales" en el menú principal tras abrir el diálogo.
- [x] 4.2 Agrandar la ventana del diálogo de generación mensual para que los radio buttons del día se lean correctamente y la tabla de líneas no ocupe todo el espacio.
- [x] 4.3 Separar en `Historico.fxml` el botón "Borrar/Anular" en dos botones: "Anular" y "Borrar".
- [x] 4.4 Implementar `EstadoService.anularFacturas(...)` con resumen de anuladas, ya anuladas y fallos; añadir test `EstadoServiceTest`.
- [x] 4.5 Implementar `FacturaService.borrarFactura(...)` con borrado físico de factura, versiones y líneas; registrar el número liberado en `numero_disponible`; añadir test.
- [x] 4.6 Crear la migración `007_numeros_disponibles.sql` y el repositorio `NumeroDisponibleRepository`.
- [x] 4.7 Implementar `NumeroService.huecosDisponibles(...)` y la pregunta en el editor de facturas nuevas para usar el hueco o continuar con el siguiente número.
- [x] 4.8 Añadir menú contextual en la tabla del histórico con las opciones "Exportar a PDF", "Anular facturas seleccionadas" y "Borrar facturas seleccionadas".
- [x] 4.9 Implementar la exportación múltiple a PDF con opción a un PDF por factura o un único PDF agrupado.
- [x] 4.10 Mostrar confirmación y resumen tras anular o borrar, y refrescar la tabla del histórico.
- [x] 4.11 Ejecutar la suite completa con `mvn test` y confirmar que todos los tests siguen en verde.

## 5. Huecos de numeración en facturación mensual

- [x] 5.1 Implementar `NumeroService.proponerNumeros(...)` para obtener una lista ordenada de correlativos, rellenando huecos primero.
- [x] 5.2 Modificar `FacturacionMensualService.generar(...)` para aceptar `usarHuecos` y asignar los correlativos propuestos a cada factura generada.
- [x] 5.3 Preguntar en el diálogo de generación mensual si se quieren usar los huecos disponibles, mostrando la lista de números propuestos.
- [x] 5.4 Añadir test que verifique que la generación mensual reutiliza los números de facturas borradas.
- [x] 5.5 Ejecutar la suite completa con `mvn test` y confirmar que todos los tests siguen en verde.

