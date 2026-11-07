package com.jrmarket.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.jrmarket.dao.JRMarketDAO;
import com.jrmarket.model.*;

/**
 * Controlador de autenticación del sistema JRMarket.
 */
public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblMensaje;
    @FXML private Button btnIngresar;
    @FXML private Button btnSalir;

    private List<Usuario> usuariosRegistrados = new ArrayList<>();
    private List<Rol> rolesDisponibles = new ArrayList<>();
    private final JRMarketDAO dao = new JRMarketDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarEstructuraSeguridad();
        lblMensaje.setText("");
    }

    /**
     * Recupera Roles, Permisos, RolPermiso y Usuarios desde MySQL mediante JRMarketDAO.
     */
    private void cargarEstructuraSeguridad() {
        rolesDisponibles.clear();
        usuariosRegistrados.clear();

        List<Rol> rolesBD = dao.consultarRolesConPermisos();
        if (rolesBD != null && !rolesBD.isEmpty()) {
            rolesDisponibles.addAll(rolesBD);
            List<Usuario> usuariosBD = dao.consultarUsuarios(rolesDisponibles);
            if (usuariosBD != null && !usuariosBD.isEmpty()) {
                usuariosRegistrados.addAll(usuariosBD);
                return;
            }
        }

        // Respaldo en caso de que el motor MySQL esté apagado
        /*Permiso ventas = new Permiso(1, Permiso.VENTAS, "Acceso al mostrador de ventas");
        Permiso cierreCaja = new Permiso(2, Permiso.CIERRE_CAJA, "Conciliación y arqueo de turno");
        Permiso catalogo = new Permiso(3, Permiso.CATALOGO, "Consulta del catálogo de productos");
        Permiso compras = new Permiso(4, Permiso.COMPRAS, "Registro de compras y reposición");
        Permiso gestionProductos = new Permiso(5, Permiso.GESTION_PRODUCTOS, "Alta, baja y modificación de productos");
        Permiso usuarios = new Permiso(6, Permiso.USUARIOS, "Administración de usuarios y permisos");

        Rol admin = new Rol(1, "ADMINISTRADOR", "Control total", "2026-01-01", null);
        Rol vendedor = new Rol(2, "VENDEDOR", "Atención mostrador", "2026-01-01", null);
        Rol repositor = new Rol(3, "REPOSITOR", "Abastecimiento", "2026-01-01", null);*/

        /*admin.getRolPermisos().add(new RolPermiso(1, "2026-01-01", admin, ventas));
        admin.getRolPermisos().add(new RolPermiso(2, "2026-01-01", admin, cierreCaja));
        admin.getRolPermisos().add(new RolPermiso(3, "2026-01-01", admin, catalogo));
        admin.getRolPermisos().add(new RolPermiso(4, "2026-01-01", admin, compras));
        admin.getRolPermisos().add(new RolPermiso(5, "2026-01-01", admin, gestionProductos));
        admin.getRolPermisos().add(new RolPermiso(6, "2026-01-01", admin, usuarios));

        vendedor.getRolPermisos().add(new RolPermiso(7, "2026-01-01", vendedor, ventas));
        vendedor.getRolPermisos().add(new RolPermiso(8, "2026-01-01", vendedor, cierreCaja));
        repositor.getRolPermisos().add(new RolPermiso(9, "2026-01-01", repositor, compras));

        rolesDisponibles.add(admin);
        rolesDisponibles.add(vendedor);
        rolesDisponibles.add(repositor);

        String hashClaveDemo = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";
        usuariosRegistrados.add(new Usuario(1, "cristian", hashClaveDemo, "Cristian Palmero", true, admin));
        usuariosRegistrados.add(new Usuario(2, "sofia", hashClaveDemo, "Sofía Martinez", true, vendedor));
        usuariosRegistrados.add(new Usuario(3, "lucas", hashClaveDemo, "Lucas Gómez", true, repositor));
        usuariosRegistrados.add(new Usuario(4, "estefania", hashClaveDemo, "Estefania Pacheco", false, vendedor));*/
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

        Usuario usuarioEncontrado = null;
        Iterator<Usuario> it = usuariosRegistrados.iterator();
        while (it.hasNext()) {
            Usuario u = it.next();
            if (u.getNombreUsuario().equalsIgnoreCase(user)) {
                usuarioEncontrado = u;
                break;
            }
        }

        if (usuarioEncontrado == null) {
            lblMensaje.setText("El nombre de usuario no existe en el sistema.");
            txtClave.clear();
            txtUsuario.requestFocus();
            return;
        }

        if (!usuarioEncontrado.isEstado()) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Acceso Denegado");
            alerta.setHeaderText("Cuenta de Usuario Inactiva");
            alerta.setContentText("El usuario '" + usuarioEncontrado.getNombreUsuario()
                + "' (" + usuarioEncontrado.getNombreCompleto()
                + ") se encuentra inhabilitado para operar.\n\nComuníquese con la administración de JRMARKET.");
            alerta.showAndWait();

            lblMensaje.setText("Acceso bloqueado: Usuario inactivo.");
            txtClave.clear();
            txtUsuario.requestFocus();
            return;
        }

        if (usuarioEncontrado.autenticar(pass)) {
            abrirMenuPrincipal(usuarioEncontrado);
        } else {
            lblMensaje.setText("Contraseña incorrecta.");
            txtClave.clear();
            txtClave.requestFocus();
        }
    }

    private void abrirMenuPrincipal(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/jrmarket/view/MenuPrincipalView.fxml"));
            Parent root = loader.load();

            MenuPrincipalController controller = loader.getController();
            controller.setUsuarioLogueado(usuario, usuariosRegistrados, rolesDisponibles);

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