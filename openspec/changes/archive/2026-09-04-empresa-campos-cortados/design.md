## Context

La sección Empresa es un `GridPane` de 4 columnas sin `ColumnConstraints` (`Configuracion.fxml:29-48`): cada columna mide lo que pide su hijo más ancho. La zona derecha dispone de unos 740 px (1024 menos lista lateral de 200, separación de 12 y rellenos). La fila Nombre+Actividad pide unos 814 px. Sobran unos 70 px, que se comen la columna derecha. Ver proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Que los siete campos de texto de la sección Empresa quepan legibles a 1024×768.
- Que el ajuste sea solo de tamaños, sin reordenar filas ni cambiar etiquetas.

**Non-Goals:**

- No se añaden `ColumnConstraints` ni se reestructura el grid.
- No se tocan las demás secciones de Configuración.
- No se cambia el tamaño mínimo de ventana.

## Decisions

### D1. Recortar prefWidth, no reestructurar

Bajar los `prefWidth` de los campos anchos hasta que la fila más ancha quepa en ~740 px. Es el ajuste que pedía el usuario y no altera el layout: las columnas se estrechan solas al pedir menos los hijos.

Se descarta meter `ColumnConstraints` con porcentajes: repartiría el espacio de forma rígida y obligaría a retocar también Dirección (que ocupa 3 columnas) y el resto de filas. Más riesgo para el mismo resultado.

### D2. Reparto orientativo del recorte

Hay que quitar unos 70 px de la fila Nombre+Actividad. Reparto que mantiene proporciones parecidas:

| Campo | Antes | Después |
|---|---|---|
| `txtNombre` | 340 | 300 |
| `txtActividad` | 240 | 200 |
| `txtDireccion` | 440 | 400 |
| `txtLocalidad` | 180 | 160 |
| `txtEmail` | 220 | 200 |

NIF (160), CP (100), Provincia (200) y Teléfono (160) no mandan en ninguna fila ancha y se quedan como están. Los números son orientativos: lo que vale es que la fila más ancha pida menos de ~740 px y que ningún campo quede tan corto que su contenido habitual no quepa (NIF, CP y Teléfono ya son cortos de por sí).

### D3. Comprobación

No hay test de layout para Configuración que mida campos (`ConfiguracionLayoutTest` existe: comprobar qué cubre y reutilizarlo si mide la sección Empresa; si no, la verificación es visual). A mano: abrir Configuración → Empresa a 1024×768 y comprobar que Actividad, Localidad y Nombre / razón social se ven enteros.

## Risks / Trade-offs

- [Reducir `txtNombre` a 300 puede apretar razones sociales largas] → Mitigación: el campo sigue siendo el más ancho de la ficha y el texto hace scroll interno; lo que se exige es que el campo se vea entero, no su contenido completo.
- [Otras resoluciones o escalas del sistema (125 %)] → Mitigación: el requisito fija el mínimo en 1024×768 al 100 %; fuera de eso no se garantiza nada nuevo.
