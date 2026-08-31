## 1. Infraestructura de datos multi-empresa

- [x] 1.1 Añadir a `db/Database.java` la raíz fija `BASE_DATA_DIR` (sobre `defaultDataDir()`), el método `setEmpresaActiva(String slug)` que fija `dataDir = BASE_DATA_DIR/<slug>`, y `getEmpresasDisponibles()` que lista las subcarpetas de `BASE_DATA_DIR` que contienen `facturas.db`. Verificar que `setDataDir(Path)` para pruebas sigue dirigiendo la carpeta activa sin slug y que `getConnection()` migra la BD activa.
- [x] 1.2 Añadir la migración de instalación en el arranque (v1 → carpetas por empresa): si existe `BASE_DATA_DIR/facturas.db` y no hay ninguna subcarpeta de empresa, crear `BASE_DATA_DIR/comercial_alcazaba/`, mover `facturas.db` y `facturas.lock` dentro, registrar el nombre «Comercial Alcazaba» y fijar `ultima_empresa`. Verificar con un test que conjuga una BD vieja y comprueba que queda reubicada.
- [x] 1.3 Cambiar el lock de instancia única en `Main.java` al nuevo lock de `BASE_DATA_DIR` (aplicación única independiente de la empresa). Verificar que el test de instancia única existente sigue en verde.

## 2. Preferencias globales

- [x] 2.1 Crear la clase `service/PreferenciasGlobales` que lee y escribe `%APPDATA%\Facturacion\preferencias.properties` con `ultima_empresa`, `ventana_x`, `ventana_y`, `ventana_w`, `ventana_h` y `tema`, con valores por defecto cuando no existen. Verificar que lectura/escritura idempotente pasa un test de prueba con archivo temporal.
- [x] 2.2 Sustituir en `Main.java` el guardado/restauración de ventana (`ventana_x/y/w/h`) desde `servicios.config` por `PreferenciasGlobales`. Verificar que primeras ejecuciones abren 800x600 centradas y las siguientes restauran.
- [x] 2.3 Adaptar `ThemeManager` para leer/guardar `tema` desde `PreferenciasGlobales` en lugar de la tabla `preferencias`. Verificar que el tema se aplica y se recuerda entre sesiones con un test de `PreferenciasGlobales`.

## 3. Sesión de trabajo

- [x] 3.1 Crear la clase `service/Sesion` con `empresaSlug` y `fechaTrabajo` (y sus getters/setters), inicializada al confirmar el arranque. Verificar su uso en el resto de tareas por compilación.
- [x] 3.2 Sustituir `fechaTrabajoPreferida()` y la preferencia `fecha_trabajo` en `EditorController` por `Sesion.fechaTrabajo()` como fecha inicial de las nuevas facturas. Verificar que crear una factura nueva se inicializa con la fecha de trabajo de la sesión.
- [x] 3.3 Convertir el `DatePicker` de fecha del menú (`MenuController`) en indicador de solo lectura que muestra `Sesion.fechaTrabajo()` (la fecha ya no se cambia a mitad de sesión). Verificar que el menú muestra la fecha elegida al arrancar y no permite editarla.

## 4. Correlativo por año

- [x] 4.1 Crear la migración SQL `004_serie_siguiente.sql`: tabla `serie_siguiente (serie_id, anio, siguiente)` con PK `(serie_id, anio)`, sembrada con el `siguiente_correlativo` de cada serie para el año en curso. Registrar el script en `Migrations.java`. Verificar que `openspec validate --specs` y la suite existente siguen en verde.
- [x] 4.2 Ampliar `SerieRepository`: `getSiguiente(serieId, anio)`, `actualizarSiguiente(serieId, anio, siguiente)` sobre la tabla nueva, y `correlativosAnuladas`/`correlativosActivos` con filtro por año (año extraído de `factura_version.fecha_factura`). Verificar con tests de repositorio que las consultas por año devuelven los correlativos esperados.
- [x] 4.3 Adaptar `NumeroService` para que `siguienteCorrelativo(serie, LocalDate fecha)` use el año de la fecha y la tabla `serie_siguiente`, limitando la reutilización de anulados al mismo año; mantener una sobrecarga de compatibilidad para los llamadores existentes. Verificar con los casos nuevos de `NumeroServiceTest`.
- [x] 4.4 Ajustar `EditorController.recalcularNumero()` para pedir el siguiente correlativo del año de la fecha de la factura. Verificar que al cambiar la fecha de una factura nueva a otro año el número propuesto usa el correlativo de ese año.
- [x] 4.5 Mostrar y editar en Configuración → Series el «siguiente número» del año de trabajo (`Sesion.fechaTrabajo().getYear()`). Verificar que al guardar, la próxima factura de ese año parte del valor editado y deja intacto el de otros años.

## 5. Pantalla de arranque

- [x] 5.1 Crear `Arranque.fxml` + `ArranqueController` con el listado de empresas (`EmpresaManager.listarEmpresas()`), la última empresa preseleccionada, un `ComboBox` de años para el ejercicio fiscal (por defecto el año en curso), la fecha de trabajo (automática a la fecha del sistema si el ejercicio es el actual, manual y restringida al ejercicio si es otro), un botón «Nueva...» para crear empresa desde el arranque y el botón de confirmación. Verificar que la vista carga en el smoke test de UI (applyCss + layout 800x600).
- [x] 5.2 Implementar `service/EmpresaManager` con `listarEmpresas()`, `crearEmpresa(nombre)` (genera slug, crea carpeta, añade al catálogo, pasa a activa), `conectar(slug, fecha)` (fija empresa activa, reinicia conexión, migra, inicializa `Sesion`) y `eliminarEmpresa(slug)` (solo si no es la activa). Verificar con tests de aislamiento que dos carpetas no comparten datos.
- [x] 5.3 Modificar `Main.java` para: crear `BASE_DATA_DIR`, hacer la migración de instalación (1.2), adquirir el lock global, mostrar `Arranque.fxml` como primera pantalla y solo tras confirmar conectar la empresa, crear `Servicios` y abrir el menú principal. Verificar el flujo completo arrancando la app.
- [x] 5.4 Añadir el catálogo `empresas.properties` (slug → nombre visible) gestionado por `EmpresaManager`. Verificar que las empresas sin entrada del catálogo aparecen con el slug como nombre provisional.
- [x] 5.5 Al abrir el menú principal, pedir el foco del fondo de la escena para que el primer botón de menú no quede resaltado por el foco inicial de JavaFX.

## 6. Gestión de empresas en Configuración

- [x] 6.1 Añadir al `Configuracion.fxml` la pestaña «Empresas» con tabla (nombre visible y slug), botones «Nueva empresa», «Cambiar a esta» y «Eliminar». Verificar en el smoke test de UI que la pestaña carga a 800x600.
- [x] 6.2 Implementar en `ConfiguracionController` los manejadores: «Nueva empresa» (diálogo de nombre → `EmpresaManager.crearEmpresa`), «Cambiar a esta» (conectar empresa y volver al menú principal) y «Eliminar» (confirmación + `EmpresaManager.eliminarEmpresa`, deshabilitado o bloqueado para la empresa actual). Verificar manualmente que al cambiar la empresa el menú y el resto de pantallas muestran los datos de la nueva.
- [x] 6.3 Verificar el requisito de historial global: tras cambiar de empresa el histórico muestra las facturas de esa empresa y no las de la anterior (spec «Aislamiento entre empresas»).

## 7. Verificación final

- [x] 7.1 Ejecutar la suite completa `& "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" test` desde el directorio del proyecto y confirmar que todos los tests pasan (los 59 existentes más los nuevos).
- [x] 7.2 Verificación manual con el usuario: crear una segunda empresa, entrar en 2025 y 2026, comprobar correlativos independientes por año y el aislamiento total entre empresas.

## 8. Submenú Series de Configuración

- [x] 8.1 Eliminar las series por defecto (C, P, R) de `001_init.sql` y ajustar `FacturaServiceTest` y `HistorialServiceTest` para crear su propia serie C. Verificar que una instalación limpia empieza con el listado de series vacío.
- [x] 8.2 Permitir guardar una serie con el código vacío: quitar la validación que exigía código y rechazar el guardado solo si ya existe otra serie sin código (una sola serie sin prefijo a la vez). Verificar con un test de repositorio.
- [x] 8.3 Añadir la eliminación de series en Configuración → Series: botón «Eliminar», borrado confirmado y bloqueado si la serie tiene facturas (activas o históricas), por la clave foránea y por el principio de no destruir el histórico. Añadir `SerieRepository.eliminar` y `FacturaRepository.serieTieneFacturas`. Verificar con tests.
- [x] 8.4 Ejecutar la suite completa y confirmar 77 tests en verde.
