package com.jrmarket.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Locale;

/** Convierte importes entre valores numéricos y el formato habitual argentino. */
public final class FormatoImporte {
    private static final DecimalFormatSymbols SIMBOLOS =
        DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-AR"));

    private FormatoImporte() {}

    /** Muestra dos decimales, punto de miles y coma decimal. */
    public static String formatear(double importe) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", SIMBOLOS);
        formato.setGroupingUsed(true);
        return formato.format(importe);
    }

    /** Convierte texto local, como "10.500,00", a double o rechaza el formato. */
    public static double parsear(String texto) {
        String entrada = texto == null ? "" : texto.trim();
        DecimalFormat formato = new DecimalFormat("#,##0.################", SIMBOLOS);
        formato.setParseBigDecimal(true);
        ParsePosition posicion = new ParsePosition(0);
        Number numero = formato.parse(entrada, posicion);
        if (numero == null || posicion.getIndex() != entrada.length()) {
            throw new IllegalArgumentException("El importe no tiene un formato válido.");
        }
        double importe = numero.doubleValue();
        if (!Double.isFinite(importe)) {
            throw new IllegalArgumentException("El importe debe ser un número finito.");
        }
        return importe;
    }
}
