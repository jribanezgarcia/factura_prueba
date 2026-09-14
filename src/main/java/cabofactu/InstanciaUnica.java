package cabofactu;

import cabofactu.modelo.negocio.sqlite.Database;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardOpenOption;

/**
 * Garantiza que solo haya una ventana de la aplicación abierta a la vez en este
 * usuario de Windows. Usa un bloqueo del sistema operativo sobre el fichero
 * facturas.lock de la carpeta de datos: mientras la aplicación está abierta el
 * fichero queda bloqueado y una segunda ejecución no puede bloquearlo. El sistema
 * libera el bloqueo aunque la aplicación termine de forma inesperada.
 */
public final class InstanciaUnica {

    private static FileChannel canal;
    private static FileLock bloqueo;

    private InstanciaUnica() {
    }

    /**
     * Intenta quedarse con el bloqueo de la aplicación.
     *
     * @return true si esta ejecución es la única abierta (o ya tenía el bloqueo);
     *         false si otra ejecución lo tiene
     * @throws IOException si no se puede abrir el fichero de bloqueo
     */
    public static synchronized boolean adquirir() throws IOException {
        if (bloqueo != null && bloqueo.isValid()) {
            return true;
        }
        FileChannel abierto = FileChannel.open(Database.lockPathGlobal(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            FileLock obtenido = abierto.tryLock();
            if (obtenido == null) {
                abierto.close();
                canal = null;
                bloqueo = null;
                return false;
            }
            canal = abierto;
            bloqueo = obtenido;
            return true;
        } catch (OverlappingFileLockException e) {
            abierto.close();
            canal = null;
            bloqueo = null;
            return false;
        } catch (IOException e) {
            try {
                abierto.close();
            } catch (IOException ignored) {
            }
            canal = null;
            bloqueo = null;
            throw e;
        }
    }

    /**
     * Suelta el bloqueo y cierra el fichero. No hace nada si no se tenía.
     */
    public static synchronized void liberar() {
        try {
            if (bloqueo != null && bloqueo.isValid()) {
                bloqueo.release();
            }
        } catch (IOException ignored) {
        }
        try {
            if (canal != null) {
                canal.close();
            }
        } catch (IOException ignored) {
        }
        bloqueo = null;
        canal = null;
    }
}
