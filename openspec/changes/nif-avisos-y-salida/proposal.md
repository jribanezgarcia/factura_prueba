## Why

Al probar la aplicación a mano (15/09/2026) salieron tres problemas con los datos del cliente:

- **El mismo aviso para dos errores distintos.** Un NIF sin forma de documento (`123`) y uno con la letra equivocada (`12345678A`) muestran igual «Revise el DNI, NIE o NIF/CIF introducido», porque el validador solo responde sí o no.
- **El usuario se queda atrapado en el campo.** Al salir del NIF (o del email) con un dato mal, salta un aviso y el foco vuelve al campo, así que el clic en Cancelar o Volver se pierde y no se puede salir ni perdiendo los datos. La spec lo pide así («mantiene el foco en el campo»).
- **Faltan datos obligatorios.** El NIF es opcional y una factura se puede guardar sin cliente, cuando en una factura completa el destinatario, su NIF y su domicilio son necesarios.

## What Changes

- **Datos obligatorios del cliente**: nombre, NIF, dirección, código postal, localidad y provincia. El email sigue siendo opcional, pero si se escribe con formato erróneo avisa. Las etiquetas de los obligatorios llevan asterisco, en la ficha de cliente y en el Editor.
- **Avisos distintos para el NIF**:
  - vacío: «El NIF/NIE es obligatorio.»;
  - formato incorrecto: «Formato NIF/NIE incorrecto. Debe ser como 12345678Z (DNI), X1234567L (NIE) o B12345674 (CIF).»;
  - letra o carácter final incorrecto: «La letra no es correcta.».
- **Sin quedarse atrapado**: al salir de un campo con un dato mal, el campo solo se pone en rojo. El aviso sale al pulsar Enter en el campo o al Guardar, una sola vez. Cancelar y Volver funcionan siempre.
- **Factura sin cliente no permitida**: toda factura nueva o editada necesita cliente con sus datos obligatorios.
- **La regla vive en el negocio**: una comprobación única que usan la ficha de cliente, el Editor, la generación mensual y las rectificativas, lanzando `ValidacionException` con el mensaje de cada caso.
- **Datos de prueba**: el cliente de demostración con CIF no válido (`B77777777`) y los tests que usaban NIF no válidos por descuido pasan a NIF válidos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

- `invoicing`: el requisito «Clientes» pasa de NIF opcional a datos obligatorios, define los avisos y deja de retener el foco en el campo.

## Impact

- Código: `utilidades/ValidadorDocumentoFiscal`, nuevo `modelo/negocio/ValidacionCliente`, `modelo/negocio/Clientes`, `modelo/negocio/Facturas`, `modelo/negocio/FacturacionMensual`, `vista/controlador/ClientesController`, `vista/controlador/EditorController`, `vista/recursos/Editor.fxml`.
- Datos: `src/main/resources/db/seed_demo.sql`.
- Tests: `ValidadorDocumentoFiscalTest`, nuevo `ValidacionClienteTest`, `FacturasTest`, `FacturacionMensualTest`, `EstadosTest`, `HistorialTest`, `CopiaSeguridadTest`, `ClientesValidacionNifTest`, `EditorValidacionNifTest`, `EditorIvaInactivoTest`.
- Fuera: los datos de empresa (Configuración ya exige NIF y demás, no cambia), `Rectificativas` (queda cubierta por `Facturas`), pasar la ficha de cliente a FXML y el resto de construcciones que prohíbe `AGENTS.md` fuera de las líneas tocadas.
- No hay migración: el programa está en desarrollo y los datos se van a reiniciar.
