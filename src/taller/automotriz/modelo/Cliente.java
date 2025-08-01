package taller.automotriz.modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Persona {
    private int idCliente;
    private static int contadorId = 1;
    private List<Vehiculo> vehiculos;
    
    public Cliente(String nombre, String telefono, String direccion) {
        super(nombre, telefono, direccion);
        this.idCliente = contadorId++;
        this.vehiculos = new ArrayList<>();
    }
    
    public Cliente(int idCliente, String nombre, String telefono, String direccion) {
        super(nombre, telefono, direccion);
        this.idCliente = idCliente;
        this.vehiculos = new ArrayList<>();
        if (idCliente >= contadorId) {
            contadorId = idCliente + 1;
        }
    }
    
    @Override
    public String getIdentificacion() {
        return "CLI-" + String.format("%04d", idCliente);
    }
    
    // TODOS LOS MÉTODOS PUBLIC
    public int getIdCliente() { return idCliente; }
    
    public List<Vehiculo> getVehiculos() { return vehiculos; }
    
    public void agregarVehiculo(Vehiculo vehiculo) {
        if (vehiculo != null && !vehiculos.contains(vehiculo)) {
            vehiculos.add(vehiculo);
            vehiculo.setCliente(this);
        }
    }
    
    public void removerVehiculo(Vehiculo vehiculo) {
        vehiculos.remove(vehiculo);
    }
    
    @Override
    public String toString() {
        return getIdentificacion() + " - " + nombre;
    }
    
    public String toCSV() {
        return idCliente + "," + nombre + "," + telefono + "," + direccion;
    }
    
    public static Cliente fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 4) {
            return new Cliente(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3]);
        }
        return null;
    }
}