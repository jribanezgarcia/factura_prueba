## ADDED Requirements

### Requirement: Sistema de diseño visual Apple

La aplicación SHALL aplicar un sistema de diseño visual inspirado en Ajustes de Apple: jerarquía clara, espaciado generoso, agrupación de controles en secciones con fondo de tarjeta, esquinas redondeadas, tipografía con pesos diferenciados y una paleta de acentos coherente. Todos los controles interactivos (botones, campos, tablas, listas) SHALL mostrar un estado visual de `:hover` y `:focused` inmediato y sutil. Los formularios SHALL alinear etiquetas y campos con una cuadrícula coherente. Las tablas y listas SHALL usar filas de altura uniforme, separación clara y estado seleccionado visible pero no agresivo.

#### Scenario: Pantallas con tarjetas de sección
- **WHEN** el usuario abre Configuración, el Editor o el Histórico
- **THEN** los controles se agrupan en secciones con fondo de tarjeta, esquinas redondeadas y separación respecto al fondo de la ventana

#### Scenario: Estados de foco visibles
- **WHEN** el usuario navega por el formulario con Tab o hace clic en un campo
- **THEN** el control enfocado muestra un anillo o borde de acento sutil y el botón bajo el cursor cambia de tono sin esperar al clic

#### Scenario: Tipografía jerárquica
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los títulos son más grandes y en negrita, los subtítulos usan un peso intermedio y los datos secundarios aparecen en un tono más tenue

#### Scenario: Tablas limpias
- **WHEN** el usuario abre el Histórico
- **THEN** la tabla tiene cabecera clara, filas de altura uniforme, separadores suaves y la fila seleccionada se destaca con el color de acento muy suave

### Requirement: Microinteracciones visuales

La interfaz SHALL incluir microinteracciones suaves que refuercen la sensación de pulido: transiciones cortas en cambios de color de botones, suavizado en el cambio de foco y, cuando sea técnicamente viable sin bloquear la interacción, una transición ligera al cambiar entre pantallas. Las animaciones SHALL ser cortas (menos de 200 ms) y no SHALL bloquear la entrada del usuario. Si el sistema operativo indica preferencia por movimiento reducido, las animaciones se reducirán o desactivarán.

#### Scenario: Hover en botón primario
- **WHEN** el usuario pasa el ratón por encima de un botón primario
- **THEN** el fondo del botón cambia a un tono ligeramente más oscuro de forma suave

#### Scenario: Transición de foco en campo
- **WHEN** el usuario hace clic en un campo de texto
- **THEN** el borde de acento aparece con una transición suave en lugar de un cambio brusco

#### Scenario: Movimiento reducido
- **WHEN** el sistema operativo tiene activada la opción de reducir animaciones
- **THEN** la aplicación omite o acorta las transiciones visuales


