## 1. Cancelar el cierre
- [x] 1.1 `cerrarAplicacion` devuelve boolean segun se cierre o se cancele
- [x] 1.2 El manejador de `setOnCloseRequest` consume el evento cuando se cancela
- [x] 1.3 Mantener el cierre sin preguntar en la pantalla de arranque

## 2. Salir desde el menu principal
- [x] 2.1 `MenuController.salir` dispara `WINDOW_CLOSE_REQUEST` en vez de `close()`

## 3. Verificacion manual
- [x] 3.1 Cerrar con la X y responder que no: la ventana sigue visible
- [x] 3.2 Pulsar Salir en el menu y responder que no: la ventana sigue visible
- [x] 3.3 Con cambios sin guardar, cancelar: no se cierra ni se pierde nada
- [x] 3.4 Responder que si y volver a abrir la aplicacion: arranca sin aviso de instancia en ejecucion

## 4. Cierre
- [x] 4.1 /opsx-archive (este change lleva skip_specs)
- [x] 4.2 Commit y push
