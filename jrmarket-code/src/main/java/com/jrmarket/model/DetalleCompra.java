package com.jrmarket.model;

import java.io.Serializable;

public class DetalleCompra implements Serializable {
    private int idDetalleCompra;
    private int cantidad;
    private double costoUnitario;
    private double subtotal;
    private Producto producto;
    private Compra compra;

    public DetalleCompra() {}

    public DetalleCompra(int idDetalleCompra, int cantidad, double costoUnitario, Producto producto) {
        this.idDetalleCompra = idDetalleCompra;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.producto = producto;
        this.subtotal = calcularSubtotal();
    }

    public double calcularSubtotal() {
        this.subtotal = this.cantidad * this.costoUnitario;
        return this.subtotal;
    }

    public double getSubtotal() {
        if (this.subtotal == 0.0 && this.cantidad > 0 && this.costoUnitario > 0.0) {
            calcularSubtotal();
        }
        return subtotal;
    }

    public int getIdDetalleCompra() { return idDetalleCompra; }
    public void setIdDetalleCompra(int idDetalleCompra) { this.idDetalleCompra = idDetalleCompra; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad; 
        calcularSubtotal();
    }

    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { 
        this.costoUnitario = costoUnitario; 
        calcularSubtotal();
    }

    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }
}