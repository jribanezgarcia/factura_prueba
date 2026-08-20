## MODIFIED Requirements

### Requirement: Exportación a PDF

La aplicación SHALL exportar facturas a PDF en A4 vertical, con diseño moderno y profesional inspirado en la información del documento actual sin necesidad de copiarlo. La cabecera SHALL usar el texto de empresa o el logo según configuración. El pie legal SHALL ser configurable y repetirse en todas las páginas. Si hay varias páginas, SHALL repetirse la cabecera y el pie y SHALL aparecer `Página X de Y`. Las descripciones largas SHALL ajustarse automáticamente. Los importes SHALL usar formato español (`1.250,50 €`) y las fechas formato español (`11/08/2026`). Una factura anulada SHALL poder exportarse y SHALL aparecer claramente marcada como `ANULADA`. Si se exporta una versión concreta, el contenido SHALL corresponder exactamente a esa versión. El PDF SHALL usar la configuración actual de empresa, logo, cabecera y pie legal. Los PDF generados SHALL permanecer como documentos independientes. La estructura de almacenamiento SHALL ser `Facturas/AAAA/SERIE/` (p. ej. `Facturas/2026/C/`) y el nombre SHALL ser `CODIGO-CORRELATIVO-MES.pdf` (p. ej. `C-59-7.pdf`), sustituyendo la barra por un guion en el nombre de archivo. El nombre del archivo SHALL NOT incluir la versión de la factura.

#### Scenario: Exportar factura de varias páginas
- **WHEN** el usuario exporta una factura con descripciones largas que ocupa varias páginas
- **THEN** el PDF repite cabecera y pie en cada página e indica `Página X de Y`

#### Scenario: Exportar factura anulada
- **WHEN** el usuario exporta una factura anulada
- **THEN** el PDF muestra la marca `ANULADA` de forma destacada

#### Scenario: Exportar versión concreta
- **WHEN** el usuario exporta una versión concreta del histórico
- **THEN** el PDF refleja exactamente los datos de esa versión

#### Scenario: Nombres de archivo
- **WHEN** el usuario exporta la factura C-59/8
- **THEN** se genera el archivo `Facturas/2026/C/C-59-8.pdf`