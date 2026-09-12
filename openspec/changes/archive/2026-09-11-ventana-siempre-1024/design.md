## Context

En `Main.java` la ventana se coloca en dos momentos:

- `aplicarPreferenciasVentana(stage)` (línea 185), llamado en la línea 96 antes de mostrar el arranque. Lee `ventana_x`, `ventana_y`, `ventana_w` y `ventana_h`; si hay tamaño guardado lo aplica, si no pone 1024x768 y centra, y si hay posición guardada la aplica. Guarda además el ancho y el alto en los campos `anchoGuardado` y `altoGuardado`.
- `restaurarTamanoGuardado()` (línea 130), llamado en `entrarEnMenu` (línea 121) con la ventana oculta. Si el tamaño guardado supera el mínimo de la vista, lo aplica.

Al cerrar, `guardarPreferenciasVentana(stage)` (línea 209) escribe las cuatro preferencias.

Por su parte, `VentanaConfig.aplicar` ya fija el tamaño de cada vista y centra la ventana cuando esta está oculta (líneas 104-108). Pasa al mostrar el arranque (760x520) y al entrar en el menú, porque `entrarEnMenu` oculta la ventana antes de navegar (línea 117).

## Goals / Non-Goals

**Goals:** abrir siempre a 1024x768 y centrada; seguir guardando las preferencias.

**Non-Goals:** borrar las preferencias guardadas; cambiar el tamaño mínimo, la maximización ni el paso del arranque al menú.

## Decisions

### D1. Dejar que `VentanaConfig` coloque la ventana

No hace falta código nuevo: basta con dejar de pisar lo que ya hace `VentanaConfig`. Se retiran:

- La llamada `aplicarPreferenciasVentana(stage);` de la línea 96 y el método entero.
- La llamada `restaurarTamanoGuardado();` de la línea 121 y el método entero.
- Los campos `anchoGuardado` y `altoGuardado` (líneas 41-42), que solo usaban esos dos métodos.
- Las constantes `ANCHO_INICIAL` y `ALTO_INICIAL` (líneas 32-33), que solo usaba `aplicarPreferenciasVentana`. El 1024x768 de las vistas principales ya lo declara `VentanaConfig`.

Si al retirarlas queda algún `import` sin uso (por ejemplo `PreferenciasGlobales`), se comprueba: `guardarPreferenciasVentana` lo sigue usando, así que no debería quedar ninguno.

### D2. Se sigue guardando

`guardarPreferenciasVentana` y su llamada en `cerrarAplicacion` se quedan tal cual, por decisión del usuario del 10/09/2026. Si algún día se quiere volver a restaurar, los datos estarán.

## Risks / Trade-offs

- **Quien usaba la ventana grande** tendrá que maximizarla en cada sesión. Es lo decidido.
- **Monitores de menos de 1024x768.** No cambia nada respecto a hoy: el mínimo ya era 1024x768.
