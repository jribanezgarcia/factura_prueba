package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.FilaHistorial;
import cabofactu.modelo.negocio.sqlite.HistorialDAO;

import java.util.List;

/**
 * Consultas del historico con filtros combinados (serie, cliente/NIF, fechas,
 * importes y estado), una fila por version y ordenadas por numero de factura.
 */
public class Historial {

    private final HistorialDAO historialDAO;

    public Historial(HistorialDAO historialDAO) {
        this.historialDAO = historialDAO;
    }

    public List<FilaHistorial> buscar(FiltrosHistorial filtros) {
        return historialDAO.buscar(filtros);
    }
}
