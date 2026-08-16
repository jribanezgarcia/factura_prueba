## Context

El NIF se captura tanto al alta o edición de clientes como dentro del editor de facturas. El dato de cliente de una factura se guarda además como snapshot de versión, por lo que debe rechazarse antes de persistir.

## Design

Se añadirá una utilidad sin estado, `DocumentoFiscalValidator`, que normalice el texto (espacios eliminados y mayúsculas) y distinga:

- DNI: ocho dígitos y letra calculada por módulo 23.
- NIE: prefijo X, Y o Z convertido respectivamente a 0, 1 o 2, siete dígitos y letra calculada por módulo 23.
- NIF/CIF de entidad: prefijo permitido, siete dígitos de cuerpo y carácter de control calculado por el algoritmo oficial; se aceptará letra o dígito cuando el tipo de entidad lo permita.

El campo vacío se considera válido para conservar el comportamiento actual de NIF opcional. Cualquier formato no reconocido se considera inválido.

La UI validará al recibir una acción Enter o perder el foco. Si es inválido se mostrará un aviso junto al campo y se devolverá el foco al NIF. El mismo validador se usará antes de guardar como salvaguarda, para que ninguna ruta de guardado persista un documento inválido.

## Testing

Pruebas unitarias con DNI, NIE y NIF/CIF válidos e inválidos, incluida la normalización de minúsculas. Pruebas de UI para que el alta de cliente y el editor rechacen un NIF inválido no vacío al guardar.
