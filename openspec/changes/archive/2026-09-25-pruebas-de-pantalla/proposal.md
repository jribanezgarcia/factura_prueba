## Why

Cada módulo termina con una lista de comprobaciones que tiene que pasar una persona a mano, una por una: 18 en `modulo-series`. Y el módulo que viene, `modulo-facturas`, es el más grande del proyecto.

Los 309 tests de hoy cubren el negocio y las clases de datos. De las pantallas solo se comprueba que el FXML carga sin errores de cableado (`CargaPantallasTest`): **nadie pulsa un botón, nadie escribe en un campo y nadie contesta a un aviso**.

Los dos fallos más graves que ha tenido el proyecto se escaparon justo por ahí:

| Cuándo | Qué pasó | Por qué no lo vio nadie |
|---|---|---|
| 21/09 | Los siete `temas/tema-*.css` se quedaron sin el punto de `.root` y la paleta entera murió | Ninguna prueba mira los temas |
| 22/09 | El botón «Completar datos» salía con el texto cortado | Ninguna prueba mide los controles |

Antes de escribir esto se ha comprobado que la solución funciona en esta máquina:

| Duda | Resultado medido |
|---|---|
| ¿TestFX headless con JavaFX 21 en Windows? | Sí: `testfx-junit5:4.0.18` con `openjfx-monocle:21.0.2` |
| ¿Abre ventanas o coge el ratón? | No: nada en pantalla, nada que se mueva |
| ¿Puede cerrar un aviso modal (`showAndWait`)? | Sí: escribe `B%`, pulsa «Añadir», cierra el aviso y comprueba que el campo queda en rojo |
| ¿Convive con los 309 de hoy? | Sí, **arrancando el toolkit desde un solo sitio**. Con dos formas de arrancarlo, la prueba del aviso falla |

## What Changes

- **Tres capas de prueba**, las dos de hoy más una nueva: negocio y clases de datos, carga de pantallas y **pruebas de pantalla**, que manejan los controles como lo haría el usuario.
- **TestFX en modo headless**: `mvn test` sigue siendo una orden que se puede lanzar mientras se trabaja, sin ventanas y sin que el puntero se mueva.
- **Una sola forma de arrancar JavaFX en los tests**: `PruebasJavaFx` pasa a usar `FxToolkit`, y sigue siendo el único sitio donde se arranca el toolkit.
- **Una clase de prueba por pantalla**, apoyadas en una clase base que prepara lo mismo que el arranque real: carpeta temporal, datos de demostración y `Vista` con su `Controlador`.
- **Dos pruebas de apariencia**: una comprueba que cada tema define su paleta y otra que ningún texto se recorta al tamaño mínimo de ventana. Son los dos fallos de la tabla de arriba.
- **La lista manual de cada change se queda en lo que ninguna máquina puede ver**: los diálogos nativos de Windows, el PDF impreso y el gusto por cómo queda la pantalla.

## Capacidades

### Capacidades nuevas

- `testing`: «Pruebas automáticas» y «Apariencia comprobada automáticamente».

### Capacidades modificadas

Ninguna. Este change **no cambia el comportamiento de la aplicación**: solo añade pruebas.

## A qué afecta

- **Nuevo**: `src/test/java/cabofactu/vista/PruebaDePantalla.java` (clase base) y once clases de prueba de pantalla, más `TemasTest` y `TextosCompletosTest`.
- **Cambia**: `pom.xml` (dos dependencias de test y la configuración headless de surefire), `src/test/java/cabofactu/vista/PruebasJavaFx.java` (arranca con `FxToolkit`) y `AGENTS.md` (el apartado «Tests»).
- **Un arreglo en `src/main`, y solo uno**: `FichaSerieController.reintentarCon` y su llamada en `ConfiguracionController`. Al escribir las pruebas de Series salió que, editando una serie, un fallo al guardar dejaba la ficha sin poder guardar nunca más («No se pudo guardar: null», en bucle), y que Cancelar tiraba lo escrito sin preguntar. Está en `design.md - D9`. Si cualquier otra prueba obliga a cambiar la aplicación, se para y se pregunta.
- **Un renombrado en `src/main/resources`, y solo ese**: la cabecera `colBase` de `Historico.fxml` pasa de «Base imponible» a «Base». Al escribir `TextosCompletosTest` salió que el texto pedía 86 px y la columna daba 80; con diez columnas que suman 1110 px, ensanchar aprieta al resto, y la especificación ya llama «base» a esa columna. Segunda excepción decidida con el usuario.
- **Queda fuera**: JaCoCo y las pruebas de mutación, que miden pero no prueban; los diálogos nativos de Windows (`FileChooser` y `DirectoryChooser`), que ningún robot de JavaFX puede manejar; y el aspecto del PDF.
