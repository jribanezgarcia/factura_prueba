package cabofactu.pdf;

import cabofactu.modelo.dominio.Empresa;
import cabofactu.modelo.negocio.Facturas;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

public class ExportadorPdf {

    public static final String PREF_COLOR = "color_pdf";
    public static final String COLOR_DEFECTO = "#B08D57";

    public void exportar(Facturas.VersionCompleta vc, Empresa empresa, Path ruta) throws Exception {
        exportar(vc, empresa, ruta, null);
    }

    public void exportar(Facturas.VersionCompleta vc, Empresa empresa, Path ruta, String colorHex) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            new GeneradorPdf().exportar(vc, empresa, fos, colorHex);
        }
    }

    public void exportarAgrupado(List<Facturas.VersionCompleta> versiones, Empresa empresa, Path ruta) throws Exception {
        exportarAgrupado(versiones, empresa, ruta, null);
    }

    public void exportarAgrupado(List<Facturas.VersionCompleta> versiones, Empresa empresa, Path ruta, String colorHex) throws Exception {
        new GeneradorPdf().exportarAgrupado(versiones, empresa, ruta, colorHex);
    }
}
