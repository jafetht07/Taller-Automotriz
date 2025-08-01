// ===== ARCHIVO: ControladorVehiculos.java =====
// Package: taller.automotriz.controlador

package taller.automotriz.controlador;

import java.awt.HeadlessException;
import taller.automotriz.modelo.*;
import taller.automotriz.persistencia.VehiculoDAO;
import taller.automotriz.excepciones.DatosInvalidosException;
import taller.automotriz.utiles.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import taller.automotriz.excepciones.ArchivoException;

public class ControladorVehiculos {
    private final VehiculoDAO vehiculoDAO;
    private DefaultTableModel modeloTabla;
    
    public ControladorVehiculos(VehiculoDAO vehiculoDAO) {
        this.vehiculoDAO = vehiculoDAO;
        cargarVehiculos();
    }
    
    public void configurarTabla(JTable tabla) {
        String[] columnas = {"Marca", "Modelo", "Año", "Placa", "Cliente", "Tipo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setModel(modeloTabla);
        actualizarTabla();
    }
    
    public void registrarVehiculo(JTextField txtMarca, JTextField txtModelo, JTextField txtAño, 
                                JTextField txtPlaca, JComboBox<Cliente> comboClientes) 
            throws DatosInvalidosException {
        try {
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String añoStr = txtAño.getText().trim();
            String placa = txtPlaca.getText().trim().toUpperCase();
            Cliente cliente = (Cliente) comboClientes.getSelectedItem();
            
            // Validaciones
            if (!Validador.esTextoValido(marca)) {
                throw new DatosInvalidosException("La marca es obligatoria");
            }
            if (!Validador.esTextoValido(modelo)) {
                throw new DatosInvalidosException("El modelo es obligatorio");
            }
            if (!Validador.esNumeroValido(añoStr)) {
                throw new DatosInvalidosException("El año debe ser un número válido");
            }
            if (!Validador.esPlacaValida(placa)) {
                throw new DatosInvalidosException("La placa debe tener el formato correcto (ABC-123 o AB-1234)");
            }
            if (cliente == null) {
                throw new DatosInvalidosException("Debe seleccionar un cliente");
            }
            
            int año = Integer.parseInt(añoStr);
            if (año < 1900 || año > java.time.Year.now().getValue() + 1) {
                throw new DatosInvalidosException("El año debe estar entre 1900 y " + (java.time.Year.now().getValue() + 1));
            }
            
            // Verificar que la placa no exista
            if (vehiculoDAO.buscarPorPlaca(placa) != null) {
                throw new DatosInvalidosException("Ya existe un vehículo con esa placa");
            }
            
            // Por defecto crear Auto, se puede modificar para preguntar el tipo
            Vehiculo vehiculo = new Auto(marca, modelo, año, placa);
            vehiculo.setCliente(cliente);
            cliente.agregarVehiculo(vehiculo);
            
            vehiculoDAO.guardar(vehiculo);
            
            limpiarCampos(txtMarca, txtModelo, txtAño, txtPlaca);
            comboClientes.setSelectedIndex(0);
            actualizarTabla();
            
            JOptionPane.showMessageDialog(null, 
                "Vehículo registrado exitosamente",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El año debe ser un número válido");
        } catch (HeadlessException | ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al registrar vehículo: " + e.getMessage());
        }
    }
    
    public void consultarVehiculo(JTable tabla, JTextField txtMarca, JTextField txtModelo, 
                                JTextField txtAño, JTextField txtPlaca, JComboBox<Cliente> comboClientes) {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String marca = (String) tabla.getValueAt(filaSeleccionada, 0);
            String modelo = (String) tabla.getValueAt(filaSeleccionada, 1);
            String año = tabla.getValueAt(filaSeleccionada, 2).toString();
            String placa = (String) tabla.getValueAt(filaSeleccionada, 3);
            String clienteNombre = (String) tabla.getValueAt(filaSeleccionada, 4);
            
            txtMarca.setText(marca);
            txtModelo.setText(modelo);
            txtAño.setText(año);
            txtPlaca.setText(placa);
            
            // Buscar y seleccionar el cliente en el combo
            for (int i = 0; i < comboClientes.getItemCount(); i++) {
                Cliente cliente = comboClientes.getItemAt(i);
                if (cliente != null && cliente.getNombre().equals(clienteNombre)) {
                    comboClientes.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, 
                "Seleccione un vehículo de la tabla", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void modificarVehiculo(JTable tabla, JTextField txtMarca, JTextField txtModelo, 
                                JTextField txtAño, JTextField txtPlaca, JComboBox<Cliente> comboClientes) 
            throws DatosInvalidosException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un vehículo para modificar");
        }
        
        try {
            String placaOriginal = (String) tabla.getValueAt(filaSeleccionada, 3);
            Vehiculo vehiculo = vehiculoDAO.buscarPorPlaca(placaOriginal);
            
            if (vehiculo == null) {
                throw new DatosInvalidosException("Vehículo no encontrado");
            }
            
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String añoStr = txtAño.getText().trim();
            String placa = txtPlaca.getText().trim().toUpperCase();
            Cliente cliente = (Cliente) comboClientes.getSelectedItem();
            
            // Validaciones
            if (!Validador.esTextoValido(marca)) {
                throw new DatosInvalidosException("La marca es obligatoria");
            }
            if (!Validador.esTextoValido(modelo)) {
                throw new DatosInvalidosException("El modelo es obligatorio");
            }
            if (!Validador.esNumeroValido(añoStr)) {
                throw new DatosInvalidosException("El año debe ser un número válido");
            }
            if (!Validador.esPlacaValida(placa)) {
                throw new DatosInvalidosException("La placa debe tener el formato correcto");
            }
            if (cliente == null) {
                throw new DatosInvalidosException("Debe seleccionar un cliente");
            }
            
            int año = Integer.parseInt(añoStr);
            
            // Si cambió la placa, verificar que no exista
            if (!placa.equals(placaOriginal) && vehiculoDAO.buscarPorPlaca(placa) != null) {
                throw new DatosInvalidosException("Ya existe un vehículo con esa placa");
            }
            
            vehiculo.setMarca(marca);
            vehiculo.setModelo(modelo);
            vehiculo.setAño(año);
            vehiculo.setPlaca(placa);
            vehiculo.setCliente(cliente);
            
            vehiculoDAO.actualizar(vehiculo);
            
            limpiarCampos(txtMarca, txtModelo, txtAño, txtPlaca);
            comboClientes.setSelectedIndex(0);
            actualizarTabla();
            
            JOptionPane.showMessageDialog(null, 
                "Vehículo modificado exitosamente", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El año debe ser un número válido");
        } catch (HeadlessException | ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al modificar vehículo: " + e.getMessage());
        }
    }
    
    public void eliminarVehiculo(JTable tabla) throws DatosInvalidosException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un vehículo para eliminar");
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(null,
            "¿Está seguro de eliminar este vehículo?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                String placa = (String) tabla.getValueAt(filaSeleccionada, 3);
                vehiculoDAO.eliminar(placa);
                actualizarTabla();
                
                JOptionPane.showMessageDialog(null, 
                    "Vehículo eliminado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (HeadlessException | ArchivoException e) {
                throw new DatosInvalidosException("Error al eliminar vehículo: " + e.getMessage());
            }
        }
    }
    
    public void cargarVehiculosEnCombo(JComboBox<Vehiculo> combo, Cliente cliente) {
        combo.removeAllItems();
        combo.addItem(null); // Opción vacía
        
        if (cliente != null) {
            List<Vehiculo> vehiculos = vehiculoDAO.obtenerPorCliente(cliente);
            for (Vehiculo vehiculo : vehiculos) {
                combo.addItem(vehiculo);
            }
        }
    }
    
    private void cargarVehiculos() {
        try {
            vehiculoDAO.cargarDesdeArchivo();
        } catch (ArchivoException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al cargar vehículos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarTabla() {
        if (modeloTabla != null) {
            modeloTabla.setRowCount(0);
            List<Vehiculo> vehiculos = vehiculoDAO.obtenerTodos();
            
            for (Vehiculo vehiculo : vehiculos) {
                Object[] fila = {
                    vehiculo.getMarca(),
                    vehiculo.getModelo(),
                    vehiculo.getAño(),
                    vehiculo.getPlaca(),
                    vehiculo.getCliente() != null ? vehiculo.getCliente().getNombre() : "Sin asignar",
                    vehiculo.getTipo()
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
    private void limpiarCampos(JTextField txtMarca, JTextField txtModelo, JTextField txtAño, JTextField txtPlaca) {
        txtMarca.setText("");
        txtModelo.setText("");
        txtAño.setText("");
        txtPlaca.setText("");
    }
    
    public List<Vehiculo> obtenerTodosLosVehiculos() {
        return vehiculoDAO.obtenerTodos();
    }
}
