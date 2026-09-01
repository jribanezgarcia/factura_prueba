## Why
`VentanaTransicionTest` pasaba igual con el codigo anterior al fix de tamano de ventana, comprobado revirtiendo `VentanaConfig` y ejecutando el test. No protegia contra una regresion.

El sintoma en pantalla (ventana clavada a 760x520 al entrar en el menu) solo se daba en el primary stage de la aplicacion: en un Stage creado dentro de un test la ventana nativa si crece aunque los maximos de Arranque sigan puestos, asi que ninguna asercion de tamano puede distinguir el codigo bueno del malo. Lo que si es comprobable de forma determinista son las dos causas de codigo.

## What Changes
- `VentanaTransicionTest` comprueba que `ARRANQUE` no impone `maxWidth`/`maxHeight`, que es la restriccion que heredaba la vista siguiente y recortaba la ventana.
- Test nuevo `navegarNoDesmaximizaLaVentana`: la ventana maximizada sigue maximizada tras navegar a otra vista.
- Javadoc de la clase corregido para no prometer que el test reproduce el sintoma visual.

Con el codigo anterior (`9ddd14f`) las dos aserciones nuevas fallan; con el actual pasan.

## Capabilities
### New Capabilities
- Ninguna.
### Modified Capabilities
- Ninguna (solo cobertura de tests del requisito "Tamanos de ventana por vista").

## Impact
- src/test/java/com/alcazaba/facturacion/ui/VentanaTransicionTest.java
