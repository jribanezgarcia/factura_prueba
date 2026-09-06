## MODIFIED Requirements

### Requirement: Copia de seguridad

La aplicación SHALL tener un botón para crear una copia de seguridad manual. En la V1 la copia SHALL ser únicamente del archivo SQLite; no se incluyen PDFs ni configuración. La aplicación SHALL permitir restaurar una copia de seguridad desde la misma pantalla. Antes de restaurar, la aplicación SHALL mostrar un resumen del contenido del archivo (empresa, NIF, número de facturas, última fecha y versión de esquema). La aplicación SHALL validar la copia antes de sustituir nada: rechazará archivos que no sean bases de datos válidas de la aplicación, que no contengan las tablas fundamentales de la aplicación ni que sean la propia base activa.

La aplicación SHALL decidir si acepta una copia por **su estructura**, no por su número de versión de esquema. Una copia cuya versión no coincida con la de la aplicación SHALL aceptarse si contiene todas las tablas y columnas que la aplicación necesita, avisando antes de continuar; SHALL rechazarse en caso contrario. El aviso y el rechazo SHALL decir si la versión de la copia es anterior o posterior a la de la aplicación, y SHALL NOT describir como más nueva una copia cuyo número de versión sea mayor por proceder de un historial de migraciones distinto.

Mientras la aplicación mantenga un único esquema, no existen copias de versiones anteriores a las que aplicar migraciones: una copia cuya estructura no coincida con la que la aplicación necesita SHALL rechazarse, sin intentar actualizarla. Tras restaurar, la base SHALL quedar en un estado utilizable sin que el usuario tenga que hacer nada más.

Antes de restaurar, la aplicación SHALL guardar automáticamente una copia de rescate del estado previo de la empresa activa. La aplicación SHALL permitir restaurar sobre la empresa activa o crear una nueva empresa a partir de la copia. La aplicación SHALL NOT permitir sobrescribir una empresa con los datos de otra con NIF distinto; en ese caso solo se ofrecerá crear una empresa nueva. Si el logo referenciado en la copia no existe en la máquina, la aplicación SHALL avisar y continuar sin bloquear.

#### Scenario: Crear copia de seguridad
- **WHEN** el usuario pulsa el botón de copia de seguridad y elige dónde guardarla
- **THEN** se genera una copia del archivo SQLite en la ubicación elegida

#### Scenario: Restaurar sobre la empresa activa
- **WHEN** el usuario selecciona un archivo de copia, elige «Reemplazar la empresa activa» y confirma
- **THEN** los datos de la copia sustituyen a los de la empresa activa y la aplicación vuelve al menú principal

#### Scenario: Copia de rescate automática
- **WHEN** el usuario restaura una copia sobre la empresa activa
- **THEN** antes de restaurar queda guardada una copia del estado previo en la subcarpeta `copias_previas`

#### Scenario: La base restaurada queda utilizable
- **WHEN** el usuario restaura una copia de seguridad aceptada
- **THEN** la base resultante queda a la última versión de esquema que la aplicación conoce, lista para usarse sin pasos adicionales

#### Scenario: Restaurar una copia de esquema anterior
- **WHEN** el usuario restaura una copia cuya estructura no contiene todo lo que la aplicación necesita, por proceder de un esquema anterior
- **THEN** la aplicación la rechaza e informa de qué falta, sin intentar actualizarla ni tocar los datos actuales

#### Scenario: Copia de esquema posterior con las mismas tablas
- **WHEN** el archivo de copia tiene una versión de esquema superior pero contiene todas las tablas y columnas que la aplicación conoce
- **THEN** la aplicación acepta la copia y avisa de la diferencia de versión antes de continuar
- **AND** el aviso no afirma que la copia proceda de una versión más nueva de la aplicación cuando su número mayor solo refleja un historial de migraciones distinto

#### Scenario: Copia de esquema posterior con tablas distintas
- **WHEN** el archivo de copia tiene una versión de esquema superior y falta alguna tabla o columna que la aplicación necesita
- **THEN** la aplicación rechaza la copia e informa de qué falta, indicando si su número de versión es anterior o posterior

#### Scenario: Archivo sin las tablas fundamentales
- **WHEN** el usuario selecciona un archivo que no contiene las tablas fundamentales de la aplicación
- **THEN** la aplicación rechaza el archivo e informa del error sin tocar los datos

#### Scenario: Crear empresa nueva desde una copia
- **WHEN** el usuario selecciona un archivo de copia, elige «Crear una empresa nueva con estos datos», introduce un nombre y confirma
- **THEN** se crea la empresa con los datos de la copia y la empresa activa no cambia hasta que el usuario acepta el cambio

#### Scenario: Backup con NIF distinto
- **WHEN** el NIF del archivo de copia no coincide con el NIF de la empresa activa
- **THEN** la opción «Reemplazar la empresa activa» se deshabilita y solo se ofrece crear una empresa nueva

#### Scenario: Empresa activa vacía sin NIF
- **WHEN** la empresa activa no tiene NIF configurado y no tiene ninguna factura, y el usuario restaura una copia con NIF distinto
- **THEN** se permite reemplazar la empresa activa aunque el NIF no coincida

#### Scenario: Archivo que no es una copia válida
- **WHEN** el usuario selecciona un archivo que no es una base de datos SQLite válida de la aplicación
- **THEN** la aplicación rechaza el archivo e informa del error sin tocar los datos

#### Scenario: Logo del backup inexistente
- **WHEN** la copia referencia un archivo de logo que no existe en la máquina actual
- **THEN** la aplicación avisa de que el logo no se encontrará pero permite continuar con la restauración

#### Scenario: Dos copias seguidas dentro del mismo segundo
- **WHEN** el usuario crea dos copias de seguridad en menos de un segundo
- **THEN** ambas se crean correctamente con nombres distintos sin error de colisión

### Requirement: Datos de demostración recreables

La aplicación SHALL disponer de un juego de datos de demostración que se pueda cargar bajo demanda, para probar sin teclear facturas a mano.

Los datos de demostración SHALL versionarse junto al esquema, de modo que un cambio de esquema pueda actualizarlos en el mismo sitio.

Los datos de demostración SHALL NOT depender de los identificadores concretos que el esquema asigne a sus filas sembradas: SHALL referirse a ellas por su nombre, de modo que cambiar el orden de las siembras no altere lo que la demostración representa.

La carga SHALL ser repetible: ejecutarla dos veces SHALL dejar el mismo resultado, sin datos duplicados.

Cuando la demostración no se pueda recrear porque su base está en uso, la carga SHALL detenerse con un mensaje que diga que hay que cerrar la aplicación, y SHALL NOT continuar sobre una demostración a medio borrar.

Los datos de demostración SHALL ser ficticios y SHALL NOT contener datos reales de ningún cliente.

La demostración SHALL cubrir los casos que cuestan de montar a mano: varias series, una factura con varios tipos de IVA, una con descuento global, una con retención, una con suplido, una anulada y una rectificativa.

#### Scenario: Cargar la demostración
- **WHEN** el usuario carga los datos de demostración
- **THEN** existe una empresa de demostración con clientes, series y facturas de ejemplo
- **AND** entre ellas hay una con varios tipos de IVA, una con descuento, una con retención, una con suplido, una anulada y una rectificativa

#### Scenario: Cargar la demostración dos veces
- **WHEN** el usuario carga los datos de demostración sobre una empresa de demostración que ya existía
- **THEN** la empresa queda con los mismos datos que la primera vez, sin duplicados

#### Scenario: La demostración no depende del orden de las siembras
- **WHEN** cambia el orden en que el esquema siembra los tipos de IVA
- **THEN** cada línea de la demostración sigue llevando el tipo de IVA que le corresponde por su nombre

#### Scenario: Cargar la demostración con la aplicación abierta
- **WHEN** el usuario carga los datos de demostración mientras la aplicación tiene abierta esa misma empresa
- **THEN** la carga se detiene indicando que hay que cerrar la aplicación
- **AND** la demostración anterior queda intacta, no a medio borrar

#### Scenario: Los datos de demostración son ficticios
- **WHEN** alguien revisa la empresa de demostración
- **THEN** ningún cliente, NIF ni dirección corresponde a una persona o empresa real
