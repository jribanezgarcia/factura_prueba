package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.FacturaRepository;
import cabofactu.modelo.negocio.sqlite.SerieRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class SerieService {

    private final SerieRepository serieRepository;
    private final FacturaRepository facturaRepository;
    private final Clock clock;

    public SerieService(SerieRepository serieRepository, FacturaRepository facturaRepository, Clock clock) {
        this.serieRepository = serieRepository;
        this.facturaRepository = facturaRepository;
        this.clock = clock;
    }

    public List<Serie> listar() {
        return serieRepository.listar();
    }

    public Serie getById(long id) {
        return serieRepository.getById(id);
    }

    public long insertar(Serie s) {
        return serieRepository.insertar(s, LocalDate.now(clock).getYear());
    }

    public void actualizar(Serie s) {
        serieRepository.actualizar(s);
    }

    public int getSiguiente(long serieId, int anio) {
        return serieRepository.getSiguiente(serieId, anio);
    }

    public void actualizarSiguiente(long serieId, int anio, int siguiente) {
        serieRepository.actualizarSiguiente(serieId, anio, siguiente);
    }

    public boolean tieneFacturas(long serieId) {
        return facturaRepository.serieTieneFacturas(serieId);
    }

    public void eliminar(long serieId) {
        serieRepository.eliminar(serieId);
    }
}
