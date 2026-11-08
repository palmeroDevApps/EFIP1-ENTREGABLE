package com.jrmarket.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.dao.JRMarketDAO;
import com.jrmarket.model.*;
import com.jrmarket.util.FormatoImporte;
import com.jrmarket.util.GestorArchivos;

/**
 * Controlador para la interfaz de mostrador de ventas y cobros (VentasView).
 * Administra el flujo transaccional de venta, verificación de stock y persistencia ACID.
 */
public class VentasController implements Initializable {

    // =========================================================================
    // 1. COMPONENTES FXML (VISTA)
    // =========================================================================
    
    // Panel superior: Estado del Turno y Operador
    @FXML private Label lblOperador;
    @FXML private Label lblNroTurno;
    @FXML private Label lblEstadoCaja;
    @FXML private Button btnAbrirTurno;
    @FXML private VBox boxOperacionesVenta;

    // Panel de Búsqueda y Entrada de Ítems
    @FXML private TextField txtCodigoBarras;
    @FXML private Button btnExplorar;

    // Grilla del Carrito de Ventas
    @FXML private TableView<DetalleVenta> tblDetalleVenta;
    @FXML private TableColumn<DetalleVenta, String> colCodigo;
    @FXML private TableColumn<DetalleVenta, String> colMarca;
    @FXML private TableColumn<DetalleVenta, String> colDescripcion;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotal;
    @FXML private Button btnQuitarItem;
    @FXML private Button btnCancelarVenta;

    // Panel de Liquidación, Cobro y Vuelto
    @FXML private Label lblTotalPagar;
    @FXML private ComboBox<MedioPago> cbMedioPago;
    @FXML private TextField txtNroComprobante;
    @FXML private TextField txtPagaCon;
    @FXML private Label lblVuelto;
    @FXML private Button btnConfirmarVenta;

    // =========================================================================
    // 2. ATRIBUTOS DEL MODELO Y ESTADO
    // =========================================================================
    private Usuario operadorActual;
    private List<TurnoCaja> turnosCaja;
    private TurnoCaja turnoActivo;
    private List<Producto> catalogoCompartido;

    private Venta ventaActual;
    private final ObservableList<DetalleVenta> itemsVentaObservable = FXCollections.observableArrayList();
    private final JRMarketDAO dao = new JRMarketDAO();

    // =========================================================================
    // 3. INICIALIZACIÓN Y ENLACE MVC
    // =========================================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasTabla();
        configurarOyentesCobro();
        cargarMediosPago();
    }

    /**
     * Recibe los datos de contexto desde el Menú Principal al abrir la vista.
     */
    public void inicializarContexto(Usuario usuario, List<TurnoCaja> turnos, List<Producto> catalogo) {
        this.operadorActual = usuario;
        this.turnosCaja = turnos;
        this.catalogoCompartido = catalogo;

        lblOperador.setText(usuario.getNombreCompleto());
        refrescarEstadoCaja();
        iniciarNuevaVenta();
    }

    public void observarEstadoTurno(Stage stage) {
        stage.focusedProperty().addListener((obs, antes, tieneFoco) -> {
            if (tieneFoco) {
                boolean estabaAbierto = turnoActivo != null && "ABIERTO".equalsIgnoreCase(turnoActivo.getEstado());
                boolean estaAbierto = refrescarEstadoCaja();
                if (!estabaAbierto && estaAbierto && ventaActual == null) {
                    iniciarNuevaVenta();
                }
            }
        });
    }

    // =========================================================================
    // 4. CONFIGURACIÓN VISUAL (HELPERS DE VISTA)
    // =========================================================================
    private void configurarColumnasTabla() {
        colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getCodigoBarras()));
        colMarca.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getMarca().getNombre()));
        colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getDescripcion()));

        // Edición de cantidad directa sobre la grilla
        tblDetalleVenta.setEditable(true);
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colCantidad.setOnEditCommit(event -> {
            try {
                ventaActual.actualizarCantidad(event.getRowValue(), event.getNewValue());
                itemsVentaObservable.setAll(ventaActual.getDetalles());
                actualizarTotalesUI();
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Cantidad inválida", e.getMessage());
                tblDetalleVenta.refresh();
            }
        });

        // Formato monetario para precio unitario y subtotal
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioCobrado"));
        colPrecioUnitario.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double importe, boolean empty) {
                super.updateItem(importe, empty);
                setText(empty || importe == null ? null : FormatoImporte.formatear(importe));
            }
        });

        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double importe, boolean empty) {
                super.updateItem(importe, empty);
                setText(empty || importe == null ? null : FormatoImporte.formatear(importe));
            }
        });

        tblDetalleVenta.setItems(itemsVentaObservable);
    }

    private void configurarOyentesCobro() {
        cbMedioPago.setOnAction(event -> calcularVuelto());
        txtNroComprobante.textProperty().addListener((obs, ant, act) -> {
            MedioPago medio = cbMedioPago.getValue();
            if (medio != null && !medio.isAfectaCajaFisica()) {
                calcularVuelto();
            }
        });
    }

    private void cargarMediosPago() {
        List<MedioPago> mediosBD = dao.consultarMediosPagoDisponibles();
        List<MedioPago> mediosVigentes = new ArrayList<>();

        Iterator<MedioPago> it = mediosBD.iterator();
        while (it.hasNext()) {
            MedioPago mp = it.next();
            if (mp.isVigente()) {
                mediosVigentes.add(mp);
            }
        }

        if (mediosVigentes.isEmpty()) {
            mediosVigentes.add(new MedioPago(1, "Efectivo", LocalDate.of(2026, 1, 1), null, true));
            mediosVigentes.add(new MedioPago(2, "Transferencia / QR", LocalDate.of(2026, 1, 1), null, false));
        }

        cbMedioPago.setItems(FXCollections.observableArrayList(mediosVigentes));
        cbMedioPago.getSelectionModel().selectFirst();
    }

    // =========================================================================
    // 5. GESTIÓN DEL TURNO DE CAJA
    // =========================================================================
    private TurnoCaja buscarTurnoAbierto() {
        for (int i = turnosCaja.size() - 1; i >= 0; i--) {
            TurnoCaja turno = turnosCaja.get(i);
            if ("ABIERTO".equalsIgnoreCase(turno.getEstado())) return turno;
        }
        return null;
    }

    private boolean refrescarEstadoCaja() {
        turnoActivo = buscarTurnoAbierto();
        boolean turnoAbierto = turnoActivo != null;
        int numeroTurno = turnoAbierto ? turnoActivo.getIdTurno() : turnosCaja.size() + 1;

        lblNroTurno.setText("#" + numeroTurno);
        lblEstadoCaja.setText(turnoAbierto ? "ABIERTO" : "CERRADO");
        lblEstadoCaja.setStyle(turnoAbierto ? "-fx-text-fill: #2e7d32;" : "-fx-text-fill: #b71c1c;");
        btnAbrirTurno.setDisable(turnoAbierto);
        btnAbrirTurno.setText(turnoAbierto ? "Turno abierto" : "Abrir turno");
        boxOperacionesVenta.setDisable(!turnoAbierto);

        if (!turnoAbierto) {
            ventaActual = null;
            itemsVentaObservable.clear();
            lblTotalPagar.setText("$ " + FormatoImporte.formatear(0.0));
            lblVuelto.setText("$ " + FormatoImporte.formatear(0.0));
            btnConfirmarVenta.setDisable(true);
        }
        return turnoAbierto;
    }

    @FXML
    private void abrirTurno() {
        if (refrescarEstadoCaja()) return;

        TextInputDialog dialogo = new TextInputDialog(FormatoImporte.formatear(20000.0));
        dialogo.setTitle("Apertura de turno");
        dialogo.setHeaderText("Ingrese el efectivo contado para iniciar la caja");
        dialogo.setContentText("Fondo inicial:");

        String textoMonto = dialogo.showAndWait().orElse(null);
        if (textoMonto == null) return;

        try {
            double montoInicial = FormatoImporte.parsear(textoMonto);
            if (montoInicial < 0.0) {
                throw new IllegalArgumentException("El fondo inicial no puede ser negativo.");
            }

            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            TurnoCaja nuevoTurno = new TurnoCaja(turnosCaja.size() + 1, operadorActual);
            nuevoTurno.abrirTurno(montoInicial, fecha);
            dao.insertarTurnoCaja(nuevoTurno);
            turnosCaja.add(nuevoTurno);
            turnoActivo = nuevoTurno;

            refrescarEstadoCaja();
            iniciarNuevaVenta();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta("No se pudo abrir la caja", e.getMessage());
        }
    }

    // =========================================================================
    // 6. FLUJO DEL CARRITO (CARGA Y VALIDACIÓN DE ÍTEMS)
    // =========================================================================
    private void iniciarNuevaVenta() {
        if (turnoActivo == null || !"ABIERTO".equalsIgnoreCase(turnoActivo.getEstado())) {
            ventaActual = null;
            itemsVentaObservable.clear();
            actualizarTotalesUI();
            return;
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String ticket = "TK-0001-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        MedioPago medioInicial = cbMedioPago.getValue();
        String descMedio = medioInicial != null ? medioInicial.getDescripcion() : "";

        this.ventaActual = new Venta(1, ticket, fecha, operadorActual, descMedio, turnoActivo);
        itemsVentaObservable.clear();
        actualizarTotalesUI();
    }

    @FXML
    private void agregarPorCodigo() {
        if (!validarTurnoAbierto()) return;
        String criterio = txtCodigoBarras.getText().trim();
        if (criterio.isEmpty() || catalogoCompartido == null) return;

        Producto encontrado = null;
        Iterator<Producto> it = catalogoCompartido.iterator();
        while (it.hasNext()) {
            Producto p = it.next();
            if (p.isEstado() && p.getCodigoBarras().equalsIgnoreCase(criterio)) {
                encontrado = p;
                break;
            }
        }

        if (encontrado == null) {
            abrirExploradorProductos(criterio);
            return;
        }

        agregarProductoAVenta(encontrado, 1);
    }

    @FXML
    private void abrirExploradorProductos() {
        if (!validarTurnoAbierto()) return;
        abrirExploradorProductos("");
    }

    private void abrirExploradorProductos(String filtro) {
        if (!validarTurnoAbierto()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/ProductoView.fxml"));
            Parent root = loader.load();

            ProductoController controller = loader.getController();
            controller.setCatalogo(catalogoCompartido);
            controller.setModoSeleccionVenta(true);
            controller.setBusquedaInicial(filtro);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Selector de Productos");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isItemConfirmado() && controller.getProductoSeleccionado() != null) {
                agregarProductoAVenta(controller.getProductoSeleccionado(), controller.getCantidadSeleccionada());
            }
        } catch (IOException e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private void agregarProductoAVenta(Producto producto, int cantidad) {
        if (!validarTurnoAbierto()) return;
        
        // Validación de regla de negocio en el Modelo
        if (!producto.validarDisponibilidad(cantidad)) {
            mostrarAlerta("Stock insuficiente", "Existencias disponibles: " + producto.getStockActual());
            return;
        }

        try {
            ventaActual.agregarItem(producto, cantidad);
            itemsVentaObservable.setAll(ventaActual.getDetalles());
            txtCodigoBarras.clear();
            actualizarTotalesUI();
        } catch (IllegalArgumentException e) {
            mostrarAlerta("Stock insuficiente", e.getMessage());
        }
    }

    @FXML
    private void quitarItemSeleccionado() {
        if (!validarTurnoAbierto()) return;
        DetalleVenta seleccionado = tblDetalleVenta.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            ventaActual.quitarItem(seleccionado);
            itemsVentaObservable.setAll(ventaActual.getDetalles());
            actualizarTotalesUI();
        }
    }

    @FXML
    private void cancelarVentaCompleta() {
        if (!validarTurnoAbierto()) return;
        iniciarNuevaVenta();
        txtPagaCon.clear();
        txtNroComprobante.clear();
    }

    // =========================================================================
    // 7. LIQUIDACIÓN, COBRO Y CÁLCULO DE VUELTO
    // =========================================================================
    private void actualizarTotalesUI() {
        if (ventaActual == null) {
            lblTotalPagar.setText("$ " + FormatoImporte.formatear(0.0));
            btnConfirmarVenta.setDisable(true);
            calcularVuelto();
            return;
        }
        double total = ventaActual.calcularTotal(); // Llamada polimórfica (Comprobante)
        lblTotalPagar.setText("$ " + FormatoImporte.formatear(total));
        btnConfirmarVenta.setDisable(ventaActual.getDetalles().isEmpty());
        calcularVuelto();
    }

    @FXML
    private void calcularVuelto() {
        if (ventaActual == null) {
            btnConfirmarVenta.setDisable(true);
            return;
        }

        MedioPago medioSeleccionado = cbMedioPago.getValue();
        // Caso transferencia / electrónico (no maneja efectivo físico)
        if (medioSeleccionado != null && !medioSeleccionado.isAfectaCajaFisica()) {
            lblVuelto.setText("$ 0,00");
            txtPagaCon.setDisable(true);
            txtNroComprobante.setDisable(false);
            btnConfirmarVenta.setDisable(ventaActual.getDetalles().isEmpty()
                    || txtNroComprobante.getText().trim().isEmpty());
            return;
        }

        // Caso efectivo
        txtPagaCon.setDisable(false);
        txtNroComprobante.setDisable(true);

        String texto = txtPagaCon.getText().trim();
        if (texto.isEmpty()) {
            lblVuelto.setText("$ 0,00");
            btnConfirmarVenta.setDisable(true);
            return;
        }

        try {
            double paga = Double.parseDouble(texto);
            double vuelto = ventaActual.liquidarVuelto(paga);

            if (vuelto >= 0) {
                lblVuelto.setText("$ " + FormatoImporte.formatear(vuelto));
                btnConfirmarVenta.setDisable(false);
            } else {
                lblVuelto.setText("Monto insuficiente");
                btnConfirmarVenta.setDisable(true);
            }
        } catch (NumberFormatException e) {
            lblVuelto.setText("Inválido");
            btnConfirmarVenta.setDisable(true);
        }
    }

    // =========================================================================
    // 8. CONFIRMACIÓN, PERSISTENCIA ATÓMICA Y EMISIÓN DE TICKET
    // =========================================================================
    @FXML
    private void confirmarVenta() {
        if (!validarTurnoAbierto()) return;

        MedioPago medioSeleccionado = cbMedioPago.getValue();
        if (medioSeleccionado == null || !medioSeleccionado.isVigente()) {
            mostrarAlerta("Medio de pago inválido", "Seleccione un medio de pago vigente.");
            return;
        }

        ventaActual.setMedioPago(medioSeleccionado.getDescripcion());
        ventaActual.setComprobantePago(txtNroComprobante.getText().trim());

        if (medioSeleccionado.isAfectaCajaFisica()) {
            try {
                ventaActual.liquidarVuelto(Double.parseDouble(txtPagaCon.getText().trim()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Importe inválido", "Ingrese el importe recibido en efectivo.");
                return;
            }
        }

        try {
            ventaActual.confirmarVenta();
            EstadoVenta estadoCobrada = dao.consultarEstadoVentaPorNombre("Cobrada");
            ventaActual.setEstadoActual(estadoCobrada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta("No se pudo confirmar la venta", e.getMessage());
            return;
        }

        // 1. Persistencia atómica en Base de Datos MySQL (ACID)
        boolean guardadoEnBD = dao.registrarVentaTransaccional(ventaActual, medioSeleccionado);

        // 2. Generación física de comprobante mediante java.io
        String rutaTicketTxt = "";
        try {
            File archivoTicket = GestorArchivos.exportarTicketVenta(ventaActual);
            rutaTicketTxt = "\nArchivo ticket: " + archivoTicket.getName();
        } catch (IOException e) {
            System.err.println("Error al exportar archivo de ticket: " + e.getMessage());
        }

        // 3. Notificación al usuario y reseteo
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("JRMARKET - Venta Registrada");
        alerta.setHeaderText("Ticket: " + ventaActual.getNroTicket());
        alerta.setContentText("Total cobrado: $ " + FormatoImporte.formatear(ventaActual.getTotal())
                + "\nMedio: " + medioSeleccionado.getDescripcion()
                + "\nEstado: " + ventaActual.getEstadoActual().getNombre()
                + (guardadoEnBD ? "\nPersistencia MySQL: OK" : "")
                + rutaTicketTxt);
        alerta.showAndWait();

        cancelarVentaCompleta();
    }

    // =========================================================================
    // 9. MÉTODOS DE SOPORTE Y VALIDACIONES
    // =========================================================================
    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean validarTurnoAbierto() {
        if (refrescarEstadoCaja()) return true;
        mostrarAlerta("Caja cerrada", "Abra un turno de caja antes de registrar ventas.");
        return false;
    }
}