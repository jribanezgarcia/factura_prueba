package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;
import com.alcazaba.facturacion.model.LineaFactura;
import com.alcazaba.facturacion.model.ResumenFactura;
import com.alcazaba.facturacion.service.CalculoService;
import com.alcazaba.facturacion.service.FacturaService;
import com.alcazaba.facturacion.util.Formatos;

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
public final class InvoiceDocumentBuilder {

    private static final DecimalFormat REJILLA_TIPO = new DecimalFormat("0.00",
            DecimalFormatSymbols.getInstance(new Locale("es", "ES")));

    private InvoiceDocumentBuilder() {
    }

    public static InvoiceDocument build(FacturaService.VersionCompleta vc, Empresa empresa, String colorHex) {
        FacturaVersion v = vc.version();
        ResumenFactura resumen = CalculoService.resumen(vc.lineas(), v.getDescuentoPorcentaje(),
                FacturaService.retencionDeVersion(v));
        List<LineaFactura> suplidos = CalculoService.suplidosDe(vc.lineas());
        return new InvoiceDocument(
                header(v),
                clientCard(v),
                paymentCard(v),
                linesTable(vc.lineas()),
                suplidosBlock(suplidos),
                totalsBlock(resumen, v.getDescuentoPorcentaje()),
                observations(v),
                legalFooter(empresa));
    }

    static InvoiceDocument.Header header(FacturaVersion v) {
        boolean corrective = v.getReferenciaRectifica() != null && !v.getReferenciaRectifica().isBlank();
        return new InvoiceDocument.Header(
                nz(v.getNumero()),
                Formatos.fecha(v.getFechaFactura()),
                corrective,
                corrective ? Optional.of(nz(v.getReferenciaRectifica())) : Optional.empty(),
                v.getEstado() == EstadoFactura.ANULADA);
    }

    static InvoiceDocument.ClientCard clientCard(FacturaVersion v) {
        List<InvoiceDocument.FieldRow> rows = new ArrayList<>();
        if (!nz(v.getCliNombre()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Nombre", v.getCliNombre()));
        }
        if (!nz(v.getCliNif()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("NIF", v.getCliNif()));
        }
        if (!nz(v.getCliDireccion()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Dirección", v.getCliDireccion()));
        }
        if (!nz(v.getCliCp()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Código postal", v.getCliCp()));
        }
        if (!nz(v.getCliLocalidad()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Población", v.getCliLocalidad()));
        }
        if (!nz(v.getCliProvincia()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Provincia", v.getCliProvincia()));
        }
        if (!nz(v.getCliEmail()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Email", v.getCliEmail()));
        }
        return new InvoiceDocument.ClientCard("FACTURAR A", List.copyOf(rows), "—");
    }

    static String paymentCardTitle() {
        return "DATOS DE PAGO";
    }

    static Optional<InvoiceDocument.PaymentCard> paymentCard(FacturaVersion v) {
        List<InvoiceDocument.FieldRow> rows = paymentRows(v);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new InvoiceDocument.PaymentCard(paymentCardTitle(), rows));
    }

    static List<InvoiceDocument.FieldRow> paymentRows(FacturaVersion v) {
        List<InvoiceDocument.FieldRow> rows = new ArrayList<>();
        if (!nz(v.getFormaPago()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Forma de pago", v.getFormaPago()));
        }
        if (v.getVencimiento() != null) {
            rows.add(new InvoiceDocument.FieldRow("Vencimiento", Formatos.fecha(v.getVencimiento())));
        }
        if (!nz(v.getRealizadaPor()).isBlank()) {
            rows.add(new InvoiceDocument.FieldRow("Realizada por", v.getRealizadaPor()));
        }
        return List.copyOf(rows);
    }

    static InvoiceDocument.LinesTable linesTable(List<LineaFactura> lineas) {
        List<InvoiceDocument.LineRow> rows = new ArrayList<>();
        if (lineas != null) {
            for (LineaFactura l : lineas) {
                if (l.isEsSuplido()) {
                    continue;
                }
                rows.add(new InvoiceDocument.LineRow(
                        String.valueOf(l.getCantidad()),
                        nz(l.getDescripcion()),
                        importePdf(l.getPrecioUnitario()),
                        l.isExenta() ? "Exento" : l.getIvaPorcentaje() + " %",
                        importePdf(CalculoService.totalConIva(l))));
            }
        }
        return new InvoiceDocument.LinesTable(
                List.of("CANT.", "DESCRIPCIÓN", "PRECIO", "IVA %", "TOTAL"), List.copyOf(rows));
    }

    static Optional<InvoiceDocument.SuplidosBlock> suplidosBlock(List<LineaFactura> suplidos) {
        if (suplidos == null || suplidos.isEmpty()) {
            return Optional.empty();
        }
        List<InvoiceDocument.SuplidoRow> rows = new ArrayList<>();
        for (LineaFactura l : suplidos) {
            BigDecimal importe = l.getTotalBase() == null ? BigDecimal.ZERO : l.getTotalBase();
            rows.add(new InvoiceDocument.SuplidoRow(nz(l.getDescripcion()), importePdf(importe)));
        }
        return Optional.of(new InvoiceDocument.SuplidosBlock(
                List.of("SUPLIDOS", "IMPORTE"),
                List.copyOf(rows),
                "Suplidos pagados en nombre y por cuenta del cliente, facturados a su nombre. No sujetos a IVA ni a retención."));
    }

    static InvoiceDocument.TotalsBlock totalsBlock(ResumenFactura r, int descuento) {
        List<InvoiceDocument.IvaRow> rows = new ArrayList<>();
        for (ResumenFactura.IvaGrupo g : r.getGrupos()) {
            rows.add(new InvoiceDocument.IvaRow(
                    g.isExento() ? "Exento" : porcentajeRejilla(g.getPorcentaje()),
                    importePdf(g.getBase()),
                    g.isExento() ? "—" : importePdf(g.getCuota())));
        }
        InvoiceDocument.IvaRow totalsRow = new InvoiceDocument.IvaRow(
                "Totales", importePdf(r.getBaseTotal()), importePdf(r.getIvaTotal()));

        Optional<InvoiceDocument.RetentionRow> retention = Optional.empty();
        if (r.getImporteRetencion() != null && r.getImporteRetencion().compareTo(BigDecimal.ZERO) > 0) {
            String etiqueta = r.getNombreRetencion() != null && !r.getNombreRetencion().isBlank()
                    ? r.getNombreRetencion() + " " + r.getPorcentajeRetencion() + " %"
                    : "Retención " + r.getPorcentajeRetencion() + " %";
            retention = Optional.of(new InvoiceDocument.RetentionRow(
                    etiqueta, "−" + importePdf(r.getImporteRetencion())));
        }
        Optional<InvoiceDocument.SuplidosTotalRow> suplidosRow = Optional.empty();
        if (r.getTotalSuplidos() != null && r.getTotalSuplidos().compareTo(BigDecimal.ZERO) > 0) {
            suplidosRow = Optional.of(new InvoiceDocument.SuplidosTotalRow(
                    "Suplidos", "+" + importePdf(r.getTotalSuplidos())));
        }
        InvoiceDocument.Liquidation liquidation = new InvoiceDocument.Liquidation(
                "LIQUIDACIÓN",
                "Base imponible", importePdf(r.getBaseTotal()),
                "Total IVA repercutido", importePdf(r.getIvaTotal()),
                retention, suplidosRow,
                new InvoiceDocument.TotalBand("TOTAL", Formatos.moneda(r.getTotal())));
        return new InvoiceDocument.TotalsBlock(
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
        String motivo = r.getGrupos().stream()
                .filter(g -> g.isExento())
                .map(ResumenFactura.IvaGrupo::getMotivoExencion)
                .filter(m -> m != null && !m.isBlank())
                .findFirst()
                .orElse(null);
        if (motivo != null) {
            nota += " Exención " + motivo + ".";
        }
        return Optional.of(nota);
    }

    static Optional<String> observations(FacturaVersion v) {
        String obs = v.getObservaciones();
        return obs != null && !obs.isBlank() ? Optional.of(obs) : Optional.empty();
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
