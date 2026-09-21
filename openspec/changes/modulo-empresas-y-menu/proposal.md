## Why

Las empresas se gestionan hoy desde dos sitios y con un bloqueo que complica el programa:

- **Crear empresas se puede en el arranque y en Configuración**, y cambiar y eliminar solo en Configuración, en una sección «Empresas» con su tabla, sus botones y un ofrecimiento de «¿cambiar a ella?». Dos caminos para lo mismo.
- **Una empresa con datos incompletos bloquea toda la aplicación**: se entra a la fuerza en Configuración, la barra de navegación se desactiva salvo Salir y no se puede ni mirar el menú hasta rellenarlo todo.
- **`Empresas` y `Sesion` son clases de métodos `static`**, no singletons como el resto del negocio, y `Empresas` guarda cada empresa en un `record` metido dentro de la propia clase (`Empresas.EmpresaInfo`). Además usa `var`, streams, `::`, ternarios e `IllegalArgumentException`.
- La carpeta de cada empresa se llama en el código **«slug»**, una palabra que no explica nada.

Decisiones F8 y F11 del replanteo (`borrador_changes/analisis-desde-cero.md`).

## What Changes

- **Las empresas solo se crean, se eligen y se eliminan en el arranque.** El arranque gana un botón **Eliminar**, con confirmación.
- **Configuración pierde la sección «Empresas»** y gana el botón **«Cambiar de empresa»**, que cierra la empresa en uso y vuelve a abrir la ventana de arranque.
- **Sin bloqueo**: al entrar se abre siempre el menú. Si faltan datos de la empresa, el menú muestra un **aviso con los datos que faltan y un botón «Completar datos»**. La barra de navegación deja de bloquearse.
- **Los datos se exigen donde hacen falta**: guardar una factura, crear una rectificativa, generar las mensuales y exportar a PDF avisan y no siguen si la empresa está incompleta. Esto estaba previsto para el módulo de facturas, pero sin ello quitar el bloqueo dejaría guardar facturas sin los datos de la empresa.
- **`Empresas` y `Sesion` pasan a ser singletons** (`Empresas.getEmpresas()`, `Sesion.getSesion()`), con `alta`, `baja`, `listado`, `abrir` y `cerrar`.
- **El `record` sale a una clase de datos normal**, `EmpresaDisponible` (carpeta y nombre), que se ordena por nombre y se escribe sola en el desplegable del arranque. Así desaparecen las dos celdas anónimas que lo pintaban.
- **«slug» pasa a llamarse «carpeta»** en todo el código que se toca.
- **`Controlador` y `Modelo`** reciben las operaciones de empresa y la comprobación de datos.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Gestión de empresas» (todo en el arranque, eliminar con confirmación, «Cambiar de empresa»), «Datos obligatorios de la empresa» (sin bloqueo, aviso en el menú, comprobación al guardar, rectificar, generar y exportar), «Configuración» y «Configuración organizada por secciones» (sin la sección Empresas).

## A qué afecta

- **Nuevo**: `modelo/dominio/EmpresaDisponible`.
- **Se rehacen**: `modelo/negocio/Empresas`, `modelo/negocio/Sesion`.
- **Cambian**: `vista/Vista`, `vista/LanzadorVentanaPrincipal`, `controlador/Controlador`, `modelo/Modelo`, `modelo/negocio/Configuracion`, `modelo/negocio/Reloj`, `vista/controlador/ArranqueController`, `vista/controlador/MenuPrincipalController`, `vista/controlador/ConfiguracionController`, `vista/controlador/BarraNavegacionController`, `Arranque.fxml`, `MenuPrincipal.fxml`, `Configuracion.fxml` y `temas/base.css`.
- **Solo plumbing** (cambian las llamadas, no lo que hacen): `fichero/CopiaSeguridad`, `modelo/negocio/sqlite/CargarDemo`, `PreparacionDatos`, `vista/controlador/CopiaSeguridadController`, y los cinco sitios que ahora comprueban los datos de la empresa en `EditorController`, `HistoricoController` y `GenerarFacturasMensualesController`.
- **Tests**: se rehace `EmpresasTest`, se añade `EmpresaDisponibleTest`, se amplía `ConfiguracionTest` y se adaptan `CopiaSeguridadTest`, `CopiaSeguridadDAOTest` y `PreparacionDatosTest`.
- **Queda fuera**: el ejercicio y la fecha de trabajo del arranque siguen como están (F4). `Empresa` (los datos fiscales) no se valida todavía en sus setters: eso va con Configuración, en el módulo siguiente. Restaurar una copia como empresa nueva sigue ofreciendo cambiar a ella desde Copias (F12 es del módulo de copias).
