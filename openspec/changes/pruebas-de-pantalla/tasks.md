> Rutas relativas a `src/test/java/cabofactu/` salvo que se diga otra cosa. El código exacto y los criterios están en `design.md`. Se sigue `AGENTS.md` también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, **sin clases dentro de clases ni clases anónimas**, sin `Optional`, siempre `import` y Javadoc corto por clase. **No se toca nada de `src/main/java` ni de `src/main/resources`**, con dos excepciones ya preguntadas y decididas: la tarea 3.1a (24/09) y el renombrado de la cabecera `colBase` en `Historico.fxml` (la tarea lo explica). Si alguna otra prueba obliga a cambiar la aplicación, se para y se pregunta. **No tocar** el `.root` de los `temas/tema-*.css`.

## 1. Andamiaje (no se sigue hasta que esto esté en verde)

- [x] 1.1 En `pom.xml`, las dos dependencias de test y las seis propiedades de surefire. Ver `design.md - D1`.
- [x] 1.2 `vista/PruebasJavaFx.java`: `arrancarFx()` con `FxToolkit.registerPrimaryStage()`, fuera el `CountDownLatch`, el `try/catch` y los imports que sobren. Ver `design.md - D2`.
- [x] 1.3 Prueba de humo: una clase con **una** prueba que abra `FichaSerie.fxml`, escriba `B%`, pulse «Añadir», cierre el aviso y compruebe que `txtCodigo` tiene la clase `campo-error`.
- [x] 1.4 `mvn test`: la prueba de humo en verde, **sin que se abra ninguna ventana ni se mueva el puntero**.
- [x] 1.5 `mvn test` entero: las 309 de siempre más la de humo, todas en verde en el mismo JVM. Si alguna falla por el toolkit, aplicar el plan B de `design.md - D2` (`reuseForks=false`) y dejarlo escrito aquí.

> Si 1.4 o 1.5 no se pueden dejar en verde, **parar y avisar**: el resto del change depende de esto.

## 2. La clase base

- [x] 2.1 Crear `vista/PruebaDePantalla.java`: carpeta temporal, `CargarDemo.cargar()`, `abrirEmpresa(...)`, `prepararVista(...)`, y las ayudas `mostrarPantalla(String)` y `cerrarAviso()`. Ver `design.md - D3`.
- [x] 2.2 Pasar la prueba de humo a heredar de `PruebaDePantalla` y comprobar que sigue en verde.
- [x] 2.3 Quitar de `PruebaDePantalla` el `System.out.println("REPRO …")` de `pulsar()` y los dos métodos que solo existen para él (`ventanaDe` y `nombreVentana`), y el `import` de `Set`, que no se usa.
- [x] 2.4 Añadir `aceptarAviso()` a `PruebaDePantalla`, junto a `cerrarAviso()`, y que **ninguna prueba toque un diálogo por su cuenta**: fuera los `pulsar("Aceptar")` sueltos. Una sola forma de contestar a un aviso.

## 3. Configuración y clientes

- [x] 3.1 `vista/PantallaSeriesTest.java` con los casos de la tabla de `design.md - D5` (la prueba de humo se convierte en uno de ellos). 9 casos en verde; el duplicado comprueba que la ficha se reabre (Cancelar sale sin preguntar porque `reintentarCon` deja lo escrito como base).
- [x] 3.1a **El único arreglo en la aplicación**: `FichaSerieController.reintentarCon` y su llamada en `ConfiguracionController.abrirFichaSerie`. Ver `design.md - D9`. Arregla dos cosas: que editar una serie y fallar al guardar deje de romper el guardado, y que Cancelar vuelva a preguntar antes de tirar lo escrito.
- [x] 3.1b Rehacer dos casos de `PantallaSeriesTest` contra el comportamiento bueno: en `codigoRepetidoAvisaYSigueAbierta`, que Cancelar **sí** pregunte; y uno nuevo, `editarConCodigoRepetidoSeCorrigeYGuarda`, que edita la serie A, le pone el código `R`, cierra el aviso, lo corrige a `B` y comprueba que **se guarda** y que la tabla lo enseña.
- [x] 3.2 `vista/PantallaIvaTest.java`. 7 casos en verde: tabla, alta válida, porcentaje malo con marca y descarte, editar nombre, eliminar en uso con sus dos avisos, eliminar libre y eliminar sin selección.
- [x] 3.3 `vista/PantallaRetencionesTest.java`. 6 casos en verde: los mismos que IVA menos el sin selección, con el borrado libre creando antes su tipo.
- [x] 3.4 `vista/PantallaConfiguracionTest.java`. 4 casos en verde: datos demo, NIF malo con marca, guardado que persiste al recargar, y cambio de empresa con confirmación hasta el arranque.
- [x] 3.5 `vista/PantallaClientesTest.java`. 8 casos en verde: tabla con conteo, alta válida, NIF malo con marca y descarte, email malo, edición con doble clic, borrado con facturas que inactiva, borrado libre y borrado sin selección. Sin caso de NIF repetido: `cliente.nif` no tiene UNIQUE y la aplicación guarda duplicados sin avisar.
- [x] 3.6 `mvn test` en verde.

## 4. Arranque y menú

- [x] 4.1 `vista/PantallaArranqueTest.java`. 3 casos en verde: demo seleccionada y Entrar al menú, nombre vacío que no crea nada, y crear y eliminar empresa (al borrar la elegida se reselecciona la demo).
- [x] 4.2 `vista/PantallaMenuPrincipalTest.java`, con el caso de la empresa incompleta que bloquea y el de la empresa completa que desbloquea. 3 casos en verde: completa entra al menú, nueva cae en Configuración bloqueada con franja y Volver desactivado, y completar los datos lleva al menú con «Datos de la empresa completados.».
- [x] 4.3 `mvn test` en verde.

## 5. Facturación

- [x] 5.1 `vista/PantallaEditorTest.java`. Comportamiento visible sin editar celdas: número propuesto (calculado desde la fecha real), totales a cero, añadir línea vacía, guardar sin cliente y sin líneas, rectificar que crea y guarda, y salida con cambios. Sin número ocupado ni hueco: el número solo vale al crear (al editar se ignora) y el hueco solo sale en factura nueva, y lo uno y lo otro piden líneas con contenido, que exigen editar celdas (se reescribe en modulo-facturas).
- [x] 5.2 `vista/PantallaHistoricoTest.java`. 5 casos en verde: buscar con sus seis filas e importes, filtro por serie R, anulado que crea versión, borrado físico y doble clic que abre el editor.
- [x] 5.3 `vista/PantallaMensualesTest.java`. 4 casos en verde: lo que dice que va a generar, sin cliente, sin líneas y meses invertidos. Sin generar de verdad: pide descripción en las celdas (mismo motivo que 5.1).
- [x] 5.4 `vista/PantallaCopiasTest.java`, sin tocar el `FileChooser`. 2 casos en verde: estados iniciales de crear y de restaurar, y vuelta al menú.
- [x] 5.5 `mvn test` en verde.

## 6. Apariencia

- [x] 6.1 Crear `vista/TemasTest.java`. Ver `design.md - D6`. Por cada tema, cada variable de color de su `.root` resuelve a `Paint`; la fuente solo se comprueba textual porque en headless todo da System.
- [x] 6.2 Crear `vista/TextosCompletosTest.java`. Ver `design.md - D6`. Trece pantallas a 1024x768 sin textos cortados. Al escribirlo salió un recorte real (cabecera «Base imponible» del histórico, 80 de 86): **segunda excepción a no tocar `src/main`**, decidida por el usuario; `colBase` pasa a «Base», que es como la llama la especificación. Reparto de columnas intacto.
- [x] 6.3 Comprobar que las dos **fallan de verdad**. `TemasTest`: quitado el punto de `.root` en `tema-esmeralda.css` → rojo, y el mensaje dice qué tema es; devuelto el fichero, verde otra vez. `TextosCompletosTest`: devuelta un momento la cabecera larga del histórico → rojo, con el detalle en la causa («Textos cortados en Historico.fxml: [Base imponible (80.0 de 86.42578125)]»); repuesto «Base», verde otra vez. **Cuidado**: los dos ficheros estaban sin commitear, así que `git checkout --` los devuelve a HEAD y se lleva por delante el trabajo del change; hay que reponerlo a mano.

## 7. Normas y estado

- [x] 7.1 `AGENTS.md`, apartado «Tests»: las tres capas, las dos pruebas de apariencia, el arranque único desde `PruebasJavaFx` y lo que se queda a mano. Ver `design.md - D8`.
- [x] 7.2 Añadir este change a «En curso» en `ESTADO.md`.

## 8. Repaso final

- [x] 8.1 `grep -rnE "\bvar \b|\.stream\(\)|::|record |\? .*: " src/test/java/cabofactu/vista/`: sin resultados.
- [x] 8.2 `git status`: de `src/main` solo los **tres** ficheros de las dos excepciones acordadas: `FichaSerieController` y `ConfiguracionController` (3.1a) y `Historico.fxml` (6.2, una palabra). Nada más.
- [x] 8.3 Con la aplicación cerrada, borrado `target` y `mvn test`: **381 pruebas, 0 fallos, 4:21 min**, y ningún `ClassCastException` en el log.
- [x] 8.4 `openspec validate pruebas-de-pantalla --strict`: válido.

## 9. Pruebas manuales

> Ahora son cinco, no dieciocho: lo demás lo comprueba `mvn test`.

- [x] 9.1 Arrancar la aplicación y usarla un minuto: sigue igual que antes.
- [x] 9.2 Copia de seguridad: crear una y restaurarla (el diálogo de archivos de Windows no lo puede probar ningún robot).
- [x] 9.3 Exportar una factura a PDF y mirarla.
- [x] 9.4 Elegir un logo de empresa desde el diálogo de archivos.
- [x] 9.5 Pasar por los siete temas y ver que siguen bien.
