package taller.automotriz.utiles;

import java.util.regex.Pattern;

public class Validador {
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\d{8}$");
    private static final Pattern PATRON_PLACA = Pattern.compile("^[A-Z]{3}-\\d{3}$|^[A-Z]{2}-\\d{4}$");
    private static final Pattern PATRON_NUMERO = Pattern.compile("^\\d+(\\.\\d+)?$");
    
    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }
    
    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && PATRON_TELEFONO.matcher(telefono).matches();
    }
    
    public static boolean esPlacaValida(String placa) {
        return placa != null && PATRON_PLACA.matcher(placa.toUpperCase()).matches();
    }
    
    public static boolean esNumeroValido(String numero) {
        return numero != null && PATRON_NUMERO.matcher(numero).matches();
    }
    
    public static boolean esFechaValida(String fecha) {
        // Formato yyyy-MM-dd
        Pattern patron = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        return fecha != null && patron.matcher(fecha).matches();
    }
}
