package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.TipoIva;
import com.alcazaba.facturacion.repository.IvaRepository;

import java.sql.SQLException;
import java.util.List;

public class IvaService {

    private final IvaRepository ivaRepository;

    public IvaService(IvaRepository ivaRepository) {
        this.ivaRepository = ivaRepository;
    }

    public List<TipoIva> listar(boolean soloActivos) throws SQLException {
        return ivaRepository.listar(soloActivos);
    }

    public TipoIva getById(long id) throws SQLException {
        return ivaRepository.getById(id);
    }

    public long insertar(TipoIva t) throws SQLException {
        return ivaRepository.insertar(t);
    }

    public void actualizar(TipoIva t) throws SQLException {
        ivaRepository.actualizar(t);
    }

    public void setActivo(long id, boolean activo) throws SQLException {
        ivaRepository.setActivo(id, activo);
    }

    public boolean enUso(long id) throws SQLException {
        return ivaRepository.enUso(id);
    }
}
