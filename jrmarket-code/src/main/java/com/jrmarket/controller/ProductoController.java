package com.jrmarket.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.model.Marca;
import com.jrmarket.model.Producto;
import com.jrmarket.model.TipoProducto;
import com.jrmarket.util.FormatoImporte;

public class ProductoController implements Initializable {

    @FXML private Label lblTituloVentana;
    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> cbFiltroRubro;
    @FXML private Label lblTotalRegistros;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, String> colDescripcion;
    @FXML private TableColumn<Producto, String> colRubro;
    @FXML private TableColumn<Producto, Double> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStockActual;
    @FXML private TableColumn<Producto, Integer> colStockMinimo;

    @FXML private HBox boxAccionesABML;
    @FXML private Button btnNuevo;
    @FXML private Button btnModificar;
    @FXML private Button btnBaja;

    @FXML private HBox boxSeleccionVenta;
    @FXML private HBox boxCostoCompra;
    @FXML private Spinner<Integer> spnCantidadModal;
    @FXML private TextField txtCostoUnitarioModal;
    @FXML private Button btnAgregarVenta;
    @FXML private Button btnCerrar;

    private ObservableList<Producto> listaProductos;
    private FilteredList<Producto> listaFiltrada;
    private Producto productoSeleccionado;
    private int cantidadSeleccionada = 1;
    private double costoUnitarioSeleccionado;
    private boolean itemConfirmado = false;
    private boolean modoSeleccionCompra;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spnCantidadModal.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        cbFiltroRubro.setItems(FXCollections.observableArrayList("Todos", "Golosinas", "Bebidas", "Snacks"));
        cbFiltroRubro.getSelectionModel().selectFirst();

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colMarca.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMarca().getNombre()));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colRubro.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipoProducto().getNombre()));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecioVenta.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double importe, boolean empty) {
                super.updateItem(importe, empty);
                setText(empty || importe == null ? null : FormatoImporte.formatear(importe));
            }
        });
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, ant, selec) -> {
            boolean hay = (selec != null);
            btnAgregarVenta.setDisable(!hay);
            btnModificar.setDisable(!hay);
            btnBaja.setDisable(!hay);
            if (selec != null && modoSeleccionCompra) {
                txtCostoUnitarioModal.setText(Double.toString(selec.getPrecioCosto()));
            }
        });

        setModoSeleccionVenta(false);
    }

    public void setCatalogo(List<Producto> catalogo) {
        this.listaProductos = FXCollections.observableList(catalogo);
        this.listaFiltrada = new FilteredList<>(listaProductos, p -> true);
        this.listaFiltrada.setPredicate(Producto::isEstado);
        tblProductos.setItems(listaFiltrada);
        actualizarContador();
    }

    public void setBusquedaInicial(String busqueda) {
        txtBusqueda.setText(busqueda == null ? "" : busqueda);
        filtrarProductos();
        txtBusqueda.requestFocus();
        txtBusqueda.positionCaret(txtBusqueda.getText().length());
    }

    public void setModoSeleccionVenta(boolean modoVentaActivo) {
        configurarModoSeleccion(modoVentaActivo, "Mostrador");
    }

    public void setModoSeleccionCompra(boolean modoCompraActivo) {
        modoSeleccionCompra = modoCompraActivo;
        configurarModoSeleccion(modoCompraActivo, "Compras");
    }

    private void configurarModoSeleccion(boolean modoSeleccionActivo, String origen) {
        boxSeleccionVenta.setVisible(modoSeleccionActivo);
        boxSeleccionVenta.setManaged(modoSeleccionActivo);
        boxCostoCompra.setVisible(modoSeleccionActivo && modoSeleccionCompra);
        boxCostoCompra.setManaged(modoSeleccionActivo && modoSeleccionCompra);
        boxAccionesABML.setVisible(!modoSeleccionActivo);
        boxAccionesABML.setManaged(!modoSeleccionActivo);
        btnAgregarVenta.setText("Agregar a " + origen);
        lblTituloVentana.setText(modoSeleccionActivo
            ? "JRMARKET v1.0 | Selector para " + origen
            : "JRMARKET v1.0 | Catálogo General (ABML)");
    }

    @FXML
    private void filtrarProductos() {
        if (listaFiltrada == null) return;
        String texto = txtBusqueda.getText().trim().toLowerCase();
        String rubro = cbFiltroRubro.getValue();

        listaFiltrada.setPredicate(p -> {
            if (!p.isEstado()) return false;
            boolean matchRubro = rubro == null || rubro.equals("Todos") || p.getTipoProducto().getNombre().equalsIgnoreCase(rubro);
            if (!matchRubro) return false;
            if (texto.isEmpty()) return true;

            return p.getCodigoBarras().toLowerCase().contains(texto) ||
                   p.getDescripcion().toLowerCase().contains(texto) ||
                   p.getMarca().getNombre().toLowerCase().contains(texto);
        });
        actualizarContador();
    }

    private void actualizarContador() {
        lblTotalRegistros.setText("Total: " + (listaFiltrada != null ? listaFiltrada.size() : 0) + " artículos");
    }

    @FXML
    private void nuevoProducto() {
        Producto nuevo = mostrarFormularioProducto(null);
        if (nuevo != null) {
            listaProductos.add(nuevo);
            filtrarProductos();
        }
    }

    @FXML
    private void modificarProducto() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado != null && mostrarFormularioProducto(seleccionado) != null) {
            filtrarProductos();
            tblProductos.refresh();
        }
    }

    private Producto mostrarFormularioProducto(Producto original) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(original == null ? "Nuevo producto" : "Modificar producto");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.setResultConverter(boton -> boton);

        TextField codigo = new TextField(original == null ? "" : original.getCodigoBarras());
        TextField descripcion = new TextField(original == null ? "" : original.getDescripcion());
        TextField precioVenta = new TextField(original == null ? "" : Double.toString(original.getPrecioVenta()));
        TextField precioCosto = new TextField(original == null ? "" : Double.toString(original.getPrecioCosto()));
        TextField stock = new TextField(original == null ? "0" : Integer.toString(original.getStockActual()));
        TextField stockMinimo = new TextField(original == null ? "0" : Integer.toString(original.getStockMinimo()));

        List<Marca> marcas = new ArrayList<>();
        List<TipoProducto> tipos = new ArrayList<>();
        for (Producto producto : listaProductos) {
            if (producto.getMarca() != null && !marcas.contains(producto.getMarca())) marcas.add(producto.getMarca());
            if (producto.getTipoProducto() != null && !tipos.contains(producto.getTipoProducto())) tipos.add(producto.getTipoProducto());
        }
        ComboBox<Marca> marca = new ComboBox<>(FXCollections.observableArrayList(marcas));
        ComboBox<TipoProducto> tipo = new ComboBox<>(FXCollections.observableArrayList(tipos));
        if (original != null) {
            marca.getSelectionModel().select(original.getMarca());
            tipo.getSelectionModel().select(original.getTipoProducto());
        } else {
            marca.getSelectionModel().selectFirst();
            tipo.getSelectionModel().selectFirst();
        }

        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(8);
        campos.addRow(0, new Label("Código de barras"), codigo);
        campos.addRow(1, new Label("Descripción"), descripcion);
        campos.addRow(2, new Label("Marca"), marca);
        campos.addRow(3, new Label("Tipo de producto"), tipo);
        campos.addRow(4, new Label("Precio de venta"), precioVenta);
        campos.addRow(5, new Label("Precio de costo"), precioCosto);
        campos.addRow(6, new Label("Stock actual"), stock);
        campos.addRow(7, new Label("Stock mínimo"), stockMinimo);
        dialogo.getDialogPane().setContent(campos);

        if (!dialogo.showAndWait().orElse(ButtonType.CANCEL).equals(ButtonType.OK)) return null;

        try {
            String codigoIngresado = codigo.getText().trim();
            String descripcionIngresada = descripcion.getText().trim();
            double ventaIngresada = Double.parseDouble(precioVenta.getText().trim());
            double costoIngresado = Double.parseDouble(precioCosto.getText().trim());
            int stockIngresado = Integer.parseInt(stock.getText().trim());
            int minimoIngresado = Integer.parseInt(stockMinimo.getText().trim());

            if (codigoIngresado.isEmpty() || descripcionIngresada.isEmpty() || marca.getValue() == null
                    || tipo.getValue() == null || !Double.isFinite(ventaIngresada) || ventaIngresada <= 0.0
                    || !Double.isFinite(costoIngresado) || costoIngresado < 0.0
                    || stockIngresado < 0 || minimoIngresado < 0) {
                throw new IllegalArgumentException("Complete los campos y use valores válidos.");
            }
            for (Producto producto : listaProductos) {
                if (producto != original && producto.getCodigoBarras().equalsIgnoreCase(codigoIngresado)) {
                    throw new IllegalArgumentException("El código de barras ya está registrado.");
                }
            }

            if (original == null) {
                int id = listaProductos.stream().mapToInt(Producto::getIdProducto).max().orElse(0) + 1;
                return new Producto(id, codigoIngresado, descripcionIngresada, ventaIngresada, costoIngresado,
                    stockIngresado, minimoIngresado, true, LocalDate.now().toString(), null,
                    marca.getValue(), tipo.getValue());
            }

            original.setCodigoBarras(codigoIngresado);
            original.setDescripcion(descripcionIngresada);
            original.setPrecioVenta(ventaIngresada);
            original.setPrecioCosto(costoIngresado);
            original.setStockActual(stockIngresado);
            original.setStockMinimo(minimoIngresado);
            original.setMarca(marca.getValue());
            original.setTipoProducto(tipo.getValue());
            return original;
        } catch (IllegalArgumentException e) {
            mostrarAlerta("Datos inválidos", e.getMessage());
            return null;
        }
    }

    @FXML
    private void darDeBajaProducto() {
        Producto sel = tblProductos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro de que desea dar de baja el producto seleccionado?",
                ButtonType.OK, ButtonType.CANCEL);
            if (confirmacion.showAndWait().orElse(ButtonType.CANCEL).equals(ButtonType.OK)) {
                sel.setEstado(false);
                sel.setFechaHasta(LocalDate.now().toString());
                filtrarProductos();
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void agregarYVolver() {
        Producto sel = tblProductos.getSelectionModel().getSelectedItem();
        if (sel != null) {
            if (modoSeleccionCompra) {
                try {
                    costoUnitarioSeleccionado = Double.parseDouble(txtCostoUnitarioModal.getText().trim());
                    if (!Double.isFinite(costoUnitarioSeleccionado) || costoUnitarioSeleccionado < 0.0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Costo inválido", "Ingrese un costo unitario válido para el producto.");
                    txtCostoUnitarioModal.requestFocus();
                    return;
                }
            }
            this.productoSeleccionado = sel;
            this.cantidadSeleccionada = spnCantidadModal.getValue();
            this.itemConfirmado = true;
            cerrarVentana();
        }
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    public Producto getProductoSeleccionado() { return productoSeleccionado; }
    public int getCantidadSeleccionada() { return cantidadSeleccionada; }
    public double getCostoUnitarioSeleccionado() { return costoUnitarioSeleccionado; }
    public boolean isItemConfirmado() { return itemConfirmado; }
}