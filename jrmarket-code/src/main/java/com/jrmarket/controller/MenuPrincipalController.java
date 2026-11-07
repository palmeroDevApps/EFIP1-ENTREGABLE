package com.jrmarket.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.dao.JRMarketDAO;
import com.jrmarket.model.*;

/**
 * Controlador principal de la interfaz del menú del sistema JRMarket.
 */
public class MenuPrincipalController implements Initializable {

    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Label lblEstadoPie;
    @FXML private Button btnCerrarSesion;

    @FXML private Button btnModuloVentas;
    @FXML private Button btnModuloCierre;
    @FXML private Button btnModuloCatalogo;
    @FXML private Button btnModuloCompras;
    @FXML private Button btnModuloUsuarios;

    private Usuario usuarioActual;
    private List<TurnoCaja> turnosCaja = new ArrayList<>();
    private List<Producto> catalogoProductos = new ArrayList<>();
    private List<Proveedor> proveedores = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Rol> roles = new ArrayList<>();
    private final JRMarketDAO dao = new JRMarketDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatosDesdeBaseDeDatos();
    }

    private void cargarDatosDesdeBaseDeDatos() {
        catalogoProductos.clear();
        proveedores.clear();

        List<Producto> productosBD = dao.consultarProductosDisponibles();
        List<Proveedor> proveedoresBD = dao.consultarProveedoresDisponibles();

        if (!productosBD.isEmpty() && !proveedoresBD.isEmpty()) {
            catalogoProductos.addAll(productosBD);
            proveedores.addAll(proveedoresBD);
        } else {
            inicializarDatosSemilla();
        }
    }

    private void inicializarDatosSemilla() {
        Marca mMilka = new Marca(1, "Milka", "2026-01-01", null);
        Marca mCepita = new Marca(2, "Cepita", "2026-01-01", null);
        Marca mCoca = new Marca(3, "Coca-Cola", "2026-01-01", null);
        Marca mLays = new Marca(4, "Lays", "2026-01-01", null);
        Marca mBeldent = new Marca(5, "Beldent", "2026-01-01", null);
        Marca mArcor = new Marca(6, "Arcor", "2026-01-01", null);

        TipoProducto tpGolosinas = new TipoProducto(1, "Golosinas", "Dulces y chocolates", "2026-01-01", null);
        TipoProducto tpBebidas = new TipoProducto(2, "Bebidas", "Gaseosas y jugos", "2026-01-01", null);
        TipoProducto tpSnacks = new TipoProducto(3, "Snacks", "Salados", "2026-01-01", null);

        catalogoProductos.add(new Producto(1, "779001", "Alfajor Triple Oreo 61g", 1200.0, 700.0, 18, 5, true, "2026-01-01", null, mMilka, tpGolosinas));
        catalogoProductos.add(new Producto(2, "779009", "Jugo Naranja Fresh 500ml", 1300.0, 800.0, 24, 10, true, "2026-01-01", null, mCepita, tpBebidas));
        catalogoProductos.add(new Producto(3, "779015", "Gaseosa 500ml Original", 1800.0, 1100.0, 4, 12, true, "2026-01-01", null, mCoca, tpBebidas));
        catalogoProductos.add(new Producto(4, "779022", "Papas Fritas Clásicas 85g", 2100.0, 1300.0, 15, 5, true, "2026-01-01", null, mLays, tpSnacks));
        catalogoProductos.add(new Producto(5, "779035", "Chicles Menta Fuerte 10u", 600.0, 350.0, 8, 20, true, "2026-01-01", null, mBeldent, tpGolosinas));
        catalogoProductos.add(new Producto(6, "779040", "Turrón de Maní 25g", 450.0, 250.0, 60, 20, true, "2026-01-01", null, mArcor, tpGolosinas));

        proveedores.add(new Proveedor(1, "Mayorista BLOW MAX", "30-67891234-9", "Mendoza", "261-4455667", true));
        proveedores.add(new Proveedor(2, "TADICOR Mayorista", "30-71175642-1", "Las Heras", "261-4455889", true));
        proveedores.add(new Proveedor(3, "OSCAR DAVID Mayorista", "30-58912345-6", "Godoy Cruz", "261-4455990", true));
    }

    public void setUsuarioLogueado(Usuario usuario, List<Usuario> usuarios, List<Rol> roles) {
        this.usuarios = usuarios;
        this.roles = roles;

        if (usuario == null || !usuario.isEstado()) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de Seguridad");
            alerta.setHeaderText("Acceso no autorizado");
            alerta.setContentText("No se puede iniciar sesión con un usuario inactivo.");
            alerta.showAndWait();
            cerrarSesion();
            return;
        }

        this.usuarioActual = usuario;
        this.turnosCaja = new ArrayList<>(dao.consultarTurnosCaja(this.usuarios));

        lblUsuario.setText(usuario.getNombreCompleto());
        lblRol.setText(usuario.getRol().getNombre());
        lblEstadoPie.setText("Operador: " + usuario.getNombreCompleto() + " (" + usuario.getNombreUsuario() + ")");

        aplicarPermisosSegunRol();
    }

    private void aplicarPermisosSegunRol() {
        if (usuarioActual == null) {
            btnModuloVentas.setDisable(true);
            btnModuloCierre.setDisable(true);
            btnModuloCatalogo.setDisable(true);
            btnModuloCompras.setDisable(true);
            btnModuloUsuarios.setDisable(true);
            btnModuloVentas.setOpacity(0.45);
            btnModuloCierre.setOpacity(0.45);
            btnModuloCatalogo.setOpacity(0.45);
            btnModuloCompras.setOpacity(0.45);
            btnModuloUsuarios.setOpacity(0.45);
            return;
        }

        btnModuloVentas.setDisable(!usuarioActual.tienePermiso(Permiso.VENTAS));
        btnModuloCierre.setDisable(!usuarioActual.tienePermiso(Permiso.CIERRE_CAJA));
        btnModuloCatalogo.setDisable(!usuarioActual.tienePermiso(Permiso.CATALOGO));
        btnModuloCompras.setDisable(!usuarioActual.tienePermiso(Permiso.COMPRAS));

        boolean puedeGestionarUsuarios = usuarioActual.tienePermiso(Permiso.USUARIOS);
        btnModuloUsuarios.setDisable(!puedeGestionarUsuarios);

        btnModuloVentas.setOpacity(btnModuloVentas.isDisable() ? 0.45 : 1.0);
        btnModuloCierre.setOpacity(btnModuloCierre.isDisable() ? 0.45 : 1.0);
        btnModuloCatalogo.setOpacity(btnModuloCatalogo.isDisable() ? 0.45 : 1.0);
        btnModuloCompras.setOpacity(btnModuloCompras.isDisable() ? 0.45 : 1.0);
        btnModuloUsuarios.setOpacity(btnModuloUsuarios.isDisable() ? 0.45 : 1.0);
    }

    @FXML
    private void abrirVentas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/VentasView.fxml"));
            Parent root = loader.load();

            VentasController ctrl = loader.getController();
            ctrl.inicializarContexto(usuarioActual, turnosCaja, catalogoProductos);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Mostrador de Ventas");
            stage.setScene(new Scene(root));
            ctrl.observarEstadoTurno(stage);
            stage.show();
        } catch (Exception e) {
            mostrarError("No se pudo abrir el mostrador de ventas", e);
        }
    }

    @FXML
    private void abrirCierreCaja() {
        TurnoCaja turnoActivo = buscarTurnoAbierto();
        if (turnoActivo == null) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION,
                "No hay un turno abierto. Abra la caja desde el módulo de Ventas antes de realizar el cierre.");
            alerta.setTitle("Caja cerrada");
            alerta.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/CierreCajaView.fxml"));
            Parent root = loader.load();

            CierreTurnoController ctrl = loader.getController();
            ctrl.inicializarContexto(usuarioActual, turnoActivo);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Cierre de Turno y Arqueo");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarError("No se pudo abrir el cierre de caja", e);
        }
    }

    private TurnoCaja buscarTurnoAbierto() {
        for (int indice = turnosCaja.size() - 1; indice >= 0; indice--) {
            TurnoCaja turno = turnosCaja.get(indice);
            if ("ABIERTO".equalsIgnoreCase(turno.getEstado())) return turno;
        }
        return null;
    }

    @FXML
    private void abrirCatalogo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/ProductoView.fxml"));
            Parent root = loader.load();

            ProductoController ctrl = loader.getController();
            ctrl.setCatalogo(catalogoProductos);
            ctrl.setUsuarioActual(usuarioActual);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Catálogo General de Productos");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarError("No se pudo abrir el catálogo", e);
        }
    }

    @FXML
    private void abrirCompras() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/ComprasView.fxml"));
            Parent root = loader.load();

            ComprasController ctrl = loader.getController();
            ctrl.inicializarContexto(usuarioActual, catalogoProductos, proveedores);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Gestión de Compras y Reposición");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarError("No se pudo abrir compras", e);
        }
    }

    @FXML
    private void abrirUsuarios() {
        if (usuarioActual == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No hay una sesión activa para gestionar usuarios.");
            alerta.setTitle("Seguridad");
            alerta.setHeaderText(null);
            alerta.showAndWait();
            return;
        }

        boolean puedeGestionar = usuarioActual.tienePermiso(Permiso.USUARIOS);

        if (!puedeGestionar) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Acceso restringido: requiere permisos de Administrador.");
            alerta.setTitle("Seguridad");
            alerta.setHeaderText(null);
            alerta.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/UsuarioView.fxml"));
            Parent root = loader.load();

            UsuarioController ctrl = loader.getController();
            ctrl.inicializarContexto(usuarios, roles);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET - Gestión de Usuarios y Permisos");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            mostrarError("No se pudo abrir la gestión de usuarios", e);
        }
    }

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(e.getClass().getSimpleName() + ": " + e.getMessage());

        StringWriter textoError = new StringWriter();
        e.printStackTrace(new PrintWriter(textoError));
        TextArea detalles = new TextArea(textoError.toString());
        detalles.setEditable(false);
        detalles.setWrapText(false);
        detalles.setPrefRowCount(16);
        detalles.setPrefColumnCount(90);
        alert.getDialogPane().setExpandableContent(detalles);
        alert.showAndWait();
    }

    @FXML
    private void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("JRMARKET v1.0 - Control de Acceso");
            stage.setScene(new Scene(root, 480, 500));
            stage.show();

            Stage actual = (Stage) btnCerrarSesion.getScene().getWindow();
            actual.close();
        } catch (Exception e) {
            mostrarError("No se pudo cerrar la sesión", e);
        }
    }
}