package com.jrmarket.util;

import com.jrmarket.model.DetalleVenta;
import com.jrmarket.model.Producto;
import com.jrmarket.model.TurnoCaja;
import com.jrmarket.model.Venta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

/**
 * Utilidad encargada de la manipulación de archivos de texto (.txt) mediante el paquete java.io.
 * Permite exportar comprobantes de venta, listas de reposición y reportes de arqueo de caja.
 */
public final class GestorArchivos {

    private static final String CARPETA_REPORTES = "reportes_jrmarket";

    private GestorArchivos() {}

    /**
     * Emite un comprobante de venta en formato de texto utilizando File, FileOutputStream y PrintStream.
     */
    public static File exportarTicketVenta(Venta venta) throws IOException {
        File directorio = asegurarDirectorio();
        String nombreArchivo = "ticket_" + venta.getNroTicket().replaceAll("[^a-zA-Z0-9_-]", "_") + ".txt";
        File archivoTicket = new File(directorio, nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(archivoTicket);
             PrintStream ps = new PrintStream(fos, true, StandardCharsets.UTF_8)) {

            ps.println("================================================");
            ps.println("           JRMARKET - COMPROBANTE POS           ");
            ps.println("================================================");
            ps.println("Ticket Nro : " + venta.getNroTicket());
            ps.println("Fecha/Hora : " + venta.getFechaHora());
            ps.println("Operador   : " + (venta.getUsuario() != null ? venta.getUsuario().getNombreCompleto() : "Operador"));
            ps.println("Medio Pago : " + venta.getMedioPago());
            ps.println("------------------------------------------------");
            ps.println("CANT  DESCRIPCIÓN                      SUBTOTAL ");
            ps.println("------------------------------------------------");

            Iterator<DetalleVenta> it = venta.getDetalles().iterator();
            while (it.hasNext()) {
                DetalleVenta dv = it.next();
                String desc = dv.getProducto().getDescripcion();
                if (desc.length() > 28) {
                    desc = desc.substring(0, 28);
                }
                ps.printf("%-5d %-30s $ %8s%n",
                        dv.getCantidad(),
                        desc,
                        FormatoImporte.formatear(dv.getSubtotal()));
            }

            ps.println("------------------------------------------------");
            ps.println("TOTAL COBRADO: $ " + FormatoImporte.formatear(venta.getTotal()));
            if (venta.getMedioPago() != null && venta.getMedioPago().toUpperCase().contains("EFECTIVO")) {
                ps.println("PAGA CON     : $ " + FormatoImporte.formatear(venta.getMontoRecibido()));
                ps.println("SU VUELTO    : $ " + FormatoImporte.formatear(venta.getVuelto()));
            } else {
                ps.println("COMPROBANTE  : " + venta.getComprobantePago());
            }
            ps.println("================================================");
        }
        return archivoTicket;
    }

    /**
     * Exporta la lista sugerida de compras para reposición usando File, FileWriter y BufferedWriter.
     */
    public static File exportarListaFaltantes(List<Producto> faltantes) throws IOException {
        File directorio = asegurarDirectorio();
        File archivoLista = new File(directorio, "lista_compras_sugerida.txt");

        try (FileWriter fw = new FileWriter(archivoLista, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("==========================================================");
            bw.newLine();
            bw.write("       JRMARKET - LISTA SUGERIDA DE COMPRAS (STOCK)       ");
            bw.newLine();
            bw.write("==========================================================");
            bw.newLine();
            bw.write("Fecha de emisión: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            bw.newLine();
            bw.write("Artículos en estado crítico: " + faltantes.size());
            bw.newLine();
            bw.write("----------------------------------------------------------");
            bw.newLine();

            Iterator<Producto> it = faltantes.iterator();
            while (it.hasNext()) {
                Producto p = it.next();
                int sugerido = p.getStockMinimo() * 2 - p.getStockActual();
                bw.write("Código: " + p.getCodigoBarras() + " | " + p.getDescripcion()
                        + " (" + p.getMarca().getNombre() + ")");
                bw.newLine();
                bw.write("   Stock Actual: " + p.getStockActual()
                        + " | Stock Mínimo: " + p.getStockMinimo()
                        + " | Compra Sugerida: " + sugerido + " u.");
                bw.newLine();
                bw.write("----------------------------------------------------------");
                bw.newLine();
            }
            bw.flush();
        }
        return archivoLista;
    }

    /**
     * Exporta el informe de arqueo y cierre de turno en un archivo de texto plano.
     */
    public static File exportarArqueoTurno(TurnoCaja turno) throws IOException {
        File directorio = asegurarDirectorio();
        File archivoArqueo = new File(directorio, "arqueo_turno_" + turno.getIdTurno() + ".txt");

        try (FileWriter fw = new FileWriter(archivoArqueo, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("================================================");
            bw.newLine();
            bw.write("      JRMARKET - CIERRE Y ARQUEO DE TURNO       ");
            bw.newLine();
            bw.write("================================================");
            bw.newLine();
            bw.write("Turno Nro        : #" + turno.getIdTurno());
            bw.newLine();
            bw.write("Operador         : " + (turno.getUsuario() != null ? turno.getUsuario().getNombreCompleto() : "-"));
            bw.newLine();
            bw.write("Apertura         : " + turno.getFechaHoraApertura());
            bw.newLine();
            bw.write("Cierre           : " + turno.getFechaHoraCierre());
            bw.newLine();
            bw.write("Fondo Inicial    : $ " + FormatoImporte.formatear(turno.getMontoInicial()));
            bw.newLine();
            bw.write("Ventas Efectivo  : $ " + FormatoImporte.formatear(turno.getTotalVentasEfectivo()));
            bw.newLine();
            bw.write("Ventas Transf.   : $ " + FormatoImporte.formatear(turno.getTotalVentasTransferencia()));
            bw.newLine();
            bw.write("Saldo Teórico    : $ " + FormatoImporte.formatear(turno.getSaldoTeoricoSistema()));
            bw.newLine();
            bw.write("Saldo Físico Real: $ " + FormatoImporte.formatear(turno.getSaldoRealFisico()));
            bw.newLine();
            bw.write("Diferencia Total : $ " + FormatoImporte.formatear(turno.getDiferencia()));
            bw.newLine();
            bw.write("Observaciones    : " + (turno.getObservaciones() != null ? turno.getObservaciones() : "Sin observaciones"));
            bw.newLine();
            bw.write("================================================");
            bw.newLine();
            bw.flush();
        }
        return archivoArqueo;
    }

    private static File asegurarDirectorio() throws IOException {
        File dir = new File(CARPETA_REPORTES);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio de reportes: " + dir.getAbsolutePath());
        }
        return dir;
    }
}