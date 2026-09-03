## ADDED Requirements

### Requirement: Iconos en los diálogos de aviso

Cada diálogo de aviso de la aplicación SHALL mostrar el icono correspondiente a su tipo, de modo que el usuario identifique de un vistazo la índole del aviso. Los diálogos de información SHALL mostrar un icono de información; los diálogos de error SHALL mostrar un icono de alerta/error; y los diálogos de confirmación (incluidos los de cambios sin guardar y de guardar versión) SHALL mostrar un icono de pregunta/confirmación. El icono SHALL dibujarse con el color de acento del tema de apariencia activo para integrarse en cualquiera de los temas. La ventana de cada diálogo de aviso SHALL mostrar además el icono de aplicación de la marca en su barra de título y en la barra de tareas de Windows, igual que el resto de ventanas de la aplicación. Los diálogos SHALL conservar su título, mensaje y botones actuales; la adición de los iconos no SHALL alterar el flujo de confirmación/cancelación ni el contenido textual.

#### Scenario: Diálogo de información con icono de información
- **WHEN** la aplicación muestra un diálogo de información
- **THEN** el diálogo muestra el icono de información junto al mensaje

#### Scenario: Diálogo de error con icono de alerta
- **WHEN** la aplicación muestra un diálogo de error
- **THEN** el diálogo muestra el icono de alerta/error junto al mensaje

#### Scenario: Diálogo de confirmación con icono de pregunta
- **WHEN** la aplicación muestra un diálogo de confirmación (incluidos cambios sin guardar y guardar versión)
- **THEN** el diálogo muestra el icono de pregunta/confirmación junto al mensaje

#### Scenario: Icono con el color de acento del tema
- **WHEN** la aplicación muestra un diálogo de aviso con el tema de apariencia activo
- **THEN** el icono se dibuja con el color de acento de ese tema

#### Scenario: Icono de aplicación en la ventana del diálogo
- **WHEN** la aplicación muestra un diálogo de aviso
- **THEN** la barra de título y la barra de tareas de Windows de esa ventana muestran el icono de aplicación de la marca
