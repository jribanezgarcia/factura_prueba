## 1. Nombre de archivo compartido

- [x] 1.1 Extraer a un método estático reutilizable el nombre propuesto (`CODIGO-CORRELATIVO-MES.pdf`, barra por guion) y usarlo desde el editor y el histórico
- [x] 1.2 Test unitario del nombre propuesto (serie C con mes, serie R sin mes, sustitución de la barra)

## 2. UI del histórico

- [x] 2.1 `Historico.fxml`: botón «Exportar PDF» junto a Buscar/Volver
- [x] 2.2 Selección múltiple en la tabla (`SelectionMode.MULTIPLE`), manteniendo el doble clic para abrir

## 3. Exportación

- [x] 3.1 Individual: FileChooser con nombre y carpeta propuestas; Task en segundo plano; recordar última carpeta
- [x] 3.2 Lote: DirectoryChooser único; bucle abrirVersion + exportar en Task con progreso; resumen final con generadas/falladas
- [x] 3.3 Color de acento leído de la preferencia `color_pdf` para todas las exportaciones

## 4. Verificación

- [x] 4.1 Suite completa verde (`mvn test`)
- [ ] 4.2 Verificación manual: exportar una factura y un lote de varias desde el histórico
