package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.repository.TipoRetencionRepository;

import java.sql.SQLException;
import java.util.List;

public class RetencionService {

    private final TipoRetencionRepository tipoRetencionRepository;

    public RetencionService(TipoRetencionRepository tipoRetencionRepository) {
        this.tipoRetencionRepository = tipoRetencionRepository;
    }

    public List<TipoRetencion> listar(boolean soloActivos) throws SQLException {
        return tipoRetencionRepository.listar(soloActivos);
    }

    public long insertar(TipoRetencion t) throws SQLException {
        return tipoRetencionRepository.insertar(t);
    }

    public void actualizar(TipoRetencion t) throws SQLException {
        tipoRetencionRepository.actualizar(t);
    }

    public void setActivo(long id, boolean activo) throws SQLException {
        tipoRetencionRepository.setActivo(id, activo);
    }

    public boolean enUso(long id) throws SQLException {
        return tipoRetencionRepository.enUso(id);
    }
}
