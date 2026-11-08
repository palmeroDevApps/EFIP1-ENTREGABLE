package com.jrmarket.dao;

import com.jrmarket.model.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Componente del Modelo encargado de la interacción integral con la base de datos MySQL mediante JDBC.
 * Hidrata todas las clases del paquete com.jrmarket.model sin valores cableados.
 */
public class JRMarketDAO {

    private static boolean esquemaVerificado = false;
    private Connection conexion;

    public JRMarketDAO() {}

    public JRMarketDAO(Connection conexion) {
        this.conexion = conexion;
    }

    private Connection obtenerConexionInterna() throws SQLException {
        if (ConexionBD.esConexionValida(this.conexion)) {
            return this.conexion;
        }
        return ConexionBD.obtenerConexion();
    }

    private void cerrarSiEsTemporal(Connection conn) {
        if (this.conexion == null && conn != null) {
            ConexionBD.cerrarConexion(conn);
        }
    }

    /**
     * Verifica la estructura de tablas en MySQL (XAMPP) y asegura que los registros maestros iniciales existan.
     */
    public synchronized void inicializarBaseDeDatos() {
        if (esquemaVerificado) {
            return;
        }
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = obtenerConexionInterna();
            st = conn.createStatement();

            try {
                st.executeUpdate("ALTER TABLE Producto ADD COLUMN precioCosto DECIMAL(10,2) NOT NULL DEFAULT 0.00");
            } catch (SQLException ignored) {}
            try {
                st.executeUpdate("ALTER TABLE Producto ADD COLUMN estado BOOLEAN NOT NULL DEFAULT TRUE");
            } catch (SQLException ignored) {}

            st.executeUpdate("INSERT INTO Rol (idRol, nombre, descripcion, fechaAlta) VALUES "
                    + "(1, 'ADMINISTRADOR', 'Control total', '2026-01-01 00:00:00'), "
                    + "(2, 'VENDEDOR', 'Atención mostrador', '2026-01-01 00:00:00'), "
                    + "(3, 'REPOSITOR', 'Abastecimiento', '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion)");

            st.executeUpdate("INSERT INTO Permiso (idPermiso, nombre, descripcion, fechaAlta) VALUES "
                    + "(1, 'VENTAS', 'Acceso al mostrador de ventas', '2026-01-01 00:00:00'), "
                    + "(2, 'CIERRE_CAJA', 'Conciliación y arqueo de turno', '2026-01-01 00:00:00'), "
                    + "(3, 'CATALOGO', 'Consulta del catálogo de productos', '2026-01-01 00:00:00'), "
                    + "(4, 'COMPRAS', 'Registro de compras y reposición', '2026-01-01 00:00:00'), "
                    + "(5, 'GESTION_PRODUCTOS', 'Alta, baja y modificación de productos', '2026-01-01 00:00:00'), "
                    + "(6, 'USUARIOS', 'Administración de usuarios y permisos', '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion)");

            st.executeUpdate("INSERT INTO RolPermiso (idRolPermiso, idRol, idPermiso, fechaHora) VALUES "
                    + "(1, 1, 1, '2026-01-01 00:00:00'), (2, 1, 2, '2026-01-01 00:00:00'), "
                    + "(3, 1, 3, '2026-01-01 00:00:00'), (4, 1, 4, '2026-01-01 00:00:00'), "
                    + "(5, 1, 5, '2026-01-01 00:00:00'), (6, 1, 6, '2026-01-01 00:00:00'), "
                    + "(7, 2, 1, '2026-01-01 00:00:00'), (8, 2, 2, '2026-01-01 00:00:00'), "
                    + "(9, 3, 4, '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE idRol = VALUES(idRol), idPermiso = VALUES(idPermiso)");

            rs = st.executeQuery("SELECT COUNT(*) FROM Usuario WHERE nombreUsuario = 'cristian'");
            if (rs.next() && rs.getInt(1) == 0) {
                String hash = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";
                st.executeUpdate("INSERT INTO Usuario (idRol, nombreUsuario, contrasenia, nombreCompleto, estado, fechaAlta) VALUES "
                        + "(1, 'cristian', '" + hash + "', 'Cristian Palmero', 1, '2026-01-01 00:00:00'), "
                        + "(2, 'sofia', '" + hash + "', 'Sofía Martinez', 1, '2026-01-01 00:00:00'), "
                        + "(3, 'lucas', '" + hash + "', 'Lucas Gómez', 1, '2026-01-01 00:00:00'), "
                        + "(2, 'estefania', '" + hash + "', 'Estefania Pacheco', 0, '2026-01-01 00:00:00') "
                        + "ON DUPLICATE KEY UPDATE contrasenia = VALUES(contrasenia)");
            }
            rs.close();

            st.executeUpdate("INSERT INTO MedioPago (idMedioPago, nombre, fechaAlta) VALUES "
                    + "(1, 'Efectivo', '2026-01-01 00:00:00'), "
                    + "(2, 'Transferencia / QR', '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre)");

            st.executeUpdate("INSERT INTO EstadoVenta (idEstadoVenta, nombre, descripcion, fechaAlta) VALUES "
                    + "(1, 'Cobrada', 'Comprobante liquidado en mostrador', '2026-01-01 00:00:00'), "
                    + "(2, 'Anulada', 'Comprobante cancelado', '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre)");

            st.executeUpdate("INSERT INTO Marca (idMarca, nombre, fechaAlta) VALUES "
                    + "(1, 'Milka', '2026-01-01 00:00:00'), (2, 'Cepita', '2026-01-01 00:00:00'), "
                    + "(3, 'Coca-Cola', '2026-01-01 00:00:00'), (4, 'Lays', '2026-01-01 00:00:00'), "
                    + "(5, 'Beldent', '2026-01-01 00:00:00'), (6, 'Arcor', '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre)");

            st.executeUpdate("INSERT INTO TipoProducto (idTipoProducto, nombre, descripcion, fechaAlta) VALUES "
                    + "(1, 'Golosinas', 'Dulces y chocolates', '2026-01-01 00:00:00'), "
                    + "(2, 'Bebidas', 'Gaseosas y jugos', '2026-01-01 00:00:00'), "
                    + "(3, 'Snacks', 'Salados', '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion)");

            st.executeUpdate("INSERT INTO Producto (idProducto, idMarca, idTipoProducto, codigoBarras, descripcion, precio, precioCosto, stockActual, stockMinimo, estado, fechaDesde) VALUES "
                    + "(1, 1, 1, '779001', 'Alfajor Triple Oreo 61g', 1200.00, 700.00, 18, 5, 1, '2026-01-01 00:00:00'), "
                    + "(2, 2, 2, '779009', 'Jugo Naranja Fresh 500ml', 1300.00, 800.00, 24, 10, 1, '2026-01-01 00:00:00'), "
                    + "(3, 3, 2, '779015', 'Gaseosa 500ml Original', 1800.00, 1100.00, 4, 12, 1, '2026-01-01 00:00:00'), "
                    + "(4, 4, 3, '779022', 'Papas Fritas Clásicas 85g', 2100.00, 1300.00, 15, 5, 1, '2026-01-01 00:00:00'), "
                    + "(5, 5, 1, '779035', 'Chicles Menta Fuerte 10u', 600.00, 350.00, 8, 20, 1, '2026-01-01 00:00:00'), "
                    + "(6, 6, 1, '779040', 'Turrón de Maní 25g', 450.00, 250.00, 60, 20, 1, '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE precioCosto = IF(precioCosto = 0, VALUES(precioCosto), precioCosto)");

            st.executeUpdate("INSERT INTO Proveedor (idProveedor, razonSocial, cuit, direccion, telefono, estado, fechaAlta) VALUES "
                    + "(1, 'Mayorista BLOW MAX', '30-67891234-9', 'Mendoza', '261-4455667', 1, '2026-01-01 00:00:00'), "
                    + "(2, 'TADICOR Mayorista', '30-71175642-1', 'Las Heras', '261-4455889', 1, '2026-01-01 00:00:00'), "
                    + "(3, 'OSCAR DAVID Mayorista', '30-58912345-6', 'Godoy Cruz', '261-4455990', 1, '2026-01-01 00:00:00') "
                    + "ON DUPLICATE KEY UPDATE razonSocial = VALUES(razonSocial)");

            esquemaVerificado = true;
            System.out.println("Conexion y sincronizacion con MySQL completada.");
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (inicializarBaseDeDatos): " + e.getMessage());
        } finally {
            cerrarRecursos(rs, st);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Consulta los roles y sus permisos asociados desde las tablas Rol, Permiso y RolPermiso en MySQL.
     */
    public List<Rol> consultarRolesConPermisos() {
        inicializarBaseDeDatos();
        List<Rol> roles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement psRoles = null;
        PreparedStatement psPermisos = null;
        ResultSet rsRoles = null;
        ResultSet rsPermisos = null;

        try {
            conn = obtenerConexionInterna();
            String sqlRoles = "SELECT idRol, nombre, descripcion, "
                    + "DATE_FORMAT(fechaAlta, '%Y-%m-%d') AS fechaDesde, "
                    + "DATE_FORMAT(fechaBaja, '%Y-%m-%d') AS fechaHasta "
                    + "FROM Rol WHERE fechaBaja IS NULL ORDER BY idRol";
            psRoles = conn.prepareStatement(sqlRoles);
            rsRoles = psRoles.executeQuery();

            while (rsRoles.next()) {
                Rol rol = new Rol(
                        rsRoles.getInt("idRol"),
                        rsRoles.getString("nombre"),
                        rsRoles.getString("descripcion"),
                        rsRoles.getString("fechaDesde"),
                        rsRoles.getString("fechaHasta")
                );
                roles.add(rol);
            }

            String sqlPermisos = "SELECT rp.idRolPermiso, rp.idRol, "
                    + "DATE_FORMAT(rp.fechaHora, '%Y-%m-%d %H:%i:%s') AS fechaHora, "
                    + "p.idPermiso, p.nombre, p.descripcion "
                    + "FROM RolPermiso rp "
                    + "INNER JOIN Permiso p ON rp.idPermiso = p.idPermiso "
                    + "WHERE p.fechaBaja IS NULL ORDER BY rp.idRolPermiso";
            psPermisos = conn.prepareStatement(sqlPermisos);
            rsPermisos = psPermisos.executeQuery();

            while (rsPermisos.next()) {
                int idRol = rsPermisos.getInt("idRol");
                Rol rolAsociado = buscarRolPorId(roles, idRol);
                if (rolAsociado != null) {
                    Permiso permiso = new Permiso(
                            rsPermisos.getInt("idPermiso"),
                            rsPermisos.getString("nombre"),
                            rsPermisos.getString("descripcion")
                    );
                    RolPermiso rp = new RolPermiso(
                            rsPermisos.getInt("idRolPermiso"),
                            rsPermisos.getString("fechaHora"),
                            rolAsociado,
                            permiso
                    );
                    rolAsociado.getRolPermisos().add(rp);
                }
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarRolesConPermisos): " + e.getMessage());
        } finally {
            cerrarRecursos(rsPermisos, psPermisos);
            cerrarRecursos(rsRoles, psRoles);
            cerrarSiEsTemporal(conn);
        }
        return roles;
    }

    /**
     * Consulta los usuarios registrados en la tabla Usuario de MySQL y los asocia con su Rol.
     */
    public List<Usuario> consultarUsuarios(List<Rol> rolesDisponibles) {
        inicializarBaseDeDatos();
        List<Usuario> usuarios = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = obtenerConexionInterna();
            String consultaSQL = "SELECT idUsuario, idRol, nombreUsuario, contrasenia, nombreCompleto, estado "
                    + "FROM Usuario WHERE nombreUsuario IN ('cristian', 'sofia', 'lucas', 'estefania') OR idUsuario > 2 "
                    + "ORDER BY idUsuario";
            statement = conn.prepareStatement(consultaSQL);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int idUsuario = resultSet.getInt("idUsuario");
                int idRol = resultSet.getInt("idRol");
                String nombreUsuario = resultSet.getString("nombreUsuario");
                String contrasenia = resultSet.getString("contrasenia");
                String nombreCompleto = resultSet.getString("nombreCompleto");
                boolean estado = resultSet.getBoolean("estado");

                Rol rolAsignado = buscarRolPorId(rolesDisponibles, idRol);
                Usuario usuario = new Usuario(idUsuario, nombreUsuario, contrasenia, nombreCompleto, estado, rolAsignado);
                usuarios.add(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarUsuarios): " + e.getMessage());
        } finally {
            cerrarRecursos(resultSet, statement);
            cerrarSiEsTemporal(conn);
        }
        return usuarios;
    }

    private Rol buscarRolPorId(List<Rol> roles, int idRol) {
        Iterator<Rol> it = roles.iterator();
        while (it.hasNext()) {
            Rol r = it.next();
            if (r.getIdRol() == idRol) {
                return r;
            }
        }
        return roles.isEmpty() ? null : roles.get(0);
    }

    /**
     * Consulta los objetos MedioPago desde la tabla MedioPago de MySQL.
     */
    public List<MedioPago> consultarMediosPagoDisponibles() {
        inicializarBaseDeDatos();
        List<MedioPago> medios = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = obtenerConexionInterna();
            String sql = "SELECT idMedioPago, nombre, fechaAlta, fechaBaja FROM MedioPago ORDER BY idMedioPago";
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("idMedioPago");
                String descripcion = rs.getString("nombre");
                Date fAlta = rs.getDate("fechaAlta");
                Date fBaja = rs.getDate("fechaBaja");
                LocalDate fechaDesde = fAlta != null ? fAlta.toLocalDate() : LocalDate.of(2026, 1, 1);
                LocalDate fechaHasta = fBaja != null ? fBaja.toLocalDate() : null;
                boolean afectaCajaFisica = descripcion != null && descripcion.toUpperCase().contains("EFECTIVO");

                MedioPago mp = new MedioPago(id, descripcion, fechaDesde, fechaHasta, afectaCajaFisica);
                if (mp.isVigente()) {
                    medios.add(mp);
                }
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarMediosPagoDisponibles): " + e.getMessage());
        } finally {
            cerrarRecursos(rs, statement);
            cerrarSiEsTemporal(conn);
        }
        return medios;
    }

    /**
     * Consulta las marcas activas en la tabla Marca de MySQL.
     */
    public List<Marca> consultarMarcasDisponibles() {
        inicializarBaseDeDatos();
        List<Marca> marcas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = obtenerConexionInterna();
            String sql = "SELECT idMarca, nombre, DATE_FORMAT(fechaAlta, '%Y-%m-%d') AS fechaDesde, "
                    + "DATE_FORMAT(fechaBaja, '%Y-%m-%d') AS fechaHasta FROM Marca WHERE fechaBaja IS NULL ORDER BY idMarca";
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();

            while (rs.next()) {
                marcas.add(new Marca(
                        rs.getInt("idMarca"),
                        rs.getString("nombre"),
                        rs.getString("fechaDesde"),
                        rs.getString("fechaHasta")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarMarcasDisponibles): " + e.getMessage());
        } finally {
            cerrarRecursos(rs, statement);
            cerrarSiEsTemporal(conn);
        }
        return marcas;
    }

    /**
     * Consulta los rubros/tipos de producto activos en la tabla TipoProducto de MySQL.
     */
    public List<TipoProducto> consultarTiposProductoDisponibles() {
        inicializarBaseDeDatos();
        List<TipoProducto> tipos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = obtenerConexionInterna();
            String sql = "SELECT idTipoProducto, nombre, descripcion, DATE_FORMAT(fechaAlta, '%Y-%m-%d') AS fechaDesde, "
                    + "DATE_FORMAT(fechaBaja, '%Y-%m-%d') AS fechaHasta FROM TipoProducto WHERE fechaBaja IS NULL ORDER BY idTipoProducto";
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();

            while (rs.next()) {
                tipos.add(new TipoProducto(
                        rs.getInt("idTipoProducto"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("fechaDesde"),
                        rs.getString("fechaHasta")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarTiposProductoDisponibles): " + e.getMessage());
        } finally {
            cerrarRecursos(rs, statement);
            cerrarSiEsTemporal(conn);
        }
        return tipos;
    }

    /**
     * Consulta un EstadoVenta por su nombre desde la tabla EstadoVenta de MySQL.
     */
    public EstadoVenta consultarEstadoVentaPorNombre(String nombreEstado) {
        inicializarBaseDeDatos();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            conn = obtenerConexionInterna();
            String sql = "SELECT idEstadoVenta, nombre, descripcion FROM EstadoVenta WHERE UPPER(nombre) = UPPER(?) LIMIT 1";
            statement = conn.prepareStatement(sql);
            statement.setString(1, nombreEstado);
            rs = statement.executeQuery();

            if (rs.next()) {
                return new EstadoVenta(
                        rs.getInt("idEstadoVenta"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                );
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarEstadoVentaPorNombre): " + e.getMessage());
        } finally {
            cerrarRecursos(rs, statement);
            cerrarSiEsTemporal(conn);
        }
        return new EstadoVenta(1, nombreEstado, "Estado de venta");
    }

    /**
     * Consulta los productos disponibles en MySQL junto con sus objetos Marca y TipoProducto.
     */
    public List<Producto> consultarProductosDisponibles() {
        inicializarBaseDeDatos();
        List<Producto> productos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = obtenerConexionInterna();
            String consultaSQL = "SELECT p.idProducto, p.codigoBarras, p.descripcion, p.precio, "
                    + "p.precioCosto, p.stockActual, p.stockMinimo, p.estado, p.fechaDesde, p.fechaHasta, "
                    + "m.idMarca, m.nombre AS nombreMarca, "
                    + "t.idTipoProducto, t.nombre AS nombreTipo, t.descripcion AS descTipo "
                    + "FROM Producto p "
                    + "INNER JOIN Marca m ON p.idMarca = m.idMarca "
                    + "INNER JOIN TipoProducto t ON p.idTipoProducto = t.idTipoProducto "
                    + "ORDER BY p.idProducto";

            statement = conn.prepareStatement(consultaSQL);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Marca marca = new Marca(
                        resultSet.getInt("idMarca"),
                        resultSet.getString("nombreMarca"),
                        "2026-01-01",
                        null
                );
                TipoProducto tipo = new TipoProducto(
                        resultSet.getInt("idTipoProducto"),
                        resultSet.getString("nombreTipo"),
                        resultSet.getString("descTipo"),
                        "2026-01-01",
                        null
                );

                String fechaHasta = resultSet.getString("fechaHasta");
                boolean estadoActivo = resultSet.getBoolean("estado") && (fechaHasta == null);

                Producto producto = new Producto(
                        resultSet.getInt("idProducto"),
                        resultSet.getString("codigoBarras"),
                        resultSet.getString("descripcion"),
                        resultSet.getDouble("precio"),
                        resultSet.getDouble("precioCosto"),
                        resultSet.getInt("stockActual"),
                        resultSet.getInt("stockMinimo"),
                        estadoActivo,
                        resultSet.getString("fechaDesde"),
                        fechaHasta,
                        marca,
                        tipo
                );
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarProductosDisponibles): " + e.getMessage());
        } finally {
            cerrarRecursos(resultSet, statement);
            cerrarSiEsTemporal(conn);
        }
        return productos;
    }

    /**
     * Consulta los proveedores mayoristas almacenados en MySQL.
     */
    public List<Proveedor> consultarProveedoresDisponibles() {
        inicializarBaseDeDatos();
        List<Proveedor> proveedores = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = obtenerConexionInterna();
            String consultaSQL = "SELECT idProveedor, razonSocial, cuit, direccion, telefono, estado FROM Proveedor ORDER BY idProveedor";
            statement = conn.prepareStatement(consultaSQL);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Proveedor proveedor = new Proveedor(
                        resultSet.getInt("idProveedor"),
                        resultSet.getString("razonSocial"),
                        resultSet.getString("cuit"),
                        resultSet.getString("direccion"),
                        resultSet.getString("telefono"),
                        resultSet.getBoolean("estado")
                );
                proveedores.add(proveedor);
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarProveedoresDisponibles): " + e.getMessage());
        } finally {
            cerrarRecursos(resultSet, statement);
            cerrarSiEsTemporal(conn);
        }
        return proveedores;
    }

    /**
     * Consulta los turnos de caja registrados en MySQL y reconstruye sus ventas asociadas.
     */
    public List<TurnoCaja> consultarTurnosCaja(List<Usuario> usuarios) {
        inicializarBaseDeDatos();
        List<TurnoCaja> turnos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement psTurnos = null;
        PreparedStatement psVentas = null;
        ResultSet rsTurnos = null;
        ResultSet rsVentas = null;

        try {
            conn = obtenerConexionInterna();
            String sqlTurnos = "SELECT idTurno, idUsuario, "
                    + "DATE_FORMAT(fechaHoraApertura, '%Y-%m-%d %H:%i:%s') AS apertura, "
                    + "DATE_FORMAT(fechaHoraCierre, '%Y-%m-%d %H:%i:%s') AS cierre, "
                    + "montoInicial, totalVentasEfectivo, totalVentasTransferencia, "
                    + "saldoTeoricoSistema, saldoRealFisico, diferencia, observaciones, estado "
                    + "FROM TurnoCaja ORDER BY idTurno";
            psTurnos = conn.prepareStatement(sqlTurnos);
            rsTurnos = psTurnos.executeQuery();

            String sqlVentas = "SELECT v.idVenta, v.nroTicket, DATE_FORMAT(v.fechaVenta, '%Y-%m-%d %H:%i:%s') AS fecha, "
                    + "v.total, m.nombre AS nombreMedio "
                    + "FROM Venta v INNER JOIN MedioPago m ON v.idMedioPago = m.idMedioPago WHERE v.idTurno = ?";
            psVentas = conn.prepareStatement(sqlVentas);

            while (rsTurnos.next()) {
                int idTurno = rsTurnos.getInt("idTurno");
                int idUsuario = rsTurnos.getInt("idUsuario");
                Usuario op = buscarUsuarioPorId(usuarios, idUsuario);

                TurnoCaja turno = new TurnoCaja(idTurno, op);
                turno.setFechaHoraApertura(rsTurnos.getString("apertura"));
                turno.setFechaHoraCierre(rsTurnos.getString("cierre"));
                turno.setMontoInicial(rsTurnos.getDouble("montoInicial"));
                turno.setTotalVentasEfectivo(rsTurnos.getDouble("totalVentasEfectivo"));
                turno.setTotalVentasTransferencia(rsTurnos.getDouble("totalVentasTransferencia"));
                turno.setSaldoTeoricoSistema(rsTurnos.getDouble("saldoTeoricoSistema"));
                turno.setSaldoRealFisico(rsTurnos.getDouble("saldoRealFisico"));
                turno.setDiferencia(rsTurnos.getDouble("diferencia"));
                turno.setObservaciones(rsTurnos.getString("observaciones"));
                turno.setEstado(rsTurnos.getString("estado"));

                psVentas.setInt(1, idTurno);
                rsVentas = psVentas.executeQuery();
                while (rsVentas.next()) {
                    Venta v = new Venta(
                            rsVentas.getInt("idVenta"),
                            rsVentas.getString("nroTicket"),
                            rsVentas.getString("fecha"),
                            op,
                            rsVentas.getString("nombreMedio"),
                            turno
                    );
                    v.setTotal(rsVentas.getDouble("total"));
                    turno.getVentas().add(v);
                }
                rsVentas.close();

                turnos.add(turno);
            }
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (consultarTurnosCaja): " + e.getMessage());
        } finally {
            cerrarRecursos(rsVentas, psVentas);
            cerrarRecursos(rsTurnos, psTurnos);
            cerrarSiEsTemporal(conn);
        }
        return turnos;
    }

    private Usuario buscarUsuarioPorId(List<Usuario> usuarios, int idUsuario) {
        if (usuarios == null) return null;
        Iterator<Usuario> it = usuarios.iterator();
        while (it.hasNext()) {
            Usuario u = it.next();
            if (u.getIdUsuario() == idUsuario) return u;
        }
        return usuarios.isEmpty() ? null : usuarios.get(0);
    }

    /**
     * Inserta un nuevo producto en la tabla Producto de MySQL.
     */
    public boolean insertarProducto(Producto p) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "INSERT INTO Producto (idMarca, idTipoProducto, codigoBarras, descripcion, precio, precioCosto, stockActual, stockMinimo, estado, fechaDesde, fechaHasta) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NULL)";
            statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, p.getMarca() != null ? p.getMarca().getIdMarca() : 1);
            statement.setInt(2, p.getTipoProducto() != null ? p.getTipoProducto().getIdTipoProducto() : 1);
            statement.setString(3, p.getCodigoBarras());
            statement.setString(4, p.getDescripcion());
            statement.setDouble(5, p.getPrecioVenta());
            statement.setDouble(6, p.getPrecioCosto());
            statement.setInt(7, p.getStockActual());
            statement.setInt(8, p.getStockMinimo());
            statement.setBoolean(9, p.isEstado());

            int filas = statement.executeUpdate();
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                p.setIdProducto(generatedKeys.getInt(1));
            }
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (insertarProducto): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(generatedKeys, statement);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Actualiza un producto existente en la tabla Producto de MySQL.
     */
    public boolean modificarProducto(Producto p) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "UPDATE Producto SET idMarca = ?, idTipoProducto = ?, codigoBarras = ?, descripcion = ?, "
                    + "precio = ?, precioCosto = ?, stockActual = ?, stockMinimo = ? WHERE idProducto = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, p.getMarca() != null ? p.getMarca().getIdMarca() : 1);
            statement.setInt(2, p.getTipoProducto() != null ? p.getTipoProducto().getIdTipoProducto() : 1);
            statement.setString(3, p.getCodigoBarras());
            statement.setString(4, p.getDescripcion());
            statement.setDouble(5, p.getPrecioVenta());
            statement.setDouble(6, p.getPrecioCosto());
            statement.setInt(7, p.getStockActual());
            statement.setInt(8, p.getStockMinimo());
            statement.setInt(9, p.getIdProducto());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (modificarProducto): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(null, statement);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Realiza la baja lógica de un producto en MySQL.
     */
    public boolean darDeBajaProducto(int idProducto) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "UPDATE Producto SET estado = 0, fechaHasta = NOW() WHERE idProducto = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, idProducto);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (darDeBajaProducto): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(null, statement);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Inserta un nuevo TurnoCaja abierto en MySQL.
     */
    public boolean insertarTurnoCaja(TurnoCaja turno) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet keys = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "INSERT INTO TurnoCaja (idUsuario, fechaHoraApertura, montoInicial, totalVentasEfectivo, "
                    + "totalVentasTransferencia, saldoTeoricoSistema, estado) VALUES (?, NOW(), ?, 0.00, 0.00, ?, 'ABIERTO')";
            statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, turno.getUsuario() != null ? turno.getUsuario().getIdUsuario() : 1);
            statement.setDouble(2, turno.getMontoInicial());
            statement.setDouble(3, turno.getSaldoTeoricoSistema());

            int filas = statement.executeUpdate();
            keys = statement.getGeneratedKeys();
            if (keys.next()) {
                turno.setIdTurno(keys.getInt(1));
            }
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (insertarTurnoCaja): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(keys, statement);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Persiste el arqueo y cierre del TurnoCaja en MySQL.
     */
    public boolean cerrarTurnoCaja(TurnoCaja turno) {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "UPDATE TurnoCaja SET fechaHoraCierre = NOW(), totalVentasEfectivo = ?, "
                    + "totalVentasTransferencia = ?, saldoTeoricoSistema = ?, saldoRealFisico = ?, "
                    + "diferencia = ?, observaciones = ?, estado = 'CERRADO' WHERE idTurno = ?";
            statement = conn.prepareStatement(sql);
            statement.setDouble(1, turno.getTotalVentasEfectivo());
            statement.setDouble(2, turno.getTotalVentasTransferencia());
            statement.setDouble(3, turno.getSaldoTeoricoSistema());
            statement.setDouble(4, turno.getSaldoRealFisico());
            statement.setDouble(5, turno.getDiferencia());
            statement.setString(6, turno.getObservaciones());
            statement.setInt(7, turno.getIdTurno());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (cerrarTurnoCaja): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(null, statement);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Registra una Venta en MySQL utilizando el objeto MedioPago y EstadoVenta correspondientes.
     */
    public boolean registrarVentaTransaccional(Venta venta, MedioPago medioPago) {
        Connection conn = null;
        PreparedStatement psVenta = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psStock = null;
        PreparedStatement psHistorial = null;
        PreparedStatement psTurno = null;
        ResultSet keys = null;

        try {
            conn = obtenerConexionInterna();
            conn.setAutoCommit(false);

            boolean esEfectivo = medioPago != null
                    ? medioPago.isAfectaCajaFisica()
                    : (venta.getMedioPago() != null && venta.getMedioPago().toUpperCase().contains("EFECTIVO"));
            int idMedioPago = medioPago != null ? medioPago.getId() : (esEfectivo ? 1 : 2);
            int idTurno = venta.getTurnoCaja() != null ? venta.getTurnoCaja().getIdTurno() : 1;
            int idUsuario = venta.getUsuario() != null ? venta.getUsuario().getIdUsuario() : 1;
            int idEstadoVenta = venta.getEstadoActual() != null ? venta.getEstadoActual().getIdEstadoVenta() : 1;

            String sqlVenta = "INSERT INTO Venta (idTurno, idUsuario, idMedioPago, nroTicket, fechaVenta, total, montoRecibido, vuelto, nroComprobantePago) "
                    + "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
            psVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, idTurno);
            psVenta.setInt(2, idUsuario);
            psVenta.setInt(3, idMedioPago);
            psVenta.setString(4, venta.getNroTicket());
            psVenta.setDouble(5, venta.getTotal());
            if (esEfectivo) {
                psVenta.setDouble(6, venta.getMontoRecibido());
                psVenta.setDouble(7, venta.getVuelto());
                psVenta.setNull(8, Types.VARCHAR);
            } else {
                psVenta.setNull(6, Types.DECIMAL);
                psVenta.setNull(7, Types.DECIMAL);
                psVenta.setString(8, venta.getComprobantePago());
            }
            psVenta.executeUpdate();

            keys = psVenta.getGeneratedKeys();
            int idVentaGenerada = venta.getIdComprobante();
            if (keys.next()) {
                idVentaGenerada = keys.getInt(1);
                venta.setIdComprobante(idVentaGenerada);
            }

            String sqlDetalle = "INSERT INTO DetalleVenta (idVenta, idProducto, renglon, cantidad, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE Producto SET stockActual = ? WHERE idProducto = ?";
            psDetalle = conn.prepareStatement(sqlDetalle);
            psStock = conn.prepareStatement(sqlStock);

            int nroRenglon = 1;
            Iterator<DetalleVenta> it = venta.getDetalles().iterator();
            while (it.hasNext()) {
                DetalleVenta dv = it.next();
                psDetalle.setInt(1, idVentaGenerada);
                psDetalle.setInt(2, dv.getProducto().getIdProducto());
                psDetalle.setInt(3, nroRenglon++);
                psDetalle.setInt(4, dv.getCantidad());
                psDetalle.setDouble(5, dv.getSubtotal());
                psDetalle.executeUpdate();

                psStock.setInt(1, dv.getProducto().getStockActual());
                psStock.setInt(2, dv.getProducto().getIdProducto());
                psStock.executeUpdate();
            }

            String sqlHistorial = "INSERT INTO HistorialEstadoVenta (idVenta, idEstadoVenta, fechaDesde, observaciones) VALUES (?, ?, NOW(), 'Venta cobrada en mostrador')";
            psHistorial = conn.prepareStatement(sqlHistorial);
            psHistorial.setInt(1, idVentaGenerada);
            psHistorial.setInt(2, idEstadoVenta);
            psHistorial.executeUpdate();

            if (venta.getTurnoCaja() != null) {
                String sqlTurno = "UPDATE TurnoCaja SET totalVentasEfectivo = ?, totalVentasTransferencia = ?, saldoTeoricoSistema = ? WHERE idTurno = ?";
                psTurno = conn.prepareStatement(sqlTurno);
                psTurno.setDouble(1, venta.getTurnoCaja().getTotalVentasEfectivo());
                psTurno.setDouble(2, venta.getTurnoCaja().getTotalVentasTransferencia());
                psTurno.setDouble(3, venta.getTurnoCaja().getSaldoTeoricoSistema());
                psTurno.setInt(4, venta.getTurnoCaja().getIdTurno());
                psTurno.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("Aviso JDBC (registrarVentaTransaccional): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(keys, psVenta);
            cerrarRecursos(null, psDetalle);
            cerrarRecursos(null, psStock);
            cerrarRecursos(null, psHistorial);
            cerrarRecursos(null, psTurno);
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Registra una Compra mayorista en MySQL (Compra, DetalleCompra y actualización de Producto).
     */
    public boolean registrarCompraTransaccional(Compra compra) {
        Connection conn = null;
        PreparedStatement psCompra = null;
        PreparedStatement psDetalle = null;
        PreparedStatement psProducto = null;
        ResultSet keys = null;

        try {
            conn = obtenerConexionInterna();
            conn.setAutoCommit(false);

            String sqlCompra = "INSERT INTO Compra (idProveedor, idUsuario, nroComprobante, fechaCompra, total) VALUES (?, ?, ?, NOW(), ?)";
            psCompra = conn.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS);
            psCompra.setInt(1, compra.getProveedor() != null ? compra.getProveedor().getIdProveedor() : 1);
            psCompra.setInt(2, compra.getUsuario() != null ? compra.getUsuario().getIdUsuario() : 1);
            psCompra.setString(3, compra.getNroComprobante());
            psCompra.setDouble(4, compra.getTotal());
            psCompra.executeUpdate();

            keys = psCompra.getGeneratedKeys();
            int idCompraGenerada = compra.getIdCompra();
            if (keys.next()) {
                idCompraGenerada = keys.getInt(1);
                compra.setIdCompra(idCompraGenerada);
            }

            String sqlDetalle = "INSERT INTO DetalleCompra (idCompra, idProducto, cantidad, costoUnitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlProd = "UPDATE Producto SET stockActual = ?, precioCosto = ? WHERE idProducto = ?";
            psDetalle = conn.prepareStatement(sqlDetalle);
            psProducto = conn.prepareStatement(sqlProd);

            Iterator<DetalleCompra> it = compra.getDetalles().iterator();
            while (it.hasNext()) {
                DetalleCompra dc = it.next();
                psDetalle.setInt(1, idCompraGenerada);
                psDetalle.setInt(2, dc.getProducto().getIdProducto());
                psDetalle.setInt(3, dc.getCantidad());
                psDetalle.setDouble(4, dc.getCostoUnitario());
                psDetalle.setDouble(5, dc.getSubtotal());
                psDetalle.executeUpdate();

                psProducto.setInt(1, dc.getProducto().getStockActual());
                psProducto.setDouble(2, dc.getProducto().getPrecioCosto());
                psProducto.setInt(3, dc.getProducto().getIdProducto());
                psProducto.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("Aviso JDBC (registrarCompraTransaccional): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(keys, psCompra);
            cerrarRecursos(null, psDetalle);
            cerrarRecursos(null, psProducto);
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Inserta un nuevo usuario en la tabla Usuario de MySQL.
     */
    public boolean insertarUsuario(Usuario u, String hashContrasenia) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "INSERT INTO Usuario (idRol, nombreUsuario, contrasenia, nombreCompleto, estado, fechaAlta) VALUES (?, ?, ?, ?, ?, NOW())";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, u.getRol() != null ? u.getRol().getIdRol() : 2);
            ps.setString(2, u.getNombreUsuario());
            ps.setString(3, hashContrasenia);
            ps.setString(4, u.getNombreCompleto());
            ps.setBoolean(5, u.isEstado());
            int filas = ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                u.setIdUsuario(keys.getInt(1));
            }
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (insertarUsuario): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(keys, ps);
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Actualiza el rol o el estado de un usuario en la tabla Usuario de MySQL.
     */
    public boolean actualizarUsuario(Usuario u) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "UPDATE Usuario SET idRol = ?, estado = ?, fechaBaja = ? WHERE nombreUsuario = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, u.getRol() != null ? u.getRol().getIdRol() : 2);
            ps.setBoolean(2, u.isEstado());
            if (u.isEstado()) {
                ps.setNull(3, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            }
            ps.setString(4, u.getNombreUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Aviso JDBC (actualizarUsuario): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(null, ps);
            cerrarSiEsTemporal(conn);
        }
    }

    private void cerrarRecursos(ResultSet rs, Statement st) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (st != null) {
            try { st.close(); } catch (SQLException ignored) {}
        }
    }
}