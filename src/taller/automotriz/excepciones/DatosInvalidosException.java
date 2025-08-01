package taller.automotriz.excepciones;

public class DatosInvalidosException extends Exception {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
    
    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

