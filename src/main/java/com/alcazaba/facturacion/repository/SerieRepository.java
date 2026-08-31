package com.alcazaba.facturacion.repository;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.model.Serie;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerieRepository {

    public List<Serie> listar() throws SQLException {
        List<Serie> lista = new ArrayList<>();
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM serie ORDER BY codigo")) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }
        return lista;
    }

    public Serie getById(long id) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM serie WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Serie getByCodigo(String codigo) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM serie WHERE codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public long insertar(Serie s) throws SQLException {
        String sql = "INSERT INTO serie (codigo, descripcion, es_rectificativa, siguiente_correlativo, reutilizar_anulados, sufijo_fecha) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getCodigo());
            ps.setString(2, s.getDescripcion());
            ps.setInt(3, s.isEsRectificativa() ? 1 : 0);
            ps.setInt(4, s.getSiguienteCorrelativo());
            ps.setInt(5, s.isReutilizarAnulados() ? 1 : 0);
            ps.setString(6, s.getSufijoFecha() != null ? s.getSufijoFecha().name() : "MES");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                long id = rs.getLong(1);
                inicializarSiguiente(id, s.getSiguienteCorrelativo());
                return id;
            }
        }
    }

    /**
     * Siembra el contador del anio en curso con el valor configurado de la
     * serie. El resto de anios arrancan en 1.
     */
    private void inicializarSiguiente(long serieId, int siguiente) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT OR IGNORE INTO serie_siguiente (serie_id, anio, siguiente) VALUES (?, ?, ?)")) {
            ps.setLong(1, serieId);
            ps.setInt(2, LocalDate.now().getYear());
            ps.setInt(3, siguiente);
            ps.executeUpdate();
        }
    }

    public void actualizar(Serie s) throws SQLException {
        String sql = "UPDATE serie SET codigo = ?, descripcion = ?, es_rectificativa = ?, siguiente_correlativo = ?, "
                + "reutilizar_anulados = ?, sufijo_fecha = ? WHERE id = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getCodigo());
            ps.setString(2, s.getDescripcion());
            ps.setInt(3, s.isEsRectificativa() ? 1 : 0);
            ps.setInt(4, s.getSiguienteCorrelativo());
            ps.setInt(5, s.isReutilizarAnulados() ? 1 : 0);
            ps.setString(6, s.getSufijoFecha() != null ? s.getSufijoFecha().name() : "MES");
            ps.setLong(7, s.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarSiguiente(long id, int siguiente) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "UPDATE serie SET siguiente_correlativo = ? WHERE id = ?")) {
            ps.setInt(1, siguiente);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Siguiente correlativo configurado para la serie en un anio concreto.
     * Si el anio no tiene contador propio, se parte de 1.
     */
    public int getSiguiente(long serieId, int anio) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT siguiente FROM serie_siguiente WHERE serie_id = ? AND anio = ?")) {
            ps.setLong(1, serieId);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        }
    }

    /**
     * Guarda el siguiente correlativo de la serie para un anio concreto.
     */
    public void actualizarSiguiente(long serieId, int anio, int siguiente) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "INSERT INTO serie_siguiente (serie_id, anio, siguiente) VALUES (?, ?, ?) "
                        + "ON CONFLICT(serie_id, anio) DO UPDATE SET siguiente = excluded.siguiente")) {
            ps.setLong(1, serieId);
            ps.setInt(2, anio);
            ps.setInt(3, siguiente);
            ps.executeUpdate();
        }
    }

    /**
     * Correlativos de facturas cuya ultima version es ANULADA en esta serie y
     * cuyo ultimo guardado cae en el anio indicado.
     */
    public Set<Integer> correlativosAnuladas(long serieId, int anio) throws SQLException {
        String sql = """
                SELECT f.correlativo FROM factura f
                JOIN factura_version v ON v.id = (
                    SELECT v2.id FROM factura_version v2 WHERE v2.factura_id = f.id
                    ORDER BY v2.version_num DESC LIMIT 1
                )
                WHERE f.serie_id = ? AND v.estado = 'ANULADA'
                    AND CAST(strftime('%Y', v.fecha_factura) AS INTEGER) = ?
                """;
        Set<Integer> set = new HashSet<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setLong(1, serieId);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getInt(1));
                }
            }
        }
        return set;
    }

    /**
     * Correlativos de facturas cuya ultima version es EMITIDA en esta serie y
     * cuyo ultimo guardado cae en el anio indicado.
     */
    public Set<Integer> correlativosActivos(long serieId, int anio) throws SQLException {
        String sql = """
                SELECT f.correlativo FROM factura f
                JOIN factura_version v ON v.id = (
                    SELECT v2.id FROM factura_version v2 WHERE v2.factura_id = f.id
                    ORDER BY v2.version_num DESC LIMIT 1
                )
                WHERE f.serie_id = ? AND v.estado = 'EMITIDA'
                    AND CAST(strftime('%Y', v.fecha_factura) AS INTEGER) = ?
                """;
        Set<Integer> set = new HashSet<>();
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setLong(1, serieId);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getInt(1));
                }
            }
        }
        return set;
    }

    /**
     * Borra la serie y su contador de anios. Solo debe llamarse si la serie no
     * tiene facturas, o el borrado fallara por la clave foranea.
     */
    public void eliminar(long serieId) throws SQLException {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM serie_siguiente WHERE serie_id = ?")) {
            ps.setLong(1, serieId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = Database.getConnection().prepareStatement("DELETE FROM serie WHERE id = ?")) {
            ps.setLong(1, serieId);
            ps.executeUpdate();
        }
    }

    private Serie map(ResultSet rs) throws SQLException {
        Serie s = new Serie();
        s.setId(rs.getLong("id"));
        s.setCodigo(rs.getString("codigo"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setEsRectificativa(rs.getInt("es_rectificativa") == 1);
        s.setSiguienteCorrelativo(rs.getInt("siguiente_correlativo"));
        s.setReutilizarAnulados(rs.getInt("reutilizar_anulados") == 1);
        String sufijo = rs.getString("sufijo_fecha");
        if (sufijo != null && !sufijo.isBlank()) {
            s.setSufijoFecha(Serie.SufijoFecha.valueOf(sufijo));
        }
        return s;
    }
}
