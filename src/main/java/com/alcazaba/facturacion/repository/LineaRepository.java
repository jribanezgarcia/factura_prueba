package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.LineaFactura;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LineaRepository {

    public void insertarLineas(long versionId, List<LineaFactura> lineas) throws SQLException {
        String sql = """
                INSERT INTO factura_linea (factura_version_id, orden, cantidad, descripcion, precio_unitario,
                    total_base, tipo_iva_id, iva_nombre, iva_porcentaje, iva_motivo_exencion, iva_importe)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            for (LineaFactura l : lineas) {
                ps.setLong(1, versionId);
                ps.setInt(2, l.getOrden());
                ps.setInt(3, l.getCantidad());
                ps.setString(4, l.getDescripcion());
                ps.setString(5, l.getPrecioUnitario() == null ? "0" : l.getPrecioUnitario().toPlainString());
                ps.setString(6, l.getTotalBase() == null ? "0.00" : l.getTotalBase().toPlainString());
                if (l.getTipoIvaId() == null) {
                    ps.setNull(7, java.sql.Types.INTEGER);
                } else {
                    ps.setLong(7, l.getTipoIvaId());
                }
                ps.setString(8, l.getIvaNombre());
                if (l.isExenta()) {
                    ps.setNull(9, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(9, l.getIvaPorcentaje());
                }
                ps.setString(10, l.getIvaMotivoExencion());
                ps.setString(11, l.getIvaImporte() == null ? "0.00" : l.getIvaImporte().toPlainString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void eliminarPorVersion(long versionId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM factura_linea WHERE factura_version_id = ?")) {
            ps.setLong(1, versionId);
            ps.executeUpdate();
        }
    }

    public List<LineaFactura> getLineas(long versionId) throws SQLException {
        List<LineaFactura> lista = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM factura_linea WHERE factura_version_id = ? ORDER BY orden")) {
            ps.setLong(1, versionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LineaFactura l = new LineaFactura();
                    l.setId(rs.getLong("id"));
                    l.setOrden(rs.getInt("orden"));
                    l.setCantidad(rs.getInt("cantidad"));
                    l.setDescripcion(rs.getString("descripcion"));
                    l.setPrecioUnitario(new BigDecimal(rs.getString("precio_unitario")));
                    l.setTotalBase(new BigDecimal(rs.getString("total_base")));
                    long tiva = rs.getLong("tipo_iva_id");
                    l.setTipoIvaId(rs.wasNull() ? null : tiva);
                    l.setIvaNombre(rs.getString("iva_nombre"));
                    int pct = rs.getInt("iva_porcentaje");
                    l.setIvaPorcentaje(rs.wasNull() ? null : pct);
                    l.setIvaMotivoExencion(rs.getString("iva_motivo_exencion"));
                    l.setIvaImporte(new BigDecimal(rs.getString("iva_importe")));
                    lista.add(l);
                }
            }
        }
        return lista;
    }
}
