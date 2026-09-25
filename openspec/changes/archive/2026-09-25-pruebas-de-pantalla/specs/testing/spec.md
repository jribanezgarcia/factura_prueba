## ADDED Requirements

### Requirement: Pruebas automáticas

El proyecto SHALL mantener tres capas de pruebas automáticas, ejecutables con una sola orden (`mvn test`): pruebas del negocio y de las clases de datos contra una base de datos temporal, una prueba que cargue cada pantalla FXML y compruebe su cableado, y una prueba de pantalla por cada pantalla de la aplicación que maneje los controles como lo haría el usuario. Las pruebas de pantalla SHALL ejecutarse sin abrir ventanas en el escritorio ni mover el puntero, y SHALL NOT necesitar que nadie conteste a un aviso: cuando una prueba provoque un aviso modal, la propia prueba SHALL cerrarlo. Las pruebas de pantalla SHALL comprobar únicamente lo que ve el usuario —textos, contenido de las tablas, controles activos o desactivados— y SHALL NOT leer los campos internos de los controladores. El toolkit JavaFX SHALL arrancarse desde un único punto del código de pruebas. Las pruebas SHALL trabajar siempre sobre una carpeta temporal y SHALL NOT tocar los datos reales del usuario.

#### Scenario: La batería se ejecuta sin ventanas
- **WHEN** el usuario ejecuta la batería de pruebas con la aplicación cerrada
- **THEN** todas las pruebas se ejecutan sin que aparezca ninguna ventana en el escritorio ni se mueva el puntero
- **AND** los datos reales del usuario quedan intactos

#### Scenario: Un aviso modal no detiene la batería
- **WHEN** una prueba de pantalla provoca un aviso modal de la aplicación
- **THEN** la prueba lo cierra por sí misma y continúa, sin quedarse esperando a nadie

#### Scenario: Una pantalla nueva llega con su prueba
- **WHEN** se añade una pantalla nueva a la aplicación
- **THEN** se añade también su prueba de pantalla, que comprueba lo que el usuario ve al usarla

#### Scenario: Un comportamiento roto se ve en la batería
- **WHEN** un cambio rompe el comportamiento de una pantalla, por ejemplo el número que se propone al crear una factura
- **THEN** la batería de pruebas falla, sin que nadie tenga que abrir la aplicación para darse cuenta

### Requirement: Apariencia comprobada automáticamente

La aplicación SHALL comprobar automáticamente dos cosas de su apariencia: que cada tema define la paleta de colores que las pantallas usan, y que ningún texto de las pantallas se recorta con la ventana en su tamaño mínimo. Las dos comprobaciones SHALL formar parte de la misma batería de pruebas. El resto de la apariencia —que los colores peguen, que los espacios queden bien— SHALL seguir comprobándose a mano.

#### Scenario: Un tema sin paleta hace fallar la batería
- **WHEN** un tema deja de definir los colores de la paleta
- **THEN** la batería de pruebas falla e indica qué tema es

#### Scenario: Un texto recortado hace fallar la batería
- **WHEN** un botón o una etiqueta se queda más estrecho que el texto que tiene que mostrar, con la ventana en su tamaño mínimo
- **THEN** la batería de pruebas falla e indica qué control es
