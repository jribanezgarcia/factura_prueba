package cabofactu.modelo.negocio;

import cabofactu.modelo.dominio.Cliente;
import cabofactu.modelo.dominio.EstadoFactura;
import cabofactu.modelo.dominio.Factura;
import cabofactu.modelo.dominio.FiltrosHistorial;
import cabofactu.modelo.dominio.LineaFactura;
import cabofactu.modelo.dominio.ResumenFactura;
import cabofactu.modelo.dominio.Serie;
import cabofactu.modelo.dominio.TipoRetencion;
import cabofactu.modelo.negocio.sqlite.Conexion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Las facturas de la empresa activa: es el único sitio con el SQL de las
 * tablas factura y factura_linea. Cada factura es una sola fila; editarla la
 * sobrescribe y anularla cambia el estado de esa misma fila.
 */
public class Facturas {

    private static Facturas facturas;

    private Facturas() {
    }

    public static Facturas getFacturas() {
        if (facturas == null) {
            facturas = new Facturas();
        }
        return facturas;
    }

    /** Damos de alta la factura con sus líneas: o se guarda todo o no se guarda nada. */
    public long alta(Factura factura) throws Exception {
        comprobarFactura(factura);
        Connection conexion = abrirConexion();
        try {
            conexion.setAutoCommit(false);
            long id = insertar(factura);
            conexion.commit();
            return id;
        } catch (SQLException e) {
            revertir(conexion);
            throw new Exception("Error SQLite: " + e.getMessage());
        } catch (Exception e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrarTransaccion(conexion);
        }
    }

    /** Damos de alta varias facturas de una vez: o se guardan todas o no se guarda ninguna. */
    public void altaVarias(List<Factura> facturas) throws Exception {
        if (facturas == null) {
            throw new Exception("No hay facturas para guardar.");
        }
        for (Factura factura : facturas) {
            comprobarFactura(factura);
        }
        Connection conexion = abrirConexion();
        try {
            conexion.setAutoCommit(false);
            for (Factura factura : facturas) {
                insertar(factura);
            }
            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new Exception("Error SQLite: " + e.getMessage());
        } catch (Exception e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrarTransaccion(conexion);
        }
    }

    /** Sobrescribimos la factura y sustituimos sus líneas, sin cambiarla de año. */
    public void modificar(Factura factura) throws Exception {
        if (factura == null || factura.getId() == null) {
            throw new Exception("No se ha encontrado la factura.");
        }
        Factura actual = buscar(factura.getId());
        if (actual == null) {
            throw new Exception("No se ha encontrado la factura.");
        }
        if (actual.getEstado() == EstadoFactura.ANULADA) {
            throw new Exception("Una factura anulada no se puede editar.");
        }
        conservarIdentidad(factura, actual);
        comprobarFactura(factura);

        Connection conexion = abrirConexion();
        try {
            conexion.setAutoCommit(false);
            asegurarCliente(factura.getCliente());
            calcularTotales(factura);
            actualizar(factura);
            borrarLineas(factura.getId());
            guardarLineas(factura.getId(), factura.getLineas());
            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new Exception("Error SQLite: " + e.getMessage());
        } catch (Exception e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrarTransaccion(conexion);
        }
    }

    /** Borramos la factura y sus líneas, salvo que una rectificativa la corrija. */
    public void baja(long id) throws Exception {
        Factura actual = buscar(id);
        if (actual == null) {
            throw new Exception("No se ha encontrado la factura.");
        }
        Connection conexion = abrirConexion();
        try {
            conexion.setAutoCommit(false);
            String rectificativa = buscarRectificativa(id);
            if (rectificativa != null) {
                throw new Exception(String.format("La factura %s tiene la rectificativa %s y no se puede borrar.",
                        actual.getNumero(), rectificativa));
            }
            borrarLineas(id);
            String borrar = "DELETE FROM factura WHERE id = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(borrar)) {
                sentencia.setLong(1, id);
                int filas = sentencia.executeUpdate();
                if (filas == 0) {
                    throw new Exception("No se ha encontrado la factura.");
                }
            }
            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new Exception("Error SQLite: " + e.getMessage());
        } catch (Exception e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrarTransaccion(conexion);
        }
    }

    /** Una factura por su id, con su serie, su cliente, sus líneas y sus totales, o null si no está. */
    public Factura buscar(long id) throws Exception {
        String consulta = """
                SELECT id, serie_id, anio, correlativo, numero, fecha, estado, cliente_id,
                    cli_nombre, cli_nif, cli_direccion, cli_cp, cli_localidad, cli_provincia, cli_email,
                    descuento, observaciones, rectifica_id, forma_pago, vencimiento, realizada_por,
                    retencion_id, retencion_nombre, retencion_porcentaje,
                    base_total, iva_total, importe_retencion, total_suplidos, total
                FROM factura WHERE id = ?
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return crearFactura(filas, true);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** El histórico: una Factura por fila, sin líneas, ordenada por serie, año y correlativo. */
    public List<Factura> listado(FiltrosHistorial filtros) throws Exception {
        FiltrosHistorial criterios = filtros;
        if (criterios == null) {
            criterios = new FiltrosHistorial();
        }
        StringBuilder consulta = new StringBuilder("""
                SELECT f.id, f.serie_id, f.anio, f.correlativo, f.numero, f.fecha, f.estado, f.cliente_id,
                    f.cli_nombre, f.cli_nif, f.cli_direccion, f.cli_cp, f.cli_localidad, f.cli_provincia, f.cli_email,
                    f.descuento, f.observaciones, f.rectifica_id, f.forma_pago, f.vencimiento, f.realizada_por,
                    f.retencion_id, f.retencion_nombre, f.retencion_porcentaje,
                    f.base_total, f.iva_total, f.importe_retencion, f.total_suplidos, f.total
                FROM factura f JOIN serie s ON s.id = f.serie_id WHERE 1 = 1
                """);
        List<Object> parametros = parametrosListado(criterios, consulta);
        consulta.append(" ORDER BY s.codigo, f.anio, f.correlativo");

        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta.toString())) {
            ponerParametros(sentencia, parametros);
            return leerListado(sentencia);
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Añadimos los filtros del histórico a la consulta y devolvemos sus parámetros en orden. */
    private List<Object> parametrosListado(FiltrosHistorial criterios, StringBuilder consulta) {
        List<Object> parametros = new ArrayList<>();
        if (criterios.getSerieCodigo() != null && !criterios.getSerieCodigo().isBlank()) {
            consulta.append(" AND s.codigo = ?");
            parametros.add(criterios.getSerieCodigo().trim());
        }
        if (criterios.getClienteTexto() != null && !criterios.getClienteTexto().isBlank()) {
            consulta.append(" AND (f.cli_nombre LIKE ? OR f.cli_nif LIKE ?)");
            String parecido = "%" + criterios.getClienteTexto().trim() + "%";
            parametros.add(parecido);
            parametros.add(parecido);
        }
        if (criterios.getFechaDesde() != null) {
            consulta.append(" AND f.fecha >= ?");
            parametros.add(criterios.getFechaDesde().toString());
        }
        if (criterios.getFechaHasta() != null) {
            consulta.append(" AND f.fecha <= ?");
            parametros.add(criterios.getFechaHasta().toString());
        }
        if (criterios.getImporteDesde() != null) {
            consulta.append(" AND CAST(f.total AS REAL) >= ?");
            parametros.add(criterios.getImporteDesde());
        }
        if (criterios.getImporteHasta() != null) {
            consulta.append(" AND CAST(f.total AS REAL) <= ?");
            parametros.add(criterios.getImporteHasta());
        }
        if (criterios.getEstado() != null) {
            consulta.append(" AND f.estado = ?");
            parametros.add(criterios.getEstado().name());
        }
        return parametros;
    }

    /** Leemos el histórico ya ejecutado: una Factura por fila, sin líneas. */
    private List<Factura> leerListado(PreparedStatement sentencia) throws Exception {
        List<Factura> lista = new ArrayList<>();
        try (ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                lista.add(crearFactura(filas, false));
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Anulamos la factura: cambia el estado de la misma fila. */
    public void anular(long id) throws Exception {
        cambiarEstado(id, EstadoFactura.ANULADA);
    }

    /** Restauramos la factura anulada a emitida, conservando su número. */
    public void restaurar(long id) throws Exception {
        cambiarEstado(id, EstadoFactura.EMITIDA);
    }

    /** Creamos la rectificativa en la serie R, copiando los datos de la factura original. */
    public long rectificar(long id, LocalDate fecha) throws Exception {
        if (fecha == null) {
            throw new Exception("Indique la fecha de la rectificativa.");
        }
        Factura original = buscar(id);
        if (original == null) {
            throw new Exception("No se ha encontrado la factura.");
        }
        Serie serie = Series.getSeries().rectificativa();
        if (serie == null) {
            throw new Exception("No hay ninguna serie de rectificativas. Créala en Configuración.");
        }
        ValidacionCliente.comprobar(original.getCliente());
        Cliente cliente = copiarCliente(original.getCliente());
        TipoRetencion retencion = copiarRetencion(original.getRetencion());
        List<LineaFactura> lineas = copiarLineas(original.getLineas());
        Factura rectificativa = new Factura(serie, fecha, cliente);
        rectificativa.setDescuento(original.getDescuento());
        rectificativa.setObservaciones(original.getObservaciones());
        rectificativa.setRectificaId(original.getId());
        rectificativa.setRetencion(retencion);
        rectificativa.setLineas(lineas);
        return alta(rectificativa);
    }

    /** Copiamos el cliente original para la rectificativa, conservando su id del maestro. */
    private Cliente copiarCliente(Cliente original) throws Exception {
        Cliente cliente = new Cliente(original.getNombre(), original.getNif(), original.getDireccion(),
                original.getCp(), original.getLocalidad(), original.getProvincia());
        cliente.setId(original.getId());
        cliente.setEmail(original.getEmail());
        return cliente;
    }

    /** Copiamos la retención original para la rectificativa, con su id congelado. */
    private TipoRetencion copiarRetencion(TipoRetencion original) throws Exception {
        if (original == null) {
            return null;
        }
        return new TipoRetencion(original);
    }

    /** Copiamos las líneas originales para la rectificativa, sin sus ids. */
    private List<LineaFactura> copiarLineas(List<LineaFactura> originales) {
        List<LineaFactura> lineas = new ArrayList<>();
        if (originales == null) {
            return lineas;
        }
        for (LineaFactura linea : originales) {
            lineas.add(new LineaFactura(linea));
        }
        return lineas;
    }

    /** Cuántas facturas hay en la empresa activa, para el resumen de la copia. */
    public int contar() throws Exception {
        String consulta = "SELECT COUNT(*) FROM factura";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta);
                ResultSet filas = sentencia.executeQuery()) {
            filas.next();
            return filas.getInt(1);
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Cuántas líneas tiene la factura, para avisar antes de borrarla. */
    public int numeroDeLineas(long id) throws Exception {
        String consulta = "SELECT COUNT(*) FROM factura_linea WHERE factura_id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                filas.next();
                return filas.getInt(1);
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** True si ese cliente ya tiene alguna factura en ese mes, para no generarla dos veces. */
    public boolean clienteTieneFacturaEnMes(long clienteId, int anio, int mes) throws Exception {
        YearMonth periodo;
        try {
            periodo = YearMonth.of(anio, mes);
        } catch (DateTimeException e) {
            throw new Exception("Indique un mes válido.");
        }
        String consulta = "SELECT 1 FROM factura WHERE cliente_id = ? AND fecha >= ? AND fecha <= ? LIMIT 1";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, clienteId);
            sentencia.setString(2, periodo.atDay(1).toString());
            sentencia.setString(3, periodo.atEndOfMonth().toString());
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next();
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos la factura y sus líneas dentro de la transacción que abrió el llamador. */
    private long insertar(Factura factura) throws Exception {
        if (factura.getCorrelativo() <= 0) {
            factura.setCorrelativo(Series.getSeries().siguienteCorrelativo(factura.getSerie(), factura.getFecha()));
        }
        String numero = Series.getSeries().formarNumero(factura.getSerie(), factura.getCorrelativo(), factura.getFecha());
        if (Series.getSeries().correlativoOcupado(factura.getSerie(), factura.getCorrelativo(), factura.getFecha())) {
            throw new Exception(String.format("El número %s ya lo tiene otra factura de la serie %s.",
                    numero, factura.getSerie().getCodigo()));
        }
        factura.setNumero(numero);
        asegurarCliente(factura.getCliente());
        calcularTotales(factura);

        String insertar = """
                INSERT INTO factura (serie_id, anio, correlativo, numero, fecha, estado, cliente_id,
                    cli_nombre, cli_nif, cli_direccion, cli_cp, cli_localidad, cli_provincia, cli_email,
                    descuento, observaciones, rectifica_id, forma_pago, vencimiento, realizada_por,
                    retencion_id, retencion_nombre, retencion_porcentaje,
                    base_total, iva_total, importe_retencion, total_suplidos, total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion()
                .prepareStatement(insertar, Statement.RETURN_GENERATED_KEYS)) {
            ponerFactura(sentencia, factura);
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha podido guardar la factura.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                claves.next();
                long id = claves.getLong(1);
                factura.setId(id);
                guardarLineas(id, factura.getLineas());
                return id;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Guardamos los cambios de la factura dentro de la transacción que abrió modificar. */
    private void actualizar(Factura factura) throws Exception {
        String actualizar = """
                UPDATE factura SET serie_id = ?, anio = ?, correlativo = ?, numero = ?, fecha = ?, estado = ?,
                    cliente_id = ?, cli_nombre = ?, cli_nif = ?, cli_direccion = ?, cli_cp = ?, cli_localidad = ?,
                    cli_provincia = ?, cli_email = ?, descuento = ?, observaciones = ?, rectifica_id = ?,
                    forma_pago = ?, vencimiento = ?, realizada_por = ?, retencion_id = ?, retencion_nombre = ?,
                    retencion_porcentaje = ?, base_total = ?, iva_total = ?, importe_retencion = ?,
                    total_suplidos = ?, total = ? WHERE id = ?
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(actualizar)) {
            ponerFactura(sentencia, factura);
            sentencia.setLong(29, factura.getId());
            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new Exception("No se ha encontrado la factura.");
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Cambiamos el estado de la misma fila, con su propia transacción. */
    private void cambiarEstado(long id, EstadoFactura estado) throws Exception {
        Factura actual = buscar(id);
        if (actual == null) {
            throw new Exception("No se ha encontrado la factura.");
        }
        if (estado == EstadoFactura.ANULADA && actual.getEstado() != EstadoFactura.EMITIDA) {
            throw new Exception("Solo se pueden anular facturas en estado Emitida.");
        }
        if (estado == EstadoFactura.EMITIDA && actual.getEstado() != EstadoFactura.ANULADA) {
            throw new Exception("Solo se pueden restaurar facturas en estado Anulada.");
        }
        Connection conexion = abrirConexion();
        try {
            conexion.setAutoCommit(false);
            String actualizar = "UPDATE factura SET estado = ? WHERE id = ?";
            try (PreparedStatement sentencia = conexion.prepareStatement(actualizar)) {
                sentencia.setString(1, estado.name());
                sentencia.setLong(2, id);
                int filas = sentencia.executeUpdate();
                if (filas == 0) {
                    throw new Exception("No se ha encontrado la factura.");
                }
            }
            conexion.commit();
        } catch (SQLException e) {
            revertir(conexion);
            throw new Exception("Error SQLite: " + e.getMessage());
        } catch (Exception e) {
            revertir(conexion);
            throw e;
        } finally {
            cerrarTransaccion(conexion);
        }
    }

    /** Conservamos serie, correlativo, estado y rectificada: editar solo cambia el contenido y la fecha dentro del año. */
    private void conservarIdentidad(Factura factura, Factura actual) throws Exception {
        if (factura.getFecha() == null) {
            throw new Exception("Indique la fecha de la factura.");
        }
        if (factura.getAnio() != actual.getAnio()) {
            throw new Exception("Una factura emitida no puede cambiar de año. Si es de otro año, anúlala y crea una nueva.");
        }
        factura.setSerie(actual.getSerie());
        factura.setCorrelativo(actual.getCorrelativo());
        factura.setEstado(actual.getEstado());
        factura.setRectificaId(actual.getRectificaId());
        factura.setNumero(Series.getSeries().formarNumero(actual.getSerie(), actual.getCorrelativo(), factura.getFecha()));
    }

    /** Comprobamos la factura antes de tocar la base de datos, sin anidar comprobaciones. */
    private void comprobarFactura(Factura factura) throws Exception {
        if (factura == null) {
            throw new Exception("Indique los datos de la factura.");
        }
        if (factura.getSerie() == null) {
            throw new Exception("Seleccione la serie.");
        }
        if (factura.getSerie().getId() == null) {
            throw new Exception("Seleccione una serie guardada.");
        }
        if (factura.getFecha() == null) {
            throw new Exception("Indique la fecha de la factura.");
        }
        if (factura.getEstado() == null) {
            throw new Exception("Indique el estado de la factura.");
        }
        ValidacionCliente.comprobar(factura.getCliente());
        comprobarLineas(factura.getLineas());
        if (factura.getDescuento() < 0 || factura.getDescuento() > 100) {
            throw new Exception("El descuento debe estar entre 0 y 100.");
        }
    }

    /** Comprobamos que haya líneas válidas, sin anidar comprobaciones. */
    private void comprobarLineas(List<LineaFactura> lineas) throws Exception {
        if (lineas == null || lineas.isEmpty()) {
            throw new Exception("La factura debe tener al menos una línea.");
        }
        for (LineaFactura linea : lineas) {
            if (linea.getCantidad() < 1) {
                throw new Exception("La cantidad de cada línea debe ser al menos 1.");
            }
            if (linea.getPrecioUnitario() == null) {
                linea.setPrecioUnitario(BigDecimal.ZERO);
            }
            if (linea.getPrecioUnitario().signum() < 0) {
                throw new Exception("Los importes no pueden ser negativos.");
            }
            if (linea.getTipoIvaId() == null) {
                throw new Exception("Cada línea debe tener un tipo de IVA.");
            }
        }
    }

    /**
     * Si el cliente se ha escrito a mano, usamos el que ya tiene ese NIF o, si no
     * hay ninguno, lo damos de alta. Su ficha no se toca: la factura guarda su
     * propia copia de los datos.
     */
    private void asegurarCliente(Cliente cliente) throws Exception {
        if (cliente == null || cliente.getId() != null) {
            return;
        }
        Cliente existente = Clientes.getClientes().buscarPorNif(cliente.getNif());
        if (existente != null) {
            cliente.setId(existente.getId());
            return;
        }
        cliente.setId(Clientes.getClientes().alta(cliente));
    }

    /** Calculamos los cinco totales de la factura con el resumen del negocio. */
    private void calcularTotales(Factura factura) {
        ResumenFactura resumen = Calculos.resumen(factura.getLineas(), factura.getDescuento(), factura.getRetencion());
        factura.setBaseTotal(resumen.getBaseTotal());
        factura.setIvaTotal(resumen.getIvaTotal());
        factura.setImporteRetencion(resumen.getImporteRetencion());
        factura.setTotalSuplidos(resumen.getTotalSuplidos());
        factura.setTotal(resumen.getTotal());
    }

    /** Ponemos todos los datos de la factura en la sentencia, para el alta y la modificación. */
    private void ponerFactura(PreparedStatement sentencia, Factura factura) throws SQLException {
        ponerIdentificacion(sentencia, factura);
        ponerCliente(sentencia, factura.getCliente());
        ponerContenido(sentencia, factura);
        ponerTotales(sentencia, factura);
    }

    /** Ponemos serie, año, número, fecha y estado en la sentencia. */
    private void ponerIdentificacion(PreparedStatement sentencia, Factura factura) throws SQLException {
        sentencia.setLong(1, factura.getSerie().getId());
        sentencia.setInt(2, factura.getAnio());
        sentencia.setInt(3, factura.getCorrelativo());
        sentencia.setString(4, factura.getNumero());
        sentencia.setString(5, factura.getFecha().toString());
        sentencia.setString(6, factura.getEstado().name());
    }

    /** Ponemos la copia del cliente en la sentencia. */
    private void ponerCliente(PreparedStatement sentencia, Cliente cliente) throws SQLException {
        if (cliente.getId() == null) {
            sentencia.setNull(7, Types.INTEGER);
        } else {
            sentencia.setLong(7, cliente.getId());
        }
        sentencia.setString(8, cliente.getNombre());
        sentencia.setString(9, cliente.getNif());
        sentencia.setString(10, cliente.getDireccion());
        sentencia.setString(11, cliente.getCp());
        sentencia.setString(12, cliente.getLocalidad());
        sentencia.setString(13, cliente.getProvincia());
        sentencia.setString(14, cliente.getEmail());
    }

    /** Ponemos descuento, observaciones, rectificada, pago y retención en la sentencia. */
    private void ponerContenido(PreparedStatement sentencia, Factura factura) throws SQLException {
        sentencia.setInt(15, factura.getDescuento());
        sentencia.setString(16, factura.getObservaciones());
        if (factura.getRectificaId() == null) {
            sentencia.setNull(17, Types.INTEGER);
        } else {
            sentencia.setLong(17, factura.getRectificaId());
        }
        sentencia.setString(18, factura.getFormaPago());
        if (factura.getVencimiento() == null) {
            sentencia.setString(19, null);
        } else {
            sentencia.setString(19, factura.getVencimiento().toString());
        }
        sentencia.setString(20, factura.getRealizadaPor());
        if (factura.getRetencion() == null || factura.getRetencion().getId() == null) {
            sentencia.setNull(21, Types.INTEGER);
        } else {
            sentencia.setLong(21, factura.getRetencion().getId());
        }
        if (factura.getRetencion() == null) {
            sentencia.setString(22, null);
        } else {
            sentencia.setString(22, factura.getRetencion().getNombre());
        }
        if (factura.getRetencion() == null || factura.getRetencion().getPorcentaje() == null) {
            sentencia.setNull(23, Types.INTEGER);
        } else {
            sentencia.setInt(23, factura.getRetencion().getPorcentaje());
        }
    }

    /** Ponemos los cinco totales de la factura en la sentencia. */
    private void ponerTotales(PreparedStatement sentencia, Factura factura) throws SQLException {
        sentencia.setString(24, importe(factura.getBaseTotal()));
        sentencia.setString(25, importe(factura.getIvaTotal()));
        sentencia.setString(26, importe(factura.getImporteRetencion()));
        sentencia.setString(27, importe(factura.getTotalSuplidos()));
        sentencia.setString(28, importe(factura.getTotal()));
    }

    /** Guardamos las líneas en orden, sustituyendo las que hubiera. */
    private void guardarLineas(long facturaId, List<LineaFactura> lineas) throws Exception {
        List<LineaFactura> lineasAGuardar = lineas;
        if (lineasAGuardar == null) {
            lineasAGuardar = new ArrayList<>();
        }
        String insertar = """
                INSERT INTO factura_linea (factura_id, orden, cantidad, descripcion, precio_unitario,
                    tipo_iva_id, iva_nombre, iva_porcentaje, iva_motivo_exencion, es_suplido)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(insertar)) {
            int orden = 1;
            for (LineaFactura linea : lineasAGuardar) {
                ponerLinea(sentencia, facturaId, linea, orden);
                orden++;
            }
            sentencia.executeBatch();
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Ponemos una línea en la sentencia, con su orden ya normalizado. */
    private void ponerLinea(PreparedStatement sentencia, long facturaId, LineaFactura linea, int orden)
            throws SQLException {
        linea.setOrden(orden);
        sentencia.setLong(1, facturaId);
        sentencia.setInt(2, linea.getOrden());
        sentencia.setInt(3, linea.getCantidad());
        sentencia.setString(4, linea.getDescripcion());
        sentencia.setString(5, importe(linea.getPrecioUnitario()));
        if (linea.getTipoIvaId() == null) {
            sentencia.setNull(6, Types.INTEGER);
        } else {
            sentencia.setLong(6, linea.getTipoIvaId());
        }
        sentencia.setString(7, linea.getIvaNombre());
        if (linea.getIvaPorcentaje() == null) {
            sentencia.setNull(8, Types.INTEGER);
        } else {
            sentencia.setInt(8, linea.getIvaPorcentaje());
        }
        sentencia.setString(9, linea.getIvaMotivoExencion());
        int suplido = 0;
        if (linea.isEsSuplido()) {
            suplido = 1;
        }
        sentencia.setInt(10, suplido);
        sentencia.addBatch();
    }

    /** Borramos las líneas de la factura dentro de la transacción que abrió el llamador. */
    private void borrarLineas(long facturaId) throws Exception {
        String borrar = "DELETE FROM factura_linea WHERE factura_id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(borrar)) {
            sentencia.setLong(1, facturaId);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** El número de la factura que rectifica esta, o null si no rectifica ninguna. */
    private String buscarRectificativa(long id) throws Exception {
        String consulta = "SELECT numero FROM factura WHERE rectifica_id = ? ORDER BY id LIMIT 1";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return filas.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Ponemos los parámetros del listado en la sentencia, en el orden en que se pidieron. */
    private void ponerParametros(PreparedStatement sentencia, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object parametro = parametros.get(i);
            if (parametro instanceof String) {
                sentencia.setString(i + 1, (String) parametro);
            } else if (parametro instanceof BigDecimal) {
                sentencia.setBigDecimal(i + 1, (BigDecimal) parametro);
            } else {
                sentencia.setObject(i + 1, parametro);
            }
        }
    }

    /** Pasamos una fila a objeto: el constructor con los obligatorios y después los opcionales. */
    private Factura crearFactura(ResultSet fila, boolean conLineas) throws Exception {
        Serie serie = Series.getSeries().buscar(fila.getLong("serie_id"));
        if (serie == null) {
            throw new Exception("No se ha encontrado la serie de la factura.");
        }
        Cliente cliente = crearCliente(fila);
        Factura factura = new Factura(serie, LocalDate.parse(fila.getString("fecha")), cliente);
        factura.setId(fila.getLong("id"));
        factura.setCorrelativo(fila.getInt("correlativo"));
        factura.setNumero(fila.getString("numero"));
        factura.setEstado(EstadoFactura.from(fila.getString("estado")));
        factura.setDescuento(fila.getInt("descuento"));
        factura.setObservaciones(fila.getString("observaciones"));
        long rectificaId = fila.getLong("rectifica_id");
        if (fila.wasNull()) {
            factura.setRectificaId(null);
        } else {
            factura.setRectificaId(rectificaId);
        }
        factura.setFormaPago(fila.getString("forma_pago"));
        String vencimiento = fila.getString("vencimiento");
        if (vencimiento == null || vencimiento.isBlank()) {
            factura.setVencimiento(null);
        } else {
            factura.setVencimiento(LocalDate.parse(vencimiento));
        }
        factura.setRealizadaPor(fila.getString("realizada_por"));
        ponerRetencion(factura, fila);
        factura.setBaseTotal(importe(fila.getString("base_total")));
        factura.setIvaTotal(importe(fila.getString("iva_total")));
        factura.setImporteRetencion(importe(fila.getString("importe_retencion")));
        factura.setTotalSuplidos(importe(fila.getString("total_suplidos")));
        factura.setTotal(importe(fila.getString("total")));
        if (factura.getRectificaId() != null) {
            factura.setRectificaNumero(buscarNumero(factura.getRectificaId()));
        }
        if (conLineas) {
            factura.setLineas(lineas(factura.getId()));
        } else {
            factura.setLineas(new ArrayList<>());
        }
        return factura;
    }

    /** Reconstruimos la copia del cliente desde sus columnas congeladas, con su id del maestro. */
    private Cliente crearCliente(ResultSet fila) throws Exception {
        Cliente cliente = new Cliente(fila.getString("cli_nombre"), fila.getString("cli_nif"),
                fila.getString("cli_direccion"), fila.getString("cli_cp"),
                fila.getString("cli_localidad"), fila.getString("cli_provincia"));
        long clienteId = fila.getLong("cliente_id");
        if (fila.wasNull()) {
            cliente.setId(null);
        } else {
            cliente.setId(clienteId);
        }
        cliente.setEmail(fila.getString("cli_email"));
        return cliente;
    }

    /** Reconstruimos la retención congelada al emitir: manda sobre el catálogo actual. */
    private void ponerRetencion(Factura factura, ResultSet fila) throws Exception {
        long retencionId = fila.getLong("retencion_id");
        boolean sinId = fila.wasNull();
        String nombre = fila.getString("retencion_nombre");
        int porcentaje = fila.getInt("retencion_porcentaje");
        boolean sinPorcentaje = fila.wasNull();
        if (sinId && (nombre == null || nombre.isBlank()) && sinPorcentaje) {
            factura.setRetencion(null);
            return;
        }
        if (nombre == null || nombre.isBlank()) {
            nombre = "Retención";
        }
        if (sinPorcentaje) {
            porcentaje = 0;
        }
        TipoRetencion retencion = new TipoRetencion(nombre, porcentaje);
        if (!sinId) {
            retencion.setId(retencionId);
        }
        factura.setRetencion(retencion);
    }

    /** Las líneas de la factura, ordenadas por su orden. */
    private List<LineaFactura> lineas(long facturaId) throws Exception {
        String consulta = """
                SELECT id, orden, cantidad, descripcion, precio_unitario, tipo_iva_id,
                    iva_nombre, iva_porcentaje, iva_motivo_exencion, es_suplido
                FROM factura_linea WHERE factura_id = ? ORDER BY orden
                """;
        List<LineaFactura> lista = new ArrayList<>();
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, facturaId);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(crearLinea(filas));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
        return lista;
    }

    /** Pasamos una fila de línea a objeto. */
    private LineaFactura crearLinea(ResultSet fila) throws SQLException {
        LineaFactura linea = new LineaFactura();
        linea.setId(fila.getLong("id"));
        linea.setOrden(fila.getInt("orden"));
        linea.setCantidad(fila.getInt("cantidad"));
        linea.setDescripcion(fila.getString("descripcion"));
        linea.setPrecioUnitario(importe(fila.getString("precio_unitario")));
        long tipoIvaId = fila.getLong("tipo_iva_id");
        if (fila.wasNull()) {
            linea.setTipoIvaId(null);
        } else {
            linea.setTipoIvaId(tipoIvaId);
        }
        linea.setIvaNombre(fila.getString("iva_nombre"));
        int porcentaje = fila.getInt("iva_porcentaje");
        if (fila.wasNull()) {
            linea.setIvaPorcentaje(null);
        } else {
            linea.setIvaPorcentaje(porcentaje);
        }
        linea.setIvaMotivoExencion(fila.getString("iva_motivo_exencion"));
        linea.setEsSuplido(fila.getInt("es_suplido") == 1);
        return linea;
    }

    /** El número de una factura por su id, o cadena vacía si ya no está. */
    private String buscarNumero(long id) throws Exception {
        String consulta = "SELECT numero FROM factura WHERE id = ?";
        try (PreparedStatement sentencia = Conexion.establecerConexion().prepareStatement(consulta)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (filas.next()) {
                    return filas.getString(1);
                }
                return "";
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Abrimos la conexión compartida traduciendo sus errores al mensaje que verá el usuario. */
    private Connection abrirConexion() throws Exception {
        try {
            return Conexion.establecerConexion();
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Revertimos la transacción en curso, avisando también si falla la reversión. */
    private void revertir(Connection conexion) throws Exception {
        try {
            conexion.rollback();
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Dejamos la conexión como estaba: con la confirmación automática activada. */
    private void cerrarTransaccion(Connection conexion) throws Exception {
        try {
            if (!conexion.getAutoCommit()) {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Error SQLite: " + e.getMessage());
        }
    }

    /** Un importe de texto de la base a BigDecimal, con cero cuando viene vacío. */
    private BigDecimal importe(String valor) {
        if (valor == null || valor.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(valor);
    }

    /** Un BigDecimal a texto de la base, con cero cuando viene vacío. */
    private String importe(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }
        return valor.toPlainString();
    }
}
