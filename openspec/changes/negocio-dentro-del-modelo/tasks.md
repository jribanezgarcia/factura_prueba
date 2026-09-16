> Aplicar **después** de archivar `mvc-como-biblioteca8`. Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. Código y Javadoc exactos en `design.md`. Seguir `AGENTS.md` en todo el código nuevo y en las líneas que se reescriben: sin ternarios, sin `var`, sin streams ni `::`, siempre `import`, Javadoc corto en primera persona del plural. En las líneas donde solo se sustituye la llamada no se arregla nada más.

## 1. Negocio

- [ ] 1.1 `modelo/negocio/Sesion.java` como clase normal. Ver `design.md - D1`.
- [ ] 1.2 `modelo/negocio/Empresas.java` sin `static`, con la `Sesion` por constructor. Ver `design.md - D2`.
- [ ] 1.3 `modelo/negocio/Reloj.java` recibe la `Sesion`. Ver `design.md - D3`.
- [ ] 1.4 `modelo/Modelo.java`: crear `Sesion` y `Empresas`, pasarlas a `Reloj` y `CopiaSeguridad`, y añadir `getSesion()` y `getEmpresas()`. Ver `design.md - D4`.
- [ ] 1.5 `fichero/CopiaSeguridad.java` recibe las `Empresas` por constructor. Ver `design.md - D5`.
- [ ] 1.6 `modelo/negocio/sqlite/CargarDemo.java`, `PreparacionDatos.java` y `controlador/Controlador.java`. Ver `design.md - D6`.

## 2. Pantallas

- [ ] 2.1 `ArranqueController`, `ConfiguracionController` y `CopiaSeguridadController`: llamada completa a `getEmpresas()` y `getSesion()`. Ver `design.md - D7`.
- [ ] 2.2 `grep -rn "Empresas\.[a-z]\|Sesion\.[a-z]" src/main`: sin resultados (sí puede aparecer `Empresas.EmpresaInfo`).
- [ ] 2.3 `mvn -q compile` sin errores.

## 3. AGENTS.md

- [ ] 3.1 Añadir `Calculos` y `PreferenciasGlobales` a la lista de herramientas `static`. Ver `design.md - D8`.

## 4. Tests

- [ ] 4.1 Adaptar `EmpresasTest`, `CopiaSeguridadTest`, `CopiaSeguridadDAOTest` y `PreparacionDatosTest`. Ver `design.md - D9`.
- [ ] 4.2 `grep -rn "Empresas\.[a-z]\|Sesion\.[a-z]" src/test`: sin resultados.
- [ ] 4.3 Con la aplicación cerrada, borrar `target` y `mvn test`: todos en verde.
- [ ] 4.4 En `Sesion`, `Reloj` y en las líneas nuevas o reescritas, buscar construcciones prohibidas por `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, nombres completos de clase): ninguna.
- [ ] 4.5 `openspec validate negocio-dentro-del-modelo --strict` sin errores.

## 5. Pruebas manuales

- [ ] 5.1 Borrar `%APPDATA%\Facturacion` y abrir: se carga la demostración y sale preseleccionada con el aviso de bienvenida.
- [ ] 5.2 Entrar en la demostración con un ejercicio anterior y una fecha a mano: el menú muestra esa fecha de trabajo y una factura nueva la trae como fecha inicial.
- [ ] 5.3 En Configuración > Empresas: crear una empresa, cambiar a ella, volver a la demostración y eliminar la otra. Intentar eliminar la activa: aviso de que no se puede.
- [ ] 5.4 Restaurar una copia como empresa nueva y cambiar a ella: entra sin errores.
