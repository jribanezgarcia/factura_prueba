## Why

El formato de numeracion de facturas esta hardcodeado: siempre {codigo}-{correlativo}/{mes}. Algunas empresas necesitan formatos sin prefijo de letra y con ano en lugar de mes (ej: 56-2026 en vez de C-56/7). Actualmente no hay forma de configurar esto.

## What Changes

- Anadir campo sufijo_fecha a la tabla serie con tres opciones: MES (default, comportamiento actual), ANIO y NINGUNO.
- Permitir que el campo codigo de una serie este vacio (sin prefijo de letra).
- Modificar NumeroService.formarNumero() para generar el numero segun el sufijo_fecha y si codigo esta vacio.
- Modificar NumeroService.parseCorrelativo() para extraer el correlativo de los nuevos formatos.
- Anadir ComboBox en la pestana Series de Configuracion para elegir el formato, con ejemplo vivo del resultado.
- Anadir tests para los nuevos formatos.

## Capabilities

### New Capabilities

### Modified Capabilities

- invoicing/spec.md: El requisito Numeracion por series cambia para soportar formatos configurables (sufijo de mes, ano o ninguno; codigo opcional). Se anaden escenarios para los nuevos formatos.

## Impact

- model/Serie.java: nuevo enum SufijoFecha y campo sufijoFecha.
- db/migrations/: nueva migracion ALTER TABLE serie ADD COLUMN sufijo_fecha.
- service/NumeroService.java: logica de formarNumero y parseCorrelativo.
- repository/SerieRepository.java: leer/escribir sufijo_fecha.
- ui/ConfiguracionController.java: ComboBox de formato.
- ui/Configuracion.fxml: campo ComboBox en pestana Series.
- NumeroServiceTest.java: tests nuevos.
