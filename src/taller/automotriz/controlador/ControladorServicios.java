// ===== ARCHIVO: ControladorServicios.java =====
// Package: taller.automotriz.controlador

package taller.automotriz.controlador;

import java.awt.HeadlessException;
import taller.automotriz.modelo.Servicio;
import taller.automotriz.persistencia.ServicioDAO;
import taller.automotriz.excepciones.DatosInvalidosException;
import taller.automotriz.utiles.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import taller.automotriz.excepciones.ArchivoException;

public class ControladorServicios {
    private final ServicioDAO servicioDAO;
    private DefaultTableModel modeloTabla;
    
    public ControladorServicios() {
        this.servicioDAO = new ServicioDAO();
        cargarServicios();
    }
    
    public ControladorServicios(ServicioDAO servicioDAO) {
    this.servicioDAO = servicioDAO;
    
    }
    
    public void configurarTabla(JTable tabla) {
        String[] columnas = {"ID", "Servicio", "Tipo", "Costo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setModel(modeloTabla);
        actualizarTabla();
    }
    
    public void registrarServicio(JTextField txtServicios, JComboBox<String> comboTipos, JTextField txtCosto) 
            throws DatosInvalidosException {
        try {
            String nombreServicio = txtServicios.getText().trim();
            String tipoStr = (String) comboTipos.getSelectedItem();
            String costoStr = txtCosto.getText().trim();
            
            // Validaciones
            if (!Validador.esTextoValido(nombreServicio)) {
                throw new DatosInvalidosException("El nombre del servicio es obligatorio");
            }
            if (tipoStr == null) {
                throw new DatosInvalidosException("Debe seleccionar un tipo de servicio");
            }
            if (!Validador.esNumeroValido(costoStr)) {
                throw new DatosInvalidosException("El costo debe ser un número válido");
            }
            
            double costo = Double.parseDouble(costoStr);
            if (costo <= 0) {
                throw new DatosInvalidosException("El costo debe ser mayor a cero");
            }
            
            // Convertir la descripción del combo al enum
            Servicio.TipoServicio tipo = convertirStringATipo(tipoStr);
            Servicio servicio = new Servicio(nombreServicio, tipo, costo);
            
            servicioDAO.guardar(servicio);
            
            limpiarCampos(txtServicios, txtCosto);
            comboTipos.setSelectedIndex(0);
            actualizarTabla();
            
            JOptionPane.showMessageDialog(null, 
                "Servicio registrado exitosamente\nID: " + servicio.getIdServicio(),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El costo debe ser un número válido");
        } catch (HeadlessException | ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al registrar servicio: " + e.getMessage());
        }
    }
    
    public void consultarServicio(JTable tabla, JTextField txtServicios, JComboBox<String> comboTipos, JTextField txtCosto) {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String servicio = (String) tabla.getValueAt(filaSeleccionada, 1);
            String tipo = (String) tabla.getValueAt(filaSeleccionada, 2);
            String costo = tabla.getValueAt(filaSeleccionada, 3).toString();
            
            txtServicios.setText(servicio);
            txtCosto.setText(costo);
            
            // Seleccionar el tipo en el combo
            for (int i = 0; i < comboTipos.getItemCount(); i++) {
                if (comboTipos.getItemAt(i).equals(tipo)) {
                    comboTipos.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, 
                "Seleccione un servicio de la tabla", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void modificarServicio(JTable tabla, JTextField txtServicios, 
                                JComboBox<String> comboTipos, JTextField txtCosto) 
            throws DatosInvalidosException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un servicio para modificar");
        }
        
        try {
            int id = (Integer) tabla.getValueAt(filaSeleccionada, 0);
            Servicio servicio = servicioDAO.buscarPorId(id);
            
            if (servicio == null) {
                throw new DatosInvalidosException("Servicio no encontrado");
            }
            
            String nombreServicio = txtServicios.getText().trim();
            String tipoStr = (String) comboTipos.getSelectedItem();
            String costoStr = txtCosto.getText().trim();
            
            // Validaciones
            if (!Validador.esTextoValido(nombreServicio)) {
                throw new DatosInvalidosException("El nombre del servicio es obligatorio");
            }
            if (tipoStr == null) {
                throw new DatosInvalidosException("Debe seleccionar un tipo de servicio");
            }
            if (!Validador.esNumeroValido(costoStr)) {
                throw new DatosInvalidosException("El costo debe ser un número válido");
            }
            
            double costo = Double.parseDouble(costoStr);
            if (costo <= 0) {
                throw new DatosInvalidosException("El costo debe ser mayor a cero");
            }
            
            Servicio.TipoServicio tipo = convertirStringATipo(tipoStr);
            
            servicio.setNombreServicio(nombreServicio);
            servicio.setTipo(tipo);
            servicio.setCosto(costo);
            
            servicioDAO.actualizar(servicio);
            
            limpiarCampos(txtServicios, txtCosto);
            comboTipos.setSelectedIndex(0);
            actualizarTabla();
            
            JOptionPane.showMessageDialog(null, 
                "Servicio modificado exitosamente", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El costo debe ser un número válido");
        } catch (HeadlessException | ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al modificar servicio: " + e.getMessage());
        }
    }
    
    public void eliminarServicio(JTable tabla) throws DatosInvalidosException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un servicio para eliminar");
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(null,
            "¿Está seguro de eliminar este servicio?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                int id = (Integer) tabla.getValueAt(filaSeleccionada, 0);
                servicioDAO.eliminar(id);
                actualizarTabla();
                
                JOptionPane.showMessageDialog(null, 
                    "Servicio eliminado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (HeadlessException | ArchivoException e) {
                throw new DatosInvalidosException("Error al eliminar servicio: " + e.getMessage());
            }
        }
    }
    
    // Método para convertir string del combo a TipoServicio
    private Servicio.TipoServicio convertirStringATipo(String tipoStr) {
        return switch (tipoStr) {
            case "Mecánica" -> Servicio.TipoServicio.MECANICA;
            case "Pintura" -> Servicio.TipoServicio.PINTURA;
            case "Revisión" -> Servicio.TipoServicio.REVISION;
            case "Otros" -> Servicio.TipoServicio.OTROS;
            default -> Servicio.TipoServicio.OTROS;
        };
    }
    
    private void cargarServicios() {
        try {
            servicioDAO.cargarDesdeArchivo();
        } catch (ArchivoException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al cargar servicios: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarTabla() {
        if (modeloTabla != null) {
            modeloTabla.setRowCount(0);
            List<Servicio> servicios = servicioDAO.obtenerTodos();
            
            for (Servicio servicio : servicios) {
                Object[] fila = {
                    servicio.getIdServicio(),
                    servicio.getNombreServicio(),
                    servicio.getTipo().getDescripcion(),
                    String.format("%.2f", servicio.getCosto())
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
    private void limpiarCampos(JTextField txtServicios, JTextField txtCosto) {
        txtServicios.setText("");
        txtCosto.setText("");
    }
    
    public List<Servicio> obtenerTodosLosServicios() {
        return servicioDAO.obtenerTodos();
    }
    
    public List<Servicio> obtenerServiciosPorTipo(Servicio.TipoServicio tipo) {
        return servicioDAO.obtenerPorTipo(tipo);
    }
}