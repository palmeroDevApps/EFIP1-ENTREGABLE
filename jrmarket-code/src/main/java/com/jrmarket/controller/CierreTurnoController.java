package com.jrmarket.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.jrmarket.dao.JRMarketDAO;
import com.jrmarket.model.TurnoCaja;
import com.jrmarket.model.Usuario;
import com.jrmarket.util.FormatoImporte;
import com.jrmarket.util.GestorArchivos;

/** Presenta el balance del turno y coordina su arqueo y cierre con persistencia en MySQL y archivo TXT. */
public class CierreTurnoController implements Initializable {

    @FXML private Label lblOperador;
    @FXML private Label lblTurnoId;
    @FXML private Label lblFechaApertura;
    @FXML private Label lblFechaCierre;
    @FXML private Label lblEstadoTurno;
    @FXML private HBox boxAlertaRol;
    @FXML private Label lblMensajeRol;

    @FXML private Label lblFondoInicial;
    @FXML private Label lblVentasEfectivo;
    @FXML private Label lblEfectivoEsperado;
    @FXML private Label lblVentasTransferencia;
    @FXML private Label lblCantidadTickets;
    @FXML private Label lblTotalFacturado;

    @FXML private VBox panelArqueo;
    @FXML private TextField txtEfectivoReal;
    @FXML private Label lblDesvioEfectivo;
    @FXML private TextField txtTransferenciaReal;
    @FXML private Label lblDesvioTransferencia;
    @FXML private Label lblDiferenciaTotal;
    @FXML private Label lblEstadoDiferencia;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnConfirmarCierre;
    @FXML private Label lblMensajeFeedback;

    private TurnoCaja turnoActual;
    private Usuario operador;
    private final JRMarketDAO dao = new JRMarketDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void inicializarContexto(Usuario usuario, TurnoCaja turno) {
        this.operador = usuario;
        this.turnoActual = turno;

        lblOperador.setText(usuario.getNombreCompleto());
        lblTurnoId.setText("Turno: #" + turno.getIdTurno());
        lblFechaApertura.setText("Apertura: " + turno.getFechaHoraApertura());
        lblEstadoTurno.setText(turno.getEstado());

        lblFondoInicial.setText("$ " + FormatoImporte.formatear(turno.getMontoInicial()));
        lblVentasEfectivo.setText("$ " + FormatoImporte.formatear(turno.getTotalVentasEfectivo()));
        lblEfectivoEsperado.setText("$ " + FormatoImporte.formatear(turno.getSaldoTeoricoSistema()));
        lblVentasTransferencia.setText("$ " + FormatoImporte.formatear(turno.getTotalVentasTransferencia()));
        lblCantidadTickets.setText(turno.getVentas().size() + " tickets emitidos");
        lblTotalFacturado.setText("$ " + FormatoImporte.formatear(turno.getTotalVentasEfectivo() + turno.getTotalVentasTransferencia()));

        boolean tienePermiso = usuario.tienePermiso("CIERRE_CAJA");
        panelArqueo.setDisable(!tienePermiso);
        boxAlertaRol.setVisible(!tienePermiso);
        boxAlertaRol.setManaged(!tienePermiso);
    }

    @FXML
    private void calcularConciliacion() {
        try {
            double efReal = txtEfectivoReal.getText().isEmpty() ? 0.0 : Double.parseDouble(txtEfectivoReal.getText());
            double trReal = txtTransferenciaReal.getText().isEmpty() ? 0.0 : Double.parseDouble(txtTransferenciaReal.getText());
            if (!Double.isFinite(efReal) || !Double.isFinite(trReal) || efReal < 0.0 || trReal < 0.0) {
                throw new IllegalArgumentException("Los importes deben ser números positivos.");
            }

            turnoActual.conciliarSaldo(efReal, trReal, txtObservaciones.getText());

            double desvioEf = efReal - turnoActual.getSaldoTeoricoSistema();
            double desvioTr = trReal - turnoActual.getTotalVentasTransferencia();
            lblDesvioEfectivo.setText("$ " + FormatoImporte.formatear(desvioEf));
            lblDesvioTransferencia.setText("$ " + FormatoImporte.formatear(desvioTr));
            lblDiferenciaTotal.setText("$ " + FormatoImporte.formatear(turnoActual.getDiferencia()));

            btnConfirmarCierre.setDisable(false);
        } catch (IllegalArgumentException e) {
            btnConfirmarCierre.setDisable(true);
        }
    }

    @FXML
    private void confirmarCierreTurno() {
        String fechaCierre = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        turnoActual.setFechaHoraCierre(fechaCierre);
        turnoActual.cerrarTurno();

        dao.cerrarTurnoCaja(turnoActual);
        String infoArchivo = "";
        try {
            File reporte = GestorArchivos.exportarArqueoTurno(turnoActual);
            infoArchivo = " (Exportado: " + reporte.getName() + ")";
        } catch (IOException e) {
            System.err.println("Error al exportar arqueo: " + e.getMessage());
        }

        lblEstadoTurno.setText(turnoActual.getEstado());
        lblFechaCierre.setText("Cierre: " + fechaCierre);

        txtEfectivoReal.setDisable(true);
        txtTransferenciaReal.setDisable(true);
        txtObservaciones.setDisable(true);
        btnConfirmarCierre.setDisable(true);

        lblMensajeFeedback.setText("Turno cerrado con éxito. Saldo físico registrado: $ "
                + FormatoImporte.formatear(turnoActual.getSaldoRealFisico()) + infoArchivo);
    }

    @FXML
    private void cancelarCierre() {
        txtEfectivoReal.clear();
        txtTransferenciaReal.clear();
        txtObservaciones.clear();
        calcularConciliacion();
    }
}