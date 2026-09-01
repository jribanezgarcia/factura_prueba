## MODIFIED Requirements

### Requirement: Sistema de diseño visual Apple

La aplicación SHALL aplicar un sistema de diseño visual inspirado en Ajustes de Apple: jerarquía clara, espaciado generoso, agrupación de controles en secciones con fondo de tarjeta, esquinas redondeadas, tipografía con pesos diferenciados y una paleta de acentos coherente. Todos los controles interactivos (botones, campos, tablas, listas) SHALL mostrar un estado visual de `:hover` y `:focused` inmediato y sutil. Los formularios SHALL alinear etiquetas y campos con una cuadrícula coherente. Las tablas y listas SHALL usar filas de altura uniforme, separación clara y estado seleccionado visible pero no agresivo. Además, los paneles de contenido SHALL mantener un margen claro respecto al borde de la ventana y respecto a la barra de menú superior, de modo que los campos y tarjetas no queden pegados al borde ni se perciban solapados con la navegación. En cada pantalla principal, los paneles de contenido SHALL aplicarse como tarjetas con fondo, borde, esquinas redondeadas y espaciado interior; ningún panel SHALL quedarse sin ese estilo por una clase de estilo mal declarada.

#### Scenario: Pantallas con tarjetas de sección
- **WHEN** el usuario abre Configuración, el Editor, el Histórico, Clientes, Versiones o Backup
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

#### Scenario: Margen respecto al borde de la ventana
- **WHEN** el usuario abre cualquier pantalla principal
- **THEN** los paneles de contenido mantienen un margen visible respecto al borde izquierdo, derecho, superior e inferior de la ventana

#### Scenario: Separación respecto a la barra de menú
- **WHEN** el usuario abre el Histórico o la Configuración
- **THEN** existe una separación clara entre la barra de menú superior y la primera fila de controles o pestañas

#### Scenario: Filtros sin tocar el borde izquierdo
- **WHEN** el usuario abre el Histórico
- **THEN** el campo "Serie" y los demás filtros no están pegados al borde izquierdo de su tarjeta

#### Scenario: Ningún panel pierde el estilo de tarjeta
- **WHEN** la aplicación carga cualquiera de las pantallas principales
- **THEN** cada panel de contenido recibe las clases de estilo que declara y ninguno queda con una única clase errónea que impida aplicar el fondo de tarjeta, el borde, las esquinas redondeadas ni el espaciado