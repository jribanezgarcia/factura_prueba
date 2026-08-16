package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.FiltrosHistorial;
import com.alcazaba.facturacion.model.HistorialFila;
import com.alcazaba.facturacion.repository.HistorialRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Consultas del historico con filtros combinados (serie, cliente/NIF, fechas,
 * importes y estado), una fila por version y ordenadas por fecha y numero.
 */
public class HistorialService {

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public List<HistorialFila> buscar(FiltrosHistorial filtros) throws SQLException {
        return historialRepository.buscar(filtros);
    }
}
