## 1. Servicio

- [x] 1.1 `FacturaService.guardarEditada`: sobrecarga con `comoNuevaVersion` que fuerza `crearVersion`
- [x] 1.2 Test: guardar como nueva versión deja v1 intacta y crea v2 exportable

## 2. Interfaz

- [x] 2.1 `Dialogos`: enum `ModoGuardarVersion` + método en `Impl` (default mapea al confirmar antiguo) + diálogo de tres botones
- [x] 2.2 `EditorController`: usar el nuevo diálogo y pasar el modo al servicio

## 3. Verificación

- [x] 3.1 Suite completa `mvn test` en verde
