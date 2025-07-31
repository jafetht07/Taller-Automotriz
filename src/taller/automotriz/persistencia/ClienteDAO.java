package taller.automotriz.persistencia;

import taller.automotriz.modelo.Cliente;
import taller.automotriz.excepciones.ArchivoException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    private static final String ARCHIVO_CLIENTES = "clientes.csv";
    private final List<Cliente> clientes;
    
    public ClienteDAO() {
        this.clientes = new ArrayList<>();
    }
    
    public void guardar(Cliente cliente) throws ArchivoException {
        if (cliente != null) {
            clientes.add(cliente);
            guardarEnArchivo();
        }
    }
    
    public Cliente buscarPorId(int id) {
        for (Cliente cliente : clientes) {
            if (cliente.getIdCliente() == id) {
                return cliente;
            }
        }
        return null;
    }
    
    public Cliente buscarPorNombre(String nombre) {
        for (Cliente cliente : clientes) {
            if (cliente.getNombre().equalsIgnoreCase(nombre)) {
                return cliente;
            } else {
            }
        }
        return null;
    }
    
    public List<Cliente> obtenerTodos() {
        return new ArrayList<>(clientes);
    }
    
    public void actualizar(Cliente clienteActualizado) throws ArchivoException {
        if (clienteActualizado != null) {
            for (int i = 0; i < clientes.size(); i++) {
                if (clientes.get(i).getIdCliente() == clienteActualizado.getIdCliente()) {
                    clientes.set(i, clienteActualizado);
                    guardarEnArchivo();
                    return;
                }
            }
        }
    }
    
    public boolean eliminar(int id) throws ArchivoException {
        boolean eliminado = clientes.removeIf(c -> c.getIdCliente() == id);
        if (eliminado) {
            guardarEnArchivo();
        }
        return eliminado;
    }
    
    public void cargarDesdeArchivo() throws ArchivoException {
        List<String> lineas = ManejadorArchivos.leerArchivo(ARCHIVO_CLIENTES);
        clientes.clear();
        
        for (String linea : lineas) {
            try {
                Cliente cliente = Cliente.fromCSV(linea);
                clientes.add(cliente);
            } catch (Exception e) {
                System.err.println("Error al cargar cliente: " + linea + " - " + e.getMessage());
            }
        }
    }
    
    private void guardarEnArchivo() throws ArchivoException {
        List<String> lineas = new ArrayList<>();
        for (Cliente cliente : clientes) {
            lineas.add(cliente.toCSV());
        }
        ManejadorArchivos.escribirArchivo(ARCHIVO_CLIENTES, lineas);
    }
    
    public int obtenerCantidad() {
        return clientes.size();
    }
    
    public boolean existe(int id) {
        return buscarPorId(id) != null;
    }
}