> Las razones y las firmas exactas están en `design.md`. No cambia ningún comportamiento visible. No añadir comentarios en el código.

## 1. Servicios nuevos

- [x] 1.1 Crear `src/main/java/com/alcazaba/facturacion/service/ClienteService.java` con constructor `ClienteService(ClienteRepository)` y los métodos de `design.md - D1`, cada uno delegando en el método homónimo del repositorio.
- [x] 1.2 Crear `service/SerieService.java` con constructor `SerieService(SerieRepository, FacturaRepository)`. `tieneFacturas(long serieId)` delega en `FacturaRepository.serieTieneFacturas`. Ver `design.md - D2`.
- [x] 1.3 Crear `service/IvaService.java` con constructor `IvaService(IvaRepository)`.
- [x] 1.4 Crear `service/RetencionService.java` con constructor `RetencionService(TipoRetencionRepository)`.
- [x] 1.5 Crear `service/ConfigService.java` con constructor `ConfigService(ConfigRepository)`.
- [x] 1.6 En los cinco: campos `private final`, métodos `public` con `throws SQLException`, sin lógica añadida y sin métodos que la interfaz no use hoy.

## 2. Servicios.java

- [x] 2.1 En `service/Servicios.java`, convertir los diez repositorios en variables locales del constructor, creando una sola instancia de cada uno. Ver `design.md - D3`.
- [x] 2.2 Cambiar el tipo de los campos públicos `clientes`, `series`, `ivas`, `retenciones` y `config` a `ClienteService`, `SerieService`, `IvaService`, `RetencionService` y `ConfigService`.
- [x] 2.3 Eliminar los campos públicos `facturas`, `versiones`, `lineas`, `historial` y `numerosDisponibles`.
- [x] 2.4 Construir los servicios existentes (`numeros`, `versionado`, `factura`, `estado`, `rectificativas`, `facturacionMensual`, `historialService`, `backup`) con las variables locales, en el mismo orden y con los mismos argumentos que hoy.

## 3. Interfaz

- [x] 3.1 Compilar (`mvn -q compile`) y corregir cada error en `ui/`. El único uso que no compila por diseño es `ConfiguracionController.eliminarSerie`: cambiar `servicios.facturas.serieTieneFacturas(s.getId())` por `servicios.series.tieneFacturas(s.getId())`.
- [x] 3.2 Comprobar que no queda ningún import de `com.alcazaba.facturacion.repository` en `src/main/java/com/alcazaba/facturacion/ui/`.
- [x] 3.3 Comprobar con una búsqueda que `Servicios.java` no tiene ningún campo `public` cuyo tipo termine en `Repository`.
- [x] 3.4 No tocar el SQL directo de `BackupController` (líneas 216-217) ni `Database.dataDir()` de `EditorController`: son de otro change.

## 4. Tests

- [x] 4.1 Compilar los tests (`mvn -q test-compile`) y ajustar `src/test/java/com/alcazaba/facturacion/ui/EditorIvaInactivoTest.java` y `ClientesNifValidationTest.java` si algún uso no compila. Ver `design.md - D4`.
- [x] 4.2 No cambiar lo que comprueba ningún test.

## 5. Verificación

- [x] 5.1 `mvn test` en verde.
- [ ] 5.2 Arrancar la aplicación y comprobar: Clientes (listar, crear, editar, activar/desactivar, borrar), Configuración (empresa, preferencias, IVA, retenciones, series, incluido intentar eliminar una serie con facturas), Editor (cargar series, IVA, retenciones, buscar cliente, guardar y exportar PDF), Histórico (filtro de series, exportar) y Facturación mensual (listas de clientes, series, IVA y retenciones).
