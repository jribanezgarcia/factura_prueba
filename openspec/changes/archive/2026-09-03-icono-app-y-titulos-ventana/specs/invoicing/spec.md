## ADDED Requirements

### Requirement: Identidad de la aplicación en la interfaz

La aplicación SHALL mostrar un icono de aplicación propio en cada una de sus ventanas. El icono SHALL aplicarse a la ventana principal y a las ventanas secundarias (`Stage`) que la aplicación abre, de modo que se vea en la barra de tareas, en la esquina de la ventana y en la vista minimizada. La ventana principal SHALL mostrarse siempre con un título compuesto por la marca «CaboFactu®», un espacio y el nombre de la pantalla activa. Las ventanas secundarias SHALL mostrar el mismo prefijo de marca delante de su propio título («CaboFactu® » + título).

#### Scenario: Icono en la ventana principal
- **WHEN** la aplicación inicia su ventana principal
- **THEN** la ventana muestra el icono de aplicación en su barra de título, en la barra de tareas de Windows y en la vista minimizada

#### Scenario: Icono en ventanas secundarias
- **WHEN** la aplicación abre una ventana secundaria de tipo `Stage` (p. ej. el diálogo «Generar facturas mensuales»)
- **THEN** esa ventana muestra el mismo icono de aplicación en su barra de título y en la barra de tareas de Windows

#### Scenario: Título de la ventana principal por pantalla
- **WHEN** el usuario navega entre las pantallas de la aplicación (Menú Principal, Histórico, Configuración, Editor, Clientes, Versiones, Copias o Arranque)
- **THEN** la ventana principal se titula «CaboFactu® <nombre de la pantalla actual>»

#### Scenario: Título con prefijo de marca en ventanas secundarias
- **WHEN** se abre una ventana secundaria de tipo `Stage` con su propio título
- **THEN** el título mostrado es «CaboFactu® <título propio de la ventana>»
