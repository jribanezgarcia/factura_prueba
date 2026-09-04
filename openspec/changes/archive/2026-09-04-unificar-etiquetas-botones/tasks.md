## 1. Comprobación previa

- [x] 1.1 Buscar en `src/test` los literales que se van a cambiar («Borrar», «Generar mensual», «Crear copia de seguridad», «Restaurar copia») por si alguno aparece en un assert, sobre todo en `UiSmokeTest`.
- [x] 1.2 Anotar aquí los tests afectados, si los hay. → Ninguno. El único "Borrar" es un nombre de empresa en datos de test (`EmpresaManagerTest:88`).

## 2. Histórico

- [x] 2.1 «Borrar» pasa a «Eliminar».
- [x] 2.2 «Generar mensual» pasa a «Generar mensuales».
- [x] 2.3 Comprobar que el menú contextual de la tabla (`HistoricoController:150-158`) usa los mismos términos que los botones: hoy dice «Anular facturas seleccionadas» y «Borrar facturas seleccionadas». → Cambiado a «Eliminar facturas seleccionadas».

## 3. Menú principal

- [x] 3.1 «Generar facturas mensuales» pasa a «Generar mensuales», dejando la descripción larga en el subtítulo de la tarjeta, que es donde cabe.

## 4. Copia de seguridad

- [x] 4.1 «Crear copia de seguridad» pasa a «Crear copia».
- [x] 4.2 «Restaurar copia» pasa a «Restaurar».
- [x] 4.3 Comprobar que ambas siguen siendo inequívocas dentro de sus tarjetas, que ya se titulan «Copia de seguridad» y «Restaurar una copia». → Confirmado.

## 5. Revisión del resto

- [x] 5.1 Clientes: «Nuevo», «Editar», «Eliminar», «Volver» ya cumplen el criterio; confirmar que no hay que tocar nada. → Confirmado.
- [x] 5.2 Generar mensuales: «Cancelar» **se conserva** por ser un diálogo modal; confirmar que es el único sitio de la aplicación donde aparece. → Confirmado.
- [x] 5.3 Configuración: revisar «Nuevo», «Guardar» e «Inactivar/Activar» en las secciones de IVA, Retenciones y Series. → Todo correcto.

## 6. Especificación

- [x] 6.1 ADDED «Criterio de etiquetado de botones», con el criterio de D1 y escenarios para «Eliminar» frente a «Borrar» y para «Volver» frente a «Cancelar».

## 7. Verificación final

- [x] 7.1 Suite completa en verde con `mvn test` (165 tests, 0 fallos).
- [x] 7.2 Repaso visual de Histórico, Menú principal, Copias, Clientes y Generar mensuales.
