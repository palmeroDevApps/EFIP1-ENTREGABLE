package com.jrmarket.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.model.*;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblMensaje;
    @FXML private Button btnIngresar;
    @FXML private Button btnSalir;

    private List<Usuario> usuariosRegistrados = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarEstructuraSeguridad();
        lblMensaje.setText("");
    }

    private void cargarEstructuraSeguridad() {
        // Permisos del diagrama
        Permiso pVentas = new Permiso(1, "VENTAS", "Acceso al mostrador de ventas");
        Permiso pCierre = new Permiso(2, "CIERRE_CAJA", "Conciliación y arqueo de turno");
        Permiso pCatalogo = new Permiso(3, "CATALOGO", "Consulta de productos");
        Permiso pCompras = new Permiso(4, "COMPRAS", "Registro de compras y reposición");

        // Roles del diagrama
        Rol rolAdmin = new Rol(1, "ADMINISTRADOR", "Control total", "2026-01-01", null);
        Rol rolVendedor = new Rol(2, "VENDEDOR", "Atención mostrador", "2026-01-01", null);
        Rol rolRepositor = new Rol(3, "REPOSITOR", "Abastecimiento", "2026-01-01", null);

        // Relaciones RolPermiso
        rolAdmin.getRolPermisos().add(new RolPermiso(1, "2026-01-01", rolAdmin, pVentas));
        rolAdmin.getRolPermisos().add(new RolPermiso(2, "2026-01-01", rolAdmin, pCierre));
        rolAdmin.getRolPermisos().add(new RolPermiso(3, "2026-01-01", rolAdmin, pCatalogo));
        rolAdmin.getRolPermisos().add(new RolPermiso(4, "2026-01-01", rolAdmin, pCompras));

        rolVendedor.getRolPermisos().add(new RolPermiso(5, "2026-01-01", rolVendedor, pVentas));
        rolVendedor.getRolPermisos().add(new RolPermiso(6, "2026-01-01", rolVendedor, pCierre));
        rolVendedor.getRolPermisos().add(new RolPermiso(7, "2026-01-01", rolVendedor, pCatalogo));

        rolRepositor.getRolPermisos().add(new RolPermiso(8, "2026-01-01", rolRepositor, pCatalogo));
        rolRepositor.getRolPermisos().add(new RolPermiso(9, "2026-01-01", rolRepositor, pCompras));

        // Usuarios
        String hashClaveDemo = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";
        usuariosRegistrados.add(new Usuario(1, "cristian", hashClaveDemo, "Cristian Palmero", true, rolAdmin));
        usuariosRegistrados.add(new Usuario(2, "sofia", hashClaveDemo, "Sofía Martinez", true, rolVendedor));
        usuariosRegistrados.add(new Usuario(3, "lucas", hashClaveDemo, "Lucas Gómez", true, rolRepositor));
    }

    @FXML
    private void enfocarClave() {
        txtClave.requestFocus();
    }

    @FXML
    private void iniciarSesion() {
        String user = txtUsuario.getText().trim();
        String pass = txtClave.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Por favor complete su usuario y contraseña.");
            return;
        }

        Usuario usuarioAutenticado = null;
        for (Usuario u : usuariosRegistrados) {
            if (u.getNombreUsuario().equalsIgnoreCase(user) && u.autenticar(pass)) {
                usuarioAutenticado = u;
                break;
            }
        }

        if (usuarioAutenticado != null) {
            abrirMenuPrincipal(usuarioAutenticado);
        } else {
            lblMensaje.setText("Credenciales incorrectas o usuario inactivo.");
            txtClave.clear();
            txtClave.requestFocus();
        }
    }

    private void abrirMenuPrincipal(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/MenuPrincipalView.fxml"));
            Parent root = loader.load();

            // Inyección directa del modelo Usuario al controlador receptor
            MenuPrincipalController controller = loader.getController();
            controller.setUsuarioLogueado(usuario);

            Stage stage = new Stage();
            stage.setTitle("JRMARKET v1.0 - Panel de Control Principal");
            stage.setScene(new Scene(root, 960, 650));
            stage.show();

            Stage loginStage = (Stage) btnIngresar.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            lblMensaje.setText("Error al abrir pantalla principal: " + e.getMessage());
        }
    }

    @FXML
    private void salir() {
        System.exit(0);
    }
}