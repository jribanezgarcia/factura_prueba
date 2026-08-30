## MODIFIED Requirements

### Requirement: Numeración por series

La aplicación SHALL tener series de numeración. Inicialmente existirán C (Cocinas), P (Puertas) y R (Rectificativas). Cada serie SHALL tener su propio correlativo. El correlativo SHALL ser la identidad de la factura; el componente de fecha (mes o año) SHALL recalcularse según la fecha y guardarse en cada versión. Las series SHALL poder crearse y configurarse desde la aplicación. La aplicación SHALL recordar la última serie utilizada y proponerla al crear la siguiente factura. El número SHALL proponerse automáticamente y poder modificarse manualmente. El número SHALL NOT consumirse hasta que la factura se guarda correctamente. SHALL ser configurable por serie el comportamiento con números anulados: continuar hacia delante o reutilizar números anulados; el comportamiento predeterminado SHALL ser continuar hacia delante. En Configuración → Series el usuario SHALL poder ver las series, ver el siguiente número y modificarlo.

Cada serie SHALL tener un campo \sufijo_fecha\ con tres opciones posibles: \MES\ (comportamiento actual, formato CODIGO-CORRELATIVO/MES), \ANIO\ (formato CODIGO-CORRELATIVO-ANIO o CORRELATIVO-ANIO si no hay código) y \NINGUNO\ (formato CODIGO-CORRELATIVO o solo CORRELATIVO si no hay código). El campo \codigo\ de una serie SHALL poder estar vacío, en cuyo caso el número NO tendrá prefijo de letra. El formato predeterminado para series nuevas SHALL ser \MES\ (comportamiento actual).

#### Scenario: Propuesta de número con formato MES (actual)
- **WHEN** el usuario crea una factura en la serie C con formato MES, fecha 11/08/2026 y el siguiente correlativo es 58
- **THEN** la aplicación propone el número \C-58/8\

#### Scenario: Propuesta de número con formato ANIO sin código
- **WHEN** el usuario crea una factura en una serie sin código, formato ANIO, fecha 15/07/2026 y el siguiente correlativo es 56
- **THEN** la aplicación propone el número \56-2026\

#### Scenario: Propuesta de número con formato ANIO con código
- **WHEN** el usuario crea una factura en la serie C con formato ANIO, fecha 15/07/2026 y el siguiente correlativo es 56
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
- **WHEN** el usuario intenta restaurar una factura anulada cuyo número está ocupado por otra factura activa
- **THEN** la aplicación impide la restauración e informa del motivo

#### Scenario: Configurar formato de serie
- **WHEN** el usuario crea o edita una serie en Configuración y cambia el formato
- **THEN** el ejemplo debajo del desplegable se actualiza mostrando el resultado con correlativo 56 y fecha 15/07/2026
