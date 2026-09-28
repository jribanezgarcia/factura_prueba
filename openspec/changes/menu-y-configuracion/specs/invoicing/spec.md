## MODIFIED Requirements

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear y configurar en su ficha, ver el siguiente número y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la acción «Cambiar de empresa», que vuelve a la pantalla de arranque. El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, última carpeta de exportación y tema de apariencia. El tema SHALL guardarse en cada empresa, igual que el color del PDF, de modo que cada empresa conserve su propio tema. Al entrar en una empresa o cambiar a otra SHALL aplicarse el tema guardado en ella y, si nunca se ha guardado ninguno, el tema biblioteca8. La pantalla de arranque, que se muestra antes de elegir empresa, SHALL usar el tema de la última empresa abierta. El tamaño y la posición de la ventana SHALL NOT recordarse: cada pantalla se abre con el tamaño que le corresponde.

En el tamaño mínimo de ventana (1024×768), todos los campos de la sección Datos fiscales SHALL verse completos, sin recortes por el borde derecho.

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
- **WHEN** el usuario abre Configuración → Series de numeración
- **THEN** ve el siguiente número de cada serie, calculado a partir de sus facturas del año de trabajo, y no puede cambiarlo a mano

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
- **WHEN** el usuario abre la sección Datos fiscales de Configuración en el tamaño mínimo de ventana
- **THEN** los campos Nombre o razón social, Actividad y Localidad se ven enteros, sin recortes

#### Scenario: La ventana no recuerda posición ni tamaño
- **WHEN** el usuario cierra la aplicación con la ventana movida y vuelve a abrirla
- **THEN** la pantalla se abre con su tamaño propio y no se guarda la posición ni el tamaño anteriores

### Requirement: Configuración organizada por secciones

La pantalla de Configuración SHALL organizarse en secciones navegables desde una lista lateral, en lugar de pestañas. La lista SHALL agrupar las secciones en cuatro grupos, en este orden:

- DATOS DE LA EMPRESA: Datos fiscales y Logotipo;
- FISCALIDAD: Tipos de IVA, Retenciones IRPF y Series de numeración;
- FACTURA EN PDF: Diseño del PDF;
- PREFERENCIAS: Apariencia y Carpetas.

Cada sección SHALL tener un único cometido: Datos fiscales, los datos de la empresa; Logotipo, la imagen de la empresa con su vista previa; Diseño del PDF, la cabecera, el pie legal y el color de acento de los PDF con la vista previa de la cabecera; Apariencia, el tema de la aplicación; Carpetas, la carpeta de los PDF y la última carpeta usada.

Cada sección SHALL mostrarse en la lista con un icono pequeño junto a su nombre, y la sección elegida SHALL distinguirse sin usar negrita. Al abrirse, cada sección SHALL empezar por su icono, su título, que es el mismo nombre de la lista, y una frase que explique para qué sirve.

El botón de guardado general SHALL mostrarse únicamente en las secciones cuyos datos guarda (Datos fiscales, Logotipo, Diseño del PDF, Apariencia y Carpetas) y SHALL ocultarse en las secciones que se administran fila a fila (Tipos de IVA, Retenciones IRPF y Series de numeración), donde cada una conserva sus propias acciones. El tema de la aplicación SHALL presentarse en la sección Apariencia por tratarse de una preferencia de la empresa activa.

#### Scenario: Navegación entre secciones
- **WHEN** el usuario selecciona una sección en la lista lateral
- **THEN** el contenido de esa sección ocupa la zona derecha y el resto de secciones queda oculto

#### Scenario: El botón de guardado solo donde aplica
- **WHEN** el usuario abre las secciones de Tipos de IVA, Retenciones IRPF o Series de numeración
- **THEN** el botón de guardado general no se muestra, y las acciones disponibles son las propias de la sección

#### Scenario: Cada sección cabe sin scroll
- **WHEN** el usuario recorre las secciones con la ventana en su tamaño mínimo de 1024×768
- **THEN** el contenido de cada sección es visible completo sin necesidad de desplazarse

#### Scenario: Cada sección se presenta sola
- **WHEN** el usuario abre la sección Diseño del PDF
- **THEN** ve su icono, el título «Diseño del PDF» y la frase que explica para qué sirve
- **AND** en esa sección están la cabecera, el pie legal, el color de acento y la vista previa, y no el tema de la aplicación ni las carpetas

#### Scenario: Quitar el logotipo
- **WHEN** el usuario pulsa «Quitar logotipo» en la sección Logotipo y guarda
- **THEN** la empresa queda sin logo, la vista previa del logotipo queda vacía y los PDF usan la cabecera de texto

### Requirement: Datos obligatorios de la empresa

La aplicación SHALL exigir que la empresa activa tenga completos sus datos obligatorios antes de permitir trabajar con ella: nombre o razón social, NIF válido, dirección, código postal válido, localidad, provincia, email válido y teléfono.

Al entrar en una empresa a la que le falte alguno de esos datos, sea desde la pantalla de arranque o al restaurar una copia, la aplicación SHALL abrir Configuración en la sección Datos fiscales en lugar del menú principal, SHALL mostrar un texto que explique que para empezar a usar el programa hay que completar los datos de la empresa, y SHALL desactivar el botón de volver al menú y toda la barra de navegación salvo la opción de salir. Mientras tanto SHALL seguir disponibles todas las secciones de Configuración, el guardado de la configuración y la acción «Cambiar de empresa». Si el nombre de la empresa está vacío, SHALL proponerse el nombre con el que se creó.

Los campos obligatorios de la sección Datos fiscales SHALL marcarse con un asterisco. Guardar la configuración SHALL NOT ser posible mientras falte algún dato obligatorio o no sea válido: la aplicación SHALL marcar como erróneos todos los campos incorrectos y SHALL mostrar un único aviso, el del primer dato incorrecto. Cuando se completan unos datos que estaban pendientes, la aplicación SHALL pasar al menú principal.

#### Scenario: Entrar en una empresa recién creada
- **WHEN** el usuario entra en una empresa que acaba de crear
- **THEN** se abre Configuración en la sección Datos fiscales con el texto explicativo y el nombre de la empresa ya propuesto
- **AND** la barra de navegación solo permite salir

#### Scenario: Guardar con datos incompletos
- **WHEN** el usuario pulsa Guardar cambios sin haber rellenado el teléfono y con un NIF no válido
- **THEN** la configuración no se guarda
- **AND** los campos NIF y Teléfono quedan marcados como erróneos y se muestra un único aviso, el del NIF

#### Scenario: Completar los datos
- **WHEN** el usuario rellena todos los datos obligatorios con valores válidos y guarda
- **THEN** los datos se guardan, la barra de navegación se habilita y la aplicación muestra el menú principal

#### Scenario: Empresa existente incompleta
- **WHEN** el usuario entra en una empresa que ya tenía facturas pero no tiene email
- **THEN** se abre Configuración en la sección Datos fiscales en lugar del menú principal

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
