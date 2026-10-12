package com.jrmarket.model;

import java.io.Serializable;

/**
 * Representa una autorización funcional del sistema.
 *
 * <p>Un rol se compone de varios permisos que determinan qué módulos y acciones
 * puede ejecutar un usuario autenticado. Esta abstracción permite limitar el acceso
 * de forma centralizada, evitando comparaciones por nombre de rol en varios puntos
 * de la aplicación.</p>
 */
public class Permiso implements Serializable {
    public static final String VENTAS = "VENTAS";
    public static final String CIERRE_CAJA = "CIERRE_CAJA";
    public static final String CATALOGO = "CATALOGO";
    public static final String COMPRAS = "COMPRAS";
    public static final String USUARIOS = "USUARIOS";
    public static final String GESTION_PRODUCTOS = "GESTION_PRODUCTOS";

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