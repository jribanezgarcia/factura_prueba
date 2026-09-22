## Why

Configuración es la pantalla más grande que queda después del Editor y tampoco sigue las normas de `AGENTS.md`:

- **`ConfiguracionController` tiene 942 líneas** y dentro lleva una clase (`ItemSeccion`) y dos clases anónimas (la celda de la lista lateral y el traductor del desplegable del tema).
- **`Empresa` no se valida**. Las reglas de sus datos obligatorios están repetidas en `Configuracion.datosPendientes()`, y se guardan **cuatro columnas del logo** (`logo_x`, `logo_y`, `logo_ancho`, `logo_alto`) que nadie lee.
- **IVA y retenciones siguen el patrón viejo**: un negocio que solo reenvía, un DAO con el SQL (`SELECT *`, ternarios, `DatosException`) y unas filas de «alta rápida» debajo de la tabla, distintas de la ficha de Clientes.

Además, **el usuario decide volver a bloquear** la aplicación cuando a la empresa le faltan datos obligatorios (22/09/2026). El módulo anterior lo había quitado siguiendo la decisión F8: ahora se vuelve al bloqueo, pero dejando salir por «Cambiar de empresa».

## What Changes

- **La empresa incompleta bloquea**: al entrar se abre Configuración en la sección Empresa con un texto explicativo, la barra de navegación solo deja Salir y el botón Volver se desactiva. Se pueden usar todas las secciones de Configuración, Guardar y «Cambiar de empresa». Al guardar los datos completos se pasa al menú.
- **Se quitan la franja del menú y las cinco comprobaciones de «Faltan datos»** que puso el módulo anterior: con el bloqueo, esas pantallas no se pueden abrir.
- **`Empresa` se valida igual que `Cliente`**: los ocho obligatorios en el constructor, un `errorX` por campo y todos los campos malos en rojo. Una empresa incompleta es `null`. Salen los cuatro campos del logo.
- **`Configuracion`, `TiposIva` y `TiposRetencion` pasan a ser singletons con el SQL dentro**. `ConfiguracionDAO`, `TipoIvaDAO` y `TipoRetencionDAO` desaparecen.
- **`TipoIva` y `TipoRetencion` se validan solos**, con sus `errorX`.
- **IVA y retenciones se dan de alta y se editan en una ficha modal** (`FichaTipoIva`, `FichaTipoRetencion`), como Clientes.
- **Configuración sigue siendo un solo FXML con un solo controlador, y se ve igual.** La lista lateral pasa al FXML (`Label` para los títulos y `ToggleButton` para las secciones) y desaparecen `ItemSeccion` y las dos clases anónimas.
- **Un solo botón «Guardar configuración»**, como hoy, que guarda la empresa, la cabecera y las preferencias del PDF y la apariencia.
- **`Controlador` y `Modelo`** reciben las operaciones de configuración, IVA y retenciones, y desaparecen `getConfiguracion()`, `getTiposIva()` y `getTiposRetencion()`.

## Capacidades

### Capacidades nuevas

Ninguna.

### Capacidades modificadas

- `invoicing`: «Datos obligatorios de la empresa» (vuelve el bloqueo, con «Cambiar de empresa» disponible y todos los campos malos en rojo), «Gestión de empresas» (una empresa recién creada abre Configuración), «IVA» (ficha propia, y qué no se puede cambiar de un tipo existente) y «Ventana» (las altas rápidas solo quedan en Series).

## A qué afecta

- **Nuevo**: `vista/controlador/FichaTipoIvaController`, `vista/controlador/FichaTipoRetencionController`, `FichaTipoIva.fxml`, `FichaTipoRetencion.fxml`.
- **Se borran**: `modelo/negocio/sqlite/ConfiguracionDAO`, `modelo/negocio/sqlite/TipoIvaDAO`, `modelo/negocio/sqlite/TipoRetencionDAO`.
- **Se rehacen**: `modelo/dominio/Empresa`, `modelo/dominio/TipoIva`, `modelo/dominio/TipoRetencion`, `modelo/negocio/Configuracion`, `modelo/negocio/TiposIva`, `modelo/negocio/TiposRetencion`, y las secciones Empresa, Cabecera y pie, PDF y apariencia, IVA y Retenciones de `ConfiguracionController` y `Configuracion.fxml`.
- **Cambian**: `controlador/Controlador`, `modelo/Modelo`, `vista/Vista`, `vista/utilidades/GestorTemas`, `vista/controlador/BarraNavegacionController`, `vista/controlador/MenuPrincipalController`, `MenuPrincipal.fxml`, `temas/base.css`, `db/crear_tablas.sql`.
- **Solo plumbing**: `EditorController`, `HistoricoController`, `GenerarFacturasMensualesController`, `CopiaSeguridadController`, `Empresas.recordarTema`, `Facturas.retencionDeVersion`, `Rectificativas`, `CopiaSeguridadDAO` y `vista/utilidades/PreviaCabecera`.
- **Tests**: nuevos `EmpresaTest`, `TipoIvaTest`, `TipoRetencionTest`, `TiposIvaTest` y `TiposRetencionTest`; se rehace `ConfiguracionTest`; se borra `TipoRetencionDAOTest`; y se adaptan los que construyen estas clases.
- **Queda fuera**: la sección Series se queda con su código de hoy, incluidos su traductor anónimo y el `enum` dentro de `Serie`, y se rehace con la numeración en el módulo de facturas. `PreviaCabecera` y la cabecera del PDF no cambian por dentro.
