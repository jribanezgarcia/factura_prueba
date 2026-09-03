## MODIFIED Requirements

### Requirement: Cambios sin guardar

Si hay cambios sin guardar, la aplicación SHALL ofrecer tres opciones: Guardar y volver/salir, Descartar cambios y volver/salir, y Cancelar. Si no hay cambios, la aplicación SHALL permitir salir normalmente.

La confirmación SHALL pedirse ante **cualquier** navegación que abandone una vista con cambios sin guardar, no solo al pulsar Volver o al cerrar la aplicación: los botones de la barra de navegación, las entradas del menú principal y la apertura de una factura desde el Histórico o desde Versiones SHALL pasar por la misma confirmación. Si el usuario cancela, la aplicación SHALL permanecer en la vista actual sin cambiar de pantalla.

La confirmación SHALL mostrarse **una sola vez** por cada gesto del usuario.

#### Scenario: Cerrar con cambios sin guardar
- **WHEN** el usuario intenta volver o cerrar con cambios sin guardar
- **THEN** se muestra la confirmación con las opciones Guardar, Descartar o Cancelar

#### Scenario: Navegar desde la barra con cambios sin guardar
- **WHEN** el usuario tiene una factura a medias y pulsa un botón de la barra de navegación (Menú principal, Histórico, Clientes, Configuración o Copia de seguridad)
- **THEN** se muestra la confirmación de cambios sin guardar antes de cambiar de pantalla

#### Scenario: Cancelar la navegación
- **WHEN** el usuario elige Cancelar en la confirmación de cambios sin guardar
- **THEN** la aplicación permanece en la vista actual con sus datos intactos y no se carga la pantalla destino

#### Scenario: La confirmación no se repite
- **WHEN** el usuario pulsa Volver o Nueva factura en el editor con cambios sin guardar
- **THEN** la confirmación aparece una sola vez, y al elegir Guardar la factura se guarda una sola vez antes de navegar
