package com.jrmarket.model;

import java.io.Serializable;

/** Datos compartidos por comprobantes de venta y de compra. */
public abstract class Comprobante implements Serializable {
    protected int idComprobante;
    protected String nroComprobante;
    protected String fechaHora;
    protected double total;
    protected Usuario usuario;

    public Comprobante() {}

    public Comprobante(int idComprobante, String nroComprobante, String fechaHora, Usuario usuario) {
        this.idComprobante = idComprobante;
        this.nroComprobante = nroComprobante;
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.total = 0.0;
    }

    /** Cada tipo de comprobante define cómo sumar los importes de su detalle. */
    public abstract double calcularTotal();

    public int getIdComprobante() { return idComprobante; }
    public void setIdComprobante(int idComprobante) { this.idComprobante = idComprobante; }

    public String getNroComprobante() { return nroComprobante; }
    public void setNroComprobante(String nroComprobante) { this.nroComprobante = nroComprobante; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}