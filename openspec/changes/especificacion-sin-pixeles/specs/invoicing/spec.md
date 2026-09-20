## ADDED Requirements

### Requirement: Apariencia de la interfaz

La aplicación SHALL presentar la misma apariencia en todas sus pantallas. Los detalles de acabado —colores, sombreados, márgenes, tamaños de letra y de icono, esquinas y animaciones— SHALL definirse en las hojas de estilo (`base.css` y un fichero por tema) y SHALL NOT describirse en esta especificación.

Las reglas de apariencia que sí SHALL cumplirse son estas:

La aplicación SHALL ofrecer los temas de apariencia biblioteca8 (predeterminado), omarchy, esmeralda, terracota, negro-dorado, sakura y neon. El tema SHALL elegirse en Configuración, SHALL guardarse en cada empresa y SHALL aplicarse a todas las pantallas y a los diálogos de aviso. La pantalla de arranque SHALL usar el tema de la última empresa abierta.

Con cualquier tema, todo el texto de la interfaz SHALL leerse con claridad sobre su fondo, incluido el texto de ayuda de los campos vacíos.

En el tamaño mínimo de ventana, ninguna pantalla SHALL recortar ni ocultar controles: las filas de campos y de botones SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo unido cada grupo de botones.

Una misma acción SHALL llamarse igual en todas las pantallas, y su botón SHALL nombrar la acción, no el objeto. Dentro de una misma barra, todos los botones SHALL presentarse con el mismo criterio de icono, etiqueta y tamaño.

Cada diálogo de aviso SHALL mostrar el icono correspondiente a su tipo —información, error o pregunta— y el icono de la aplicación en su ventana.

La negrita SHALL reservarse a los importes; ningún otro texto de la interfaz SHALL mostrarse en negrita.

#### Scenario: Cambiar el tema
- **WHEN** el usuario elige otro tema en Configuración y guarda
- **THEN** la interfaz cambia de tema y ese tema queda guardado en la empresa activa

#### Scenario: Texto legible en cualquier tema
- **WHEN** el usuario abre Clientes o el Histórico con un tema oscuro
- **THEN** los textos y el texto de ayuda de los campos se leen con claridad

#### Scenario: Ninguna pantalla recorta contenido
- **WHEN** el usuario recorre las pantallas con la ventana en su tamaño mínimo
- **THEN** todos los campos y botones siguen visibles, reorganizados en varias líneas si hace falta

#### Scenario: La misma acción se llama igual
- **WHEN** el usuario compara el botón de borrar de Clientes con el del Histórico
- **THEN** los dos usan la misma palabra para esa acción

#### Scenario: Iconos en los diálogos
- **WHEN** la aplicación muestra un aviso de información, de error o de confirmación
- **THEN** el diálogo muestra el icono de su tipo y la ventana muestra el icono de la aplicación

#### Scenario: La negrita solo en los importes
- **WHEN** el usuario mira cualquier pantalla de la aplicación
- **THEN** solo los importes de las filas de totales se muestran en negrita

## REMOVED Requirements

### Requirement: Barra de acciones del editor sin desbordamiento

### Requirement: Distribución estable al redimensionar en Editor e Histórico

### Requirement: Iconos en los diálogos de aviso

### Requirement: Pantalla de generación mensual con estética alineada

### Requirement: Estilo de zona de acciones en tema por defecto

### Requirement: Sistema de diseño visual Apple

### Requirement: Microinteracciones visuales

### Requirement: Tamaños de ventana por vista

### Requirement: Menú principal adaptado a 1024×768

### Requirement: Editor legible sin scroll en facturas cortas

### Requirement: Criterio de etiquetado de botones

### Requirement: Botones de acción con icono identificativo

### Requirement: Sombreado uniforme de los botones de solo texto

### Requirement: Composición centrada del menú principal

### Requirement: Marca en la pantalla de arranque

### Requirement: Consonancia tipográfica de la interfaz

### Requirement: La negrita queda reservada a los importes

### Requirement: Iconos de tamaño uniforme dentro de una barra

### Requirement: Barra de acciones sobre franja propia en Editor, Clientes e Histórico

### Requirement: Temas y apariencia
