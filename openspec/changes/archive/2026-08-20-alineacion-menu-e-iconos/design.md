## Context

La aplicación JavaFX usa FXML por vistas (`MenuPrincipal.fxml`, etc.) y una barra de navegación construida en Java (`BarraNavegacion.java`) con botones de icono `SVGPath`. El menú principal (`MenuPrincipal.fxml`) es un `BorderPane` cuyo `center` es un `HBox` con dos columnas: un `VBox` con la caja del logo (`StackPane.menu-logo-box`, 260×200, con `ImageView` `preserveRatio` y `fitWidth=260`) y un `VBox` con la lista de botones. El logo real es apaisado (1847×851). La estética viene de `base.css` + un `tema-*.css` (p. ej. `tema-biblioteca8.css`).

Ver `proposal.md` — Why para la motivación. Este cambio es puramente visual.

## Goals / Non-Goals

**Goals:**

- Que el bloque de empresa/logo y la lista de botones del menú principal queden alineados por su borde superior (variante A aprobada en `prototipos/ajustes-menu-iconos.html`).
- Sustituir el icono de Histórico por un glifo de lista/expediente de documentos (opción 3 aprobada).
- Sustituir el icono de Copia de seguridad por un glifo de disquete (guardar).
- Aplicar ambos glifos tanto en el menú principal como en la barra de navegación.

**Non-Goals:**

- No cambiar acciones, navegación, lógica de negocio, modelo de datos ni la spec (`skip_specs: true`).
- No modificar los temas ni el resto de iconos.

## Decisions

**Alineación de columnas por el borde superior en `MenuPrincipal.fxml`.**
El `HBox` del `center` usa `alignment="TOP_LEFT"` para que las dos columnas queden ancladas por su borde superior, y la caja del logo se redimensiona a una proporción acorde al logo real (260×120, min 240×110). Como el `HBox` llena toda la zona central y `TOP_LEFT` pegaría el bloque al margen superior izquierdo, el `HBox` se envuelve en un `StackPane` y se limita a su tamaño preferido (`maxWidth`/`maxHeight` = `USE_PREF_SIZE`): el `StackPane` centra el bloque completo en la ventana y dentro del bloque las columnas conservan el anclaje superior. El `VBox` de botones fija `prefWidth=430` (antes crecía por `hgrow`) para mantener el ancho del mockup. El resultado equivale a la variante A: bloque centrado con las dos columnas ancladas arriba.
- Alternativa descartada: apilar logo y botones verticalmente (variante B) o cabecera de empresa (variante C); el usuario eligió la A por mantener el layout izquierda/derecha ya aprobado.

**Glifo de Histórico = lista/expediente.**
Se sustituye la lupa por el trazado de un portapapeles con líneas (material "assignment"): `M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z`. Es un solo `SVGPath` y encaja con el resto de glifos monocromo actuales.
- Alternativas descartadas: reloj con flecha y reloj simple (opciones 1 y 2); el usuario eligió la lista.

**Glifo de Copia de seguridad = disquete.**
Se sustituye la caja genérica por el disquete de guardar (material "save"): `M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z`. Un solo `SVGPath`, monocromo, reconocible como "guardar".
- Alternativas descartadas: caja fuerte con dial, caja fuerte con candado y escudo con check; el usuario pidió expresamente el icono tipo disquete/guardar.

**Un solo punto de definición de los glifos.**
Los dos glifos se cambian en `MenuPrincipal.fxml` (constantes `content="..."` de los `SVGPath`) y en `BarraNavegacion.java` (constantes `ICONO_HISTORICO` e `ICONO_BACKUP`), que son los únicos sitios donde viven.

## Risks / Trade-offs

- Los glifos nuevos deben leerse bien a tamaño menú (34px con escala 1.4) y en la barra (22px, blanco sobre azul); el trazado de disquete es denso a 22px → Verificar visualmente ambas vistas al ejecutar; si el disquete no se distingue a 22px, se puede subir la escala del icono de la barra en `base.css`.
- Al ser cambio visual, no hay tests de lógica que lo cubran → Validar con la suite (`mvn.cmd test`, 31 tests) para confirmar que nada se rompe y con una ejecución manual.