## Context

`base.css` declara `-fx-font-weight: bold` en dieciséis sitios:

| Se queda | Se retira |
|---|---|
| `.total-fila .valor` | `.section-title`, `.titulo`, `.chip-anulada`, `.nav-button`, `.btn-ribbon`, `.primary-button.btn-ribbon`, el bloque de `btn-suave` que la declara, `.table-view .column-header .label`, `.bloque-titulo`, `.empresa-nombre`, `.menu-item .nombre`, la cabecera de `.dialog-card`, `.lista-secciones .list-cell`, su variante seleccionada y `.grupo-secciones` |

Los siete temas declaran además `.total-grande .label` en negrita, que también se queda.

La acción principal se distingue hoy por dos vías: `.primary-button.btn-ribbon` con texto de acento y negrita, y `.primary-button.btn-suave` con `-fx-boton-lavado-fuerte` y texto de acento. Esa variable se usa en tres reglas de `base.css` —reposo, puntero encima y pulsado— y está declarada en los siete temas.

El texto de los botones con icono lo pone hoy el tema por su clase de tipo, en color de texto normal, mientras que el icono va en acento por `.icono-boton`. De ahí que el color se corte a mitad del botón.

## Goals / Non-Goals

**Goals:**

- Que la jerarquía la den el tamaño, el color y el orden, y no el peso de la letra.
- Que el color de un botón con icono alcance a todo el botón, no solo al dibujo.

**Non-Goals:**

- No se cambia la familia tipográfica. Ya es Segoe UI, la misma que usa la aplicación de referencia; lo que se percibía como otra fuente era el peso.
- No se cambia ningún tamaño de letra.
- No se retira el sombreado suave de los botones de solo texto.
- No se toca el color rojo de los botones destructivos.
- No se toca la barra de navegación más allá de quitarle la negrita: su texto va sobre el fondo de acento y debe seguir en el color que cada tema le da.

## Decisions

### D1. Decisión: la negrita se reserva a los importes

Un importe es el único texto de esta aplicación que se busca con la vista en lugar de leerse en orden. Ahí la negrita hace un trabajo que ningún otro recurso hace igual de rápido.

En todo lo demás compite consigo misma: cuando dieciséis cosas están en negrita, ninguna destaca.

Alternativa descartada: quitarla también de los importes, como hace la aplicación de referencia. Descartada por decisión expresa del usuario, y con criterio: los totales son el dato que más se consulta de la pantalla.

### D2. Decisión: icono y etiqueta comparten color

Se añade `-fx-text-fill: -fx-accent` al selector de dos clases que agrupa `primary-button`, `default-button` y `action-button` con `btn-ribbon`. Gana al tema por especificidad, igual que ya hace el resto de esa familia de reglas.

Los destructivos quedan fuera de ese selector a propósito: el tema ya les pone el rojo por su clase de tipo, y así lo conservan tanto en el icono como en la etiqueta.

### D3. Decisión: la acción principal deja de existir como categoría visual

Se retira la negrita y el texto de acento de `.primary-button.btn-ribbon`, que pasa a ser idéntico a `.action-button.btn-ribbon`, y se elimina entera la regla `.primary-button.btn-suave` junto con sus variantes de puntero encima y pulsado.

`primary-button` sigue existiendo como clase en los FXML y no se toca ninguno: simplemente deja de pintar distinto. Eso conserva la intención declarada en el marcado por si algún día se quiere recuperar.

Con eso `-fx-boton-lavado-fuerte` se queda sin ninguna referencia y se retira de los siete temas. Es la variable que se afinó hasta 0.15. Su gemela `-fx-boton-lavado`, la del sombreado normal, se queda intacta.

### D4. Decisión: la regla del icono del botón principal no se toca

`.primary-button.btn-ribbon .icono-boton` pone ese icono en color de acento. Parece parte de la distinción que estamos retirando, pero no lo es.

Sin esa regla, el tema aplicaría `.primary-button .icono-boton`, que pinta el icono del color que contrasta **sobre** el acento —blanco en Biblioteca8—, pensado para cuando el botón tenía fondo de acento. Sobre la barra transparente ese icono sería invisible.

Es la trampa de este change. Se conserva.

## Riesgos

`.grupo-secciones`, los rótulos CONFIGURACIÓN GENERAL y CATÁLOGOS de la lista lateral, van a 11px con opacidad 0.55. Al quitarles la negrita pueden quedar demasiado tenues. Si no se leen, la salida es subir la opacidad, nunca devolverles el peso.

Lo mismo con `.chip-anulada`: pierde la negrita y conserva solo el rojo. Hay un requisito vigente que exige que una factura anulada se identifique de un vistazo.

## Verificación

- Recorrer todas las pantallas comprobando que no queda ni una negrita fuera de los totales.
- Comprobar que en el Editor y en el Histórico icono y etiqueta comparten color, y que Anular sigue en rojo en ambos.
- Comprobar que Guardar se ve exactamente igual que Nueva.
- Comprobar la lista lateral de Configuración y el distintivo ANULADA, que son los dos puntos donde la pérdida de peso puede pasar factura.
- Repetir en un tema claro y en uno oscuro.
