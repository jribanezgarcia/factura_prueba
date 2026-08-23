## ADDED Requirements

### Requirement: Ventana

La aplicacion SHALL abrir su ventana con las siguientes medidas: en la primera ejecucion (sin preferencias de ventana guardadas) SHALL medir 800x600 y SHALL quedar centrada en la pantalla principal; en ejecuciones posteriores SHALL restaurar el tamano y la posicion guardados de la ultima sesion. El tamano minimo de ventana SHALL ser 800x600 y el usuario SHALL poder redimensionar hasta ese minimo. Con la ventana en su tamano minimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Historico y las filas de alta rapida de IVA y Series en Configuracion SHALL reorganizarse en varias lineas cuando el ancho no baste, manteniendo cada grupo de botones de accion unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible.

#### Scenario: Primera ejecucion abre a 800x600 centrada
- **WHEN** el usuario inicia la aplicacion sin preferencias de ventana guardadas
- **THEN** la ventana mide 800x600 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la ultima sesion
- **WHEN** el usuario cierra la aplicacion tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana recupera el tamano y la posicion de la sesion anterior

#### Scenario: Minimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla mas pequena
- **THEN** la ventana no puede bajar de 800x600

#### Scenario: Filtros del Historico con ventana minima
- **WHEN** la ventana esta al minimo 800x600 y se abre el Historico
- **THEN** todos los filtros siguen visibles reorganizados en varias lineas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rapidas de IVA y Series con ventana minima
- **WHEN** la ventana esta al minimo 800x600 y se abren las pestanas IVA o Series de Configuracion
- **THEN** los campos de alta se reorganizan sin cortarse y los botones Nuevo, Guardar e Inactivar/Activar (o Nuevo y Guardar en Series) permanecen visibles y agrupados

#### Scenario: Cabecera del Editor con ventana minima
- **WHEN** la ventana esta al minimo 800x600 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana
