package com.jrmarket.dao;

import com.jrmarket.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Componente del Modelo encargado de la persistencia y consulta de datos en MySQL mediante JDBC.
 * Sigue la estructura de PreparedStatement, ResultSet y ArrayList definida en la cátedra.
 */
public class JRMarketDAO {

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
     * Consulta los usuarios registrados en MySQL y los vincula con su Rol en memoria.
     */
    public List<Usuario> consultarUsuarios(List<Rol> rolesDisponibles) {
        List<Usuario> usuarios = new ArrayList<>();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = obtenerConexionInterna();
            String consultaSQL = "SELECT idUsuario, idRol, nombreUsuario, contrasenia, nombreCompleto, estado FROM Usuario ORDER BY idUsuario";
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
     * Consulta el catálogo de productos disponibles desde MySQL junto con su Marca y TipoProducto.
     */
    public List<Producto> consultarProductosDisponibles() {
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
     * Consulta los proveedores mayoristas activos en la base de datos MySQL.
     */
    public List<Proveedor> consultarProveedoresDisponibles() {
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
     * Actualiza los datos comerciales y de stock de un producto en MySQL.
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
     * Registra la baja lógica de un producto en MySQL seteando estado = 0 y fechaHasta = NOW().
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
     * Registra la apertura de un nuevo TurnoCaja en la base de datos MySQL.
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
     * Actualiza el cierre y arqueo del turno de caja en MySQL.
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
     * Persiste una Venta completa (Cabecera, DetalleVenta, HistorialEstadoVenta, Stock y TurnoCaja)
     * utilizando una transacción ACID en MySQL.
     */
    public boolean registrarVentaTransaccional(Venta venta) {
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

            int idMedioPago = (venta.getMedioPago() != null && venta.getMedioPago().toUpperCase().contains("EFECTIVO")) ? 1 : 2;
            int idTurno = venta.getTurnoCaja() != null ? venta.getTurnoCaja().getIdTurno() : 1;
            int idUsuario = venta.getUsuario() != null ? venta.getUsuario().getIdUsuario() : 1;

            String sqlVenta = "INSERT INTO Venta (idTurno, idUsuario, idMedioPago, nroTicket, fechaVenta, total, montoRecibido, vuelto, nroComprobantePago) "
                    + "VALUES (?, ?, ?, ?, NOW(), ?, ?, ?, ?)";
            psVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, idTurno);
            psVenta.setInt(2, idUsuario);
            psVenta.setInt(3, idMedioPago);
            psVenta.setString(4, venta.getNroTicket());
            psVenta.setDouble(5, venta.getTotal());
            if (idMedioPago == 1) {
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
            int idVentaGenerada = venta.getIdVenta();
            if (keys.next()) {
                idVentaGenerada = keys.getInt(1);
                venta.setIdVenta(idVentaGenerada);
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

            String sqlHistorial = "INSERT INTO HistorialEstadoVenta (idVenta, idEstadoVenta, fechaDesde, observaciones) VALUES (?, 1, NOW(), 'Transacción finalizada en mostrador')";
            psHistorial = conn.prepareStatement(sqlHistorial);
            psHistorial.setInt(1, idVentaGenerada);
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
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback de venta: " + ex.getMessage());
                }
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
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Persiste una Compra mayorista en MySQL (Compra, DetalleCompra y actualización de stock/costo en Producto).
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
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback de compra: " + ex.getMessage());
                }
            }
            System.err.println("Aviso JDBC (registrarCompraTransaccional): " + e.getMessage());
            return false;
        } finally {
            cerrarRecursos(keys, psCompra);
            cerrarRecursos(null, psDetalle);
            cerrarRecursos(null, psProducto);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
            cerrarSiEsTemporal(conn);
        }
    }

    /**
     * Inserta un nuevo usuario operador en la tabla Usuario de MySQL.
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
     * Actualiza el rol o el estado activo/inactivo de un usuario en MySQL.
     */
    public boolean actualizarUsuario(Usuario u) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = obtenerConexionInterna();
            String sql = "UPDATE Usuario SET idRol = ?, estado = ?, fechaBaja = ? WHERE idUsuario = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, u.getRol() != null ? u.getRol().getIdRol() : 2);
            ps.setBoolean(2, u.isEstado());
            if (u.isEstado()) {
                ps.setNull(3, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            }
            ps.setInt(4, u.getIdUsuario());
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