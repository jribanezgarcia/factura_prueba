package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.FacturaDAO;
import cabofactu.modelo.negocio.sqlite.SerieDAO;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class Series {

    private final SerieDAO serieDAO;
    private final FacturaDAO facturaDAO;
    private final Clock clock;

    public Series(SerieDAO serieDAO, FacturaDAO facturaDAO, Clock clock) {
        this.serieDAO = serieDAO;
        this.facturaDAO = facturaDAO;
        this.clock = clock;
    }

    public List<Serie> listar() {
        return serieDAO.listar();
    }

    public Serie getById(long id) {
        return serieDAO.getById(id);
    }

    public long insertar(Serie s) {
        return serieDAO.insertar(s, LocalDate.now(clock).getYear());
    }

    public void actualizar(Serie s) {
        serieDAO.actualizar(s);
    }

    public int getSiguiente(long serieId, int anio) {
        return serieDAO.getSiguiente(serieId, anio);
    }

    public void actualizarSiguiente(long serieId, int anio, int siguiente) {
        serieDAO.actualizarSiguiente(serieId, anio, siguiente);
    }

    public boolean tieneFacturas(long serieId) {
        return facturaDAO.serieTieneFacturas(serieId);
    }

    public void eliminar(long serieId) {
        serieDAO.eliminar(serieId);
    }
}
