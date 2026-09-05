> Los valores orientativos de cada campo están en `design.md`, sección
> «D2. Reparto orientativo del recorte». Lo que vale es que la fila más
> ancha pida menos de ~740 px; los números pueden ajustarse.

## 1. Ajuste de campos en Configuracion.fxml

- [x] 1.1 Bajar los `prefWidth` de `txtNombre`, `txtActividad`, `txtDireccion`, `txtLocalidad` y `txtEmail` (sección Empresa, líneas 29-48) y verificar que el FXML sigue cargando sin errores.
- [x] 1.2 Comprobar que NIF, CP, Provincia y Teléfono se quedan con sus valores actuales.

## 2. Especificación

- [x] 2.1 MODIFIED «Configuración»: en el tamaño mínimo de ventana (1024×768), todos los campos de la sección Empresa SHALL verse completos. Escenario nuevo de campos legibles a 1024×768.

## 3. Verificación final

- [x] 3.1 Suite completa en verde con `mvn test`.
- [x] 3.2 A mano: abrir Configuración → Empresa a 1024×768 y comprobar que Nombre / razón social, Actividad y Localidad se ven enteros, sin recortes por el borde derecho.
