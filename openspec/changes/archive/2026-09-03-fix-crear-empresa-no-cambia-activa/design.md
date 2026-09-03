## Context

`EmpresaManager.crearEmpresa()` activa la empresa nueva (cambia `Database.dataDir()`, cierra la conexión, la abre y migra, cambia `ULTIMA_EMPRESA` y la sesión). En la pantalla de arranque es correcto porque no hay empresa abierta; desde `ConfiguracionController.nuevaEmpresa()` con una empresa en uso deja la aplicación en un estado incoherente donde la UI muestra una empresa pero se escribe en otra. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Que `crearEmpresa` haga solo lo que dice: crear carpeta, base de datos migrada y entrada en el catálogo, sin tocar el estado global de empresa activa.
- Que cambiar de empresa siga siendo una acción explícita (`conectar`), la única puerta para hacerlo.
- Que al crear desde Configuración se ofrezca cambiar a la nueva empresa.

**Non-Goals:**
- No cambiar el esquema de base de datos ni las firmas públicas de `crearEmpresa`/`conectar`.
- No modificar `Main.java` ni `ArranqueController.java`.

## Decisions

### D1. No restaurar el estado global, sino no tocarlo

La solución de "guardar y restaurar `Database.dataDir()`/`Sesion`" deja una ventana en la que la conexión global apunta a la empresa equivocada y una excepción a mitad puede dejar la app apuntando a la base nueva. Se descarta por fragilidad.

En su lugar, `crearEmpresa` abre una conexión JDBC **local y temporal** a la ruta de la empresa nueva (`Database.dbPathDe(slug)`), ejecuta `Migrations.migrate(c)`, y la cierra. La conexión global de la empresa activa no se cierra ni se entera. `Migrations.migrate(Connection)` ya es público y recibe la conexión por parámetro, así que se reutiliza tal cual.

### D2. Accesor `Database.dbPathDe(String slug)`

`DB_FILE` es privada; se añade un accesor junto a `dbPath()` para construir la ruta de cualquier empresa sin activarla:
```java
public static Path dbPathDe(String slug) {
    return baseDataDir.resolve(slug).resolve(DB_FILE);
}
```

### D3. Ofrecer el cambio en Configuración

En `nuevaEmpresa()`, tras crear y refrescar la tabla, se pregunta al usuario si quiere cambiar a la empresa nueva. Si acepta, se selecciona por slug sobre la lista recién refrescada (no se reutiliza el objeto `nueva`, porque `refrescarEmpresas()` repuebla la lista con instancias nuevas) y se llama a `cambiarEmpresa()`, que ya hace `conectar` + navegar al menú. `ArranqueController.nuevaEmpresa()` no cambia porque ya selecciona explícitamente por slug y `entrar()` conecta explícitamente.

## Risks / Trade-offs

- [Instalación nueva: `prepararDatos()` crea «Comercial Alcazaba» sin activarla] → Verificado que `ArranqueController.cargarEmpresas()` preselecciona la primera cuando `ULTIMA_EMPRESA` es null y `entrar()` conecta antes de construir `Servicios`. Si algo llamara a `Database.getConnection()` en esa ventana crearía un `facturas.db` suelto en la raíz; está verificado que hoy no ocurre.
- [Tests de otros servicios que usen `crearEmpresa` como atajo] → `crearEmpresa` solo se usa en `EmpresaManagerTest` entre los tests; `DatabaseTest` no lo usa. Se revisa `BackupServiceTest` si existiera.

## Migration Plan

Sin cambios de datos. Es un cambio de comportamiento de creación de empresa; `conectar` sigue siendo la vía para cambiar. Rollback trivial restaurando `crearEmpresa`.

## Open Questions

Ninguna.
