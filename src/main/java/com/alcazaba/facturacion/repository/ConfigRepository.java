package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigRepository {

    public Empresa getEmpresa() throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM empresa WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return map(rs);
            }
        }
        return new Empresa();
    }

    public void saveEmpresa(Empresa e) throws SQLException {
        String sql = """
                UPDATE empresa SET nombre = ?, nif = ?, direccion = ?, cp = ?, localidad = ?, provincia = ?,
                    actividad = ?, email = ?, telefono = ?, cabecera_modo = ?, logo_path = ?, logo_x = ?, logo_y = ?,
                    logo_ancho = ?, logo_alto = ?, pie_legal = ?
                WHERE id = 1
                """;
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getNif());
            ps.setString(3, e.getDireccion());
            ps.setString(4, e.getCp());
            ps.setString(5, e.getLocalidad());
            ps.setString(6, e.getProvincia());
            ps.setString(7, e.getActividad());
            ps.setString(8, e.getEmail());
            ps.setString(9, e.getTelefono());
            ps.setString(10, e.getCabeceraModo());
            ps.setString(11, e.getLogoPath());
            ps.setInt(12, e.getLogoX());
            ps.setInt(13, e.getLogoY());
            if (e.getLogoAncho() == null) {
                ps.setNull(14, java.sql.Types.INTEGER);
            } else {
                ps.setInt(14, e.getLogoAncho());
            }
            if (e.getLogoAlto() == null) {
                ps.setNull(15, java.sql.Types.INTEGER);
            } else {
                ps.setInt(15, e.getLogoAlto());
            }
            ps.setString(16, e.getPieLegal());
            ps.executeUpdate();
        }
    }

    public String getPreferencia(String clave) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT valor FROM preferencias WHERE clave = ?")) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public void setPreferencia(String clave, String valor) throws SQLException {
        Connection conn = Database.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO preferencias (clave, valor) VALUES (?, ?) "
                        + "ON CONFLICT(clave) DO UPDATE SET valor = excluded.valor")) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        }
    }

    private Empresa map(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setNombre(rs.getString("nombre"));
        e.setNif(rs.getString("nif"));
        e.setDireccion(rs.getString("direccion"));
        e.setCp(rs.getString("cp"));
        e.setLocalidad(rs.getString("localidad"));
        e.setProvincia(rs.getString("provincia"));
        e.setActividad(rs.getString("actividad"));
        e.setEmail(rs.getString("email"));
        e.setTelefono(rs.getString("telefono"));
        e.setCabeceraModo(rs.getString("cabecera_modo"));
        e.setLogoPath(rs.getString("logo_path"));
        e.setLogoX(rs.getInt("logo_x"));
        e.setLogoY(rs.getInt("logo_y"));
        int ancho = rs.getInt("logo_ancho");
        e.setLogoAncho(rs.wasNull() ? null : ancho);
        int alto = rs.getInt("logo_alto");
        e.setLogoAlto(rs.wasNull() ? null : alto);
        e.setPieLegal(rs.getString("pie_legal"));
        return e;
    }
}
