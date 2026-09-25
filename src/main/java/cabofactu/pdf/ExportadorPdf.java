package cabofactu.pdf;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.dominio.Factura;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

public class ExportadorPdf {

    public static final String PREF_COLOR = "color_pdf";
    public static final String COLOR_DEFECTO = "#B08D57";

    public void exportar(Factura factura, Empresa empresa, Path ruta) throws Exception {
        exportar(factura, empresa, ruta, null);
    }

    public void exportar(Factura factura, Empresa empresa, Path ruta, String colorHex) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            new GeneradorPdf().exportar(factura, empresa, fos, colorHex);
        }
    }

    public void exportarAgrupado(List<Factura> facturas, Empresa empresa, Path ruta) throws Exception {
        exportarAgrupado(facturas, empresa, ruta, null);
    }

    public void exportarAgrupado(List<Factura> facturas, Empresa empresa, Path ruta, String colorHex) throws Exception {
        new GeneradorPdf().exportarAgrupado(facturas, empresa, ruta, colorHex);
    }
}
