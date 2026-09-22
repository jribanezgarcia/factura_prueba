> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código, los mensajes y los Javadoc exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: solo `Exception` en lo nuevo, sin ternarios, sin `var`, sin streams, sin `::`, **sin clases dentro de clases ni clases anónimas**, sin `Optional` (salvo el de `showAndWait()` con `isPresent()`/`get()`), siempre `import` y Javadoc corto en primera persona del plural. En los ficheros que solo cambian de plumbing (sección 9) no se arregla nada más. **No tocar** el `.root` de los `temas/tema-*.css` ni la sección Series.

## 1. Clases de datos

- [ ] 1.1 Rehacer `modelo/dominio/Empresa.java`: constructor con los ocho obligatorios, setters que validan con sus `errorX`, opcionales con `""`, constantes `CABECERA_TEXTO`/`CABECERA_LOGO`, `isCabeceraLogo()`, constructor copia y `equals`/`hashCode` por NIF. Sin los cuatro campos del logo. Ver `design.md - D1`.
- [ ] 1.2 Rehacer `modelo/dominio/TipoIva.java`: constructor, `errorNombre`, `errorPorcentaje(String)`, suplido que deja el porcentaje en `null`, getters de texto y `toString()`; borrar `label()`. Ver `design.md - D3`.
- [ ] 1.3 Rehacer `modelo/dominio/TipoRetencion.java` igual. Ver `design.md - D3`.

## 2. Negocio

- [ ] 2.1 Rehacer `modelo/negocio/Configuracion.java` como singleton con el SQL: `buscarEmpresa()` (que devuelve `null` si está incompleta), `modificarEmpresa`, `preferencia` y `guardarPreferencia`. Borrar `datosPendientes`, `empresaCompleta` y `comprobarEmpresaCompleta`. Ver `design.md - D2`.
- [ ] 2.2 Rehacer `modelo/negocio/TiposIva.java` y `TiposRetencion.java` como singletons con el SQL: `listado`, `buscar`, `alta`, `modificar` (con sus guardas) y `enUso`. Ver `design.md - D4`.
- [ ] 2.3 Borrar `modelo/negocio/sqlite/ConfiguracionDAO.java`, `TipoIvaDAO.java` y `TipoRetencionDAO.java`.
- [ ] 2.4 `grep -rn "SELECT \*" src/main/java/cabofactu/modelo/negocio/Configuracion.java src/main/java/cabofactu/modelo/negocio/TiposIva.java src/main/java/cabofactu/modelo/negocio/TiposRetencion.java`: sin resultados.

## 3. Controlador y Modelo

- [ ] 3.1 En `modelo/Modelo.java` y `controlador/Controlador.java`, las operaciones de `design.md - D5`, de una línea cada una. Borrar `getConfiguracion()`, `getTiposIva()`, `getTiposRetencion()`, `datosPendientesEmpresa()` y `comprobarDatosEmpresa()`.

## 4. Vista, temas y barra

- [ ] 4.1 `vista/Vista.java`: `mostrarInicio()` con el bloqueo y la constante `CONFIGURACION`; borrar `comprobarDatosEmpresa()`; `GestorTemas.aplicar(escena)`. Ver `design.md - D6`.
- [ ] 4.2 `vista/utilidades/GestorTemas.java`: `aplicar(Scene)` sin `Modelo`, `guardar()` por el controlador, y los nuevos `nombres()` y `claveDe(String)`. Ver `design.md - D6`.
- [ ] 4.3 `BarraNavegacionController`: volver a añadir `bloquearSalvoSalir()`, desactivando los seis botones por su nombre. Ver `design.md - D6`.

## 5. Configuración: lista lateral, bloqueo y Guardar

- [ ] 5.1 `Configuracion.fxml`: cambiar el `ListView` por el `VBox` con `Label` y `ToggleButton` de `design.md - D7`; `ToggleGroup grupoSecciones` en `fx:define`.
- [ ] 5.2 `ConfiguracionController`: `verEmpresa`, `verCabecera`, `verPdf`, `verIva`, `verRetenciones`, `verSeries` y `mostrarSeccion(VBox, ToggleButton, boolean)`. Borrar `ItemSeccion`, `configurarSecciones`, la celda anónima y el campo `listaSecciones`. Ver `design.md - D7`.
- [ ] 5.3 `temas/base.css`: sustituir las reglas de `.lista-secciones .list-cell` por las de botones de `design.md - D7`.
- [ ] 5.4 `ConfiguracionController.initialize` y `cargarEmpresa()` con el bloqueo (`bloqueada`, `lblDatosPendientes`, nombre propuesto, `bloquearSalvoSalir()`, `btnVolver` desactivado). Nuevo texto de `lblDatosPendientes`. Ver `design.md - D7`.
- [ ] 5.5 Guardar global: `guardar`, `marcarCamposMalos`, `revisar`, `quitarMarcas`, `empresaDeLosCampos` y `guardarPreferenciasPdf`. Ver `design.md - D7`.
- [ ] 5.6 Vista previa: `repintarPrevia()` con `empresaDeLosCampos()` en un `try`, `txtNombre` como disparador, y `PreviaCabecera.mostrar(null, …)` pinta el aviso. Ver `design.md - D7`.
- [ ] 5.7 Tema sin traductor: `comboTema` con `GestorTemas.nombres()` y `claveDe`; borrar el `StringConverter` anónimo de `cargarTema`. Ver `design.md - D7`.

## 6. IVA y retenciones con ficha

- [ ] 6.1 Crear `FichaTipoIva.fxml` y `vista/controlador/FichaTipoIvaController.java` con el patrón de `FichaClienteController` y las reglas de modo editar y modo añadir de `design.md - D8`.
- [ ] 6.2 Crear `FichaTipoRetencion.fxml` y `vista/controlador/FichaTipoRetencionController.java` igual. Ver `design.md - D8`.
- [ ] 6.3 En `Configuracion.fxml`, secciones IVA y Retenciones: fuera las filas de alta rápida y el botón «Inactivar/Activar»; botones Nuevo y Editar encima de la tabla; tabla con `onMouseClicked`. Ver `design.md - D7`.
- [ ] 6.4 En `ConfiguracionController`: columnas con `PropertyValueFactory` sobre los getters de texto; `seleccionarIva`, `nuevoIva`, `editarIva`, `abrirFichaIva` y lo mismo para retenciones. Borrar el código viejo de las altas rápidas. Ver `design.md - D7`.

## 7. Menú

- [ ] 7.1 Quitar la franja de `MenuPrincipal.fxml`, `mostrarDatosPendientes()` y `completarDatos()` de `MenuPrincipalController`, y `.franja-aviso` de `base.css`; `cargarEmpresa()` con `buscarEmpresa()`. Ver `design.md - D9`.

## 8. Base de datos

- [ ] 8.1 `src/main/resources/db/crear_tablas.sql` y la lista de `CopiaSeguridadDAO`: fuera `logo_x`, `logo_y`, `logo_ancho` y `logo_alto`. Ver `design.md - D10`.

## 9. Plumbing

- [ ] 9.1 Los cambios de la tabla de `design.md - D11` en `EditorController`, `HistoricoController`, `GenerarFacturasMensualesController`, `CopiaSeguridadController`, `Empresas.recordarTema`, `Facturas.retencionDeVersion` y `Rectificativas`, incluida la retirada de las cinco llamadas a `comprobarDatosEmpresa()`.
- [ ] 9.2 `grep -rn "ConfiguracionDAO\|TipoIvaDAO\|TipoRetencionDAO\|getConfiguracion()\|getTiposIva()\|getTiposRetencion()\|comprobarDatosEmpresa\|datosPendientes\|franjaDatosPendientes\|ItemSeccion\|\.label()" src/main/java`: sin resultados (el `.label()` de los registros del PDF no cuenta).
- [ ] 9.3 `mvn -q compile` sin errores.

## 10. Tests

- [ ] 10.1 Crear `EmpresaTest`, `TipoIvaTest` y `TipoRetencionTest` en `src/test/java/cabofactu/modelo/dominio/`. Ver `design.md - D12`.
- [ ] 10.2 Crear `TiposIvaTest` y `TiposRetencionTest` en `src/test/java/cabofactu/modelo/negocio/`, contra una base temporal; borrar `sqlite/TipoRetencionDAOTest.java`. Ver `design.md - D12`.
- [ ] 10.3 Rehacer `ConfiguracionTest`. Ver `design.md - D12`.
- [ ] 10.4 Adaptar `CalculosTest`, `FacturasTest`, `FacturacionMensualTest`, `ConstructorDocumentoFacturaTest`, `DisposicionCabeceraTest`, `ExportadorPdfTest`, `CopiaSeguridadTest` y `EmpresasTest`.
- [ ] 10.5 Añadir `FichaTipoIva.fxml` y `FichaTipoRetencion.fxml` a `CargaPantallasTest`.
- [ ] 10.6 Con la aplicación cerrada, borrar `target` y ejecutar `mvn test`: todos en verde y **ningún** `ClassCastException` en el log.
- [ ] 10.7 En `Empresa`, `TipoIva`, `TipoRetencion`, `Configuracion`, `TiposIva`, `TiposRetencion`, las dos fichas y las líneas nuevas de `ConfiguracionController`, `Vista`, `GestorTemas` y `MenuPrincipalController`, buscar lo que prohíbe `AGENTS.md`: ninguno. En `ConfiguracionController` solo pueden quedar el `StringConverter` anónimo y el `Serie.SufijoFecha` de la sección Series.
- [ ] 10.8 `openspec validate modulo-configuracion --strict` sin errores.

## 11. Documentación y estado

- [ ] 11.1 Añadir este change a «En curso» en `ESTADO.md`.

## 12. Pruebas manuales

- [ ] 12.1 Arranque → «Nueva...» → «Prueba Dos» → Entrar: se abre **Configuración** en la sección Empresa, con el texto «Para empezar a usar el programa…» y «Prueba Dos» propuesto en el nombre. En la barra de arriba solo funciona **Salir**, y **Volver** está desactivado.
- [ ] 12.2 Durante el bloqueo, recorrer las seis secciones de la lista lateral: todas se abren, y la lista **se ve igual que antes** (títulos en gris con su raya, sección elegida resaltada, resaltado al pasar el ratón).
- [ ] 12.3 Pulsar dos veces seguidas la misma sección de la lista: sigue marcada y su contenido sigue a la vista.
- [ ] 12.4 Dejar vacíos el NIF y el teléfono y pulsar «Guardar configuración»: **los dos campos en rojo** y un único aviso, el del NIF. Probar también un NIF mal escrito, un CP que empiece por 53 y un email sin arroba.
- [ ] 12.5 Pulsar Guardar estando en la sección PDF y apariencia con algún dato de la empresa mal: salta a la sección Empresa con los campos marcados.
- [ ] 12.6 Durante el bloqueo, «Cambiar de empresa» → Aceptar: vuelve al arranque sin pedir los datos.
- [ ] 12.7 Volver a «Prueba Dos», rellenar todo bien y guardar: «Datos de la empresa completados», pasa al **menú** y la barra ya funciona entera.
- [ ] 12.8 Cabecera y pie: con los datos completos, la vista previa se ve; cambiar a logo, elegir una imagen, escribir un pie legal y guardar. Salir, volver a entrar: se conserva todo.
- [ ] 12.9 En una empresa nueva todavía incompleta, la vista previa muestra el aviso «La vista previa aparece cuando…» en lugar de una cabecera a medias.
- [ ] 12.10 PDF y apariencia: cambiar el tema (el desplegable muestra «Negro y dorado», «Sakura»…), el color y la carpeta; guardar; al volver a entrar se conservan y el tema se aplica.
- [ ] 12.11 IVA → **Nuevo**: se abre la ficha en ventana propia. Guardar con el nombre vacío y el porcentaje `150`: los dos en rojo y un solo aviso. Corregir y guardar: aparece en la tabla.
- [ ] 12.12 IVA → doble clic en un tipo **usado en facturas** («IVA 21%» en la demo): el porcentaje sale desactivado con su aviso y «Es suplido» también; cambiar el nombre y guardar: se guarda.
- [ ] 12.13 IVA → Nuevo → marcar «Es suplido»: el porcentaje se vacía y se desactiva; guardar: en la tabla sale «Suplido».
- [ ] 12.14 IVA → editar un tipo y desmarcar «Activo»: en la tabla pasa a «No» y ya no se ofrece en el Editor para facturas nuevas.
- [ ] 12.15 IVA → Nuevo → escribir algo y cerrar con Cancelar o con la X: pregunta si descartar.
- [ ] 12.16 Retenciones: Nuevo, editar y el porcentaje bloqueado en una retención usada («IRPF profesional» en la demo), igual que en IVA.
- [ ] 12.17 Series: se ve y funciona **exactamente igual que antes**.
- [ ] 12.18 Editor: el desplegable de IVA de las líneas y el de retención («Sin retención» primero) siguen cargándose; guardar una factura y exportarla a PDF funciona, con la cabecera y el pie de la empresa.
- [ ] 12.19 Histórico: exportar a PDF funciona. «Facturar mes»: sus desplegables de IVA y retención se cargan.
- [ ] 12.20 Menú de una empresa completa: ya no hay franja, y la empresa de demostración entra directa al menú.
