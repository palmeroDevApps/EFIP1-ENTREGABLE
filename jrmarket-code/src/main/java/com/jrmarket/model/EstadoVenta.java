package com.jrmarket.model;

import java.io.Serializable;

/** Estado del ciclo de vida de una venta, por ejemplo cobrada o anulada. */
public class EstadoVenta implements Serializable {
    private int idEstadoVenta;
    private String nombre;
    private String descripcion;

    public EstadoVenta() {}

    public EstadoVenta(int idEstadoVenta, String nombre, String descripcion) {
        this.idEstadoVenta = idEstadoVenta;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdEstadoVenta() { return idEstadoVenta; }
    public void setIdEstadoVenta(int idEstadoVenta) { this.idEstadoVenta = idEstadoVenta; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() { return nombre; }
}