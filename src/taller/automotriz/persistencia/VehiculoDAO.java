package taller.automotriz.persistencia;

import taller.automotriz.modelo.*;
import taller.automotriz.excepciones.ArchivoException;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {
    private static final String ARCHIVO_VEHICULOS = "vehiculos.csv";
    private final List<Vehiculo> vehiculos;
    private final ClienteDAO clienteDAO;
    
    public VehiculoDAO(ClienteDAO clienteDAO) {
        this.vehiculos = new ArrayList<>();
        this.clienteDAO = clienteDAO;
    }
    
    public void guardar(Vehiculo vehiculo) throws ArchivoException {
        if (vehiculo != null) {
            vehiculos.add(vehiculo);
            guardarEnArchivo();
        }
    }
    
    public Vehiculo buscarPorPlaca(String placa) {
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getPlaca().equalsIgnoreCase(placa)) {
                return vehiculo;
            }
        }
        return null;
    }
    
    public List<Vehiculo> obtenerTodos() {
        return new ArrayList<>(vehiculos);
    }
    
    public List<Vehiculo> obtenerPorCliente(Cliente cliente) {
        List<Vehiculo> vehiculosCliente = new ArrayList<>();
        if (cliente != null) {
            for (Vehiculo vehiculo : vehiculos) {
                if (vehiculo.getCliente() != null && 
                    vehiculo.getCliente().getIdCliente() == cliente.getIdCliente()) {
                    vehiculosCliente.add(vehiculo);
                }
            }
        }
        return vehiculosCliente;
    }
    
    public List<Vehiculo> obtenerPorTipo(String tipo) {
        List<Vehiculo> vehiculosTipo = new ArrayList<>();
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getTipo().equalsIgnoreCase(tipo)) {
                vehiculosTipo.add(vehiculo);
            }
        }
        return vehiculosTipo;
    }
    
    public void actualizar(Vehiculo vehiculoActualizado) throws ArchivoException {
        if (vehiculoActualizado != null) {
            for (int i = 0; i < vehiculos.size(); i++) {
                if (vehiculos.get(i).getPlaca().equals(vehiculoActualizado.getPlaca())) {
                    vehiculos.set(i, vehiculoActualizado);
                    guardarEnArchivo();
                    return;
                } else {
                }
            }
        }
    }
    
    public boolean eliminar(String placa) throws ArchivoException {
        boolean eliminado = vehiculos.removeIf(v -> v.getPlaca().equalsIgnoreCase(placa));
        if (eliminado) {
            guardarEnArchivo();
        }
        return eliminado;
    }
    
    public void cargarDesdeArchivo() throws ArchivoException {
        List<String> lineas = ManejadorArchivos.leerArchivo(ARCHIVO_VEHICULOS);
        vehiculos.clear();
        
        for (String linea : lineas) {
            try {
                Vehiculo vehiculo = fromCSV(linea);
                if (vehiculo != null) {
                    vehiculos.add(vehiculo);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar vehículo: " + linea + " - " + e.getMessage());
            }
        }
    }
    
    private Vehiculo fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length < 6) return null;
        
        try {
            String marca = parts[0];
            String modelo = parts[1];
            int año = Integer.parseInt(parts[2]);
            String placa = parts[3];
            String clienteIdStr = parts[4];
            String tipo = parts[5];
            
            Vehiculo vehiculo;
            if ("Auto".equals(tipo)) {
                vehiculo = new Auto(marca, modelo, año, placa);
            } else {
                vehiculo = new Moto(marca, modelo, año, placa);
            }
            
            // Asignar cliente si existe
            if (!clienteIdStr.isEmpty()) {
                int clienteId = Integer.parseInt(clienteIdStr);
                Cliente cliente = clienteDAO.buscarPorId(clienteId);
                if (cliente != null) {
                    vehiculo.setCliente(cliente);
                }
            }
            
            return vehiculo;
            
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void guardarEnArchivo() throws ArchivoException {
        List<String> lineas = new ArrayList<>();
        for (Vehiculo vehiculo : vehiculos) {
            lineas.add(vehiculo.toCSV());
        }
        ManejadorArchivos.escribirArchivo(ARCHIVO_VEHICULOS, lineas);
    }
    
    public int obtenerCantidad() {
        return vehiculos.size();
    }
    
    public boolean existe(String placa) {
        return buscarPorPlaca(placa) != null;
    }
}
