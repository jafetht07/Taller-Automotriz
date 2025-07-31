package taller.automotriz.modelo;

public class Servicio {
    private int idServicio;
    private String nombreServicio;
    private TipoServicio tipo;
    private double costo;
    private static int contadorId = 1;
    
    public enum TipoServicio {
        MECANICA("Mecánica"),
        PINTURA("Pintura"),
        REVISION("Revisión"),
        OTROS("Otros");
        
        private final String descripcion;
        
        TipoServicio(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        @Override
        public String toString() {
            return descripcion;
        }
    }
    
    public Servicio(String nombreServicio, TipoServicio tipo, double costo) {
        this.idServicio = contadorId++;
        this.nombreServicio = nombreServicio;
        this.tipo = tipo;
        this.costo = costo;
    }
    
    public Servicio(int idServicio, String nombreServicio, TipoServicio tipo, double costo) {
        this.idServicio = idServicio;
        this.nombreServicio = nombreServicio;
        this.tipo = tipo;
        this.costo = costo;
        if (idServicio >= contadorId) {
            contadorId = idServicio + 1;
        }
    }
    
    // Getters y setters
    public int getIdServicio() { return idServicio; }
    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    
    public TipoServicio getTipo() { return tipo; }
    public void setTipo(TipoServicio tipo) { this.tipo = tipo; }
    
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }
    
    @Override
    public String toString() {
        return nombreServicio + " - $" + costo;
    }
    
    public String toCSV() {
        return idServicio + "," + nombreServicio + "," + tipo.name() + "," + costo;
    }
    
    public static Servicio fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        return new Servicio(
            Integer.parseInt(parts[0]),
            parts[1],
            TipoServicio.valueOf(parts[2]),
            Double.parseDouble(parts[3])
        );
    }
}
