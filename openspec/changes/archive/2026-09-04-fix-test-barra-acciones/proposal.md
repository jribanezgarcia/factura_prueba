## Why

El change `fix-barra-acciones-editor` resolvió el fallo, pero al revisarlo aparecen cuatro cosas que conviene rematar. La primera es seria.

### 1. `EditorBarraAccionesTest` no puede fallar

Sus dos métodos de test pasan incondicionalmente. Las dos aserciones están muertas:

**La del chevrón** (`EditorBarraAccionesTest:99` y `:189`):

```java
Node overflow = root.lookup(".tool-bar-overflow-button");
assertFalse(overflow != null && overflow.isVisible());
```

Al sustituirse el `ToolBar` por un `HBox` ese nodo ya no existe nunca, así que `overflow` es siempre `null`, la expresión completa es siempre `false`, y la línea equivale a `assertFalse(false)`.

**La de los bounds** (`:109` y `:201`):

```java
assertFalse(bounds.isEmpty(), "El boton ... no debe tener bounds vacios");
```

`Bounds.isEmpty()` solo devuelve `true` cuando el ancho o el alto son **negativos**. Un botón maquetado nunca lo está: ni empujado fuera del área visible, donde conserva su ancho, ni aplastado a cero.

Y la comprobación que sí valía se quedó a medias: `double anchoEscena = stage.getScene().getWidth()` se calcula en la línea 97 y **no se usa en ninguna parte del fichero**. Era la contención que pedía la tarea 1.2 del change anterior.

Esto pesa más de lo que parece porque el cambio a `HBox` quitó el paracaídas: el `ToolBar` al menos sacaba un chevrón, y un `HBox` comprime los botones en silencio. El test es ahora el único guardián, y no guarda nada.

### 2. El título se recorta siempre, no solo cuando hace falta

`lblTitulo` quedó con `maxWidth="130.0"`. El motivo documentado es correcto: con la factura anulada el chip ANULADA ocupa unos 80 px entre el título y el separador, y con 200 px de título el conjunto no cabía.

Pero `maxWidth` es un tope **estático** elegido para el peor caso, y ese caso es minoritario. En una factura normal no hay chip y quedan unos 210 px libres. «Factura C-59/7 (v1)» a 17 px mide unos 137 px, así que **toda factura guardada muestra el título con elipsis**, perdiendo justo el final, que es donde va la versión.

### 3. El ancho fijo de los botones de navegación va demasiado justo

`.nav-button` recibió `-fx-min-width`, `-fx-pref-width` y `-fx-max-width` a 80 px. Igualar los anchos es buena idea —alinea los iconos y evita una fila irregular— pero el valor aprieta: «Configuración» a 10 px mide unos 61 px contra los 64 px de caja de contenido (80 menos 16 de relleno). Quedan ~3 px, y con Windows al 125 % de escala se recorta. Al ser `-fx-max-width` un tope duro, el botón tampoco puede crecer.

### 4. Palabra clave normativa mal escrita

El requisito «Barra de acciones del editor sin desbordamiento» dice **`SHALL NO`** donde debe decir `SHALL NOT`.

## What Changes

- Las aserciones de `EditorBarraAccionesTest` pasan a comprobar lo que de verdad importa con un `HBox`: que ningún botón se sale del ancho de la escena y que **ninguno queda comprimido por debajo de su ancho preferido**.
- El tope de anchura del título pasa a depender del estado: estrecho solo cuando el chip ANULADA está visible.
- El ancho de los botones de navegación deja de ser un tope duro insuficiente.
- Se corrige `SHALL NO` por `SHALL NOT`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se corrige la redacción del requisito «Barra de acciones del editor sin desbordamiento» y se precisa que el título solo se recorta cuando el espacio lo exige.

## Impact

- `ui/EditorBarraAccionesTest`: las cuatro aserciones muertas de sus dos métodos.
- `ui/EditorController`: una línea en `actualizarBotonesEstado()`.
- `themes/base.css`: el ancho de `.nav-button`.
- No se toca lógica de negocio, ni el cálculo, ni la persistencia, ni el PDF.

### Fuera de alcance

Hay otro `SHALL NO` preexistente en el requisito «Numeración por series», sin relación con este trabajo. Se deja anotado para corregirlo aparte y no arrastrar aquí un requisito largo que nada tiene que ver.
