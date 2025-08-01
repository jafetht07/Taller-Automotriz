package taller.automotriz.modelo;

import taller.automotriz.utiles.FormateadorFecha;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenTrabajo implements Facturable {
    private int numeroOrden;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private List<Servicio> servicios;
    private LocalDate fechaIngreso;
    private LocalDate fechaEntrega;
    private double costoTotal;
    private EstadoOrden estado;
    private String observaciones;
    private static int contadorOrden = 1;
    
    public enum EstadoOrden {
        ABIERTA("Abierta"),
        CERRADA("Cerrada");
        
        private final String descripcion;
        
        EstadoOrden(String descripcion) {
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
    
    // Constructor para nueva orden
    public OrdenTrabajo(Cliente cliente, Vehiculo vehiculo) {
        this.numeroOrden = contadorOrden++;
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.servicios = new ArrayList<>();
        this.fechaIngreso = LocalDate.now();
        this.estado = EstadoOrden.ABIERTA;
        this.observaciones = "";
        this.costoTotal = 0.0;
    }
    
    // Constructor para cargar desde archivo
    public OrdenTrabajo(int numeroOrden, Cliente cliente, Vehiculo vehiculo, 
                       LocalDate fechaIngreso, LocalDate fechaEntrega, 
                       double costoTotal, EstadoOrden estado, String observaciones) {
        this.numeroOrden = numeroOrden;
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.servicios = new ArrayList<>();
        this.fechaIngreso = fechaIngreso;
        this.fechaEntrega = fechaEntrega;
        this.costoTotal = costoTotal;
        this.estado = estado;
        this.observaciones = observaciones != null ? observaciones : "";
        
        if (numeroOrden >= contadorOrden) {
            contadorOrden = numeroOrden + 1;
        }
    }
    
    // Getters y setters
    public int getNumeroOrden() { return numeroOrden; }
    
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
    
    public List<Servicio> getServicios() { return servicios; }
    
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    
    public double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(double costoTotal) { this.costoTotal = costoTotal; }
    
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { 
        this.observaciones = observaciones != null ? observaciones : ""; 
    }
    
    // Métodos para manejar servicios
    public void agregarServicio(Servicio servicio) {
        if (servicio != null && !servicios.contains(servicio)) {
            servicios.add(servicio);
            calcularTotal();
        }
    }
    
    public void removerServicio(Servicio servicio) {
        if (servicios.remove(servicio)) {
            calcularTotal();
        }
    }
    
    public void limpiarServicios() {
        servicios.clear();
        calcularTotal();
    }
    
    // Implementación de la interfaz Facturable
    @Override
    public double calcularTotal() {
        costoTotal = 0.0;
        for (Servicio servicio : servicios) {
            costoTotal += servicio.getCosto();
        }
        return costoTotal;
    }
    
    @Override
    public String generarFactura() {
        StringBuilder factura = new StringBuilder();
        factura.append("=== FACTURA ORDEN DE TRABAJO ===\n");
        factura.append("Número de Orden: ").append(numeroOrden).append("\n");
        factura.append("Cliente: ").append(cliente.getNombre()).append("\n");
        factura.append("Teléfono: ").append(cliente.getTelefono()).append("\n");
        factura.append("Vehículo: ").append(vehiculo.toString()).append("\n");
        factura.append("Fecha Ingreso: ").append(FormateadorFecha.formatearParaMostrar(fechaIngreso)).append("\n");
        
        if (fechaEntrega != null) {
            factura.append("Fecha Entrega: ").append(FormateadorFecha.formatearParaMostrar(fechaEntrega)).append("\n");
        }
        
        factura.append("Estado: ").append(estado.getDescripcion()).append("\n");
        factura.append("\nServicios:\n");
        
        for (Servicio servicio : servicios) {
            factura.append("- ").append(servicio.getNombreServicio())
                   .append(" (").append(servicio.getTipo().getDescripcion()).append(")")
                   .append(" - $").append(String.format("%.2f", servicio.getCosto())).append("\n");
        }
        
        if (!observaciones.isEmpty()) {
            factura.append("\nObservaciones: ").append(observaciones).append("\n");
        }
        
        factura.append("\nTOTAL: $").append(String.format("%.2f", costoTotal));
        return factura.toString();
    }
    
    @Override
    public void aplicarDescuento(double porcentaje) {
        if (porcentaje >= 0 && porcentaje <= 100) {
            costoTotal = costoTotal * (1 - porcentaje / 100);
        }
    }
    
    // Método para cerrar la orden
    public void cerrarOrden() {
        this.estado = EstadoOrden.CERRADA;
        if (this.fechaEntrega == null) {
            this.fechaEntrega = LocalDate.now();
        }
    }
    
    // Método para reabrir la orden (si es necesario)
    public void reabrirOrden() {
        this.estado = EstadoOrden.ABIERTA;
        this.fechaEntrega = null;
    }
    
    // Verificar si se puede modificar
    public boolean puedeModificar() {
        return estado == EstadoOrden.ABIERTA;
    }
    
    @Override
    public String toString() {
        return "Orden #" + numeroOrden + " - " + cliente.getNombre() + " (" + estado.getDescripcion() + ")";
    }
    
    // Método para convertir a CSV
    public String toCSV() {
        return numeroOrden + "," + 
               cliente.getIdCliente() + "," + 
               vehiculo.getPlaca() + "," +
               FormateadorFecha.formatearParaArchivo(fechaIngreso) + "," + 
               (fechaEntrega != null ? FormateadorFecha.formatearParaArchivo(fechaEntrega) : "") + "," +
               costoTotal + "," + 
               estado.name() + "," + 
               (observaciones != null ? observaciones.replace(",", ";") : "");
    }
}
