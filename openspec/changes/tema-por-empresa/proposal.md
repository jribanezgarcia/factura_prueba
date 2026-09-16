## Why

Al probar la aplicación a mano (15/09/2026), al cambiar el tema en una empresa el tema cambia también en todas las demás. Hoy es así a propósito: el tema se guarda en `preferencias.properties`, compartido entre empresas, mientras que el color del PDF ya se guarda en cada empresa. El usuario quiere que cada empresa tenga su propio tema, como el color del PDF.

## What Changes

- **Tema por empresa**: al guardar en Configuración, el tema se guarda en la tabla `preferencias` de la empresa activa (clave `tema`), igual que `color_pdf`.
- **Al entrar o cambiar de empresa** se aplica el tema de esa empresa. Si nunca se guardó ninguno, se aplica biblioteca8.
- **Pantalla de arranque**: como se muestra antes de abrir ninguna base de datos, usa el tema de la última empresa abierta. Para eso, cada vez que conectamos con una empresa copiamos su tema a `preferencias.properties` (clave `tema`, que ya existe). Así `GestorTemas` sigue leyendo el tema de un solo sitio.
- **Restaurar una copia sobre la empresa activa** trae el tema guardado en la copia.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Configuración» pasa de tema global a tema por empresa y añade tres escenarios; el requisito «Configuración organizada por secciones» deja de decir que el tema es una preferencia compartida.

## Impact

- Código: `modelo/negocio/Empresas`, `modelo/negocio/PreferenciasGlobales` (solo su Javadoc), `fichero/CopiaSeguridad`, `vista/utilidades/GestorTemas`.
- Tests: `EmpresasTest`, `CopiaSeguridadTest`.
- Sin cambios en la base de datos: la tabla `preferencias` ya existe en cada empresa.
- No hay migración: el programa está en desarrollo y los datos se van a reiniciar. Las empresas existentes empiezan con biblioteca8 hasta que se guarde un tema en ellas.
- Fuera: el aviso al modificar el cliente desde una factura (change aparte) y las construcciones que prohíbe `AGENTS.md` fuera de las líneas tocadas.
