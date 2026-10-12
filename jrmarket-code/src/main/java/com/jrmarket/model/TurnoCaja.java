package com.jrmarket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Estado y balance de una sesión de caja abierta por un usuario. */
public class TurnoCaja implements Serializable {
    private int idTurno;
    private String fechaHoraApertura;
    private String fechaHoraCierre;
    private double montoInicial;
    private double totalVentasEfectivo;
    private double totalVentasTransferencia;
    private double saldoTeoricoSistema;
    private double saldoRealFisico;
    private double diferencia;
    private String observaciones;
    private String estado;
    private Usuario usuario;
    private List<Venta> ventas;

    public TurnoCaja() {
        this.ventas = new ArrayList<>();
        // Un turno debe abrirse explícitamente después de contar el fondo inicial.
        this.estado = "CERRADO";
    }

    public TurnoCaja(int idTurno, Usuario usuario) {
        this();
        this.idTurno = idTurno;
        this.usuario = usuario;
    }

    /** Inicia el turno con el efectivo contado y reinicia los acumuladores del turno. */
    public void abrirTurno(double montoBase, String fechaHoraApertura) {
        if (!"CERRADO".equalsIgnoreCase(this.estado)) {
            throw new IllegalStateException("El turno ya está abierto.");
        }
        if (!Double.isFinite(montoBase) || montoBase < 0.0
                || fechaHoraApertura == null || fechaHoraApertura.trim().isEmpty()) {
            throw new IllegalArgumentException("El fondo inicial y la fecha de apertura deben ser válidos.");
        }

        this.montoInicial = montoBase;
        this.totalVentasEfectivo = 0.0;
        this.totalVentasTransferencia = 0.0;
        this.saldoTeoricoSistema = montoBase;
        this.saldoRealFisico = 0.0;
        this.diferencia = 0.0;
        this.observaciones = null;
        this.fechaHoraApertura = fechaHoraApertura;
        this.fechaHoraCierre = null;
        this.ventas.clear();
        this.estado = "ABIERTO";
    }

    /** Acumula la venta una sola vez y separa efectivo de transferencias. */
    public void acumularVenta(Venta v) {
        if (v == null) return;
        if (!"ABIERTO".equalsIgnoreCase(this.estado)) {
            throw new IllegalStateException("No se pueden registrar ventas en un turno cerrado.");
        }
        if (this.ventas.contains(v)) return;
        this.ventas.add(v);
        if (v.getMedioPago() != null && v.getMedioPago().toUpperCase().contains("EFECTIVO")) {
            this.totalVentasEfectivo += v.getTotal();
            this.saldoTeoricoSistema += v.getTotal();
        } else {
            this.totalVentasTransferencia += v.getTotal();
        }
    }

    /** Conciliación simple del efectivo para compatibilidad con llamadas anteriores. */
    public void conciliarSaldo(double montoContado, String obs) {
        this.saldoRealFisico = montoContado;
        this.observaciones = obs;
        this.diferencia = montoContado - this.saldoTeoricoSistema;
    }

    /** Compara efectivo y transferencias contadas contra los totales del sistema. */
    public void conciliarSaldo(double efectivoContado, double transferenciasVerificadas, String obs) {
        if (!Double.isFinite(efectivoContado) || !Double.isFinite(transferenciasVerificadas)
                || efectivoContado < 0.0 || transferenciasVerificadas < 0.0) {
            throw new IllegalArgumentException("Los importes conciliados deben ser positivos.");
        }
        this.saldoRealFisico = efectivoContado + transferenciasVerificadas;
        this.observaciones = obs;
        this.diferencia = efectivoContado - this.saldoTeoricoSistema
            + transferenciasVerificadas - this.totalVentasTransferencia;
    }

    /** Cambia el turno abierto a cerrado al finalizar la jornada del operador. */
    public void cerrarTurno() {
        if (!"ABIERTO".equalsIgnoreCase(this.estado)) {
            throw new IllegalStateException("El turno no está abierto.");
        }
        this.estado = "CERRADO";
    }

    public int getIdTurno() { return idTurno; }
    public void setIdTurno(int idTurno) { this.idTurno = idTurno; }

    public String getFechaHoraApertura() { return fechaHoraApertura; }
    public void setFechaHoraApertura(String fechaHoraApertura) { this.fechaHoraApertura = fechaHoraApertura; }

    public String getFechaHoraCierre() { return fechaHoraCierre; }
    public void setFechaHoraCierre(String fechaHoraCierre) { this.fechaHoraCierre = fechaHoraCierre; }

    public double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(double montoInicial) { this.montoInicial = montoInicial; }

    public double getTotalVentasEfectivo() { return totalVentasEfectivo; }
    public void setTotalVentasEfectivo(double totalVentasEfectivo) { this.totalVentasEfectivo = totalVentasEfectivo; }

    public double getTotalVentasTransferencia() { return totalVentasTransferencia; }
    public void setTotalVentasTransferencia(double totalVentasTransferencia) { this.totalVentasTransferencia = totalVentasTransferencia; }

    public double getSaldoTeoricoSistema() { return saldoTeoricoSistema; }
    public void setSaldoTeoricoSistema(double saldoTeoricoSistema) { this.saldoTeoricoSistema = saldoTeoricoSistema; }

    public double getSaldoRealFisico() { return saldoRealFisico; }
    public void setSaldoRealFisico(double saldoRealFisico) { this.saldoRealFisico = saldoRealFisico; }

    public double getDiferencia() { return diferencia; }
    public void setDiferencia(double diferencia) { this.diferencia = diferencia; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Venta> getVentas() { return ventas; }
    public void setVentas(List<Venta> ventas) { this.ventas = ventas; }
}