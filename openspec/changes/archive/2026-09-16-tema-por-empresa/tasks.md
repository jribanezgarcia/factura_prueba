> Rutas relativas a `src/main/java/cabofactu/` salvo que se diga otra cosa. Código y textos exactos en `design.md`. Seguir `AGENTS.md` en todo el código nuevo o modificado: sin ternarios, sin `var`, sin streams ni `::`, siempre `import`, Javadoc corto en los métodos públicos nuevos (primera persona del plural). No arreglar de paso construcciones antiguas fuera de las líneas tocadas.

## 1. Guardar el tema en la empresa

- [x] 1.1 En `vista/utilidades/GestorTemas.java`, `guardar(Modelo)` guarda el tema en la empresa activa y en `PreferenciasGlobales`. Ver `design.md - D2`.

## 2. Recordar el tema al conectar

- [x] 2.1 En `modelo/negocio/Empresas.java`, añadir `recordarTema()` y llamarlo como última instrucción de `conectar`. Ver `design.md - D3`.
- [x] 2.2 En `fichero/CopiaSeguridad.java`, llamar a `Empresas.recordarTema()` en `restaurarEnEmpresaActiva` antes de `return rescate;`. Ver `design.md - D4`.
- [x] 2.3 En `modelo/negocio/PreferenciasGlobales.java`, actualizar el Javadoc de la clase. Ver `design.md - D5`.
- [x] 2.4 `mvn -q compile` sin errores.

## 3. Tests

- [x] 3.1 En `src/test/java/cabofactu/modelo/negocio/EmpresasTest.java`, añadir `conectarRecuerdaElTemaDeCadaEmpresa`. Ver `design.md - D6`.
- [x] 3.2 En `src/test/java/cabofactu/fichero/CopiaSeguridadTest.java`, añadir `restaurarRecuerdaElTemaDeLaCopia`. Ver `design.md - D6`.
- [x] 3.3 Con la aplicación cerrada, `mvn clean test`: todos en verde.
- [x] 3.4 Buscar en las líneas nuevas o modificadas construcciones prohibidas por `AGENTS.md` (`? :`, `var `, `.stream(`, `::`, nombres completos de paquete): ninguna.
- [x] 3.5 `openspec validate tema-por-empresa --strict` sin errores.

## 4. Pruebas manuales

- [x] 4.1 Entrar en la empresa de demostración, elegir omarchy en Configuración > PDF y apariencia y guardar: la interfaz queda en omarchy.
- [x] 4.2 Crear otra empresa y cambiar a ella: se ve con biblioteca8.
- [x] 4.3 En la empresa nueva, guardar esmeralda. Volver a la de demostración: se ve omarchy. Volver a la nueva: esmeralda.
- [x] 4.4 Cerrar la aplicación con la empresa nueva abierta y volver a abrirla: la pantalla de arranque sale en esmeralda. Entrar en la demostración: omarchy.
- [x] 4.5 Elegir un tema en el combo sin guardar e ir al menú: vuelve el tema guardado de la empresa.
- [x] 4.6 Hacer una copia de seguridad, guardar otro tema, restaurar la copia sobre la empresa activa: vuelve el tema de la copia.
