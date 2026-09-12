> Las razones están en `design.md`. Ninguna pantalla debe cambiar de aspecto.

## 1. base.css

- [x] 1.1 En `src/main/resources/com/alcazaba/facturacion/themes/base.css`, quitar `.danger-button` y `.action-danger-button` de los nueve selectores agrupados que las nombran: la regla base de botones (líneas 125-126), y los bloques de `btn-ribbon` (174-176, 194-196, 203-205, 211-213) y de `btn-suave` (220-222, 234-236, 243-245, 252-254). Ver `design.md - D1`.
- [x] 1.2 Dejar intactos los demás selectores de cada grupo y sus declaraciones. Ningún bloque debe quedarse sin selector ni con una coma suelta al final.
- [x] 1.3 Comprobar con una búsqueda que no queda ninguna aparición de `danger` en `base.css`.

## 2. Temas y test

- [x] 2.1 En cada uno de los siete `tema-*.css`, borrar las cuatro líneas de peligro: `.danger-button`, `.action-danger-button`, `.action-danger-button:hover` y la de `.danger-button .icono-boton, .action-danger-button .icono-boton`. Ver `design.md - D2`.
- [x] 2.2 No tocar ninguna otra regla de los temas.
- [x] 2.3 En `src/test/java/com/alcazaba/facturacion/ui/BotonesTest.java`, en `botonesSoloTexto`, quitar las dos condiciones que descartan `danger-button` y `action-danger-button`. Ver `design.md - D3`.
- [x] 2.4 Comprobar con una búsqueda que no queda ninguna aparición de `danger` en `src`.

## 3. Verificación final

- [x] 3.1 `mvn test` en verde.
- [x] 3.2 Abrir el Editor, el Histórico, Clientes, Configuración, Copia de seguridad y la generación mensual con un tema claro y uno oscuro, y comprobar que todos los botones se ven exactamente igual que antes: mismo color de texto, mismo sombreado y mismos estados al pasar el puntero, al pulsar y al recibir el foco.
