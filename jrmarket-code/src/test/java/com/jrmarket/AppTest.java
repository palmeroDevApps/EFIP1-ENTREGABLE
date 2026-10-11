package com.jrmarket;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.jrmarket.model.Compra;
import com.jrmarket.model.DetalleCompra;
import com.jrmarket.model.DetalleVenta;
import com.jrmarket.model.Producto;
import com.jrmarket.model.Rol;
import com.jrmarket.model.TurnoCaja;
import com.jrmarket.model.Usuario;
import com.jrmarket.model.Venta;
import com.jrmarket.util.FormatoImporte;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test(expected = IllegalArgumentException.class)
    public void ventaNoPermiteSuperarStockConAgregadosRepetidos() {
        Producto producto = producto(5);
        Venta venta = new Venta(1, "T-1", "2026-10-09", null, "Efectivo",
            turnoAbierto(0.0));

        venta.agregarItem(producto, 4);
        venta.agregarItem(producto, 2);
    }

    @Test
    public void confirmarVentaDescuentaStockYConservaCostoHistorico() {
        Producto producto = producto(5);
        TurnoCaja turno = turnoAbierto(100.0);
        Venta venta = new Venta(1, "T-1", "2026-10-09", null, "Efectivo", turno);
        venta.agregarItem(producto, 2);
        venta.liquidarVuelto(30.0);

        venta.confirmarVenta();

        assertEquals(3, producto.getStockActual());
        assertEquals(6.0, venta.getDetalles().get(0).getCostoHistorico(), 0.001);
        assertEquals(20.0, turno.getTotalVentasEfectivo(), 0.001);
        assertEquals(1, turno.getVentas().size());
    }

    @Test
    public void actualizarCantidadDelRenglonRecalculaSubtotalYTotal() {
        Producto producto = producto(5);
        Venta venta = new Venta(1, "T-1", "2026-10-09", null, "Efectivo",
            turnoAbierto(0.0));
        venta.agregarItem(producto, 1);
        DetalleVenta detalle = venta.getDetalles().get(0);

        venta.actualizarCantidad(detalle, 3);

        assertEquals(30.0, detalle.getSubtotal(), 0.001);
        assertEquals(30.0, venta.getTotal(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void actualizarCantidadDelRenglonNoPermiteSuperarStock() {
        Producto producto = producto(5);
        Venta venta = new Venta(1, "T-1", "2026-10-09", null, "Efectivo",
            turnoAbierto(0.0));
        venta.agregarItem(producto, 1);

        venta.actualizarCantidad(venta.getDetalles().get(0), 6);
    }

    @Test
    public void conciliacionComparaEfectivoYTransferenciasPorSeparado() {
        TurnoCaja turno = turnoAbierto(100.0);
        turno.setSaldoTeoricoSistema(150.0);
        turno.setTotalVentasTransferencia(40.0);

        turno.conciliarSaldo(150.0, 40.0, "Sin diferencias");

        assertEquals(190.0, turno.getSaldoRealFisico(), 0.001);
        assertEquals(0.0, turno.getDiferencia(), 0.001);
    }

    @Test
    public void confirmarCompraActualizaStockYCostoDeReposicion() {
        Producto producto = producto(5);
        Compra compra = new Compra(1, "F-1", "2026-10-09", null, null);
        compra.agregarDetalle(producto, 3, 8.5);

        compra.confirmarIngresoMercaderia();

        assertEquals(8, producto.getStockActual());
        assertEquals(8.5, producto.getPrecioCosto(), 0.001);
    }

    @Test
    public void actualizarCostoUnitarioDelRenglonRecalculaSubtotalYTotal() {
        Producto producto = producto(5);
        Compra compra = new Compra(1, "F-1", "2026-10-09", null, null);
        compra.agregarDetalle(producto, 3, 8.5);
        DetalleCompra detalle = compra.getDetalles().get(0);

        compra.actualizarCostoUnitario(detalle, 10.0);

        assertEquals(30.0, detalle.getSubtotal(), 0.001);
        assertEquals(30.0, compra.getTotal(), 0.001);
    }

    @Test
    public void autenticarComparaClaveConHashSha256() {
        String hashClaveDemo = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";
        Usuario usuario = new Usuario(1, "prueba", hashClaveDemo, "Usuario de prueba", true,
            new Rol(1, "VENDEDOR", "", "2026-10-09", null));

        assertTrue(usuario.autenticar("1234"));
    }

    @Test
    public void formatoImporteUsaSeparadoresArgentinos() {
        assertEquals("10.500,00", FormatoImporte.formatear(10500.0));
        assertEquals(10500.0, FormatoImporte.parsear("10.500,00"), 0.001);
    }

    @Test
    public void turnoNuevoIniciaCerradoYAbreConMontoManual() {
        TurnoCaja turno = new TurnoCaja(1, null);

        assertEquals("CERRADO", turno.getEstado());
        turno.abrirTurno(25000.0, "2026-10-09 09:00:00");

        assertEquals("ABIERTO", turno.getEstado());
        assertEquals(25000.0, turno.getMontoInicial(), 0.001);
        assertEquals("2026-10-09 09:00:00", turno.getFechaHoraApertura());
    }

    @Test
    public void ventaConCajaCerradaNoDescuentaStock() {
        Producto producto = producto(5);
        TurnoCaja turno = new TurnoCaja(1, null);
        Venta venta = new Venta(1, "T-1", "2026-10-09", null, "Efectivo", turno);
        venta.agregarItem(producto, 1);
        venta.liquidarVuelto(10.0);

        try {
            venta.confirmarVenta();
        } catch (IllegalStateException e) {
            assertEquals(5, producto.getStockActual());
            assertTrue(turno.getVentas().isEmpty());
            return;
        }
        throw new AssertionError("No debería permitirse una venta con la caja cerrada.");
    }

    private Producto producto(int stock) {
        return new Producto(1, "779001", "Producto de prueba", 10.0, 6.0,
            stock, 1, true, "2026-10-09", null, null, null);
    }

    private TurnoCaja turnoAbierto(double montoInicial) {
        TurnoCaja turno = new TurnoCaja(1, null);
        turno.abrirTurno(montoInicial, "2026-10-09 09:00:00");
        return turno;
    }
}
