## ADDED Requirements

### Requirement: Datos obligatorios de la empresa

La aplicación SHALL exigir que la empresa activa tenga completos sus datos obligatorios antes de permitir trabajar con ella: nombre o razón social, NIF válido, dirección, código postal válido, localidad, provincia, email válido y teléfono.

Al entrar en una empresa a la que le falte alguno de esos datos, sea desde la pantalla de arranque, al cambiar de empresa o al restaurar una copia, la aplicación SHALL abrir Configuración en la sección Empresa en lugar del menú principal, SHALL mostrar un texto que explique que para empezar a usar el programa hay que completar los datos de la empresa, y SHALL desactivar la navegación hacia otras pantallas salvo la opción de salir. Las demás secciones de Configuración SHALL seguir disponibles. Si el nombre de la empresa está vacío, SHALL proponerse el nombre con el que se creó.

Los campos obligatorios de la sección Empresa SHALL marcarse con un asterisco. Guardar la configuración SHALL NOT ser posible mientras falte algún dato obligatorio o no sea válido, y la aplicación SHALL indicar cuáles son. Cuando los datos se completan desde ese modo, la aplicación SHALL pasar al menú principal.

#### Scenario: Entrar en una empresa recién creada
- **WHEN** el usuario entra en una empresa que acaba de crear
- **THEN** se abre Configuración en la sección Empresa con el texto explicativo y el nombre de la empresa ya propuesto
- **AND** la barra de navegación solo permite salir

#### Scenario: Guardar con datos incompletos
- **WHEN** el usuario pulsa Guardar configuración sin haber rellenado el teléfono y con un NIF no válido
- **THEN** la configuración no se guarda y la aplicación indica que faltan o no son válidos NIF y Teléfono

#### Scenario: Completar los datos
- **WHEN** el usuario rellena todos los datos obligatorios con valores válidos y guarda
- **THEN** los datos se guardan y la aplicación muestra el menú principal

#### Scenario: Empresa existente incompleta
- **WHEN** el usuario entra en una empresa que ya tenía facturas pero no tiene email
- **THEN** se abre Configuración en la sección Empresa en lugar del menú principal

#### Scenario: Empresa completa
- **WHEN** el usuario entra en una empresa con todos los datos obligatorios válidos
- **THEN** se abre el menú principal directamente

#### Scenario: No se pueden vaciar los datos de una empresa completa
- **WHEN** el usuario borra el NIF de una empresa completa en Configuración y guarda
- **THEN** la configuración no se guarda y la aplicación indica que falta el NIF

#### Scenario: La demostración entra al menú
- **WHEN** el usuario entra en la empresa de demostración recién cargada
- **THEN** se abre el menú principal directamente

## MODIFIED Requirements

### Requirement: Gestión de empresas

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y un identificador interno (slug) que da nombre a su carpeta de datos. La aplicación SHALL listar las empresas disponibles, crear nuevas empresas, cambiar a una empresa existente y eliminar una empresa distinta de la actual. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa, sin interrumpir la empresa que esté en uso. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa actualmente activa SHALL NOT poder eliminarse. Cambiar de empresa SHALL ser siempre una acción explícita del usuario; crear una empresa desde Configuración SHALL NOT cambiar la empresa activa, ni la conexión en curso, ni la última empresa recordada.

Cuando al abrir la aplicación no exista ninguna empresa, la aplicación SHALL cargar la empresa de demostración y SHALL dejarla preseleccionada. La aplicación SHALL NOT crear ninguna otra empresa por su cuenta ni SHALL volver a cargar la demostración si ya existe alguna empresa. Cuando no exista ninguna empresa, o la única sea la de demostración, la pantalla de arranque SHALL mostrar un texto breve que invite a crear la empresa propia. En el arranque en que se carga la demostración, y siempre que no exista ninguna empresa, la aplicación SHALL mostrar además un aviso con la explicación completa: qué es la empresa de demostración, cómo crear la propia y que después habrá que completar sus datos.

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
- **THEN** la aplicación pasa a la empresa nueva y, como sus datos están incompletos, abre Configuración en la sección Empresa

#### Scenario: Eliminar empresa no actual
- **WHEN** el usuario elimina una empresa distinta de la actual y confirma
- **THEN** la empresa desaparece del listado de disponibles

#### Scenario: La empresa activa no se elimina
- **WHEN** el usuario intenta eliminar la empresa actualmente en uso
- **THEN** la aplicación no permite eliminarla

#### Scenario: Última empresa preseleccionada
- **WHEN** el usuario abre la aplicación después de haber usado «Talleres Ejemplo S.L.»
- **THEN** «Talleres Ejemplo S.L.» aparece seleccionada por defecto en la pantalla de arranque

#### Scenario: Crear empresa desde el arranque
- **WHEN** el usuario usa la opción de crear empresa de la pantalla de arranque
- **THEN** se crea la empresa, aparece en el listado y queda seleccionada sin salir de la pantalla de arranque

#### Scenario: Instalación nueva
- **WHEN** el usuario abre la aplicación por primera vez y no existe ninguna empresa
- **THEN** se carga la empresa de demostración, aparece preseleccionada en la pantalla de arranque y se ve el texto breve que invita a crear la empresa propia
- **AND** aparece un aviso con la explicación completa

#### Scenario: La demostración no se recarga
- **WHEN** el usuario vuelve a abrir la aplicación y la única empresa es la de demostración
- **THEN** no se carga ni se duplica la empresa de demostración y no aparece el aviso, pero sí el texto breve

#### Scenario: Con empresa propia no hay texto de ayuda
- **WHEN** el usuario abre la aplicación y existe alguna empresa distinta de la de demostración
- **THEN** la pantalla de arranque no muestra el texto de ayuda
