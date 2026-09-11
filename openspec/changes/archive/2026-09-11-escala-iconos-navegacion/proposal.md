## Why

Los iconos de la barra de navegación ya van dentro de una caja fija de 26x26, pero dos de ellos se corrigieron a mano desplazándolos en horizontal: Histórico 1,3 píxeles a la izquierda y Salir 2,8 a la derecha. Es un parche: mueve el dibujo fuera del centro de su caja para compensar a ojo que unos iconos parecen más grandes o más descentrados que otros.

Además, todos comparten la misma escala, 1.25, aunque sus dibujos ocupen trozos muy distintos de la rejilla: el de Clientes se ve claramente más pequeño que los demás y el de Histórico más grande.

## What Changes

- Desaparecen los desplazamientos manuales: cada icono SHALL quedar centrado en su caja.
- Cada icono SHALL poder llevar su propia escala proporcional, de modo que todos se vean de un tamaño parecido: 1.25 en general, 1.15 en Histórico y 1.4 en Clientes.
- La escala pasa de la hoja de estilos al código de la barra, porque una regla CSS pisaría la escala propia de cada icono.
- La barra de navegación entra en el requisito «Iconos de tamaño uniforme dentro de una barra», con su caja de 26x26.
- No cambia ningún trazado, texto, tooltip ni destino.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Iconos de tamaño uniforme dentro de una barra» deja de excluir la barra de navegación y admite, solo en ella, una escala proporcional por icono en lugar de desplazamientos manuales.

## Impact

- `src/main/java/com/alcazaba/facturacion/ui/BarraNavegacion.java`: el parámetro `offsetX` pasa a ser `escala`; desaparece `setTranslateX`.
- `src/main/resources/com/alcazaba/facturacion/themes/base.css`: se retira la escala de `.nav-button .nav-icon`.
- Ningún FXML ni ningún tema.
