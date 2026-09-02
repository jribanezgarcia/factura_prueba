## 1. Validadores
- [ ] 1.1 Validador de codigo postal: cinco digitos, dos primeras cifras entre 01 y 52, vacio NO valido
- [ ] 1.2 Validador de email: vacio SI valido, con contenido un patron razonable
- [ ] 1.3 Tests de ambos, al estilo de `DocumentoFiscalValidatorTest`, incluyendo los limites de provincia
- [ ] 1.4 Comprobar que los tests fallan antes de implementar los validadores

## 2. Tema en los dialogos
- [ ] 2.1 Helper en `Dialogos` que aplique tema y clase de tarjeta a un `DialogPane`
- [ ] 2.2 Usarlo en error, info, confirmar, confirmarCambiosSinGuardar y modoGuardarVersion
- [ ] 2.3 Completar `.dialog-card` en `base.css` con fondo y borde derivados del tema
- [ ] 2.4 Revisar el resultado en un tema claro y en uno oscuro

## 3. Ficha de cliente
- [ ] 3.1 Ensanchar el dialogo al orden del doble
- [ ] 3.2 `ColumnConstraints` con hgrow y `maxWidth` infinito para que los campos se estiren de verdad
- [ ] 3.3 Direccion ocupando la fila entera
- [ ] 3.4 Aplicar el helper de tema al dialogo
- [ ] 3.5 `initOwner` a la ventana principal

## 4. Validacion en el formulario
- [ ] 4.1 CP con el mismo patron que el NIF: borde rojo, aviso, revalidacion al perder el foco y bloqueo del guardado
- [ ] 4.2 Email igual, pero admitiendo el blanco
- [ ] 4.3 No tocar la validacion de NIF, que ya funciona

## 5. Verificacion
- [ ] 5.1 Suite completa en verde
- [ ] 5.2 Alta con NIF, CP y email invalidos: no deja guardar
- [ ] 5.3 Email en blanco: si deja guardar. CP en blanco: no deja
- [ ] 5.4 Direccion larga legible entera
- [ ] 5.5 Editar un cliente antiguo sin CP y confirmar que el comportamiento es el esperado

## 6. Cierre
- [ ] 6.1 Anotar en CONTINUAR_MAÑANA.md el buscador de codigos postales como idea futura
- [ ] 6.2 /opsx-sync-specs y /opsx-archive
- [ ] 6.3 Commit y push
