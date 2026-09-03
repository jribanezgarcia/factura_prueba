## 1. Recurso del icono

- [x] 1.1 Copiar `logos/logo1.png` → `src/main/resources/com/alcazaba/facturacion/images/icono-aplicacion.png`.

## 2. Helper de icono y marca

- [x] 2.1 Crear `ui/Ventanas` con constante `PREFIJO = "CaboFactu® "` y `aplicarIcono(Stage)` que cargue el recurso `images/icono-aplicacion.png` (por classpath, defensivo si falta) y llame `stage.getIcons().add(...)`.

## 3. Ventana principal: icono y título por pantalla

- [x] 3.1 `Main.configurarVentana`: aplicar el icono con `Ventanas.aplicarIcono(stage)` y fijar el título inicial como `Ventanas.PREFIJO + "Seleccion de empresa"` (la app arranca en la pantalla de selección).
- [x] 3.2 `VentanaConfig`: añadir campo `titulo` por vista (ARRANQUE, MENU, EDITOR, CONFIGURACION, HISTORICO, CLIENTES, VERSIONES, BACKUP, GENERAR_MENSUAL).
- [x] 3.3 `Navegador.mostrar`: tras `VentanaConfig.para(fxml)` aplicar la config, fijar `stage.setTitle(Ventanas.PREFIJO + cfg.titulo())`, siempre que exista config para el FXML.

## 4. Ventana secundaria

- [x] 4.1 `GenerarFacturasMensualesController`: al crear `Stage dialog`, aplicar `Ventanas.aplicarIcono(dialog)` y titularlo `Ventanas.PREFIJO + "Generar facturas mensuales"`.

## 5. Pruebas

- [x] 5.1 Añadir (o ampliar) test de ventana que cargue `MenuPrincipal` y verifique `stage.getTitle()` == `"CaboFactu® Menu Principal"` y que el `Stage` tiene al menos un icono.
- [x] 5.2 Ejecutar la suite completa (`mvn -o test` desde el proyecto) y confirmar todos los tests en verde (155 + nuevos).

## 6. Cierre

- [x] 6.1 Sincronizar la spec delta con `openspec/specs/invoicing/spec.md`.
- [x] 6.2 Archivar el change y actualizar CONTINUAR_MAÑANA.md.
- [x] 6.3 Commit y push.
