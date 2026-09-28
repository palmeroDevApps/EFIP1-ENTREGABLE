-- Creación de la base de datos
CREATE DATABASE IF NOT EXISTS `jrmarket` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE `jrmarket`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `HistorialEstadoVenta`;
DROP TABLE IF EXISTS `DetalleVenta`;
DROP TABLE IF EXISTS `Venta`;
DROP TABLE IF EXISTS `EstadoVenta`;
DROP TABLE IF EXISTS `MedioPago`;
DROP TABLE IF EXISTS `TurnoCaja`;
DROP TABLE IF EXISTS `DetalleCompra`;
DROP TABLE IF EXISTS `Compra`;
DROP TABLE IF EXISTS `Producto`;
DROP TABLE IF EXISTS `TipoProducto`;
DROP TABLE IF EXISTS `Marca`;
DROP TABLE IF EXISTS `Proveedor`;
DROP TABLE IF EXISTS `Usuario`;
DROP TABLE IF EXISTS `RolPermiso`;
DROP TABLE IF EXISTS `Permiso`;
DROP TABLE IF EXISTS `Rol`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. Rol
CREATE TABLE `Rol` (
    `idRol` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL,
    `descripcion` VARCHAR(150) NULL,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idRol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 2. Permiso
CREATE TABLE `Permiso` (
    `idPermiso` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(60) NOT NULL,
    `descripcion` VARCHAR(150) NULL,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idPermiso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 3. RolPermiso
CREATE TABLE `RolPermiso` (
    `idRolPermiso` INT NOT NULL AUTO_INCREMENT,
    `idRol` INT NOT NULL,
    `idPermiso` INT NOT NULL,
    `fechaHora` DATETIME NOT NULL,
    PRIMARY KEY (`idRolPermiso`),
    CONSTRAINT `fk_RolPermiso_Rol` FOREIGN KEY (`idRol`) REFERENCES `Rol` (`idRol`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_RolPermiso_Permiso` FOREIGN KEY (`idPermiso`) REFERENCES `Permiso` (`idPermiso`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 4. Usuario
CREATE TABLE `Usuario` (
    `idUsuario` INT NOT NULL AUTO_INCREMENT,
    `idRol` INT NOT NULL,
    `nombreUsuario` VARCHAR(50) NOT NULL UNIQUE,
    `contrasenia` VARCHAR(255) NOT NULL,
    `nombreCompleto` VARCHAR(100) NOT NULL,
    `estado` BOOLEAN NOT NULL DEFAULT TRUE,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idUsuario`),
    CONSTRAINT `fk_Usuario_Rol` FOREIGN KEY (`idRol`) REFERENCES `Rol` (`idRol`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 5. Proveedor
CREATE TABLE `Proveedor` (
    `idProveedor` INT NOT NULL AUTO_INCREMENT,
    `razonSocial` VARCHAR(100) NOT NULL,
    `cuit` VARCHAR(13) NOT NULL UNIQUE,
    `direccion` VARCHAR(150) NULL,
    `telefono` VARCHAR(30) NULL,
    `estado` BOOLEAN NOT NULL DEFAULT TRUE,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idProveedor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 6. Marca
CREATE TABLE `Marca` (
    `idMarca` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(60) NOT NULL,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idMarca`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 7. TipoProducto
CREATE TABLE `TipoProducto` (
    `idTipoProducto` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(60) NOT NULL,
    `descripcion` VARCHAR(150) NULL,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idTipoProducto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 8. Producto
CREATE TABLE `Producto` (
    `idProducto` INT NOT NULL AUTO_INCREMENT,
    `idMarca` INT NOT NULL,
    `idTipoProducto` INT NOT NULL,
    `codigoBarras` VARCHAR(50) NOT NULL UNIQUE,
    `descripcion` VARCHAR(150) NOT NULL,
    `precio` DECIMAL(10,2) NOT NULL,
    `stockActual` INT NOT NULL DEFAULT 0,
    `stockMinimo` INT NOT NULL DEFAULT 5,
    `fechaDesde` DATETIME NOT NULL,
    `fechaHasta` DATETIME NULL,
    PRIMARY KEY (`idProducto`),
    CONSTRAINT `fk_Producto_Marca` FOREIGN KEY (`idMarca`) REFERENCES `Marca` (`idMarca`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_Producto_TipoProducto` FOREIGN KEY (`idTipoProducto`) REFERENCES `TipoProducto` (`idTipoProducto`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 9. Compra
CREATE TABLE `Compra` (
    `idCompra` INT NOT NULL AUTO_INCREMENT,
    `idProveedor` INT NOT NULL,
    `idUsuario` INT NOT NULL,
    `nroComprobante` VARCHAR(50) NOT NULL,
    `fechaCompra` DATETIME NOT NULL,
    `total` DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (`idCompra`),
    CONSTRAINT `fk_Compra_Proveedor` FOREIGN KEY (`idProveedor`) REFERENCES `Proveedor` (`idProveedor`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_Compra_Usuario` FOREIGN KEY (`idUsuario`) REFERENCES `Usuario` (`idUsuario`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 10. DetalleCompra
CREATE TABLE `DetalleCompra` (
    `idDetalleCompra` INT NOT NULL AUTO_INCREMENT,
    `idCompra` INT NOT NULL,
    `idProducto` INT NOT NULL,
    `cantidad` INT NOT NULL,
    `costoUnitario` DECIMAL(10,2) NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (`idDetalleCompra`),
    CONSTRAINT `fk_DetalleCompra_Compra` FOREIGN KEY (`idCompra`) REFERENCES `Compra` (`idCompra`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_DetalleCompra_Producto` FOREIGN KEY (`idProducto`) REFERENCES `Producto` (`idProducto`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 11. TurnoCaja
CREATE TABLE `TurnoCaja` (
    `idTurno` INT NOT NULL AUTO_INCREMENT,
    `idUsuario` INT NOT NULL,
    `fechaHoraApertura` DATETIME NOT NULL,
    `fechaHoraCierre` DATETIME NULL,
    `montoInicial` DECIMAL(10,2) NOT NULL,
    `totalVentasEfectivo` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `totalVentasTransferencia` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `saldoTeoricoSistema` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `saldoRealFisico` DECIMAL(12,2) NULL,
    `diferencia` DECIMAL(10,2) NULL,
    `observaciones` VARCHAR(255) NULL,
    `estado` VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    PRIMARY KEY (`idTurno`),
    CONSTRAINT `fk_TurnoCaja_Usuario` FOREIGN KEY (`idUsuario`) REFERENCES `Usuario` (`idUsuario`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 12. MedioPago
CREATE TABLE `MedioPago` (
    `idMedioPago` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idMedioPago`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 13. Venta
CREATE TABLE `Venta` (
    `idVenta` INT NOT NULL AUTO_INCREMENT,
    `idTurno` INT NOT NULL,
    `idUsuario` INT NOT NULL,
    `idMedioPago` INT NOT NULL,
    `nroTicket` VARCHAR(50) NOT NULL UNIQUE,
    `fechaVenta` DATETIME NOT NULL,
    `total` DECIMAL(12,2) NOT NULL,
    `montoRecibido` DECIMAL(12,2) NULL,
    `vuelto` DECIMAL(10,2) NULL,
    `nroComprobantePago` VARCHAR(60) NULL,
    PRIMARY KEY (`idVenta`),
    CONSTRAINT `fk_Venta_TurnoCaja` FOREIGN KEY (`idTurno`) REFERENCES `TurnoCaja` (`idTurno`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_Venta_Usuario` FOREIGN KEY (`idUsuario`) REFERENCES `Usuario` (`idUsuario`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_Venta_MedioPago` FOREIGN KEY (`idMedioPago`) REFERENCES `MedioPago` (`idMedioPago`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 14. DetalleVenta
CREATE TABLE `DetalleVenta` (
    `idDetalleVenta` INT NOT NULL AUTO_INCREMENT,
    `idVenta` INT NOT NULL,
    `idProducto` INT NOT NULL,
    `renglon` INT NOT NULL,
    `cantidad` INT NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (`idDetalleVenta`),
    CONSTRAINT `fk_DetalleVenta_Venta` FOREIGN KEY (`idVenta`) REFERENCES `Venta` (`idVenta`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_DetalleVenta_Producto` FOREIGN KEY (`idProducto`) REFERENCES `Producto` (`idProducto`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 15. EstadoVenta
CREATE TABLE `EstadoVenta` (
    `idEstadoVenta` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL,
    `descripcion` VARCHAR(150) NULL,
    `fechaAlta` DATETIME NOT NULL,
    `fechaBaja` DATETIME NULL,
    PRIMARY KEY (`idEstadoVenta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- 16. HistorialEstadoVenta
CREATE TABLE `HistorialEstadoVenta` (
    `idHistorial` INT NOT NULL AUTO_INCREMENT,
    `idVenta` INT NOT NULL,
    `idEstadoVenta` INT NOT NULL,
    `fechaDesde` DATETIME NOT NULL,
    `fechaHasta` DATETIME NULL,
    `observaciones` VARCHAR(255) NULL,
    PRIMARY KEY (`idHistorial`),
    CONSTRAINT `fk_Historial_Venta` FOREIGN KEY (`idVenta`) REFERENCES `Venta` (`idVenta`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_Historial_EstadoVenta` FOREIGN KEY (`idEstadoVenta`) REFERENCES `EstadoVenta` (`idEstadoVenta`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;


