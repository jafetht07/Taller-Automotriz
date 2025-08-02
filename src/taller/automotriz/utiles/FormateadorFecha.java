package taller.automotriz.utiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FormateadorFecha {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_MOSTRAR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static String formatearParaMostrar(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_MOSTRAR) : "";
    }
    
    public static String formatearParaArchivo(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }
    
    public static LocalDate parsearDesdeArchivo(String fechaStr) {
        try {
            return fechaStr != null && !fechaStr.trim().isEmpty() 
                ? LocalDate.parse(fechaStr, FORMATO_FECHA) 
                : null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public static LocalDate parsearDesdeFormulario(String fechaStr) {
        try {
            if (fechaStr == null || fechaStr.trim().isEmpty()) {
                return null;
            }
            
            // Intentar primero con formato yyyy-MM-dd
            try {
                return LocalDate.parse(fechaStr, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                // Si falla, intentar con formato dd/MM/yyyy
                return LocalDate.parse(fechaStr, FORMATO_MOSTRAR);
            }
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    public static String fechaActual() {
        return LocalDate.now().format(FORMATO_FECHA);
    }
    
    public static LocalDate obtenerFechaActual() {
        return LocalDate.now();
    }
}
