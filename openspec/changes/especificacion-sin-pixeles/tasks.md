> Este change **no toca código**: solo la especificación. No hay que compilar ni ejecutar tests.

## 1. Comprobaciones antes de archivar

- [ ] 1.1 Los 20 requisitos de `## REMOVED Requirements` existen en `openspec/specs/invoicing/spec.md` con **exactamente** ese título (compruébalo con `openspec show invoicing --type spec --json --requirements --no-scenarios`).
- [ ] 1.2 El requisito nuevo «Apariencia de la interfaz» **no** existe ya en el spec vivo.
- [ ] 1.3 `openspec validate especificacion-sin-pixeles --strict` sin errores.
- [ ] 1.4 `git status --short` solo muestra la carpeta de este change y `ESTADO.md`.

## 2. Estado

- [ ] 2.1 Añadir este change a la sección «En curso» de `ESTADO.md`, con una línea de qué cambia.

## 3. Después de archivar

- [ ] 3.1 `openspec show invoicing --type spec --json --requirements --no-scenarios` devuelve **35 requisitos**, con «Apariencia de la interfaz» entre ellos y sin ninguno de los 20 retirados.
- [ ] 3.2 `openspec validate --all` sin errores.
- [ ] 3.3 `grep -n "Apple\|Microinteracciones\|Sombreado uniforme" openspec/specs/invoicing/spec.md`: sin resultados.
- [ ] 3.4 Pasar el change a «Hecho» en `ESTADO.md` y actualizar «Qué toca ahora».

## 4. Pruebas manuales

- [ ] 4.1 Ninguna: este change no cambia el programa. Basta con abrir la aplicación una vez y comprobar que sigue viéndose igual.
