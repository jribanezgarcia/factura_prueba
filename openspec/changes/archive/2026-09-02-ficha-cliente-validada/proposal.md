## Why

El formulario de alta y edicion de cliente se queda corto y desentona con el resto de la aplicacion:

- **El campo Direccion es demasiado estrecho.** El dialogo no fija ancho y el `GridPane` no tiene `ColumnConstraints`, asi que todos los campos salen al ancho por defecto y una direccion normal no se lee entera.
- **No hay validacion de codigo postal ni de email.** El codigo postal es justamente el dato que mas problemas da al facturar, porque muchas veces el cliente no lo aporta.
- **El dialogo no aplica el tema.** Y no es solo este: **ningun dialogo de la aplicacion lo hace**, ni los avisos de error, ni las confirmaciones. Todos salen con el gris por defecto de JavaFX, al lado de una aplicacion que tiene siete paletas de color.
- `.dialog-card` esta definido en `base.css` pero **no lo usa nadie**, y ademas solo declara radio, grosor de borde y padding, sin colores: tal cual esta, no pinta nada.

Lo que **si** esta bien y no hay que tocar: la validacion de NIF ya existe y esta bien resuelta, con `DocumentoFiscalValidator.esValido(...)`, borde rojo, aviso, revalidacion al perder el foco y un `addEventFilter` que bloquea el guardado.

## What Changes

- **Helper de tema para dialogos** en `Dialogos`, aplicado a todos: error, informacion, confirmacion, confirmacion de cambios sin guardar, modo de guardado de version y la ficha de cliente. Arregla el aspecto de los dialogos de toda la aplicacion, no solo de este formulario.
- **`.dialog-card` se completa** con fondo y borde derivados del tema, igual que hace `.card`.
- **El dialogo de cliente crece**: ancho del orden del doble del actual, con `ColumnConstraints` para que los campos se ensanchen de verdad, y **Direccion ocupando la fila entera**.
- **Validacion de codigo postal, obligatoria**: cinco digitos con las dos primeras cifras entre 01 y 52. **No se admite en blanco**, a diferencia del NIF y del email, porque es dato necesario para facturar.
- **Validacion de email, opcional**: se admite en blanco; si tiene contenido, se comprueba con un patron razonable.
- Ambas validaciones **copian el patron que ya usa el NIF**: borde rojo, aviso y bloqueo del guardado.
- El dialogo pasa a tener `initOwner`, que hoy no tiene.

## Capabilities

### New Capabilities
- Validacion de codigo postal y de email en la ficha de cliente.

### Modified Capabilities
- Ninguna en cuanto a comportamiento de negocio; el resto es presentacion.

## Impact

- src/main/java/com/alcazaba/facturacion/ui/ClientesController.java
- src/main/java/com/alcazaba/facturacion/ui/Dialogos.java
- src/main/java/com/alcazaba/facturacion/util/ (validadores nuevos de CP y email)
- src/main/resources/com/alcazaba/facturacion/themes/base.css
- src/test/java/com/alcazaba/facturacion/util/ (tests de los validadores nuevos)
