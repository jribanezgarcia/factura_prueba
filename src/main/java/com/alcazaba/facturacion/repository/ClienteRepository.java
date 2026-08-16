package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteRepository {

    public List<Cliente> listar(boolean soloActivos) throws SQLException {
        String sql = "SELECT * FROM cliente" + (soloActivos ? " WHERE activo = 1" : "") + " ORDER BY nombre";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }
        return lista;
    }

    public List<Cliente> buscar(String texto, boolean soloActivos) throws SQLException {
        String sql = "SELECT * FROM cliente WHERE (nombre LIKE ? OR nif LIKE ?)"
                + (soloActivos ? " AND activo = 1" : "")
                + " ORDER BY nombre LIMIT 100";
        List<Cliente> lista = new ArrayList<>();
        String like = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
        }
        return lista;
    }

    public Cliente getById(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM cliente WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public long insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, nif, direccion, cp, localidad, provincia, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNif());
            ps.setString(3, c.getDireccion());
            ps.setString(4, c.getCp());
            ps.setString(5, c.getLocalidad());
            ps.setString(6, c.getProvincia());
            ps.setInt(7, c.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nombre = ?, nif = ?, direccion = ?, cp = ?, localidad = ?, provincia = ?, activo = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNif());
            ps.setString(3, c.getDireccion());
            ps.setString(4, c.getCp());
            ps.setString(5, c.getLocalidad());
            ps.setString(6, c.getProvincia());
            ps.setInt(7, c.isActivo() ? 1 : 0);
            ps.setLong(8, c.getId());
            ps.executeUpdate();
        }
    }

    public void borrarFisico(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM cliente WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void setActivo(long id, boolean activo) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE cliente SET activo = ? WHERE id = ?")) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneFacturas(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT COUNT(*) FROM factura WHERE cliente_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getLong("id"));
        c.setNombre(rs.getString("nombre"));
        c.setNif(rs.getString("nif"));
        c.setDireccion(rs.getString("direccion"));
        c.setCp(rs.getString("cp"));
        c.setLocalidad(rs.getString("localidad"));
        c.setProvincia(rs.getString("provincia"));
        c.setActivo(rs.getInt("activo") == 1);
        return c;
    }
}
