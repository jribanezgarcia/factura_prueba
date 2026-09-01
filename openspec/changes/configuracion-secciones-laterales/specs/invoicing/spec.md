## ADDED Requirements

### Requirement: Configuración organizada por secciones

La pantalla de Configuración SHALL organizarse en secciones navegables desde una lista lateral, en lugar de pestañas. Las secciones SHALL ser: Empresa, Cabecera y pie, PDF y apariencia, IVA, Retenciones, Series y Empresas. El botón de guardado general SHALL mostrarse únicamente en las secciones cuyos datos guarda (Empresa, Cabecera y pie, y PDF y apariencia) y SHALL ocultarse en las secciones que se administran fila a fila (IVA, Retenciones, Series y Empresas), donde cada una conserva sus propias acciones. El tema de la aplicación SHALL presentarse en la sección PDF y apariencia, por tratarse de una preferencia global compartida entre empresas.

#### Scenario: Navegación entre secciones
- **WHEN** el usuario selecciona una sección en la lista lateral
- **THEN** el contenido de esa sección ocupa la zona derecha y el resto de secciones queda oculto

#### Scenario: El botón de guardado solo donde aplica
- **WHEN** el usuario abre las secciones de IVA, Retenciones, Series o Empresas
- **THEN** el botón de guardado general no se muestra, y las acciones disponibles son las propias de la sección

#### Scenario: Cada sección cabe sin scroll
- **WHEN** el usuario recorre las secciones con la ventana en su tamaño mínimo de 1024×768
- **THEN** el contenido de cada sección es visible completo sin necesidad de desplazarse

### Requirement: Vista previa de la cabecera del PDF

La sección Cabecera y pie SHALL mostrar una previsualización de la cabecera del PDF que refleje el modo elegido, el logo con su posición y tamaño efectivos y el color de acento configurado, actualizándose al modificar cualquiera de esos valores. La previsualización SHALL usar las mismas reglas de geometría que emplea la generación del PDF, de modo que el tamaño efectivo del logo mostrado coincida con el impreso. La aplicación SHALL indicar junto a los campos de tamaño el tamaño efectivo resultante, y SHALL advertir de que la previsualización es aproximada.

#### Scenario: La previsualización refleja el tamaño real del logo
- **WHEN** el usuario configura un logo de 120 × 60 pt
- **THEN** la previsualización lo muestra al tamaño efectivo con el que se imprime, que es el doble del configurado, y la pantalla indica ese tamaño efectivo

#### Scenario: La previsualización reacciona a los cambios
- **WHEN** el usuario cambia la imagen del logo, su posición, su tamaño o el color de acento
- **THEN** la previsualización se actualiza sin necesidad de guardar ni de exportar una factura

#### Scenario: Modo texto
- **WHEN** el usuario elige el modo de cabecera con datos de empresa
- **THEN** la previsualización muestra las líneas de la empresa con el NIF destacado, como aparecen en el PDF
