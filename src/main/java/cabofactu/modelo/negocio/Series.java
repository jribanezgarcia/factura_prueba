package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.FormatoNumero;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Las series de numeración de la empresa activa y sus números: es el único
 * sitio con el SQL de la tabla serie y con las reglas de numeración. El
 * siguiente número se calcula a partir de las facturas, así que no hay ningún
 * contador que se pueda desincronizar.
 */
public class Series {

    private static Series series;

    private Series() {
    }

    public static Series getSeries() {
        if (series == null) {
            series = new Series();
        }
        return series;
    }

    /** Todas las series, ordenadas por código. */
    public List<Serie> listado() throws Exception {
        String consulta = "SELECT id, codigo, descripcion, es_rectificativa, sufijo_fecha "
                + "FROM serie ORDER BY codigo";
        List<Serie> lista = new ArrayList<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
             ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                lista.add(crearSerie(filas));
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Una serie por su id, o null si no está. */
    public Serie buscar(long id) throws Exception {
        String consulta = "SELECT id, codigo, descripcion, es_rectificativa, sufijo_fecha "
                + "FROM serie WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearSerie(filas);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Damos de alta la serie y devolvemos el id que le ha puesto la base de datos. */
    public long alta(Serie serie) throws Exception {
        if (serie == null) {
            throw new Exception("Indique los datos de la serie.");
        }
        comprobarCodigo(serie, null);
        String insertar = "INSERT INTO serie (codigo, descripcion, es_rectificativa, sufijo_fecha) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = Conexion.establecerConexion()
                .prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
            ponerDatos(sentencia, serie);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar la serie.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                claves.next();
                return claves.getLong(1);
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos los cambios de la serie, con las mismas comprobaciones del alta. */
    public void modificar(Serie serie) throws Exception {
        if (serie == null) {
            throw new Exception("Indique los datos de la serie.");
        }
        if (buscar(serie.getId()) == null) {
            throw new Exception("No se ha encontrado la serie.");
        }
        comprobarCodigo(serie, serie.getId());
        String actualizar = "UPDATE serie SET codigo = ?, descripcion = ?, es_rectificativa = ?, "
                + "sufijo_fecha = ? WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            ponerDatos(sentencia, serie);
            sentencia.setLong(5, serie.getId());
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar la serie.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Borramos la serie, que no puede tener ninguna factura. */
    public void baja(long id) throws Exception {
        if (tieneFacturas(id)) {
            throw new Exception("La serie tiene facturas (activas o históricas) y no se puede eliminar. "
                    + "El histórico no se elimina.");
        }
        String borrar = "DELETE FROM serie WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(borrar)) {
            sentencia.setLong(1, id);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado la serie.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** True si la serie tiene alguna factura, activa o anulada. */
    public boolean tieneFacturas(long id) throws Exception {
        String consulta = "SELECT COUNT(*) FROM factura WHERE serie_id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return filas.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /**
     * Comprobamos el código contra las demás series: no puede repetirse y solo
     * puede haber una serie sin código. Al modificar, la propia serie no cuenta.
     */
    private void comprobarCodigo(Serie serie, Long mismoId) throws Exception {
        boolean sinCodigo = serie.getCodigo() == null || serie.getCodigo().isBlank();
        for (Serie otra : listado()) {
            if (mismoId != null && mismoId.equals(otra.getId())) {
                continue;
            }
            if (sinCodigo) {
                if (otra.getCodigo() == null || otra.getCodigo().isBlank()) {
                    throw new Exception("Solo puede haber una serie sin código. Ponle un código para distinguirla.");
                }
            } else if (serie.getCodigo().equalsIgnoreCase(otra.getCodigo())) {
                throw new Exception("Ya existe una serie con el código " + serie.getCodigo() + ".");
            }
        }
    }

    /** Ponemos los cuatro datos de la serie en la sentencia, para el alta y la modificación. */
    private void ponerDatos(PreparedStatement sentencia, Serie serie) throws SQLException {
        sentencia.setString(1, serie.getCodigo());
        sentencia.setString(2, serie.getDescripcion());
        int rectificativa = 0;
        if (serie.isEsRectificativa()) {
            rectificativa = 1;
        }
        sentencia.setInt(3, rectificativa);
        String formato = "MES";
        if (serie.getFormato() != null) {
            formato = serie.getFormato().name();
        }
        sentencia.setString(4, formato);
    }

    /** Pasamos la fila a objeto: el constructor con los obligatorios y después el id. */
    private Serie crearSerie(ResultSet fila) throws Exception {
        String textoFormato = fila.getString("sufijo_fecha");
        FormatoNumero formato = FormatoNumero.MES;
        if (textoFormato != null && !textoFormato.isBlank()) {
            formato = FormatoNumero.valueOf(textoFormato);
        }
        Serie serie = new Serie(fila.getString("codigo"), fila.getString("descripcion"), formato,
                fila.getInt("es_rectificativa") == 1);
        serie.setId(fila.getLong("id"));
        return serie;
    }

    /**
     * Correlativos que ya tiene alguna factura de esa serie en ese año, anuladas
     * incluidas: una factura anulada conserva su número para siempre.
     *
     * Cómo funciona: el año sale de la fecha de la última versión de cada factura.
     * Cuando las facturas dejen de tener versiones, esta consulta será de una sola tabla.
     * El CAST es necesario: sin él, comparar el texto del año con el número no
     * devuelve ninguna fila.
     */
    private Set<Integer> correlativosUsados(long serieId, int anio) throws Exception {
        String consulta = """
                SELECT f.correlativo FROM factura f
                JOIN factura_version v ON v.id = (
                    SELECT v2.id FROM factura_version v2 WHERE v2.factura_id = f.id
                    ORDER BY v2.version_num DESC LIMIT 1)
                WHERE f.serie_id = ? AND CAST(strftime('%Y', v.fecha_factura) AS INTEGER) = ?
                """;
        Set<Integer> usados = new HashSet<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, serieId);
            sentencia.setInt(2, anio);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    usados.add(filas.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return usados;
    }

    /**
     * Correlativos que tiene alguna factura activa de esa serie en ese año.
     *
     * Cómo funciona: igual que los usados, pero solo con la última versión en
     * estado EMITIDA. Es lo que mira el restaurar de una factura anulada.
     */
    private Set<Integer> correlativosActivos(long serieId, int anio) throws Exception {
        String consulta = """
                SELECT f.correlativo FROM factura f
                JOIN factura_version v ON v.id = (
                    SELECT v2.id FROM factura_version v2 WHERE v2.factura_id = f.id
                    ORDER BY v2.version_num DESC LIMIT 1)
                WHERE f.serie_id = ? AND v.estado = 'EMITIDA'
                    AND CAST(strftime('%Y', v.fecha_factura) AS INTEGER) = ?
                """;
        Set<Integer> activos = new HashSet<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, serieId);
            sentencia.setInt(2, anio);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    activos.add(filas.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return activos;
    }

    /** El siguiente correlativo de la serie en el año de esa fecha: el mayor usado más uno. */
    public int siguienteCorrelativo(Serie serie, LocalDate fecha) throws Exception {
        int mayor = 0;
        for (int usado : correlativosUsados(serie.getId(), anioDe(fecha))) {
            if (usado > mayor) {
                mayor = usado;
            }
        }
        return mayor + 1;
    }

    /**
     * Los correlativos libres por debajo del mayor: los que quedaron vacíos al
     * borrar facturas. Van de menor a mayor.
     */
    public List<Integer> huecos(Serie serie, LocalDate fecha) throws Exception {
        Set<Integer> usados = correlativosUsados(serie.getId(), anioDe(fecha));
        List<Integer> libres = new ArrayList<>();
        int siguiente = siguienteCorrelativo(serie, fecha);
        for (int correlativo = 1; correlativo < siguiente; correlativo++) {
            if (!usados.contains(correlativo)) {
                libres.add(correlativo);
            }
        }
        return libres;
    }

    /**
     * Tantos correlativos libres como haga falta para las mensuales: primero
     * los huecos, si se piden, y después los siguientes, sin contador.
     */
    public List<Integer> proponerNumeros(Serie serie, int anio, int cantidad, boolean usarHuecos)
            throws Exception {
        if (cantidad <= 0) {
            return new ArrayList<>();
        }
        Set<Integer> usados = correlativosUsados(serie.getId(), anio);
        int mayor = 0;
        for (int usado : usados) {
            if (usado > mayor) {
                mayor = usado;
            }
        }
        List<Integer> propuesta = new ArrayList<>();
        if (usarHuecos) {
            for (int correlativo = 1; correlativo <= mayor; correlativo++) {
                if (!usados.contains(correlativo)) {
                    propuesta.add(correlativo);
                }
                if (propuesta.size() == cantidad) {
                    return propuesta;
                }
            }
        }
        int correlativo = mayor + 1;
        while (propuesta.size() < cantidad) {
            propuesta.add(correlativo);
            correlativo++;
        }
        return propuesta;
    }

    /**
     * Formamos el número con el formato de la serie.
     * - MES: CODIGO-CORRELATIVO/MES (p. ej. C-59/8) o CORRELATIVO/MES (p. ej. 59/8)
     * - ANIO: CODIGO-CORRELATIVO-ANIO (p. ej. C-59-2026) o CORRELATIVO-ANIO (p. ej. 59-2026)
     * - NINGUNO: CODIGO-CORRELATIVO (p. ej. C-59) o CORRELATIVO (p. ej. 59)
     * - Rectificativa: siempre CODIGO-CORRELATIVO (p. ej. R-1), sin fecha.
     * - El guion separa código y correlativo; la barra separa correlativo y mes.
     */
    public String formarNumero(Serie serie, int correlativo, LocalDate fecha) {
        String codigo = serie.getCodigo();
        boolean tieneCodigo = codigo != null && !codigo.isBlank();
        String prefijo;
        if (tieneCodigo) {
            prefijo = codigo + "-";
        } else {
            prefijo = "";
        }
        if (serie.isEsRectificativa()) {
            return prefijo + correlativo;
        }
        FormatoNumero formato = serie.getFormato();
        if (formato == null) {
            formato = FormatoNumero.MES;
        }
        if (formato == FormatoNumero.MES) {
            return prefijo + correlativo + "/" + fecha.getMonthValue();
        }
        if (formato == FormatoNumero.ANIO) {
            return prefijo + correlativo + "-" + fecha.getYear();
        }
        return prefijo + correlativo;
    }

    /**
     * Extrae el correlativo de un número escrito manualmente (el campo número
     * es editable en creación). Formatos según el formato:
     * MES: C-59/8 o 59/8
     * ANIO: C-59-2026 o 59-2026
     * NINGUNO: C-59 o 59
     * Rectificativa: C-1 o 1
     * Devuelve null si el número no se ajusta a la serie indicada.
     */
    public Integer parseCorrelativo(Serie serie, String numero) {
        if (numero == null || serie == null) {
            return null;
        }
        String texto = numero.trim();
        String codigo = serie.getCodigo();
        boolean tieneCodigo = codigo != null && !codigo.isBlank();
        String prefijo;
        if (tieneCodigo) {
            prefijo = codigo + "-";
        } else {
            prefijo = "";
        }
        if (!texto.startsWith(prefijo)) {
            return null;
        }
        String resto = texto.substring(prefijo.length());
        if (serie.isEsRectificativa()) {
            try {
                return Integer.parseInt(resto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        FormatoNumero formato = serie.getFormato();
        if (formato == null) {
            formato = FormatoNumero.MES;
        }
        String parte;
        if (formato == FormatoNumero.MES) {
            int barra = resto.indexOf('/');
            if (barra >= 0) {
                parte = resto.substring(0, barra);
            } else {
                parte = resto;
            }
        } else if (formato == FormatoNumero.ANIO) {
            int guion = resto.indexOf('-');
            if (guion >= 0) {
                parte = resto.substring(0, guion);
            } else {
                parte = resto;
            }
        } else {
            parte = resto;
        }
        try {
            return Integer.parseInt(parte);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** True si ese correlativo lo tiene una factura activa de la serie en el año de la fecha. */
    public boolean correlativoOcupado(Serie serie, int correlativo, LocalDate fecha) throws Exception {
        return correlativosActivos(serie.getId(), anioDe(fecha)).contains(correlativo);
    }

    private int anioDe(LocalDate fecha) {
        return fecha.getYear();
    }
}
