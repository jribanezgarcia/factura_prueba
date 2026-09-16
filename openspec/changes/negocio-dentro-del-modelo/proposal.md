## Why

Tras `mvc-como-biblioteca8`, el `Modelo` guarda casi todo el negocio y las pantallas lo piden con `Vista.getInstancia().getControlador().getModelo()`. Quedan fuera dos clases `static` que no son herramientas:

- **`Empresas`** es negocio (crear, listar, conectar y borrar empresas), como `Libros` en Biblioteca8.
- **`Sesion`** guarda datos (empresa activa y fecha de trabajo) en campos `static`.

`AGENTS.md` solo permite `static` en clases herramienta que no guardan datos propios. `Calculos` (fórmulas) y `PreferenciasGlobales` (lectura y escritura de un fichero) sí lo son y se quedan como están, igual que `Formatos` y `Conexion`.

## What Changes

- **`Sesion`**: clase normal con campos `private`, `inicializar`, `getEmpresaSlug()` y `getFechaTrabajo()`.
- **`Empresas`**: clase normal que recibe la `Sesion` por constructor; todos sus métodos dejan de ser `static`.
- **`Modelo`** crea una `Sesion` y unas `Empresas` y las da con `getSesion()` y `getEmpresas()`. `Reloj` y `CopiaSeguridad` reciben lo que necesitan por constructor.
- **`CargarDemo`** y **`PreparacionDatos`** (herramientas `static`) reciben las `Empresas` por parámetro.
- **Pantallas**: `Empresas.x(...)` y `Sesion.x()` pasan a la llamada completa `Vista.getInstancia().getControlador().getModelo().getEmpresas()...` / `.getSesion()...`.
- **`AGENTS.md`**: `Calculos` y `PreferenciasGlobales` se añaden a la lista de herramientas `static`.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna: `skip_specs: true`. Es una reorganización interna sin cambio de comportamiento; los specs no citan clases.

## Impact

- Código: `modelo/negocio/Sesion`, `modelo/negocio/Empresas`, `modelo/negocio/Reloj`, `modelo/Modelo`, `fichero/CopiaSeguridad`, `modelo/negocio/sqlite/CargarDemo`, `PreparacionDatos`, `controlador/Controlador`, `vista/controlador/ArranqueController`, `vista/controlador/ConfiguracionController`, `vista/controlador/CopiaSeguridadController`.
- Tests: `EmpresasTest`, `CopiaSeguridadTest`, `CopiaSeguridadDAOTest`, `PreparacionDatosTest`.
- `AGENTS.md` (lista de herramientas `static`).
- Fuera: `Empresas.EmpresaInfo` sigue siendo un record anidado (change `clases-independientes`); los streams, ternarios y `var` que ya tiene `Empresas` fuera de las líneas tocadas (change `java-clasico`).
- Requiere `mvc-como-biblioteca8` archivado.
