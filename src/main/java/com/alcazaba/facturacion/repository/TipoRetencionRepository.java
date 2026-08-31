package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.TipoRetencion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoRetencionRepository {

    public List<TipoRetencion> listar(boolean soloActivos) throws SQLException {
        String sql = "SELECT * FROM tipo_retencion" + (soloActivos ? " WHERE activo = 1" : "") + " ORDER BY id";
        List<TipoRetencion> lista = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }
        return lista;
    }

    public TipoRetencion getById(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM tipo_retencion WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public long insertar(TipoRetencion t) throws SQLException {
        String sql = "INSERT INTO tipo_retencion (nombre, porcentaje, activo) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNombre());
            ps.setInt(2, t.getPorcentaje());
            ps.setInt(3, t.isActivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public void actualizar(TipoRetencion t) throws SQLException {
        String sql = "UPDATE tipo_retencion SET nombre = ?, porcentaje = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setInt(2, t.getPorcentaje());
            ps.setInt(3, t.isActivo() ? 1 : 0);
            ps.setLong(4, t.getId());
            ps.executeUpdate();
        }
    }

    public void setActivo(long id, boolean activo) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE tipo_retencion SET activo = ? WHERE id = ?")) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public boolean enUso(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM factura_version WHERE tipo_retencion_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private TipoRetencion map(ResultSet rs) throws SQLException {
        TipoRetencion t = new TipoRetencion();
        t.setId(rs.getLong("id"));
        t.setNombre(rs.getString("nombre"));
        t.setPorcentaje(rs.getInt("porcentaje"));
        t.setActivo(rs.getInt("activo") == 1);
        return t;
    }
}
