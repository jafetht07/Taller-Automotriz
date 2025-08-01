package taller.automotriz.modelo;

public class Auto extends Vehiculo {
    private int numeroPuertas;
    
    public Auto(String marca, String modelo, int año, String placa, int numeroPuertas) {
        super(marca, modelo, año, placa);
        this.numeroPuertas = numeroPuertas;
    }
    
    public Auto(String marca, String modelo, int año, String placa) {
        this(marca, modelo, año, placa, 4); // Default 4 puertas
    }
    
    @Override
    public String getTipo() {
        return "Auto";
    }
    
    public int getNumeroPuertas() { return numeroPuertas; }
    public void setNumeroPuertas(int numeroPuertas) { this.numeroPuertas = numeroPuertas; }
}
