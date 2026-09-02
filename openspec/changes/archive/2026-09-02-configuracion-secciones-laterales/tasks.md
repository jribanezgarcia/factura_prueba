## 1. Geometria compartida de cabecera
- [x] 1.1 Crear `CabeceraLayout` en el paquete `pdf` con el doble del logo y topes 480/170, defectos 120/60, offsets X/Y, lineas de empresa con NIF destacado, margen lateral 40 pt y alto de cabecera `42 + lineas * 13 + 18` con minimo 108
- [x] 1.2 Hacer que `PdfService` delegue en `CabeceraLayout` sin cambiar ningun valor
- [x] 1.3 `CabeceraLayoutTest`: doble, topes, defectos y alto de cabecera

## 2. Barra lateral de secciones
- [x] 2.1 Sustituir el `TabPane` de `Configuracion.fxml` por `ListView` de secciones + `StackPane` con un `VBox` por seccion
- [x] 2.2 `configurarSecciones()` en el controlador: seleccion de la lista muestra su seccion
- [x] 2.3 La barra de guardado se oculta con visible/managed en IVA, Retenciones, Series y Empresas
- [x] 2.4 Mover el tema de la aplicacion de la seccion Empresa a PDF y apariencia
- [x] 2.5 Mantener los `styleClass` con COMAS al reescribir el FXML

## 3. Vista previa de la cabecera
- [x] 3.1 Crear `PreviaCabecera` en el paquete `ui` usando `CabeceraLayout`
- [x] 3.2 Colocarla en la seccion Cabecera y pie, a la derecha del formulario
- [x] 3.3 Repintar al cambiar modo, logo, posicion, tamano y color de acento
- [x] 3.4 Indicar el tamano efectivo del logo junto a los campos de ancho y alto
- [x] 3.5 Rotulo discreto de "vista aproximada"

## 4. Estilos
- [x] 4.1 Clases nuevas acotadas a esta pantalla en `base.css`
- [x] 4.2 Verificar que no se tocan `.card` ni `.zona-contenido`
- [x] 4.3 Revisar la pantalla con los siete temas de color

## 5. Tests
- [x] 5.1 `ConfiguracionLayoutTest`: las siete secciones caben a 1024x768
- [x] 5.2 Barra de guardado visible solo en las tres primeras secciones
- [x] 5.3 Comprobar que los tests nuevos FALLAN con el codigo anterior

## 6. Verificacion
- [x] 6.1 mvn test con la suite completa en verde
- [x] 6.2 Recorrer las siete secciones en la app a 1024x768 sin scroll
- [x] 6.3 La vista previa reacciona a logo, posicion, tamano y color
- [x] 6.4 Guardar, volver a entrar y comprobar que se conserva todo
- [x] 6.5 Alta, modificacion e inactivacion siguen funcionando en IVA, Retenciones y Series
- [x] 6.6 Historico, Clientes, Versiones y Backup sin cambios

## 7. Cierre
- [x] 7.1 Actualizar CONTINUAR_MAÑANA.md
- [x] 7.2 Commit y push
