package cabofactu.modelo.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Todo lo que se elige en el diálogo de facturación mensual. Los setters
 * comprueban lo que reciben, así que una PlantillaMensual que existe siempre
 * se puede generar.
 */
public class PlantillaMensual {

    private Cliente cliente;
    private Serie serie;
    private int mesInicio;
    private int mesFin;
    private ModoDia modoDia;
    private int diaFijo;
    private TipoIva tipoIva;
    private TipoRetencion retencion;
    private List<LineaFactura> lineas;
    private boolean anadirMes;

    public PlantillaMensual(Cliente cliente, Serie serie, int mesInicio, int mesFin, ModoDia modoDia,
                             int diaFijo, TipoIva tipoIva, List<LineaFactura> lineas) throws Exception {
        setCliente(cliente);
        setSerie(serie);
        setMesInicio(mesInicio);
        setMesFin(mesFin);
        setModoDia(modoDia);
        setDiaFijo(diaFijo);
        setTipoIva(tipoIva);
        setLineas(lineas);
        setAnadirMes(false);
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) throws Exception {
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Seleccione un cliente.");
        }
        this.cliente = cliente;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) throws Exception {
        if (serie == null) {
            throw new Exception("Seleccione una serie.");
        }
        this.serie = serie;
    }

    public int getMesInicio() {
        return mesInicio;
    }

    /** El mes de inicio se comprueba contra el de fin, ya esté puesto o no. */
    public void setMesInicio(int mesInicio) throws Exception {
        String error = errorMes(mesInicio);
        if (error == null) {
            error = errorRangoMeses(mesInicio, this.mesFin);
        }
        if (error != null) {
            throw new Exception(error);
        }
        this.mesInicio = mesInicio;
    }

    public int getMesFin() {
        return mesFin;
    }

    /** El mes de fin se comprueba contra el de inicio, ya esté puesto o no. */
    public void setMesFin(int mesFin) throws Exception {
        String error = errorMes(mesFin);
        if (error == null) {
            error = errorRangoMeses(this.mesInicio, mesFin);
        }
        if (error != null) {
            throw new Exception(error);
        }
        this.mesFin = mesFin;
    }

    public ModoDia getModoDia() {
        return modoDia;
    }

    public void setModoDia(ModoDia modoDia) throws Exception {
        if (modoDia == null) {
            throw new Exception("Elija el día del mes.");
        }
        this.modoDia = modoDia;
    }

    public int getDiaFijo() {
        return diaFijo;
    }

    /** El día fijo solo se comprueba cuando el modo elegido es FIJO. */
    public void setDiaFijo(int diaFijo) throws Exception {
        if (modoDia == ModoDia.FIJO && (diaFijo < 1 || diaFijo > 31)) {
            throw new Exception("El día del mes debe estar entre 1 y 31.");
        }
        this.diaFijo = diaFijo;
    }

    public TipoIva getTipoIva() {
        return tipoIva;
    }

    public void setTipoIva(TipoIva tipoIva) throws Exception {
        if (tipoIva == null) {
            throw new Exception("Seleccione un tipo de IVA.");
        }
        this.tipoIva = tipoIva;
    }

    public TipoRetencion getRetencion() {
        return retencion;
    }

    public void setRetencion(TipoRetencion retencion) {
        this.retencion = retencion;
    }

    public List<LineaFactura> getLineas() {
        return lineas;
    }

    /** Guardamos una copia de la lista, para que no cambie por fuera. */
    public void setLineas(List<LineaFactura> lineas) throws Exception {
        if (lineas == null || lineas.isEmpty()) {
            throw new Exception("Añada al menos una línea con descripción.");
        }
        this.lineas = new ArrayList<>(lineas);
    }

    public boolean isAnadirMes() {
        return anadirMes;
    }

    public void setAnadirMes(boolean anadirMes) {
        this.anadirMes = anadirMes;
    }

    /** Cuántos meses tiene el rango, contando los dos extremos. */
    public int getCantidadMeses() {
        return mesFin - mesInicio + 1;
    }

    /** El mes debe estar entre enero y diciembre, o null si está bien. */
    private static String errorMes(int mes) {
        if (mes < 1 || mes > 12) {
            return "El mes debe estar entre enero y diciembre.";
        }
        return null;
    }

    /** El inicio no puede ser posterior al fin, con los dos puestos; null si está bien o falta uno. */
    private static String errorRangoMeses(int inicio, int fin) {
        if (inicio != 0 && fin != 0 && inicio > fin) {
            return "El mes de inicio debe ser anterior o igual al mes de fin.";
        }
        return null;
    }
}
