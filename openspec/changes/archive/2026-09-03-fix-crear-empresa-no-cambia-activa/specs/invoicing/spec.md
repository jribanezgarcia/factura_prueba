## MODIFIED Requirements

### Requirement: Gestión de empresas

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y un identificador interno (slug) que da nombre a su carpeta de datos. La aplicación SHALL listar las empresas disponibles, crear nuevas empresas, cambiar a una empresa existente y eliminar una empresa distinta de la actual. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa, sin interrumpir la empresa que esté en uso. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa actualmente activa SHALL NOT poder eliminarse. Cambiar de empresa SHALL ser siempre una acción explícita del usuario; crear una empresa desde Configuración SHALL NOT cambiar la empresa activa, ni la conexión en curso, ni la última empresa recordada.

#### Scenario: Crear una nueva empresa
- **WHEN** el usuario crea la empresa «Asesoría María Luisa Ibáñez» desde Configuración
- **THEN** se crea su base de datos con la estructura completa y la empresa aparece en el listado
- **AND** la empresa activa no cambia: los datos que se siguen viendo y editando son los de la empresa anterior

#### Scenario: Nombre visible e identificador interno
- **WHEN** el usuario crea una empresa con nombre «Asesoría María Luisa Ibáñez»
- **THEN** el listado muestra el nombre visible y su carpeta de datos usa el identificador interno correspondiente

#### Scenario: Cambiar a otra empresa
- **WHEN** el usuario elige cambiar a una empresa distinta de la actual
- **THEN** la aplicación pasa a usar la base de datos de esa empresa y sus datos (configuración, clientes, series, facturas) sustituyen a los de la anterior

#### Scenario: Crear y aceptar el cambio
- **WHEN** el usuario crea una empresa desde Configuración y acepta el ofrecimiento de cambiar a ella
- **THEN** la aplicación pasa a la empresa nueva y recarga el menú principal

#### Scenario: Eliminar empresa no actual
- **WHEN** el usuario elimina una empresa distinta de la actual y confirma
- **THEN** la empresa desaparece del listado de disponibles

#### Scenario: La empresa activa no se elimina
- **WHEN** el usuario intenta eliminar la empresa actualmente en uso
- **THEN** la aplicación no permite eliminarla

#### Scenario: Última empresa preseleccionada
- **WHEN** el usuario abre la aplicación después de haber usado «Comercial Alcazaba»
- **THEN** «Comercial Alcazaba» aparece seleccionada por defecto en la pantalla de arranque

#### Scenario: Crear empresa desde el arranque
- **WHEN** el usuario usa la opción de crear empresa de la pantalla de arranque
- **THEN** se crea la empresa, aparece en el listado y queda seleccionada sin salir de la pantalla de arranque
