// ===== ARCHIVO: OrdenTrabajoDAO.java =====
// Package: taller.automotriz.persistencia

package taller.automotriz.persistencia;

import taller.automotriz.modelo.*;
import taller.automotriz.excepciones.ArchivoException;
import taller.automotriz.utiles.FormateadorFecha;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenTrabajoDAO {
    private static final String ARCHIVO_ORDENES = "ordenes_trabajo.csv";
    private static final String ARCHIVO_SERVICIOS_ORDEN = "servicios_orden.csv";
    private final List<OrdenTrabajo> ordenes;
    private final ClienteDAO clienteDAO;
    private final VehiculoDAO vehiculoDAO;
    private final ServicioDAO servicioDAO;
    
    public OrdenTrabajoDAO(ClienteDAO clienteDAO, VehiculoDAO vehiculoDAO, ServicioDAO servicioDAO) {
        this.ordenes = new ArrayList<>();
        this.clienteDAO = clienteDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.servicioDAO = servicioDAO;
    }
    
    public void guardar(OrdenTrabajo orden) throws ArchivoException {
        if (orden != null) {
            ordenes.add(orden);
            guardarEnArchivo();
            guardarServiciosOrden(orden);
        }
    }
    
    public OrdenTrabajo buscarPorNumero(int numeroOrden) {
        for (OrdenTrabajo orden : ordenes) {
            if (orden.getNumeroOrden() == numeroOrden) {
                return orden;
            }
        }
        return null;
    }
    
    public List<OrdenTrabajo> obtenerTodas() {
        return new ArrayList<>(ordenes);
    }
    
    public List<OrdenTrabajo> obtenerPorCliente(Cliente cliente) {
        List<OrdenTrabajo> ordenesFiltradas = new ArrayList<>();
        if (cliente != null) {
            for (OrdenTrabajo orden : ordenes) {
                if (orden.getCliente().getIdCliente() == cliente.getIdCliente()) {
                    ordenesFiltradas.add(orden);
                }
            }
        }
        return ordenesFiltradas;
    }
    
    public List<OrdenTrabajo> obtenerPorEstado(OrdenTrabajo.EstadoOrden estado) {
        List<OrdenTrabajo> ordenesFiltradas = new ArrayList<>();
        for (OrdenTrabajo orden : ordenes) {
            if (orden.getEstado() == estado) {
                ordenesFiltradas.add(orden);
            }
        }
        return ordenesFiltradas;
    }
    
    public List<OrdenTrabajo> obtenerPorVehiculo(Vehiculo vehiculo) {
        List<OrdenTrabajo> ordenesFiltradas = new ArrayList<>();
        if (vehiculo != null) {
            for (OrdenTrabajo orden : ordenes) {
                if (orden.getVehiculo().getPlaca().equals(vehiculo.getPlaca())) {
                    ordenesFiltradas.add(orden);
                }
            }
        }
        return ordenesFiltradas;
    }
    
    public void actualizar(OrdenTrabajo ordenActualizada) throws ArchivoException {
        if (ordenActualizada != null) {
            for (int i = 0; i < ordenes.size(); i++) {
                if (ordenes.get(i).getNumeroOrden() == ordenActualizada.getNumeroOrden()) {
                    ordenes.set(i, ordenActualizada);
                    guardarEnArchivo();
                    guardarServiciosOrden(ordenActualizada);
                    return;
                }
            }
        }
    }
    
    public boolean eliminar(int numeroOrden) throws ArchivoException {
        boolean eliminado = ordenes.removeIf(o -> o.getNumeroOrden() == numeroOrden);
        if (eliminado) {
            guardarEnArchivo();
            eliminarServiciosOrden(numeroOrden);
        }
        return eliminado;
    }
    
    public void cargarDesdeArchivo() throws ArchivoException {
        List<String> lineas = ManejadorArchivos.leerArchivo(ARCHIVO_ORDENES);
        ordenes.clear();
        
        for (String linea : lineas) {
            try {
                OrdenTrabajo orden = fromCSV(linea);
                if (orden != null) {
                    cargarServiciosOrden(orden);
                    ordenes.add(orden);
                }
            } catch (ArchivoException e) {
                System.err.println("Error al cargar orden: " + linea + " - " + e.getMessage());
            }
        }
    }
    
    private OrdenTrabajo fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 7) return null;
        
        try {
            int numeroOrden = Integer.parseInt(parts[0]);
            int clienteId = Integer.parseInt(parts[1]);
            String placaVehiculo = parts[2];
            LocalDate fechaIngreso = FormateadorFecha.parsearDesdeArchivo(parts[3]);
            LocalDate fechaEntrega = parts[4].isEmpty() ? null : FormateadorFecha.parsearDesdeArchivo(parts[4]);
            double costoTotal = Double.parseDouble(parts[5]);
            OrdenTrabajo.EstadoOrden estado = OrdenTrabajo.EstadoOrden.valueOf(parts[6]);
            String observaciones = parts.length > 7 ? parts[7].replace(";", ",") : "";
            
            Cliente cliente = clienteDAO.buscarPorId(clienteId);
            Vehiculo vehiculo = vehiculoDAO.buscarPorPlaca(placaVehiculo);
            
            if (cliente == null || vehiculo == null) {
                return null;
            }
            
            return new OrdenTrabajo(numeroOrden, cliente, vehiculo, fechaIngreso, 
                                  fechaEntrega, costoTotal, estado, observaciones);
                                  
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void guardarEnArchivo() throws ArchivoException {
        List<String> lineas = new ArrayList<>();
        for (OrdenTrabajo orden : ordenes) {
            lineas.add(orden.toCSV());
        }
        ManejadorArchivos.escribirArchivo(ARCHIVO_ORDENES, lineas);
    }
    
    private void guardarServiciosOrden(OrdenTrabajo orden) throws ArchivoException {
        // Cargar servicios existentes
        List<String> lineasExistentes = new ArrayList<>();
        try {
            lineasExistentes = ManejadorArchivos.leerArchivo(ARCHIVO_SERVICIOS_ORDEN);
        } catch (ArchivoException e) {
            // El archivo no existe aún, está bien
        }
        
        List<String> nuevasLineas = new ArrayList<>();
        
        // Mantener servicios de otras órdenes
        for (String linea : lineasExistentes) {
            if (!linea.startsWith(orden.getNumeroOrden() + ",")) {
                nuevasLineas.add(linea);
            }
        }
        
        // Agregar servicios de esta orden
        for (Servicio servicio : orden.getServicios()) {
            nuevasLineas.add(orden.getNumeroOrden() + "," + servicio.getIdServicio());
        }
        
        ManejadorArchivos.escribirArchivo(ARCHIVO_SERVICIOS_ORDEN, nuevasLineas);
    }
    
    private void cargarServiciosOrden(OrdenTrabajo orden) throws ArchivoException {
        try {
            List<String> lineas = ManejadorArchivos.leerArchivo(ARCHIVO_SERVICIOS_ORDEN);
            
            for (String linea : lineas) {
                String[] parts = linea.split(",");
                if (parts.length >= 2) {
                    try {
                        int numeroOrden = Integer.parseInt(parts[0]);
                        if (numeroOrden == orden.getNumeroOrden()) {
                            int servicioId = Integer.parseInt(parts[1]);
                            Servicio servicio = servicioDAO.buscarPorId(servicioId);
                            if (servicio != null) {
                                orden.agregarServicio(servicio);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar línea inválida
                    }
                }
            }
        } catch (ArchivoException e) {
            // El archivo no existe aún, está bien
        }
    }
    
    private void eliminarServiciosOrden(int numeroOrden) throws ArchivoException {
        try {
            List<String> lineasExistentes = ManejadorArchivos.leerArchivo(ARCHIVO_SERVICIOS_ORDEN);
            List<String> nuevasLineas = new ArrayList<>();
            
            for (String linea : lineasExistentes) {
                if (!linea.startsWith(numeroOrden + ",")) {
                    nuevasLineas.add(linea);
                }
            }
            
            ManejadorArchivos.escribirArchivo(ARCHIVO_SERVICIOS_ORDEN, nuevasLineas);
        } catch (ArchivoException e) {
            // El archivo no existe, está bien
        }
    }
    
    public int obtenerCantidad() {
        return ordenes.size();
    }
    
    public boolean existe(int numeroOrden) {
        return buscarPorNumero(numeroOrden) != null;
    }
    
    public double obtenerIngresosTotales() {
        double total = 0.0;
        for (OrdenTrabajo orden : ordenes) {
            if (orden.getEstado() == OrdenTrabajo.EstadoOrden.CERRADA) {
                total += orden.getCostoTotal();
            }
        }
        return total;
    }
}
