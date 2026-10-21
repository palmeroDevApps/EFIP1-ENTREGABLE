package com.jrmarket.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * Gestiona la conexión con la base de datos MySQL mediante la biblioteca estándar JDBC.
 */
public class ConexionBD {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/jrmarket?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de MySQL: " + e.getMessage(), e);
        }
        Connection conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
        verificarAdvertencias(conexion);
        System.out.println("[JDBC OK] Conectado a MySQL en 127.0.0.1:3306/jrmarket");
        return conexion;
    }

    public static boolean esConexionValida(Connection conexion) {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void verificarAdvertencias(Connection conexion) {
        try {
            SQLWarning warning = conexion.getWarnings();
            while (warning != null) {
                System.err.println("Advertencia SQL: " + warning.getMessage());
                warning = warning.getNextWarning();
            }
            conexion.clearWarnings();
        } catch (SQLException ignored) {
        }
    }

    public static void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión JDBC: " + e.getMessage());
            }
        }
    }
}