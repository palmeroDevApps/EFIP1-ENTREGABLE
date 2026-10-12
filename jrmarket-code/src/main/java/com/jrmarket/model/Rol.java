package com.jrmarket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Rol de usuario y colección de permisos habilitados para sus operaciones. */
public class Rol implements Serializable {
    private int idRol;
    private String nombre;
    private String descripcion;
    private String fechaDesde;
    private String fechaHasta;
    private List<RolPermiso> rolPermisos;

    public Rol() {
        this.rolPermisos = new ArrayList<>();
    }

    public Rol(int idRol, String nombre, String descripcion, String fechaDesde, String fechaHasta) {
        this();
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public List<RolPermiso> getRolPermisos() { return rolPermisos; }
    public void setRolPermisos(List<RolPermiso> rolPermisos) { this.rolPermisos = rolPermisos; }

    @Override
    public String toString() { return nombre; }
}