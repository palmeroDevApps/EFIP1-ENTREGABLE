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
import javafx.util.converter.IntegerStringConverter;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.model.*;
import com.jrmarket.util.FormatoImporte;

/** Controla el mostrador, la apertura manual del turno y el cobro de ventas. */
public class VentasController implements Initializable {

    @FXML private Label lblOperador;
    @FXML private Label lblNroTurno;
    @FXML private Label lblEstadoCaja;
    @FXML private Button btnAbrirTurno;
    @FXML private VBox boxOperacionesVenta;

    @FXML private TextField txtCodigoBarras;
    @FXML private Button btnExplorar;

    @FXML private TableView<DetalleVenta> tblDetalleVenta;
    @FXML private TableColumn<DetalleVenta, String> colCodigo;
    @FXML private TableColumn<DetalleVenta, String> colMarca;
    @FXML private TableColumn<DetalleVenta, String> colDescripcion;
    @FXML private TableColumn<DetalleVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleVenta, Double> colPrecioUnitario;
    @FXML private TableColumn<DetalleVenta, Double> colSubtotal;

    @FXML private Button btnQuitarItem;
    @FXML private Button btnCancelarVenta;
    @FXML private Label lblTotalPagar;
    @FXML private ComboBox<String> cbMedioPago;
    @FXML private TextField txtNroComprobante;
    @FXML private TextField txtPagaCon;
    @FXML private Label lblVuelto;
    @FXML private Button btnConfirmarVenta;

    // Dependencias inyectadas desde MenuPrincipalController
    private Usuario operadorActual;
    private List<TurnoCaja> turnosCaja;
    private TurnoCaja turnoActivo;
    private List<Producto> catalogoCompartido;

    // Entidad de negocio activa
    private Venta ventaActual;
    private ObservableList<DetalleVenta> itemsVentaObservable = FXCollections.observableArrayList();

    /** Configura los controles y enlaza las columnas con los datos del modelo. */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbMedioPago.setItems(FXCollections.observableArrayList("Efectivo", "Transferencia / QR"));
        cbMedioPago.getSelectionModel().selectFirst();
        cbMedioPago.setOnAction(event -> calcularVuelto());
        txtNroComprobante.textProperty().addListener((obs, anterior, actual) -> {
            if ("Transferencia / QR".equals(cbMedioPago.getValue())) calcularVuelto();
        });

        // Enlace a los atributos de Producto y DetalleVenta
        colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getCodigoBarras()));
        colMarca.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getMarca().getNombre()));
        colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getDescripcion()));
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
        colPrecioUnitario.setCellValueFactory(new PropertyValueFactory<>("precioCobrado"));
        colPrecioUnitario.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double importe, boolean empty) {
                super.updateItem(importe, empty);
                setText(empty || importe == null ? null : FormatoImporte.formatear(importe));
            }
        });
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double importe, boolean empty) {
                super.updateItem(importe, empty);
                setText(empty || importe == null ? null : FormatoImporte.formatear(importe));
            }
        });

        tblDetalleVenta.setItems(itemsVentaObservable);
    }

    /** Recibe del menú el usuario y las colecciones compartidas de esta sesión. */
    public void inicializarContexto(Usuario usuario, List<TurnoCaja> turnos, List<Producto> catalogo) {
        this.operadorActual = usuario;
        this.turnosCaja = turnos;
        this.catalogoCompartido = catalogo;

        lblOperador.setText(usuario.getNombreCompleto());
        refrescarEstadoCaja();
        iniciarNuevaVenta();
    }

    /** Busca desde el turno más reciente para mantener el orden de apertura. */
    private TurnoCaja buscarTurnoAbierto() {
        for (int indice = turnosCaja.size() - 1; indice >= 0; indice--) {
            TurnoCaja turno = turnosCaja.get(indice);
            if ("ABIERTO".equalsIgnoreCase(turno.getEstado())) return turno;
        }
        return null;
    }

    /** Actualiza el encabezado y bloquea ventas cuando no existe un turno abierto. */
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
    /** Solicita el efectivo contado e inicia un nuevo turno en memoria. */
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
            turnosCaja.add(nuevoTurno);
            turnoActivo = nuevoTurno;
            refrescarEstadoCaja();
            iniciarNuevaVenta();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta("No se pudo abrir la caja", e.getMessage());
        }
    }

    /** Crea el comprobante vacío asociado al turno activo. */
    private void iniciarNuevaVenta() {
        if (turnoActivo == null || !"ABIERTO".equalsIgnoreCase(turnoActivo.getEstado())) {
            ventaActual = null;
            itemsVentaObservable.clear();
            actualizarTotalesUI();
            return;
        }
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String ticket = "TK-0001-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);

        this.ventaActual = new Venta(1, ticket, fecha, operadorActual, cbMedioPago.getValue(), turnoActivo);
        itemsVentaObservable.clear();
        actualizarTotalesUI();
    }

    /** Sincroniza el mostrador cuando se vuelve de la ventana de cierre de caja. */
    public void observarEstadoTurno(Stage stage) {
        stage.focusedProperty().addListener((observacion, focoAnterior, tieneFoco) -> {
            if (tieneFoco) {
                boolean estabaAbierto = turnoActivo != null
                    && "ABIERTO".equalsIgnoreCase(turnoActivo.getEstado());
                boolean estaAbierto = refrescarEstadoCaja();
                if (!estabaAbierto && estaAbierto && ventaActual == null) {
                    iniciarNuevaVenta();
                }
            }
        });
    }
    @FXML
    /** Agrega por código exacto o abre el selector con el texto escrito como filtro. */
    private void agregarPorCodigo() {
        if (!validarTurnoAbierto()) return;
        String criterio = txtCodigoBarras.getText().trim();
        if (criterio.isEmpty() || catalogoCompartido == null) return;

        Producto encontrado = null;
        for (Producto p : catalogoCompartido) {
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

    /** Abre el catálogo modal y recibe el producto y cantidad elegidos. */
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

    /** Valida stock y refleja en la tabla el renglón agregado al modelo. */
    private void agregarProductoAVenta(Producto producto, int cantidad) {
        if (!validarTurnoAbierto()) return;
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

    /** Mantiene total, vuelto y disponibilidad del botón según el contenido actual. */
    private void actualizarTotalesUI() {
        if (ventaActual == null) {
            lblTotalPagar.setText("$ " + FormatoImporte.formatear(0.0));
            btnConfirmarVenta.setDisable(true);
            calcularVuelto();
            return;
        }
        double total = ventaActual.calcularTotal();
        lblTotalPagar.setText("$ " + FormatoImporte.formatear(total));
        btnConfirmarVenta.setDisable(ventaActual.getDetalles().isEmpty());
        calcularVuelto();
    }

    @FXML
    /** Actualiza el vuelto o valida el comprobante según el medio seleccionado. */
    private void calcularVuelto() {
        if (ventaActual == null) {
            btnConfirmarVenta.setDisable(true);
            return;
        }
        if ("Transferencia / QR".equals(cbMedioPago.getValue())) {
            lblVuelto.setText("$ 0,00");
            txtPagaCon.setDisable(true);
            txtNroComprobante.setDisable(false);
            btnConfirmarVenta.setDisable(ventaActual.getDetalles().isEmpty()
                || txtNroComprobante.getText().trim().isEmpty());
            return;
        }

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

    @FXML
    /** Pide al modelo validar pago, turno y stock antes de confirmar la venta. */
    private void confirmarVenta() {
        if (!validarTurnoAbierto()) return;
        ventaActual.setMedioPago(cbMedioPago.getValue());
        ventaActual.setComprobantePago(txtNroComprobante.getText().trim());
        if (cbMedioPago.getValue() != null && cbMedioPago.getValue().contains("Efectivo")) {
            try {
                ventaActual.liquidarVuelto(Double.parseDouble(txtPagaCon.getText().trim()));
            } catch (NumberFormatException e) {
                mostrarAlerta("Importe inválido", "Ingrese el importe recibido en efectivo.");
                return;
            }
        }

        try {
            ventaActual.confirmarVenta();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta("No se pudo confirmar la venta", e.getMessage());
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("JRMARKET - Venta Registrada");
        alerta.setHeaderText("Ticket: " + ventaActual.getNroTicket());
        alerta.setContentText("Total cobrado: $ " + FormatoImporte.formatear(ventaActual.getTotal()) +
                              "\nMedio: " + ventaActual.getMedioPago() +
                              "\nEstado: " + ventaActual.getEstadoActual().getNombre());
        alerta.showAndWait();

        cancelarVentaCompleta();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /** Evita que un handler registre operaciones luego del cierre del turno. */
    private boolean validarTurnoAbierto() {
        if (refrescarEstadoCaja()) return true;
        mostrarAlerta("Caja cerrada", "Abra un turno de caja antes de registrar ventas.");
        return false;
    }
}