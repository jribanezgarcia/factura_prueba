## MODIFIED Requirements

### Requirement: Temas y apariencia

La aplicación SHALL disponer de un sistema de temas de apariencia de la interfaz compuesto por una hoja de estilos base común y temas de colores. La aplicación SHALL ofrecer los temas biblioteca8 (predeterminado), omarchy, esmeralda, terracota, negro-dorado, sakura y neon. El tema activo SHALL aplicarse a cada pantalla al cargarla, SHALL poder cambiarse desde Configuración y SHALL recordarse entre sesiones.

El texto de ayuda de los campos (prompt) SHALL leerse con claridad sobre el fondo de cada tema, incluidos los oscuros. El texto ya escrito no cambia.

#### Scenario: Cambiar el tema al vuelo
- **WHEN** el usuario selecciona un tema distinto en Configuración
- **THEN** la interfaz cambia de tema de inmediato

#### Scenario: Tema recordado entre sesiones
- **WHEN** el usuario reinicia la aplicación después de guardar un tema en Configuración
- **THEN** el tema guardado se aplica en todas las pantallas

#### Scenario: Tema predeterminado
- **WHEN** el usuario abre la aplicación sin un tema guardado
- **THEN** se aplica el tema biblioteca8

#### Scenario: El texto de ayuda se lee en los temas oscuros
- **WHEN** el usuario abre Clientes o el Histórico con un tema oscuro
- **THEN** el texto de ayuda del campo de búsqueda se lee con claridad sin haber escrito nada
