package com.jrmarket.model;

import java.io.Serializable;

public class Permiso implements Serializable {
    private int idPermiso;
    private String nombre;
    private String descripcion;

    public Permiso() {}

    public Permiso(int idPermiso, String nombre, String descripcion) {
        this.idPermiso = idPermiso;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdPermiso() { return idPermiso; }
    public void setIdPermiso(int idPermiso) { this.idPermiso = idPermiso; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() { return nombre; }
}