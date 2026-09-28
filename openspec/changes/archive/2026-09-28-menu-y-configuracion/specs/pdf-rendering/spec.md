## MODIFIED Requirements

### Requirement: Vista previa de la cabecera del PDF

La sección Diseño del PDF SHALL mostrar una vista previa de la cabecera del PDF que refleje el modo elegido, el logo de la empresa con su posición y tamaño efectivos y el color de acento configurado, actualizándose al modificar cualquiera de esos valores. La vista previa SHALL usar las mismas reglas de geometría que emplea la generación del PDF, de modo que el tamaño efectivo del logo mostrado coincida con el impreso.

#### Scenario: La previsualización refleja el tamaño real del logo
- **WHEN** la empresa tiene un logo y el usuario elige la cabecera con logotipo
- **THEN** la vista previa muestra el logo al tamaño con el que se imprime dentro de la caja fija del logo

#### Scenario: La previsualización reacciona a los cambios
- **WHEN** el usuario cambia la imagen del logo, el modo de cabecera o el color de acento
- **THEN** la vista previa se actualiza sin necesidad de guardar ni de exportar una factura

#### Scenario: Modo texto
- **WHEN** el usuario elige el modo de cabecera con datos de empresa
- **THEN** la previsualización muestra las líneas de la empresa con el NIF destacado, como aparecen en el PDF

### Requirement: Aviso de logo de baja resolución

Al seleccionar un logo en la sección Logotipo, la aplicación SHALL comprobar si la imagen tiene resolución suficiente para imprimirse dentro de la caja fija del logo. Cuando la imagen tenga menos de 1,5 píxeles por punto del tamaño con el que se dibuja en el PDF, la aplicación SHALL mostrar un aviso informativo indicando que el logo puede verse borroso al imprimir. El aviso SHALL NOT impedir usar la imagen: el logo queda seleccionado igualmente.

#### Scenario: Logo pequeño
- **WHEN** el usuario selecciona como logo una imagen de 128 × 128 píxeles
- **THEN** la aplicación avisa de que el logo puede verse borroso al imprimir
- **AND** la ruta del logo queda puesta y la vista previa del logotipo lo muestra

#### Scenario: Logo con resolución suficiente
- **WHEN** el usuario selecciona como logo una imagen de 1254 × 1254 píxeles
- **THEN** la aplicación no muestra ningún aviso
