## 1. Planificación OpenSpec

- [x] 1.1 Crear change `fix-reutilizar-numeros-borrados`.
- [x] 1.2 Redactar `proposal.md`, `spec.md`, `design.md`.

## 2. Implementación

- [x] 2.1 Modificar `NumeroService.siguienteCorrelativo(...)` para usar `huecosDisponibles` como primera opción.
- [x] 2.2 Añadir test `reutilizaNumeroBorrado` en `NumeroServiceTest`.
- [x] 2.3 Añadir tests `noReutilizaHuecoOcupadoPorActiva` y `priorizaHuecoBorradoSobreAnulado` en `NumeroServiceTest`.

## 3. Validación

- [x] 3.1 Ejecutar `mvn test` y corregir fallos.

## 4. Cierre OpenSpec

- [x] 4.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 4.2 Archivar el change `fix-reutilizar-numeros-borrados`.
- [x] 4.3 Actualizar `CONTINUAR_MAÑANA.md`.
- [x] 4.4 Hacer commit y push.
