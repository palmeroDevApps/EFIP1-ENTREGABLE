package com.jrmarket.model;

import java.io.Serializable;

public class HistorialEstadoVenta implements Serializable {
    private int idHistorial;
    private String fechaDesde;
    private String fechaHasta;
    private String observaciones;
    private Venta venta;
    private EstadoVenta estadoVenta;

    public HistorialEstadoVenta() {}

    public HistorialEstadoVenta(int idHistorial, String fechaDesde, String fechaHasta, 
                                String observaciones, Venta venta, EstadoVenta estadoVenta) {
        this.idHistorial = idHistorial;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.observaciones = observaciones;
        this.venta = venta;
        this.estadoVenta = estadoVenta;
    }

    public String getFechaHora() { return fechaDesde; }

    public EstadoVenta getEstado() { return estadoVenta; }

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public EstadoVenta getEstadoVenta() { return estadoVenta; }
    public void setEstadoVenta(EstadoVenta estadoVenta) { this.estadoVenta = estadoVenta; }
}