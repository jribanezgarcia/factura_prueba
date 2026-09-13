package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Empresa;
import com.alcazaba.facturacion.repository.ConfigRepository;


public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public Empresa getEmpresa() {
        return configRepository.getEmpresa();
    }

    public void saveEmpresa(Empresa e) {
        configRepository.saveEmpresa(e);
    }

    public String getPreferencia(String clave) {
        return configRepository.getPreferencia(clave);
    }

    public void setPreferencia(String clave, String valor) {
        configRepository.setPreferencia(clave, valor);
    }
}
