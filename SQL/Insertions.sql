USE `jrmarket`;

-- Inserción de Roles y Usuarios
INSERT INTO `Rol` (`idRol`, `nombre`, `descripcion`, `fechaAlta`) VALUES
(1, 'Administrador', 'Control total de inventario, caja y usuarios', NOW()),
(2, 'Vendedor', 'Atención en mostrador y registro de transacciones', NOW());

INSERT INTO `Usuario` (`idUsuario`, `idRol`, `nombreUsuario`, `contrasenia`, `nombreCompleto`, `estado`, `fechaAlta`) VALUES
(1, 1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'Administrador Kiosco', 1, NOW()),
(2, 2, 'cajero1', '04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb', 'Operador de Turno', 1, NOW());

-- Medios de Pago y Estados
INSERT INTO `MedioPago` (`idMedioPago`, `nombre`, `fechaAlta`) VALUES
(1, 'Efectivo', NOW()),
(2, 'Mercado Pago / Transferencia', NOW());

INSERT INTO `EstadoVenta` (`idEstadoVenta`, `nombre`, `descripcion`, `fechaAlta`) VALUES
(1, 'Cobrada', 'Venta finalizada y liquidada en mostrador', NOW()),
(2, 'Anulada', 'Comprobante cancelado por error o devolución', NOW());

-- Clasificación de Artículos
INSERT INTO `Marca` (`idMarca`, `nombre`, `fechaAlta`) VALUES
(1, 'Arcor', NOW()),
(2, 'Coca-Cola', NOW()),
(3, 'Bic', NOW()),
(4, 'Guaymallén', NOW());

INSERT INTO `TipoProducto` (`idTipoProducto`, `nombre`, `descripcion`, `fechaAlta`) VALUES
(1, 'Golosinas', 'Chocolates, alfajores y caramelos', NOW()),
(2, 'Bebidas', 'Gaseosas, aguas y jugos refrigerados', NOW()),
(3, 'Librería', 'Artículos escolares y de oficina', NOW());

-- Productos (Catálogo con artículos sobre stock mínimo y en stock crítico)
INSERT INTO `Producto` (`idProducto`, `idMarca`, `idTipoProducto`, `codigoBarras`, `descripcion`, `precio`, `stockActual`, `stockMinimo`, `fechaDesde`) VALUES
(1, 4, 1, '7790040134011', 'Alfajor Guaymallén Triple Chocolate', 500.00, 24, 10, NOW()),
(2, 2, 2, '7791813421153', 'Gaseosa Coca Cola 500ml', 1200.00, 3, 6, NOW()),       -- Caso: Stock Crítico
(3, 1, 1, '7790580123456', 'Caramelos Menthoplus Menta Fuerte', 450.00, 2, 5, NOW()),     -- Caso: Stock Crítico
(4, 3, 3, '7033061234567', 'Bolígrafo Bic Cristal Azul', 350.00, 15, 5, NOW());

-- Apertura de Turno de Caja (Fondo fijo de $20.000)
INSERT INTO `TurnoCaja` (`idTurno`, `idUsuario`, `fechaHoraApertura`, `montoInicial`, `saldoTeoricoSistema`, `estado`) VALUES
(1, 2, NOW(), 20000.00, 20000.00, 'ABIERTO');

-- Registro de Ventas de Prueba
-- Venta 1: En efectivo (Ticket 0001-00000001)
INSERT INTO `Venta` (`idVenta`, `idTurno`, `idUsuario`, `idMedioPago`, `nroTicket`, `fechaVenta`, `total`, `montoRecibido`, `vuelto`) VALUES
(1, 1, 2, 1, 'TK-0001-00000001', NOW(), 1000.00, 2000.00, 1000.00);

INSERT INTO `DetalleVenta` (`idDetalleVenta`, `idVenta`, `idProducto`, `renglon`, `cantidad`, `subtotal`) VALUES
(1, 1, 1, 1, 2, 1000.00); -- 2 Guaymallén a $500

INSERT INTO `HistorialEstadoVenta` (`idHistorial`, `idVenta`, `idEstadoVenta`, `fechaDesde`, `observaciones`) VALUES
(1, 1, 1, NOW(), 'Venta completada en mostrador');

-- Venta 2: Por transferencia MP (Ticket 0001-00000002)
INSERT INTO `Venta` (`idVenta`, `idTurno`, `idUsuario`, `idMedioPago`, `nroTicket`, `fechaVenta`, `total`, `nroComprobantePago`) VALUES
(2, 1, 2, 2, 'TK-0001-00000002', NOW(), 1200.00, 'MP-8923471023');

INSERT INTO `DetalleVenta` (`idDetalleVenta`, `idVenta`, `idProducto`, `renglon`, `cantidad`, `subtotal`) VALUES
(2, 2, 2, 1, 1, 1200.00); -- 1 Coca Cola a $1200

INSERT INTO `HistorialEstadoVenta` (`idHistorial`, `idVenta`, `idEstadoVenta`, `fechaDesde`, `observaciones`) VALUES
(2, 2, 1, NOW(), 'Pago verificado mediante billetera virtual');

