package com.jrmarket.model;

import java.time.LocalDate;

public class MedioPago {
    private int id;
    private String descripcion;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta; // null indica que sigue vigente
    private boolean afectaCajaFisica; // true = Efectivo en cajón, false = Banco / Virtual

    public MedioPago(int id, String descripcion, LocalDate fechaDesde, LocalDate fechaHasta, boolean afectaCajaFisica) {
        this.id = id;
        this.descripcion = descripcion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.afectaCajaFisica = afectaCajaFisica;
    }

    // Evalúa si el medio de pago está habilitado para operar hoy
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now();
        boolean desdeValido = (fechaDesde != null) && (!hoy.isBefore(fechaDesde));
        boolean hastaValido = (fechaHasta == null) || (!hoy.isAfter(fechaHasta));
        return desdeValido && hastaValido;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }

    public boolean isAfectaCajaFisica() { return afectaCajaFisica; }
    public void setAfectaCajaFisica(boolean afectaCajaFisica) { this.afectaCajaFisica = afectaCajaFisica; }

    @Override
    public String toString() {
        return descripcion; // Permite cargar directamente el objeto en un ComboBox de JavaFX
    }
}