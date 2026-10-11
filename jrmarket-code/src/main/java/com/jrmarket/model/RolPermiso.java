package com.jrmarket.model;

import java.io.Serializable;

public class RolPermiso implements Serializable {
    private int idRolPermiso;
    private String fechaHora;
    private Rol rol;
    private Permiso permiso;

    public RolPermiso() {}

    public RolPermiso(int idRolPermiso, String fechaHora, Rol rol, Permiso permiso) {
        this.idRolPermiso = idRolPermiso;
        this.fechaHora = fechaHora;
        this.rol = rol;
        this.permiso = permiso;
    }

    public int getIdRolPermiso() { return idRolPermiso; }
    public void setIdRolPermiso(int idRolPermiso) { this.idRolPermiso = idRolPermiso; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Permiso getPermiso() { return permiso; }
    public void setPermiso(Permiso permiso) { this.permiso = permiso; }
}