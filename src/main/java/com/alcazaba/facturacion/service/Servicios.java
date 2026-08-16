package com.alcazaba.facturacion.service;

import com.alcazaba.facturacion.db.Database;
import com.alcazaba.facturacion.repository.ClienteRepository;
import com.alcazaba.facturacion.repository.ConfigRepository;
import com.alcazaba.facturacion.repository.FacturaRepository;
import com.alcazaba.facturacion.repository.HistorialRepository;
import com.alcazaba.facturacion.repository.IvaRepository;
import com.alcazaba.facturacion.repository.LineaRepository;
import com.alcazaba.facturacion.repository.SerieRepository;
import com.alcazaba.facturacion.repository.VersionRepository;

import java.sql.SQLException;

/**
 * Contenedor de dependencias construido una sola vez en el arranque.
 * Repositorios y servicios se inyectan a mano por constructor.
 */
public class Servicios {

    public final ClienteRepository clientes;
    public final SerieRepository series;
    public final IvaRepository ivas;
    public final FacturaRepository facturas;
    public final VersionRepository versiones;
    public final LineaRepository lineas;
    public final ConfigRepository config;
    public final HistorialRepository historial;

    public final NumeroService numeros;
    public final VersionadoService versionado;
    public final FacturaService factura;
    public final EstadoService estado;
    public final RectificativaService rectificativas;
    public final HistorialService historialService;
    public final BackupService backup;

    public Servicios() throws SQLException {
        Database.getConnection();
        clientes = new ClienteRepository();
        series = new SerieRepository();
        ivas = new IvaRepository();
        facturas = new FacturaRepository();
        versiones = new VersionRepository();
        lineas = new LineaRepository();
        config = new ConfigRepository();
        historial = new HistorialRepository();

        numeros = new NumeroService(series);
        versionado = new VersionadoService(versiones, lineas);
        factura = new FacturaService(facturas, series, clientes, versiones, lineas, versionado, numeros);
        estado = new EstadoService(facturas, series, versiones, lineas, versionado, numeros, factura);
        rectificativas = new RectificativaService(factura, series);
        historialService = new HistorialService(historial);
        backup = new BackupService();
    }
}
