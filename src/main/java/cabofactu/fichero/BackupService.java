package cabofactu.fichero;

import cabofactu.modelo.negocio.sqlite.Database;
import cabofactu.modelo.negocio.sqlite.Migrations;
import cabofactu.modelo.negocio.sqlite.CopiaRepository;
import cabofactu.modelo.negocio.sqlite.FacturaRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import cabofactu.modelo.negocio.EmpresaManager;
import cabofactu.modelo.negocio.ValidationException;

/**
 * Copias de seguridad de la base de datos mediante copia consistente en
 * caliente. El archivo lleva un timestamp (facturas_AAAAMMDD_HHMMSS.db).
 */
public class BackupService {

    private final CopiaRepository copiaRepository;
    private final FacturaRepository facturaRepository;
    private final Clock clock;

    public BackupService(CopiaRepository copiaRepository, FacturaRepository facturaRepository, Clock clock) {
        this.copiaRepository = copiaRepository;
        this.facturaRepository = facturaRepository;
        this.clock = clock;
    }

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public record ResumenBackup(
            String nombreEmpresa,
            String nif,
            String logoPath,
            boolean logoExiste,
            int numFacturas,
            LocalDate ultimaFecha,
            int userVersion
    ) {
    }

    /**
     * Crea la copia en la carpeta indicada y devuelve la ruta generada.
     */
    public Path crearBackup(Path carpetaDestino) throws IOException {
        Files.createDirectories(carpetaDestino);
        String nombre = "facturas_" + LocalDateTime.now(clock).format(STAMP);
        Path archivo = rutaLibre(carpetaDestino, nombre);

        copiaRepository.crearCopia(archivo);
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

    public ResumenBackup leerResumen(Path origen) throws ValidationException {
        if (origen == null || !Files.isRegularFile(origen) || !Files.isReadable(origen)) {
            throw new ValidationException("El archivo seleccionado no es un archivo legible.");
        }
        if (origen.toAbsolutePath().normalize().equals(Database.dbPath().toAbsolutePath().normalize())) {
            throw new ValidationException("No se puede usar la propia base activa como origen de restauración.");
        }
        return copiaRepository.leerResumen(origen);
    }

    public Path restaurarEnEmpresaActiva(Path origen) throws IOException, ValidationException {
        leerResumen(origen);

        Path carpetaRescate = Database.dataDir().resolve("copias_previas");
        Path rescate = crearBackup(carpetaRescate);

        try {
            copiaRepository.reemplazarBaseActiva(origen);
        } catch (Exception e) {
            copiaRepository.reemplazarBaseActiva(rescate);
            throw new IOException("No se pudo restaurar; se ha recuperado la base anterior: " + e.getMessage(), e);
        }

        return rescate;
    }

    public EmpresaManager.EmpresaInfo restaurarComoEmpresaNueva(Path origen, String nombre)
            throws IOException, ValidationException {
        leerResumen(origen);

        EmpresaManager.EmpresaInfo nueva = null;
        try {
            nueva = EmpresaManager.crearEmpresa(nombre);

            Path destino = Database.dbPathDe(nueva.slug());
            copiaRepository.instalarComoBase(origen, destino);
        } catch (Exception e) {
            if (nueva != null) {
                try {
                    EmpresaManager.eliminarEmpresa(nueva.slug());
                } catch (Exception ignored) {
                }
            }
            throw new IOException("No se pudo crear la empresa desde la copia: " + e.getMessage(), e);
        }

        return nueva;
    }

    /** Facturas de la empresa activa, para la regla de restauración. */
    public int facturasEmpresaActiva() {
        return facturaRepository.contar();
    }

    /** Versión de esquema de la aplicación, para comparar con la copia. */
    public int versionEsquemaAplicacion() {
        return Migrations.ultimaVersion();
    }
}
