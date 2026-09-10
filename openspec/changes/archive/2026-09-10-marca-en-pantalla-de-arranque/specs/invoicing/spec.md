## ADDED Requirements

### Requirement: Marca en la pantalla de arranque

La pantalla de selección de empresa SHALL identificarse con la marca de la aplicación, «CaboFactu®», en lugar de con una descripción genérica de su función.

Junto a ese rótulo SHALL mostrarse el icono de la aplicación, el mismo que aparece en la barra de título y en la barra de tareas, situado a su izquierda y en la misma línea. El icono y el rótulo SHALL presentarse centrados como un único conjunto en la parte superior de la pantalla.

El conjunto de marca SHALL caber dentro del tamaño fijo de la pantalla de arranque sin desplazar ni recortar la tarjeta de selección de empresa, ejercicio y fecha de trabajo, ni el mensaje de error.

Los controles de la pantalla, su disposición y el flujo de selección SHALL permanecer sin cambios: la modificación es exclusivamente de identidad visual.

#### Scenario: La portada muestra la marca
- **WHEN** el usuario abre la aplicación y aparece la pantalla de selección de empresa
- **THEN** la pantalla muestra «CaboFactu®» como rótulo, no una descripción genérica de la función del programa

#### Scenario: El icono acompaña al rótulo
- **WHEN** el usuario mira la parte superior de la pantalla de arranque
- **THEN** el icono de la aplicación aparece a la izquierda del rótulo, en la misma línea, y ambos quedan centrados como conjunto

#### Scenario: El contenido sigue cabiendo
- **WHEN** el usuario abre la pantalla de arranque, que es de tamaño fijo y no redimensionable
- **THEN** la tarjeta de selección de empresa, ejercicio y fecha de trabajo se muestra completa, sin desplazarse ni recortarse, igual que antes del cambio

#### Scenario: El flujo no cambia
- **WHEN** el usuario selecciona empresa, ejercicio y fecha de trabajo y pulsa Entrar
- **THEN** la aplicación se comporta igual que antes del cambio
