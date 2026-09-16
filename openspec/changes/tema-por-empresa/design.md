## Context

Estado a 16/09/2026 (commit `e5d5c00`). Números de línea orientativos.

**Dónde se guarda hoy el tema**: `PreferenciasGlobales` (`modelo/negocio`) lee y escribe `preferencias.properties` en la carpeta raíz, fuera de las bases de datos. Tiene las claves `ULTIMA_EMPRESA` y `TEMA` (`"tema"`).

**`GestorTemas`** (`vista/utilidades`):
- `aplicar(Scene, Modelo)` lee `PreferenciasGlobales.get(PREF_TEMA)`; si no es un tema conocido, usa `POR_DEFECTO` (`"biblioteca8"`). El parámetro `modelo` no se usa.
- `seleccionar(Scene, String)` cambia `activo` y las hojas de la escena (vista previa del combo, sin guardar).
- `guardar(Modelo)` escribe `activo` en `PreferenciasGlobales`. El parámetro `modelo` no se usa.

**Quién aplica el tema**: `Navegador.mostrar` (~63) al cargar cada pantalla, incluida la de arranque, que se muestra antes de conectar con ninguna empresa; y `GenerarFacturasMensualesController.abrir` (~124). `Dialogos.aplicarTema` usa `GestorTemas.hojas()`.

**Quién guarda**: `ConfiguracionController.guardar` (~396) llama a `GestorTemas.guardar(modelo)` justo después de guardar `color_pdf` con `modelo.getConfiguracion().setPreferencia(...)`.

**Preferencias de empresa**: `ConfiguracionDAO.getPreferencia/setPreferencia` sobre la tabla `preferencias (clave, valor)` de la base activa (`001_baseline.sql:134`). `Configuracion.getPreferencia/setPreferencia` delegan en él.

**Conexión con una empresa**: `Empresas.conectar(slug, fecha)` (~66) fija la empresa activa, abre la base, inicia la sesión y guarda `ULTIMA_EMPRESA`. La llaman `ArranqueController` (~216), `ConfiguracionController.cambiarEmpresa` (~995) y `CopiaSeguridadController` al restaurar como empresa nueva (~290). Después, todas navegan con `nav.mostrarInicio()` o `entrarEnMenu()`, que vuelven a aplicar el tema.

**Restaurar sobre la empresa activa**: `CopiaSeguridad.restaurarEnEmpresaActiva` (~82) sustituye la base con `copiaSeguridadDAO.reemplazarBaseActiva(origen)`; el controlador después hace `nav.mostrarInicio()`.

**Spec**: requisito «Configuración» (`invoicing/spec.md` ~558): «El tema SHALL guardarse de forma global, compartido entre empresas»; requisito «Configuración organizada por secciones» (~605): «por tratarse de una preferencia global compartida entre empresas».

**Decisiones del usuario (15/09/2026)**: tema por empresa, como `color_pdf`; la pantalla de arranque usa el tema de la última empresa abierta; los datos se van a reiniciar, no hay migración.

## Goals / Non-Goals

**Goals:** cada empresa guarda y recupera su tema; al cambiar de empresa cambia el tema; la pantalla de arranque usa el de la última empresa; el cambio es pequeño y fácil de seguir.

**Non-Goals:**
- Cambiar el tema de la pantalla de arranque al elegir otra empresa en su desplegable (se sigue viendo el de la última empresa abierta).
- Cambiar cómo funciona la vista previa del combo de temas.
- Arreglar construcciones que prohíbe `AGENTS.md` fuera de las líneas tocadas (por ejemplo, el ternario de `GestorTemas.css`).

## Decisions

### D1. El tema se guarda en la empresa y se copia a `preferencias.properties`

El tema de verdad vive en la tabla `preferencias` de cada empresa (clave `tema`). Además, `preferencias.properties` guarda una copia: **el tema de la última empresa abierta**.

`GestorTemas.aplicar` **no cambia**: sigue leyendo `preferencias.properties`. Solo tenemos que mantener esa copia al día en tres momentos: al guardar el tema (D2), al conectar con una empresa (D3) y al restaurar una copia sobre la empresa activa (D4).

Alternativas descartadas:
- *Leer el tema de la base de datos en `aplicar`*: la pantalla de arranque se muestra antes de abrir ninguna base, así que habría que tratarla aparte y `GestorTemas` (vista) tendría que saber si hay empresa conectada.
- *Abrir la base de la última empresa al arrancar solo para leer el tema*: abre una conexión que después hay que cerrar y cambiar por la elegida.

### D2. Guardar el tema

En `GestorTemas.guardar(Modelo modelo)`, guardamos en los dos sitios:

```java
/** Guardamos el tema en la empresa activa y lo recordamos para el arranque. */
public static void guardar(Modelo modelo) {
    modelo.getConfiguracion().setPreferencia(PREF_TEMA, activo);
    PreferenciasGlobales.set(PREF_TEMA, activo);
}
```

`ConfiguracionController` no cambia: ya llama a `GestorTemas.guardar(modelo)`.

### D3. Al conectar con una empresa, recordamos su tema

Nuevo método en `Empresas`, llamado como **última instrucción** de `conectar`:

```java
/**
 * Copiamos el tema de la empresa activa a las preferencias globales, para
 * que las pantallas y el arranque usen el de esta empresa. Si la empresa no
 * tiene tema guardado, dejamos el valor vacío y se usa el tema por defecto.
 */
public static void recordarTema() {
    ConfiguracionDAO configuracionDAO = new ConfiguracionDAO();
    String tema = configuracionDAO.getPreferencia(PreferenciasGlobales.TEMA);
    if (tema == null) {
        tema = "";
    }
    PreferenciasGlobales.set(PreferenciasGlobales.TEMA, tema);
}
```

- Guardar `""` (y no dejar el tema anterior) hace que una empresa sin tema se vea con biblioteca8: `GestorTemas.aplicar` no reconoce `""` y usa `POR_DEFECTO`. `Empresas` está en el modelo y no puede usar `GestorTemas.POR_DEFECTO`, que es de la vista.
- `import cabofactu.modelo.negocio.sqlite.ConfiguracionDAO;` en `Empresas`.
- Como `conectar` ya lo hace, `ArranqueController`, `ConfiguracionController.cambiarEmpresa` y `CopiaSeguridadController` no cambian: navegan después de conectar y la pantalla nueva se carga con el tema de la empresa.

### D4. Restaurar sobre la empresa activa

En `CopiaSeguridad.restaurarEnEmpresaActiva`, después del `try/catch` y antes de `return rescate;`, llamar a `Empresas.recordarTema();`. Si la restauración falla, el `catch` lanza la excepción antes de llegar ahí y el tema no se toca. Restaurar como empresa nueva no necesita nada: pasa por `conectar` si el usuario cambia a ella.

### D5. Javadoc de `PreferenciasGlobales`

Cambiar «Preferencias compartidas entre empresas (tema, ultima empresa).» por «Preferencias fuera de las empresas: la ultima empresa abierta y su tema, que usa la pantalla de arranque.». El resto de la clase no cambia.

### D6. Tests

- `EmpresasTest`, nuevo `conectarRecuerdaElTemaDeCadaEmpresa`: crear «Primera» y «Segunda»; conectar con «primera» y guardar `tema = omarchy` con `new ConfiguracionDAO().setPreferencia(...)`; conectar con «segunda» → `PreferenciasGlobales.get(TEMA)` es `""`; guardar `esmeralda` en «segunda»; conectar con «primera» → `omarchy`; conectar con «segunda» → `esmeralda`.
- `CopiaSeguridadTest`, nuevo `restaurarRecuerdaElTemaDeLaCopia`: guardar `tema = sakura` en la empresa activa, crear una copia, cambiar la preferencia a `neon` y poner `neon` en `PreferenciasGlobales`, restaurar la copia → `PreferenciasGlobales.get(TEMA)` es `sakura`.
- Sin variables `var` ni lambdas nuevas en estos tests (`AGENTS.md`).

## Risks / Trade-offs

- **Dos sitios con el mismo dato** → la copia de `preferencias.properties` solo se escribe desde `GestorTemas.guardar` y `Empresas.recordarTema`, y siempre desde la empresa activa, así que no se desincronizan.
- **Vista previa sin guardar**: si el usuario elige un tema en el combo y sale sin guardar, la siguiente pantalla vuelve al tema guardado, igual que hoy.
- **Empresas ya creadas pierden el tema global actual** (empiezan con biblioteca8) → aceptado: los datos se van a reiniciar.
