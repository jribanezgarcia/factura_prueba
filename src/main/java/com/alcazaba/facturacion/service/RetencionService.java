package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.TipoRetencion;
import com.alcazaba.facturacion.repository.TipoRetencionRepository;

import java.util.List;

public class RetencionService {

    private final TipoRetencionRepository tipoRetencionRepository;

    public RetencionService(TipoRetencionRepository tipoRetencionRepository) {
        this.tipoRetencionRepository = tipoRetencionRepository;
    }

    public List<TipoRetencion> listar(boolean soloActivos) {
        return tipoRetencionRepository.listar(soloActivos);
    }

    public long insertar(TipoRetencion t) {
        return tipoRetencionRepository.insertar(t);
    }

    public void actualizar(TipoRetencion t) {
        tipoRetencionRepository.actualizar(t);
    }

    public void setActivo(long id, boolean activo) {
        tipoRetencionRepository.setActivo(id, activo);
    }

    public boolean enUso(long id) {
        return tipoRetencionRepository.enUso(id);
    }
}
