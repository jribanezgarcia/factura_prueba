package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Factura;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FacturaRepository {

    public long insertar(long serieId, int correlativo, Long clienteId) throws SQLException {
        String sql = "INSERT INTO factura (serie_id, correlativo, cliente_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, serieId);
            ps.setInt(2, correlativo);
            if (clienteId == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setLong(3, clienteId);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public Factura getById(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM factura WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void actualizarCliente(long facturaId, Long clienteId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("UPDATE factura SET cliente_id = ? WHERE id = ?")) {
            if (clienteId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setLong(1, clienteId);
            }
            ps.setLong(2, facturaId);
            ps.executeUpdate();
        }
    }

    /**
     * Indica si la serie tiene alguna factura (activa o historica). Si la tiene,
     * la serie no puede borrarse: la clave foranea lo impide y, ademas, no debe
     * destruirse el historico.
     */
    public boolean serieTieneFacturas(long serieId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT 1 FROM factura WHERE serie_id = ? LIMIT 1")) {
            ps.setLong(1, serieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean clienteTieneFacturaEnMes(long clienteId, int anio, int mes) throws SQLException {
        String sql = """
                SELECT 1 FROM factura f
                JOIN factura_version v ON v.factura_id = f.id
                WHERE f.cliente_id = ?
                  AND CAST(strftime('%Y', v.fecha_factura) AS INTEGER) = ?
                  AND CAST(strftime('%m', v.fecha_factura) AS INTEGER) = ?
                LIMIT 1
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setLong(1, clienteId);
            ps.setInt(2, anio);
            ps.setInt(3, mes);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void eliminar(long facturaId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM factura WHERE id = ?")) {
            ps.setLong(1, facturaId);
            ps.executeUpdate();
        }
    }

    private Factura map(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getLong("id"));
        f.setSerieId(rs.getLong("serie_id"));
        f.setCorrelativo(rs.getInt("correlativo"));
        long cid = rs.getLong("cliente_id");
        f.setClienteId(rs.wasNull() ? null : cid);
        return f;
    }
}
