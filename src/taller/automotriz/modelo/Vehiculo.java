package taller.automotriz.modelo;

public abstract class Vehiculo {
    protected String marca;
    protected String modelo;
    protected int año;
    protected String placa;
    protected Cliente cliente;
    
    public Vehiculo(String marca, String modelo, int año, String placa) {
        this.marca = marca;
        this.modelo = modelo;
        this.año = año;
        this.placa = placa;
    }
    
    // TODOS ESTOS MÉTODOS DEBEN SER PUBLIC
    public String getMarca() { 
        return marca; 
    }
    
    public void setMarca(String marca) { 
        this.marca = marca; 
    }
    
    public String getModelo() { 
        return modelo; 
    }
    
    public void setModelo(String modelo) { 
        this.modelo = modelo; 
    }
    
    public int getAño() { 
        return año; 
    }
    
    public void setAño(int año) { 
        this.año = año; 
    }
    
    // ESTE ES EL MÉTODO QUE ESTÁ CAUSANDO PROBLEMAS
    public String getPlaca() { 
        return placa; 
    }
    
    public void setPlaca(String placa) { 
        this.placa = placa; 
    }
    
    public Cliente getCliente() { 
        return cliente; 
    }
    
    public void setCliente(Cliente cliente) { 
        this.cliente = cliente; 
    }
    
    public abstract String getTipo();
    
    @Override
    public String toString() {
        return marca + " " + modelo + " (" + placa + ")";
    }
    
    public String toCSV() {
        return marca + "," + modelo + "," + año + "," + placa + "," + 
               (cliente != null ? cliente.getIdCliente() : "") + "," + getTipo();
    }
}