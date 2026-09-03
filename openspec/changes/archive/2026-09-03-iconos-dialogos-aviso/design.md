## Context

Los diálogos de aviso los construye `src/main/java/com/alcazaba/facturacion/ui/Dialogos.java` con `Alert` de JavaFX. Todos se visten en `aplicarTema(DialogPane)` con `pane.getStylesheets().setAll(ThemeManager.hojas())`. Ese `setAll` **sustituye** la hoja Modena por defecto de JavaFX, que es la que provee los gráficos de cada `AlertType` (información/error/confirmación); por eso los diálogos salen sin icono. Ver `proposal.md · Why`.

La motivación, alcance y requisitos están en `proposal.md` y `specs/invoicing/spec.md`.

El proyecto ya usa glifos SVG embebidos en Java vía `SVGPath` (patrón de `BarraNavegacion.icono(String)` → `SVGPath` con `styleClass`, coloreado por CSS), y los temas definen el acento con la variable CSS `-fx-accent` (usada p. ej. en `.dialog-card > .header-panel .label`).

## Goals / Non-Goals

**Goals:**
- Que cada diálogo de aviso muestre el icono de su tipo (información, error, confirmación).
- Que el icono se dibuje con el color de acento del tema activo y se adapte automáticamente a los 7 temas.
- Integrarlo en `Dialogos` de forma centralizada (un solo punto que tocan todos los diálogos).

**Non-Goals:**
- No cambiar títulos, mensajes, botones ni el flujo de confirmación/cancelación de los diálogos.
- No modificar el estilo de tarjeta (`dialog-card`) ni ningún `tema-*.css`.
- No reintroducir Modena (cambiar el mecanismo de `setAll` de las hojas) — fuera de alcance y arriesgado para el resto de estilos.

## Decisions

**D1 — Icono explícito por tipo en `Dialogos` (no reintroducir Modena).**
En cada constructor de `Alert` (`error`, `info`, `confirmar`, `confirmarCambiosSinGuardar`, `modoGuardarVersion`) se asignará `a.setGraphic(icono(tipo))` con un `SVGPath` correspondiente al tipo:
- `INFORMATION` → glifo de información (círculo con «i»).
- `ERROR` → glifo de alerta (triángulo con «!»).
- `CONFIRMATION` → glifo de pregunta (cuadro con «?»).

*Alternativa descartada*: mantener Modena añadiendo las hojas con `addAll` en vez de `setAll`. Restauraría los iconos por defecto pero es un cambio global de los estilos de los diálogos (y de todas las escenas que usan `hojas()`) con riesgo de regresiones visuales imprevisibles en los 7 temas. Poner un glifo propio da control determinista del color y la forma.

**D2 — Color mediante CSS con `-fx-accent` (una clase, sin tocar los temas).**
El `SVGPath` llevará `styleClass "dialog-icon"`. En `base.css` se añade la regla:

```css
.dialog-icon { -fx-fill: -fx-accent; }
```

Como cada `tema-*.css` define `-fx-accent`, el icono se coloreará automáticamente en todos los temas sin tocar ninguno. El tamaño y la forma se fijan en el propio `SVGPath` (propiedades Java) para no depender de CSS.

*Alternativa descartada*: fijar el color en Java leyendo el acento del tema activo. `ThemeManager` no expone el color de acento y derivarlo en código sería frágil; la variable CSS `-fx-accent` ya es la fuente de verdad del tema.

**D3 — Helper central `icono(AlertType)` dentro de `Dialogos`.**
Un método privado estático que devuelve el `SVGPath` adecuado, reutilizado por todos los diálogos. Mantiene el cambio localizado en la clase que ya centraliza los diálogos.

**D4 — La ventana del `Alert` también lleva el icono de aplicación de la marca.**
Además del glifo interno, cada `Alert` es una `Stage` propia que hasta ahora queda sin el icono de la app (el change de icono solo cubría las ventanas navegadas y «Generar facturas mensuales»). Antes de cada `showAndWait()` se obtiene la ventana del diálogo y se le aplica el icono reutilizando `Ventanas.aplicarIcono(Stage)`, idempotente y silenciosa si el recurso falta:

```java
Window w = a.getDialogPane().getScene().getWindow(); // Stage del Alert
if (w instanceof Stage s) Ventanas.aplicarIcono(s);
```

*Alternativa descartada*: crear un icono nuevo para los diálogos. No hace falta — se reutiliza el mismo recurso `images/icono-aplicacion.png` vía `Ventanas`, garantizando coherencia con el resto de ventanas.

## Risks / Trade-offs

- [El glifo del diálogo y el encabezado del `dialog-card` comparten el color de acento y podrían confundirse visualmente] → Mitigación: se dimensiona el `SVGPath` de forma contenida y se mantiene el `headerText`/tarjeta actual; el icono es el elemento gráfico separado en la zona de `graphic`, no una etiqueta.
- [Un glifo SVG mal trazado se ve deforme en vez de faltar] → Mitigación: se usan trazos simples y bien proporcionados; verificación visual durante la implementación.
- [Los tests existentes de diálogos (que sustituyen la implementación con `setImpl`) no ejercitan el glifo] → Mitigación: la suite sigue en verde sin cambios de test; la verificación del icono es visual.

## Migration Plan

N/A: es una mejora visual aditiva sin migración de datos ni de esquema. Rollback: revertir el commit de `Dialogos.java` + la regla `.dialog-icon` de `base.css`.
