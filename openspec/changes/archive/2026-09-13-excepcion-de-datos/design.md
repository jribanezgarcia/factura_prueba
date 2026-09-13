## Context

Estado a 13/09/2026, tras `capa-servicios-catalogos` y `reloj-inyectable`:

- **Repositorios** (`repository/`): todos los métodos públicos declaran `throws SQLException`. Los helpers privados (`map(ResultSet)`, `inicializarSiguiente`…) también.
- **Servicios**: `SQLException` aparece en `ClienteService` (8), `SerieService` (9), `IvaService` (7), `RetencionService` (6), `ConfigService` (5), `NumeroService` (7), `VersionadoService` (9), `FacturaService` (20), `EstadoService` (8), `RectificativaService` (4), `FacturacionMensualService` (5), `HistorialService` (2) y `Servicios` (3), contando imports.
- **Transacciones**: `FacturaService` (crear 91-101, editar 177-215, borrar 254-267), `EstadoService` (103-130) y `FacturacionMensualService` (65-82) hacen `Database.beginTransaction()`, `commit()` y, en `catch (SQLException | ValidationException | RuntimeException e)`, `rollback()` y relanzan.
- **`Database`**: `commit`, `rollback`, `beginTransaction` y `endTransaction` ya capturan `SQLException` y lanzan `new RuntimeException("Error al …", e)`.
- **`VersionadoService.java:111`**: `throw new java.sql.SQLException("La version " + versionId + " no existe")`.
- **Interfaz**: ningún controlador captura `SQLException`; todos capturan `ValidationException` y `Exception` (78 `catch`). La única firma con `SQLException` es `EditorController.proponerDestinoPdf` (línea 1264).
- **`Main.entrarEnMenu`** captura `Exception` alrededor de `new Servicios()`.
- **Tests**: `ClientesNifValidationTest` 197 y 205 capturan `SQLException`. Muchos métodos de test declaran `throws SQLException`, lo que sigue compilando aunque ya no se lance.

## Goals / Non-Goals

**Goals:** que ninguna firma pública de `repository/`, `service/` (salvo `BackupService` y `EmpresaManager`) ni `ui/` declare `SQLException`; que los fallos de base de datos sigan llegando a la interfaz con el mismo mensaje; que las transacciones sigan revirtiendo igual.

**Non-Goals:** cambiar los mensajes de error; distinguir tipos de fallo (duplicado, restricción, conexión perdida); tocar `BackupService`, `EmpresaManager`, `BackupController`, `Migrations` ni `Database.getConnection`; quitar los `throws SQLException` de los métodos de test que no lo necesiten.

## Decisions

### D1. Una sola excepción no comprobada, en `db`

```java
package com.alcazaba.facturacion.db;

import java.sql.SQLException;

public class DatosException extends RuntimeException {

    public DatosException(SQLException causa) {
        super(causa.getMessage(), causa);
    }

    public DatosException(String mensaje) {
        super(mensaje);
    }

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

**No comprobada.** Un fallo de base de datos no es algo que la lógica de negocio pueda corregir: se revierte la transacción y se avisa al usuario. Los controladores ya capturan `Exception` en todos los sitios donde llaman a servicios, así que no hace falta añadir ningún `catch`. Si fuera comprobada, habría que cambiar `throws SQLException` por `throws DatosException` en las mismas ~100 firmas sin ganar nada.

**En `db`.** Es la capa más baja: `Database` la lanza, los repositorios la lanzan y servicios o interfaz pueden capturarla sin que ninguna capa baja dependa de una alta. Ponerla en `service` obligaría a `db` y `repository` a importar de `service`, que es la inversión que se quiere evitar.

**Mismo mensaje.** El constructor principal copia `getMessage()` de la `SQLException`, así que el `"Error al guardar: " + e.getMessage()` de los controladores muestra exactamente el mismo texto que hoy.

Descartado: excepciones por tipo de fallo (`RegistroDuplicadoException`…). Hoy nadie las distinguiría; se añadirán cuando haya un caso que lo necesite.

### D2. La traducción se hace en el borde del repositorio

Cada método **público** de un repositorio envuelve su cuerpo:

```java
public Empresa getEmpresa() {
    try (PreparedStatement ps = Database.getConnection().prepareStatement("SELECT * FROM empresa WHERE id = 1");
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            return map(rs);
        }
        return new Empresa();
    } catch (SQLException e) {
        throw new DatosException(e);
    }
}
```

Si el método ya tiene `try-with-resources`, se añade el `catch` a ese mismo `try`. Si tiene código fuera del `try` (como `saveEmpresa`, que obtiene la conexión antes), se amplía el `try` o se envuelve el cuerpo entero; el orden de las sentencias no cambia. Los helpers **privados** siguen declarando `throws SQLException`: se llaman desde dentro del `try` del método público.

Descartado: traducir en los servicios. Obligaría a meter `try/catch` en cada método de negocio y dejaría `java.sql` importado en `service/`, que es justo lo que se quiere sacar. El repositorio es la única capa que debe saber que hay JDBC.

### D3. Servicios: quitar firmas, no añadir lógica

En los servicios de la lista se borra `throws SQLException` de todas las firmas (conservando `ValidationException` donde esté) y el import de `java.sql.SQLException`. En los tres bloques de transacción, el `catch` queda:

```java
} catch (ValidationException | RuntimeException e) {
    Database.rollback();
    throw e;
}
```

`DatosException` es `RuntimeException`, así que el `rollback` sigue ocurriendo ante un fallo de base de datos. En `borrarFactura`, que capturaba `SQLException | RuntimeException`, queda `catch (RuntimeException e)`.

Si el compilador se queja de que un multi-catch incluye tipos no lanzados, se ajusta solo esa línea sin cambiar qué se revierte.

### D4. La versión inexistente

`VersionadoService.java:111` pasa de `throw new java.sql.SQLException("La version " + versionId + " no existe")` a `throw new DatosException("La version " + versionId + " no existe")`. Se mantiene como fallo de datos, no como `ValidationException`, para que la interfaz lo muestre por el mismo camino que hoy (el `catch (Exception e)` genérico, con su prefijo "Error al …").

### D5. `Database`: mismas cuatro envolturas, tipo concreto

En `commit`, `rollback`, `beginTransaction` y `endTransaction` se cambia `throw new RuntimeException("Error al …", e)` por `throw new DatosException("Error al …", e)`, con el mismo texto. `getConnection()` no se toca: sigue lanzando `SQLException` porque sus llamantes son repositorios (que ya la traducen), `BackupService`, `EmpresaManager`, `BackupController` y `Servicios`.

### D6. `Servicios` y `EditorController`

- `Servicios()` y `Servicios(Clock)` dejan de declarar `throws SQLException`. La llamada `Database.getConnection()` del constructor se envuelve: `catch (SQLException e) { throw new DatosException(e); }`. `Main.entrarEnMenu` ya captura `Exception` y muestra el mismo mensaje.
- `EditorController.proponerDestinoPdf` pierde `throws java.sql.SQLException`; su llamante está dentro de un `catch (Exception e)`.

### D7. Fuera: `BackupService`, `EmpresaManager`, `BackupController`, `Migrations`

Estas clases abren conexiones con `DriverManager`, ejecutan `PRAGMA` y consultas propias. Traducir sus excepciones ahora sería trabajo que se tira en el change que las mueve a `db/` o `repository/`. Siguen con `SQLException` hasta entonces. `BackupService` ya lanza `ValidationException` en `leerResumen` para la interfaz.

## Risks / Trade-offs

- **Riesgo medio por tamaño, bajo por lógica:** unas cien firmas y diez repositorios, pero ningún cambio de flujo.
- **Descuido posible:** un método público de repositorio sin `catch`, que no compilaría al quitar su `throws`. El compilador lo detecta.
- **Descuido posible:** un `catch` de transacción que deje de revertir. La tarea 5.2 revisa los cinco bloques y los tests de `FacturacionMensualServiceTest` (servicio que falla a mitad de lote) lo cubren.
- **Descuido posible:** cambiar el mensaje de error al envolver. D1 obliga a copiar `getMessage()`.
- **Trade-off aceptado:** `DatosException` es genérica; no distingue causas. Suficiente mientras la interfaz solo muestra el mensaje.
