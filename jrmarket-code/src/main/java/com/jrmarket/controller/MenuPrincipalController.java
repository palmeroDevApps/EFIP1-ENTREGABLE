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

import com.jrmarket.model.*;

/**
 * Controlador principal de la interfaz del menú del sistema JRMarket.
 *
 * <p>Su responsabilidad es coordinar la navegación entre los módulos funcionales,
 * mantener el estado de sesión compartido durante la ejecución de la aplicación y
 * aplicar restricciones de acceso según el rol del usuario autenticado.</p>
 *
 * <p>Los datos cargados en memoria se reutilizan entre pantallas para reflejar un
 * mismo contexto transaccional sin necesidad de persistir la información en cada
 * ventana abierta.</p>
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

    /**
     * Estado de sesión activo para la ejecución actual de la aplicación.
     *
     * <p>Estas colecciones representan el contexto compartido entre módulos y se
     * reinician al cerrar la sesión del usuario.</p>
     */
    private Usuario usuarioActual;
    private List<TurnoCaja> turnosCaja = new ArrayList<>();
    private List<Producto> catalogoProductos = new ArrayList<>();
    private List<Proveedor> proveedores = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Rol> roles = new ArrayList<>();

    /**
     * Inicializa los datos operativos propios del menú.
     *
     * @param location Ubicación del recurso FXML asociado al controlador.
     * @param resources Recursos de internacionalización y configuración del entorno JavaFX.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inicializarDatosSemilla();
    }

    /**
     * Carga el catálogo y los proveedores iniciales para simular un entorno operativo mínimo.
     *
     * <p>Los usuarios, roles y permisos se inicializan en el login y se comparten con
     * este controlador para que las pantallas trabajen con las mismas instancias.</p>
     */
    private void inicializarDatosSemilla() {
        // 1. Definición de marcas y categorías del catálogo de productos.
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

    /**
     * Asigna el usuario autenticado a la sesión actual y actualiza el estado visual del menú.
     *
     * @param usuario Instancia del operador activo en la sesión actual.
     * @param usuarios Usuarios compartidos con los módulos de administración.
     * @param roles Roles y permisos compartidos con los módulos de administración.
     */
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
        this.turnosCaja = new ArrayList<>();

        lblUsuario.setText(usuario.getNombreCompleto());
        lblRol.setText(usuario.getRol().getNombre());
        lblEstadoPie.setText("Operador: " + usuario.getNombreCompleto() + " (" + usuario.getNombreUsuario() + ")");

        aplicarPermisosSegunRol();
    }

    /**
     * Habilita únicamente los módulos autorizados según el rol del usuario actual.
     *
     * <p>La visualización de los botones se reduce a un estado visible con opacidad
     * menor cuando no cuentan con permisos válidos.</p>
     */
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
    /**
     * Abre el módulo de ventas y comparte el contexto operativo del usuario actual.
     *
     * <p>Se inyectan los turnos, productos y datos de sesión para que la vista de ventas
     * trabaje con la misma información que el menú principal.</p>
     */
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
    /**
     * Abre la pantalla de cierre de caja solo cuando existe un turno activo.
     *
     * <p>Si no hay una caja abierta, se informa al operador para evitar errores de flujo
     * de negocio en el proceso de arqueo.</p>
     */
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

    /**
     * Busca el turno vigente más reciente dentro del contexto de sesión actual.
     *
     * @return El turno activo en curso, o null si no existe un turno abierto.
     */
    private TurnoCaja buscarTurnoAbierto() {
        for (int indice = turnosCaja.size() - 1; indice >= 0; indice--) {
            TurnoCaja turno = turnosCaja.get(indice);
            if ("ABIERTO".equalsIgnoreCase(turno.getEstado())) return turno;
        }
        return null;
    }

    @FXML
    /**
     * Abre la vista del catálogo de Productos general reutilizando el mismo conjunto de productos.
     */
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
    /**
     * Abre el módulo de compras con el catálogo y los proveedores que pertenecen a la sesión actual.
     */
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
    /**
     * Abre la administración de usuarios validando los permisos necesarios del operador.
     */
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

    /**
     * Presenta un cuadro de error con detalle técnico para facilitar la depuración.
     *
     * @param titulo Mensaje breve del problema detectado para el usuario final.
     * @param e Excepción original que produjo el error.
     */
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
    /**
     * Cierra la sesión del usuario y regresa a la pantalla de autenticación.
     *
     * <p>Se abre la vista de Login y se finaliza la ventana principal actual para
     * evitar que la sesión siga activa en memoria.</p>
     */
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