## Context

`Servicios` (`service/Servicios.java`) es el contenedor que se construye una vez en `Main.entrarEnMenu` y que `Navegador.mostrar` inyecta en cada vista con `setServicios`. Hoy publica diez repositorios (`clientes`, `series`, `ivas`, `retenciones`, `facturas`, `versiones`, `lineas`, `config`, `historial`, `numerosDisponibles`) y ocho servicios.

Usos directos de repositorios fuera de `service/` (inventario completo a 13/09/2026):

| Repositorio | Métodos usados | Dónde |
|---|---|---|
| `clientes` | `listar`, `buscar`, `insertar`, `actualizar`, `tieneFacturas`, `setActivo`, `borrarFisico` | `ClientesController` 105-178; `EditorController` 511, 523, 525; `GenerarFacturasMensualesController` 155; tests `ClientesNifValidationTest` 196, 204 y `EditorIvaInactivoTest` 114 |
| `series` | `listar`, `getById`, `insertar`, `actualizar`, `getSiguiente(long,int)`, `actualizarSiguiente(long,int,int)`, `eliminar` | `ConfiguracionController` 733-872; `EditorController` 295, 371, 1278; `HistoricoController` 115; `GenerarFacturasMensualesController` 176; test `EditorIvaInactivoTest` 121 |
| `facturas` | `serieTieneFacturas` | `ConfiguracionController` 856 |
| `ivas` | `listar`, `getById`, `insertar`, `actualizar`, `setActivo`, `enUso` | `ConfiguracionController` 465-580; `EditorController` 407, 430; `GenerarFacturasMensualesController` 222; test `EditorIvaInactivoTest` 107, 140 |
| `retenciones` | `listar`, `insertar`, `actualizar`, `setActivo`, `enUso` | `ConfiguracionController` 601-694; `EditorController` 450; `GenerarFacturasMensualesController` 247 |
| `config` | `getEmpresa`, `saveEmpresa`, `getPreferencia`, `setPreferencia` | `ConfiguracionController` 214-342; `EditorController` 253, 374, 1214, 1240, 1259, 1268, 1392; `HistoricoController` 387, 412, 462, 479, 486; `MenuController` 74; `BackupController` 208 |

Los números de línea son orientativos; lo que manda es buscar `servicios.<campo>.` y `nav.servicios().<campo>.`.

## Goals / Non-Goals

**Goals:** que la interfaz solo dependa de servicios; que `Servicios` no publique ningún repositorio.

**Non-Goals:** quitar `throws SQLException`; inyectar por constructor en los controladores; tocar `FacturaService`, `EstadoService` y el resto de servicios existentes; tocar `BackupController` más allá de la línea de `config` (su SQL directo es otro change); cambiar nombres de campos de servicios que ya existen (`factura`, `numeros`, `historialService`…).

## Decisions

### D1. Servicios delgados que delegan, con los mismos nombres de método

Cada servicio nuevo recibe sus repositorios por constructor en campos `private final` y cada método delega en el repositorio con el mismo nombre, los mismos parámetros y el mismo `throws SQLException`. Así la sustitución en los controladores es mecánica (`servicios.clientes.listar(true)` → `servicios.clientes.listar(true)`, cambiando solo el tipo del campo) y el diff no mezcla reorganización con cambios de lógica.

Descartado: mover ahora validaciones de los controladores a los servicios. Es deseable, pero mezclarlo aquí haría el change difícil de revisar y arriesgado.

Operaciones por servicio (solo las que se usan hoy fuera de `service/`, nada más):

```java
public class ClienteService {
    public ClienteService(ClienteRepository clienteRepository)
    List<Cliente> listar(boolean soloActivos)
    List<Cliente> buscar(String texto, boolean soloActivos)
    long insertar(Cliente c)
    void actualizar(Cliente c)
    boolean tieneFacturas(long id)
    void setActivo(long id, boolean activo)
    void borrarFisico(long id)
}

public class SerieService {
    public SerieService(SerieRepository serieRepository, FacturaRepository facturaRepository)
    List<Serie> listar()
    Serie getById(long id)
    long insertar(Serie s)
    void actualizar(Serie s)
    int getSiguiente(long serieId, int anio)
    void actualizarSiguiente(long serieId, int anio, int siguiente)
    boolean tieneFacturas(long serieId)
    void eliminar(long serieId)
}

public class IvaService {
    public IvaService(IvaRepository ivaRepository)
    List<TipoIva> listar(boolean soloActivos)
    TipoIva getById(long id)
    long insertar(TipoIva t)
    void actualizar(TipoIva t)
    void setActivo(long id, boolean activo)
    boolean enUso(long id)
}

public class RetencionService {
    public RetencionService(TipoRetencionRepository tipoRetencionRepository)
    List<TipoRetencion> listar(boolean soloActivos)
    long insertar(TipoRetencion t)
    void actualizar(TipoRetencion t)
    void setActivo(long id, boolean activo)
    boolean enUso(long id)
}

public class ConfigService {
    public ConfigService(ConfigRepository configRepository)
    Empresa getEmpresa()
    void saveEmpresa(Empresa e)
    String getPreferencia(String clave)
    void setPreferencia(String clave, String valor)
}
```

Todos los métodos son `public` y declaran `throws SQLException`.

### D2. La comprobación de facturas de una serie pasa a `SerieService`

`ConfiguracionController.eliminarSerie` pregunta hoy a `servicios.facturas.serieTieneFacturas`. Es una pregunta sobre la serie, así que se expone como `SerieService.tieneFacturas(long serieId)`, que delega en `FacturaRepository.serieTieneFacturas`. Con eso la interfaz no necesita `facturas` y ningún repositorio queda a la vista.

### D3. `Servicios` conserva los nombres de campo, con tipo de servicio

Los campos `clientes`, `series`, `ivas`, `retenciones` y `config` se mantienen con el mismo nombre, pero su tipo pasa a ser el servicio correspondiente. Los campos `facturas`, `versiones`, `lineas`, `historial` y `numerosDisponibles` desaparecen. Los repositorios se crean como variables locales dentro del constructor y solo se usan para construir los servicios:

```java
public final ClienteService clientes;
public final SerieService series;
public final IvaService ivas;
public final RetencionService retenciones;
public final ConfigService config;

public final NumeroService numeros;
... (resto de servicios existentes, sin cambios)

public Servicios() throws SQLException {
    Database.getConnection();
    ClienteRepository clienteRepository = new ClienteRepository();
    SerieRepository serieRepository = new SerieRepository();
    ... (los diez repositorios como locales)

    clientes = new ClienteService(clienteRepository);
    series = new SerieService(serieRepository, facturaRepository);
    ivas = new IvaService(ivaRepository);
    retenciones = new RetencionService(tipoRetencionRepository);
    config = new ConfigService(configRepository);
    numeros = new NumeroService(serieRepository, numeroDisponibleRepository);
    ... (resto igual, cambiando los nombres de las variables)
}
```

Mantener los nombres hace que casi todas las líneas de los controladores sigan compilando sin tocarlas; el compilador señala las que usan un método que el servicio no ofrece (`servicios.facturas.serieTieneFacturas`).

Descartado: renombrar a `clienteService`, `serieService`… Es más explícito, pero multiplica el diff en siete controladores sin ganar nada ahora.

### D4. Los tests usan los servicios

`EditorIvaInactivoTest` y `ClientesNifValidationTest` preparan datos con `servicios.ivas.insertar`, `servicios.clientes.insertar`, `servicios.series.insertar`, `servicios.ivas.setActivo` y `servicios.clientes.listar`. Con D3 esas líneas siguen siendo válidas porque los servicios ofrecen los mismos métodos; solo hay que comprobar que compilan. Si algún test construye repositorios por su cuenta (`new ClienteRepository()`), puede seguir haciéndolo: el objetivo es la interfaz, no los tests de servicio.

## Risks / Trade-offs

- **Riesgo bajo:** delegación pura, sin lógica nueva.
- **Descuido posible:** dejar algún repositorio público en `Servicios` "por si acaso". La tarea 3.1 lo comprueba con una búsqueda.
- **Descuido posible:** que `SerieService` y `NumeroService`/`FacturaService` reciban instancias distintas de `SerieRepository`. No rompe nada (los repositorios no tienen estado), pero se crea una sola instancia de cada repositorio y se reutiliza.
- **Trade-off aceptado:** los servicios nuevos son hoy casi solo pasarela. Su valor está en el punto de extensión que abren para los changes siguientes.
