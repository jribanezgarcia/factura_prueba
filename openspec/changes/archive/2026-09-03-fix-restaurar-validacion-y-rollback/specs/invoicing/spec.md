## MODIFIED Requirements

### Requirement: Copia de seguridad

La aplicación SHALL tener un botón para crear una copia de seguridad manual. En la V1 la copia SHALL ser únicamente del archivo SQLite; no se incluyen PDFs ni configuración. La aplicación SHALL permitir restaurar una copia de seguridad desde la misma pantalla. Antes de restaurar, la aplicación SHALL mostrar un resumen del contenido del archivo (empresa, NIF, número de facturas, última fecha y versión de esquema). La aplicación SHALL validar la copia antes de sustituir nada: rechazará archivos que no sean bases de datos válidas de la aplicación, que no contengan las tablas fundamentales de la aplicación ni que sean la propia base activa. La aplicación SHALL aceptar una copia de una versión de esquema anterior y SHALL aplicarle las migraciones pendientes al restaurarla. La aplicación SHALL NOT exigir la estructura completa de tablas salvo que la versión de esquema de la copia sea posterior a la que la aplicación conoce; en ese caso SHALL exigir la estructura completa y avisar antes de continuar. Antes de restaurar, la aplicación SHALL guardar automáticamente una copia de rescate del estado previo de la empresa activa. La aplicación SHALL permitir restaurar sobre la empresa activa o crear una nueva empresa a partir de la copia. La aplicación SHALL NOT permitir sobrescribir una empresa con los datos de otra con NIF distinto; en ese caso solo se ofrecerá crear una empresa nueva. Si el logo referenciado en la copia no existe en la máquina, la aplicación SHALL avisar y continuar sin bloquear.

#### Scenario: Crear copia de seguridad
- **WHEN** el usuario pulsa el botón de copia de seguridad y elige dónde guardarla
- **THEN** se genera una copia del archivo SQLite en la ubicación elegida

#### Scenario: Restaurar sobre la empresa activa
- **WHEN** el usuario selecciona un archivo de copia, elige «Reemplazar la empresa activa» y confirma
- **THEN** los datos de la copia sustituyen a los de la empresa activa y la aplicación vuelve al menú principal

#### Scenario: Copia de rescate automática
- **WHEN** el usuario restaura una copia sobre la empresa activa
- **THEN** antes de restaurar queda guardada una copia del estado previo en la subcarpeta `copias_previas`

#### Scenario: Restaurar una copia de esquema anterior
- **WHEN** el usuario restaura una copia cuya versión de esquema es anterior a la de la aplicación
- **THEN** la aplicación la acepta, la restaura y le aplica las migraciones pendientes, quedando al esquema actual

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

#### Scenario: Copia de esquema posterior con las mismas tablas
- **WHEN** el archivo de copia tiene una versión de esquema superior pero contiene todas las tablas y columnas que la aplicación conoce
- **THEN** la aplicación acepta la copia y avisa de que es de una versión más nueva antes de continuar

#### Scenario: Copia de esquema posterior con tablas distintas
- **WHEN** el archivo de copia tiene una versión de esquema superior y falta alguna tabla o columna que la aplicación necesita
- **THEN** la aplicación rechaza la copia e informa de las diferencias de versión

#### Scenario: Logo del backup inexistente
- **WHEN** la copia referencia un archivo de logo que no existe en la máquina actual
- **THEN** la aplicación avisa de que el logo no se encontrará pero permite continuar con la restauración

#### Scenario: Dos copias seguidas dentro del mismo segundo
- **WHEN** el usuario crea dos copias de seguridad en menos de un segundo
- **THEN** ambas se crean correctamente con nombres distintos sin error de colisión
