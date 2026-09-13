package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Serie;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.SerieRepository;

import java.sql.SQLException;
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

    public List<Serie> listar() throws SQLException {
        return serieRepository.listar();
    }

    public Serie getById(long id) throws SQLException {
        return serieRepository.getById(id);
    }

    public long insertar(Serie s) throws SQLException {
        return serieRepository.insertar(s, LocalDate.now(clock).getYear());
    }

    public void actualizar(Serie s) throws SQLException {
        serieRepository.actualizar(s);
    }

    public int getSiguiente(long serieId, int anio) throws SQLException {
        return serieRepository.getSiguiente(serieId, anio);
    }

    public void actualizarSiguiente(long serieId, int anio, int siguiente) throws SQLException {
        serieRepository.actualizarSiguiente(serieId, anio, siguiente);
    }

    public boolean tieneFacturas(long serieId) throws SQLException {
        return facturaRepository.serieTieneFacturas(serieId);
    }

    public void eliminar(long serieId) throws SQLException {
        serieRepository.eliminar(serieId);
    }
}
