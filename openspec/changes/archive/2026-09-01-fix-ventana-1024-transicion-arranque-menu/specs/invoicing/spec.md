# invoicing Delta Specification

## MODIFIED Requirements

### Requirement: Ventana

La aplicación SHALL abrir su ventana con las siguientes medidas: en la primera ejecución (sin preferencias de ventana guardadas) SHALL medir 1024x768 y SHALL quedar centrada en la pantalla principal; en ejecuciones posteriores SHALL restaurar la posición y, como máximo, el tamaño guardados de la última sesión, sin bajar nunca de 1024x768 en las vistas principales. El tamaño mínimo de las vistas principales SHALL ser 1024x768 y el usuario SHALL poder redimensionar hasta ese mínimo. Ningún tamaño de ventana guardado inferior a 1024x768 SHALL hacer que una vista principal se muestre recortada: la aplicación SHALL corregirlo al entrar en el menú. Con la ventana en su tamaño mínimo, ninguna pantalla SHALL recortar ni ocultar controles: los filtros del Histórico y las filas de alta rápida de IVA y Series en Configuración SHALL reorganizarse en varias líneas cuando el ancho no baste, manteniendo cada grupo de botones de acción unido, y los campos de la cabecera del Editor SHALL repartirse el ancho disponible. El arranque (selección de empresa) es una pantalla fija pequeña de 760x520; al pasar de ella a una vista principal con tamaño mínimo 1024x768 con la ventana ya visible, la aplicación SHALL hacer crecer la ventana hasta ese mínimo.

#### Scenario: Primera ejecución abre a 1024x768 centrada
- **WHEN** el usuario inicia la aplicación sin preferencias de ventana guardadas
- **THEN** la ventana mide 1024x768 y aparece centrada en la pantalla principal

#### Scenario: Siguientes ejecuciones restauran la última sesión
- **WHEN** el usuario cierra la aplicación tras moverla o redimensionarla y vuelve a abrirla
- **THEN** la ventana recupera la posición y el tamaño (nunca inferior a 1024x768 en las vistas principales) de la sesión anterior

#### Scenario: Tamaño guardado inferior al mínimo
- **WHEN** la aplicación encuentra un tamaño de ventana guardado inferior a 1024x768
- **THEN** al entrar en el menú la ventana se corrige a 1024x768 y no se muestra recortada

#### Scenario: Mínimo de redimensionado
- **WHEN** el usuario arrastra el borde de la ventana para hacerla más pequeña
- **THEN** la ventana no puede bajar de 1024x768

#### Scenario: Filtros del Histórico con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre el Histórico
- **THEN** todos los filtros siguen visibles reorganizados en varias líneas y los botones Exportar PDF, Buscar y Volver permanecen accesibles

#### Scenario: Altas rápidas de IVA y Series con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abren las pestañas IVA o Series de Configuración
- **THEN** los campos de alta se reorganizan sin cortarse y los botones Nuevo, Guardar e Inactivar/Activar (o Nuevo y Guardar en Series) permanecen visibles y agrupados

#### Scenario: Cabecera del Editor con ventana mínima
- **WHEN** la ventana está al mínimo 1024x768 y se abre una factura
- **THEN** los campos de la cabecera se reparten el ancho disponible sin salirse de la ventana

#### Scenario: Corrección al navegar desde una vista pequeña
- **WHEN** la aplicación pasa de la pantalla de arranque (760x520) al menú u otra vista principal con tamaño mínimo 1024x768 con la ventana ya visible
- **THEN** la ventana crece a 1024x768 (o hasta el mínimo de la vista de destino) al cargar la vista, sin necesidad de redimensionar o maximizar manualmente
