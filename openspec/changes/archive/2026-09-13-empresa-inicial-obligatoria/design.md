## Context

**Arranque actual.** `Main.start` → `prepararDatos()` crea la raíz de datos, llama a `Database.migrarInstalacionUnArchivo()` (mueve un `facturas.db` suelto a la carpeta `comercial_alcazaba` y lo registra como «Comercial Alcazaba») y, si `EmpresaManager.listarEmpresas()` está vacío, hace `EmpresaManager.crearEmpresa("Comercial Alcazaba")`. Después muestra `Arranque.fxml`. Al pulsar Entrar, `ArranqueController.entrar` conecta la empresa y `Main.entrarEnMenu` construye `Servicios` y muestra `MenuPrincipal.fxml`.

**Empresa nueva.** `EmpresaManager.crearEmpresa(nombre)` crea la base con las migraciones y guarda el nombre en `empresas.properties`; la fila `empresa` de la base queda con los campos vacíos. El nombre del catálogo no se copia a la base.

**Otras entradas a una empresa.** `ConfiguracionController.cambiarEmpresa` (tras conectar, `nav.mostrar(MenuPrincipal)`, línea ~938) y `BackupController` al restaurar (líneas ~282 y ~295).

**Guardado.** `ConfiguracionController.guardar` recoge los campos y llama a `servicios.config.saveEmpresa` sin validar nada.

**Validadores existentes en `util/`.** `DocumentoFiscalValidator.esValido` y `EmailValidator.esValido` dan por válido el vacío; `CodigoPostalValidator.esValido` exige cinco dígitos con provincia 01-52.

**Demostración.** `seed_demo.sql` pone NIF `B99999999` y CP `99999`, que **no** superan los validadores (el dígito de control de `B9999999` es 7 y la provincia 99 no existe).

**Pantalla Configuración.** La sección Empresa (`seccionEmpresa` en `Configuracion.fxml`) es una `card` con un `GridPane` de seis filas y sitio libre debajo. La barra inferior tiene «Guardar configuración» y «Volver» (sin `fx:id`). La barra de navegación la crea `BarraNavegacion.crear(nav, "configuracion")`.

## Goals / Non-Goals

**Goals:** que ninguna instalación nazca con «Comercial Alcazaba» y que una instalación nueva traiga la empresa de demostración para probar; que no se pueda trabajar con una empresa sin datos de emisor completos; que el usuario entienda qué tiene que hacer.

**Non-Goals:** cambiar el nombre del paquete `com.alcazaba`; copiar datos del catálogo a la base en `EmpresaManager` (tiene SQL propio y se reorganiza en otro change); validar el formato del teléfono; exigir logo, actividad o pie legal; bloquear el resto de secciones de Configuración.

## Decisions

### D1. Sin «Comercial Alcazaba» y sin migración antigua

`Main.prepararDatos` queda en crear la raíz de datos (`Files.createDirectories(Database.baseDataDir())`). Se borran de `Database` la constante `SLUG_EMPRESA_INICIAL` y el método `migrarInstalacionUnArchivo`, y de `DatabaseTest` los dos tests que los usan. `EmpresaManager.registrarNombre` se mantiene porque lo usa `CargarDemo`. `cmbEmpresaVacia` se conserva para D1b.

### D1b. Instalación nueva: se carga la demostración

La empresa de prueba es la de `CargarDemo` + `seed_demo.sql` («Empresa Demo S.L.», slug `demo`): clientes, series y facturas que cubren IVA múltiple, descuento, retención, suplido, anulada y rectificativa. Los tests de servicio usan bases temporales propias y no sirven como empresa instalable.

`CargarDemo` hoy solo se ejecuta como programa (`main`). Se separa:

```java
public static final String SLUG = "demo";

public static EmpresaManager.EmpresaInfo cargar() throws Exception
```

`cargar()` hace lo que hoy hace `main` a partir de `crearEmpresa("Demo")`: crear la empresa, registrar el nombre «Empresa Demo S.L.», ejecutar `seed_demo.sql` y dejar la conexión cerrada (`Database.resetConnection()`), y devuelve el `EmpresaInfo`. **No** borra nada. `main` conserva el borrado previo, llama a `cargar()` y sigue imprimiendo el recuento de facturas como ahora.

En `Main.start`, **después** de `adquirirLock()` (para no escribir datos si hay otra instancia abierta) y antes de `mostrarArranque()`:

```java
try {
    if (cmbEmpresaVacia()) {
        CargarDemo.cargar();
        PreferenciasGlobales.set(PreferenciasGlobales.ULTIMA_EMPRESA, CargarDemo.SLUG);
    }
} catch (Exception e) {
    Dialogos.error("Facturación", "No se pudo cargar la empresa de demostración:\n" + e.getMessage());
}
```

Si falla, la aplicación sigue hasta el arranque con la lista vacía (D2). Solo ocurre cuando no hay **ninguna** empresa: nunca pisa datos existentes.

Descartado: cargarla en cada arranque si falta. Si el usuario borra la demostración porque ya tiene su empresa, no debe reaparecer.

### D2. Texto de ayuda en el arranque

En `Arranque.fxml` se añade, dentro de la `card` y encima del rótulo «Empresa», un `Label fx:id="lblAyudaEmpresa"` con `wrapText="true"`, `maxWidth="400.0"`, `visible="false"` y `managed="false"`.

`ArranqueController.cargarEmpresas` lo muestra (visible y managed) con uno de estos textos y lo oculta en cualquier otro caso:

| Situación | Texto |
|---|---|
| Lista vacía | Crea tu empresa con «Nueva…» para empezar. |
| La única empresa es `CargarDemo.SLUG` | Empresa de demostración con datos ficticios. Crea la tuya con «Nueva…». |

La ventana de arranque es fija (760×520) y el `VBox` encoge las etiquetas hasta cortarlas con «…»: con los textos largos de la primera versión solo cabían dos de las tres líneas. Por eso el texto fijo es corto (una o dos líneas) y la etiqueta lleva `minHeight="-Infinity"` (`USE_PREF_SIZE`), para que nunca se recorte. La explicación completa va en un aviso emergente (D2b).

El botón Entrar ya se desactiva con la lista vacía; no cambia.

### D2b. Aviso emergente con la explicación completa

`ArranqueController` gana:

```java
public void mostrarAvisoInicial(boolean demoRecienCargada)
```

Muestra un `Dialogos.info` con título «Bienvenido» y uno de estos textos; si no se cumple ninguna condición, no muestra nada:

| Condición | Texto |
|---|---|
| `demoRecienCargada` | Se ha cargado una empresa de demostración con datos ficticios para que puedas probar el programa.\n\nCuando quieras trabajar con tu empresa, créala con «Nueva…». Al entrar en ella tendrás que completar sus datos fiscales y de contacto en Configuración. La empresa de demostración se puede eliminar después desde Configuración > Empresas. |
| Lista de empresas vacía | Para iniciar el programa crea tu empresa con «Nueva…».\n\nAl entrar en ella tendrás que completar sus datos fiscales y de contacto en Configuración. |

El aviso sale **solo en el arranque en que se carga la demostración** o cuando no hay ninguna empresa, no en cada arranque con la demostración: repetirlo cada vez molestaría y el texto fijo ya lo recuerda.

En `Main`, `mostrarArranque` guarda el `ArranqueController` y, tras `stage.show()` en `start`, se llama con `Platform.runLater(() -> arranque.mostrarAvisoInicial(demoCargada))`, donde `demoCargada` es `true` si en este arranque se ejecutó `CargarDemo.cargar()` sin error. El `runLater` hace que el aviso aparezca encima de la ventana ya visible y no antes de ella.

### D3. Qué datos son obligatorios y dónde se comprueba

En `ConfigService`:

```java
public List<String> datosPendientes(Empresa e)
public List<String> datosPendientes()
public boolean empresaCompleta()
```

`datosPendientes(Empresa)` devuelve, en este orden, la etiqueta de cada dato que falte o no sea válido:

| Etiqueta | Regla |
|---|---|
| `Nombre / razón social` | no vacío |
| `NIF` | no vacío y `DocumentoFiscalValidator.esValido` |
| `Dirección` | no vacío |
| `CP` | `CodigoPostalValidator.esValido` |
| `Localidad` | no vacío |
| `Provincia` | no vacío |
| `Email` | no vacío y `EmailValidator.esValido` |
| `Teléfono` | no vacío |

«No vacío» es tras `trim()`, con `null` contado como vacío. `datosPendientes()` aplica lo mismo a `getEmpresa()`, y `empresaCompleta()` es `datosPendientes().isEmpty()`. Sin `throws SQLException` (el change `excepcion-de-datos` ya lo quitó de `ConfigService`).

La regla va en el servicio y no en el controlador: la usan a la vez la navegación, el guardado y los tests, y es la base de lo que pedirá VeriFactu.

### D4. Un único punto de entrada: `Navegador.mostrarInicio()`

```java
public void mostrarInicio() {
    if (servicios.config.empresaCompleta()) {
        mostrar("/com/alcazaba/facturacion/ui/MenuPrincipal.fxml");
    } else {
        mostrar("/com/alcazaba/facturacion/ui/Configuracion.fxml");
    }
}
```

Lo usan los sitios donde se **entra** en una empresa: `Main.entrarEnMenu` (en lugar de `nav.mostrar(MenuPrincipal)`), `ConfiguracionController.cambiarEmpresa` y las dos ramas de restauración de `BackupController` que hoy van al Menú. Las demás llamadas a `MenuPrincipal.fxml` (botones Volver, barra de navegación) no se tocan: solo se llega a ellas con la empresa ya completa.

Si `empresaCompleta()` lanza una excepción, se deja propagar: los llamantes ya muestran el error.

### D5. Configuración en modo «datos pendientes»

`ConfiguracionController.alIniciar`, después de cargar la empresa, calcula `boolean pendiente = !servicios.config.datosPendientes(empresa).isEmpty()` y lo guarda en un campo. Si es `true`:

1. Si `txtNombre` está vacío, lo rellena con el nombre visible de la empresa activa (el `EmpresaInfo` de `EmpresaManager.listarEmpresas()` cuyo `slug` sea `Sesion.empresaSlug()`). No se guarda hasta que el usuario pulse Guardar.
2. Muestra `lblDatosPendientes` (ver D6).
3. Selecciona la sección Empresa en la lista lateral (hoy la selección inicial es `select(1)`; en este modo debe quedar la entrada de Empresa, que es la misma si la lista no ha cambiado; comprobarlo).
4. Llama a `BarraNavegacion.bloquearSalvoSalir(barra)` sobre la barra creada.
5. Desactiva `btnVolver`.

Las demás secciones (Cabecera y pie, PDF y apariencia, IVA, Retenciones, Series, Empresas) siguen accesibles.

### D6. Texto explicativo y asteriscos

En `Configuracion.fxml`, dentro de `seccionEmpresa` y **antes** del `GridPane`, un `Label fx:id="lblDatosPendientes"` con `wrapText="true"`, `visible="false"`, `managed="false"` y el texto:

> Para empezar a usar el programa tienes que completar los datos de tu empresa. Rellena los campos marcados con * y pulsa «Guardar configuración».

Va dentro de la sección Empresa y no en la cabecera de la pantalla para no restar alto al resto de secciones (escenario «Cada sección cabe sin scroll»). La sección Empresa tiene sitio libre bajo la rejilla.

Los rótulos de los ocho campos obligatorios pasan a llevar ` *`: `Nombre / razón social *`, `NIF *`, `Dirección *`, `CP *`, `Localidad *`, `Provincia *`, `Email *`, `Teléfono *`. `Actividad` no lo lleva. El test `ConfiguracionLayoutTest` («Campos de empresa legibles a 1024×768») debe seguir en verde.

El botón «Volver» recibe `fx:id="btnVolver"`.

### D7. Guardar exige los datos y cierra el modo

`ConfiguracionController.guardar`, tras `recogerEmpresa()` y **antes** de `saveEmpresa`:

```java
List<String> faltan = servicios.config.datosPendientes(empresa);
if (!faltan.isEmpty()) {
    Dialogos.error("Configuración", "Faltan datos obligatorios o no son válidos:\n" + String.join(", ", faltan));
    return;
}
```

Aplica siempre, no solo en modo pendiente: si no, se podría vaciar el NIF de una empresa completa. Si el guardado termina bien y `pendiente` era `true`, en lugar del aviso «Configuración guardada.» se muestra «Datos de la empresa completados.» y se navega con `nav.mostrar(MenuPrincipal)`.

Consecuencia aceptada: tampoco se puede guardar el tema, el color del PDF o la carpeta automática mientras falten datos de empresa, porque comparten botón.

### D8. `BarraNavegacion.bloquearSalvoSalir`

```java
public static void bloquearSalvoSalir(HBox barra)
```

Desactiva todos los `Button` de la barra excepto el de texto «Salir». Se añade a `BarraNavegacion` para no repetir en el controlador el conocimiento de cómo está hecha la barra.

### D9. Demostración válida

En `seed_demo.sql`, la empresa de demostración pasa a `nif = 'B99999997'` (CIF con dígito de control correcto) y `cp = '52999'` (provincia 52, código que no corresponde a ninguna calle real). El resto de datos ya están rellenos. Con esto la empresa de demostración entra directamente al Menú.

Si algún test comprueba literalmente `B99999999` o `99999`, se actualiza al valor nuevo.

## Risks / Trade-offs

- **Cambio de comportamiento en empresas existentes incompletas:** al entrar irán a Configuración. Es lo pedido; se refleja en la spec.
- **Tests de interfaz:** los que abren Menú o Configuración con una base vacía construyen `Navegador` y llaman a `mostrar(...)` directamente, sin pasar por `mostrarInicio`, así que no deberían cambiar. `ConfiguracionLayoutTest` abre Configuración con empresa vacía: entrará en modo pendiente (texto visible, barra bloqueada). Si eso rompe alguna medida, rellenar la empresa en la preparación del test, sin cambiar lo que mide.
- **Descuido posible:** dejar alguna entrada a empresa usando `mostrar(MenuPrincipal)` en vez de `mostrarInicio`. La tarea 6.1 las busca.
- **Descuido posible:** bloquear también el botón Salir, dejando al usuario sin salida. La tarea 7.3 lo comprueba.
