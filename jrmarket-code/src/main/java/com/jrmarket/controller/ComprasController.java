package com.jrmarket.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.print.PrinterJob;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.model.*;
import com.jrmarket.util.FormatoImporte;

/** Controla las listas sugeridas y el registro de compras a mayoristas. */
public class ComprasController implements Initializable {

    @FXML private Label lblOperador;
    @FXML private TabPane tabPaneCompras;

    // Pestaña Faltantes
    @FXML private Label lblResumenFaltantes;
    @FXML private TableView<Producto> tblFaltantes;
    @FXML private TableColumn<Producto, String> colFaltanteCodigo;
    @FXML private TableColumn<Producto, String> colFaltanteMarca;
    @FXML private TableColumn<Producto, String> colFaltanteDescripcion;
    @FXML private TableColumn<Producto, String> colFaltanteRubro;
    @FXML private TableColumn<Producto, Integer> colFaltanteStockActual;
    @FXML private TableColumn<Producto, Integer> colFaltanteStockMinimo;
    @FXML private TableColumn<Producto, Integer> colFaltanteSugerido;

    // Pestaña Compras
    @FXML private ComboBox<Proveedor> cbProveedor;
    @FXML private TextField txtNroComprobante;
    @FXML private TextField txtCodigoBarras;
    @FXML private TableView<DetalleCompra> tblDetalleCompra;
    @FXML private TableColumn<DetalleCompra, String> colCodigo;
    @FXML private TableColumn<DetalleCompra, String> colMarca;
    @FXML private TableColumn<DetalleCompra, String> colDescripcion;
    @FXML private TableColumn<DetalleCompra, Integer> colCantidad;
    @FXML private TableColumn<DetalleCompra, Double> colCostoUnitario;
    @FXML private TableColumn<DetalleCompra, Double> colSubtotal;
    @FXML private Label lblTotalCompra;
    @FXML private Button btnRegistrarCompra;

    private Usuario operador;
    private List<Producto> catalogoCompartido;
    private List<Proveedor> proveedoresCompartidos;

    private Compra compraActual;
    private ObservableList<DetalleCompra> itemsCompraObservable = FXCollections.observableArrayList();
    private ObservableList<Producto> listaFaltantesObservable = FXCollections.observableArrayList();

    /** Configura ambas tablas y permite corregir costos antes de confirmar la compra. */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Enlace a atributos de modelos
        colFaltanteCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colFaltanteMarca.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMarca().getNombre()));
        colFaltanteDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colFaltanteRubro.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipoProducto().getNombre()));
        colFaltanteStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colFaltanteStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colFaltanteSugerido.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(
            cell.getValue().getStockMinimo() * 2 - cell.getValue().getStockActual()
        ).asObject());
        tblFaltantes.setItems(listaFaltantesObservable);

        colCodigo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getCodigoBarras()));
        colMarca.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getMarca().getNombre()));
        colDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProducto().getDescripcion()));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colCostoUnitario.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double importe, boolean empty) {
                super.updateItem(importe, empty);
                setText(empty || importe == null ? null : FormatoImporte.formatear(importe));
            }
        });
        tblDetalleCompra.setEditable(true);
        colCostoUnitario.setEditable(true);
        colCostoUnitario.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {
            @Override
            public String toString(Double importe) {
                return importe == null ? "" : FormatoImporte.formatear(importe);
            }

            @Override
            public Double fromString(String texto) {
                try {
                    return FormatoImporte.parsear(texto);
                } catch (IllegalArgumentException e) {
                    return Double.NaN;
                }
            }
        }));
        colCostoUnitario.setOnEditCommit(event -> {
            try {
                compraActual.actualizarCostoUnitario(event.getRowValue(), event.getNewValue());
                itemsCompraObservable.setAll(compraActual.getDetalles());
                actualizarTotalUI();
            } catch (IllegalArgumentException e) {
                mostrarAlerta("Costo inválido", e.getMessage());
                tblDetalleCompra.refresh();
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
        tblDetalleCompra.setItems(itemsCompraObservable);
    }

    /** Recibe del menú los datos en memoria usados durante la sesión. */
    public void inicializarContexto(Usuario usuario, List<Producto> catalogo, List<Proveedor> proveedores) {
        this.operador = usuario;
        this.catalogoCompartido = catalogo;
        this.proveedoresCompartidos = proveedores;

        lblOperador.setText(usuario.getNombreCompleto());
        cbProveedor.setItems(FXCollections.observableArrayList(proveedores));
        cbProveedor.getSelectionModel().selectFirst();

        iniciarNuevaCompra();
    }

    /** Prepara una cabecera y un detalle vacíos para el siguiente comprobante. */
    private void iniciarNuevaCompra() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.compraActual = new Compra(1, "", fecha, operador, cbProveedor.getValue());
        itemsCompraObservable.clear();
        actualizarTotalUI();
    }

    @FXML
    /** Filtra el catálogo según el mínimo configurado para cada producto. */
    private void generarListaSugeridos() {
        listaFaltantesObservable.clear();
        if (catalogoCompartido == null) return;

        for (Producto p : catalogoCompartido) {
            if (p.esStockCritico()) { // Método de Producto
                listaFaltantesObservable.add(p);
            }
        }
        lblResumenFaltantes.setText(listaFaltantesObservable.size() + " artículos identificados para compra");
    }

    @FXML
    /** Abre el selector de productos con el texto de búsqueda ingresado. */
    private void agregarPorCodigo() {
        abrirExploradorProductos(txtCodigoBarras.getText().trim());
    }

    /** Refleja el total calculado y habilita confirmar cuando hay renglones. */
    private void actualizarTotalUI() {
        double total = compraActual.calcularTotal();
        lblTotalCompra.setText("$ " + FormatoImporte.formatear(total));
        btnRegistrarCompra.setDisable(compraActual.getDetalles().isEmpty());
    }

    @FXML
    /** Valida el comprobante y aplica stock y costo solo al confirmar la compra. */
    private void confirmarIngresoMercaderia() {
        String nro = txtNroComprobante.getText().trim();
        if (nro.isEmpty()) {
            mostrarAlerta("Comprobante requerido", "Ingrese el número de ticket o factura antes de confirmar la compra.");
            txtNroComprobante.requestFocus();
            return;
        }
        if (cbProveedor.getValue() == null) {
            mostrarAlerta("Mayorista requerido", "Seleccione el mayorista antes de confirmar la compra.");
            cbProveedor.requestFocus();
            return;
        }

        compraActual.setNroComprobante(nro);
        compraActual.setProveedor(cbProveedor.getValue());

        try {
            compraActual.confirmarIngresoMercaderia();
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta("No se pudo confirmar la compra", e.getMessage());
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("JRMARKET - Stock Actualizado");
        alerta.setHeaderText("Factura registrada: " + compraActual.getNroComprobante());
        alerta.setContentText("Total abonado: $ " + FormatoImporte.formatear(compraActual.getTotal()) + "\nMercadería ingresada al inventario.");
        alerta.showAndWait();

        iniciarNuevaCompra();
        txtNroComprobante.clear();
    }

    @FXML
    /** Quita el renglón seleccionado y vuelve a calcular el total. */
    private void quitarItemSeleccionado() {
        DetalleCompra sel = tblDetalleCompra.getSelectionModel().getSelectedItem();
        if (sel != null) {
            compraActual.quitarDetalle(sel);
            itemsCompraObservable.setAll(compraActual.getDetalles());
            actualizarTotalUI();
        }
    }

    @FXML private void cancelarCompraCompleta() { iniciarNuevaCompra(); }
    @FXML private void limpiarListaFaltantes() { listaFaltantesObservable.clear(); }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    /** Genera una salida impresa con las cantidades sugeridas de reposición. */
    private void imprimirListaFaltantes() {
        if (listaFaltantesObservable.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Lista de compras vacía");
            alerta.setHeaderText(null);
            alerta.setContentText("Cargue los productos sugeridos antes de imprimir la lista.");
            alerta.showAndWait();
            return;
        }

        PrinterJob trabajo = PrinterJob.createPrinterJob();
        if (trabajo == null) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Impresión no disponible");
            alerta.setHeaderText(null);
            alerta.setContentText("No se encontró una impresora disponible en el sistema.");
            alerta.showAndWait();
            return;
        }

        if (!trabajo.showPrintDialog(tblFaltantes.getScene().getWindow())) {
            return;
        }

        StringBuilder contenido = new StringBuilder();
        contenido.append("JRMarket - Lista sugerida de compras\n");
        contenido.append("Fecha: ")
            .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
            .append("\nArtículos: ").append(listaFaltantesObservable.size()).append("\n\n");

        for (Producto producto : listaFaltantesObservable) {
            int sugerido = producto.getStockMinimo() * 2 - producto.getStockActual();
            contenido.append(producto.getDescripcion()).append("\n")
                .append("Código: ").append(producto.getCodigoBarras())
                .append(" | Marca: ").append(producto.getMarca().getNombre())
                .append(" | Rubro: ").append(producto.getTipoProducto().getNombre()).append("\n")
                .append("Stock actual: ").append(producto.getStockActual())
                .append(" | Stock mínimo: ").append(producto.getStockMinimo())
                .append(" | Cantidad sugerida: ").append(sugerido).append("\n\n");
        }

        Text texto = new Text(contenido.toString());
        texto.setWrappingWidth(trabajo.getJobSettings().getPageLayout().getPrintableWidth());
        VBox pagina = new VBox(texto);
        pagina.setPadding(new Insets(24));

        if (trabajo.printPage(pagina)) {
            trabajo.endJob();
        } else {
            trabajo.cancelJob();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de impresión");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudo enviar la lista a la impresora.");
            alerta.showAndWait();
        }
    }
    @FXML
    private void abrirExploradorProductos() {
        abrirExploradorProductos("");
    }

    /** Muestra el selector modal en modo compra y devuelve costo y cantidad elegidos. */
    private void abrirExploradorProductos(String filtro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/ProductoView.fxml"));
            Parent root = loader.load();

            ProductoController controller = loader.getController();
            controller.setCatalogo(catalogoCompartido);
            controller.setModoSeleccionCompra(true);
            controller.setBusquedaInicial(filtro);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Selector de Productos para Compra");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            if (controller.isItemConfirmado() && controller.getProductoSeleccionado() != null) {
                agregarProductoSeleccionado(
                    controller.getProductoSeleccionado(), controller.getCantidadSeleccionada(),
                    controller.getCostoUnitarioSeleccionado());
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo abrir el catálogo: " + e.getMessage());
        }
    }

    /** Agrega al comprobante los datos confirmados en el selector de productos. */
    private void agregarProductoSeleccionado(Producto producto, int cantidad, double costo) {
        try {
            compraActual.agregarDetalle(producto, cantidad, costo);
            itemsCompraObservable.setAll(compraActual.getDetalles());
            actualizarTotalUI();
            txtCodigoBarras.clear();
        } catch (IllegalArgumentException e) {
            mostrarAlerta("No se pudo agregar el producto", e.getMessage());
        }
    }
}