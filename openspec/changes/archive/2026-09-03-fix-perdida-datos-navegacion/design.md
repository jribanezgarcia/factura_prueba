## Context

`Vista.puedeCerrar()` (`Vista.java:21`) es un `default` que devuelve `true`; solo `EditorController:326` lo implementa de verdad, abriendo el diálogo de cambios sin guardar. `Navegador.mostrar()` (`Navegador.java:38-63`) nunca lo llama. Ver proposal.md - Why.

`Navegador` no guarda la vista actual: la publica con `setOnVistaCambio`, y quien la retiene es `Main` (`Main.java:37, 119`), que la usa en `cerrarAplicacion()` (`:144`).

## Goals / Non-Goals

**Goals:**

- Que ningún camino de navegación pueda descartar cambios sin guardar en silencio.
- Que la guarda esté en un único sitio, para que un camino nuevo quede cubierto sin acordarse de nada.
- Que el usuario no vea el diálogo dos veces por el mismo gesto.

**Non-Goals:**

- No se añade seguimiento de cambios a `ConfiguracionController` (ver proposal.md - Fuera de alcance).
- No se toca `Main.cerrarAplicacion()` ni el flujo de cierre de ventana.
- No se cambia la semántica de `puedeCerrar()` ni el diálogo de cambios sin guardar.

## Decisions

### D1. La guarda va en `Navegador`, no en cada llamante

Repetir `if (puedeCerrar())` en los catorce sitios que llaman a `mostrar(...)` es lo que ha producido el agujero actual: tres lo hacen y once no. `Navegador.mostrar()` es el embudo por el que pasan todos, incluidos los caminos futuros.

`Navegador` gana un campo `private Vista vistaActual`, que se asigna al final de `mostrar()` (donde ya se invoca `onVistaCambio`). Al principio de `mostrar()`:

```java
if (vistaActual != null && !vistaActual.puedeCerrar()) {
    return null;
}
```

`Main` sigue con su propio `actual` vía `setOnVistaCambio`; no hace falta tocarlo.

### D2. Quitar las guardas que quedan duplicadas

Este es el punto que hay que hacer bien: `EditorController.volver()` (`:1303`), `nuevaFactura()` (`:1297`) y `verVersiones()` (`:1150`) ya preguntan por su cuenta antes de llamar a `mostrar(...)`. Si se deja la guarda nueva **y** las suyas, `Dialogos.confirmarCambiosSinGuardar()` sale **dos veces** por un solo clic, y peor: si el usuario elige «Guardar y salir», `guardar()` se ejecutaría en la primera y volvería a preguntar en la segunda.

Los tres métodos pasan a llamar directamente a `mostrar(...)` y, donde usen el resultado, a comprobar `null`.

### D3. `mostrar()` devuelve `null` al cancelar

La firma es `<T extends Vista> T mostrar(String fxml)`. Devolver `null` es la señal natural de «no se navegó». Hay exactamente tres llamantes que usan el retorno y deben comprobarlo:

| Sitio | Uso |
|---|---|
| `EditorController:1173` | `VersionesController vc = nav.mostrar(...)` |
| `HistoricoController:187` | `EditorController editor = nav.mostrar(...)` |
| `VersionesController:98` | `EditorController editor = nav.mostrar(...)` |

Los otros once ignoran el retorno y no necesitan cambios. Documentar el `null` en el Javadoc de `mostrar()`.

En los dos casos de `abrirVersion` la vista de origen (Histórico, Versiones) usa el `puedeCerrar()` por defecto, que devuelve `true`, así que hoy no pueden recibir `null`; la comprobación es para que sigan siendo correctos si esas vistas llegan a tener estado sin guardar.

### D4. Efecto sobre los tests de UI

Los tests que navegan varias veces sobre el mismo `Navegador` (`VentanaTransicionTest`, `UiSmokeTest`) usan vistas con el `puedeCerrar()` por defecto y no se ven afectados.

El riesgo está en los que dejan el editor con cambios y luego navegan: al pasar por la guarda abrirían `Dialogos.confirmarCambiosSinGuardar()`, que es un modal bloqueante en el hilo de JavaFX y colgaría la suite. `Dialogos.setImpl(...)` (`Dialogos.java:139`) existe justo para esto y ya se usa en `EditorNifValidationTest`. Revisar `EditorFlujoTecladoTest` y `EditorTamanoMinimoTest`, que escriben en el editor.

## Verificación

- Test nuevo con una `Vista` de prueba que devuelva `false`: `mostrar()` devuelve `null` y la escena del `Stage` no cambia.
- Test con la misma vista devolviendo `true`: navega con normalidad.
- A mano, el caso que motiva el change: rellenar media factura y pulsar cada uno de los seis iconos de la barra; debe preguntar siempre, una sola vez.
- A mano, comprobar que «Volver» y «Nueva factura» del editor siguen preguntando **una** vez, no dos.
