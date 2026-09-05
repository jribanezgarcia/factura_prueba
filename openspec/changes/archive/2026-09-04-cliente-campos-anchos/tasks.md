> Los valores exactos están en `design.md`, secciones «D1» y «D2».
> Usarlos tal cual; no reinterpretarlos.

## 1. Rejilla CLIENTE en Editor.fxml

- [x] 1.1 Cambiar las 2 columnas de etiqueta de la rejilla CLIENTE de `prefWidth="105.0"` a `prefWidth="75.0"` (las de FACTURA se quedan a 105) y verificar que «Dirección», «Localidad» y «Provincia» se leen enteras.
- [x] 1.2 Cambiar las columnas de campo CLIENTE de 200/210 a **240/90**, y los bloques a FACTURA 440→**420** y CLIENTE 485→**505**; verificar que el FXML carga y la suma da ≈958.
- [x] 1.3 Confirmar que FACTURA conserva etiquetas a 105 y campos 120/120.

## 2. Especificación

- [x] 2.1 MODIFIED «Distribución estable al redimensionar en Editor e Histórico»: campos de cliente anchos y etiquetas próximas, con su escenario.

## 3. Verificación final

- [x] 3.1 Suite completa en verde con `mvn test`.
- [x] 3.2 A mano a 1024×768: Nombre/Email/Localidad con ~doble ancho que NIF/CP/Provincia, etiquetas NIF/CP/Provincia junto a sus campos, Serie/Fecha legibles y sin desbordes.
