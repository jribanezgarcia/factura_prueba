## Situación de partida

| Pieza | Hoy |
|---|---|
| 35 clases de test, 309 pruebas | Negocio, clases de datos, PDF, utilidades y ficheros, contra una base temporal |
| `CargaPantallasTest` (13 pruebas) | Carga cada FXML y comprueba el cableado `@FXML`. No pulsa nada |
| `PruebasJavaFx` | Arranca el toolkit con `Platform.startup` y prepara la `Vista` |
| Pantallas | 14 FXML sin ninguna prueba de comportamiento |
| Temas | 7 `tema-*.css` y `base.css` sin ninguna prueba |

## Objetivos y lo que queda fuera

**Objetivo**: que `mvn test` compruebe lo que hoy comprueba una persona pulsando botones, y que la lista de pruebas manuales de cada change baje de dieciocho comprobaciones a cuatro o cinco.

**Fuera**: los diálogos nativos de Windows, el aspecto del PDF, el gusto por cómo queda una pantalla, JaCoCo y las pruebas de mutación. Y **no se toca `src/main`**: esto añade pruebas, no cambia la aplicación.

---

## D1. Las dependencias y el modo headless

En `pom.xml`, con ámbito `test`:

```xml
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.18</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>openjfx-monocle</artifactId>
    <version>21.0.2</version>
    <scope>test</scope>
</dependency>
```

Y en el `maven-surefire-plugin`, las seis propiedades que encienden Monocle:

```xml
<configuration>
    <systemPropertyVariables>
        <testfx.robot>glass</testfx.robot>
        <testfx.headless>true</testfx.headless>
        <glass.platform>Monocle</glass.platform>
        <monocle.platform>Headless</monocle.platform>
        <prism.order>sw</prism.order>
        <java.awt.headless>true</java.awt.headless>
    </systemPropertyVariables>
</configuration>
```

Monocle es el que dibuja «en el aire»: las pruebas no abren ventanas en el escritorio ni mueven el puntero. `openjfx-monocle:21.0.2` es la versión que corresponde a JavaFX 21. No hacen falta `--add-exports` ni `--add-opens`, porque el proyecto no tiene `module-info.java` y JavaFX va por el classpath.

**Esto está probado en la máquina del proyecto**: dos pruebas de pantalla sobre `FichaSerie.fxml` pasaron en 4,4 s, sin ventanas, incluida una que cierra un aviso modal.

---

## D2. Una sola forma de arrancar JavaFX en los tests

Es el punto delicado. Hoy `PruebasJavaFx.arrancarFx()` arranca el toolkit con `Platform.startup`, y TestFX lo arranca con `FxToolkit`. Los dos en el mismo JVM se pisan: **probado**, con las dos formas mezcladas la prueba del aviso modal falla («no se encontró el botón del aviso»), porque el robot deja de ver las ventanas que no ha abierto él.

`PruebasJavaFx.arrancarFx()` pasa a ser:

```java
public static synchronized void arrancarFx() throws Exception {
    if (arrancado) {
        return;
    }
    FxToolkit.registerPrimaryStage();
    Platform.setImplicitExit(false);
    arrancado = true;
}
```

Con eso desaparecen el `CountDownLatch`, el `try/catch` de `IllegalStateException` y los imports que ya no se usan. `CargaPantallasTest` no cambia ni una línea y la batería entera queda **en verde con las dos clases de pruebas conviviendo en el mismo JVM** (también probado).

Si aun así apareciera algún choque entre clases de test, el plan B es `<reuseForks>false</reuseForks>` en surefire: cada clase en su propio JVM. Es más lento, así que solo se usa si hace falta.

---

## D3. `PruebaDePantalla`, la clase base

`src/test/java/cabofactu/vista/PruebaDePantalla.java`, nueva. Prepara lo mismo que el arranque real de la aplicación, para que cada prueba empiece en una empresa de demostración recién hecha:

- Carpeta temporal con `Conexion.setCarpetaRaiz(...)`, para no tocar `%APPDATA%\Facturacion`.
- `CargarDemo.cargar()` y después `abrirEmpresa(CargarDemo.CARPETA, fecha)`, que es lo que hace el arranque cuando el usuario entra en una empresa.
- `PruebasJavaFx.prepararVista(new Modelo(), ventana)` para dejar la `Vista` con su `Controlador`.

Extiende `ApplicationTest` de TestFX y ofrece dos ayudas a las pruebas que hereden de ella:

- `mostrarPantalla(String fxml)`: llama a `Vista.getInstancia().mostrar(fxml)` en el hilo de JavaFX y espera a que termine.
- `cerrarAviso()`: busca el botón del `Alert` que esté abierto y lo pulsa, para las pruebas que provocan un aviso. Así el patrón del aviso se escribe **una vez**, no en cada prueba.

Los datos de la demostración son los que ya usa la aplicación (`seed_demo.sql`): dos clientes, las series A y R, facturas 1 a 5 de A con la 5 anulada, y la rectificativa R-1.

---

## D4. Cómo se escribe una prueba de pantalla

Este es el patrón, tal como quedó en la prueba de humo. Se busca por el `fx:id` del FXML, se actúa y se comprueba lo que ve el usuario:

```java
@Test
void elCodigoMaloAvisaYSeMarcaEnRojo() {
    clickOn("#txtCodigo");
    write("B%");
    clickOn("Añadir");
    cerrarAviso();
    TextField codigo = lookup("#txtCodigo").queryAs(TextField.class);
    assertTrue(codigo.getStyleClass().contains("campo-error"));
}
```

Reglas para que estas pruebas no se pudran:

- Se comprueba **lo que ve el usuario**: el texto de una etiqueta, el contenido de una tabla, si un botón está desactivado. Nunca los campos privados del controlador ni las clases de dentro.
- Se busca por `fx:id` o por el texto del botón, nunca por posiciones ni coordenadas.
- Cada prueba deja la empresa como se la encontró, porque cada clase arranca con su carpeta temporal.
- Nada de esperas por tiempo: si hace falta esperar, se espera a una condición.

Esto importa especialmente en el editor, el histórico y las mensuales, que se reescriben enteros en `modulo-facturas`: escritas así, estas pruebas **sobreviven al rewrite y demuestran que no cambió el comportamiento**.

---

## D5. Qué se cubre

Una clase por pantalla, en `src/test/java/cabofactu/vista/`. El nombre lleva delante `Pantalla` para que no choque con las clases de negocio que ya se llaman igual (`ClientesTest`, `FacturasTest`…).

| Clase | Qué comprueba |
|---|---|
| `PantallaArranqueTest` | La lista de empresas; entrar en una abre la ventana principal; crear una empresa sin nombre avisa; la fecha de trabajo elegida es la que se usa después |
| `PantallaMenuPrincipalTest` | Salen el nombre y el NIF de la empresa; con la empresa incompleta solo se puede ir a Configuración y Salir, con su aviso; al completarla se habilita el resto; los atajos llevan a su pantalla |
| `PantallaClientesTest` | La tabla lista los clientes de la demo; Nuevo guarda y aparece en la tabla; un NIF inválido avisa y **todos** los campos malos quedan en rojo a la vez; doble clic edita; cerrar con cambios pregunta; el buscador filtra |
| `PantallaConfiguracionTest` | Guardar los datos de la empresa; los obligatorios vacíos avisan; cambiar de tema lo aplica; la navegación entre secciones |
| `PantallaSeriesTest` | Las columnas y el «Siguiente (año)» calculado; la ficha con los tres formatos y el ejemplo que cambia al escribir el código, al cambiar el formato y al marcar «rectificativa»; código con símbolos, código repetido y dos series sin código; doble clic edita; Cancelar con cambios pregunta; eliminar una serie con facturas avisa |
| `PantallaIvaTest` | Alta, edición y eliminación; el porcentaje bloqueado cuando el tipo está en uso, con su aviso |
| `PantallaRetencionesTest` | Alta, edición y eliminación |
| `PantallaEditorTest` | El número propuesto es el siguiente de la serie; añadir líneas y ver los totales; guardar y que salga en el histórico; un número ocupado escrito a mano no deja guardar; con un hueco, pregunta; salir con cambios ofrece las tres opciones |
| `PantallaHistoricoTest` | Lista las facturas de la demo; filtra por serie y por estado; anular pide confirmación y cambia el estado; borrar pide confirmación |
| `PantallaMensualesTest` | Genera varias facturas con números seguidos; pregunta si rellenar los huecos |
| `PantallaCopiasTest` | La pantalla lista las copias y los botones responden. **No se toca el `FileChooser`** |

---

## D6. Las dos pruebas de apariencia

No necesitan TestFX: van sobre el andamiaje que ya existe.

`TemasTest`: por cada `temas/tema-*.css`, lo aplica a una escena y comprueba que el fondo del `.root` resuelve a un `Paint` y que las variables de la paleta están definidas. Es el fallo del `.root` borrado, que apareció como `ClassCastException ... cannot be cast to Paint`.

`TextosCompletosTest`: carga cada FXML, lo maqueta al tamaño mínimo de ventana (1024x768) y recorre todos los `Labeled`; ninguno puede tener `getWidth()` por debajo de su `prefWidth(-1)`. Es el fallo del botón «Completar datos», que se midió a mano: 80 px de ancho para 113 que pedía el texto.

---

## D7. Lo que se queda manual, y por qué

| Se queda manual | Motivo |
|---|---|
| Crear y restaurar copias de seguridad | Abre el `FileChooser` de Windows, que no es una ventana de JavaFX |
| Exportar el PDF y mirarlo | Igual, y además el aspecto del documento no se puede afirmar con un `assert` |
| Elegir el logo de la empresa | `FileChooser` |
| Mirar los siete temas | Que la paleta esté definida lo comprueba `TemasTest`; que quede bonita, no |
| Arrancar la aplicación una vez | Ninguna prueba sustituye a abrirla y usarla |

---

## D8. `AGENTS.md`

El apartado «Tests» pasa de «un único test de pantallas» a las tres capas, y añade la norma del arranque único:

- Pruebas de negocio y de clases de datos, contra una base temporal.
- Un test que carga cada FXML y comprueba el cableado.
- Una prueba de pantalla por pantalla, con TestFX en modo headless, que comprueba **lo que ve el usuario** y nunca los campos de dentro del controlador.
- JavaFX se arranca en los tests **solo** desde `PruebasJavaFx`.

## Decisiones

| Decisión | Por qué |
|---|---|
| Todas las pantallas en este change, editor e histórico incluidos | Decisión del usuario. Escritas contra el comportamiento visible, sirven de red para el rewrite de `modulo-facturas` en vez de tirarse con él |
| Un solo change, no dos | Decisión del usuario, sabiendo que es el change más grande del proyecto |
| Las dos pruebas de apariencia van dentro | Son diez líneas cada una y cubren los dos únicos fallos graves que ha tenido el proyecto |
| Capacidad nueva `testing`, sin tocar `invoicing` | Este change no cambia el comportamiento de la aplicación; meterlo en `invoicing` ensuciaría la especificación del producto |
| TestFX y no un robot propio | Es lo que usa todo el mundo en JavaFX, y es lo único que sabe cerrar un `Alert` modal abierto con `showAndWait` |

## Riesgos y renuncias

- **La batería tarda más**: las pruebas de pantalla son las más lentas del proyecto (unos 3 s por clase). Con once clases, `mvn test` crecerá alrededor de medio minuto.
- **Una prueba de pantalla es más frágil que una de negocio**: si cambia un `fx:id` o el texto de un botón, hay que tocarla. Es el precio de no pulsarlo a mano.
- **No sustituyen a mirar la aplicación**: comprueban que el comportamiento es el correcto, no que la pantalla quede bien.
- **Si una prueba obliga a cambiar `src/main`**, se para y se pregunta: este change no toca la aplicación.
