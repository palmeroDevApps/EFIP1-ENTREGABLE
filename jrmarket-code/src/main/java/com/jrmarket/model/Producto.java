package com.jrmarket.model;

import java.io.Serializable;

/** Producto del catálogo con precios, existencias y clasificación comercial. */
public class Producto implements Serializable {
    private int idProducto;
    private String codigoBarras;
    private String descripcion;
    private double precioVenta;
    private double precioCosto;
    private int stockActual;
    private int stockMinimo;
    private boolean estado;
    private String fechaDesde;
    private String fechaHasta;
    private Marca marca;
    private TipoProducto tipoProducto;

    public Producto() {}

    public Producto(int idProducto, String codigoBarras, String descripcion, double precio, 
                    int stockActual, int stockMinimo, String fechaDesde, String fechaHasta, 
                    Marca marca, TipoProducto tipoProducto) {
        this(idProducto, codigoBarras, descripcion, precio, 0.0, stockActual, stockMinimo,
             fechaHasta == null, fechaDesde, fechaHasta, marca, tipoProducto);
    }

    public Producto(int idProducto, String codigoBarras, String descripcion, double precioVenta,
                    double precioCosto, int stockActual, int stockMinimo, boolean estado,
                    String fechaDesde, String fechaHasta, Marca marca, TipoProducto tipoProducto) {
        this.idProducto = idProducto;
        this.codigoBarras = codigoBarras;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta;
        this.precioCosto = precioCosto;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.marca = marca;
        this.tipoProducto = tipoProducto;
    }

    /** Suma o resta existencias y evita que el stock resultante sea negativo. */
    public void actualizarStock(int cantidad) {
        int nuevoStock = Math.addExact(this.stockActual, cantidad);
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stockActual = nuevoStock;
    }

    /** Solo permite operar con productos activos y cantidades positivas disponibles. */
    public boolean validarDisponibilidad(int cant) {
        return this.estado && this.stockActual >= cant && cant > 0;
    }

    /** Indica si las existencias alcanzaron o bajaron del mínimo de reposición. */
    public boolean esStockCritico() {
        return this.estado && this.stockActual <= this.stockMinimo;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public double getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(double precioCosto) { this.precioCosto = precioCosto; }

    public double getPrecio() { return precioVenta; }
    public void setPrecio(double precio) { this.precioVenta = precio; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public Marca getMarca() { return marca; }
    public void setMarca(Marca marca) { this.marca = marca; }

    public TipoProducto getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(TipoProducto tipoProducto) { this.tipoProducto = tipoProducto; }

    @Override
    public String toString() { return descripcion + " ($" + precioVenta + ")"; }
}