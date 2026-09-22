## MODIFIED Requirements

### Requirement: Numeración por series

La aplicación SHALL tener series de numeración. En una instalación nueva la aplicación SHALL NO crear ninguna serie por defecto: el listado de series comienza vacío y el usuario las crea a mano. Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse en cada versión. El correlativo de cada serie SHALL ser independiente por ejercicio: cada año de trabajo reinicia su propia cuenta sobre el siguiente número de ese año, sin afectar al correlativo de otros años. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. El siguiente correlativo de una serie SHALL calcularse a partir de sus propias facturas: el mayor correlativo usado ese año más uno, o 1 si la serie no tiene ninguna factura ese año. La aplicación SHALL NOT guardar contadores de numeración. Una factura anulada SHALL conservar su correlativo, que SHALL NOT volver a proponerse. Los correlativos que queden libres por el borrado físico de facturas SHALL ofrecerse antes de proponer un correlativo nuevo. En Configuración → Series el usuario SHALL poder ver las series y su siguiente número, que SHALL NOT poder modificarse a mano.

Cada serie SHALL tener un campo \sufijo_fecha\ con tres opciones posibles: \MES\ (formato CODIGO-CORRELATIVO/MES o CORRELATIVO/MES si no hay código), \ANIO\ (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y \NINGUNO\ (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo \codigo\ de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra; solo SHALL admitirse una serie sin código a la vez, de modo que otra serie en blanco se rechaza y se identifica la serie por su descripción. El formato predeterminado para series nuevas SHALL ser \MES\. Los correlativos libres SHALL buscarse dentro del mismo año: un correlativo SHALL considerarse usado cuando lo tiene una factura de esa serie y ese ejercicio, esté activa o anulada.

#### Scenario: Propuesta de número con formato MES (actual)
- **WHEN** el usuario crea una factura en la serie C con formato MES, fecha 11/08/2026 y el siguiente correlativo de 2026 es 58
- **THEN** la aplicación propone el número \C-58/8\

#### Scenario: Propuesta de número con formato ANIO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato ANIO, fecha 15/07/2026 y el siguiente correlativo de 2026 es 56
- **THEN** la aplicación propone el número \56-2026\

#### Scenario: Propuesta de número con formato ANIO con código
- **WHEN** el usuario crea una factura en la serie C con formato ANIO, fecha 15/07/2026 y el siguiente correlativo de 2026 es 56
- **THEN** la aplicación propone el número \C-56-2026\

#### Scenario: Propuesta de número con formato NINGUNO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato NINGUNO y el siguiente correlativo es 56
- **THEN** la aplicación propone el número \56\

#### Scenario: Propuesta de número con formato NINGUNO con código
- **WHEN** el usuario crea una factura en la serie R con formato NINGUNO y el siguiente correlativo es 1
- **THEN** la aplicación propone el número \R-1\

#### Scenario: El mes sigue a la fecha con formato MES
- **WHEN** el usuario cambia la fecha de la factura de julio a agosto y la guarda con formato MES
- **THEN** el número se guarda con el mes correspondiente a la nueva fecha (p. ej. \C-59/8\) y el correlativo no cambia

#### Scenario: El año sigue a la fecha con formato ANIO
- **WHEN** el usuario cambia la fecha de la factura de 2026 a 2027 y la guarda con formato ANIO
- **THEN** el número se guarda con el año correspondiente a la nueva fecha (p. ej. \56-2027\) y el correlativo no cambia

#### Scenario: Número manual duplicado
- **WHEN** el usuario introduce manualmente un número que ya pertenece a una factura activa de la misma serie
- **THEN** la aplicación impide guardar e informa del conflicto

#### Scenario: El número no se consume al abandonar
- **WHEN** el usuario cancela una factura sin guardarla
- **THEN** el número propuesto no queda consumido y el siguiente correlativo permanece

#### Scenario: Anulada sin restaurar por número ocupado
- **WHEN** el usuario intenta restaurar una factura anulada cuyo número está ocupado por otra factura activa de la misma serie y año
- **THEN** la aplicación impide la restauración e informa del motivo

#### Scenario: Configurar formato de serie
- **WHEN** el usuario crea o edita una serie en Configuración y cambia el formato
- **THEN** el ejemplo debajo del desplegable se actualiza mostrando el resultado con correlativo 56 y fecha 15/07/2026

#### Scenario: Instalación nueva sin series
- **WHEN** el usuario crea una empresa nueva y abre Configuración → Series
- **THEN** el listado de series está vacío y no existe ninguna serie creada por la instalación

#### Scenario: Series sin código
- **WHEN** el usuario crea una serie dejando el código en blanco
- **THEN** la serie se guarda sin prefijo y sus números usan solo el correlativo y el sufijo de fecha
- **AND** si ya existe otra serie sin código, la aplicación rechaza el guardado y pide distinguir las series por código o descripción

#### Scenario: Borrar serie sin facturas
- **WHEN** el usuario elimina una serie que no tiene ninguna factura
- **THEN** la serie desaparece del listado de series

#### Scenario: Borrar serie con facturas
- **WHEN** el usuario intenta eliminar una serie que tiene alguna factura, activa o histórica
- **THEN** la aplicación impide el borrado e informa de que la serie tiene facturas y el histórico no se elimina

#### Scenario: Correlativo independiente por año
- **WHEN** la serie C tiene facturas hasta el correlativo 57 en 2026 y el usuario arranca con fecha de trabajo en 2025 y crea una factura
- **THEN** la aplicación propone el correlativo 1 para 2025, porque la serie no tiene facturas de ese año, y la numeración de 2026 no cambia

#### Scenario: Reutilización de números borrados
- **WHEN** la serie C tiene en 2026 facturas con los correlativos 1, 3 y 5, porque las del 2 y el 4 se borraron
- **THEN** al crear una factura en 2026 la aplicación ofrece el correlativo 2, el menor hueco libre, o continuar con el 6

#### Scenario: Reutilización de anulados limitada al año
- **WHEN** la serie C tiene en 2025 una factura anulada con correlativo 5, el mayor de ese año, y el usuario crea otra factura en 2025
- **THEN** la aplicación propone el correlativo 6: la factura anulada conserva el 5 y no se vuelve a ofrecer

#### Scenario: El siguiente número sale de las facturas
- **WHEN** la serie A tiene en 2026 las facturas 1, 2 y 3, y el usuario crea otra factura de 2026
- **THEN** la aplicación propone el correlativo 4

#### Scenario: Borrar la última factura libera su número
- **WHEN** la serie A tiene en 2026 las facturas 1, 2 y 3 y el usuario borra la 3
- **THEN** al crear otra factura de 2026 la aplicación propone el correlativo 3

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; los tipos de retención de IRPF; las series (crear y configurar en su ficha, ver el siguiente número y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la acción «Cambiar de empresa», que vuelve a la pantalla de arranque. El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, última carpeta de exportación y tema de apariencia. El tema SHALL guardarse en cada empresa, igual que el color del PDF, de modo que cada empresa conserve su propio tema. Al entrar en una empresa o cambiar a otra SHALL aplicarse el tema guardado en ella y, si nunca se ha guardado ninguno, el tema biblioteca8. La pantalla de arranque, que se muestra antes de elegir empresa, SHALL usar el tema de la última empresa abierta. El tamaño y la posición de la ventana SHALL NOT recordarse: cada pantalla se abre con el tamaño que le corresponde.

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
- **WHEN** el usuario abre Configuración → Series
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
- **WHEN** el usuario abre la sección Empresa de Configuración en el tamaño mínimo de ventana
- **THEN** los campos Nombre / razón social, Actividad y Localidad se ven enteros, sin recortes

#### Scenario: La ventana no recuerda posición ni tamaño
- **WHEN** el usuario cierra la aplicación con la ventana movida y vuelve a abrirla
- **THEN** la pantalla se abre con su tamaño propio y no se guarda la posición ni el tamaño anteriores

### Requirement: Ventana

La aplicación SHALL abrir su ventana siempre a 1024x768 y centrada en la pantalla principal, en la primera ejecución y en todas las siguientes. La posición y el tamaño de la ventana SHALL seguir guardándose al cerrar la aplicación, pero SHALL NOT usarse al abrirla: la aplicación SHALL ignorar cualquier posición o tamaño guardados. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo y maximizar la ventana durante la sesión. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible. El arranque (selección de empresa) SHALL mostrarse en una ventana propia, fija y pequeña de 760x520, también centrada. Al entrar en una empresa, la aplicación SHALL abrir la ventana principal a 1024x768 y centrada con la primera pantalla, y SHALL cerrar la ventana de arranque.

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
- **THEN** en las tres la tabla ocupa el ancho disponible y los botones Nuevo, Editar y Eliminar se ven completos debajo

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

#### Scenario: Corrección al navegar desde una vista pequeña
- **WHEN** el usuario pulsa Entrar en la ventana de arranque (760x520)
- **THEN** se abre la ventana principal a 1024x768 y centrada con el menú u otra vista principal, sin necesidad de redimensionar o maximizar manualmente
- **AND** la ventana de arranque se cierra
