package cabofactu.pdf;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.GrupoIva;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.negocio.Calculos;
import cabofactu.utilidades.Formatos;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Compone el contenido del documento sin dibujar nada.
 */
public final class ConstructorDocumentoFactura {

    private static final DecimalFormat REJILLA_TIPO = new DecimalFormat("0.00",
            DecimalFormatSymbols.getInstance(new Locale("es", "ES")));

    private ConstructorDocumentoFactura() {
    }

    public static DocumentoFactura build(Factura factura, Empresa empresa, String colorHex) throws Exception {
        ResumenFactura resumen = Calculos.resumen(factura.getLineas(), factura.getDescuento(), factura.getRetencion());
        List<LineaFactura> suplidos = Calculos.suplidosDe(factura.getLineas());
        return new DocumentoFactura(
                header(factura),
                clientCard(factura),
                paymentCard(factura),
                linesTable(factura.getLineas()),
                suplidosBlock(suplidos),
                totalsBlock(resumen, factura.getDescuento()),
                observations(factura),
                legalFooter(empresa));
    }

    static DocumentoFactura.Header header(Factura factura) {
        boolean corrective = factura.getRectificaId() != null;
        Optional<String> referencia = Optional.empty();
        if (corrective) {
            referencia = Optional.of(nz(factura.getRectificaNumero()));
        }
        return new DocumentoFactura.Header(
                nz(factura.getNumero()),
                Formatos.fecha(factura.getFecha()),
                corrective,
                referencia,
                factura.getEstado() == EstadoFactura.ANULADA);
    }

    static DocumentoFactura.ClientCard clientCard(Factura factura) {
        List<DocumentoFactura.FieldRow> rows = new ArrayList<>();
        if (!nz(factura.getCliente().getNombre()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Nombre", factura.getCliente().getNombre()));
        }
        if (!nz(factura.getCliente().getNif()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("NIF", factura.getCliente().getNif()));
        }
        if (!nz(factura.getCliente().getDireccion()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Dirección", factura.getCliente().getDireccion()));
        }
        if (!nz(factura.getCliente().getCp()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Código postal", factura.getCliente().getCp()));
        }
        if (!nz(factura.getCliente().getLocalidad()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Población", factura.getCliente().getLocalidad()));
        }
        if (!nz(factura.getCliente().getProvincia()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Provincia", factura.getCliente().getProvincia()));
        }
        if (!nz(factura.getCliente().getEmail()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Email", factura.getCliente().getEmail()));
        }
        return new DocumentoFactura.ClientCard("FACTURAR A", List.copyOf(rows), "—");
    }

    static String paymentCardTitle() {
        return "DATOS DE PAGO";
    }

    static Optional<DocumentoFactura.PaymentCard> paymentCard(Factura factura) {
        List<DocumentoFactura.FieldRow> rows = paymentRows(factura);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DocumentoFactura.PaymentCard(paymentCardTitle(), rows));
    }

    static List<DocumentoFactura.FieldRow> paymentRows(Factura factura) {
        List<DocumentoFactura.FieldRow> rows = new ArrayList<>();
        if (!nz(factura.getFormaPago()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Forma de pago", factura.getFormaPago()));
        }
        if (factura.getVencimiento() != null) {
            rows.add(new DocumentoFactura.FieldRow("Vencimiento", Formatos.fecha(factura.getVencimiento())));
        }
        if (!nz(factura.getRealizadaPor()).isBlank()) {
            rows.add(new DocumentoFactura.FieldRow("Realizada por", factura.getRealizadaPor()));
        }
        return List.copyOf(rows);
    }

    static DocumentoFactura.LinesTable linesTable(List<LineaFactura> lineas) {
        List<DocumentoFactura.LineRow> rows = new ArrayList<>();
        if (lineas != null) {
            for (LineaFactura l : lineas) {
                if (l.isEsSuplido()) {
                    continue;
                }
                rows.add(new DocumentoFactura.LineRow(
                        String.valueOf(l.getCantidad()),
                        nz(l.getDescripcion()),
                        importePdf(l.getPrecioUnitario()),
                        l.isExenta() ? "Exento" : l.getIvaPorcentaje() + " %",
                        importePdf(Calculos.totalConIva(l))));
            }
        }
        return new DocumentoFactura.LinesTable(
                List.of("CANT.", "DESCRIPCIÓN", "PRECIO", "IVA %", "TOTAL"), List.copyOf(rows));
    }

    static Optional<DocumentoFactura.SuplidosBlock> suplidosBlock(List<LineaFactura> suplidos) {
        if (suplidos == null || suplidos.isEmpty()) {
            return Optional.empty();
        }
        List<DocumentoFactura.SuplidoRow> rows = new ArrayList<>();
        for (LineaFactura l : suplidos) {
            BigDecimal importe = l.getTotalBase() == null ? BigDecimal.ZERO : l.getTotalBase();
            rows.add(new DocumentoFactura.SuplidoRow(nz(l.getDescripcion()), importePdf(importe)));
        }
        return Optional.of(new DocumentoFactura.SuplidosBlock(
                List.of("SUPLIDOS", "IMPORTE"),
                List.copyOf(rows),
                "Suplidos pagados en nombre y por cuenta del cliente, facturados a su nombre. No sujetos a IVA ni a retención."));
    }

    static DocumentoFactura.TotalsBlock totalsBlock(ResumenFactura r, int descuento) {
        List<DocumentoFactura.IvaRow> rows = new ArrayList<>();
        for (GrupoIva g : r.getGrupos()) {
            rows.add(new DocumentoFactura.IvaRow(
                    g.isExento() ? "Exento" : porcentajeRejilla(g.getPorcentaje()),
                    importePdf(g.getBase()),
                    g.isExento() ? "—" : importePdf(g.getCuota())));
        }
        DocumentoFactura.IvaRow totalsRow = new DocumentoFactura.IvaRow(
                "Totales", importePdf(r.getBaseTotal()), importePdf(r.getIvaTotal()));

        Optional<DocumentoFactura.RetentionRow> retention = Optional.empty();
        if (r.getImporteRetencion() != null && r.getImporteRetencion().compareTo(BigDecimal.ZERO) > 0) {
            String etiqueta = r.getNombreRetencion() != null && !r.getNombreRetencion().isBlank()
                    ? r.getNombreRetencion() + " " + r.getPorcentajeRetencion() + " %"
                    : "Retención " + r.getPorcentajeRetencion() + " %";
            retention = Optional.of(new DocumentoFactura.RetentionRow(
                    etiqueta, "−" + importePdf(r.getImporteRetencion())));
        }
        Optional<DocumentoFactura.SuplidosTotalRow> suplidosRow = Optional.empty();
        if (r.getTotalSuplidos() != null && r.getTotalSuplidos().compareTo(BigDecimal.ZERO) > 0) {
            suplidosRow = Optional.of(new DocumentoFactura.SuplidosTotalRow(
                    "Suplidos", "+" + importePdf(r.getTotalSuplidos())));
        }
        DocumentoFactura.Liquidation liquidation = new DocumentoFactura.Liquidation(
                "LIQUIDACIÓN",
                "Base imponible", importePdf(r.getBaseTotal()),
                "Total IVA repercutido", importePdf(r.getIvaTotal()),
                retention, suplidosRow,
                new DocumentoFactura.TotalBand("TOTAL", Formatos.moneda(r.getTotal())));
        return new DocumentoFactura.TotalsBlock(
                List.of("TIPO", "BASE IMPONIBLE", "CUOTA IVA"),
                List.copyOf(rows), totalsRow, discountNote(r, descuento), liquidation);
    }

    static Optional<String> discountNote(ResumenFactura r, int descuento) {
        if (r.getImporteDescuento() == null || r.getImporteDescuento().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }
        String nota = "Bases netas tras el descuento comercial del " + descuento
                + " % (−" + importePdf(r.getImporteDescuento())
                + " s/ " + importePdf(r.getBaseBruta()) + ").";
        String motivo = null;
        for (GrupoIva grupo : r.getGrupos()) {
            if (grupo.isExento() && grupo.getMotivoExencion() != null && !grupo.getMotivoExencion().isBlank()) {
                motivo = grupo.getMotivoExencion();
                break;
            }
        }
        if (motivo != null) {
            nota += " Exención " + motivo + ".";
        }
        return Optional.of(nota);
    }

    static Optional<String> observations(Factura factura) {
        String obs = factura.getObservaciones();
        if (obs != null && !obs.isBlank()) {
            return Optional.of(obs);
        }
        return Optional.empty();
    }

    static Optional<String> legalFooter(Empresa empresa) {
        String pie = empresa != null ? nz(empresa.getPieLegal()) : "";
        return pie != null && !pie.isBlank() ? Optional.of(pie) : Optional.empty();
    }

    static String importePdf(BigDecimal importe) {
        return Formatos.moneda(importe).replace("\u00a0\u20ac", "").trim();
    }

    static String porcentajeRejilla(Integer porcentaje) {
        if (porcentaje == null) {
            return "Exento";
        }
        return REJILLA_TIPO.format(BigDecimal.valueOf(porcentaje));
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
