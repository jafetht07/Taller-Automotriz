package taller.automotriz.excepciones;

public class ClienteNoEncontradoException extends Exception {
    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
    
    public ClienteNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}