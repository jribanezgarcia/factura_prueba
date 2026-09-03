## ADDED Requirements

### Requirement: Pantalla de generación mensual con estética alineada

La pantalla de generación de facturas mensuales SHALL presentar la misma estética visual que el resto de la aplicación. El título de la pantalla SHALL distinguirse con el mismo estilo de título del resto de ventanas, mostrándose en negrita y con el color de texto del tema de apariencia activo. Las etiquetas del formulario (cliente, serie, año, mes de inicio, mes de fin, día del mes, tipo de IVA y retención de IRPF) SHALL mostrarse con una separación clara respecto a los campos y a los bordes del panel, sin quedar pegadas, y SHALL usar el mismo estilo de etiqueta de formulario que el resto de pantallas. El panel que contiene el formulario SHALL usar un fondo neutro acorde con la ventana (no un fondo blanco plano contrastado) y coherente con el resto de pantallas de la aplicación. La pantalla SHALL conservar los mismos campos, controles, botones y flujo de generación actuales; la modificación es exclusivamente de apariencia.

#### Scenario: Título destacado con el color del tema
- **WHEN** la aplicación muestra la pantalla de generación de facturas mensuales
- **THEN** el título «Generar facturas mensuales» se muestra con el mismo estilo destacado (negrita y color de texto del tema) que los títulos del resto de pantallas

#### Scenario: Etiquetas separadas de los campos y del borde
- **WHEN** la aplicación muestra el formulario de la pantalla de generación mensual
- **THEN** cada etiqueta (cliente, serie, año, mes de inicio, mes de fin, día del mes, tipo de IVA, retención de IRPF) se muestra con separación clara respecto al borde del panel y a su campo, usando el estilo de etiqueta de formulario del resto de pantallas

#### Scenario: Panel con fondo neutro
- **WHEN** la aplicación muestra el panel del formulario de la pantalla de generación mensual
- **THEN** el panel se muestra con un fondo neutro del tema acorde con la ventana, y no como un bloque blanco plano contrastado

#### Scenario: Los campos y el flujo se mantienen
- **WHEN** el usuario abre la pantalla de generación mensual tras el cambio de apariencia
- **THEN** los mismos campos, controles, botones y el flujo de generación siguen disponibles y se comportan igual que antes
