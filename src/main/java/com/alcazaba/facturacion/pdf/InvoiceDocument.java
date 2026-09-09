package com.alcazaba.facturacion.pdf;

import java.util.List;
import java.util.Optional;

/**
 * Documento de factura ya compuesto a texto, sin paginar.
 */
public record InvoiceDocument(
        Header header,
        ClientCard clientCard,
        Optional<PaymentCard> paymentCard,
        LinesTable linesTable,
        Optional<SuplidosBlock> suplidos,
        TotalsBlock totals,
        Optional<String> observations,
        Optional<String> legalFooter) {

    public record Header(
            String number,
            String date,
            boolean corrective,
            Optional<String> correctsReference,
            boolean cancelled) {
    }

    public record FieldRow(String label, String value) {
    }

    public record ClientCard(String title, List<FieldRow> rows, String emptyMarker) {
    }

    public record PaymentCard(String title, List<FieldRow> rows) {
    }

    public record LineRow(String quantity, String description, String price, String iva, String total) {
    }

    public record LinesTable(List<String> headers, List<LineRow> rows) {
    }

    public record SuplidoRow(String description, String amount) {
    }

    public record SuplidosBlock(List<String> headers, List<SuplidoRow> rows, String note) {
    }

    public record IvaRow(String type, String base, String quota) {
    }

    public record RetentionRow(String label, String amount) {
    }

    public record SuplidosTotalRow(String label, String amount) {
    }

    public record TotalBand(String label, String amount) {
    }

    public record Liquidation(
            String title,
            String baseLabel,
            String baseAmount,
            String ivaLabel,
            String ivaAmount,
            Optional<RetentionRow> retention,
            Optional<SuplidosTotalRow> suplidos,
            TotalBand total) {
    }

    public record TotalsBlock(
            List<String> desgloseHeaders,
            List<IvaRow> ivaRows,
            IvaRow totalsRow,
            Optional<String> discountNote,
            Liquidation liquidation) {
    }
}
