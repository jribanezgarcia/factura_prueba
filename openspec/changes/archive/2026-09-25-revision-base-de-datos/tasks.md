> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código exacto está en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, **sin clases dentro de clases ni clases anónimas**, sin `Optional` (salvo el de `showAndWait()`), siempre `import` y Javadoc corto en primera persona del plural. **No tocar** las tablas `factura`, `factura_version` ni `factura_linea` (son de `modulo-facturas`), la tabla `preferencias`, ni el `.root` de los `temas/tema-*.css`. Commits **sin líneas de coautoría**.

## 1. Base de datos

- [x] 1.1 `src/main/resources/db/crear_tablas.sql`: las tablas `cliente`, `serie`, `tipo_iva` y `tipo_retencion` como en `design.md - D1`. Nada más del fichero cambia.
- [x] 1.2 `src/main/resources/db/seed_demo.sql`: comprobar que la demo sigue entrando (NIF distintos y nombres distintos). Si no, corregir la demo, no las restricciones.

## 2. Negocio

- [x] 2.1 `modelo/negocio/Clientes.java`: `buscarPorNif` y el privado `comprobarNif`, llamado desde `alta` y `modificar`. Ver `design.md - D2`.
- [x] 2.2 `modelo/negocio/Facturas.java`: el privado `asegurarCliente` y sustituir por él los dos bloques de `crearFactura` y `guardarEditada`. Ver `design.md - D3`.
- [x] 2.3 `modelo/negocio/TiposIva.java` y `TiposRetencion.java`: `buscarPorNombre` y el privado `comprobarNombre`, llamado desde `alta` y `modificar`. Ver `design.md - D5`.

## 3. Controlador y Modelo

- [x] 3.1 Las tres operaciones de `design.md - D6` en `modelo/Modelo.java` y `controlador/Controlador.java`, de una línea cada una.

## 4. Pantallas

- [x] 4.1 `vista/controlador/FichaClienteController.java`: `guardar` con variable local y el privado `nifLibre`. Ver `design.md - D4`.
- [x] 4.2 `vista/controlador/ClientesController.java`: en `anadirCliente`, alta si el cliente no trae id y modificación si lo trae. Ver `design.md - D4`.
- [x] 4.3 `vista/controlador/FichaTipoIvaController.java` y `FichaTipoRetencionController.java`: `guardar` con variable local y el privado `nombreLibre`. Ver `design.md - D5`.
- [x] 4.4 `mvn -q compile` sin errores.

## 5. Tests

- [x] 5.1 `ClientesTest`, `FacturasTest`, `TiposIvaTest` y `TiposRetencionTest`: los casos de `design.md - D7`.
- [x] 5.2 `PantallaClientesTest`: NIF repetido (aviso con el nombre, NIF en rojo, ficha abierta) y recuperar un cliente dado de baja. Ver `design.md - D7`.
- [x] 5.3 `PantallaIvaTest` y `PantallaRetencionesTest`: nombre repetido con aviso y nombre en rojo.
- [x] 5.4 Dar NIF distintos a los tests que hoy dan de alta dos veces el mismo. Apuntar aquí cuáles eran.

> El diff confirma que los NIF repetidos estaban en `EstadosTest.anularFacturasAnulaSoloLasEmitidas` y `HistorialTest.buscaOrdenadoPorNumeroDeFactura`. Los usados repetidamente en `FacturasTest` se conservan porque comprueban la reutilización del cliente.

## 6. Repaso

- [x] 6.1 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: "`: nada nuevo.
- [x] 6.2 `git status`: ningún fichero de las tablas de facturas ni de `preferencias` tocado.
- [x] 6.3 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde y ningún `ClassCastException` en el log. Apuntar aquí cuántas pruebas son.

> 392 pruebas: 0 fallos, 0 errores y 0 omitidas; la batería completa tarda unos 5:15 min y tampoco muestra ningún `ClassCastException` en los informes.
- [x] 6.4 `openspec validate revision-base-de-datos --strict` sin errores.

## 7. Estado

- [x] 7.1 Añadir este change a «En curso» en `ESTADO.md`.

## 8. Pruebas manuales

> Antes de empezar, cierra la aplicación y **borra la carpeta `%APPDATA%\Facturacion`**: las bases que ya existen no reciben las restricciones nuevas.

- [x] 8.1 Clientes → Nuevo, con el NIF `B88888888`: avisa de que ya lo tiene «Cliente Ejemplo S.L.», el NIF sale en rojo y la ficha sigue abierta con lo escrito.
- [x] 8.2 Borrar «Cliente Ejemplo S.L.» (tiene facturas, así que queda inactivo). Nuevo con el NIF `B88888888` y otra dirección: pregunta si recuperarlo. Aceptar: vuelve a estar activo con la dirección nueva, no hay un cliente más en la tabla y sus facturas siguen en el histórico.
- [x] 8.3 Nueva factura escribiendo a mano los datos de «Otro Cliente S.L.» (NIF `B77777779`) sin elegirlo de la lista, y guardarla: en Clientes sigue habiendo un solo «Otro Cliente S.L.».
- [x] 8.4 Configuración → IVA → Nuevo con el nombre «IVA 21%»: avisa y el nombre sale en rojo. Lo mismo en Retenciones con «IRPF profesional».
