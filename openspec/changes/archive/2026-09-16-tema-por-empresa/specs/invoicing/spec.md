## MODIFIED Requirements

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear/configurar, ver y modificar el siguiente número, configurar la reutilización de números anulados y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la gestión de empresas (ver el listado de empresas, crear una nueva, cambiar a otra y eliminar una empresa distinta de la actual). El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, última carpeta de exportación y tema de apariencia. El tema SHALL guardarse en cada empresa, igual que el color del PDF, de modo que cada empresa conserve su propio tema. Al entrar en una empresa o cambiar a otra SHALL aplicarse el tema guardado en ella y, si nunca se ha guardado ninguno, el tema biblioteca8. La pantalla de arranque, que se muestra antes de elegir empresa, SHALL usar el tema de la última empresa abierta. El tamaño y la posición de la ventana SHALL NOT recordarse: cada pantalla se abre con el tamaño que le corresponde.

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
- **WHEN** el usuario abre la pestaña Empresas de Configuración
- **THEN** ve el listado de empresas disponibles con su nombre y puede crear, cambiar o eliminar empresas

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

La pantalla de Configuración SHALL organizarse en secciones navegables desde una lista lateral, en lugar de pestañas. Las secciones SHALL ser: Empresa, Cabecera y pie, PDF y apariencia, IVA, Retenciones, Series y Empresas. El botón de guardado general SHALL mostrarse únicamente en las secciones cuyos datos guarda (Empresa, Cabecera y pie, y PDF y apariencia) y SHALL ocultarse en las secciones que se administran fila a fila (IVA, Retenciones, Series y Empresas), donde cada una conserva sus propias acciones. El tema de la aplicación SHALL presentarse en la sección PDF y apariencia, junto al color del PDF, por tratarse de una preferencia de la empresa activa.

#### Scenario: Navegación entre secciones
- **WHEN** el usuario selecciona una sección en la lista lateral
- **THEN** el contenido de esa sección ocupa la zona derecha y el resto de secciones queda oculto

#### Scenario: El botón de guardado solo donde aplica
- **WHEN** el usuario abre las secciones de IVA, Retenciones, Series o Empresas
- **THEN** el botón de guardado general no se muestra, y las acciones disponibles son las propias de la sección

#### Scenario: Cada sección cabe sin scroll
- **WHEN** el usuario recorre las secciones con la ventana en su tamaño mínimo de 1024×768
- **THEN** el contenido de cada sección es visible completo sin necesidad de desplazarse
