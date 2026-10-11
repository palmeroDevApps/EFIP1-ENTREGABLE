package com.jrmarket.model;

import java.util.ArrayList;
import java.util.List;

public class Compra extends Comprobante {
    private Proveedor proveedor;
    private List<DetalleCompra> detalles;

    public Compra() {
        super();
        this.detalles = new ArrayList<>();
    }

    public Compra(int idCompra, String nroComprobante, String fechaHora, Usuario usuario, Proveedor proveedor) {
        super(idCompra, nroComprobante, fechaHora, usuario);
        this.proveedor = proveedor;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(Producto p, int cant, double costo) {
        if (p == null || !p.isEstado() || cant <= 0 || costo < 0.0 || !Double.isFinite(costo)) {
            throw new IllegalArgumentException("Producto, cantidad y costo deben ser válidos.");
        }
        DetalleCompra detalle = new DetalleCompra(detalles.size() + 1, cant, costo, p);
        detalle.setCompra(this);
        this.detalles.add(detalle);
        calcularTotal();
    }

    public void quitarDetalle(DetalleCompra item) {
        this.detalles.remove(item);
        calcularTotal();
    }

    public void actualizarCostoUnitario(DetalleCompra detalle, double costoUnitario) {
        if (detalle == null || !detalles.contains(detalle)
                || costoUnitario < 0.0 || !Double.isFinite(costoUnitario)) {
            throw new IllegalArgumentException("El costo unitario debe ser válido para un renglón de esta compra.");
        }
        detalle.setCostoUnitario(costoUnitario);
        calcularTotal();
    }

    @Override
    public double calcularTotal() {
        double acumulado = 0.0;
        for (DetalleCompra dc : this.detalles) {
            acumulado += dc.getSubtotal();
        }
        this.total = acumulado;
        return this.total;
    }

    public void confirmarIngresoMercaderia() {
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La compra debe tener al menos un producto.");
        }
        java.util.Map<Producto, Integer> cantidadesPorProducto = new java.util.HashMap<>();
        for (DetalleCompra dc : this.detalles) {
            if (dc.getProducto() == null || !dc.getProducto().isEstado()
                    || dc.getCantidad() <= 0 || dc.getCostoUnitario() < 0.0
                    || !Double.isFinite(dc.getCostoUnitario())) {
                throw new IllegalStateException("El detalle de compra contiene datos inválidos.");
            }
            cantidadesPorProducto.merge(dc.getProducto(), dc.getCantidad(),
                (actual, adicional) -> Math.addExact(actual, adicional));
        }
        for (java.util.Map.Entry<Producto, Integer> entrada : cantidadesPorProducto.entrySet()) {
            Math.addExact(entrada.getKey().getStockActual(), entrada.getValue());
        }
        for (java.util.Map.Entry<Producto, Integer> entrada : cantidadesPorProducto.entrySet()) {
            entrada.getKey().actualizarStock(entrada.getValue());
        }
        for (DetalleCompra dc : this.detalles) {
            dc.getProducto().setPrecioCosto(dc.getCostoUnitario());
        }
    }

    public int getIdCompra() { return idComprobante; }
    public void setIdCompra(int idCompra) { this.idComprobante = idCompra; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }
}