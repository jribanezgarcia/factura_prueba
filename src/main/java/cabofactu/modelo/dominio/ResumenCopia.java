package cabofactu.modelo.dominio;

import cabofactu.utilidades.Formatos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Lo que leemos de un archivo de copia de seguridad antes de restaurarlo: los
 * datos de la empresa que contiene, para que el usuario sepa qué va a
 * sustituir. Es una clase de resultado, sin validación.
 */
public class ResumenCopia {

    private String nombreEmpresa;
    private String nif;
    private int numeroFacturas;
    private LocalDate ultimaFecha;
    private String logo;

    public ResumenCopia(String nombreEmpresa, String nif, int numeroFacturas, LocalDate ultimaFecha, String logo) {
        setNombreEmpresa(nombreEmpresa);
        setNif(nif);
        setNumeroFacturas(numeroFacturas);
        setUltimaFecha(ultimaFecha);
        setLogo(logo);
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        if (nombreEmpresa == null) {
            this.nombreEmpresa = "";
        } else {
            this.nombreEmpresa = nombreEmpresa;
        }
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        if (nif == null) {
            this.nif = "";
        } else {
            this.nif = nif;
        }
    }

    public int getNumeroFacturas() {
        return numeroFacturas;
    }

    public void setNumeroFacturas(int numeroFacturas) {
        this.numeroFacturas = numeroFacturas;
    }

    public LocalDate getUltimaFecha() {
        return ultimaFecha;
    }

    public void setUltimaFecha(LocalDate ultimaFecha) {
        this.ultimaFecha = ultimaFecha;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        if (logo == null) {
            this.logo = "";
        } else {
            this.logo = logo;
        }
    }

    /** True si la copia trae un logo y ese archivo no está en este ordenador. */
    public boolean isLogoPerdido() {
        return !logo.isEmpty() && !Files.exists(Path.of(logo));
    }

    /** El resumen que enseña la pantalla antes de restaurar. */
    public String getTexto() {
        String nifTexto = "sin NIF";
        if (!nif.isEmpty()) {
            nifTexto = nif;
        }
        String fechaTexto = "ninguna";
        if (ultimaFecha != null) {
            fechaTexto = Formatos.fecha(ultimaFecha);
        }
        String texto = String.format("Empresa: %s%nNIF: %s%nFacturas: %d%nÚltima factura: %s",
                nombreEmpresa, nifTexto, numeroFacturas, fechaTexto);
        if (isLogoPerdido()) {
            texto = texto + String.format("%nEl logo de la copia no está en este ordenador: la empresa se quedará sin logo.");
        }
        return texto;
    }
}
