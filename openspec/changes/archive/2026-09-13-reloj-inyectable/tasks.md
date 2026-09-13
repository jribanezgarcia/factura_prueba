> Las razones y las firmas exactas están en `design.md`. No cambia ningún comportamiento visible. No añadir comentarios en el código.

## 1. Reloj en los servicios

- [x] 1.1 Crear `src/main/java/com/alcazaba/facturacion/service/Reloj.java` tal como está en `design.md - D3`.
- [x] 1.2 En `service/NumeroService.java`, añadir `Clock clock` como último parámetro del constructor y cambiar los cinco `LocalDate.now()` por `LocalDate.now(clock)`. Ver `design.md - D1`.
- [x] 1.3 En `service/VersionadoService.java`, añadir `Clock clock` como último parámetro y cambiar los dos `LocalDateTime.now()` por `LocalDateTime.now(clock)`.
- [x] 1.4 En `service/FacturaService.java`, añadir `Clock clock` como último parámetro y cambiar el `LocalDate.now()` de `borrarFactura` por `LocalDate.now(clock)`.
- [x] 1.5 En `service/BackupService.java`, añadir el constructor `BackupService(Clock clock)` y cambiar el `LocalDateTime.now()` de `crearBackup` por `LocalDateTime.now(clock)`.
- [x] 1.6 No dejar sobrecargas de constructor sin `Clock` en ninguno de estos servicios.

## 2. Series

- [x] 2.1 En `repository/SerieRepository.java`, cambiar `insertar(Serie s)` por `insertar(Serie s, int anioContador)` y hacer que `inicializarSiguiente` use ese año. Ver `design.md - D2`.
- [x] 2.2 En `service/SerieService.java`, añadir `Clock clock` como tercer parámetro del constructor e `insertar(Serie s)` pasa a llamar a `serieRepository.insertar(s, LocalDate.now(clock).getYear())`. La firma pública de `SerieService.insertar` no cambia.

## 3. Raíz de composición

- [x] 3.1 En `service/Servicios.java`, añadir el campo `public final Reloj reloj`, el constructor `Servicios(Clock clock)` con todo el montaje, y dejar `Servicios()` delegando en `this(Clock.systemDefaultZone())`. Ver `design.md - D4`.
- [x] 3.2 Pasar el mismo `clock` a `Reloj`, `SerieService`, `NumeroService`, `VersionadoService`, `FacturaService` y `BackupService`.

## 4. Interfaz

- [x] 4.1 `ui/EditorController.java`: en `cargarFechaInicial` y en `crearRectificativa`, sustituir la expresión de fecha de trabajo por `servicios.reloj.fechaTrabajo()`. Quitar el import de `Sesion` si deja de usarse.
- [x] 4.2 `ui/MenuController.java`: lo mismo en `alIniciar`.
- [x] 4.3 `ui/ConfiguracionController.java`: `anioTrabajo()` pasa a `return servicios.reloj.fechaTrabajo().getYear();`. No tocar los `Sesion.empresaSlug`/`EmpresaManager.conectar(..., Sesion.fechaTrabajo())` del cambio de empresa.
- [x] 4.4 `ui/GenerarFacturasMensualesController.java`: en `configurarSpinners`, usar `servicios.reloj.hoy().getYear()`. Comprobar que `servicios` ya está asignado en ese momento; si no, mover la llamada después de `setServicios` sin cambiar lo que se ve. Ver `design.md - D3`.
- [x] 4.5 No tocar `ui/ArranqueController.java`. Ver `design.md - D5`.
- [x] 4.6 Quitar los imports de `LocalDate` que queden sin uso en los controladores tocados.

## 5. Comprobaciones

- [x] 5.1 Buscar `now()` en `src/main/java/com/alcazaba/facturacion/service` y `src/main/java/com/alcazaba/facturacion/repository`: solo pueden quedar llamadas con argumento (`now(clock)`).
- [x] 5.2 Buscar `LocalDate.now()` en `src/main/java/com/alcazaba/facturacion/ui`: solo puede quedar en `ArranqueController`.

## 6. Tests

- [x] 6.1 Compilar los tests (`mvn -q test-compile`) y ajustar las construcciones de `NumeroService`, `VersionadoService`, `FacturaService` y `BackupService` pasando `Clock.systemDefaultZone()`, y las llamadas a `SerieRepository.insertar` pasando `LocalDate.now().getYear()`. Incluye las subclases anónimas de `FacturacionMensualServiceTest`.
- [x] 6.2 No cambiar lo que comprueba ningún test existente.
- [x] 6.3 En `src/test/java/com/alcazaba/facturacion/service/NumeroServiceTest.java`, añadir un test con reloj fijo en 2031 que compruebe que `siguienteCorrelativo(serie)` usa el contador de 2031. Ver `design.md - D6`.
- [x] 6.4 Añadir un test con `VersionadoService` y el mismo reloj fijo que compruebe que `crearVersion` sella `fechaGuardado` con `2031-06-15T12:00`. Puede ir en el test de servicio que ya prepara factura y versiones (`FacturaServiceTest`).

## 7. Verificación

- [x] 7.1 `mvn test` en verde.
- [ ] 7.2 Arrancar la aplicación con una fecha de trabajo distinta de hoy y comprobar: el Menú muestra esa fecha, una factura nueva en el Editor la propone, una rectificativa la toma, en Configuración > Series el siguiente número corresponde a ese año, y la Facturación mensual abre con el año actual.
- [ ] 7.3 Crear una copia de seguridad y comprobar que el nombre del fichero lleva la fecha y hora actuales.
