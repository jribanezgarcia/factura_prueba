## Why

Los NIF introducidos para clientes pueden contener una letra de control incorrecta y la aplicación los guarda sin aviso. Esto afecta a la calidad de los datos que aparecen en facturas y PDFs.

## What Changes

- Validar documentos españoles de persona física (DNI y NIE) y de entidad (NIF/CIF) mediante sus dígitos o letras de control.
- Avisar al abandonar con Enter o con pérdida de foco un NIF no vacío e inválido en el editor de factura y en el alta/edición de clientes.
- Impedir guardar esos formularios mientras el NIF no vacío sea inválido.
- Mantener permitido dejar el campo NIF vacío.

## Impact

- Nueva utilidad de dominio para la validación de identificadores españoles y sus pruebas unitarias.
- Cambios de UI en `EditorController` y `ClientesController`.
- Se modifica la capacidad existente `invoicing`; no requiere migración de base de datos.
