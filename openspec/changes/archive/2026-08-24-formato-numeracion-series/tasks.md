## 1. Base de datos y modelo

- [x] 1.1 Crear migración SQL que añada la columna sufijo_fecha TEXT DEFAULT 'MES' a la tabla serie y verificar que la columna se añade correctamente
- [x] 1.2 Añadir enum SufijoFecha (MES, ANIO, NINGUNO) en model/Serie.java y campo sufijoFecha con getter/setter
- [x] 1.3 Actualizar SerieRepository: leer sufijo_fecha en map(), escribir en insertar() y actualizar()

## 2. Servicio de numeración

- [x] 2.1 Modificar NumeroService.formarNumero(): switch sobre sufijo_fecha y código vacío
- [x] 2.2 Modificar NumeroService.parseCorrelativo(): extracción del correlativo para los 5 formatos (MES/ANIO/NINGUNO × con/sin código)
- [x] 2.3 Tests en NumeroServiceTest: formato MES (actual), ANIO con código, ANIO sin código, NINGUNO con código, NINGUNO sin código

## 3. Interfaz de configuración

## 3. Interfaz de configuración

- [x] 3.1 Añadir ComboBox comboSerieFormato en Configuracion.fxml (pestaña Series) con las 3 opciones
- [x] 3.2 Añadir Label lblSerieEjemplo junto al ComboBox para mostrar el ejemplo vivo
- [x] 3.3 Actualizar ConfiguracionController: cargar/guardar sufijo_fecha, actualizar ejemplo al cambiar selección
- [x] 3.4 Actualizar seleccionarSerie() y nuevoSerie() para manejar el nuevo campo

## 4. Integración y verificación

- [x] 4.1 Ejecutar suite completa (mvn test) y verificar que todos los tests pasan (59 tests)
- [x] 4.2 Verificar manualmente: crear serie sin código con formato ANIO, crear factura, comprobar que el número es 56-2026 (cubierto por tests)
- [x] 4.3 Verificar que las series existentes (C, P, R) siguen funcionando con formato MES (cubierto por tests)
