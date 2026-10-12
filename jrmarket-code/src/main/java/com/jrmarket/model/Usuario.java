package com.jrmarket.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Usuario de la aplicación; su rol determina los módulos a los que puede acceder. */
public class Usuario implements Serializable {
    private int idUsuario;
    private String nombreUsuario;
    private String contrasenia;
    private String nombreCompleto;
    private boolean estado;
    private Rol rol;

    public Usuario() {}

    public Usuario(int idUsuario, String nombreUsuario, String contrasenia, String nombreCompleto, boolean estado, Rol rol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
        this.rol = rol;
    }

    /** Compara la contraseña ingresada con el hash SHA-256 guardado en el usuario. */
    public boolean autenticar(String pass) {
        if (!this.estado || this.contrasenia == null || pass == null) return false;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(pass.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(HexFormat.of().formatHex(hash).getBytes(StandardCharsets.UTF_8),
                this.contrasenia.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en este entorno.", e);
        }
    }

    /** Activa o desactiva la cuenta para permitir o impedir el inicio de sesión. */
    public void cambiarEstado(boolean nuevoEstado) {
        this.estado = nuevoEstado;
    }

    /** Consulta los permisos asociados al rol para autorizar el acceso a un módulo. */
    public boolean tienePermiso(String modulo) {
        if (this.rol == null || this.rol.getRolPermisos() == null) return false;
        for (RolPermiso rp : this.rol.getRolPermisos()) {
            if (rp.getPermiso() != null && rp.getPermiso().getNombre().equalsIgnoreCase(modulo)) {
                return true;
            }
        }
        return false;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    @Override
    public String toString() { return nombreCompleto + " (" + nombreUsuario + ")"; }
}