package taller.automotriz.persistencia;

import taller.automotriz.excepciones.ArchivoException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ManejadorArchivos {
    private static final String DIRECTORIO_DATOS = "datos/";
    
    static {
        crearDirectorioSiNoExiste();
    }
    
    private static void crearDirectorioSiNoExiste() {
        File directorio = new File(DIRECTORIO_DATOS);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }
    
    public static void escribirArchivo(String nombreArchivo, List<String> lineas) throws ArchivoException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DIRECTORIO_DATOS + nombreArchivo))) {
            for (String linea : lineas) {
                writer.println(linea);
            }
        } catch (IOException e) {
            throw new ArchivoException("Error al escribir archivo " + nombreArchivo + ": " + e.getMessage());
        }
    }
    
    public static List<String> leerArchivo(String nombreArchivo) throws ArchivoException {
        List<String> lineas = new ArrayList<>();
        File archivo = new File(DIRECTORIO_DATOS + nombreArchivo);
        
        if (!archivo.exists()) {
            return lineas; // Retorna lista vacía si el archivo no existe
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            throw new ArchivoException("Error al leer archivo " + nombreArchivo + ": " + e.getMessage());
        }
        
        return lineas;
    }
    
    public static boolean existeArchivo(String nombreArchivo) {
        return new File(DIRECTORIO_DATOS + nombreArchivo).exists();
    }
    
    public static void eliminarArchivo(String nombreArchivo) throws ArchivoException {
        File archivo = new File(DIRECTORIO_DATOS + nombreArchivo);
        if (archivo.exists() && !archivo.delete()) {
            throw new ArchivoException("No se pudo eliminar el archivo " + nombreArchivo);
        }
    }
    
    public static String obtenerRutaCompleta(String nombreArchivo) {
        return DIRECTORIO_DATOS + nombreArchivo;
    }
}
