## Why

El usuario ha detectado que, tras generar y borrar muchas facturas, el Editor propone un número nuevo en lugar de reutilizar los huecos libres que quedan registrados en `numero_disponible`. Esto provoca que la numeración salte y deje agujeros permanentes, aunque esos números estén marcados como disponibles para reutilizar.

## What Changes

- Modificar `NumeroService.siguienteCorrelativo(...)` para que, antes de proponer el siguiente correlativo, consulte los huecos libres registrados en `numero_disponible` para la serie y el año.
- Si existe algún hueco libre, proponer el menor de ellos.
- Si no hay huecos, mantener el comportamiento actual (reutilizar anuladas si está activado, o continuar con el siguiente correlativo).
- Añadir tests que cubran la reutilización de números borrados.

## Capabilities

### New Capabilities

- Ninguno.

### Modified Capabilities

- `invoicing`: numeración de facturas reutiliza huecos de facturas borradas.

## Impact

- `NumeroService.java`
- `NumeroServiceTest.java`
- No cambia la estructura de base de datos ni la API pública del servicio.
