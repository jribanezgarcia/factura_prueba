## MODIFIED Requirements

### Requirement: Identidad de empresa en la interfaz

El menú principal SHALL mostrar el nombre, el NIF y el logo de la empresa configurados. El editor de factura SHALL mostrar el logo de la empresa en su cabecera. Los datos mostrados SHALL tomarse de la configuración de empresa. El recuadro que envuelve al logo (fondo y borde) SHALL rellenarse con los colores del propio logo según su tipo de imagen, de modo que imagen y recuadro se vean como una sola pieza sea cual sea el tema. El recuadro SHALL conservar sus esquinas redondeadas y el grosor de borde que define el tema. Si el logo es principalmente transparente (PNG con canal alfa), el recuadro SHALL mantener el fondo y borde del tema sin cambios. En el editor, el logo SHALL quedar contenido en una caja de tamaño fijo dentro de la cabecera.

#### Scenario: Mostrar datos de empresa en el menú principal
- **WHEN** el usuario abre el menú principal con datos de empresa configurados
- **THEN** se muestran el nombre, el NIF y el logo de la empresa

#### Scenario: Mostrar logo en el editor
- **WHEN** el usuario abre una factura con un logo configurado
- **THEN** el logo de la empresa aparece en la cabecera del editor contenido en una caja de tamaño fijo

#### Scenario: Rellenar el recuadro con un fondo plano y opaco
- **WHEN** el logo tiene un fondo plano y opaco, como un fondo blanco o de un color uniforme
- **THEN** el recuadro adopta ese color exacto tanto en el fondo como en el borde

#### Scenario: Rellenar el recuadro con un fondo difuminado
- **WHEN** el logo es opaco pero sin fondo plano, como una fotografía o un degradado
- **THEN** el recuadro se rellena con una copia ampliada y desenfocada de la propia imagen, sin que el desenfoque se salga del recuadro

#### Scenario: Dejar intacto el recuadro de un logo transparente
- **WHEN** el logo es principalmente transparente
- **THEN** el recuadro mantiene el fondo y el borde del tema, sin cambios
