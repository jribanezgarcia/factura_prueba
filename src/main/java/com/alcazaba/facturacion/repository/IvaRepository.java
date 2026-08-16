package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.TipoIva;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IvaRepository {

    public List<TipoIva> listar(boolean soloActivos) throws SQLException {
        String sql = "SELECT * FROM tipo_iva" + (soloActivos ? " WHERE activo = 1" : "") + " ORDER BY id";
        List<TipoIva> lista = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }
        return lista;
    }

    public TipoIva getById(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM tipo_iva WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public long insertar(TipoIva t) throws SQLException {
        String sql = "INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNombre());
            if (t.isExento()) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, t.getPorcentaje());
            }
            ps.setString(3, t.getMotivoExencion());
            ps.setInt(4, t.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void actualizar(TipoIva t) throws SQLException {
        String sql = "UPDATE tipo_iva SET nombre = ?, porcentaje = ?, motivo_exencion = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            if (t.isExento()) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, t.getPorcentaje());
            }
            ps.setString(3, t.getMotivoExencion());
            ps.setInt(4, t.isActivo() ? 1 : 0);
            ps.setLong(5, t.getId());
            ps.executeUpdate();
        }
    }

    public void setActivo(long id, boolean activo) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE tipo_iva SET activo = ? WHERE id = ?")) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * True si el tipo de IVA aparece en lineas del historico (entonces ya no
     * debe poder modificarse su porcentaje ni eliminarse fisicamente).
     */
    public boolean enUso(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM factura_linea WHERE tipo_iva_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private TipoIva map(ResultSet rs) throws SQLException {
        TipoIva t = new TipoIva();
        t.setId(rs.getLong("id"));
        t.setNombre(rs.getString("nombre"));
        int pct = rs.getInt("porcentaje");
        t.setPorcentaje(rs.wasNull() ? null : pct);
        t.setMotivoExencion(rs.getString("motivo_exencion"));
        t.setActivo(rs.getInt("activo") == 1);
        return t;
    }
}
