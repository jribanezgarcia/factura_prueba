package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.HistorialFila;
import cabofactu.modelo.negocio.sqlite.HistorialRepository;

import java.util.List;

/**
 * Consultas del historico con filtros combinados (serie, cliente/NIF, fechas,
 * importes y estado), una fila por version y ordenadas por numero de factura.
 */
public class HistorialService {

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public List<HistorialFila> buscar(FiltrosHistorial filtros) {
        return historialRepository.buscar(filtros);
    }
}
