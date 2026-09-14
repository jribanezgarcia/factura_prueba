package cabofactu.modelo.negocio.sqlite;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NumeroDisponibleRepository {

    public void insertar(long serieId, int anio, int correlativo) {
        try {
            String sql = "INSERT OR IGNORE INTO numero_disponible (serie_id, anio, correlativo) VALUES (?, ?, ?)";
            try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
                ps.setLong(1, serieId);
                ps.setInt(2, anio);
                ps.setInt(3, correlativo);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public List<Integer> listar(long serieId, int anio) {
        try {
            List<Integer> lista = new ArrayList<>();
            String sql = "SELECT correlativo FROM numero_disponible WHERE serie_id = ? AND anio = ? ORDER BY correlativo";
            try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
                ps.setLong(1, serieId);
                ps.setInt(2, anio);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(rs.getInt(1));
                    }
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }

    public boolean eliminar(long serieId, int anio, int correlativo) {
        try {
            String sql = "DELETE FROM numero_disponible WHERE serie_id = ? AND anio = ? AND correlativo = ?";
            try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
                ps.setLong(1, serieId);
                ps.setInt(2, anio);
                ps.setInt(3, correlativo);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatosException(e);
        }
    }
}
