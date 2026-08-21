package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.EstadoFactura;
import com.alcazaba.facturacion.model.FacturaVersion;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VersionRepository {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long insertarVersion(FacturaVersion v) throws SQLException {
        String sql = """
                INSERT INTO factura_version (factura_id, version_num, numero, fecha_factura, fecha_guardado,
                    estado, descuento_porcentaje, observaciones, referencia_rectifica,
                    cli_nombre, cli_nif, cli_direccion, cli_cp, cli_localidad, cli_provincia,
                    cli_email, forma_pago, vencimiento, realizada_por,
                    base_total, iva_total, total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, v.getFacturaId());
            ps.setInt(2, v.getVersionNum());
            ps.setString(3, v.getNumero());
            ps.setString(4, FECHA.format(v.getFechaFactura()));
            ps.setString(5, DATETIME.format(v.getFechaGuardado()));
            ps.setString(6, v.getEstado().name());
            ps.setInt(7, v.getDescuentoPorcentaje());
            ps.setString(8, v.getObservaciones());
            ps.setString(9, v.getReferenciaRectifica());
            ps.setString(10, v.getCliNombre());
            ps.setString(11, v.getCliNif());
            ps.setString(12, v.getCliDireccion());
            ps.setString(13, v.getCliCp());
            ps.setString(14, v.getCliLocalidad());
            ps.setString(15, v.getCliProvincia());
            ps.setString(16, nzTexto(v.getCliEmail()));
            ps.setString(17, nzTexto(v.getFormaPago()));
            ps.setString(18, v.getVencimiento() == null ? null : FECHA.format(v.getVencimiento()));
            ps.setString(19, nzTexto(v.getRealizadaPor()));
            ps.setString(20, v.getBaseTotal() == null ? "0.00" : v.getBaseTotal().toPlainString());
            ps.setString(21, v.getIvaTotal() == null ? "0.00" : v.getIvaTotal().toPlainString());
            ps.setString(22, v.getTotal() == null ? "0.00" : v.getTotal().toPlainString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public int maxVersion(long facturaId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT COALESCE(MAX(version_num), 0) FROM factura_version WHERE factura_id = ?")) {
            ps.setLong(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void actualizarVersion(FacturaVersion v) throws SQLException {
        String sql = """
                UPDATE factura_version SET
                    numero = ?, fecha_factura = ?, fecha_guardado = ?, estado = ?,
                    descuento_porcentaje = ?, observaciones = ?, referencia_rectifica = ?,
                    cli_nombre = ?, cli_nif = ?, cli_direccion = ?, cli_cp = ?, cli_localidad = ?,
                    cli_provincia = ?, cli_email = ?, forma_pago = ?, vencimiento = ?, realizada_por = ?,
                    base_total = ?, iva_total = ?, total = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, v.getNumero());
            ps.setString(2, FECHA.format(v.getFechaFactura()));
            ps.setString(3, DATETIME.format(v.getFechaGuardado()));
            ps.setString(4, v.getEstado().name());
            ps.setInt(5, v.getDescuentoPorcentaje());
            ps.setString(6, v.getObservaciones());
            ps.setString(7, v.getReferenciaRectifica());
            ps.setString(8, v.getCliNombre());
            ps.setString(9, v.getCliNif());
            ps.setString(10, v.getCliDireccion());
            ps.setString(11, v.getCliCp());
            ps.setString(12, v.getCliLocalidad());
            ps.setString(13, v.getCliProvincia());
            ps.setString(14, nzTexto(v.getCliEmail()));
            ps.setString(15, nzTexto(v.getFormaPago()));
            ps.setString(16, v.getVencimiento() == null ? null : FECHA.format(v.getVencimiento()));
            ps.setString(17, nzTexto(v.getRealizadaPor()));
            ps.setString(18, v.getBaseTotal() == null ? "0.00" : v.getBaseTotal().toPlainString());
            ps.setString(19, v.getIvaTotal() == null ? "0.00" : v.getIvaTotal().toPlainString());
            ps.setString(20, v.getTotal() == null ? "0.00" : v.getTotal().toPlainString());
            ps.setLong(21, v.getId());
            ps.executeUpdate();
        }
    }

    public FacturaVersion getById(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM factura_version WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<FacturaVersion> getVersiones(long facturaId) throws SQLException {
        List<FacturaVersion> lista = new ArrayList<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM factura_version WHERE factura_id = ? ORDER BY version_num")) {
            ps.setLong(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
        }
        return lista;
    }

    public FacturaVersion ultimaVersion(long facturaId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT * FROM factura_version WHERE factura_id = ? ORDER BY version_num DESC LIMIT 1")) {
            ps.setLong(1, facturaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private FacturaVersion map(ResultSet rs) throws SQLException {
        FacturaVersion v = new FacturaVersion();
        v.setId(rs.getLong("id"));
        v.setFacturaId(rs.getLong("factura_id"));
        v.setVersionNum(rs.getInt("version_num"));
        v.setNumero(rs.getString("numero"));
        v.setFechaFactura(LocalDate.parse(rs.getString("fecha_factura"), FECHA));
        v.setFechaGuardado(LocalDateTime.parse(rs.getString("fecha_guardado"), DATETIME));
        v.setEstado(EstadoFactura.from(rs.getString("estado")));
        v.setDescuentoPorcentaje(rs.getInt("descuento_porcentaje"));
        v.setObservaciones(rs.getString("observaciones"));
        v.setReferenciaRectifica(rs.getString("referencia_rectifica"));
        v.setCliNombre(rs.getString("cli_nombre"));
        v.setCliNif(rs.getString("cli_nif"));
        v.setCliDireccion(rs.getString("cli_direccion"));
        v.setCliCp(rs.getString("cli_cp"));
        v.setCliLocalidad(rs.getString("cli_localidad"));
        v.setCliProvincia(rs.getString("cli_provincia"));
        v.setCliEmail(rs.getString("cli_email"));
        v.setFormaPago(rs.getString("forma_pago"));
        String venc = rs.getString("vencimiento");
        v.setVencimiento(venc == null || venc.isBlank() ? null : LocalDate.parse(venc, FECHA));
        v.setRealizadaPor(rs.getString("realizada_por"));
        v.setBaseTotal(new BigDecimal(rs.getString("base_total")));
        v.setIvaTotal(new BigDecimal(rs.getString("iva_total")));
        v.setTotal(new BigDecimal(rs.getString("total")));
        return v;
    }

    private String nzTexto(String s) {
        return s == null ? "" : s;
    }
}
