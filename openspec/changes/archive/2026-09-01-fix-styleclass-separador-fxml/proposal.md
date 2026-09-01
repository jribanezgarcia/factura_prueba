## Why

Los paneles de contenido de cinco pantallas (Backup, Clientes, Configuración, Histórico y Versiones) no reciben su estilo de tarjeta: declaran `styleClass="card zona-contenido"` con un espacio y, como `FXMLLoader` parte la lista por comas, cada nodo acaba con una única clase literal `card zona-contenido` que no casa con ningún selector. El síntoma visible es que esos nodos pierden el fondo de tarjeta, el borde, el radio de 14 px, la sombra y el padding, quedando el contenido pegado al borde — muy probablemente la causa de raíz de las quejas de "contenido pegado al borde" que en 2026-08-31-fix-ui-spacing se parchearon con espaciados manuales en lugar de corregir la causa.

## What Changes

- Cambiar `styleClass="card zona-contenido"` por `styleClass="card, zona-contenido"` en los 15 nodos afectados de `Backup.fxml`, `Clientes.fxml`, `Configuracion.fxml`, `Historico.fxml` y `Versiones.fxml` (mismo arreglo que ya se aplicó en `Editor.fxml`).
- Como consecuencia, esos nodos recuperan el fondo de tarjeta, borde, esquinas redondeadas de 14 px, sombra y padding de 16 px definidos en `base.css` (`.card` línea 10 y `.zona-contenido` línea 6). El padding no se acumula entre ambas clases (gana una sola declaración), por lo que el nodo pasa de 0 a 16 px, no a 32 px.
- Revisar las cinco pantallas a 1024×768 tras el cambio: el nuevo padding y los bordes consumen espacio que hoy no consumen, y no debe introducirse scroll ni recortes.
- Añadir un test antirregresión que cargue cada FXML y verifique que ninguna `styleClass` contiene un espacio.

## Capabilities

### New Capabilities
- (ninguna)

### Modified Capabilities
- `invoicing`: el requisito de diseño visual (tarjetas de sección y márgenes respecto al borde) pasa a cumplirse de verdad en Backup, Clientes, Configuración, Histórico y Versiones, no solo en Editor.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Backup.fxml` (líneas 15, 26, 32)
- `src/main/resources/com/alcazaba/facturacion/ui/Clientes.fxml` (línea 15)
- `src/main/resources/com/alcazaba/facturacion/ui/Configuracion.fxml` (líneas 24, 48, 68, 74, 110, 141, 183, 203, 218)
- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml` (línea 15)
- `src/main/resources/com/alcazaba/facturacion/ui/Versiones.fxml` (línea 15)
- `src/test/java/com/alcazaba/facturacion/ui/` (nuevo test antirregresión)
- Riesgo de impacto visual/espacial en las cinco pantallas a 1024×768; verificar a mano y con los tests de tamaño existentes.