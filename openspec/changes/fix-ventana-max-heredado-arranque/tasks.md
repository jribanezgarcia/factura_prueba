## 1. Fix de VentanaConfig
- [x] 1.1 Quitar los maximos heredables de `ARRANQUE`
- [x] 1.2 Unificar `aplicar` en un camino determinista (liberar maximos, resizable, minimos, maximos, tamano)
- [x] 1.3 Decidir el redimensionado comparando la configuracion previa guardada en el Stage, no `getWidth()`
- [x] 1.4 Maximizar solo cuando la vista lo pide

## 2. Fix de Main
- [x] 2.1 Quitar el dimensionado de `entrarEnMenu`
- [x] 2.2 Ocultar y volver a mostrar el Stage en la transicion Arranque -> Menu
- [x] 2.3 Recuperar el tamano guardado cuando supera el minimo de la vista

## 3. Tests
- [x] 3.1 Reescribir `VentanaTransicionTest` (transicion y maximos liberados)
- [x] 3.2 Anadir caso de conservacion del tamano del usuario al navegar

## 4. Verificacion
- [x] 4.1 mvn test (107 tests)
- [x] 4.2 Comprobacion visual con lanzar.bat

## 5. Cierre
- [x] 5.1 Actualizar CONTINUAR_MAÑANA.md
- [ ] 5.2 Commit y push
