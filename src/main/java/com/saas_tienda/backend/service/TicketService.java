package com.saas_tienda.backend.service;

import com.saas_tienda.backend.domain.Venta;
import com.saas_tienda.backend.domain.VentaDetalle;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private static final int ANCHO = 32;
    private static final DateTimeFormatter FECHA_TICKET = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"));

    public String crearTicket(Venta venta, BigDecimal pagoCon, BigDecimal cambio) {
        StringBuilder ticket = new StringBuilder();
        ticket.append(centrar(venta.getTienda().getNombre())).append('\n');
        if (venta.getTienda().getDireccion() != null && !venta.getTienda().getDireccion().isBlank()) {
            ticket.append(centrar(venta.getTienda().getDireccion())).append('\n');
        }
        ticket.append(linea()).append('\n');
        ticket.append("Folio: ").append(folio(venta)).append('\n');
        ticket.append("Fecha: ").append(FECHA_TICKET.format(venta.getFecha())).append('\n');
        ticket.append("Cajero: ").append(venta.getUsuario().getNombre()).append('\n');
        ticket.append(linea()).append('\n');
        for (VentaDetalle detalle : venta.getDetalles()) {
            ticket.append(recortar(detalle.getNombreConcepto(), ANCHO)).append('\n');
            String cantidadPrecio = detalle.getCantidad() + " x " + MONEDA.format(detalle.getPrecioUnitario());
            ticket.append(dosColumnas(cantidadPrecio, MONEDA.format(detalle.getSubtotal()))).append('\n');
        }
        ticket.append(linea()).append('\n');
        ticket.append(dosColumnas("TOTAL", MONEDA.format(venta.getTotal()))).append('\n');
        if (!venta.getPagos().isEmpty()) {
            venta.getPagos().forEach(pago ->
                    ticket.append(dosColumnas(pago.getMetodoPago().name(), MONEDA.format(pago.getMonto()))).append('\n'));
        } else if (pagoCon != null) {
            ticket.append(dosColumnas("PAGO", MONEDA.format(pagoCon))).append('\n');
        }
        if (cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
            ticket.append(dosColumnas("CAMBIO", MONEDA.format(cambio))).append('\n');
        }
        ticket.append(linea()).append('\n');
        ticket.append(centrar("Gracias por su compra")).append('\n');
        return ticket.toString();
    }

    public String folio(Venta venta) {
        return "V" + String.format("%08d", venta.getId());
    }

    private String linea() {
        return "-".repeat(ANCHO);
    }

    private String centrar(String texto) {
        String limpio = recortar(texto, ANCHO);
        int espacios = Math.max(0, (ANCHO - limpio.length()) / 2);
        return " ".repeat(espacios) + limpio;
    }

    private String dosColumnas(String izquierda, String derecha) {
        String izq = recortar(izquierda, ANCHO);
        String der = recortar(derecha, ANCHO);
        int espacios = Math.max(1, ANCHO - izq.length() - der.length());
        return izq + " ".repeat(espacios) + der;
    }

    private String recortar(String texto, int longitud) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.replaceAll("[\\r\\n\\t]", " ").trim();
        return limpio.length() <= longitud ? limpio : limpio.substring(0, longitud);
    }
}
