package com.jrmarket.model;

import java.io.Serializable;

public class DetalleVenta implements Serializable {
    private int idDetalleVenta;
    private int cantidad;
    private double precioCobrado;
    private double costoHistorico;
    private double subtotal;
    private Producto producto;
    private Venta venta;

    public DetalleVenta() {}

    public DetalleVenta(int idDetalleVenta, int cantidad, double precioCobrado, double costoHistorico, Producto producto) {
        this.idDetalleVenta = idDetalleVenta;
        this.cantidad = cantidad;
        this.precioCobrado = precioCobrado;
        this.costoHistorico = costoHistorico;
        this.producto = producto;
        this.subtotal = calcularSubtotal();
    }

    public double calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioCobrado;
        return this.subtotal;
    }

    public double getSubtotal() {
        if (this.subtotal == 0.0 && this.cantidad > 0 && this.precioCobrado > 0.0) {
            calcularSubtotal();
        }
        return subtotal;
    }

    public int getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(int idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad; 
        calcularSubtotal();
    }

    public double getPrecioCobrado() { return precioCobrado; }
    public void setPrecioCobrado(double precioCobrado) { 
        this.precioCobrado = precioCobrado; 
        calcularSubtotal();
    }

    public double getCostoHistorico() { return costoHistorico; }
    public void setCostoHistorico(double costoHistorico) { this.costoHistorico = costoHistorico; }

    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
}