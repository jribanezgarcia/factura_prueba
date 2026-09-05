## Why

En Configuración, sección Empresa, con la ventana en su tamaño predeterminado de 1024×768, los campos **Actividad** y **Localidad** salen cortados por el borde derecho, y **Nombre / razón social** también queda apretado. La ficha de empresa es lo primero que rellena el usuario y hoy no se puede leer entera sin ensanchar la ventana.

## What Changes

- Se reajustan las anchuras de los campos de la sección Empresa (`Configuracion.fxml`, líneas 29-48) para que todos quepan legibles a 1024×768.
- No cambia ningún dato, ninguna etiqueta, ningún comportamiento ni ninguna otra sección: es solo tamaño de campos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: se amplía «Configuración» para que los campos de la sección Empresa se vean completos en el tamaño mínimo de ventana (1024×768).

## Impact

- `ui/Configuracion.fxml`: solo los `prefWidth` de los `TextField` de la sección Empresa.
- No se toca lógica, ni controladores, ni servicios, ni persistencia, ni PDF.

### Por qué se cortan

El `GridPane` de la sección Empresa no tiene `ColumnConstraints`: cada columna mide lo que pide su hijo más ancho. La fila más ancha suma etiqueta «Nombre / razón social» (~140 px) + `txtNombre` (340) + etiqueta «Actividad» (~70) + `txtActividad` (240) + 3 separaciones de 8 = unos 814 px, contra los ~740 disponibles en la zona derecha (1024 menos lista lateral de 200, separación de 12 y rellenos). Sobran unos 70 px, que se comen la columna derecha.
