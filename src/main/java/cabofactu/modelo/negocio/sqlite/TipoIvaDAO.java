package cabofactu.modelo.negocio.sqlite;

import cabofactu.modelo.dominio.TipoIva;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoIvaDAO {

    public List<TipoIva> listar(boolean soloActivos) {
        try {
            String sql = "SELECT * FROM tipo_iva" + (soloActivos ? " WHERE activo = 1" : "") + " ORDER BY id";
            List<TipoIva> lista = new ArrayList<>();
            try (Statement st = Conexion.establecerConexion().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public TipoIva getById(long id) {
        try {
            try (PreparedStatement ps = Conexion.establecerConexion().prepareStatement("SELECT * FROM tipo_iva WHERE id = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public long insertar(TipoIva t) {
        try {
            String sql = "INSERT INTO tipo_iva (nombre, porcentaje, motivo_exencion, activo, es_suplido) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = Conexion.establecerConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getNombre());
                if (t.isExento()) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, t.getPorcentaje());
                }
                ps.setString(3, t.getMotivoExencion());
                ps.setInt(4, t.isActivo() ? 1 : 0);
                ps.setInt(5, t.isEsSuplido() ? 1 : 0);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public void actualizar(TipoIva t) {
        try {
            String sql = "UPDATE tipo_iva SET nombre = ?, porcentaje = ?, motivo_exencion = ?, activo = ?, es_suplido = ? WHERE id = ?";
            try (PreparedStatement ps = Conexion.establecerConexion().prepareStatement(sql)) {
                ps.setString(1, t.getNombre());
                if (t.isExento()) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, t.getPorcentaje());
                }
                ps.setString(3, t.getMotivoExencion());
                ps.setInt(4, t.isActivo() ? 1 : 0);
                ps.setInt(5, t.isEsSuplido() ? 1 : 0);
                ps.setLong(6, t.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public void setActivo(long id, boolean activo) {
        try {
            try (PreparedStatement ps = Conexion.establecerConexion().prepareStatement("UPDATE tipo_iva SET activo = ? WHERE id = ?")) {
                ps.setInt(1, activo ? 1 : 0);
                ps.setLong(2, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    /**
     * True si el tipo de IVA aparece en lineas del historico (entonces ya no
     * debe poder modificarse su porcentaje ni eliminarse fisicamente).
     */
    public boolean enUso(long id) {
        try {
            try (PreparedStatement ps = Conexion.establecerConexion().prepareStatement(
                    "SELECT COUNT(*) FROM factura_linea WHERE tipo_iva_id = ?")) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatosException(e);
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
        t.setEsSuplido(rs.getInt("es_suplido") == 1);
        return t;
    }
}
