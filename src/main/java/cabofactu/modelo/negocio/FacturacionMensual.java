package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ModoDia;
import cabofactu.modelo.dominio.PlantillaMensual;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.utilidades.Formatos;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Generamos las facturas mensuales de un cliente: una Factura por mes, todas
 * o ninguna. El año es siempre el de la fecha de trabajo.
 */
public class FacturacionMensual {

    private static FacturacionMensual facturacionMensual;

    private FacturacionMensual() {
    }

    public static FacturacionMensual getFacturacionMensual() {
        if (facturacionMensual == null) {
            facturacionMensual = new FacturacionMensual();
        }
        return facturacionMensual;
    }

    /** Los nombres de los meses del rango en que el cliente ya tiene factura en el año de trabajo. */
    public List<String> mesesConFactura(PlantillaMensual plantilla) throws Exception {
        int anio = anioDeTrabajo();
        List<String> meses = new ArrayList<>();
        for (int mes = plantilla.getMesInicio(); mes <= plantilla.getMesFin(); mes++) {
            if (Facturas.getFacturas().clienteTieneFacturaEnMes(plantilla.getCliente().getId(), anio, mes)) {
                meses.add(Formatos.nombreMes(mes));
            }
        }
        return meses;
    }

    /** Generamos todos los meses del rango, con o sin usar los números libres, todos a la vez. */
    public int generar(PlantillaMensual plantilla, boolean usarLibres) throws Exception {
        int anio = anioDeTrabajo();
        List<Integer> numeros = Series.getSeries().proponerNumeros(
                plantilla.getSerie(), anio, plantilla.getCantidadMeses(), usarLibres);
        List<Factura> facturas = new ArrayList<>();
        int indice = 0;
        for (int mes = plantilla.getMesInicio(); mes <= plantilla.getMesFin(); mes++) {
            facturas.add(facturaDelMes(plantilla, anio, mes, numeros.get(indice)));
            indice++;
        }
        Facturas.getFacturas().altaVarias(facturas);
        return facturas.size();
    }

    /** Construimos la factura de ese mes, con su fecha, su número pedido y su retención. */
    private Factura facturaDelMes(PlantillaMensual plantilla, int anio, int mes, int numero) throws Exception {
        LocalDate fecha = fechaDelMes(anio, mes, plantilla.getModoDia(), plantilla.getDiaFijo());
        Factura factura = new Factura(plantilla.getSerie(), fecha, new Cliente(plantilla.getCliente()));
        factura.setId(null);
        factura.setCorrelativo(numero);
        factura.setDescuento(0);
        factura.setObservaciones("");
        factura.setLineas(lineasDelMes(plantilla, mes));
        if (plantilla.getRetencion() == null) {
            factura.setRetencion(null);
        } else {
            factura.setRetencion(new TipoRetencion(plantilla.getRetencion()));
        }
        return factura;
    }

    /** La fecha de ese mes según el modo de día; el día fijo se ajusta al último día del mes. */
    private LocalDate fechaDelMes(int anio, int mes, ModoDia modoDia, int diaFijo) {
        YearMonth mesEntero = YearMonth.of(anio, mes);
        int dia = switch (modoDia) {
            case PRIMER_DIA -> 1;
            case ULTIMO_DIA -> mesEntero.lengthOfMonth();
            case FIJO -> Math.min(diaFijo, mesEntero.lengthOfMonth());
        };
        return LocalDate.of(anio, mes, dia);
    }

    /** Copiamos las líneas de la plantilla para ese mes, añadiendo el mes a la descripción si toca. */
    private List<LineaFactura> lineasDelMes(PlantillaMensual plantilla, int mes) throws Exception {
        String nombreMes = Formatos.nombreMes(mes);
        List<LineaFactura> lineas = new ArrayList<>();
        int orden = 1;
        for (LineaFactura original : plantilla.getLineas()) {
            LineaFactura linea = new LineaFactura(original.getCantidad(), original.getPrecioUnitario());
            linea.setOrden(orden);
            orden++;
            linea.setTipoIva(plantilla.getTipoIva());
            String descripcion = original.getDescripcion();
            if (plantilla.isAnadirMes()) {
                descripcion = descripcion + " - mes de " + nombreMes;
            }
            linea.setDescripcion(descripcion);
            lineas.add(linea);
        }
        return lineas;
    }

    private int anioDeTrabajo() {
        return Sesion.getSesion().getFechaTrabajo().getYear();
    }
}
