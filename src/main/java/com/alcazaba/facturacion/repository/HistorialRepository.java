package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FiltrosHistorial;
import com.alcazaba.facturacion.model.HistorialFila;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistorialRepository {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<HistorialFila> buscar(FiltrosHistorial f) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT v.id AS version_id, v.factura_id, v.version_num, v.numero, v.fecha_factura,
                       v.fecha_guardado, v.estado, v.cli_nombre, v.cli_nif,
                       v.base_total, v.iva_total, v.total, s.codigo AS serie_codigo, f.correlativo
                FROM factura_version v
                JOIN factura f ON f.id = v.factura_id
                JOIN serie s ON s.id = f.serie_id
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (f.getSerieCodigo() != null && !f.getSerieCodigo().isBlank()) {
            sql.append(" AND s.codigo = ?");
            params.add(f.getSerieCodigo().trim());
        }
        if (f.getClienteTexto() != null && !f.getClienteTexto().isBlank()) {
            sql.append(" AND (v.cli_nombre LIKE ? OR v.cli_nif LIKE ?)");
            String like = "%" + f.getClienteTexto().trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (f.getFechaDesde() != null) {
            sql.append(" AND v.fecha_factura >= ?");
            params.add(FECHA.format(f.getFechaDesde()));
        }
        if (f.getFechaHasta() != null) {
            sql.append(" AND v.fecha_factura <= ?");
            params.add(FECHA.format(f.getFechaHasta()));
        }
        if (f.getEstado() != null) {
            sql.append(" AND v.estado = ?");
            params.add(f.getEstado().name());
        }
        sql.append(" ORDER BY s.codigo, f.correlativo, v.version_num, v.fecha_factura");

        List<HistorialFila> lista = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialFila fila = map(rs);
                    if (cumpleImporte(fila, f)) {
                        lista.add(fila);
                    }
                }
            }
        }
        return lista;
    }

    private boolean cumpleImporte(HistorialFila fila, FiltrosHistorial f) {
        if (f.getImporteDesde() != null && fila.getTotal().compareTo(f.getImporteDesde()) < 0) {
            return false;
        }
        if (f.getImporteHasta() != null && fila.getTotal().compareTo(f.getImporteHasta()) > 0) {
            return false;
        }
        return true;
    }

    private HistorialFila map(ResultSet rs) throws SQLException {
        HistorialFila fila = new HistorialFila();
        fila.setVersionId(rs.getLong("version_id"));
        fila.setFacturaId(rs.getLong("factura_id"));
        fila.setVersionNum(rs.getInt("version_num"));
        fila.setNumero(rs.getString("numero"));
        fila.setFechaFactura(LocalDate.parse(rs.getString("fecha_factura"), FECHA));
        fila.setFechaGuardado(LocalDateTime.parse(rs.getString("fecha_guardado"), DATETIME));
        fila.setEstado(EstadoFactura.from(rs.getString("estado")));
        fila.setCliente(rs.getString("cli_nombre"));
        fila.setNif(rs.getString("cli_nif"));
        fila.setBase(new BigDecimal(rs.getString("base_total")));
        fila.setIva(new BigDecimal(rs.getString("iva_total")));
        fila.setTotal(new BigDecimal(rs.getString("total")));
        fila.setSerieCodigo(rs.getString("serie_codigo"));
        fila.setCorrelativo(rs.getInt("correlativo"));
        return fila;
    }
}
