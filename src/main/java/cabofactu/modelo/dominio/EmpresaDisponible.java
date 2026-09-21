package cabofactu.modelo.dominio;

/**
 * Una empresa que se puede abrir desde el arranque: el nombre de su carpeta
 * de datos y el nombre que ve el usuario. Se ordena por nombre.
 */
public class EmpresaDisponible implements Comparable<EmpresaDisponible> {

    private String carpeta;
    private String nombre;

    public EmpresaDisponible(String carpeta, String nombre) throws Exception {
        setCarpeta(carpeta);
        setNombre(nombre);
    }

    public String getCarpeta() {
        return carpeta;
    }

    public void setCarpeta(String carpeta) throws Exception {
        if (carpeta == null || carpeta.isBlank()) {
            throw new Exception("La empresa no tiene carpeta de datos.");
        }
        this.carpeta = carpeta.trim();
    }

    public String getNombre() {
        return nombre;
    }

    /** Si no tiene nombre guardado, usamos el de su carpeta. */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            this.nombre = carpeta;
        } else {
            this.nombre = nombre.trim();
        }
    }

    /** Ordenamos por nombre, sin distinguir mayúsculas. */
    @Override
    public int compareTo(EmpresaDisponible otra) {
        return nombre.compareToIgnoreCase(otra.getNombre());
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        EmpresaDisponible empresa = (EmpresaDisponible) otro;
        return carpeta.equals(empresa.carpeta);
    }

    @Override
    public int hashCode() {
        return carpeta.hashCode();
    }

    /** Lo que se ve en el desplegable del arranque. */
    @Override
    public String toString() {
        return nombre;
    }
}
