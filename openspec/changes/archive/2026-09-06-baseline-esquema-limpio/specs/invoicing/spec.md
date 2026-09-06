## ADDED Requirements

### Requirement: Base de datos desde un esquema único

La base de datos de cada empresa SHALL crearse a partir de un único script de esquema, sin necesidad de encadenar migraciones sucesivas para llegar al estado actual.

Todos los importes monetarios SHALL guardarse con el mismo tipo de columna, de modo que ninguna cantidad se almacene con un criterio distinto del resto.

Las filas sembradas por el esquema SHALL NOT depender de que un identificador concreto esté libre. Toda base recién creada SHALL disponer de los tipos de IVA habituales y del tipo «Suplido».

Ejecutar el proceso de creación sobre una base que ya está al día SHALL NOT duplicar ninguna fila sembrada.

#### Scenario: Base nueva lista para facturar
- **WHEN** el usuario crea una empresa nueva
- **THEN** la base resultante tiene todas las tablas de la aplicación y ofrece los tipos de IVA habituales más «Suplido»
- **AND** exactamente un tipo de IVA está marcado como suplido

#### Scenario: Los importes comparten criterio
- **WHEN** se guarda una factura con retención y suplidos
- **THEN** la base imponible, la cuota de IVA, el importe de retención, el total de suplidos y el total se almacenan con el mismo tipo de columna
- **AND** los decimales se conservan exactamente como se calcularon

#### Scenario: Crear la base dos veces no duplica nada
- **WHEN** el proceso de creación se ejecuta de nuevo sobre una base que ya está al día
- **THEN** el número de tipos de IVA, de tipos de retención y de filas de empresa no cambia

### Requirement: Datos de demostración recreables

La aplicación SHALL disponer de un juego de datos de demostración que se pueda cargar bajo demanda, para probar sin teclear facturas a mano.

Los datos de demostración SHALL versionarse junto al esquema, de modo que un cambio de esquema pueda actualizarlos en el mismo sitio.

La carga SHALL ser repetible: ejecutarla dos veces SHALL dejar el mismo resultado, sin datos duplicados.

Los datos de demostración SHALL ser ficticios y SHALL NOT contener datos reales de ningún cliente.

La demostración SHALL cubrir los casos que cuestan de montar a mano: varias series, una factura con varios tipos de IVA, una con descuento global, una con retención, una con suplido, una anulada y una rectificativa.

#### Scenario: Cargar la demostración
- **WHEN** el usuario carga los datos de demostración
- **THEN** existe una empresa de demostración con clientes, series y facturas de ejemplo
- **AND** entre ellas hay una con varios tipos de IVA, una con descuento, una con retención, una con suplido, una anulada y una rectificativa

#### Scenario: Cargar la demostración dos veces
- **WHEN** el usuario carga los datos de demostración sobre una empresa de demostración que ya existía
- **THEN** la empresa queda con los mismos datos que la primera vez, sin duplicados

#### Scenario: Los datos de demostración son ficticios
- **WHEN** alguien revisa la empresa de demostración
- **THEN** ningún cliente, NIF ni dirección corresponde a una persona o empresa real
