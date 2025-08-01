package taller.automotriz.controlador;

import java.awt.HeadlessException;
import taller.automotriz.modelo.Cliente;
import taller.automotriz.persistencia.ClienteDAO;
import taller.automotriz.excepciones.DatosInvalidosException;
import taller.automotriz.excepciones.ClienteNoEncontradoException;
import taller.automotriz.utiles.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import taller.automotriz.excepciones.ArchivoException;

public class ControladorClientes {
    private final ClienteDAO clienteDAO;
    private DefaultTableModel modeloTabla;
    
    public ControladorClientes() {
        this.clienteDAO = new ClienteDAO();
        cargarClientes();
    }
    
    public ControladorClientes(ClienteDAO clienteDAO) {
    this.clienteDAO = clienteDAO;
    
    }
    
    public void configurarTabla(JTable tabla) {
        String[] columnas = {"ID", "Nombre Completo", "Teléfono", "Dirección"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setModel(modeloTabla);
        tabla.setEnabled(true); // Habilitar la tabla
        actualizarTabla();
    }
    
    public void registrarCliente(JTextField txtNombre, JTextField txtTelefono, JTextField txtDireccion) 
            throws DatosInvalidosException {
        try {
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();
            
            // Validaciones
            if (!Validador.esTextoValido(nombre)) {
                throw new DatosInvalidosException("El nombre es obligatorio");
            }
            if (!Validador.esTelefonoValido(telefono)) {
                throw new DatosInvalidosException("El teléfono debe tener 8 dígitos");
            }
            if (!Validador.esTextoValido(direccion)) {
                throw new DatosInvalidosException("La dirección es obligatoria");
            }
            
            Cliente cliente = new Cliente(nombre, telefono, direccion);
            clienteDAO.guardar(cliente);
            
            limpiarCampos(txtNombre, txtTelefono, txtDireccion);
            actualizarTabla();
            
            JOptionPane.showMessageDialog(null, 
                "Cliente registrado exitosamente\nID: " + cliente.getIdentificacion(),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (HeadlessException | ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al registrar cliente: " + e.getMessage());
        }
    }
    
    public void consultarCliente(JTable tabla, JTextField txtNombre, JTextField txtTelefono, JTextField txtDireccion) {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombre = (String) tabla.getValueAt(filaSeleccionada, 1);
            String telefono = (String) tabla.getValueAt(filaSeleccionada, 2);
            String direccion = (String) tabla.getValueAt(filaSeleccionada, 3);
            
            txtNombre.setText(nombre);
            txtTelefono.setText(telefono);
            txtDireccion.setText(direccion);
        } else {
            JOptionPane.showMessageDialog(null, 
                "Seleccione un cliente de la tabla", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void modificarCliente(JTable tabla, JTextField txtNombre, JTextField txtTelefono, JTextField txtDireccion) 
            throws DatosInvalidosException, ClienteNoEncontradoException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un cliente para modificar");
        }
        
        try {
            String idStr = (String) tabla.getValueAt(filaSeleccionada, 0);
            int id = Integer.parseInt(idStr.replace("CLI-", ""));
            
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();
            
            // Validaciones
            if (!Validador.esTextoValido(nombre)) {
                throw new DatosInvalidosException("El nombre es obligatorio");
            }
            if (!Validador.esTelefonoValido(telefono)) {
                throw new DatosInvalidosException("El teléfono debe tener 8 dígitos");
            }
            if (!Validador.esTextoValido(direccion)) {
                throw new DatosInvalidosException("La dirección es obligatoria");
            }
            
            Cliente cliente = clienteDAO.buscarPorId(id);
            if (cliente == null) {
                throw new ClienteNoEncontradoException("Cliente no encontrado");
            }
            
            cliente.setNombre(nombre);
            cliente.setTelefono(telefono);
            cliente.setDireccion(direccion);
            
            clienteDAO.actualizar(cliente);
            
            limpiarCampos(txtNombre, txtTelefono, txtDireccion);
            actualizarTabla();
            
            JOptionPane.showMessageDialog(null, 
                "Cliente modificado exitosamente", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("ID de cliente inválido");
        } catch (HeadlessException | ArchivoException | ClienteNoEncontradoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al modificar cliente: " + e.getMessage());
        }
    }
    
    public void eliminarCliente(JTable tabla) throws ClienteNoEncontradoException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new ClienteNoEncontradoException("Seleccione un cliente para eliminar");
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(null,
            "¿Está seguro de eliminar este cliente?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);
            
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                String idStr = (String) tabla.getValueAt(filaSeleccionada, 0);
                int id = Integer.parseInt(idStr.replace("CLI-", ""));
                
                clienteDAO.eliminar(id);
                actualizarTabla();
                
                JOptionPane.showMessageDialog(null, 
                    "Cliente eliminado exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (HeadlessException | NumberFormatException | ArchivoException e) {
                throw new ClienteNoEncontradoException("Error al eliminar cliente: " + e.getMessage());
            }
        }
    }
    
    public void cargarClientesEnCombo(JComboBox<Cliente> combo) {
        combo.removeAllItems();
        combo.addItem(null); // Opción vacía
        
        List<Cliente> clientes = clienteDAO.obtenerTodos();
        for (Cliente cliente : clientes) {
            combo.addItem(cliente);
        }
    }
    
    private void cargarClientes() {
        try {
            clienteDAO.cargarDesdeArchivo();
        } catch (ArchivoException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al cargar clientes: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarTabla() {
        if (modeloTabla != null) {
            modeloTabla.setRowCount(0);
            List<Cliente> clientes = clienteDAO.obtenerTodos();
            
            for (Cliente cliente : clientes) {
                Object[] fila = {
                    cliente.getIdentificacion(),
                    cliente.getNombre(),
                    cliente.getTelefono(),
                    cliente.getDireccion()
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
    private void limpiarCampos(JTextField txtNombre, JTextField txtTelefono, JTextField txtDireccion) {
        txtNombre.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
    }
    
    public Cliente obtenerClientePorId(int id) {
        return clienteDAO.buscarPorId(id);
    }
    
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteDAO.obtenerTodos();
    }
}
