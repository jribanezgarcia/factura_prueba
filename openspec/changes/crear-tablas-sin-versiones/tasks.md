> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. Código, mensajes y Javadoc exactos en `design.md`. Seguir `AGENTS.md` en todo el código nuevo o modificado, también en los tests: sin ternarios, sin `var`, sin streams ni `::`, siempre `import`, Javadoc corto en primera persona del plural. No arreglar de paso construcciones antiguas fuera de las líneas tocadas.

## 1. Script y creación de tablas

- [ ] 1.1 Mover con `git mv` `src/main/resources/db/migrations/001_baseline.sql` a `src/main/resources/db/crear_tablas.sql` sin cambiar su contenido. Ver `design.md - D3`.
- [ ] 1.2 En `modelo/negocio/sqlite/Conexion.java`, añadir `SCRIPT_TABLAS`, `crearTablasSiFaltan`, `existeTabla`, `leerScript` y `ejecutarScript`. Ver `design.md - D1` y `D2`.
- [ ] 1.3 En `Conexion`, `establecerConexion()` y `crearBase(Path)` llaman a `crearTablasSiFaltan`; borrar `migrarBase(Path)`. Ver `design.md - D2`.
- [ ] 1.4 Borrar `modelo/negocio/sqlite/Migraciones.java`.

## 2. Copias de seguridad

- [ ] 2.1 En `modelo/negocio/sqlite/CopiaSeguridadDAO.java`, quitar `Conexion.migrarBase` de `instalarComoBase`. Ver `design.md - D4`.
- [ ] 2.2 En `CopiaSeguridadDAO.leerResumen`, quitar la lectura de la versión, su comprobación y `notaVersion`. Ver `design.md - D5`.
- [ ] 2.3 En `fichero/CopiaSeguridad.java`, quitar `versionActual` de `ResumenCopia` y el método `versionEsquemaAplicacion()`. Ver `design.md - D5`.
- [ ] 2.4 En `vista/controlador/CopiaSeguridadController.java`, quitar de `mostrarResumen` las líneas de la versión de esquema. Ver `design.md - D5`.

## 3. Javadoc

- [ ] 3.1 Actualizar el Javadoc de `modelo/negocio/sqlite/CargarDemo.java` y de `Empresas.conectar`. Ver `design.md - D6`.
- [ ] 3.2 `grep -rni "migraci\|user_version\|versionActual\|versionEsquema" src/main`: sin resultados.
- [ ] 3.3 `mvn -q compile` sin errores.

## 4. Tests

- [ ] 4.1 Borrar `src/test/java/cabofactu/modelo/negocio/sqlite/MigracionesTest.java`.
- [ ] 4.2 En `ConexionTest`, sustituir `crearBaseDejaVersionDeEsquema` y `migrarBaseNoFalla` por `crearBaseCreaTodasLasTablas` y `crearTablasDosVecesNoDuplica`. Ver `design.md - D7`.
- [ ] 4.3 Adaptar `EmpresasTest.laBaseNuevaTieneElEsquemaCompleto`. Ver `design.md - D7`.
- [ ] 4.4 Adaptar los cinco tests de `CopiaSeguridadTest` indicados en `design.md - D7`.
- [ ] 4.5 `grep -rni "migraci\|user_version\|versionActual" src/test`: sin resultados.
- [ ] 4.6 Con la aplicación cerrada, borrar `target` y `mvn test`: todos en verde.
- [ ] 4.7 Buscar en las líneas nuevas o modificadas construcciones prohibidas por `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, nombres completos de clase): ninguna.
- [ ] 4.8 `openspec validate crear-tablas-sin-versiones --strict` sin errores.

## 5. Pruebas manuales

- [ ] 5.1 Cerrar la aplicación, borrar la carpeta `%APPDATA%\Facturacion` y abrir: se carga la empresa de demostración y se puede entrar y ver sus facturas.
- [ ] 5.2 Crear una empresa nueva, entrar y completar sus datos: en una factura nueva salen los tipos de IVA 21 %, 10 %, Exento y Suplido, **una sola vez cada uno**.
- [ ] 5.3 Cerrar y volver a abrir la aplicación y entrar en esa empresa: los tipos de IVA siguen sin repetirse.
- [ ] 5.4 Hacer una copia de seguridad y seleccionarla para restaurar: el resumen muestra empresa, NIF, facturas y última fecha, **sin** línea de versión de esquema.
- [ ] 5.5 Restaurar esa copia sobre la empresa activa y, después, como empresa nueva: las dos terminan bien y se puede entrar en la empresa nueva.
