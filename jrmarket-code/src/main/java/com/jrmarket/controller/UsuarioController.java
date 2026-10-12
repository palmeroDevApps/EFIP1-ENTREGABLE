package com.jrmarket.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.ResourceBundle;
import com.jrmarket.model.Rol;
import com.jrmarket.model.Usuario;

public class UsuarioController implements Initializable {
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private Button btnCambiarRol;
    @FXML private Button btnToggleEstado;

    private ObservableList<Usuario> listaUsuariosObservable;
    private List<Rol> rolesDisponibles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colRol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getRol() != null ? cell.getValue().getRol().getNombre() : "Sin Rol"
        ));
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().isEstado() ? "ACTIVO" : "INACTIVO"
        ));

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            boolean haySeleccion = (seleccionado != null);
            btnCambiarRol.setDisable(!haySeleccion);
            btnToggleEstado.setDisable(!haySeleccion);
        });
    }

    public void inicializarContexto(List<Usuario> usuarios, List<Rol> roles) {
        this.listaUsuariosObservable = FXCollections.observableList(usuarios);
        this.rolesDisponibles = roles;
        tblUsuarios.setItems(listaUsuariosObservable);
    }

    @FXML
    private void nuevoUsuario() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtUser = new TextField();
        PasswordField txtPass = new PasswordField();
        TextField txtNombre = new TextField();
        ComboBox<Rol> cbRol = new ComboBox<>(FXCollections.observableArrayList(rolesDisponibles));
        cbRol.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Nombre de Usuario:"), txtUser);
        grid.addRow(1, new Label("Contraseña:"), txtPass);
        grid.addRow(2, new Label("Nombre Completo:"), txtNombre);
        grid.addRow(3, new Label("Rol:"), cbRol);
        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String u = txtUser.getText().trim();
            String p = txtPass.getText().trim();
            String n = txtNombre.getText().trim();
            if (u.isEmpty() || p.isEmpty() || n.isEmpty() || cbRol.getValue() == null) {
                mostrarAlerta("Datos incompletos", "Todos los campos son obligatorios.");
                return;
            }
            try {
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(p.getBytes(StandardCharsets.UTF_8));
                String hashStr = HexFormat.of().formatHex(hash);
                int nuevoId = listaUsuariosObservable.stream().mapToInt(Usuario::getIdUsuario).max().orElse(0) + 1;
                Usuario nuevo = new Usuario(nuevoId, u, hashStr, n, true, cbRol.getValue());
                listaUsuariosObservable.add(nuevo);
                tblUsuarios.refresh();
            } catch (NoSuchAlgorithmException e) {
                mostrarAlerta("Error", "Error al procesar el cifrado de contraseña.");
            }
        }
    }

    @FXML
    private void cambiarRolUsuario() {
        Usuario sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        ChoiceDialog<Rol> dialog = new ChoiceDialog<>(sel.getRol(), rolesDisponibles);
        dialog.setTitle("Asignar Rol");
        dialog.setHeaderText("Modificar rol para: " + sel.getNombreCompleto());
        dialog.setContentText("Seleccione nuevo rol:");
        dialog.showAndWait().ifPresent(nuevoRol -> {
            sel.setRol(nuevoRol);
            tblUsuarios.refresh();
        });
    }

    @FXML
    private void alternarEstadoUsuario() {
        Usuario sel = tblUsuarios.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.cambiarEstado(!sel.isEstado());
            tblUsuarios.refresh();
        }
    }

    @FXML
    private void cerrarVentana() {
        ((Stage) tblUsuarios.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setContentText(msg); a.showAndWait();
    }
}