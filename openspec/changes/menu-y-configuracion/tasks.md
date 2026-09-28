> Todo el contenido está en `design.md`. Código según `AGENTS.md`. Colores en `base.css` solo con las variables del tema. Commits **sin líneas de coautoría**, por rutas. Antes de archivar, la auditoría de «Transición» de `AGENTS.md` sobre todo `src/main`.

## 1. Estado

- [ ] 1.1 Añadir este change a «En curso» en `ESTADO.md`.

## 2. Menú principal

- [ ] 2.1 `base.css` y los siete `tema-*.css` según `design.md - D2`.
- [ ] 2.2 Prueba nueva en `PantallaMenuPrincipalTest` (D2). Comprobar que falla quitando la regla nueva de `base.css` y que pasa con ella.

## 3. Configuración

- [ ] 3.1 `Configuracion.fxml`: barra lateral con los cuatro grupos y las ocho secciones, sus iconos y los paneles reorganizados según `design.md - D3` y `D5`, con la cabecera de cada sección (icono, título y ayuda de D4).
- [ ] 3.2 `ConfiguracionController`: `fx:id`, métodos `ver…` y `mostrarSeccion` según D3; con la empresa bloqueada se abre Datos fiscales; vista previa del logotipo y `quitarLogotipo` según D3.
- [ ] 3.3 Estilos de `lista-secciones`, `caja-icono-seccion`, `icono-seccion`, `cabecera-seccion`, `titulo-seccion` y `ayuda-seccion` en `base.css` según D5.
- [ ] 3.4 Todos los textos de D4, incluido «Siguiente n.º (año)» en el controlador y los mensajes que nombren un campo renombrado.
- [ ] 3.5 «...» → «…» en los FXML de D4. `grep -rn '\.\.\."' src/main` no devuelve nada.
- [ ] 3.6 Comprobar a 1024×768 que cada sección cabe sin desplazarse. Apuntar aquí si hizo falta reducir el pie legal o la vista previa (D3).

## 4. Pruebas

- [ ] 4.1 Adaptar las pruebas a los textos nuevos (D7). `grep` en `src/test` de los textos viejos de D4: nada.
- [ ] 4.2 `TextosCompletosTest` con las ocho secciones.
- [ ] 4.3 Pruebas nuevas de `PantallaConfiguracionTest` (D7).

## 5. Especificación y documentación

- [ ] 5.1 `openspec validate menu-y-configuracion --strict` en verde (los deltas ya están escritos).
- [ ] 5.2 `docs/diseno.md`, ronda 8 (D8), y `README.md` con `configuracion.png` en la tabla de capturas.

## 6. Capturas (sesión principal, con Computer use)

- [ ] 6.1 Rehacer `docs/capturas/menu.png` y añadir `docs/capturas/configuracion.png` (D8), con la demo y el tema biblioteca8.

## 7. Repaso

- [ ] 7.1 El `grep` de estilo de siempre sobre los ficheros tocados y la auditoría de «Transición» sobre todo `src/main`: solo las excepciones escritas.
- [ ] 7.2 `rm -rf target` y `mvn test` completo: todo en verde. Apuntar aquí pruebas y tiempo, y la cifra en `README.md` y `docs/metodologia.md` si cambia.

## 8. Pruebas manuales (el alumno)

- [ ] 8.1 Menú principal en biblioteca8 y en un tema oscuro: sin bordes; al pasar el ratón, el icono crece y el nombre se resalta y se subraya. Con el tabulador pasa lo mismo.
- [ ] 8.2 Configuración: los cuatro grupos y las ocho secciones con sus iconos; la sección elegida con su raya y sin negrita; cada sección con su título y su ayuda; nada cortado.
- [ ] 8.3 Logotipo: elegir una imagen se ve en la vista previa; «Quitar logotipo» y «Guardar cambios» → el menú principal sin logo y el PDF con cabecera de texto.
- [ ] 8.4 Diseño del PDF: cambiar cabecera, pie y color se refleja en la vista previa; guardar y exportar una factura.
- [ ] 8.5 Una empresa nueva abre Configuración en Datos fiscales con el aviso nuevo.
