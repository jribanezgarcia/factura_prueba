## 1. Implementación

- [x] 1.1 Añadir en `db/Database.java` el accesor `dbPathDe(String slug)` junto a `dbPath()` y verificar que compila (`mvn compile`).
- [x] 1.2 Reescribir `EmpresaManager.crearEmpresa` para que cree la base de datos de la empresa nueva sobre una conexión JDBC local y temporal (sin `setEmpresaActiva`, `resetConnection`, `getConnection` globales, `ULTIMA_EMPRESA` ni `Sesion.inicializar`); verificar que compila y que `conectar`, `eliminarEmpresa`, `listarEmpresas`, `registrarNombre` y `slugDe` quedan intactos.
- [x] 1.3 En `ui/ConfiguracionController.nuevaEmpresa()`, sustituir el aviso + refresco por la oferta de cambiar a la empresa nueva (seleccionando por slug sobre la lista refrescada y llamando a `cambiarEmpresa()`); verificar que compila.

## 2. Pruebas

- [x] 2.1 En `EmpresaManagerTest`, renombrar `creaEmpresaYLaDejaActiva` a `crearCreaLaBaseSinActivarla` y comprobar `Files.exists(Database.dbPathDe("mi_empresa"))`.
- [x] 2.2 Ajustar `dosEmpresasNoCompartenDatos`, `eliminarEmpresaBorraCarpeta` y `noSePuedeEliminarLaActiva` intercalando `EmpresaManager.conectar(slug, fecha)` donde antes se usaba `crearEmpresa` como atajo para cambiar de empresa.
- [x] 2.3 Añadir los tests `crearNoCambiaLaEmpresaActiva`, `crearNoRompeLaConexionEnCurso` (reproduce el fallo real: lee una serie insertada tras crear otra empresa) y `laBaseNuevaTieneElEsquemaCompleto` (`PRAGMA user_version == Migrations.ultimaVersion()`).
- [x] 2.4 Ejecutar la suite completa (`mvn test`) y confirmar los tests en verde (141/141).

## 3. Cierre

- [x] 3.1 Sync de specs (este change SI lleva delta) y actualizar CONTINUAR_MAÑANA.md.
- [x] 3.2 /opsx-archive y commit/push.
