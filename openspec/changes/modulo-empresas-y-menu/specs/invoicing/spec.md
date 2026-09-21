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
- **THEN** la aplicación abre el menú principal de la empresa nueva con el aviso de los datos que le faltan

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

La empresa activa SHALL tener unos datos obligatorios: nombre o razón social, NIF válido, dirección, código postal válido, localidad, provincia, email válido y teléfono. Mientras falte alguno, la aplicación SHALL dejar trabajar en todas las pantallas, pero SHALL NOT permitir guardar una factura, crear una rectificativa, generar las facturas mensuales ni exportar a PDF; al intentarlo SHALL avisar de qué datos faltan.

Al entrar en una empresa, la aplicación SHALL abrir siempre el menú principal y SHALL NOT bloquear la navegación. Si a la empresa le falta algún dato obligatorio, el menú SHALL mostrar un aviso con los datos que faltan y una acción «Completar datos» que abre Configuración en la sección Empresa; el aviso SHALL desaparecer en cuanto los datos estén completos. Si el nombre de la empresa está vacío, Configuración SHALL proponer el nombre con el que se creó.

Los campos obligatorios de la sección Empresa SHALL marcarse con un asterisco. Guardar la configuración SHALL NOT ser posible mientras falte algún dato obligatorio o no sea válido, y la aplicación SHALL indicar cuáles son. Cuando se completan unos datos que estaban pendientes, la aplicación SHALL pasar al menú principal.

#### Scenario: Entrar en una empresa recién creada
- **WHEN** el usuario entra en una empresa que acaba de crear
- **THEN** se abre el menú principal con el aviso de los datos que faltan
- **AND** todas las pantallas se pueden abrir desde el menú y desde la barra de navegación

#### Scenario: Guardar con datos incompletos
- **WHEN** el usuario pulsa Guardar configuración sin haber rellenado el teléfono y con un NIF no válido
- **THEN** la configuración no se guarda y la aplicación indica que faltan o no son válidos NIF y Teléfono

#### Scenario: Completar los datos
- **WHEN** el usuario pulsa «Completar datos» en el aviso del menú, rellena todos los datos obligatorios con valores válidos y guarda
- **THEN** los datos se guardan y la aplicación muestra el menú principal sin el aviso

#### Scenario: Empresa existente incompleta
- **WHEN** el usuario entra en una empresa que ya tenía facturas pero no tiene email
- **THEN** se abre el menú principal con un aviso que indica que falta el email

#### Scenario: Empresa completa
- **WHEN** el usuario entra en una empresa con todos los datos obligatorios válidos
- **THEN** se abre el menú principal sin ningún aviso

#### Scenario: No se pueden vaciar los datos de una empresa completa
- **WHEN** el usuario borra el NIF de una empresa completa en Configuración y guarda
- **THEN** la configuración no se guarda y la aplicación indica que falta el NIF

#### Scenario: La demostración entra al menú
- **WHEN** el usuario entra en la empresa de demostración recién cargada
- **THEN** se abre el menú principal directamente

#### Scenario: Guardar una factura con la empresa incompleta
- **WHEN** a la empresa le falta el teléfono y el usuario intenta guardar una factura
- **THEN** la factura no se guarda y la aplicación avisa de que falta el teléfono

#### Scenario: Exportar o generar con la empresa incompleta
- **WHEN** a la empresa le falta algún dato obligatorio y el usuario intenta exportar a PDF, crear una rectificativa o generar las facturas mensuales
- **THEN** la acción no se realiza y la aplicación avisa de los datos que faltan

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear/configurar, ver y modificar el siguiente número, configurar la reutilización de números anulados y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la acción «Cambiar de empresa», que vuelve a la pantalla de arranque. El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, última carpeta de exportación y tema de apariencia. El tema SHALL guardarse en cada empresa, igual que el color del PDF, de modo que cada empresa conserve su propio tema. Al entrar en una empresa o cambiar a otra SHALL aplicarse el tema guardado en ella y, si nunca se ha guardado ninguno, el tema biblioteca8. La pantalla de arranque, que se muestra antes de elegir empresa, SHALL usar el tema de la última empresa abierta. El tamaño y la posición de la ventana SHALL NOT recordarse: cada pantalla se abre con el tamaño que le corresponde.

En el tamaño mínimo de ventana (1024×768), todos los campos de la sección Empresa SHALL verse completos, sin recortes por el borde derecho.

#### Scenario: Configurar empresa
- **WHEN** el usuario guarda los datos de la empresa en Configuración
- **THEN** esos datos se usan en las nuevas exportaciones a PDF

#### Scenario: Elegir modo de cabecera
- **WHEN** el usuario selecciona un logo
- **THEN** el PDF usa el logo como cabecera y los datos de empresa permanecen guardados

#### Scenario: Pie legal configurable
- **WHEN** el usuario modifica el texto legal del pie
- **THEN** el nuevo texto se repite en las páginas de los PDF generados

#### Scenario: Modificar siguiente número
- **WHEN** el usuario modifica el siguiente número de una serie en Configuración
- **THEN** la próxima factura de esa serie se propone a partir del nuevo valor

#### Scenario: Cambiar el tema de la interfaz
- **WHEN** el usuario selecciona un tema en Configuración y guarda
- **THEN** el tema se aplica de inmediato y queda guardado en la empresa activa para las siguientes sesiones

#### Scenario: Cada empresa conserva su tema
- **WHEN** el usuario guarda el tema omarchy en una empresa, cambia a otra empresa que tiene guardado el tema esmeralda
- **THEN** la interfaz pasa a mostrarse con esmeralda
- **AND** al volver a la primera empresa se muestra de nuevo con omarchy

#### Scenario: Empresa sin tema guardado
- **WHEN** el usuario entra en una empresa en la que nunca se ha guardado un tema
- **THEN** la interfaz se muestra con el tema biblioteca8, aunque la empresa anterior tuviera otro

#### Scenario: El arranque usa el tema de la última empresa
- **WHEN** el usuario abre la aplicación y la última empresa abierta tiene guardado el tema sakura
- **THEN** la pantalla de arranque se muestra con sakura

#### Scenario: Cambiar el color del PDF
- **WHEN** el usuario elige un color en Configuración y guarda
- **THEN** los nuevos PDF usan ese color de acento y sus tonos derivados
- **AND** si se restablece el valor por defecto o la preferencia no existe, se usa `#B08D57`

#### Scenario: Gestionar empresas desde Configuración
- **WHEN** el usuario abre Configuración
- **THEN** encuentra la acción «Cambiar de empresa» y ninguna opción para crear o eliminar empresas, que están en la pantalla de arranque

#### Scenario: Configurar tipos de retención
- **WHEN** el usuario añade un tipo de retención del 15% con nombre "IRPF 15%" en Configuración
- **THEN** ese tipo queda disponible para seleccionar en las facturas de esa empresa

#### Scenario: Campos de empresa legibles a 1024×768
- **WHEN** el usuario abre la sección Empresa de Configuración en el tamaño mínimo de ventana
- **THEN** los campos Nombre / razón social, Actividad y Localidad se ven enteros, sin recortes

#### Scenario: La ventana no recuerda posición ni tamaño
- **WHEN** el usuario cierra la aplicación con la ventana movida y vuelve a abrirla
- **THEN** la pantalla se abre con su tamaño propio y no se guarda la posición ni el tamaño anteriores

### Requirement: Configuración organizada por secciones

La pantalla de Configuración SHALL organizarse en secciones navegables desde una lista lateral, en lugar de pestañas. Las secciones SHALL ser: Empresa, Cabecera y pie, PDF y apariencia, IVA, Retenciones y Series. El botón de guardado general SHALL mostrarse únicamente en las secciones cuyos datos guarda (Empresa, Cabecera y pie, y PDF y apariencia) y SHALL ocultarse en las secciones que se administran fila a fila (IVA, Retenciones y Series), donde cada una conserva sus propias acciones. El tema de la aplicación SHALL presentarse en la sección PDF y apariencia, junto al color del PDF, por tratarse de una preferencia de la empresa activa.

#### Scenario: Navegación entre secciones
- **WHEN** el usuario selecciona una sección en la lista lateral
- **THEN** el contenido de esa sección ocupa la zona derecha y el resto de secciones queda oculto

#### Scenario: El botón de guardado solo donde aplica
- **WHEN** el usuario abre las secciones de IVA, Retenciones o Series
- **THEN** el botón de guardado general no se muestra, y las acciones disponibles son las propias de la sección

#### Scenario: Cada sección cabe sin scroll
- **WHEN** el usuario recorre las secciones con la ventana en su tamaño mínimo de 1024×768
- **THEN** el contenido de cada sección es visible completo sin necesidad de desplazarse
