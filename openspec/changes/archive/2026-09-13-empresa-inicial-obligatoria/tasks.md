> Las razones, textos y firmas exactos están en `design.md`. Requiere `excepcion-de-datos` archivado. No añadir comentarios en el código.

## 1. Quitar la empresa automática

- [x] 1.1 En `src/main/java/com/alcazaba/facturacion/Main.java`, dejar `prepararDatos()` en crear la raíz de datos: quitar la migración, el registro y la creación de «Comercial Alcazaba». Conservar `cmbEmpresaVacia`. Quitar imports sobrantes. Ver `design.md - D1`.
- [x] 1.2 En `db/Database.java`, borrar `SLUG_EMPRESA_INICIAL` y `migrarInstalacionUnArchivo`.
- [x] 1.3 En `src/test/java/com/alcazaba/facturacion/db/DatabaseTest.java`, borrar los tests que usan esa migración.
- [x] 1.4 No tocar `EmpresaManager.registrarNombre` (lo usa `CargarDemo`).
- [x] 1.5 En `db/CargarDemo.java`, hacer `SLUG` público y extraer `cargar()` según `design.md - D1b`. `main` conserva el borrado previo y el mensaje final, y llama a `cargar()`.
- [x] 1.6 En `Main.start`, después de `adquirirLock()` y antes de `mostrarArranque()`, cargar la demostración si no hay ninguna empresa y dejarla como última empresa, con el tratamiento de error de `design.md - D1b`.

## 2. Arranque: texto de ayuda

- [x] 2.1 En `src/main/resources/com/alcazaba/facturacion/ui/Arranque.fxml`, añadir `lblAyudaEmpresa`. Ver `design.md - D2`.
- [x] 2.2 En `ui/ArranqueController.java`, en `cargarEmpresas`, mostrar `lblAyudaEmpresa` con el texto que corresponda (lista vacía o solo la demostración) y ocultarlo en otro caso.
- [x] 2.3 Corrección tras la prueba manual: el texto de ayuda sale cortado con «…». En `ArranqueController` sustituir los dos textos por los cortos de `design.md - D2`, y en `Arranque.fxml` añadir `minHeight="-Infinity"` a `lblAyudaEmpresa`.
- [x] 2.4 Añadir `ArranqueController.mostrarAvisoInicial(boolean demoRecienCargada)` con los textos de `design.md - D2b`, y llamarlo desde `Main.start` con `Platform.runLater` después de `stage.show()`, pasando si en este arranque se cargó la demostración.
- [ ] 2.5 Comprobar con la carpeta de datos vacía: sale el aviso de bienvenida encima de la ventana, al aceptarlo el texto fijo se lee entero y la tarjeta (Entrar incluido) no se recorta. Cerrar y volver a abrir: ya no sale el aviso, pero sí el texto fijo. Crear otra empresa: desaparece el texto fijo.

## 3. Regla de datos obligatorios

- [x] 3.1 En `service/ConfigService.java`, añadir `datosPendientes(Empresa)`, `datosPendientes()` y `empresaCompleta()` con las etiquetas, el orden y las reglas de `design.md - D3`.
- [x] 3.2 Crear `src/test/java/com/alcazaba/facturacion/service/ConfigServiceTest.java` con, al menos: empresa vacía devuelve las ocho etiquetas en orden; empresa completa y válida devuelve lista vacía; NIF no válido devuelve `NIF`; CP `99999` devuelve `CP`; email mal formado devuelve `Email`; campos con solo espacios cuentan como vacíos.

## 4. Entrada a una empresa

- [x] 4.1 En `ui/Navegador.java`, añadir `mostrarInicio()`. Ver `design.md - D4`.
- [x] 4.2 Usar `mostrarInicio()` en `Main.entrarEnMenu`, en `ConfiguracionController.cambiarEmpresa` y en las dos ramas de `BackupController` que tras restaurar navegan al Menú.
- [x] 4.3 No cambiar el resto de navegaciones a `MenuPrincipal.fxml`.

## 5. Configuración en modo datos pendientes

- [x] 5.1 En `Configuracion.fxml`, añadir `lblDatosPendientes` al principio de `seccionEmpresa`, poner ` *` en los ocho rótulos obligatorios y dar `fx:id="btnVolver"` al botón Volver. Ver `design.md - D6`.
- [x] 5.2 En `ui/BarraNavegacion.java`, añadir `bloquearSalvoSalir(HBox barra)`. Ver `design.md - D8`.
- [x] 5.3 En `ui/ConfiguracionController.java`, en `alIniciar`, calcular el modo pendiente y aplicar los cinco pasos de `design.md - D5`. Guardar la referencia a la barra creada para poder bloquearla.
- [x] 5.4 En `guardar()`, validar con `servicios.config.datosPendientes(empresa)` antes de guardar, y al terminar bien en modo pendiente mostrar «Datos de la empresa completados.» y navegar al Menú. Ver `design.md - D7`.

## 6. Demostración y comprobaciones

- [x] 6.1 Buscar en `src/main/java` las navegaciones a `MenuPrincipal.fxml` que siguen a conectar una empresa (`EmpresaManager.conectar`): todas deben usar `mostrarInicio()`.
- [x] 6.2 En `src/main/resources/db/seed_demo.sql`, cambiar el NIF de la empresa a `B99999997` y el CP a `52999`. Ver `design.md - D9`. Actualizar cualquier test que compruebe los valores antiguos.
- [x] 6.3 Buscar `Comercial Alcazaba` y `comercial_alcazaba` en `src/main`: no debe quedar ninguno. En `src/test` puede quedar como nombre de prueba en `EmpresaManagerTest`.

## 7. Tests y verificación

- [x] 7.1 `mvn test` en verde. Si `ConfiguracionLayoutTest` u otro test de interfaz falla por el modo pendiente, rellenar la empresa en su preparación sin cambiar lo que mide. Ver `design.md - Risks`.
- [ ] 7.2 Con la carpeta de datos vacía (renombrar temporalmente la real), arrancar: aparece «Empresa Demo S.L.» preseleccionada con el texto de demostración. Entrar: se abre el Menú directamente y hay facturas de ejemplo. Cerrar y volver a abrir: la demostración no se recarga ni se duplica.
- [ ] 7.2b Desde el arranque, crear una empresa con «Nueva…»: el texto de ayuda desaparece. Entrar en ella.
- [ ] 7.3 Comprobar que se abre Configuración > Empresa con el texto, el nombre propuesto, asteriscos, barra bloqueada salvo Salir y Volver desactivado; que Salir funciona; y que se puede ir a IVA o Series.
- [ ] 7.4 Pulsar Guardar con datos incompletos y ver la lista de lo que falta. Completar con datos válidos, guardar y comprobar que se pasa al Menú.
- [ ] 7.5 Cerrar y volver a entrar en esa empresa: debe abrir el Menú directamente.
- [ ] 7.6 En una empresa completa, borrar el NIF en Configuración y guardar: no se guarda y avisa.
- [ ] 7.7 Con la ventana a 1024×768, comprobar que la sección Empresa en modo pendiente se ve entera sin desplazarse.
- [ ] 7.8 Restaurar la carpeta de datos real.
