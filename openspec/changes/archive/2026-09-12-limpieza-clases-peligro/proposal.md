## Why

Desde que ningún botón de la aplicación se muestra en color de peligro, las clases `danger-button` y `action-danger-button` no las usa ningún FXML ni ningún controlador. Sus reglas siguen ocupando sitio en `base.css` y en los siete temas: un rojo por tema, sus estados y el relleno de sus iconos. Cualquiera que lea el CSS creerá que hay botones rojos en alguna pantalla.

## What Changes

- Se retiran de `base.css` y de los siete temas todas las reglas de `danger-button` y `action-danger-button`, incluidas las que solo las nombran dentro de un selector agrupado.
- Ninguna pantalla cambia de aspecto: ya no había ningún botón con esas clases.
- `BotonesTest` deja de excluir esas dos clases, que ya no puede encontrar.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

No se modifica ningún requisito: `skip_specs: true`. El requisito «Sombreado uniforme de los botones de solo texto» ya dice que ningún botón de acciones destructivas se muestra en color de peligro, y ningún requisito nombra clases de estilo.

## Impact

- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: nueve selectores agrupados pierden sus líneas de peligro.
- Los siete `tema-*.css`: cuatro reglas cada uno.
- `src/test/java/com/alcazaba/facturacion/ui/BotonesTest.java`: dos condiciones que sobran.
- Ningún FXML, ningún `.java` de producción.
