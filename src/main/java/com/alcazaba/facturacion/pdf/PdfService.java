package com.alcazaba.facturacion.pdf;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.service.FacturaService;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;

public class PdfService {

    public static final String PREF_COLOR = "color_pdf";
    public static final String COLOR_DEFECTO = "#B08D57";

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta) throws Exception {
        exportar(vc, empresa, ruta, null);
    }

    public void exportar(FacturaService.VersionCompleta vc, Empresa empresa, Path ruta, String colorHex) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(ruta.toFile())) {
            new OpenPdfRenderer().exportar(vc, empresa, fos, colorHex);
        }
    }

    public void exportarAgrupado(List<FacturaService.VersionCompleta> versiones, Empresa empresa, Path ruta) throws Exception {
        exportarAgrupado(versiones, empresa, ruta, null);
    }

    public void exportarAgrupado(List<FacturaService.VersionCompleta> versiones, Empresa empresa, Path ruta, String colorHex) throws Exception {
        new OpenPdfRenderer().exportarAgrupado(versiones, empresa, ruta, colorHex);
    }
}
