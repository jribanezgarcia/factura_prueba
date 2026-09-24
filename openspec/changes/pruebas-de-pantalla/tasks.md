> Rutas relativas a `src/test/java/cabofactu/` salvo que se diga otra cosa. El código exacto y los criterios están en `design.md`. Se sigue `AGENTS.md` también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, **sin clases dentro de clases ni clases anónimas**, sin `Optional`, siempre `import` y Javadoc corto por clase. **No se toca nada de `src/main/java` ni de `src/main/resources`**, con una única excepción ya preguntada y decidida el 24/09: la tarea 3.1a. Si alguna otra prueba obliga a cambiar la aplicación, se para y se pregunta. **No tocar** el `.root` de los `temas/tema-*.css`.

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
- [ ] 2.3 Quitar de `PruebaDePantalla` el `System.out.println("REPRO …")` de `pulsar()` y los dos métodos que solo existen para él (`ventanaDe` y `nombreVentana`), y el `import` de `Set`, que no se usa.
- [ ] 2.4 Añadir `aceptarAviso()` a `PruebaDePantalla`, junto a `cerrarAviso()`, y que **ninguna prueba toque un diálogo por su cuenta**: fuera los `pulsar("Aceptar")` sueltos. Una sola forma de contestar a un aviso.

## 3. Configuración y clientes

- [x] 3.1 `vista/PantallaSeriesTest.java` con los casos de la tabla de `design.md - D5` (la prueba de humo se convierte en uno de ellos). 9 casos en verde; el duplicado comprueba que la ficha se reabre (Cancelar sale sin preguntar porque `reintentarCon` deja lo escrito como base).
- [ ] 3.1a **El único arreglo en la aplicación**: `FichaSerieController.reintentarCon` y su llamada en `ConfiguracionController.abrirFichaSerie`. Ver `design.md - D9`. Arregla dos cosas: que editar una serie y fallar al guardar deje de romper el guardado, y que Cancelar vuelva a preguntar antes de tirar lo escrito.
- [ ] 3.1b Rehacer dos casos de `PantallaSeriesTest` contra el comportamiento bueno: en `codigoRepetidoAvisaYSigueAbierta`, que Cancelar **sí** pregunte; y uno nuevo, `editarConCodigoRepetidoSeCorrigeYGuarda`, que edita la serie A, le pone el código `R`, cierra el aviso, lo corrige a `B` y comprueba que **se guarda** y que la tabla lo enseña.
- [ ] 3.2 `vista/PantallaIvaTest.java`.
- [ ] 3.3 `vista/PantallaRetencionesTest.java`.
- [ ] 3.4 `vista/PantallaConfiguracionTest.java`.
- [ ] 3.5 `vista/PantallaClientesTest.java`.
- [ ] 3.6 `mvn test` en verde.

## 4. Arranque y menú

- [ ] 4.1 `vista/PantallaArranqueTest.java`.
- [ ] 4.2 `vista/PantallaMenuPrincipalTest.java`, con el caso de la empresa incompleta que bloquea y el de la empresa completa que desbloquea.
- [ ] 4.3 `mvn test` en verde.

## 5. Facturación

- [ ] 5.1 `vista/PantallaEditorTest.java`. Solo comportamiento visible: número propuesto, totales, guardar, número ocupado, hueco y salida con cambios.
- [ ] 5.2 `vista/PantallaHistoricoTest.java`.
- [ ] 5.3 `vista/PantallaMensualesTest.java`.
- [ ] 5.4 `vista/PantallaCopiasTest.java`, sin tocar el `FileChooser`.
- [ ] 5.5 `mvn test` en verde.

## 6. Apariencia

- [ ] 6.1 Crear `vista/TemasTest.java`. Ver `design.md - D6`.
- [ ] 6.2 Crear `vista/TextosCompletosTest.java`. Ver `design.md - D6`.
- [ ] 6.3 Comprobar que las dos **fallan de verdad**: quitar temporalmente el punto de un `.root`, ver `TemasTest` en rojo y devolverlo; estrechar temporalmente un botón, ver `TextosCompletosTest` en rojo y devolverlo. Dejar escrito aquí qué salió.

## 7. Normas y estado

- [ ] 7.1 `AGENTS.md`, apartado «Tests»: las tres capas y el arranque único. Ver `design.md - D8`.
- [x] 7.2 Añadir este change a «En curso» en `ESTADO.md`.

## 8. Repaso final

- [ ] 8.1 `grep -rnE "\bvar \b|\.stream\(\)|::|record |\? .*: " src/test/java/cabofactu/vista/`: sin resultados nuevos.
- [ ] 8.2 `git status`: **ni un fichero de `src/main` tocado**.
- [ ] 8.3 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde y ningún `ClassCastException` en el log. Apuntar aquí cuántas pruebas son y cuánto tarda.
- [ ] 8.4 `openspec validate pruebas-de-pantalla --strict` sin errores.

## 9. Pruebas manuales

> Ahora son cinco, no dieciocho: lo demás lo comprueba `mvn test`.

- [ ] 9.1 Arrancar la aplicación y usarla un minuto: sigue igual que antes.
- [ ] 9.2 Copia de seguridad: crear una y restaurarla (el diálogo de archivos de Windows no lo puede probar ningún robot).
- [ ] 9.3 Exportar una factura a PDF y mirarla.
- [ ] 9.4 Elegir un logo de empresa desde el diálogo de archivos.
- [ ] 9.5 Pasar por los siete temas y ver que siguen bien.
