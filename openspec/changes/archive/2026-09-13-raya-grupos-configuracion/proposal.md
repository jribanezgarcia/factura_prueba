## Why

En la lista lateral de Configuración, los dos rótulos de grupo —CONFIGURACIÓN GENERAL y CATÁLOGOS— se distinguen de las secciones que encabezan solo por ser un punto más pequeños y algo más tenues. Al recorrer la lista apenas se aprecia dónde acaba un grupo y empieza el siguiente, y los rótulos quedan como una entrada más en vez de como la cabecera de un bloque.

## What Changes

- Los dos rótulos de grupo de la lista de secciones llevan una raya horizontal debajo, a todo el ancho de la lista.
- El rótulo gana dos píxeles de separación inferior para que el texto no quede pegado a la raya.
- Nada más cambia: mismos grupos, mismas secciones, mismo orden, mismos colores y misma navegación. Los rótulos siguen sin poder seleccionarse.
- Los siete paneles de sección no reciben ningún título nuevo: añadirlo consumiría alto y comprometería el escenario «Cada sección cabe sin scroll» a 1024×768.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

No se modifica ningún requisito: `skip_specs: true`. «Configuración organizada por secciones» describe qué secciones hay y desde dónde se navegan, no cómo se dibujan los rótulos de grupo. «Consonancia tipográfica de la interfaz» habla de las entradas seleccionables de la lista, que no cambian. «La negrita queda reservada a los importes» se respeta: la raya da jerarquía sin recurrir al peso de la letra.

## Impact

- `src/main/java/com/alcazaba/facturacion/ui/ConfiguracionController.java`: la rama de grupo de la fábrica de celdas de `configurarSecciones()`.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: la regla `grupo-secciones`.
- Ningún FXML, ningún `tema-*.css`, ninguna otra pantalla.
