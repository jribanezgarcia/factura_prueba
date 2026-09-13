package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.repository.ConfigRepository;

import java.sql.SQLException;

public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public Empresa getEmpresa() throws SQLException {
        return configRepository.getEmpresa();
    }

    public void saveEmpresa(Empresa e) throws SQLException {
        configRepository.saveEmpresa(e);
    }

    public String getPreferencia(String clave) throws SQLException {
        return configRepository.getPreferencia(clave);
    }

    public void setPreferencia(String clave, String valor) throws SQLException {
        configRepository.setPreferencia(clave, valor);
    }
}
