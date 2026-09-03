# Proyecto: Aplicación de facturación de escritorio

## Instrucciones para OpenCode / OpenSpec

**Regla obligatoria:** todo el trabajo en este proyecto se realiza SIEMPRE
con el flujo OpenSpec (`/opsx-propose` → `/opsx-apply` →
`/opsx-sync` → `/opsx-archive`). No se toca código ni la spec
fuera de ese flujo. El CLI `openspec` ya está instalado.

Este documento describe el objetivo y los requisitos conocidos de una
nueva aplicación de facturación. Se utilizará como contexto para
`/opsx-explore`.

**Importante:** en esta fase NO quiero que implementes código, NO quiero
que crees todavía la arquitectura definitiva ni que empieces a
programar. Quiero explorar y entender bien el problema, detectar
contradicciones o decisiones importantes que falten y preparar el
terreno para `/opsx-propose`.

La aplicación debe ser una primera versión sencilla y funcional. No hay
que sobrediseñarla.

------------------------------------------------------------------------

# 1. Objetivo

Quiero sustituir una hoja de cálculo que utilizo actualmente para crear
facturas una a una por una aplicación de escritorio para Windows.

La aplicación debe permitir:

-   Crear facturas.
-   Editarlas.
-   Guardarlas en una base de datos local.
-   Mantener un histórico completo.
-   Mantener versiones de las facturas.
-   Buscar facturas.
-   Generar/exportar las facturas a PDF.
-   Crear facturas rectificativas.
-   Mantener todo localmente, sin servidor.
-   Ser sencilla y rápida de utilizar.

Actualmente el usuario domina Java y SQL. Java es el lenguaje principal
de desarrollo.

Se ha proporcionado como referencia una factura real en Excel:
`factura alcazaba factura C-57-7 2026.xlsx`

No se debe implementar todavía la importación de ese Excel. La
importación de facturas antiguas será una funcionalidad futura.

------------------------------------------------------------------------

# 2. Tecnología

Tecnología decidida:

-   Java 21 LTS.
-   JavaFX.
-   Maven.
-   SQLite como base de datos local.
-   JDBC para acceso a SQLite.
-   Arquitectura MVC con capas de Service y Repository.
-   Aplicación de escritorio Windows.
-   Un único usuario.
-   Un único ordenador.
-   Solo una instancia de la aplicación ejecutándose a la vez.
-   Idioma: español.
-   Moneda: euro (€).

No utilizar frameworks innecesarios como Spring Boot, Hibernate/JPA,
etc., salvo que durante la exploración exista una razón muy clara.

La base de datos debe estar en una carpeta de datos de la aplicación,
separada de la instalación.

------------------------------------------------------------------------

# 3. Arquitectura deseada

Se busca una arquitectura sencilla pero mantenible:

-   View / JavaFX / FXML
-   Controller
-   Service
-   Repository / JDBC
-   Model / entidades

Los Controllers no deben contener la lógica de negocio importante.

Los Services deben encargarse de reglas como:

-   numeración;
-   versionado;
-   cálculo de IVA;
-   descuentos;
-   creación de rectificativas;
-   anulaciones;
-   validaciones;
-   operaciones transaccionales.

Los Repositories deben encargarse del acceso a SQLite.

Las operaciones importantes de persistencia deben utilizar transacciones
y hacer COMMIT/ROLLBACK correctamente.

------------------------------------------------------------------------

# 4. Clientes

Debe existir una gestión básica de clientes.

Datos del cliente:

-   Nombre / razón social.
-   NIF.
-   Dirección.
-   Código postal.
-   Localidad.
-   Provincia.

Al crear una factura se podrá buscar el cliente por:

-   Nombre / razón social.
-   NIF.

La búsqueda debe ser incremental mientras se escribe.

Al seleccionar un cliente, sus datos se cargan en la factura.

Los datos del cliente pueden modificarse desde la factura y esos cambios
deben actualizar también la ficha general del cliente.

Si un cliente tiene facturas asociadas:

-   no podrá eliminarse físicamente;
-   podrá marcarse como inactivo.

Un cliente inactivo:

-   no debe aparecer normalmente al crear nuevas facturas;
-   sí debe seguir apareciendo en el histórico;
-   sus facturas deben seguir siendo consultables.

Si un cliente no tiene facturas asociadas, puede eliminarse físicamente.

------------------------------------------------------------------------

# 5. Facturas normales

La factura debe ser sencilla de introducir, similar a trabajar con
Excel.

Datos principales:

-   Número de factura.
-   Fecha.
-   Cliente.
-   Líneas.
-   Descuento general.
-   IVA.
-   Observaciones.
-   Totales.

La cantidad de cada línea:

-   es un número entero;
-   empieza por defecto en 1.

Cada línea tendrá:

-   Cantidad.
-   Descripción.
-   Precio unitario.
-   Total.
-   IVA.

El usuario debe poder:

-   añadir líneas;
-   eliminar líneas;
-   eliminar con botón;
-   eliminar con Delete/Supr;
-   mantener el orden de introducción de las líneas.

No hay límite artificial de líneas.

La descripción puede ser larga y ocupar varias líneas en el PDF.

Flujo de teclado deseado:

Cantidad → Enter → Descripción → Enter → Precio unitario/Total → Enter →
siguiente línea.

Al pulsar Enter en Total:

-   si la línea está completa, se crea la siguiente;
-   si falta un dato necesario, no se crea todavía.

------------------------------------------------------------------------

# 6. Precios y cálculos

Se utilizará `BigDecimal` para importes y cálculos decimales.

No utilizar `double`/`float` para cálculos monetarios.

El redondeo acordado es:

`RoundingMode.HALF_UP`

Los importes mostrados en pantalla/PDF tendrán 2 decimales.

El precio unitario puede tener más precisión internamente aunque se
muestre con 2 decimales.

El usuario debe poder modificar tanto:

-   Precio unitario
-   Total de línea

Si cambia el precio unitario, se recalcula el total.

Si cambia el total, se recalcula el precio unitario.

El precio/total debe tener en cuenta el IVA correspondiente.

------------------------------------------------------------------------

# 7. IVA

El IVA será configurable.

El 21% será el tipo utilizado por defecto.

Cada línea puede tener un tipo de IVA diferente.

Debe existir:

-   tipos porcentuales;
-   IVA exento.

Los tipos de IVA podrán:

-   crearse;
-   modificarse mientras sea seguro hacerlo;
-   marcarse como inactivos si ya se han utilizado;
-   no eliminarse físicamente cuando formen parte del histórico.

Para IVA exento se debe poder indicar un motivo/texto de exención.

El resumen de una factura debe desglosar cada tipo de IVA por separado.

Ejemplo conceptual:

Base 21% → IVA 21% Base 10% → IVA 10% Base exenta → IVA 0%

------------------------------------------------------------------------

# 8. Descuento

La factura tendrá un descuento general.

El descuento:

-   se aplica sobre la factura, no sobre cada línea;
-   siempre será porcentual;
-   será un porcentaje entero;
-   empieza siempre en 0%;
-   no habrá descuento predeterminado configurable.

El descuento se aplica antes del cálculo del IVA.

Si existen varios tipos de IVA, el cálculo debe mantener correctamente
las bases de cada tipo.

------------------------------------------------------------------------

# 9. Numeración

Existen inicialmente tres series:

-   C = Cocinas
-   P = Puertas
-   R = Rectificativas

Las series C y P tienen formato:

`CODIGO-NUMERO-MES`

Ejemplos:

-   `C-57-7`
-   `C-58-7`
-   `P-35-8`

El número correlativo es independiente del mes.

La serie R tiene formato:

`R-NUMERO`

Ejemplos:

-   `R-1`
-   `R-2`

Cada serie tiene su contador independiente.

Las series serán configurables desde la aplicación para poder añadir
nuevas series normales en el futuro.

Las nuevas series normales seguirán el formato:

`CODIGO-NUMERO-MES`

La serie R es especial y no utiliza el mes.

Las series NO tienen que crearse automáticamente al instalar: el usuario
podrá configurarlas desde Configuración.

En Configuración → Series se debe poder:

-   ver las series;
-   ver el siguiente número;
-   modificar el siguiente número.

La aplicación recordará la última serie utilizada y la propondrá
automáticamente al crear la siguiente factura.

El número se propone automáticamente, pero puede modificarse
manualmente.

El número no se consume hasta que la factura se guarda correctamente.

Si se introduce manualmente un número que ya pertenece a una factura
activa de la misma serie, no se permite guardar.

Los números de facturas anuladas pueden reutilizarse.

El comportamiento de reutilización de números anulados debe ser
configurable por serie: - continuar hacia delante; - o reutilizar
números anulados.

La fecha determina el último componente de las series normales.

Ejemplo: - fecha 11/08/2026 → `C-58-8` - fecha 25/07/2026 → `C-58-7`

La fecha de factura es editable mediante un selector/calendario.

------------------------------------------------------------------------

# 10. Fecha de trabajo

Al abrir la aplicación, la fecha de trabajo será la fecha del sistema.

El usuario puede cambiarla.

La fecha de trabajo se utiliza como valor inicial de la fecha de nuevas
facturas.

Al cambiar la fecha de una factura, el mes del número propuesto se
actualiza automáticamente.

El número definitivo se valida y consume al guardar.

------------------------------------------------------------------------

# 11. Versionado

Las facturas pueden editarse siempre que estén emitidas.

Cada guardado de cambios crea una nueva versión.

Las versiones anteriores no se sobrescriben.

Cada factura tiene su propio contador:

-   v1
-   v2
-   v3
-   etc.

Cada versión guarda:

-   todos los datos completos de la factura;
-   fecha de factura;
-   fecha/hora en que se creó esa versión.

No hace falta guardar un resumen de diferencias entre versiones.

Cualquier versión puede abrirse.

Si se edita una versión anterior, al guardar se crea una nueva versión a
partir de esa versión, sin modificar la versión histórica.

Ejemplo:

C-57-7: - v1 - v2 - v3 - v4 creada editando v1

El histórico mostrará cada versión como una fila independiente.

------------------------------------------------------------------------

# 12. Estados

Solo habrá dos estados:

-   Emitida
-   Anulada

No habrá estado Borrador en la V1.

Una factura emitida puede editarse.

Una factura anulada:

-   no puede editarse;
-   puede consultarse;
-   puede exportarse a PDF;
-   puede restaurarse a Emitida;
-   requiere confirmación para anularse/restaurarse.

Si se intenta restaurar una factura anulada y su número está ocupado por
otra factura activa, no se permitirá restaurarla.

Si se restaura una versión emitida anterior de una factura anulada, se
creará una nueva versión Emitida.

------------------------------------------------------------------------

# 13. Rectificativas

Las rectificativas utilizan la serie independiente:

`R-1`, `R-2`, etc.

No distinguen entre cocina y puerta.

Una rectificativa debe indicar qué factura rectifica.

La referencia se genera automáticamente, pero puede modificarse
manualmente.

Las rectificativas se crean desde una factura existente.

Al crear una rectificativa:

-   se copian los datos de la factura original;
-   se copia cliente;
-   se copian líneas;
-   cantidades;
-   descripciones;
-   precios;
-   IVA;
-   descuento;
-   observaciones;
-   referencia a la factura original.

Después puede modificarse libremente.

La fecha de una rectificativa será inicialmente la fecha de trabajo
actual, aunque se podrá cambiar.

Una rectificativa puede ser parcial o total.

En esta V1, las rectificativas se crean desde una factura existente; no
hace falta una opción independiente de "Nueva rectificativa" en el menú
principal.

------------------------------------------------------------------------

# 14. Observaciones

Todas las facturas tendrán un campo libre de Observaciones.

En las rectificativas se podrá utilizar para indicar la factura
rectificada.

------------------------------------------------------------------------

# 15. Histórico

Debe existir un histórico de facturas.

El histórico permitirá buscar por:

-   Serie.
-   Cliente / razón social.
-   NIF.
-   Fecha desde/hasta.
-   Importe desde/hasta.
-   Estado.

Los filtros se combinan entre sí.

La búsqueda se ejecuta mediante botón "Buscar", no en tiempo real.

Los resultados se ordenarán por número de factura.

Cada versión aparece como una fila independiente.

Columnas:

-   Fecha.
-   Número.
-   Versión.
-   Cliente.
-   NIF.
-   Base.
-   IVA.
-   Total.
-   Estado.

Al seleccionar una factura/versión se podrá abrir directamente.

------------------------------------------------------------------------

# 16. Menú principal

La aplicación debe tener un menú principal sencillo y moderno.

Opciones:

-   Nueva factura.
-   Histórico.
-   Configuración.
-   Copia de seguridad.
-   Salir.

No habrá una opción "Nueva rectificativa" en el menú principal.

Dentro de una factura habrá una barra superior aproximadamente:

-   Guardar.
-   Exportar PDF.
-   Versiones.
-   Crear rectificativa.
-   Nueva factura.
-   Volver.

Solo se tendrá una factura abierta a la vez.

------------------------------------------------------------------------

# 17. Cambios sin guardar

Si hay cambios sin guardar y el usuario pulsa Volver o cierra la
aplicación:

-   Guardar y volver/salir.
-   Descartar cambios y volver/salir.
-   Cancelar.

Si no hay cambios, se puede salir normalmente.

------------------------------------------------------------------------

# 18. Atajos de teclado

Atajos básicos:

-   Ctrl+N → Nueva factura.
-   Ctrl+S → Guardar.
-   Ctrl+F → Buscar.
-   Ctrl+P → Exportar PDF.
-   Esc → volver/cancelar cuando corresponda.

------------------------------------------------------------------------

# 19. Configuración

Debe existir una pantalla de Configuración.

Debe permitir configurar:

## Empresa

-   Nombre.
-   NIF.
-   Dirección.
-   Código postal.
-   Localidad.
-   Provincia.
-   Resto de datos necesarios para la cabecera.

## Cabecera

Dos modos: - Texto con datos de empresa. - Imagen/logo.

El logo: - se selecciona desde un archivo; - permite ajustar tamaño y
posición.

Los datos de empresa siempre se guardan aunque la cabecera visible
utilice solo el logo.

## Pie

-   Texto legal configurable.
-   Debe poder utilizarse el texto legal que aparece actualmente en el
    Excel.

## IVA

-   Gestión de tipos de IVA.

## Series

-   Crear/configurar series.
-   Ver y modificar siguiente número.
-   Configurar comportamiento de reutilización de números anulados.

## PDFs

-   Carpeta automática de almacenamiento.
-   Última carpeta utilizada para exportación.

La aplicación recordará preferencias de trabajo como: - última serie
utilizada; - tamaño/posición de ventana; - última carpeta de
exportación.

------------------------------------------------------------------------

# 20. PDF

Las facturas se exportarán a PDF.

Formato: - A4 vertical. - Diseño moderno y profesional. - Inspirado en
la información y estructura del Excel actual, pero no tiene que copiarlo
exactamente.

Cabecera: - texto de empresa; - o imagen/logo.

Pie: - texto legal configurable.

El pie legal se repite en todas las páginas.

Si hay varias páginas: - se repite la cabecera; - se repite el pie; -
aparece `Página X de Y`.

Las descripciones largas se ajustan automáticamente.

Los importes utilizan formato español:

`1.250,50 €`

Las fechas utilizan formato español:

`11/08/2026`

Una factura anulada se puede exportar y debe aparecer claramente marcada
como:

`ANULADA`

Si se exporta una versión concreta, el contenido debe corresponder
exactamente a esa versión.

El PDF utiliza la configuración actual de: - empresa; - logo; -
cabecera; - pie legal.

Los PDFs ya generados permanecen como documentos independientes.

Estructura de almacenamiento propuesta:

Facturas/ - 2026/ - C/ - P/ - R/ - 2027/ - C/ - P/ - R/

Nombre propuesto:

`C-57-7_v1.pdf`

La librería concreta de PDF queda abierta para la fase de diseño
técnico.

------------------------------------------------------------------------

# 21. Base de datos y datos

SQLite será un archivo local.

La ubicación estará en una carpeta de datos de la aplicación, separada
de la instalación.

La aplicación será la vía normal para modificar los datos.

La base de datos contendrá el histórico completo.

Las operaciones importantes serán transaccionales.

No se deben eliminar físicamente datos históricos que hayan sido
utilizados.

------------------------------------------------------------------------

# 22. Copia de seguridad

Debe existir un botón para crear una copia de seguridad manual.

En la V1 la copia de seguridad será únicamente del archivo SQLite.

No es necesario incluir PDFs ni configuración en esta primera versión.

------------------------------------------------------------------------

# 23. Distribución futura

Cuando la aplicación esté completamente terminada y probada:

-   instalador Windows `.exe`;
-   versión portable.

Esto NO forma parte de la primera fase de desarrollo.

Las futuras actualizaciones deben conservar: - base de datos; - PDFs; -
configuración.

La instalación de una nueva versión no debe sobrescribir la carpeta de
datos.

------------------------------------------------------------------------

# 24. Funcionalidades futuras, fuera de V1

Estas funcionalidades NO deben implementarse ahora:

1.  Importación de facturas históricas desde Excel.
2.  Catálogo de productos/servicios.
3.  Exportación de facturas a Excel/CSV.

Se podrán añadir posteriormente.

------------------------------------------------------------------------

# 25. Principios de diseño

Prioridades:

1.  Sencillez.
2.  Rapidez para crear una factura.
3.  Seguridad del histórico.
4.  Código mantenible.
5.  No sobrediseñar.
6.  Experiencia parecida a Excel para introducir líneas.
7.  Arquitectura limpia pero apropiada para una aplicación pequeña.

No convertir esta aplicación en un ERP.

------------------------------------------------------------------------

# 26. Qué quiero de `/opsx-explore`

En esta fase quiero que OpenCode:

1.  Analice estos requisitos.
2.  Detecte contradicciones o decisiones que realmente bloqueen el
    diseño.
3.  Revise el proyecto actual si ya existe código.
4.  Identifique los principales riesgos técnicos.
5.  Proponga las preguntas mínimas que aún sean imprescindibles.
6.  No implemente código.
7.  No cree todavía la solución definitiva.
8.  No pase todavía a `/opsx-propose`.

Si faltan detalles menores, elige la opción más sencilla y razonable
para una V1 en lugar de preguntarme por cada pequeño detalle.

El objetivo de esta fase es terminar con una comprensión clara del
producto y estar preparados para pasar después a `/opsx-propose`.
