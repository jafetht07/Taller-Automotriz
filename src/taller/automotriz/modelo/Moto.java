package taller.automotriz.modelo;

public class Moto extends Vehiculo {
    private int cilindraje;
    
    public Moto(String marca, String modelo, int año, String placa, int cilindraje) {
        super(marca, modelo, año, placa);
        this.cilindraje = cilindraje;
    }
    
    public Moto(String marca, String modelo, int año, String placa) {
        this(marca, modelo, año, placa, 150); // Default 150cc
    }
    
    @Override
    public String getTipo() {
        return "Moto";
    }
    
    public int getCilindraje() { return cilindraje; }
    public void setCilindraje(int cilindraje) { this.cilindraje = cilindraje; }
}
