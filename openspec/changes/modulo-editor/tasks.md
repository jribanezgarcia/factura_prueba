> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. El código y los mensajes exactos están en `design.md`. Se sigue `AGENTS.md` en todo el código nuevo y reescrito, también en los tests: solo `Exception`, sin ternarios, sin `var`, sin streams, sin `::`, sin clases anónimas, sin `record`, sin `instanceof` con variable, sin `Optional` (salvo el de `showAndWait()`), siempre `import`, lambdas que solo llaman a un método con nombre y Javadoc corto en primera persona del plural. Las únicas clases dentro de otra son las celdas de `EditorController`. **No tocar** el aspecto del editor, el `Clock` de `CopiaSeguridad`, las tablas ni el `.root` de los `temas/tema-*.css`. Commits **sin líneas de coautoría**.

## 1. Piezas de fuera

- [ ] 1.1 Nuevo `modelo/dominio/GrupoIva.java` en lugar de `ResumenFactura.IvaGrupo`, con su constructor y sus tres getters de texto; `ResumenFactura.getFilaTotales()`. Adaptar `Calculos` y `pdf/ConstructorDocumentoFactura`. Ver `design.md - D6`.
- [ ] 1.2 `modelo/dominio/LineaFactura.java`: fuera el constructor vacío; constructor con cantidad y precio; setters que validan; `setTipoIva`, `tieneContenido` y los getters de texto. Adaptar `Facturas` y `FacturacionMensual`. Ver `design.md - D6`.
- [ ] 1.3 Borrar `modelo/negocio/ValidacionCliente.java`; en `Facturas` (dos sitios) y `FacturacionMensual`, la comprobación de cliente `null` de `design.md - D6`.
- [ ] 1.4 Borrar `modelo/negocio/Reloj.java`; `Sesion.getFechaTrabajo()` con la fecha de hoy si no hay; `fechaTrabajo()` en `Modelo` y `Controlador`; `Modelo` sin `reloj` ni `getReloj()`. Adaptar `ConfiguracionController`, `MenuPrincipalController` y `GenerarFacturasMensualesController`. Ver `design.md - D6`.
- [ ] 1.5 `mvn -q compile` sin errores (el editor, con lo justo para compilar hasta la sección 3).

## 2. Utilidades de la vista

- [ ] 2.1 `vista/utilidades/Dialogos.java`: `mostrarDialogoNumeroLibre`. Ver `design.md - D3`.
- [ ] 2.2 Nuevo `vista/utilidades/ConversorCliente.java`. Ver `design.md - D2`.

## 3. El editor

- [ ] 3.1 Rehacer `vista/controlador/EditorController.java` con el orden, el `initialize`, `cambiado()` y `aplicarEstado()` de `design.md - D1`. Borrar todo lo que no sea del editor nuevo: `DIAGNOSTICO_FOCO`, `trazarFoco`, `editarCeldaSegura`, `etiquetaMatriz`, `avisarPrimerErrorCliente`, `setEditable`, `actualizarBotonesEstado`, `nz`, los separadores y el comentario «(6.4)».
- [ ] 3.2 El cliente: buscador con `ConversorCliente`, las marcas en rojo como la ficha, `clienteDeFormulario` por NIF, `confirmarCliente` con el aviso del cliente nuevo y `pedirActualizarFicha`. Ver `design.md - D2`.
- [ ] 3.3 El número: `numeroPropuesto`, el número libre solo con el número propuesto y con `Dialogos.mostrarDialogoNumeroLibre`. Ver `design.md - D3`.
- [ ] 3.4 La tabla de líneas: columnas con `PropertyValueFactory`, las celdas `CeldaTexto`, `CeldaCantidad`, `CeldaPrecio`, `CeldaTotal`, `CeldaDescripcion` (tres renglones) y `CeldaIva`, el salto con Enter con un solo `runLater`, y `teclaEnTabla`. Ver `design.md - D4`.
- [ ] 3.5 Totales, exportar a PDF sin `Task` ni `Thread`, rectificar, anular, restaurar y atajos. Ver `design.md - D5`.
- [ ] 3.6 `guardar` partido en métodos con nombre, en este orden:
  1. Comprobar el cliente, la fecha y las líneas.
  2. `confirmarCliente`.
  3. `pedirActualizarFicha`.
  4. Guardar la factura nueva (con el número libre) o la emitida (con su confirmación).
  5. Actualizar la ficha si se aceptó.
- [ ] 3.7 `mvn -q compile` sin errores.

## 4. Tests

- [ ] 4.1 `PruebaDePantalla`: `cancelarAviso()` y `escribirEnCelda(...)`. Ver `design.md - D7`.
- [ ] 4.2 `PantallaEditorTest`: los nueve casos de `design.md - D7`. Los de hoy siguen pasando.
- [ ] 4.3 `LineaFacturaTest`, `GrupoIvaTest`, `SesionTest` y el caso de `FacturasTest` de `design.md - D7`; adaptar `CalculosTest` y los tests que construyen líneas.

## 5. Repaso

- [ ] 5.1 `grep -rn "ValidacionCliente\|Reloj\|getReloj\|IvaGrupo\|new LineaFactura()" src/`: sin resultados.
- [ ] 5.2 En los ficheros tocados, `grep -nE "\bvar \b|\.stream\(\)|::|record |\? .*: |new [A-Za-z<>]+\([^)]*\) *\{|instanceof [A-Za-z<>?]+ [a-z]|javafx\.[a-z]+\.[A-Z]|new Thread|Task<"`: nada.
- [ ] 5.3 `grep -n "System.out\|ignored" vista/controlador/EditorController.java`: nada.
- [ ] 5.4 Apuntar aquí cuántas líneas tiene `EditorController.java`.
- [ ] 5.5 Con la aplicación cerrada, borrar `target` y `mvn test`: todo en verde y ningún `ClassCastException` en el log. Apuntar aquí cuántas pruebas son y cuánto tarda.
- [ ] 5.6 `openspec validate modulo-editor --strict` sin errores.

## 6. Estado

- [ ] 6.1 Añadir este change a «En curso» en `ESTADO.md`.
- [ ] 6.2 En «Trampas conocidas» de `ESTADO.md`, quitar las dos que ya no son verdad: «**Anular crea versión**…» y «**Rectificar guarda al momento y `Guardar` abre versiones**…».

## 7. Pruebas manuales

> La base de datos no cambia: no hace falta borrar `%APPDATA%\Facturacion`.

- [ ] 7.1 Nueva factura. En la primera línea, sin tocar el ratón: cantidad, Enter, descripción, Enter, precio, Enter, Enter. El cursor salta cada vez a la celda siguiente, listo para escribir, y al final aparece una línea nueva con el cursor en su cantidad.
- [ ] 7.2 Escribir una descripción de cuatro o cinco renglones: mientras se escribe, el cuadro tiene tres renglones con barra; al pulsar Enter, la fila crece y se lee entera.
- [ ] 7.3 Cambiar el IVA de una línea con su desplegable, y poner otra como suplido: los totales y el desglose se actualizan.
- [ ] 7.4 Marcar «Total de línea con IVA incluido», escribir 121 en el total de una línea al 21 %: el precio queda en 100,00.
- [ ] 7.5 Cliente escrito a mano con un NIF nuevo: avisa de que se guardará en la lista. Cancelar: no se guarda nada. Aceptar: se guarda, y el cliente sale en Clientes.
- [ ] 7.6 Elegir un cliente de la lista y cambiarle el email: pregunta si actualizar su ficha. Elegir otro cliente y cambiarle el NIF: no pregunta por su ficha y avisa de cliente nuevo.
- [ ] 7.7 Borrar una factura desde el histórico que no sea la última de su serie y crear una nueva de esa serie: al guardar ofrece el número libre con los dos botones. Crear otra escribiendo el número a mano: no pregunta.
- [ ] 7.8 Exportar a PDF desde el editor: sale el cursor de espera, el aviso con la ruta y el PDF se ve bien.
- [ ] 7.9 Ctrl+S guarda, Ctrl+P exporta, Ctrl+N abre una factura nueva y Esc vuelve al menú (con el aviso de cambios si los hay).
- [ ] 7.10 El editor se ve igual que antes a 1024×768 y maximizado.
