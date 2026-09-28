> Todo el contenido está en `design.md`. Código según `AGENTS.md`. Colores en `base.css` solo con las variables del tema. Commits **sin líneas de coautoría**, por rutas. Antes de archivar, la auditoría de «Transición» de `AGENTS.md` sobre todo `src/main`.

## 1. Estado

- [x] 1.1 Añadir este change a «En curso» en `ESTADO.md`.

## 2. Menú principal

- [x] 2.1 `base.css` y los siete `tema-*.css` según `design.md - D2`.
- [x] 2.2 Prueba nueva en `PantallaMenuPrincipalTest` (D2). Comprobar que falla quitando la regla nueva de `base.css` y que pasa con ella. Comprobado: quitando `-fx-background-color/-insets/-border-color/-border-width` de `.opcion-menu` en `base.css` la prueba falla («Tiene fondo»); con la regla puesta, pasa.

## 3. Configuración

- [x] 3.1 `Configuracion.fxml`: barra lateral con los cuatro grupos y las ocho secciones, sus iconos y los paneles reorganizados según `design.md - D3` y `D5`, con la cabecera de cada sección (icono, título y ayuda de D4).
- [x] 3.2 `ConfiguracionController`: `fx:id`, métodos `ver…` y `mostrarSeccion` según D3; con la empresa bloqueada se abre Datos fiscales; vista previa del logotipo y `quitarLogotipo` según D3.
- [x] 3.3 Estilos de `lista-secciones`, `caja-icono-seccion`, `icono-seccion`, `cabecera-seccion`, `titulo-seccion` y `ayuda-seccion` en `base.css` según D5.
- [x] 3.4 Todos los textos de D4, incluido «Siguiente n.º (año)» en el controlador y los mensajes que nombren un campo renombrado (`errorDireccion` → «domicilio fiscal», `errorEmail` → «correo electrónico», en `Empresa.java` y `EmpresaTest.java`).
- [x] 3.5 «...» → «…» en los FXML de D4 (`Arranque.fxml`, `Clientes.fxml`, y los dos de `Configuracion.fxml`; `CopiaSeguridad.fxml` ya usaba «…»). `grep -rn '\.\.\."' src/main` no devuelve nada.
- [x] 3.6 Comprobar a 1024×768 que cada sección cabe sin desplazarse. No hizo falta tocar `prefRowCount` del pie legal ni el alto de la vista previa: las ocho secciones cupieron así. Lo que sí hizo falta fue un ajuste horizontal, no previsto en D3: en Diseño del PDF, la frase de ayuda del color de acento (en la columna 1 del `GridPane`, junto a la vista previa de 280 px) se recortaba por 10 px (`TextosCompletosTest`, 428 de 437.6). Se corrigió dándole `GridPane.columnSpan="2"` para que ocupe también la columna de las etiquetas, como hacía el texto equivalente en el `GridPane` de tres columnas de antes. Confirmado con `TextosCompletosTest` (12/12 en verde).

## 4. Pruebas

- [x] 4.1 Adaptar las pruebas a los textos nuevos (D7). `grep` en `src/test` de los textos viejos de D4: nada.
- [x] 4.2 `TextosCompletosTest` con las ocho secciones.
- [x] 4.3 Pruebas nuevas de `PantallaConfiguracionTest` (D7).

## 5. Especificación y documentación

- [x] 5.1 `openspec validate menu-y-configuracion --strict` en verde (los deltas ya están escritos).
- [x] 5.2 `docs/diseno.md`, ronda 8 (D8), y `README.md` con `configuracion.png` en la tabla de capturas.

## 6. Capturas (sesión principal, con Computer use)

- [x] 6.1 Rehacer `docs/capturas/menu.png` y añadir `docs/capturas/configuracion.png` (D8), con la demo y el tema biblioteca8.
  - Hechas por la sesión principal con Computer use (28/09): menú sin bordes y Configuración en Datos fiscales, 1010 × 761. Revisadas también Logotipo y Diseño del PDF: caben enteras.

## 7. Repaso

- [x] 7.1 El `grep` de estilo de siempre sobre los ficheros tocados y la auditoría de «Transición» sobre todo `src/main`: sin `var`, `record`, ternarios, `::` ni `.stream()` nuevos. Lo único que aparece son las excepciones ya conocidas del paquete `pdf` (`record` en `DocumentoFactura.java` y ternarios en `CabeceraPiePdf.java`, `ConstructorDocumentoFactura.java`, `DisposicionCabecera.java`, `EstiloPdf.java`, `GeneradorPdf.java`) y un `::` que es parte del texto `"jdbc:sqlite::memory:"` en `CopiaSeguridad.java`, no el operador.
- [x] 7.2 `rm -rf target` y `mvn test` completo: todo en verde. **480 pruebas, 0 fallos, 7:52 min.** Sin `ClassCastException` en la salida. Cifra actualizada de 475 a 480 en `README.md` y `docs/metodologia.md` (las cinco pruebas nuevas de las secciones 2 y 4). En el primer intento fallaron 15 pruebas de `PantallaIvaTest` y `PantallaRetencionesTest` por dos motivos no previstos en D7: `pulsar("IVA")`/`pulsar("Retenciones")` buscaban el texto viejo del botón lateral (ahora «Tipos de IVA» y «Retenciones IRPF»), y `PantallaIvaTest` buscaba la columna por su nombre viejo «Tipo» (ahora «Porcentaje», D4). Corregido en ambos ficheros y confirmado en verde antes de relanzar la batería completa.

## 8. Pruebas manuales (el alumno)

- [ ] 8.1 Menú principal en biblioteca8 y en un tema oscuro: sin bordes; al pasar el ratón, el icono crece y el nombre se resalta y se subraya. Con el tabulador pasa lo mismo.
- [ ] 8.2 Configuración: los cuatro grupos y las ocho secciones con sus iconos; la sección elegida con su raya y sin negrita; cada sección con su título y su ayuda; nada cortado.
- [ ] 8.3 Logotipo: elegir una imagen se ve en la vista previa; «Quitar logotipo» y «Guardar cambios» → el menú principal sin logo y el PDF con cabecera de texto.
- [ ] 8.4 Diseño del PDF: cambiar cabecera, pie y color se refleja en la vista previa; guardar y exportar una factura.
- [ ] 8.5 Una empresa nueva abre Configuración en Datos fiscales con el aviso nuevo.
