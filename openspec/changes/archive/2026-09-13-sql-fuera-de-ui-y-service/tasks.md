> Firmas, orden de validación y mensajes exactos en `design.md`. No cambia nada visible. Solo Javadoc breve en las clases y métodos públicos nuevos (`CopiaRepository`, `Database.crearBase`, `Database.migrarBase`, `FacturaRepository.contar`, `BackupService.facturasEmpresaActiva`, `BackupService.versionEsquemaAplicacion`); ningún otro comentario.

## 1. Capa de datos

- [x] 1.1 En `db/Database.java`, extraer `abrir(Path)` y añadir `crearBase(Path)` y `migrarBase(Path)`; `getConnection()` usa `abrir`. Ver `design.md - D3`.
- [x] 1.2 Crear `repository/CopiaRepository.java` con los cuatro métodos de `design.md - D1`, moviendo tal cual desde `BackupService` la lectura y comprobación de la copia (constantes de tablas, `comprobarTablasNucleo`, `elementosFaltantes`, `notaVersion`, `borrarDiario`). Sin `record` nuevos.
- [x] 1.3 En `repository/FacturaRepository.java`, añadir `contar()`. Ver `design.md - D4`.

## 2. Servicios

- [x] 2.1 En `service/BackupService.java`, cambiar el constructor y dejar `crearBackup`, `leerResumen`, `restaurarEnEmpresaActiva` y `restaurarComoEmpresaNueva` llamando a `CopiaRepository`, conservando mensajes. Añadir `facturasEmpresaActiva()` y `versionEsquemaAplicacion()`. Quitar lo que se ha movido al repositorio y los imports de `java.sql`. Ver `design.md - D2`.
- [x] 2.2 En `service/EmpresaManager.java`, usar `Database.crearBase(destino)` en `crearEmpresa` y quitar los imports de `java.sql`.
- [x] 2.3 En `service/Servicios.java`, crear `CopiaRepository` y pasarlo a `BackupService`. Ver `design.md - D6`.

## 3. Interfaz

- [x] 3.1 En `ui/BackupController.java`, aplicar `design.md - D5` y quitar los imports de `Database`, `ResultSet` y `Statement`.

## 4. Instancia única

- [x] 4.1 En `InstanciaUnica.adquirir()`, cerrar el canal si `tryLock()` lanza `IOException` y relanzarla. Ver `design.md - D7`.

## 5. Comprobaciones

- [x] 5.1 Buscar `java.sql`, `DriverManager`, `PRAGMA`, `SELECT`, `VACUUM` y `sqlite_master` en `src/main/java/com/alcazaba/facturacion/service` y `ui`: no debe quedar ninguno.
- [x] 5.2 Buscar `import com.alcazaba.facturacion.db` y `facturacion.db.` en `ui/`: no debe quedar ninguno en `BackupController`.

## 6. Tests

- [x] 6.1 Ajustar la construcción de `BackupService` en `service/BackupServiceTest.java` sin cambiar lo que comprueba. Ver `design.md - D8`.
- [x] 6.2 Crear `src/test/java/com/alcazaba/facturacion/repository/CopiaRepositoryTest.java` con los casos de `design.md - D8`.
- [x] 6.3 Añadir a `src/test/java/com/alcazaba/facturacion/db/DatabaseTest.java` los casos de `crearBase` y `migrarBase`.

## 7. Verificación

- [x] 7.1 `mvn test` en verde (los 13 tests de `BackupServiceTest` incluidos).
- [ ] 7.2 Arrancar la aplicación, ir a Copias y crear una copia: se genera `facturas_AAAAMMDD_HHMMSS.db` en la carpeta elegida.
- [ ] 7.3 Seleccionar esa copia: el resumen muestra empresa, NIF, facturas, última fecha y versión de esquema como antes.
- [ ] 7.4 Restaurarla en la empresa activa y comprobar que se llega al Menú con los datos y que queda la copia de rescate en `copias_previas`.
- [ ] 7.5 Restaurar la misma copia como empresa nueva y comprobar que aparece en el listado y se puede entrar en ella.
- [ ] 7.6 Seleccionar como copia un fichero que no sea una base de datos (por ejemplo, un `.txt` renombrado a `.db`): sale el mismo aviso de error que antes.
- [ ] 7.7 Crear una empresa nueva desde Configuración: se crea sin errores.
