> Todo en `src/main/java/com/alcazaba/facturacion/Main.java`. Las razones, en `design.md`.

## 1. Dejar de restaurar

- [x] 1.1 Borrar la llamada `aplicarPreferenciasVentana(stage);` (línea 96) y el método `aplicarPreferenciasVentana` entero (línea 185). Ver `design.md - D1`.
- [x] 1.2 Borrar la llamada `restaurarTamanoGuardado();` en `entrarEnMenu` (línea 121) y el método `restaurarTamanoGuardado` entero, con su comentario (líneas 125-137).
- [x] 1.3 Borrar los campos `anchoGuardado` y `altoGuardado` (líneas 41-42) y las constantes `ANCHO_INICIAL` y `ALTO_INICIAL` (líneas 32-33).
- [x] 1.4 No tocar `guardarPreferenciasVentana` ni su llamada en `cerrarAplicacion`. Ver `design.md - D2`.
- [x] 1.5 Comprobar que no queda ningún `import` sin uso.

## 2. Verificación final

- [x] 2.1 `mvn test` en verde.
- [x] 2.2 Abrir la aplicación, mover la ventana a una esquina, agrandarla y cerrarla. Volver a abrirla: el arranque debe salir centrado a 760x520 y, al entrar, el menú centrado a 1024x768.
- [x] 2.3 Maximizar durante la sesión y comprobar que funciona, y que la ventana no baja de 1024x768 al encogerla.
- [x] 2.4 Comprobar que el archivo de preferencias sigue recibiendo `ventana_x`, `ventana_y`, `ventana_w` y `ventana_h` al cerrar.
