## MODIFIED Requirements

### Requirement: Ventana

La aplicación SHALL abrir su ventana siempre a 1024x768 y centrada en la pantalla principal, en la primera ejecución y en todas las siguientes. La posición y el tamaño de la ventana SHALL seguir guardándose al cerrar la aplicación, pero SHALL NOT usarse al abrirla: la aplicación SHALL ignorar cualquier posición o tamaño guardados. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo y maximizar la ventana durante la sesión. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico y las filas de alta rápida de IVA y Series en Configuración SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible. El arranque (selección de empresa) SHALL mostrarse en una ventana propia, fija y pequeña de 760x520, también centrada. Al entrar en una empresa, la aplicación SHALL abrir la ventana principal a 1024x768 y centrada con la primera pantalla, y SHALL cerrar la ventana de arranque.

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
- **WHEN** la ventana está al mínimo 1024x768 y se abren las pestañas IVA o Series de Configuración
- **THEN** los campos de alta se reorganizan sin cortarse y los botones Nuevo, Guardar e Inactivar/Activar (o Nuevo y Guardar en Series) permanecen visibles y agrupados

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

#### Scenario: Corrección al navegar desde una vista pequeña
- **WHEN** el usuario pulsa Entrar en la ventana de arranque (760x520)
- **THEN** se abre la ventana principal a 1024x768 y centrada con el menú u otra vista principal, sin necesidad de redimensionar o maximizar manualmente
- **AND** la ventana de arranque se cierra

### Requirement: Identidad de la aplicación en la interfaz

La aplicación SHALL mostrar un icono de aplicación propio en cada una de sus ventanas. El icono SHALL aplicarse a la ventana principal y a las ventanas secundarias (`Stage`) que la aplicación abre, de modo que se vea en la barra de tareas, en la esquina de la ventana y en la vista minimizada. La ventana principal SHALL mostrarse siempre con un título compuesto por la marca «CaboFactu®», un espacio y el nombre de la pantalla activa. Las ventanas secundarias SHALL mostrar el mismo prefijo de marca delante de su propio título («CaboFactu® » + título).

#### Scenario: Icono en la ventana principal
- **WHEN** la aplicación inicia su ventana principal
- **THEN** la ventana muestra el icono de aplicación en su barra de título, en la barra de tareas de Windows y en la vista minimizada

#### Scenario: Icono en ventanas secundarias
- **WHEN** la aplicación abre una ventana secundaria de tipo `Stage` (p. ej. el diálogo «Generar facturas mensuales»)
- **THEN** esa ventana muestra el mismo icono de aplicación en su barra de título y en la barra de tareas de Windows

#### Scenario: Título de la ventana principal por pantalla
- **WHEN** el usuario navega entre las pantallas de la aplicación (Menú Principal, Histórico, Configuración, Editor, Clientes, Versiones o Copias)
- **THEN** la ventana principal se titula «CaboFactu® <nombre de la pantalla actual>»
- **AND** la ventana de arranque, que es una ventana propia, se titula «CaboFactu® Seleccion de empresa»

#### Scenario: Título con prefijo de marca en ventanas secundarias
- **WHEN** se abre una ventana secundaria de tipo `Stage` con su propio título
- **THEN** el título mostrado es «CaboFactu® <título propio de la ventana>»
