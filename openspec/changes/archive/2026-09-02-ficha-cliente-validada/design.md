## Componentes Afectados

- `ClientesController.dialogoCliente(...)`: el formulario, construido en codigo y no en FXML.
- `Dialogos`: helper de tema, aplicado a todos sus dialogos.
- `util`: validadores nuevos de codigo postal y email, al lado de `DocumentoFiscalValidator`.
- `base.css`: completar `.dialog-card`.

## Punto de partida

El formulario **no es un FXML**: se construye a mano en `ClientesController.dialogoCliente(...)`, un `Dialog<Cliente>` con un `GridPane` sin `styleClass`, sin tamano fijado y sin hoja de estilos.

**El patron de validacion ya esta inventado en ese mismo metodo**, para el NIF, y funciona asi:

1. Un `BooleanSupplier` que consulta al validador.
2. Un `Runnable` que pinta el borde rojo, avisa con `Dialogos.error` y devuelve el foco, protegido con un `boolean[]` centinela para no reentrar.
3. `setOnAction` y un listener de `focusedProperty` para validar al salir del campo.
4. Un `addEventFilter(ActionEvent.ACTION, ...)` sobre el boton Guardar que consume el evento si el valor no es valido.

**Las validaciones de CP y email deben calcarlo**, no inventar otro mecanismo. El resultado tiene que sentirse igual en los tres campos.

## Reglas de validacion

- **Codigo postal**: exactamente cinco digitos; las dos primeras cifras, entre `01` y `52`, que son las provincias espanolas. **Obligatorio: en blanco no vale.**
- **Email**: opcional. En blanco se acepta. Con contenido, un patron razonable del tipo `algo@algo.algo`, sin pretender cubrir el RFC entero, que en la practica solo genera falsos negativos.
- **NIF**: se queda como esta. `DocumentoFiscalValidator.esValido("")` devuelve `true`, o sea que el NIF en blanco se sigue aceptando.

**Efecto que hay que asumir a conciencia:** al hacer obligatorio el codigo postal, editar un cliente antiguo que no lo tenga obligara a rellenarlo antes de poder guardar. Es deliberado, pero conviene saberlo antes de que sorprenda.

## Tema en los dialogos

`ThemeManager.seleccionar(Scene, tema)` y `ThemeManager.aplicar(Scene, Servicios)` trabajan sobre `Scene`. Un `DialogPane` es un `Pane`, asi que tiene `getStylesheets()` propio: el helper puede anadir ahi la hoja del tema activo y la clase de tarjeta, sin depender de que el dialogo ya este mostrado.

`.dialog-card` hoy solo declara `-fx-background-radius`, `-fx-border-radius`, `-fx-border-width` y `-fx-padding`. Le faltan los colores; anadirlos derivados del tema como hace `.card`, con `-fx-background-color: -fx-base` y borde `derive(-fx-base, -10%)`. Ojo con los siete temas: hay que mirarlo al menos en uno claro y en uno oscuro, `negro-dorado` por ejemplo.

## Tamano del formulario

No basta con ensanchar el dialogo: **sin `ColumnConstraints` con `hgrow`, los campos no se estiran** aunque haya sitio. Hay que darle a la columna de campos `hgrow="ALWAYS"` y `fillWidth`, poner `maxWidth` infinito en los controles, y que **Direccion ocupe las dos columnas**.

## Futuro, no ahora

Queda anotado para mas adelante: **buscador de codigos postales**. Es el dato que mas lata da al cumplimentar facturas, porque el cliente muchas veces no lo trae. La idea es poder buscar por localidad y que rellene el CP, o al reves. No entra en este change.

## Testing

- Tests unitarios de los dos validadores nuevos, al estilo de `DocumentoFiscalValidatorTest`: codigos postales validos e invalidos, incluyendo el limite de provincia (`00xxx` y `53xxx` no valen, `01xxx` y `52xxx` si), la cadena vacia como **invalida**, y emails con y sin arroba, con y sin dominio, y la cadena vacia como **valida**.
- **Comprobar que los tests nuevos fallan antes de escribir los validadores**, que para eso se escriben primero.
- Verificacion manual: dar de alta con NIF, CP y email invalidos y comprobar que no deja guardar; dejar el email en blanco y comprobar que si deja; escribir una direccion larga y ver que se lee entera; y abrir el dialogo con un tema claro y con uno oscuro.
