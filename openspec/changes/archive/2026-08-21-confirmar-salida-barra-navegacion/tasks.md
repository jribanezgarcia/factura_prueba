## 1. Salir desde la barra de navegación

- [x] 1.1 Sustituir en `BarraNavegacion.java` la acción del botón Salir (`nav.stage().close()`) por el lanzamiento del evento `WINDOW_CLOSE_REQUEST` sobre la ventana, de forma que se reutilice el cierre central de `Main.java`; verificar ejecutando la aplicación que al pulsar Salir aparece la confirmación «¿Seguro que deseas salir de la aplicación?» y que al aceptar se cierra guardando preferencias y liberando el lock.

## 2. Verificación final

- [x] 2.1 Ejecutar la suite completa (`mvn.cmd test`) y verificar que compila y que todos los tests pasan sin regresiones.
