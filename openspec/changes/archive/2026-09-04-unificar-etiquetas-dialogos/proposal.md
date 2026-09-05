## Why

El change `unificar-etiquetas-botones` fijó el criterio «la acción destructiva se llama Eliminar, nunca Borrar» y lo aplicó a los botones. Pero se quedó ahí: **los diálogos que abren esos mismos botones siguen diciendo «Borrar»**.

El recorrido más visible es también el más delicado. En el Histórico el usuario pulsa el botón **Eliminar** y lo que aparece es:

```
Título:  Borrar
Cuerpo:  Se van a borrar físicamente N factura(s).
```

Sitios afectados:

| Sitio | Texto |
|---|---|
| `HistoricoController:234, 250, 253, 277` | `Dialogos.info("Borrar", ...)` y `confirmar("Borrar", "Se van a borrar físicamente...")` |
| `HistoricoController:203, 210, 223, 226` | `"Borrar/Anular"` como título del flujo de anulación |
| `ConfiguracionController:837, 847, 854` | `"Borrar serie"`, «no puede borrarse», «No se pudo borrar la serie» |
| `ConfiguracionController:938` | «Se borrará físicamente su carpeta de datos», dentro del diálogo «Eliminar empresa» |

Mientras tanto `ClientesController` dice «Eliminar» en el botón, en el título del diálogo y en el cuerpo. El resultado es que la aplicación quedó a medias: **la incoherencia se ha movido del botón al diálogo**, y precisamente en la acción irreversible, que es donde el usuario más necesita entender qué va a pasar.

El requisito «Criterio de etiquetado de botones» no lo impide porque habla solo de etiquetas de botón. Esa es la raíz: el criterio se redactó demasiado estrecho.

## What Changes

- El criterio de un verbo por concepto SHALL alcanzar también a los **títulos y cuerpos de los diálogos** que abren esos botones, no solo a las etiquetas.
- Se sustituye «Borrar» por «Eliminar» en los diálogos del Histórico y de Configuración, incluidos los cuerpos de mensaje.
- El título `"Borrar/Anular"` se separa según lo que hace realmente cada flujo.
- No cambia ninguna acción ni ningún manejador: es texto de interfaz.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía «Criterio de etiquetado de botones» para que cubra el texto de los diálogos asociados a cada acción.

## Impact

- `ui/HistoricoController`: títulos y cuerpos de los diálogos de eliminación y de anulación.
- `ui/ConfiguracionController`: diálogos de eliminación de serie y de empresa.
- No se toca lógica, ni servicios, ni persistencia, ni PDF.

### Nota sobre los identificadores del código

`btnBorrar`, `borrarSeleccionadas()` e `itemBorrar` son nombres internos, no texto visible, y **no** entran en este change. Renombrarlos sería ruido en el diff sin efecto para el usuario; si algún día se hace, que sea en un cambio propio de refactor.
