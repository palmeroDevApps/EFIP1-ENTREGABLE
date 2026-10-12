package com.jrmarket.model;

import java.io.Serializable;

/** Marca comercial utilizada para clasificar productos del catálogo. */
public class Marca implements Serializable {
    private int idMarca;
    private String nombre;
    private String fechaDesde;
    private String fechaHasta;

    public Marca() {}

    public Marca(int idMarca, String nombre, String fechaDesde, String fechaHasta) {
        this.idMarca = idMarca;
        this.nombre = nombre;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public int getIdMarca() { return idMarca; }
    public void setIdMarca(int idMarca) { this.idMarca = idMarca; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    @Override
    public String toString() { return nombre; }
}