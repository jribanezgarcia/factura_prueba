## MODIFIED Requirements

### Requirement: Fecha de trabajo

La fecha de trabajo SHALL fijarse en la pantalla de arranque de la aplicación junto con la selección de la empresa y el año del ejercicio fiscal. El usuario SHALL elegir el año del ejercicio en la pantalla de arranque. La fecha de trabajo define el mes y el año utilizados para la numeración de las nuevas facturas (formatos MES y ANIO) y SHALL usarse como valor inicial de la fecha de las nuevas facturas. Si el ejercicio elegido es el año en curso, la fecha de trabajo SHALL fijarse automáticamente a la fecha del sistema (no editable); si el ejercicio es otro año, la fecha de trabajo SHALL pedirse a mano y solo SHALL admitirse dentro de ese ejercicio. La fecha de trabajo SHALL poder cambiarse solo al arrancar de la aplicación. Al cambiar la fecha de una factura dentro del editor, el mes del número propuesto SHALL actualizarse automáticamente.

#### Scenario: Fecha inicial al abrir
- **WHEN** el usuario abre la aplicación
- **THEN** la pantalla de arranque propone el año del sistema como ejercicio fiscal y la última empresa utilizada queda preseleccionada; si el ejercicio es el actual, la fecha de trabajo es la fecha del sistema

#### Scenario: Ejercicio distinto al actual
- **WHEN** el usuario elige en la pantalla de arranque un ejercicio distinto al año en curso
- **THEN** debe indicar manualmente una fecha de trabajo dentro de ese ejercicio; no se admiten fechas fuera de él

#### Scenario: Cambio de fecha de trabajo
- **WHEN** el usuario cambia la fecha de trabajo en la pantalla de arranque y crea después una nueva factura
- **THEN** la factura se inicializa con esa fecha de trabajo

#### Scenario: La fecha de trabajo define mes y año
- **WHEN** el usuario arranca con fecha de trabajo 15/07/2025 y crea facturas en series con formato MES y formato ANIO
- **THEN** los números propuestos usan el mes 7 en el formato MES y el año 2025 en el formato ANIO

### Requirement: Numeración por series

La aplicación SHALL tener series de numeración. En una instalación nueva la aplicación SHALL NO crear ninguna serie por defecto: el listado de series comienza vacío y el usuario las crea a mano. Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse en cada versión. El correlativo de cada serie SHALL ser independiente por ejercicio: cada año de trabajo reinicia su propia cuenta sobre el siguiente número de ese año, sin afectar al correlativo de otros años. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. SHALL ser configurable por serie el comportamiento con números anulados: continuar hacia delante o reutilizar números anulados; el comportamiento predeterminado SHALL ser continuar hacia delante. En Configuración → Series el usuario SHALL poder ver las series, ver el siguiente número y modificarlo.

Cada serie SHALL tener un campo \sufijo_fecha\ con tres opciones posibles: \MES\ (formato CODIGO-CORRELATIVO/MES o CORRELATIVO/MES si no hay código), \ANIO\ (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y \NINGUNO\ (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo \codigo\ de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra; solo SHALL admitirse una serie sin código a la vez, de modo que otra serie en blanco se rechaza y se identifica la serie por su descripción. El formato predeterminado para series nuevas SHALL ser \MES\. La reutilización de números anulados SHALL operar dentro del mismo año: solo se reutilizan correlativos anulados de ese ejercicio y solo se consideran ocupados los correlativos activos de ese ejercicio.

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
- **WHEN** la serie C tiene un siguiente correlativo 58 para 2026 y el usuario arranca con fecha de trabajo en 2025 y crea una factura
- **THEN** la aplicación propone la primera factura de la serie C de 2025 con su correlativo propio de ese año, sin alterar el siguiente correlativo de 2026

#### Scenario: Reutilización de anulados limitada al año
- **WHEN** la serie C tiene en 2025 una factura anulada con correlativo 5 y en 2026 una factura activa con correlativo 5, y la serie reutiliza números anulados
- **THEN** al crear una factura en 2025 la aplicación propone reutilizar el correlativo 5, que está libre solo dentro del ejercicio 2025

### Requirement: Persistencia local

La aplicación SHALL guardar los datos de cada empresa en una base de datos SQLite local dedicada ubicada en una carpeta de datos de la aplicación, separada de la instalación. Cada empresa SHALL tener su propia base de datos: los datos de una empresa SHALL NOT mezclarse con los de otra. La aplicación SHALL ser la vía normal para modificar los datos. La base de datos de la empresa activa SHALL contener su histórico completo y su configuración de empresa. Las operaciones importantes de persistencia SHALL ser transaccionales con confirmación y reversión correctas. No SHALL eliminarse físicamente datos históricos que hayan sido utilizados. La aplicación SHALL ejecutarse como una única instancia a la vez.

#### Scenario: Guardado transaccional
- **WHEN** el usuario guarda una factura con sus líneas y su versión
- **THEN** la operación se confirma de forma atómica o se revierte por completo si falla

#### Scenario: Segunda instancia
- **WHEN** el usuario intenta abrir una segunda instancia de la aplicación
- **THEN** la segunda instancia no se abre y se notifica al usuario

#### Scenario: Aislamiento entre empresas
- **WHEN** el usuario crea facturas en la empresa A, cambia a la empresa B y abre el histórico
- **THEN** en la empresa B no aparecen las facturas de la empresa A

### Requirement: Configuración

La aplicación SHALL tener una pantalla de Configuración que permita configurar: los datos de la empresa (nombre, NIF, dirección, código postal, localidad, provincia y resto de datos necesarios para la cabecera); la cabecera del documento en dos modos, texto con datos de empresa o imagen/logo; el pie con texto legal libre configurable por el usuario; el tema de apariencia de la interfaz; los tipos de IVA; las series (crear/configurar, ver y modificar el siguiente número, configurar la reutilización de números anulados y eliminar series sin facturas); las carpetas de PDF; el color de acento usado en los PDF exportados; y la gestión de empresas (ver el listado de empresas, crear una nueva, cambiar a otra y eliminar una empresa distinta de la actual). El color SHALL guardarse como preferencia `color_pdf`; si nunca se configura, los PDF SHALL usar arena Alcazaba (`#B08D57`), y del color elegido SHALL derivarse el resto de tonos del documento. La aplicación SHALL recordar preferencias de trabajo: última serie utilizada, tamaño/posición de ventana, última carpeta de exportación y tema de apariencia. El tamaño/posición de ventana y el tema SHALL guardarse de forma global, compartidos entre empresas.

#### Scenario: Configurar empresa
- **WHEN** el usuario guarda los datos de la empresa en Configuración
- **THEN** esos datos se usan en las nuevas exportaciones a PDF

#### Scenario: Elegir modo de cabecera
- **WHEN** el usuario selecciona un logo y lo ajusta en tamaño y posición
- **THEN** el PDF usa el logo como cabecera y los datos de empresa permanecen guardados

#### Scenario: Pie legal configurable
- **WHEN** el usuario modifica el texto legal del pie
- **THEN** el nuevo texto se repite en las páginas de los PDF generados

#### Scenario: Modificar siguiente número
- **WHEN** el usuario modifica el siguiente número de una serie en Configuración
- **THEN** la próxima factura de esa serie se propone a partir del nuevo valor

#### Scenario: Cambiar el tema de la interfaz
- **WHEN** el usuario selecciona un tema en Configuración y guarda
- **THEN** el tema se aplica de inmediato y queda guardado para las siguientes sesiones

#### Scenario: Cambiar el color del PDF
- **WHEN** el usuario elige un color en Configuración y guarda
- **THEN** los nuevos PDF usan ese color de acento y sus tonos derivados
- **AND** si se restablece el valor por defecto o la preferencia no existe, se usa `#B08D57`

#### Scenario: Gestionar empresas desde Configuración
- **WHEN** el usuario abre la pestaña Empresas de Configuración
- **THEN** ve el listado de empresas disponibles con su nombre y puede crear, cambiar o eliminar empresas

## ADDED Requirements

### Requirement: Gestión de empresas

La aplicación SHALL permitir gestionar varias empresas con datos totalmente aislados. Cada empresa SHALL tener un nombre visible y un identificador interno (slug) que da nombre a su carpeta de datos. La aplicación SHALL listar las empresas disponibles, crear nuevas empresas, cambiar a una empresa existente y eliminar una empresa distinta de la actual. Al crear una empresa nueva la aplicación SHALL crear su base de datos desde cero con la estructura de tablas completa. La aplicación SHALL recordar de forma global la última empresa utilizada y SHALL preseleccionarla al abrir. Cada empresa SHALL tener su propia configuración de empresa, clientes, series, tipos de IVA y facturas. La empresa actualmente activa SHALL NOT poder eliminarse.

#### Scenario: Crear una nueva empresa
- **WHEN** el usuario crea la empresa «Asesoría María Luisa Ibáñez» desde Configuración
- **THEN** se crea su base de datos con la estructura completa y la empresa aparece en el listado y queda seleccionada

#### Scenario: Nombre visible e identificador interno
- **WHEN** el usuario crea una empresa con nombre «Asesoría María Luisa Ibáñez»
- **THEN** el listado muestra el nombre visible y su carpeta de datos usa el identificador interno correspondiente

#### Scenario: Cambiar a otra empresa
- **WHEN** el usuario elige cambiar a una empresa distinta de la actual
- **THEN** la aplicación pasa a usar la base de datos de esa empresa y sus datos (configuración, clientes, series, facturas) sustituyen a los de la anterior

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