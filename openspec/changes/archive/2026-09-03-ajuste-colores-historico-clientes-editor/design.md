## Context

- Ver proposal.md — Why. El tema por defecto es **Biblioteca8** (`ThemeManager.DEFAULT = "biblioteca8"`, `tema-biblioteca8.css`); `base.css` se aplica siempre y luego el CSS del tema activo.
- En `tema-biblioteca8.css`:
  - `.action-button` es gris (`#B0B4B8` de fondo, `#F3F5F6` de texto) y se usa **solo en el Editor** (barra superior y fila de líneas).
  - `.card` (base.css) tiene `-fx-background-color: -fx-base` = `#FFFFFF` en este tema.
- El usuario decidió (vía prototipo y preguntas): gris `#F0F0F0` para las tarjetas de Histórico y Clientes; botones del Editor de acción blancos con texto negro; aplicar **solo en Biblioteca8**.

## Goals / Non-Goals

**Goals:**
- Botones de acción del Editor en blanco y negro (legibles) solo en Biblioteca8.
- Fondo gris claro `#F0F0F0` en la tarjeta superior de Histórico y Clientes solo en Biblioteca8.
- No alterar Guardar (primario) ni Anular (peligro).

**Non-Goals:**
- No tocar el resto de temas.
- No cambiar lógica, controladores, FXML de contenido ni repositorios.
- No cambiar el fondo de las tablas ni de otras tarjetas (solo la tarjeta superior de acciones).

## Decisions

- **D1 — Ámbito solo en Biblioteca8.** Los cambios van únicamente en `tema-biblioteca8.css`. Alternativa descartada: tocar `base.css` (se aplicaría a todos los temas y rompería los oscuros).

- **D2 — Botones del Editor: cambiar la clase `.action-button`.** En `tema-biblioteca8.css` se reemplaza el estilo gris de `.action-button` (y su `:hover`) por fondo blanco `#FFFFFF`, texto `#1F2937` y borde `#D8DBDF`. Como `action-button` solo se usa en el Editor (barra superior + fila de líneas), este único cambio cubre Exportar PDF, Versiones, Crear rectificativa, Restaurar, Nueva factura, Volver, Añadir línea y Eliminar línea de forma uniforme. `primary-button` (Guardar) y `action-danger-button` (Anular) quedan intactos. Alternativa descartada: añadir una clase nueva por botón (más difusa y con más código), innecesaria porque toda la clase comparte el mismo aspecto deseado.

- **D3 — Tarjeta gris de Histórico y Clientes con clase propia.** Las tarjetas superiores son `styleClass="card, zona-contenido"`, combinación compartida con Backup, Configuración y Versiones; y la tabla central usa `card` solo. Por tanto no se puede estilizar por `.card` ni `.card.zona-contenido` sin colorear zonas no deseadas. Se añade una clase propia (`panel-busqueda`) a la tarjeta superior de `Historico.fxml` y `Clientes.fxml`, y en `tema-biblioteca8.css` se define `.panel-busqueda { -fx-background-color: #F6F6F6; }`. La tabla central (`.card` sola) y el resto de pantallas conservan su fondo blanco.

## Risks / Trade-offs

- **Cambiar toda la clase `.action-button` también afecta a Añadir línea y Eliminar línea** (botones inferiores del Editor). No se pidieron explícitamente, pero comparten estilo y quedan coherentes con el resto; no se considera un riesgo visual. → Mitigación: documentado en proposal/spec.
- **Un fondo gris fijo `#F6F6F6` muy próximo al de la página (`#E8EAED`)** podría distinguirse poco. → Mitigación: es el tono elegido por el usuario tras el prototipo (quería algo discreto y poco llamativo).
- **Futuro cambio del aspecto de `action-button` global::** al ser clase compartida, cualquier ajuste futuro afecta a todos los botones de acción del Editor; se considera deseable por uniformidad.
