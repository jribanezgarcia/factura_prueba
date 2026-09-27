## ADDED Requirements

### Requirement: Errores inesperados

Cuando se produzca un error que ninguna pantalla haya previsto, la aplicación SHALL mostrar un aviso «Error inesperado» con el mensaje del error, o con su tipo si no tiene mensaje, y SHALL seguir abierta. La aplicación SHALL añadir al fichero `errores.log` de su carpeta de datos la fecha y la hora, el tipo, el mensaje y el detalle completo de cada error inesperado, para poder saber qué pasó aunque no se repita. Si el fichero no se puede escribir, la aplicación SHALL seguir funcionando igualmente.

#### Scenario: Error no previsto en una pantalla
- **WHEN** una acción del usuario provoca un error que la pantalla no captura
- **THEN** la aplicación muestra el aviso «Error inesperado» con el mensaje del error
- **AND** la aplicación sigue abierta y se puede seguir trabajando

#### Scenario: El error queda registrado
- **WHEN** se produce un error inesperado
- **THEN** en `errores.log` de la carpeta de datos se añade una entrada con la fecha y la hora, el tipo del error, su mensaje y el detalle de dónde ocurrió

#### Scenario: Error sin mensaje
- **WHEN** el error inesperado no trae mensaje
- **THEN** el aviso muestra el tipo del error en su lugar

## MODIFIED Requirements

### Requirement: Identidad de la aplicación en la interfaz

La aplicación SHALL mostrar un icono de aplicación propio en cada una de sus ventanas. El icono SHALL aplicarse a la ventana principal y a las ventanas secundarias (`Stage`) que la aplicación abre, de modo que se vea en la barra de tareas, en la esquina de la ventana y en la vista minimizada. La ventana principal SHALL mostrarse siempre con un título compuesto por la marca «CaboFactu®», un espacio y el nombre de la pantalla activa, escrito con sus tildes. Las ventanas secundarias SHALL mostrar el mismo prefijo de marca delante de su propio título («CaboFactu® » + título).

#### Scenario: Icono en la ventana principal
- **WHEN** la aplicación inicia su ventana principal
- **THEN** la ventana muestra el icono de aplicación en su barra de título, en la barra de tareas de Windows y en la vista minimizada

#### Scenario: Icono en ventanas secundarias
- **WHEN** la aplicación abre una ventana secundaria de tipo `Stage` (p. ej. el diálogo «Generar facturas mensuales»)
- **THEN** esa ventana muestra el mismo icono de aplicación en su barra de título y en la barra de tareas de Windows

#### Scenario: Título de la ventana principal por pantalla
- **WHEN** el usuario navega entre las pantallas de la aplicación (Menú Principal, Histórico, Configuración, Editor, Clientes o Copias)
- **THEN** la ventana principal se titula «CaboFactu® <nombre de la pantalla actual>»
- **AND** la ventana de arranque, que es una ventana propia, se titula «CaboFactu® Selección de empresa»

#### Scenario: Título con prefijo de marca en ventanas secundarias
- **WHEN** se abre una ventana secundaria de tipo `Stage` con su propio título
- **THEN** el título mostrado es «CaboFactu® <título propio de la ventana>»

### Requirement: Datos de demostración recreables

La aplicación SHALL disponer de un juego de datos de demostración que se pueda cargar bajo demanda, para probar sin teclear facturas a mano.

Los datos de demostración SHALL versionarse junto al esquema, de modo que un cambio de esquema pueda actualizarlos en el mismo sitio.

Los datos de demostración SHALL NOT depender de los identificadores concretos que el esquema asigne a sus filas sembradas: SHALL referirse a ellas por su nombre, de modo que cambiar el orden de las siembras no altere lo que la demostración representa.

La carga SHALL ser repetible: ejecutarla dos veces SHALL dejar el mismo resultado, sin datos duplicados.

Cuando la demostración no se pueda recrear porque su base está en uso, la carga SHALL detenerse con un mensaje que diga que hay que cerrar la aplicación, y SHALL NOT continuar sobre una demostración a medio borrar.

Los datos de demostración SHALL ser ficticios y SHALL NOT contener datos reales de ningún cliente.

La empresa de demostración SHALL traer un logo ficticio y la cabecera del PDF en modo logo, para que se vea cómo queda el logo en el menú, en el editor y en el PDF sin configurar nada. El logo SHALL ir dentro de la aplicación y SHALL copiarse a la carpeta de datos de la demostración al cargarla.

La demostración SHALL cubrir los casos que cuestan de montar a mano: varias series, una factura con varios tipos de IVA, una con descuento global, una con retención, una con suplido, una anulada y una rectificativa.

#### Scenario: Cargar la demostración
- **WHEN** el usuario carga los datos de demostración
- **THEN** existe una empresa de demostración con clientes, series y facturas de ejemplo
- **AND** entre ellas hay una con varios tipos de IVA, una con descuento, una con retención, una con suplido, una anulada y una rectificativa

#### Scenario: Cargar la demostración dos veces
- **WHEN** el usuario carga los datos de demostración sobre una empresa de demostración que ya existía
- **THEN** la empresa queda con los mismos datos que la primera vez, sin duplicados

#### Scenario: La demostración no depende del orden de las siembras
- **WHEN** cambia el orden en que el esquema siembra los tipos de IVA
- **THEN** cada línea de la demostración sigue llevando el tipo de IVA que le corresponde por su nombre

#### Scenario: Cargar la demostración con la aplicación abierta
- **WHEN** el usuario carga los datos de demostración mientras la aplicación tiene abierta esa misma empresa
- **THEN** la carga se detiene indicando que hay que cerrar la aplicación
- **AND** la demostración anterior queda intacta, no a medio borrar

#### Scenario: Los datos de demostración son ficticios
- **WHEN** alguien revisa la empresa de demostración
- **THEN** ningún cliente, NIF ni dirección corresponde a una persona o empresa real

#### Scenario: La demostración trae logo
- **WHEN** se carga la empresa de demostración
- **THEN** la empresa tiene un logo que existe en su carpeta de datos y la cabecera del PDF en modo logo
- **AND** el menú principal, el editor y el PDF de sus facturas muestran ese logo
