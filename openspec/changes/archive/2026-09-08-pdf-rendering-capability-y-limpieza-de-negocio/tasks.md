> Decisiones y sus porqués: `design.md`.
>
> **El criterio de aceptación no se negocia (D4):** los 33 tests de
> `PdfServiceTest` pasan **sin modificar ni una línea**. Si uno falla, el refactor
> está mal: en este change nada puede alterar el documento. No relajes una
> aserción, no actualices un número esperado, no comentes un test. Para y dilo.
>
> **No corrijas nada de maquetación** aunque lo veas roto. Anótalo y sigue.

## 1. Fotografía de partida

- [x] 1.1 Pasar `mvn test` y anotar que los 33 tests de `PdfServiceTest` están en verde antes de tocar nada.
- [x] 1.2 Exportar a una carpeta temporal un PDF de cada caso representativo: factura simple, con dos tipos de IVA, con descuento global, con retención, con suplidos, de varias páginas y anulada. Guardar esos PDF como referencia byte a byte para comparar al final.
- [x] 1.3 Anotar el número de páginas de cada uno.

## 2. `totalConIva`: comprobar antes de mover

- [x] 2.1 Leer `CalculoService` y comprobar si ya existe un método que calcule el total con IVA de una línea (`totalLinea`, o equivalente).
- [x] 2.2 Si ya existe y da el mismo resultado: borrar `totalConIva(...)` de `PdfService.java:664` y llamar al de `CalculoService`. **No añadas un método nuevo.**
- [x] 2.3 Si no existe o difiere en el redondeo: añadirlo a `CalculoService` replicando exactamente el comportamiento actual, incluido el redondeo. Documentar la diferencia si la hay.
- [x] 2.4 Test unitario del método en `CalculoServiceTest`, con al menos un tipo normal, uno exento y un suplido.
- [x] 2.5 Pasar `PdfServiceTest`. Debe seguir en verde.

## 3. `suplidosDe`: el criterio vuelve al dominio

- [x] 3.1 Comprobar si `TipoIva` ya sabe responder si un tipo es suplido. Si lo sabe, el filtro se resuelve donde toque sin criterio duplicado.
- [x] 3.2 Retirar `suplidosDe(...)` de `PdfService.java:577` y obtener la lista ya filtrada desde la capa de servicio.
- [x] 3.3 Cuidado con el caso de **todas las líneas suplidos**: hoy la tabla se imprime igualmente con solo la cabecera. Ese comportamiento no cambia.
- [x] 3.4 Test del filtrado en la capa de servicio: sin suplidos, solo suplidos, y mezcla.
- [x] 3.5 Pasar `PdfServiceTest`. Debe seguir en verde.

## 4. `retencionDeVersion`: decidir método o campo

- [x] 4.1 Leer `FacturaService.VersionCompleta` y `FacturaVersion` y decidir según `design.md - D3`: ¿la retención ya resuelta cabe como campo de `VersionCompleta`, o es mejor un método en `FacturaService`?
- [x] 4.2 Anotar la decisión y el porqué antes de escribir código.
- [x] 4.3 Implementarla. El snapshot congelado (`tipo_retencion_nombre`, `tipo_retencion_porcentaje`) manda siempre sobre el catálogo actual: una factura antigua SHALL seguir mostrando el porcentaje que tenía al emitirse, aunque el tipo se haya cambiado después.
- [x] 4.4 Retirar `retencionDeVersion(...)` de `PdfService.java:653`.
- [x] 4.5 Test que cubra explícitamente el caso de factura antigua cuyo tipo de retención cambió después. Es el que protege el snapshot.
- [x] 4.6 Pasar `PdfServiceTest`. Debe seguir en verde.

## 5. Barrido de lo que quede

- [x] 5.1 Buscar en `PdfService.java` cualquier otro cálculo o clasificación que no sea de dibujo. Candidatos: `porcentajeRejilla(...)` (`:821`) e `importePdf(...)` (`:828`), que son **formato**, no negocio: esos se quedan.
- [x] 5.2 Si aparece alguno más que sí sea negocio, anotarlo y decidir si entra en este change o en el siguiente. Ante la duda, entra aquí: el change 2 no debe arrastrar negocio dentro del modelo de documento.
- [x] 5.3 Comprobar que `PdfService` ya no importa nada del paquete de reglas de cálculo salvo lo imprescindible.

## 6. Verificación de que nada ha cambiado

- [x] 6.1 `mvn test` completo en verde, con `PdfServiceTest` sin modificar.
- [x] 6.2 Regenerar los PDF del punto 1.2 y compararlos con los de referencia: mismo número de páginas y mismo texto extraído.
- [x] 6.3 Si algún PDF difiere, **parar**. No sigas al change 2 con una diferencia sin explicar.
- [x] 6.4 Abrir a ojo el PDF de la factura con suplidos y el de varias páginas, que son los dos casos con más partes móviles.

## 7. La especificación

- [x] 7.1 Revisar que los seis requisitos trasladados aparecen en `specs/pdf-rendering/spec.md` con su texto literal, sin una coma cambiada (D5).
- [x] 7.2 Revisar que en `invoicing` «Orden del desglose de totales» pasa a «Desglose de totales por tipo de IVA» y «Suplidos» pasa a «Suplidos en la facturación», ambos **sin** su párrafo de PDF y sin sus escenarios de PDF («Factura con descuento en el PDF», «Factura con varios tipos de IVA y descuento», «El PDF no usa los rótulos de la escalera», «El suplido tiene su propio bloque en el PDF», «Un suplido nunca se rotula como exento», «Factura de solo suplidos»), y que esos fragmentos están íntegros en la capability nueva. Se quedan en `invoicing` «Los importes no cambian», «Factura sin descuento» y «Factura sin suplidos», que son transversales.
- [x] 7.3 Comprobar que ninguna regla de negocio se ha ido a `pdf-rendering`: que un suplido no entra en la base imponible tiene que seguir estando en `invoicing`, en «Suplidos en la facturación».
- [x] 7.4 Comprobar que no queda ningún requisito duplicado en las dos capabilities y que cada escenario vive en una sola.

## 8. Cierre

- [x] 8.1 Anotar en el change los defectos de maquetación que hayan aparecido y no se hayan tocado, para proponerlos aparte.
- [x] 8.2 Dejar constancia de la decisión del punto 4.1, que el change 2 va a necesitar.
- [ ] 8.3 Aplicar y archivar por el flujo habitual.
