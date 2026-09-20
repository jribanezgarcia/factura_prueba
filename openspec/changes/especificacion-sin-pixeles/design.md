## Context

`openspec/specs/invoicing/spec.md`: 54 requisitos, 1.672 líneas. Contados el 20/09/2026, **19 requisitos (unas 700 líneas) solo describen apariencia** y salen de ~58 changes de retoque visual archivados. Un vigésimo, «Temas y apariencia», sí dice algo que importa y se reescribe dentro del requisito nuevo.

`openspec/specs/pdf-rendering/spec.md` (10 requisitos) describe el documento impreso: eso sí es comportamiento y no se toca.

Decisión del usuario (19/09/2026): «los ~20 requisitos visuales se resumen en uno, Apariencia». No cambia nada de lo que se ve.

## Goals / Non-Goals

**Goals:** que la especificación describa **qué hace** la aplicación; que los detalles de aspecto vivan en el CSS; que quede un único requisito con las reglas visuales que sí hay que cumplir.

**Non-Goals:**
- Cambiar el aspecto, el CSS o cualquier línea de código.
- Tocar `pdf-rendering`.
- Tocar los requisitos que describen **qué hay** en una pantalla: «Ventana», «Menú y navegación», «Identidad de empresa en la interfaz», «Identidad de la aplicación en la interfaz» y «Configuración organizada por secciones».

## Decisions

### D1. Los 20 requisitos que se retiran

Con el motivo por el que se va cada uno:

| Requisito | Por qué se retira |
|---|---|
| Barra de acciones del editor sin desbordamiento | Anchuras y compresión de botones; queda cubierto por «ninguna pantalla recorta contenido» |
| Distribución estable al redimensionar en Editor e Histórico | Anchuras de campos al maximizar |
| Iconos en los diálogos de aviso | Se resume en el requisito nuevo |
| Pantalla de generación mensual con estética alineada | «Que se vea como el resto»: eso es el CSS |
| Estilo de zona de acciones en tema por defecto | Colores exactos (`#F6F6F6`) de un tema |
| Sistema de diseño visual Apple | Espaciados, esquinas y tipografía |
| Microinteracciones visuales | Animaciones de hover y foco |
| Tamaños de ventana por vista | Se solapa con el requisito «Ventana», que se queda |
| Menú principal adaptado a 1024×768 | Márgenes del menú |
| Editor legible sin scroll en facturas cortas | Alturas de bloques del editor |
| Criterio de etiquetado de botones | Se resume en «una misma acción se llama siempre igual» |
| Botones de acción con icono identificativo | Fondos, bordes y silueta de los botones |
| Sombreado uniforme de los botones de solo texto | Grises y tinte del sombreado |
| Composición centrada del menú principal | Centrado del bloque de tarjetas |
| Marca en la pantalla de arranque | Colocación del rótulo y del icono |
| Consonancia tipográfica de la interfaz | Pesos y tamaños de letra |
| La negrita queda reservada a los importes | Se resume en el requisito nuevo |
| Iconos de tamaño uniforme dentro de una barra | Tamaño de la caja del icono |
| Barra de acciones sobre franja propia en Editor, Clientes e Histórico | Franja de fondo y colocación |
| Temas y apariencia | Sus reglas pasan enteras al requisito nuevo |

Son **20 requisitos**: 19 de aspecto y «Temas y apariencia», cuyo contenido se conserva entero en el requisito nuevo.

### D2. El requisito nuevo

Se añade **«Apariencia de la interfaz»**, con el texto y los escenarios que están en `specs/invoicing/spec.md` de este change. Recoge, en una sola pieza:

- los siete temas, el predeterminado, dónde se eligen y que se guardan por empresa;
- que la interfaz se lee con claridad en todos ellos, incluido el texto de ayuda de los campos;
- que ninguna pantalla recorta contenido en el tamaño mínimo de ventana;
- que una misma acción se llama igual en todas las pantallas y que los botones de una misma barra se presentan igual;
- los iconos de los diálogos de aviso;
- la negrita reservada a los importes;
- y que **el resto del aspecto vive en `base.css` y en el fichero de cada tema**, no en la especificación.

### D3. Qué NO se toca

- `pdf-rendering` entero.
- «Ventana» (1024×768, arranque 760×520, mínimos y reorganización de filas).
- «Menú y navegación», «Identidad de empresa en la interfaz», «Identidad de la aplicación en la interfaz» y «Configuración organizada por secciones».
- Ni una línea de `src/` ni de los CSS.

## Risks / Trade-offs

- **Se pierde el detalle escrito de algunos acabados** (el gris `#F6F6F6` del tema por defecto, el tinte del sombreado). Sigue estando **en el CSS**, que es donde se aplica, y en los changes archivados si alguna vez hace falta saber por qué se hizo así.
- **El archivado retira esos requisitos del spec vivo**: si alguno se considera imprescindible, hay que decirlo antes de archivar.
