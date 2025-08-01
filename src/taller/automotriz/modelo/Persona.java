package taller.automotriz.modelo;

public abstract class Persona {
    protected String nombre;
    protected String telefono;
    protected String direccion;
    
    public Persona(String nombre, String telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
    }
    
    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public abstract String getIdentificacion();
    
    @Override
    public String toString() {
        return nombre;
    }
}

