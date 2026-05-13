package com.saas_tienda.backend.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImpresionTicketService {
    private static final byte[] INICIALIZAR = new byte[] {0x1B, 0x40};
    private static final byte[] CORTAR_PAPEL = new byte[] {0x1D, 0x56, 0x00};
    private static final byte[] ABRIR_CAJON = new byte[] {0x1B, 0x70, 0x00, 0x19, (byte) 0xFA};
    private static final Charset TICKET_CHARSET = StandardCharsets.ISO_8859_1;

    private final String impresoraConfigurada;

    public ImpresionTicketService(@Value("${app.impresora-termica:}") String impresoraConfigurada) {
        this.impresoraConfigurada = impresoraConfigurada;
    }

    public ResultadoImpresion imprimir(String contenido, boolean abrirCajon) {
        PrintService printer = resolverImpresora();
        if (printer == null) {
            return new ResultadoImpresion(false, false, null, "No se encontro impresora termica/default configurada");
        }
        try {
            List<byte[]> partes = new ArrayList<>();
            partes.add(INICIALIZAR);
            if (abrirCajon) {
                partes.add(ABRIR_CAJON);
            }
            partes.add(contenido.getBytes(TICKET_CHARSET));
            partes.add("\n\n\n".getBytes(StandardCharsets.US_ASCII));
            partes.add(CORTAR_PAPEL);
            imprimirBytes(printer, unir(partes));
            return new ResultadoImpresion(true, abrirCajon, printer.getName(), "Ticket enviado a impresion");
        } catch (Exception ex) {
            return new ResultadoImpresion(false, false, printer.getName(), ex.getMessage());
        }
    }

    public ResultadoImpresion abrirCajon() {
        PrintService printer = resolverImpresora();
        if (printer == null) {
            return new ResultadoImpresion(false, false, null, "No se encontro impresora termica/default configurada");
        }
        try {
            imprimirBytes(printer, unir(List.of(INICIALIZAR, ABRIR_CAJON)));
            return new ResultadoImpresion(false, true, printer.getName(), "Pulso enviado al cajon");
        } catch (Exception ex) {
            return new ResultadoImpresion(false, false, printer.getName(), ex.getMessage());
        }
    }

    private PrintService resolverImpresora() {
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        if (impresoraConfigurada != null && !impresoraConfigurada.isBlank()) {
            String nombre = impresoraConfigurada.trim();
            for (PrintService printer : printers) {
                if (printer.getName().equalsIgnoreCase(nombre)) {
                    return printer;
                }
            }
        }
        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultPrinter != null) {
            return defaultPrinter;
        }
        return printers.length == 0 ? null : printers[0];
    }

    private void imprimirBytes(PrintService printer, byte[] bytes) throws Exception {
        DocPrintJob job = printer.createPrintJob();
        Doc doc = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        job.print(doc, null);
    }

    private byte[] unir(List<byte[]> partes) {
        int total = partes.stream().mapToInt(parte -> parte.length).sum();
        byte[] bytes = new byte[total];
        int offset = 0;
        for (byte[] parte : partes) {
            System.arraycopy(parte, 0, bytes, offset, parte.length);
            offset += parte.length;
        }
        return bytes;
    }

    public record ResultadoImpresion(boolean impreso, boolean cajonAbierto, String impresora, String mensaje) {
    }
}
