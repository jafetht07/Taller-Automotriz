package taller.automotriz.modelo;

public interface Facturable {
    double calcularTotal();
    String generarFactura();
    void aplicarDescuento(double porcentaje);
}
