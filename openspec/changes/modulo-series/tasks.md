> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código, los mensajes y los Javadoc exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: solo `Exception` en lo nuevo, sin ternarios, sin `var`, sin streams, sin `::`, **sin clases dentro de clases ni clases anónimas**, sin `Optional` (salvo el de `showAndWait()` con `isPresent()`/`get()`), siempre `import` y Javadoc corto en primera persona del plural. En los ficheros que solo cambian de plumbing (sección 6) no se arregla nada más. **No tocar** el `.root` de los `temas/tema-*.css` ni nada de las versiones de factura: eso es del change siguiente.

## 1. Clases de datos

- [ ] 1.1 Crear `modelo/dominio/FormatoNumero.java` con sus tres valores y su texto. Ver `design.md - D1`.
- [ ] 1.2 Rehacer `modelo/dominio/Serie.java`: constructor, `errorCodigo`, `errorFormato`, código en mayúsculas, getters de texto, constructor copia y `equals`/`hashCode` por el id. Fuera `SufijoFecha`, `siguienteCorrelativo` y `reutilizarAnulados`. Ver `design.md - D2`.
- [ ] 1.3 `grep -rn "SufijoFecha" src`: sin resultados.

## 2. Negocio

- [ ] 2.1 Rehacer `modelo/negocio/Series.java` como singleton con el SQL de las series: `listado`, `buscar`, `alta`, `modificar`, `baja` y `tieneFacturas`, con las dos comprobaciones del código. Ver `design.md - D3`.
- [ ] 2.2 En `Series`, las dos consultas privadas `correlativosUsados` y `correlativosActivos`, con su comentario «Cómo funciona». Ver `design.md - D3`.
- [ ] 2.3 En `Series`, la numeración: `siguienteCorrelativo`, `huecos`, `proponerNumeros`, `formarNumero`, `parseCorrelativo` y `correlativoOcupado`. Ver `design.md - D3`.
- [ ] 2.4 Borrar `modelo/negocio/Numeracion.java`, `modelo/negocio/sqlite/SerieDAO.java` y `modelo/negocio/sqlite/NumeroDisponibleDAO.java`.
- [ ] 2.5 `grep -rn "SELECT \*" src/main/java/cabofactu/modelo/negocio/Series.java`: sin resultados.

## 3. Controlador y Modelo

- [ ] 3.1 Las doce operaciones de `design.md - D4` en `modelo/Modelo.java` y `controlador/Controlador.java`, de una línea cada una. Borrar `getSeries()`, `getNumeracion()` y los dos DAO del constructor de `Modelo`.

## 4. La ficha de serie

- [ ] 4.1 Crear `src/main/resources/cabofactu/vista/recursos/FichaSerie.fxml` con código, descripción, formato, la casilla de rectificativa, la etiqueta del ejemplo y los botones Cancelar y Guardar. Ver `design.md - D5`.
- [ ] 4.2 Crear `vista/controlador/FichaSerieController.java` con el patrón de `FichaTipoIvaController` y `actualizarEjemplo()`. Ver `design.md - D5`.

## 5. La sección Series de Configuración

- [ ] 5.1 En `Configuracion.fxml`, sección Series: fuera la fila de alta rápida y la columna «Reutilizar anulados»; añadir la columna «Formato» y los botones Nuevo, Editar y Eliminar debajo de la tabla; `onMouseClicked` en la tabla. Ver `design.md - D6`.
- [ ] 5.2 En `ConfiguracionController`: `seleccionarSerie(MouseEvent)`, `nuevaSerie`, `editarSerie`, `eliminarSerie`, `abrirFichaSerie` y `refrescarSeries` con la columna «Siguiente (año)» calculada. Borrar el código viejo de la sección, con su `StringConverter` anónimo. Ver `design.md - D6`.
- [ ] 5.3 `grep -nE "new [A-Za-z<>]*\([^)]*\) *\{|static (final )?class" src/main/java/cabofactu/vista/controlador/ConfiguracionController.java`: sin resultados.

## 6. Plumbing

- [ ] 6.1 `modelo/negocio/Facturas.java`: fuera `SerieDAO`, `NumeroDisponibleDAO` y `Numeracion` del constructor y de los campos; fuera las tres líneas de contadores al crear factura y el apunte del hueco al borrarla; el resto por `Series.getSeries()`. Ver `design.md - D8`.
- [ ] 6.2 `Estados`, `Rectificativas` y `FacturacionMensual` por `Series.getSeries()`, y sus constructores sin los DAO. Ver `design.md - D8`.
- [ ] 6.3 `EditorController`, `GenerarFacturasMensualesController` y `HistoricoController` por las operaciones del controlador; en `pedirHueco`, un `for` en lugar del stream. Ver `design.md - D8`.
- [ ] 6.4 `grep -rn "Numeracion\|SerieDAO\|NumeroDisponibleDAO\|getSeries()\|getNumeracion()\|reutilizarAnulados\|siguienteCorrelativo()" src/main/java`: sin resultados (salvo `Series.getSeries()`).
- [ ] 6.5 `mvn -q compile` sin errores.

## 7. Base de datos

- [ ] 7.1 `db/crear_tablas.sql`: la tabla `serie` sin `siguiente_correlativo` ni `reutilizar_anulados`, y fuera las tablas `serie_siguiente` y `numero_disponible`. Ver `design.md - D7`.
- [ ] 7.2 `db/seed_demo.sql`: el `INSERT` de series sin esas columnas y fuera los de `serie_siguiente`.
- [ ] 7.3 `CopiaSeguridadDAO`: fuera las entradas de `serie_siguiente` y `numero_disponible` y esas dos columnas de `serie`.

## 8. Tests

- [ ] 8.1 Crear `src/test/java/cabofactu/modelo/dominio/SerieTest.java`. Ver `design.md - D9`.
- [ ] 8.2 Crear `src/test/java/cabofactu/modelo/negocio/SeriesTest.java` con los casos de `design.md - D9`, contra una base temporal.
- [ ] 8.3 Borrar `modelo/negocio/NumeracionTest.java` y `modelo/negocio/sqlite/SerieDAOTest.java`.
- [ ] 8.4 Adaptar `FacturasTest`, `EstadosTest`, `HistorialTest`, `FacturacionMensualTest`, `ClientesTest` y `EmpresasTest`.
- [ ] 8.5 Añadir `FichaSerie.fxml` a `CargaPantallasTest`.
- [ ] 8.6 Con la aplicación cerrada, borrar `target` y ejecutar `mvn test`: todos en verde y **ningún** `ClassCastException` en el log.
- [ ] 8.7 En `FormatoNumero`, `Serie`, `Series`, `FichaSerieController` y las líneas nuevas de `ConfiguracionController`, `Modelo` y `Controlador`, buscar lo que prohíbe `AGENTS.md`: ninguno.
- [ ] 8.8 `openspec validate modulo-series --strict` sin errores.

## 9. Documentación y estado

- [ ] 9.1 Añadir este change a «En curso» en `ESTADO.md`.

## 10. Pruebas manuales

> Antes de empezar, borra la carpeta `%APPDATA%\Facturacion` para que se cargue la demostración con el esquema nuevo.

- [ ] 10.1 Configuración → Series: se ven las series A y R con su código, descripción, si son rectificativas, su formato y la columna «Siguiente (2026)». Ya **no** hay columna «Reutilizar anulados» ni campos debajo de la tabla.
- [ ] 10.2 La columna «Siguiente (2026)» de la serie A coincide con el mayor número de sus facturas más uno.
- [ ] 10.3 **Nuevo**: se abre la ficha. El desplegable de formato muestra los tres textos («Código-Número/Mes (ej: C-56/7)»…) y la etiqueta del ejemplo cambia al escribir el código y al cambiar el formato.
- [ ] 10.4 Guardar una serie con el código `B%`: sale el aviso de que el código solo puede tener letras y números, y el campo queda en rojo.
- [ ] 10.5 Guardar una serie con el código `A`, que ya existe: avisa de que ya existe una serie con ese código.
- [ ] 10.6 Crear una serie dejando el código vacío: se guarda y en la tabla sale «(sin código)». Crear otra sin código: avisa de que solo puede haber una.
- [ ] 10.7 Doble clic en una serie: se abre la ficha con sus datos. Cambiar la descripción y guardar: se ve en la tabla.
- [ ] 10.8 En la ficha, escribir algo y cerrar con Cancelar o con la X: pregunta si descartar.
- [ ] 10.9 Eliminar la serie que creaste sin facturas: desaparece. Intentar eliminar la serie A: avisa de que tiene facturas.
- [ ] 10.10 Nueva factura con la serie A: el número propuesto es el mayor de 2026 más uno, con el formato de la serie.
- [ ] 10.11 Guardar esa factura, crear otra: el número sube de uno en uno.
- [ ] 10.12 Histórico: borrar una factura del medio (por ejemplo la A-2). Crear una factura nueva: pregunta si usar el hueco 2 o continuar. Probar las dos respuestas.
- [ ] 10.13 Anular una factura y crear otra: **no** se ofrece el número de la anulada; se sigue con el siguiente.
- [ ] 10.14 Escribir un número a mano que ya tiene una factura activa: no deja guardar y avisa.
- [ ] 10.15 Escribir a mano un número muy alto (por ejemplo A-100/9) y guardar: la siguiente factura se propone como la 101.
- [ ] 10.16 Facturar mes: genera varias facturas con números seguidos y, si hay huecos, pregunta si rellenarlos.
- [ ] 10.17 Crear una serie nueva y emitir su primera factura: sale con el correlativo 1.
- [ ] 10.18 Copia de seguridad: crear una copia y restaurarla como empresa nueva; entrar en ella y comprobar que las series y la numeración siguen bien.
