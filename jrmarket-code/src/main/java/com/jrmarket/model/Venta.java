package com.jrmarket.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Venta de mostrador: reúne renglones, forma de pago, estado y turno asociado. */
public class Venta extends Comprobante {
    private String medioPago;
    private double montoRecibido;
    private double vuelto;
    private String comprobantePago;
    private TurnoCaja turnoCaja;
    private List<DetalleVenta> detalles;
    private EstadoVenta estadoActual;
    private List<HistorialEstadoVenta> historiales;

    public Venta() {
        super();
        this.detalles = new ArrayList<>();
        this.historiales = new ArrayList<>();
    }

    public Venta(int idVenta, String nroTicket, String fechaHora, Usuario usuario, 
                 String medioPago, TurnoCaja turnoCaja) {
        super(idVenta, nroTicket, fechaHora, usuario);
        this.medioPago = medioPago;
        this.turnoCaja = turnoCaja;
        this.detalles = new ArrayList<>();
        this.historiales = new ArrayList<>();
    }

    /** Agrega un producto al detalle o acumula cantidad en su renglón existente. */
    public void agregarItem(Producto p, int cantidad) {
        if (p == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }

        DetalleVenta existente = null;
        int cantidadAcumulada = cantidad;
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto() == p) {
                existente = detalle;
                cantidadAcumulada = Math.addExact(cantidadAcumulada, detalle.getCantidad());
            }
        }
        if (!p.validarDisponibilidad(cantidadAcumulada)) {
            throw new IllegalArgumentException("La cantidad solicitada supera el stock disponible o el producto está inactivo.");
        }

        if (existente != null) {
            existente.setCantidad(cantidadAcumulada);
            calcularTotal();
            return;
        }

        DetalleVenta detalle = new DetalleVenta(detalles.size() + 1, cantidad, p.getPrecioVenta(), p.getPrecioCosto(), p);
        detalle.setVenta(this);
        this.detalles.add(detalle);
        calcularTotal();
    }

    /** Quita un renglón y vuelve a calcular el total de la venta. */
    public void quitarItem(DetalleVenta item) {
        this.detalles.remove(item);
        calcularTotal();
    }

    /** Corrige una cantidad sin permitir que el total del producto supere el stock. */
    public void actualizarCantidad(DetalleVenta item, int cantidad) {
        if (item == null || !detalles.contains(item) || cantidad <= 0) {
            throw new IllegalArgumentException("Seleccione un renglón e ingrese una cantidad mayor que cero.");
        }

        Producto producto = item.getProducto();
        int cantidadTotal = cantidad;
        for (DetalleVenta detalle : detalles) {
            if (detalle != item && detalle.getProducto() == producto) {
                cantidadTotal = Math.addExact(cantidadTotal, detalle.getCantidad());
            }
        }
        if (producto == null || !producto.validarDisponibilidad(cantidadTotal)) {
            throw new IllegalArgumentException("La cantidad supera el stock disponible o el producto está inactivo.");
        }

        item.setCantidad(cantidad);
        calcularTotal();
    }

    /** Suma los subtotales de los renglones y guarda el total calculado. */
    @Override
    public double calcularTotal() {
        double acumulador = 0.0;
        for (DetalleVenta d : this.detalles) {
            acumulador += d.getSubtotal();
        }
        this.total = acumulador;
        return this.total;
    }

    /** Registra lo recibido en efectivo y calcula el vuelto para el cliente. */
    public double liquidarVuelto(double montoRecibido) {
        this.montoRecibido = montoRecibido;
        this.vuelto = montoRecibido - this.total;
        return this.vuelto;
    }

    /** Cambia el estado actual y agrega una entrada al historial de la venta. */
    public void cambiarEstado(EstadoVenta nuevoEstado, String obs) {
        this.estadoActual = nuevoEstado;
        HistorialEstadoVenta h = new HistorialEstadoVenta(
            historiales.size() + 1, 
            this.fechaHora != null ? this.fechaHora : "NOW", 
            null, 
            obs, 
            this, 
            nuevoEstado
        );
        this.historiales.add(h);
    }

    public EstadoVenta getEstadoActual() { return estadoActual; }

    /** Imputa esta venta al turno de caja asociado. */
    public void registrarCobro() {
        if (this.turnoCaja != null) {
            this.turnoCaja.acumularVenta(this);
        }
    }

    /** Valida pago, turno y stock; luego descuenta existencias y registra el cobro. */
    public void confirmarVenta() {
        if (detalles.isEmpty()) {
            throw new IllegalStateException("La venta debe tener al menos un producto.");
        }
        if (turnoCaja == null || !"ABIERTO".equalsIgnoreCase(turnoCaja.getEstado())) {
            throw new IllegalStateException("No hay un turno de caja abierto.");
        }
        if (estadoActual != null && "COBRADA".equalsIgnoreCase(estadoActual.getNombre())) {
            throw new IllegalStateException("La venta ya fue confirmada.");
        }

        calcularTotal();
        if (esPagoEfectivo()) {
            if (montoRecibido < total) {
                throw new IllegalArgumentException("El monto recibido no cubre el total de la venta.");
            }
            vuelto = montoRecibido - total;
        } else if (comprobantePago == null || comprobantePago.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el número de comprobante de la transferencia.");
        }

        Map<Producto, Integer> cantidadesPorProducto = new HashMap<>();
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto() == null || detalle.getCantidad() <= 0) {
                throw new IllegalStateException("El detalle de venta contiene datos inválidos.");
            }
            cantidadesPorProducto.merge(detalle.getProducto(), detalle.getCantidad(),
                (actual, adicional) -> Math.addExact(actual, adicional));
        }
        for (Map.Entry<Producto, Integer> entrada : cantidadesPorProducto.entrySet()) {
            if (!entrada.getKey().validarDisponibilidad(entrada.getValue())) {
                throw new IllegalStateException("El stock cambió y ya no alcanza para completar la venta.");
            }
        }

        for (Map.Entry<Producto, Integer> entrada : cantidadesPorProducto.entrySet()) {
            entrada.getKey().actualizarStock(-entrada.getValue());
        }
        cambiarEstado(new EstadoVenta(1, "Cobrada", "Comprobante liquidado en mostrador"), "Transacción finalizada");
        registrarCobro();
    }

    /** Reconoce las variantes del nombre de efectivo usadas por la interfaz. */
    private boolean esPagoEfectivo() {
        return medioPago != null && medioPago.toUpperCase().contains("EFECTIVO");
    }

    public int getIdVenta() { return idComprobante; }
    public void setIdVenta(int idVenta) { this.idComprobante = idVenta; }

    public String getNroTicket() { return nroComprobante; }
    public void setNroTicket(String nroTicket) { this.nroComprobante = nroTicket; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }

    public double getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(double montoRecibido) { this.montoRecibido = montoRecibido; }

    public double getVuelto() { return vuelto; }
    public void setVuelto(double vuelto) { this.vuelto = vuelto; }

    public String getComprobantePago() { return comprobantePago; }
    public void setComprobantePago(String comprobantePago) { this.comprobantePago = comprobantePago; }

    public TurnoCaja getTurnoCaja() { return turnoCaja; }
    public void setTurnoCaja(TurnoCaja turnoCaja) { this.turnoCaja = turnoCaja; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public void setEstadoActual(EstadoVenta estadoActual) { this.estadoActual = estadoActual; }

    public List<HistorialEstadoVenta> getHistoriales() { return historiales; }
    public void setHistoriales(List<HistorialEstadoVenta> historiales) { this.historiales = historiales; }
}