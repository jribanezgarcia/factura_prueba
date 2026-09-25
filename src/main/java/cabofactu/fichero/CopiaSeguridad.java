package cabofactu.fichero;

import cabofactu.modelo.negocio.sqlite.Conexion;
import cabofactu.modelo.negocio.sqlite.CopiaSeguridadDAO;
import cabofactu.modelo.negocio.Facturas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import cabofactu.modelo.dominio.EmpresaDisponible;
import cabofactu.modelo.negocio.Empresas;

/**
 * Copias de seguridad de la base de datos mediante copia consistente en
 * caliente. El archivo lleva un timestamp (facturas_AAAAMMDD_HHMMSS.db).
 */
public class CopiaSeguridad {

    private final CopiaSeguridadDAO copiaSeguridadDAO;
    private final Facturas facturas;
    private final Clock clock;

    public CopiaSeguridad(CopiaSeguridadDAO copiaSeguridadDAO, Facturas facturas, Clock clock) {
        this.copiaSeguridadDAO = copiaSeguridadDAO;
        this.facturas = facturas;
        this.clock = clock;
    }

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public record ResumenCopia(
            String nombreEmpresa,
            String nif,
            String logoPath,
            boolean logoExiste,
            int numFacturas,
            LocalDate ultimaFecha
    ) {
    }

    /**
     * Crea la copia en la carpeta indicada y devuelve la ruta generada.
     */
    public Path crearCopia(Path carpetaDestino) throws IOException {
        Files.createDirectories(carpetaDestino);
        String nombre = "facturas_" + LocalDateTime.now(clock).format(STAMP);
        Path archivo = rutaLibre(carpetaDestino, nombre);

        copiaSeguridadDAO.crearCopia(archivo);
        return archivo;
    }

    static Path rutaLibre(Path carpeta, String base) {
        Path primero = carpeta.resolve(base + ".db");
        if (!Files.exists(primero)) {
            return primero;
        }
        for (int i = 2; ; i++) {
            Path candidato = carpeta.resolve(base + "_" + i + ".db");
            if (!Files.exists(candidato)) {
                return candidato;
            }
        }
    }

    public ResumenCopia leerResumen(Path origen) throws Exception {
        if (origen == null || !Files.isRegularFile(origen) || !Files.isReadable(origen)) {
            throw new Exception("El archivo seleccionado no es un archivo legible.");
        }
        if (origen.toAbsolutePath().normalize().equals(Conexion.rutaBase().toAbsolutePath().normalize())) {
            throw new Exception("No se puede usar la propia base activa como origen de restauración.");
        }
        return copiaSeguridadDAO.leerResumen(origen);
    }

    public Path restaurarEnEmpresaActiva(Path origen) throws IOException, Exception {
        leerResumen(origen);

        Path carpetaRescate = Conexion.carpetaEmpresa().resolve("copias_previas");
        Path rescate = crearCopia(carpetaRescate);

        try {
            copiaSeguridadDAO.reemplazarBaseActiva(origen);
        } catch (Exception e) {
            copiaSeguridadDAO.reemplazarBaseActiva(rescate);
            throw new IOException("No se pudo restaurar; se ha recuperado la base anterior: " + e.getMessage(), e);
        }

        Empresas.getEmpresas().recordarTema();
        return rescate;
    }

    public EmpresaDisponible restaurarComoEmpresaNueva(Path origen, String nombre)
            throws Exception {
        leerResumen(origen);

        EmpresaDisponible nueva = null;
        try {
            nueva = Empresas.getEmpresas().alta(nombre);

            Path destino = Conexion.rutaBaseDe(nueva.getCarpeta());
            copiaSeguridadDAO.instalarComoBase(origen, destino);
        } catch (Exception e) {
            if (nueva != null) {
                try {
                    Empresas.getEmpresas().baja(nueva.getCarpeta());
                } catch (Exception ignored) {
                }
            }
            throw new IOException("No se pudo crear la empresa desde la copia: " + e.getMessage(), e);
        }

        return nueva;
    }

    /** Facturas de la empresa activa, para la regla de restauración. */
    public int facturasEmpresaActiva() throws Exception {
        return facturas.contar();
    }
}
