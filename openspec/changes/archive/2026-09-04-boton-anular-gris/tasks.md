> Los valores exactos están en `design.md`, sección «D1». Usarlos tal cual.

## 1. Estilo del botón de peligro en Biblioteca8

- [x] 1.1 En `tema-biblioteca8.css`, cambiar `.action-danger-button` a fondo `#FFFFFF`, texto `#C0392B`, borde `#E9B7AE`, y su `:hover` a fondo `#F9E7E4` manteniendo el texto; verificar que el fichero sigue cargando (abrir la app o ejecutar `UiSmokeTest`).
- [x] 1.2 Confirmar que ningún otro botón usa `action-danger-button` (hoy solo `btnAnular` en `Editor.fxml:29`).

## 2. Especificación

- [x] 2.1 MODIFIED «Estilo de zona de acciones en tema por defecto»: Anular SHALL mantener estilo de peligro con fondo blanco y texto rojo, que SHALL NOT confundirse con un deshabilitado. Escenario nuevo de Anular distinguible.

## 3. Verificación final

- [x] 3.1 Suite completa en verde con `mvn test`.
- [x] 3.2 A mano con Biblioteca8: factura emitida (Anular visible) frente a Versiones/Rectificativa deshabilitados en factura nueva; comprobar que el Anular se distingue a simple vista.
