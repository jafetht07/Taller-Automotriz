package taller.automotriz.persistencia;

import taller.automotriz.modelo.Servicio;
import taller.automotriz.excepciones.ArchivoException;
import java.util.ArrayList;
import java.util.List;

public class ServicioDAO {
    private static final String ARCHIVO_SERVICIOS = "servicios.csv";
    private List<Servicio> servicios;
    
    public ServicioDAO() {
        this.servicios = new ArrayList<>();
    }
    
    public void guardar(Servicio servicio) throws ArchivoException {
        if (servicio != null) {
            servicios.add(servicio);
            guardarEnArchivo();
        }
    }
    
    public Servicio buscarPorId(int id) {
        for (Servicio servicio : servicios) {
            if (servicio.getIdServicio() == id) {
                return servicio;
            }
        }
        return null;
    }
    
    public List<Servicio> obtenerTodos() {
        return new ArrayList<>(servicios);
    }
    
    public List<Servicio> obtenerPorTipo(Servicio.TipoServicio tipo) {
        List<Servicio> serviciosFiltrados = new ArrayList<>();
        for (Servicio servicio : servicios) {
            if (servicio.getTipo() == tipo) {
                serviciosFiltrados.add(servicio);
            }
        }
        return serviciosFiltrados;
    }
    
    public List<Servicio> buscarPorNombre(String nombre) {
        List<Servicio> serviciosEncontrados = new ArrayList<>();
        for (Servicio servicio : servicios) {
            if (servicio.getNombreServicio().toLowerCase().contains(nombre.toLowerCase())) {
                serviciosEncontrados.add(servicio);
            }
        }
        return serviciosEncontrados;
    }
    
    public void actualizar(Servicio servicioActualizado) throws ArchivoException {
        if (servicioActualizado != null) {
            for (int i = 0; i < servicios.size(); i++) {
                if (servicios.get(i).getIdServicio() == servicioActualizado.getIdServicio()) {
                    servicios.set(i, servicioActualizado);
                    guardarEnArchivo();
                    return;
                }
            }
        }
    }
    
    public boolean eliminar(int id) throws ArchivoException {
        boolean eliminado = servicios.removeIf(s -> s.getIdServicio() == id);
        if (eliminado) {
            guardarEnArchivo();
        }
        return eliminado;
    }
    
    public void cargarDesdeArchivo() throws ArchivoException {
        List<String> lineas = ManejadorArchivos.leerArchivo(ARCHIVO_SERVICIOS);
        servicios.clear();
        
        for (String linea : lineas) {
            try {
                Servicio servicio = Servicio.fromCSV(linea);
                if (servicio != null) {
                    servicios.add(servicio);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar servicio: " + linea + " - " + e.getMessage());
            }
        }
    }
    
    private void guardarEnArchivo() throws ArchivoException {
        List<String> lineas = new ArrayList<>();
        for (Servicio servicio : servicios) {
            lineas.add(servicio.toCSV());
        }
        ManejadorArchivos.escribirArchivo(ARCHIVO_SERVICIOS, lineas);
    }
    
    public int obtenerCantidad() {
        return servicios.size();
    }
    
    public boolean existe(int id) {
        return buscarPorId(id) != null;
    }
    
    public double obtenerCostoPromedio() {
        if (servicios.isEmpty()) {
            return 0.0;
        }
        
        double suma = 0.0;
        for (Servicio servicio : servicios) {
            suma += servicio.getCosto();
        }
        return suma / servicios.size();
    }
}
