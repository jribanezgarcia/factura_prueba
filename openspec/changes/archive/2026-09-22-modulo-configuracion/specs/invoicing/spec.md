## MODIFIED Requirements

### Requirement: Gestión de empresas

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y una carpeta de datos cuyo nombre se forma a partir de él. Crear, elegir y eliminar empresas SHALL hacerse únicamente en la pantalla de arranque. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa. Eliminar una empresa SHALL pedir confirmación y SHALL borrar su carpeta de datos. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa en uso SHALL NOT poder eliminarse. Para trabajar con otra empresa, Configuración SHALL ofrecer la acción «Cambiar de empresa», que cierra la empresa en uso y vuelve a la pantalla de arranque; cambiar de empresa SHALL ser siempre una acción explícita del usuario.

Cuando al abrir la aplicación no exista ninguna empresa, la aplicación SHALL cargar la empresa de demostración y SHALL dejarla preseleccionada. La aplicación SHALL NOT crear ninguna otra empresa por su cuenta ni SHALL volver a cargar la demostración si ya existe alguna empresa. Cuando no exista ninguna empresa, o la única sea la de demostración, la pantalla de arranque SHALL mostrar un texto que invite a crear la empresa propia y a completar después sus datos.

#### Scenario: Crear una nueva empresa
- **WHEN** el usuario crea la empresa «Asesoría María Luisa Ibáñez» desde la pantalla de arranque
- **THEN** se crea su base de datos con la estructura completa y la empresa aparece en el listado de la pantalla de arranque

#### Scenario: Nombre visible e identificador interno
- **WHEN** el usuario crea una empresa con nombre «Asesoría María Luisa Ibáñez»
- **THEN** el listado muestra el nombre visible y su carpeta de datos usa un nombre formado a partir de él

#### Scenario: Cambiar a otra empresa
- **WHEN** el usuario pulsa «Cambiar de empresa» en Configuración, confirma, elige otra empresa en la pantalla de arranque y entra
- **THEN** la aplicación pasa a usar la base de datos de esa empresa y sus datos (configuración, clientes, series, facturas) sustituyen a los de la anterior

#### Scenario: Crear y aceptar el cambio
- **WHEN** el usuario crea una empresa en la pantalla de arranque y entra en ella
- **THEN** la aplicación abre Configuración en la sección Empresa, porque a la empresa nueva le faltan sus datos obligatorios

#### Scenario: Eliminar empresa no actual
- **WHEN** el usuario elimina una empresa en la pantalla de arranque y confirma
- **THEN** la empresa desaparece del listado y se borra su carpeta de datos

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
- **THEN** se carga la empresa de demostración, aparece preseleccionada en la pantalla de arranque y se ve el texto que invita a crear la empresa propia

#### Scenario: La demostración no se recarga
- **WHEN** el usuario vuelve a abrir la aplicación y ya existe alguna empresa
- **THEN** no se carga ni se duplica la empresa de demostración

#### Scenario: Con empresa propia no hay texto de ayuda
- **WHEN** el usuario abre la aplicación y existe alguna empresa distinta de la de demostración
- **THEN** la pantalla de arranque no muestra el texto de ayuda

#### Scenario: Cancelar la eliminación de una empresa
- **WHEN** el usuario pulsa Eliminar en la pantalla de arranque y cancela la confirmación
- **THEN** la empresa sigue en el listado y sus datos no se tocan

#### Scenario: Volver al arranque desde Configuración
- **WHEN** el usuario pulsa «Cambiar de empresa» en Configuración y confirma
- **THEN** se cierra la ventana principal y se abre la pantalla de arranque con la empresa que se estaba usando preseleccionada

#### Scenario: Configuración ya no gestiona empresas
- **WHEN** el usuario abre Configuración
- **THEN** no hay ninguna sección para crear o eliminar empresas

### Requirement: Datos obligatorios de la empresa

La aplicación SHALL exigir que la empresa activa tenga completos sus datos obligatorios antes de permitir trabajar con ella: nombre o razón social, NIF válido, dirección, código postal válido, localidad, provincia, email válido y teléfono.

Al entrar en una empresa a la que le falte alguno de esos datos, sea desde la pantalla de arranque o al restaurar una copia, la aplicación SHALL abrir Configuración en la sección Empresa en lugar del menú principal, SHALL mostrar un texto que explique que para empezar a usar el programa hay que completar los datos de la empresa, y SHALL desactivar el botón de volver al menú y toda la barra de navegación salvo la opción de salir. Mientras tanto SHALL seguir disponibles todas las secciones de Configuración, el guardado de la configuración y la acción «Cambiar de empresa». Si el nombre de la empresa está vacío, SHALL proponerse el nombre con el que se creó.

Los campos obligatorios de la sección Empresa SHALL marcarse con un asterisco. Guardar la configuración SHALL NOT ser posible mientras falte algún dato obligatorio o no sea válido: la aplicación SHALL marcar como erróneos todos los campos incorrectos y SHALL mostrar un único aviso, el del primer dato incorrecto. Cuando se completan unos datos que estaban pendientes, la aplicación SHALL pasar al menú principal.

#### Scenario: Entrar en una empresa recién creada
- **WHEN** el usuario entra en una empresa que acaba de crear
- **THEN** se abre Configuración en la sección Empresa con el texto explicativo y el nombre de la empresa ya propuesto
- **AND** la barra de navegación solo permite salir

#### Scenario: Guardar con datos incompletos
- **WHEN** el usuario pulsa Guardar configuración sin haber rellenado el teléfono y con un NIF no válido
- **THEN** la configuración no se guarda
- **AND** los campos NIF y Teléfono quedan marcados como erróneos y se muestra un único aviso, el del NIF

#### Scenario: Completar los datos
- **WHEN** el usuario rellena todos los datos obligatorios con valores válidos y guarda
- **THEN** los datos se guardan, la barra de navegación se habilita y la aplicación muestra el menú principal

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

#### Scenario: Guardar una factura con la empresa incompleta
- **WHEN** a la empresa le falta el teléfono
- **THEN** el usuario no puede llegar al editor de facturas: la aplicación está en Configuración con la barra de navegación bloqueada

#### Scenario: Exportar o generar con la empresa incompleta
- **WHEN** a la empresa le falta algún dato obligatorio
- **THEN** no se puede exportar a PDF, crear una rectificativa ni generar las facturas mensuales, porque ninguna de esas pantallas está disponible hasta completar los datos

#### Scenario: Cambiar de empresa durante el bloqueo
- **WHEN** el usuario ha entrado en una empresa incompleta por error y pulsa «Cambiar de empresa» en Configuración
- **THEN** vuelve a la pantalla de arranque sin tener que completar los datos ni cerrar la aplicación

### Requirement: IVA

La aplicación SHALL permitir configurar tipos de IVA: tipos porcentuales e IVA exento. Para IVA exento SHALL poder indicarse un motivo o texto de exención. Cada línea SHALL poder tener un tipo de IVA diferente. Los tipos de IVA SHALL poder crearse, modificarse mientras sea seguro, marcarse como inactivos si ya se han utilizado y SHALL NOT eliminarse físicamente si forman parte del histórico. El alta y la edición de un tipo de IVA, y también de un tipo de retención, SHALL hacerse en una ficha propia que se abre desde la tabla de Configuración. El porcentaje de un tipo que ya aparece en facturas SHALL NOT poder modificarse, un tipo existente SHALL NOT poder pasar de porcentaje a exento ni al revés, y SHALL NOT poder convertirse en suplido ni dejar de serlo. En la exportación a PDF, el resumen de la factura SHALL desglosar cada tipo de IVA por separado (base y cuota), y en el editor la aplicación SHALL mostrar la base total, el IVA total y el total general como valores separados. Los cálculos SHALL usar BigDecimal; no se permite usar double/float para importes monetarios.

#### Scenario: Líneas con distintos tipos de IVA
- **WHEN** una factura tiene líneas con tipos de IVA diferentes, incluida una exenta
- **THEN** el resumen muestra cada tipo por separado: base 21% e IVA 21%, base 10% e IVA 10%, y base exenta con IVA 0% y su motivo de exención

#### Scenario: Inactivar tipo de IVA usado
- **WHEN** el usuario intenta inactivar un tipo de IVA que ya aparece en facturas del histórico
- **THEN** el tipo pasa a inactivo, no se ofrece para nuevas facturas y el histórico se conserva intacto

#### Scenario: Alta de un tipo de IVA en su ficha
- **WHEN** el usuario pulsa Nuevo en la sección IVA de Configuración, rellena el nombre y el porcentaje y guarda la ficha
- **THEN** el tipo aparece en la tabla y queda disponible para las facturas nuevas

#### Scenario: Porcentaje bloqueado en un tipo usado
- **WHEN** el usuario abre la ficha de un tipo de IVA o de retención que ya aparece en facturas
- **THEN** el porcentaje se muestra pero no se puede modificar, y el resto de los datos sí

#### Scenario: Eliminar un tipo sin uso
- **WHEN** el usuario elimina un tipo de IVA o de retención que no aparece en ninguna factura y confirma
- **THEN** el tipo desaparece de la tabla

#### Scenario: Eliminar un tipo en uso
- **WHEN** el usuario intenta eliminar un tipo de IVA o de retención que ya aparece en facturas
- **THEN** la aplicación no lo permite y propone desactivarlo en su ficha

### Requirement: Ventana

La aplicación SHALL abrir su ventana siempre a 1024x768 y centrada en la pantalla principal, en la primera ejecución y en todas las siguientes. La posición y el tamaño de la ventana SHALL seguir guardándose al cerrar la aplicación, pero SHALL NOT usarse al abrirla: la aplicación SHALL ignorar cualquier posición o tamaño guardados. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo y maximizar la ventana durante la sesión. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico y la fila de alta rápida de Series en Configuración SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible. El arranque (selección de empresa) SHALL mostrarse en una ventana propia, fija y pequeña de 760x520, también centrada. Al entrar en una empresa, la aplicación SHALL abrir la ventana principal a 1024x768 y centrada con la primera pantalla, y SHALL cerrar la ventana de arranque.

#### Scenario: Primera ejecución abre a 1024x768 centrada
- **WHEN** el usuario inicia la aplicación sin preferencias de ventana guardadas
- **THEN** la ventana mide 1024x768 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la última sesión
- **WHEN** el usuario cierra la aplicación tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana vuelve a abrirse a 1024x768 y centrada en la pantalla principal, sin recuperar la posición ni el tamaño de la sesión anterior, aunque estos sigan guardados

#### Scenario: Tamaño guardado inferior al mínimo
- **WHEN** la aplicación encuentra un tamaño de ventana guardado inferior a 1024x768
- **THEN** lo ignora y la ventana se abre a 1024x768, sin mostrarse recortada

#### Scenario: Mínimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla más pequeña
- **THEN** la ventana no puede bajar de 1024x768

#### Scenario: Filtros del Histórico con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre el Histórico
- **THEN** todos los filtros siguen visibles reorganizados en varias líneas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rápidas de IVA y Series con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abren las secciones IVA, Retenciones o Series de Configuración
- **THEN** en Series los campos de alta se reorganizan sin cortarse y sus botones permanecen visibles y agrupados, y en IVA y Retenciones los botones de la tabla se ven completos

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

#### Scenario: Corrección al navegar desde una vista pequeña
- **WHEN** el usuario pulsa Entrar en la ventana de arranque (760x520)
- **THEN** se abre la ventana principal a 1024x768 y centrada con el menú u otra vista principal, sin necesidad de redimensionar o maximizar manualmente
- **AND** la ventana de arranque se cierra
