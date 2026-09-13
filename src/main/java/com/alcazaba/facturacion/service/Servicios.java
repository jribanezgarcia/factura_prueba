package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.repository.ClienteRepository;
import com.alcazaba.facturacion.repository.ConfigRepository;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.HistorialRepository;
import com.alcazaba.facturacion.repository.IvaRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.NumeroDisponibleRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.TipoRetencionRepository;
import com.alcazaba.facturacion.repository.VersionRepository;

import java.sql.SQLException;

/**
 * Contenedor de dependencias construido una sola vez en el arranque.
 * Repositorios y servicios se inyectan a mano por constructor.
 */
public class Servicios {

    public final ClienteService clientes;
    public final SerieService series;
    public final IvaService ivas;
    public final RetencionService retenciones;
    public final ConfigService config;

    public final NumeroService numeros;
    public final VersionadoService versionado;
    public final FacturaService factura;
    public final EstadoService estado;
    public final RectificativaService rectificativas;
    public final FacturacionMensualService facturacionMensual;
    public final HistorialService historialService;
    public final BackupService backup;

    public Servicios() throws SQLException {
        Database.getConnection();
        ClienteRepository clienteRepository = new ClienteRepository();
        SerieRepository serieRepository = new SerieRepository();
        IvaRepository ivaRepository = new IvaRepository();
        TipoRetencionRepository tipoRetencionRepository = new TipoRetencionRepository();
        FacturaRepository facturaRepository = new FacturaRepository();
        VersionRepository versionRepository = new VersionRepository();
        LineaRepository lineaRepository = new LineaRepository();
        ConfigRepository configRepository = new ConfigRepository();
        HistorialRepository historialRepository = new HistorialRepository();
        NumeroDisponibleRepository numeroDisponibleRepository = new NumeroDisponibleRepository();

        clientes = new ClienteService(clienteRepository);
        series = new SerieService(serieRepository, facturaRepository);
        ivas = new IvaService(ivaRepository);
        retenciones = new RetencionService(tipoRetencionRepository);
        config = new ConfigService(configRepository);

        numeros = new NumeroService(serieRepository, numeroDisponibleRepository);
        versionado = new VersionadoService(versionRepository, lineaRepository);
        factura = new FacturaService(facturaRepository, serieRepository, clienteRepository, versionRepository, lineaRepository, versionado, numeros, numeroDisponibleRepository);
        estado = new EstadoService(facturaRepository, serieRepository, versionRepository, lineaRepository, versionado, numeros, factura);
        rectificativas = new RectificativaService(factura, serieRepository, tipoRetencionRepository);
        facturacionMensual = new FacturacionMensualService(factura, facturaRepository, numeros);
        historialService = new HistorialService(historialRepository);
        backup = new BackupService();
    }
}
