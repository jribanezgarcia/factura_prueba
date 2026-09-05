## Context

`btnAnular` es el único botón con `styleClass="action-danger-button"` (`Editor.fxml:29`) y el código nunca lo deshabilita (`EditorController:1034-1035` solo alterna visible/managed). El problema es solo de color en el tema por defecto. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que el Anular habilitado se distinga a simple vista de un botón deshabilitado.
- Que el rojo encaje con la paleta que Biblioteca8 ya usa para peligro.

**Non-Goals:**

- No se crea estilo de `:disabled` propio: fuera de alcance.
- No se tocan los otros seis temas ni ningún FXML o controlador.

## Decisions

### D1. Reutilizar el rojo del propio tema, no inventar uno

Biblioteca8 ya pinta el peligro con `#C0392B` (texto de `.danger-button` y de `.chip-anulada`, borde `#E9B7AE` en `.danger-button`). La regla pasa a:

```css
.action-danger-button { -fx-background-color: #FFFFFF; -fx-text-fill: #C0392B; -fx-border-color: #E9B7AE; }
.action-danger-button:hover { -fx-background-color: #F9E7E4; -fx-text-fill: #C0392B; }
```

El hover es un tinte rojo claro, siguiendo el patrón de los demás temas claros (esmeralda usa `#F6E3E3`, sakura `#F6E3EC`, terracota `#F6E3D8`).

Se descarta mantener cualquier gris de fondo: cualquier gris sobre tarjeta `#F6F6F6` con texto claro se sigue leyendo como deshabilitado.

### D2. Alcance mínimo: dos líneas de un fichero

Solo cambian `.action-danger-button` y su `:hover` en `tema-biblioteca8.css`. Como `btnAnular` es su único uso, el cambio no afecta a ningún otro botón ni pantalla.

## Risks / Trade-offs

- [El Anular en blanco con texto rojo se parece al resto de botones de acción, que van en blanco con texto negro] → Mitigación: el texto rojo `#C0392B` lo sigue marcando como peligroso; es el mismo tratamiento que `.danger-button` ya usa en el tema.
- [Un botón Anular realmente deshabilitado (si algún día se deshabilita) usaría el gris de Modena] → Mitigación: precisamente esa es la distinción que se busca; no se crea `:disabled` propio en este change.

## Verificación

- A mano con el tema Biblioteca8: abrir una factura emitida (Anular visible) junto a Versiones/Rectificativa deshabilitados en factura nueva y comprobar que el Anular se distingue.
- `mvn test` en verde (el cambio es solo CSS, sin lógica afectada).
