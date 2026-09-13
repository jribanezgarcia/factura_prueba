package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.model.Cliente;
import com.alcazaba.facturacion.repository.ClienteRepository;

import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listar(boolean soloActivos) throws SQLException {
        return clienteRepository.listar(soloActivos);
    }

    public List<Cliente> buscar(String texto, boolean soloActivos) throws SQLException {
        return clienteRepository.buscar(texto, soloActivos);
    }

    public long insertar(Cliente c) throws SQLException {
        return clienteRepository.insertar(c);
    }

    public void actualizar(Cliente c) throws SQLException {
        clienteRepository.actualizar(c);
    }

    public boolean tieneFacturas(long id) throws SQLException {
        return clienteRepository.tieneFacturas(id);
    }

    public void setActivo(long id, boolean activo) throws SQLException {
        clienteRepository.setActivo(id, activo);
    }

    public void borrarFisico(long id) throws SQLException {
        clienteRepository.borrarFisico(id);
    }
}
