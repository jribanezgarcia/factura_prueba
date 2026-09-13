## Context

Ninguna hoja fija `-fx-prompt-text-fill`: el color del texto de ayuda cae al valor de la plataforma, pensado sobre fondo claro. Los campos usan `-fx-control-inner-background` de fondo: blanco en los cuatro temas claros y casi negro en los tres oscuros (negro-dorado `#121214`, neón `#151A3A`, omarchy `#131A3A`). El texto normal sí va en `-fx-text-background-color`, claro en los oscuros. Ver `proposal.md - Why`.

## Goals / Non-Goals

**Goals:** prompt legible en los siete temas sin tocar textos ni comportamiento.

**Non-Goals:** cambiar el color del texto ya escrito; tocar FXML o Java.

## Decisions

### D1. Una sola regla en `base.css` con `derive`

Fijar el prompt a partir del color de texto que ya declara cada tema, oscurecido para que se lea como ayuda y no como valor:

```css
.text-field, .text-area, .combo-box {
    -fx-prompt-text-fill: derive(-fx-text-background-color, -40%);
}
```

En los temas claros oscurece un texto ya oscuro (sigue contrastando sobre blanco); en los oscuros convierte el texto claro en un gris medio, legible sobre el fondo casi negro. Un solo sitio, sin valores literales por tema.

Alternativa descartada: un literal por tema. Más control, pero siete valores que mantener para algo que `derive` ya resuelve; solo se usará si el contraste medido no llega.

## Risks / Trade-offs

- **Contraste real.** El valor `-40%` es un punto de partida: hay que medirlo a ojo en un tema claro y en uno oscuro, y moverlo si no llega. Lo cubre la verificación.
- **`derive` sobre fondos blancos.** Oscurecer `#1F2937` un 40% lo acerca al negro; sobre blanco sigue legible, pero se comprueba igual.
