## Why

El botón «Facturar mes» del Histórico muestra su etiqueta recortada: se lee `Facturar ...`.

Medido a 1024×768 con el tema por defecto: la etiqueta son dos palabras, así que necesita dos líneas —70px de alto—, pero la fila solo le da 55, que es el alto que marcan los otros cinco botones de la barra. El corte es vertical, no horizontal.

Las alternativas se han medido a 12px, sobre los 68px útiles de un botón de 72:

| Etiqueta | Ancho del texto | ¿Cabe en una línea? |
|---|---|---|
| `Facturar mes` | 67,8 | No: se parte y se recorta |
| `Gen. mensual` | 71,5 | No |
| `Gen.mensual` | 68,2 | No |
| `Mensuales` | 56,1 | Sí |
| `Mensual` | 44,7 | Sí, con holgura |

## What Changes

- El botón de generación mensual del Histórico SHALL llamarse «Mensual», que cabe en una línea sin recortarse.
- Ese botón SHALL mostrar el tooltip «Generar facturas mensuales», de modo que el nombre completo de la acción siga estando a la vista al posar el puntero.
- La opción del menú principal SHALL seguir llamándose «Facturar mes», donde el espacio no aprieta.
- Se adopta así, para las barras de iconos, el criterio que ya sigue la barra de navegación: etiqueta visible breve y nombre completo en el tooltip.
- No cambia ninguna acción, ningún icono ni ninguna medida.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se ajustan tres requisitos que daban por hecho que la etiqueta del botón y el nombre de la opción del menú son literalmente el mismo texto: «Criterio de etiquetado de botones», «Menú y navegación» y «Barra de acciones del editor sin desbordamiento», cuyo escenario exigía ver «Facturar mes» en dos líneas.

## Impact

- `src/main/resources/com/alcazaba/facturacion/ui/Historico.fxml`: el texto del botón y su `Tooltip`.
- Ningún `.java`, ningún CSS, ningún tema, ningún otro FXML.
