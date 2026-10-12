package com.jrmarket.model;

import java.io.Serializable;

/** Rubro que agrupa productos para búsqueda y organización del catálogo. */
public class TipoProducto implements Serializable {
    private int idTipoProducto;
    private String nombre;
    private String descripcion;
    private String fechaDesde;
    private String fechaHasta;

    public TipoProducto() {}

    public TipoProducto(int idTipoProducto, String nombre, String descripcion, String fechaDesde, String fechaHasta) {
        this.idTipoProducto = idTipoProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public int getIdTipoProducto() { return idTipoProducto; }
    public void setIdTipoProducto(int idTipoProducto) { this.idTipoProducto = idTipoProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    @Override
    public String toString() { return nombre; }
}