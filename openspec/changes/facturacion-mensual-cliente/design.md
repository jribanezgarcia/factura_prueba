## Context

El proyecto es una aplicación JavaFX de facturación con arquitectura por capas (`ui`, `service`, `repository`, `model`). Ya existen `FacturaService`, `VersionadoService`, `NumeroService`, `CalculoService`, `ClienteRepository`, `SerieRepository`, `IvaRepository` y `TipoRetencionRepository`. El nuevo change añade un diálogo de generación masiva de facturas mensuales que reutiliza estos servicios en lugar de tocar la base de datos directamente.

## Goals / Non-Goals

**Goals:**
- Permitir generar de una sola vez varias facturas mensuales para un cliente.
- Reutilizar la lógica existente de creación de facturas para mantener cuadre, numeración y validaciones.
- Hacer el diálogo suficientemente flexible para el caso típico de servicios recurrentes.

**Non-Goals:**
- No se crean plantillas de cliente persistentes en esta versión.
- No se soportan importes variables por mes dentro de la misma generación.
- No se modifica el modelo de datos; no hay migraciones.

## Decisions

1. **Diálogo modal propio (`GenerarFacturasMensuales.fxml` + controller)**  
   Se abre desde el menú principal y desde el histórico. Mantiene la UI separada del editor normal y evita mezclar flujos.

2. **Reutilizar `FacturaService.crearFactura(...)` vía `crearFacturaSinTransaccion(...)`**  
   Se extrae la lógica de creación de facturas de la transacción pública para poder ejecutarla dentro de la transacción global del servicio de facturación mensual. Cada factura sigue pasando por las mismas validaciones, cálculo de totales y asignación de número que una factura creada a mano.

3. **Líneas globales para todos los meses**  
   El usuario introduce las líneas una vez en el diálogo y se replican en cada factura. Cada línea tiene cantidad, descripción, precio unitario y un checkbox *Añadir mes*. Esto cubre el caso principal de servicios recurrentes sin complicar el diálogo.

4. **IVA y retención seleccionados a nivel de diálogo**  
   El usuario elige un tipo de IVA y, opcionalmente, un tipo de retención IRPF que se aplican a todas las líneas/facturas generadas. Se asignan a cada línea al construir la factura.

5. **Modos de fecha: día fijo, primer día o último día**  
   El diálogo ofrece tres opciones mediante radio buttons: un día fijo editable (spinner), el día 1 del mes o el último día del mes. El modo `FIJO` ajusta al último día válido si el número elegido no existe en ese mes.

6. **Detección de duplicados por cliente, mes y año**  
   Antes de generar se detectan los meses que ya tienen facturas para el cliente. El sistema muestra un diálogo de confirmación con esos meses y permite al usuario cancelar o generar las facturas de todos modos. Si se acepta, se generan todas las facturas del rango; si se cancela, no se crea ninguna.

7. **Proceso dentro de una transacción**  
   Todas las facturas se generan dentro de una transacción SQLite. Si falla alguna, se hace rollback para evitar números consumidos o facturas parciales.

8. **Anulación y borrado separados desde el histórico**  
   La pantalla de histórico expone dos botones: "Anular" y "Borrar". También se ofrece un menú contextual con esas mismas opciones y "Exportar a PDF". `EstadoService.anularFacturas(...)` cambia el estado a `ANULADA` y devuelve un resumen. `FacturaService.borrarFactura(...)` elimina físicamente la factura, sus versiones y líneas, y registra el número liberado en `numero_disponible`. Antes de borrar se muestra un aviso con el número de versiones y líneas afectadas.

9. **Gestión de huecos de numeración**  
   Al borrar una factura se inserta su correlativo en la tabla `numero_disponible` (serie, año, correlativo). `NumeroService.huecosDisponibles(...)` devuelve esos números que no estén ocupados por facturas activas y `NumeroService.proponerNumeros(...)` construye una lista ordenada rellenando huecos primero. Tanto el editor de facturas nuevas como la generación mensual preguntan una sola vez si se quieren usar los huecos, mostrando la lista de números propuestos. Si se acepta, se asignan esos números y se eliminan del registro al guardar la factura.

10. **Exportación múltiple a PDF**  
    Cuando se seleccionan varias facturas y se pulsa "Exportar a PDF", se muestra un diálogo para elegir entre generar un PDF por factura o un único PDF agrupado. El `PdfService` implementa `exportarAgrupado(...)` generando cada factura en memoria y concatenándolas con `PdfCopy`.

11. **Ventana de generación con tamaño adecuado**  
    El diálogo `GenerarFacturasMensuales` se abre con un tamaño mayor para que los radio buttons del día del mes se lean correctamente, sin que la tabla de líneas ocupe todo el espacio disponible. El botón del menú principal pierde el foco visual tras abrir el diálogo.

## Risks / Trade-offs

- **[Risk]** Si la serie tiene formato `MES`, los números generados en el mismo mes podrían no ser consecutivos si se omiten duplicados.  
  **Mitigación:** El resumen informa claramente de los meses omitidos y el usuario puede revisar la numeración.

- **[Risk]** Crear 12 facturas seguidas puede ser lento si se dispara mucha lógica por factura.  
  **Mitigación:** La operación es puntual y el volumen es pequeño (12 facturas/mes máximo). Si en el futuro escala, se puede añadir una barra de progreso.

- **[Risk]** Si el usuario cierra el diálogo durante la generación, puede quedar una transacción abierta.  
  **Mitigación:** La transacción se gestiona en el servicio con try-with-resources / bloque finally; en caso de excepción se hace rollback.

- **[Risk]** Borrar una factura de forma física puede destruir información histórica si se usa de forma incorrecta.  
  **Mitigación:** La acción de borrar muestra un aviso explícito con las versiones y líneas afectadas, requiere confirmación y afecta solo a la factura seleccionada.

## UI Mockup

El prototipo siguiente representa el diálogo de generación mensual. Los colores y tipografía siguen el estilo actual de la aplicación.

```html
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Generar facturas mensuales</title>
<style>
  :root {
    --bg: #C7C8CA;
    --surface: #FFFFFF;
    --text: #1F2937;
    --muted: #6B7280;
    --accent: #296796;
    --accent-strong: #1A55A8;
    --border: #D1D5DB;
    --radius: 8px;
    --shadow: 0 2px 6px rgba(0,0,0,0.12);
  }
  body { font-family: "Segoe UI", sans-serif; background: var(--bg); display: flex; justify-content: center; padding: 40px; color: var(--text); }
  .dialog { background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); width: 880px; padding: 24px; }
  h2 { margin: 0 0 20px 0; font-size: 20px; color: var(--accent-strong); }
  .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
  .field { display: flex; flex-direction: column; }
  .field label { font-size: 12px; color: var(--muted); margin-bottom: 4px; }
  .field input, .field select { padding: 8px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; }
  .row { display: flex; gap: 12px; align-items: flex-end; }
  .lines { margin-bottom: 16px; }
  .lines h3 { font-size: 14px; margin: 0 0 8px 0; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  th, td { border: 1px solid var(--border); padding: 8px; text-align: left; }
  th { background: #F3F4F6; }
  .actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
  button { padding: 10px 18px; border-radius: 6px; border: none; cursor: pointer; font-size: 14px; }
  .primary { background: var(--accent); color: #fff; }
  .secondary { background: #fff; color: var(--text); border: 1px solid var(--border); }
</style>
</head>
<body>
<div class="dialog">
  <h2>Generar facturas mensuales</h2>
  <div class="grid">
    <div class="field">
      <label>Cliente</label>
      <select><option>Paco Martínez López</option></select>
    </div>
    <div class="field">
      <label>Serie de numeración</label>
      <select><option>F - Formato MES</option></select>
    </div>
    <div class="field">
      <label>Año</label>
      <input type="number" value="2026">
    </div>
    <div class="row">
      <div class="field">
        <label>Mes inicio</label>
        <select><option>enero</option><option selected>febrero</option></select>
      </div>
      <div class="field">
        <label>Mes fin</label>
        <select><option>diciembre</option></select>
      </div>
    </div>
    <div class="field">
      <label>Día del mes</label>
      <div class="row">
        <input type="number" value="15" style="width:80px">
        <label><input type="radio" name="dia" checked> Día fijo</label>
        <label><input type="radio" name="dia"> Primer día</label>
        <label><input type="radio" name="dia"> Último día</label>
      </div>
    </div>
    <div class="field">
      <label>Tipo de IVA</label>
      <select><option>21 %</option></select>
    </div>
    <div class="field">
      <label>Retención IRPF</label>
      <select><option>15 % IRPF</option><option>-- Sin retención --</option></select>
    </div>
  </div>

  <div class="lines">
    <h3>Líneas de cada factura</h3>
    <table>
      <thead>
        <tr><th>Cant.</th><th>Descripción</th><th>Precio</th><th>Añadir mes</th></tr>
      </thead>
      <tbody>
        <tr>
          <td><input type="number" value="1" style="width:50px"></td>
          <td><input type="text" value="contabilidad y laboral" style="width:100%"></td>
          <td><input type="number" value="60.00" style="width:80px"></td>
          <td><input type="checkbox" checked></td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="actions">
    <button class="secondary">Cancelar</button>
    <button class="primary">Generar 11 facturas</button>
  </div>
</div>
</body>
</html>
```
