## Why

En los temas oscuros, el texto de ayuda de los campos de búsqueda (Clientes, Histórico) no se lee: ninguna hoja fija `-fx-prompt-text-fill` y el color cae al valor de la plataforma, que sobre esos fondos no contrasta.

## What Changes

- El texto de ayuda (prompt) de los campos SHALL leerse con claridad en los siete temas, incluidos los oscuros.
- No cambia ningún texto de ayuda, ningún comportamiento ni el color del texto ya escrito: solo el color del prompt.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito de temas y apariencia pasa a exigir prompt legible en cada tema.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/`: una o dos reglas por tema (o una regla común en `base.css` si un solo valor vale para los siete).
- Ningún FXML, ningún `.java`, ningún test nuevo salvo que haga falta fijar el contraste.
