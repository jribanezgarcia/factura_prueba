## Context

Rejilla CLIENTE actual (`Editor.fxml`, bloque `prefWidth/maxWidth 485`): etiquetas a 105 fijas, campos 200/210 compartiendo 251 px → columna ancha ~122, estrecha ~129. La etiqueta «NIF» (~20 px) deja ~93 px de hueco hasta su campo. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Nombre, Email y Localidad con al menos el doble de ancho que NIF/CP/Provincia.
- Etiquetas NIF/CP/Provincia pegadas a sus campos.
- No romper Serie/Fecha (~103 px hoy) ni salirse de los 968 px disponibles.

**Non-Goals:**

- No se reordenan filas ni se cambia ninguna etiqueta.
- No se toca el bloque FACTURA salvo ceder 20 px de su `VBox`.
- No se tocan filtros, controladores ni otros temas.

## Decisions

### D1. Etiquetas de CLIENTE a 75, no a 105

El 105 lo exige FACTURA («Forma de pago»); las etiquetas más largas de CLIENTE («Dirección», «Localidad») rondan los 65 px, así que 75 las cubre con margen y recorta el hueco de «NIF»/«CP» de ~93 a ~63 px.

Se descarta alinear a la derecha las etiquetas cortas: acercaría «NIF» a su campo pero dejaría cada etiqueta en un sitio distinto según su longitud; columna estrecha y alineación común es más limpio.

### D2. Campos 240/90 y bloques 420/505

- Columnas de campo CLIENTE: 200/210 → **240/90**. Presupuesto: 505 − 150 − 24 = 331 ≥ 320 → las columnas quedan a su `prefWidth` exacto, sin compresión: ancha **240** (~2× los 122 actuales), estrecha **90** (≈ los 88 actuales, sin retroceso; NIF necesita ~79).
- Bloque CLIENTE 485 → **505**; bloque FACTURA 440 → **420** (Serie/Fecha pasan de ~103 a ~93 px, siguen legibles).
- Cuenta total: 420 + 14 + separador + 14 + 505 ≈ 958 ≤ 968 ✓. Rejilla CLIENTE = 75+240+75+90+3×8 = **504 ≤ 505** ✓.

Se descarta duplicar la ancha manteniendo la estrecha en 129: pediría 240+129+174 = 543 de bloque y dejaría a FACTURA en ~382, devolviendo Serie/Fecha a ~74 px (lo que se acabó de arreglar).

### D3. Contrapartida aceptada: etiquetas desalineadas entre bloques

FACTURA alinea a 105 y CLIENTE a 75. Cada bloque alinea las suyas; entre bloques no coinciden. Se acepta porque la alternativa (105 en ambos) hace imposible el objetivo. Queda anotado aquí para que no se "arregle" después sin ver el motivo.

## Risks / Trade-offs

- [Serie/Fecha bajan de ~103 a ~93 px] → Mitigación: siguen muy por encima de los ~73 px que eran ilegibles; la verificación visual a 1024 lo confirma.
- [Provincia en 90 px no muestra provincias largas enteras] → Mitigación: ya era así (88 px); el campo hace scroll interno y no es un campo de relleno habitual.
- [Si el reparto real del `GridPane` no sigue exactamente la proporción de `prefWidth`] → Mitigación: los valores dejan margen (504 frente a 505); la verificación visual confirma los anchos finales.

## Verificación

- `mvn test` en verde (cambio solo FXML, sin lógica afectada).
- A mano a 1024×768: Nombre/Email/Localidad con ~doble ancho que NIF/CP/Provincia, etiquetas NIF/CP/Provincia junto a sus campos, Serie/Fecha legibles, total sin desbordes.
