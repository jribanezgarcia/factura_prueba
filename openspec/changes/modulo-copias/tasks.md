> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código y los mensajes exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y reescrito, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, sin clases anónimas, sin `record`, **sin clases, `enum` ni `record` dentro de otra clase**, sin `instanceof` con variable, sin `Optional` (salvo el de `showAndWait()`), siempre `import`, sin `Task` ni `Thread`, lambdas que solo llaman a un método con nombre y Javadoc corto en primera persona del plural. **No tocar** el paquete `pdf`, el script de tablas, el editor, el histórico, los temas ni `base.css`. Commits **sin líneas de coautoría**.

## 1. Datos y conexión

- [x] 1.1 Nuevo `modelo/dominio/ResumenCopia.java`. Ver `design.md - D1`.
- [x] 1.2 Reescribir `modelo/negocio/sqlite/Conexion.java`: fuera los cuatro métodos de transacción, `crearBase` con `throws Exception`, `getEmpresasDisponibles` con `listFiles()`, sin ternario, `leerScript` con `SQLException`, sin `synchronized` y con Javadoc. Ver `design.md - D3`.
- [x] 1.3 `modelo/negocio/PreferenciasGlobales.java`: constante `CARPETA_COPIAS`. `modelo/negocio/Empresas.java`: `recordarUltima(String carpeta)`, y `abrir` pasa a usarlo. Ver `design.md - D2`.

## 2. Negocio

- [x] 2.1 Nuevo `modelo/negocio/CopiaSeguridad.java` como singleton, con los métodos públicos y privados de `design.md - D2`.
- [x] 2.2 Borrar `fichero/CopiaSeguridad.java` (y la carpeta `fichero`), `modelo/negocio/sqlite/CopiaSeguridadDAO.java` y `modelo/negocio/sqlite/DatosException.java`.
- [x] 2.3 `modelo/Modelo.java` y `controlador/Controlador.java`: las siete operaciones de `design.md - D2`; fuera `Modelo(Clock)`, el campo `copiaSeguridad`, `getCopiaSeguridad()` y `Controlador.getModelo()`.
- [x] 2.4 `mvn -q compile` sin errores (la pantalla, con lo justo para compilar hasta la sección 3).

- [x] 2.5 La copia de rescate de `restaurar` no cambia la carpeta recordada: `crear` y `restaurar` comparten el privado `copiarEn`, y solo `crear` guarda `CARPETA_COPIAS`. Test `restaurarNoCambiaLaCarpetaRecordada`. Ver `design.md - D2`.

## 3. La pantalla

- [x] 3.1 `vista/recursos/CopiaSeguridad.fxml`: imports explícitos, `btnCrear` con `Crear copia…`, `lblCarpeta`, `Elegir copia…`, `onAction="#cambiarDestino"` en los dos `RadioButton`, y fuera `lblDestino`, `lblResultado` y `lblResultadoRestauracion`. Ver `design.md - D4`.
- [x] 3.2 Rehacer `vista/controlador/CopiaSeguridadController.java` sin hilos, con `crearCopia`, `elegirCopia`, `prepararDestino`, `cambiarDestino`, `restaurar`, `reemplazarEmpresa`, `crearEmpresaDesdeCopia`, `volver` y los privados de ayuda. Ver `design.md - D4`.
- [x] 3.3 `mvn -q compile` sin errores.

## 4. Tests

- [x] 4.1 Mover `fichero/CopiaSeguridadTest` a `modelo/negocio/CopiaSeguridadTest`, pasar a él los cuatro casos de `modelo/negocio/sqlite/CopiaSeguridadDAOTest`, borrar este y añadir los casos nuevos de `design.md - D5`.
- [x] 4.2 Nuevo `modelo/dominio/ResumenCopiaTest` con los casos de `design.md - D5`.
- [x] 4.3 `ConexionTest`: el caso nuevo de `getEmpresasDisponibles`. Los que usen `crearBase` declaran `throws Exception`.
- [x] 4.4 `PantallaCopiasTest` con los casos de `design.md - D5`. Cada caso empieza con `PreferenciasGlobales.CARPETA_COPIAS` vacía, para que no dependa del orden. `CargaPantallasTest` y `TextosCompletosTest` siguen pasando con la pantalla nueva.

## 5. Repaso

- [x] 5.1 `grep -rn "CopiaSeguridadDAO\|DatosException\|cabofactu.fichero\|getCopiaSeguridad\|getModelo()\|java.time.Clock\|iniciarTransaccion\|lblDestino\|lblResultadoRestauracion" src/`: sin resultados (salvo `CopiaSeguridad.getCopiaSeguridad()`, el propio singleton nuevo).
- [x] 5.2 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: |new [A-Za-z<>]+\([^)]*\) *\{|instanceof [A-Za-z<>?]+ [a-z]|javafx\.[a-z]+\.[A-Z]|java\.[a-z]+\.[a-z]+\.[A-Z]|new Thread|Task<|StringBuilder|static class|enum [A-Z]|synchronized"` sin contar las líneas `import`: dos coincidencias, ambas ajenas al change (el `::memory:` de la URL JDBC y un ternario preexistente sin tocar en `PreferenciasGlobales.get`).
- [x] 5.3 Líneas: `CopiaSeguridadController.java` 239, `CopiaSeguridad.java` 293, `Conexion.java` 202. Ningún método pasa de 40 líneas (el más largo, `crearResumen`, 34).
- [x] 5.4 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde. 465 pruebas, sin fallos, en 7:37 min. Sin `ClassCastException` en el log.
- [x] 5.5 `openspec validate modulo-copias --strict` sin errores ni avisos.

## 6. Documentación

- [x] 6.1 `AGENTS.md`: quitar `.fichero` de la lista de paquetes, y en «Transición» quitar la frase de lo que queda (el DAO de las copias y `DatosException`) y decir que todo el código cumple ya estas normas. La regla de no introducir nada prohibido se queda.
- [x] 6.2 Añadir este change a «En curso» en `ESTADO.md`.

## 7. Pruebas manuales

> La base de datos no cambia: no hace falta borrar `%APPDATA%\Facturacion`.

- [ ] 7.1 En Copias, la primera vez, `lblCarpeta` dice que no se ha elegido carpeta. «Crear copia…» abre el selector de carpetas; al elegir una se crea `demo_AAAAMMDD_HHMMSS.db` sin más pasos, sale el aviso con la ruta y la etiqueta enseña la carpeta. Cancelar el selector no hace nada.
- [ ] 7.2 Volver a pulsar «Crear copia…»: el selector se abre en la misma carpeta. Otra copia en el mismo segundo sale con `_2`.
- [ ] 7.3 Cambiar de empresa, entrar en Copias: la etiqueta enseña la misma carpeta, y la copia se llama con la carpeta de esa empresa.
- [ ] 7.4 «Elegir copia…» se abre en la carpeta de las copias. Al elegir una copia de la misma empresa sale el resumen (empresa, NIF, facturas y última factura en formato `dd/mm/aaaa`) y se pueden elegir las dos opciones.
- [ ] 7.5 Elegir un archivo que no es una base (un `.txt` renombrado a `.db`): avisa y no enseña resumen.
- [ ] 7.6 Reemplazar la empresa activa: pide confirmación con su nombre, avisa con la ruta del rescate en `copias_previas` y vuelve al menú con los datos de la copia.
- [ ] 7.7 Elegir una copia de otra empresa (otro NIF): «Reemplazar» está desactivado y queda marcada «Crear una empresa nueva», con el campo del nombre.
- [ ] 7.8 Crear una empresa desde una copia sin escribir el nombre: avisa. Con nombre, pide confirmación, la crea y pregunta si cambiar a ella. «No»: sigue en Copias con la misma empresa y el bloque de restaurar vacío.
- [ ] 7.9 Repetir la 7.8 contestando «Sí»: se cierra la empresa y sale la pantalla de arranque con la empresa nueva elegida; al entrar, tiene los datos de la copia.
- [ ] 7.10 La pantalla se ve bien y se lee en biblioteca8 y en omarchy, sin textos cortados.
