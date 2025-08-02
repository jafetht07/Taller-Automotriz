package taller.automotriz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import taller.automotriz.controlador.*;
import taller.automotriz.modelo.*;
import taller.automotriz.persistencia.*;
import taller.automotriz.excepciones.*;
import taller.automotriz.utiles.FormateadorFecha;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;




/**
 * @author Tu_Indania y Jafeth
 */
public class PantallaPrincipal extends javax.swing.JFrame {

    // Controladores
    private ControladorClientes controladorClientes;
    private ControladorVehiculos controladorVehiculos;
    private ControladorServicios controladorServicios;
    private ControladorOrdenes controladorOrdenes;

    // DAOs
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;
    private ServicioDAO servicioDAO;

    
    public PantallaPrincipal() {
        initComponents();
        inicializarControladores();
        configurarEventos();
        configurarTablas();
        configurarRadioButtons();
        configurarFechasIniciales();
    }

   private void inicializarControladores() {
    try {
        // Inicializar DAOs UNA SOLA VEZ
        clienteDAO = new ClienteDAO();
        vehiculoDAO = new VehiculoDAO(clienteDAO);
        servicioDAO = new ServicioDAO();
        
        // Cargar datos desde archivos PRIMERO
        clienteDAO.cargarDesdeArchivo(); 
        vehiculoDAO.cargarDesdeArchivo();
        servicioDAO.cargarDesdeArchivo();
        
        // Inicializar controladores con DAOs compartidos
        controladorClientes = new ControladorClientes(clienteDAO);     
        controladorVehiculos = new ControladorVehiculos(vehiculoDAO);
        controladorServicios = new ControladorServicios(servicioDAO);  
        controladorOrdenes = new ControladorOrdenes(clienteDAO, vehiculoDAO, servicioDAO);
        
        // Cargar datos iniciales en combos
        cargarDatosIniciales();
        
    } catch (ArchivoException e) {
        JOptionPane.showMessageDialog(this, 
            "Error al inicializar la aplicación: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void cargarDatosIniciales() {
    // Configurar combo de tipos de servicio
    ComboTiposDeServicios.removeAllItems();
    for (Servicio.TipoServicio tipo : Servicio.TipoServicio.values()) {
        ComboTiposDeServicios.addItem(tipo.getDescripcion());
    }
    
    // CARGAR CLIENTES REALES en los combos
    cargarClientesEnCombos();
}

    private void configurarTablas() {
        if (controladorClientes != null) {
            controladorClientes.configurarTabla(tblClientes);
        }
        if (controladorVehiculos != null) {
            controladorVehiculos.configurarTabla(tblVehiculo);
        }
        if (controladorServicios != null) {
            controladorServicios.configurarTabla(tblServicios);
        }
        if (controladorOrdenes != null) {
            controladorOrdenes.configurarTabla(tablaordenes);
        }
    }

    private void configurarRadioButtons() {
        // Agrupar radio buttons de estado de orden
        buttonGroup1.add(rdbAbierta);
        buttonGroup1.add(rdbCerada);
        rdbAbierta.setSelected(true); // Por defecto abierta
    }

    private void configurarFechasIniciales() {
        // Establecer fecha actual en campos de fecha
        String fechaActual = FormateadorFecha.fechaActual();
        txtFechaDeIngresados.setText(fechaActual);
    }

    private void configurarEventos() {
        //  EVENTOS CLIENTES 
        btnRegistrar.addActionListener(e -> registrarCliente());
        btnConsultar.addActionListener(e -> consultarCliente());
        btnModificar.addActionListener(e -> modificarCliente());
        btnEliminar.addActionListener(e -> eliminarCliente());
        
        //  EVENTOS VEHÍCULOS 
        btnRegistarVeiculo.addActionListener(e -> registrarVehiculo());
        btnConsultarVehiculo.addActionListener(e -> consultarVehiculo());
        btnModificarVehiculo.addActionListener(e -> modificarVehiculo());
        btnEliminarVehiculo.addActionListener(e -> eliminarVehiculo());
        
        //  EVENTOS SERVICIOS 
        btnRegistarServicios.addActionListener(e -> registrarServicio());
        btnConsultarServicios.addActionListener(e -> consultarServicio());
        btnModificarServicios.addActionListener(e -> modificarServicio());
        btnEliminarServicios.addActionListener(e -> eliminarServicio());
        
        // EVENTOS ÓRDENES
        btnRegistrarOrden.addActionListener(e -> registrarOrden());
        btnConsultarOrden.addActionListener(e -> consultarOrden());
        btnModificarOrden.addActionListener(e -> modificarOrden());
        btnCerrarOrden.addActionListener(e -> cerrarOrden());
        
        // === EVENTOS COMBOS ===
        comboCliente.addActionListener(e -> clienteSeleccionado());
    }

    // ===============================
    // MÉTODOS PARA CLIENTES
    // ===============================

    private void registrarCliente() {
        try {
            controladorClientes.registrarCliente(txtNombre, txtTelefono, txtDireccion);
            actualizarCombosClientes();
        } catch (DatosInvalidosException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void consultarCliente() {
        controladorClientes.consultarCliente(tblClientes, txtNombre, txtTelefono, txtDireccion);
    }


    private void eliminarCliente() {
        try {
            controladorClientes.eliminarCliente(tblClientes);
            actualizarCombosClientes();
        } catch (ClienteNoEncontradoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCombosClientes() {
    cargarClientesEnCombos(); // Usar el método que carga datos reales
}
// AGREGAR ESTE MÉTODO NUEVO DESPUÉS del método anterior:


    private void registrarVehiculo() {
    try {
        // Obtener cliente seleccionado
        String clienteNombre = (String) comboClientes.getSelectedItem();
        if (clienteNombre == null || clienteNombre.equals("Seleccione un cliente")) {
            throw new DatosInvalidosException("Debe seleccionar un cliente");
        }
        
        Cliente cliente = clienteDAO.buscarPorNombre(clienteNombre);
        if (cliente == null) {
            throw new DatosInvalidosException("Cliente no encontrado");
        }
        
        // Crear un combo temporal con el cliente seleccionado
        JComboBox<Cliente> comboTemp = new JComboBox<>();
        comboTemp.addItem(cliente);
        
        controladorVehiculos.registrarVehiculo(txtMarca, txtModelo, txtAhno, txtPlaca, comboTemp);
        
        // Actualizar combos después de registrar
        cargarClientesEnCombos();
        
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void consultarVehiculo() {
    // Crear un combo temporal para la consulta
    JComboBox<Cliente> comboTemp = new JComboBox<>();
    List<Cliente> clientes = clienteDAO.obtenerTodos();
    for (Cliente cliente : clientes) {
        comboTemp.addItem(cliente);
    }
    
    controladorVehiculos.consultarVehiculo(tblVehiculo, txtMarca, txtModelo, txtAhno, txtPlaca, comboTemp);
}



   private void eliminarVehiculo() {
    try {
        controladorVehiculos.eliminarVehiculo(tblVehiculo);
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void clienteSeleccionado() {
    comboVeiculo.removeAllItems();
    comboVeiculo.addItem("Seleccione un vehiculo");
    
    String clienteNombre = (String) comboCliente.getSelectedItem();
    if (clienteNombre != null && !clienteNombre.equals("Seleccione un cliente")) {
        // Buscar el cliente por nombre
        Cliente cliente = clienteDAO.buscarPorNombre(clienteNombre);
        if (cliente != null) {
            // Obtener vehículos del cliente
            List<Vehiculo> vehiculos = vehiculoDAO.obtenerPorCliente(cliente);
            for (Vehiculo vehiculo : vehiculos) {
                comboVeiculo.addItem(vehiculo.getMarca() + " " + vehiculo.getModelo() + " (" + vehiculo.getPlaca() + ")");
            }
        }
    }
}

    // ===============================
    // MÉTODOS PARA SERVICIOS
    // ===============================

   private void registrarServicio() {
    try {
        String nombreServicio = txtServicios.getText().trim();
        String tipoStr = (String) ComboTiposDeServicios.getSelectedItem();
        String costoStr = txtCosto.getText().trim();
        
        // Validaciones mejoradas
        if (nombreServicio.isEmpty()) {
            throw new DatosInvalidosException("El nombre del servicio es obligatorio");
        }
        
        if (nombreServicio.length() > 50) {
            throw new DatosInvalidosException("El nombre del servicio no puede tener más de 50 caracteres");
        }
        
        if (tipoStr == null) {
            throw new DatosInvalidosException("Debe seleccionar un tipo de servicio");
        }
        
        if (costoStr.isEmpty()) {
            throw new DatosInvalidosException("El costo es obligatorio");
        }
        
        // Validar costo con mejor feedback
        double costo;
        try {
            costo = Double.parseDouble(costoStr);
            if (costo <= 0) {
                throw new DatosInvalidosException("El costo debe ser mayor a cero");
            }
            if (costo > 999999.99) {
                throw new DatosInvalidosException("El costo no puede ser mayor a ₡999,999.99");
            }
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El costo debe ser un número válido. Use punto (.) para decimales");
        }
        
        // Convertir tipo
        Servicio.TipoServicio tipo = convertirStringATipoServicio(tipoStr);
        
        // Crear servicio
        Servicio servicio = new Servicio(nombreServicio, tipo, costo);
        
        // Guardar
        servicioDAO.guardar(servicio);
        
        // Limpiar campos
        txtServicios.setText("");
        txtCosto.setText("");
        ComboTiposDeServicios.setSelectedIndex(0);
        
        // Actualizar tabla
        if (controladorServicios != null) {
            controladorServicios.configurarTabla(tblServicios);
        }
        
        // Mensaje de éxito
        JOptionPane.showMessageDialog(this, 
            """
            Servicio registrado exitosamente
            
            ID: """ + servicio.getIdServicio() + "\n" +
            "Nombre: " + nombreServicio + "\n" +
            "Tipo: " + tipoStr + "\n" +
            "Costo: ₡" + String.format("%.2f", costo),
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, "❌ " + e.getMessage(), "Error de Validación", JOptionPane.ERROR_MESSAGE);
    } catch (HeadlessException | ArchivoException e) {
        JOptionPane.showMessageDialog(this, " Error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void consultarServicio() {
        controladorServicios.consultarServicio(tblServicios, txtServicios, ComboTiposDeServicios, txtCosto);
    }

    private void modificarServicio() {
    try {
        int filaSeleccionada = tblServicios.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un servicio para modificar");
        }
        
        // Obtener datos de la fila seleccionada
        String id = tblServicios.getValueAt(filaSeleccionada, 0).toString();
        String nombreActual = (String) tblServicios.getValueAt(filaSeleccionada, 1);
        String tipoActual = (String) tblServicios.getValueAt(filaSeleccionada, 2);
        String costoActual = tblServicios.getValueAt(filaSeleccionada, 3).toString();
        
        // Mostrar ventana de modificación
        mostrarVentanaModificarServicio(id, nombreActual, tipoActual, costoActual);
        
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// ===== AGREGAR ESTE MÉTODO NUEVO =====

private void mostrarVentanaModificarServicio(String servicioId, String nombreActual, 
                                           String tipoActual, String costoActual) {
    
    // Crear diálogo
    JDialog dialogo = new JDialog(this, "Modificar Servicio", true);
    dialogo.setLayout(new BorderLayout());
    dialogo.setSize(450, 350);
    dialogo.setLocationRelativeTo(this);
    
    // Panel principal
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel lblTitulo = new JLabel("MODIFICAR SERVICIO - ID: " + servicioId);
    lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    panelPrincipal.add(lblTitulo);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Campo: Nombre del servicio
    panelPrincipal.add(new JLabel("Nombre del Servicio:"));
    JTextField txtNombreServicioMod = new JTextField(nombreActual);
    txtNombreServicioMod.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtNombreServicioMod.getPreferredSize().height));
    panelPrincipal.add(txtNombreServicioMod);
    panelPrincipal.add(Box.createVerticalStrut(15));
    
    // Campo: Tipo de servicio
    panelPrincipal.add(new JLabel("Tipo de Servicio:"));
    JComboBox<String> comboTipoMod = new JComboBox<>();
    
    // Cargar tipos de servicio
    for (Servicio.TipoServicio tipo : Servicio.TipoServicio.values()) {
        comboTipoMod.addItem(tipo.getDescripcion());
    }
    
    // Seleccionar el tipo actual
    comboTipoMod.setSelectedItem(tipoActual);
    comboTipoMod.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboTipoMod.getPreferredSize().height));
    panelPrincipal.add(comboTipoMod);
    panelPrincipal.add(Box.createVerticalStrut(15));
    
    // Campo: Costo
    panelPrincipal.add(new JLabel("Costo (₡):"));
    JTextField txtCostoMod = new JTextField(costoActual);
    txtCostoMod.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtCostoMod.getPreferredSize().height));
    panelPrincipal.add(txtCostoMod);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Panel de información adicional
    JPanel panelInfo = new JPanel();
    panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
    panelInfo.setBorder(BorderFactory.createTitledBorder("Informacion"));
    panelInfo.add(new JLabel("• El costo debe ser un nimero positivo"));
    panelInfo.add(new JLabel("• Use punto (.) para decimales: 25000.50"));
    panelInfo.add(new JLabel("• Todos los campos son obligatorios"));
    panelPrincipal.add(panelInfo);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Botones
    JPanel panelBotones = new JPanel(new FlowLayout());
    JButton btnGuardar = new JButton("Guardar Cambios");
    JButton btnCancelar = new JButton("Cancelar");
    
    // Estilo de botones
    btnGuardar.setPreferredSize(new Dimension(150, 35));
    btnCancelar.setPreferredSize(new Dimension(120, 35));
    
    btnGuardar.addActionListener(e -> {
        try {
            // Obtener nuevos datos
            String nuevoNombre = txtNombreServicioMod.getText().trim();
            String nuevoTipoStr = (String) comboTipoMod.getSelectedItem();
            String nuevoCostoStr = txtCostoMod.getText().trim();
            
            // Validaciones
            if (nuevoNombre.isEmpty()) {
                throw new DatosInvalidosException("El nombre del servicio es obligatorio");
            }
            
            if (nuevoTipoStr == null) {
                throw new DatosInvalidosException("Debe seleccionar un tipo de servicio");
            }
            
            if (nuevoCostoStr.isEmpty()) {
                throw new DatosInvalidosException("El costo es obligatorio");
            }
            
            // Validar costo
            double nuevoCosto;
            try {
                nuevoCosto = Double.parseDouble(nuevoCostoStr);
                if (nuevoCosto <= 0) {
                    throw new DatosInvalidosException("El costo debe ser mayor a cero");
                }
                if (nuevoCosto > 999999.99) {
                    throw new DatosInvalidosException("El costo no puede ser mayor a ₡999,999.99");
                }
            } catch (NumberFormatException ex) {
                throw new DatosInvalidosException("El costo debe ser un nimero valido (use punto para decimales)");
            }
            
            // Convertir tipo
            Servicio.TipoServicio nuevoTipo = convertirStringATipoServicio(nuevoTipoStr);
            
            // Buscar servicio
            int id = Integer.parseInt(servicioId);
            Servicio servicio = servicioDAO.buscarPorId(id);
            
            if (servicio == null) {
                throw new DatosInvalidosException("Servicio no encontrado");
            }
            
            // Actualizar servicio
            servicio.setNombreServicio(nuevoNombre);
            servicio.setTipo(nuevoTipo);
            servicio.setCosto(nuevoCosto);
            
            // Guardar cambios
            servicioDAO.actualizar(servicio);
            
            // Actualizar tabla
            if (controladorServicios != null) {
                controladorServicios.configurarTabla(tblServicios);
            }
            
            // Mensaje de éxito
            JOptionPane.showMessageDialog(dialogo, 
                """
                Servicio modificado exitosamente
                
                Nombre: """ + nuevoNombre + "\n" +
                "Tipo: " + nuevoTipoStr + "\n" +
                "Costo: ₡" + String.format("%.2f", nuevoCosto), 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            dialogo.dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "❌ ID de servicio inválido", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException | ArchivoException | DatosInvalidosException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "❌ Error al modificar servicio:\n" + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    
    btnCancelar.addActionListener(e -> dialogo.dispose());
    
    panelBotones.add(btnGuardar);
    panelBotones.add(btnCancelar);
    
    // Agregar al diálogo
    dialogo.add(panelPrincipal, BorderLayout.CENTER);
    dialogo.add(panelBotones, BorderLayout.SOUTH);
    
    dialogo.setVisible(true);
}

// ===== AGREGAR ESTE MÉTODO AUXILIAR =====

private Servicio.TipoServicio convertirStringATipoServicio(String tipoStr) {
    return switch (tipoStr) {
        case "Mecánica" -> Servicio.TipoServicio.MECANICA;
        case "Pintura" -> Servicio.TipoServicio.PINTURA;
        case "Revisión" -> Servicio.TipoServicio.REVISION;
        case "Otros" -> Servicio.TipoServicio.OTROS;
        default -> Servicio.TipoServicio.OTROS;
    };
}

    private void eliminarServicio() {
        try {
            controladorServicios.eliminarServicio(tblServicios);
        } catch (DatosInvalidosException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===============================
    // MÉTODOS PARA ÓRDENES
    // ===============================

  

private void registrarOrden() {
    try {
        System.out.println("=== INICIANDO REGISTRO DE ORDEN ===");
        
        //  1. VALIDAR CLIENTE SELECCIONADO
        String clienteNombre = (String) comboCliente.getSelectedItem();
        System.out.println("Cliente seleccionado: " + clienteNombre);
        
        if (clienteNombre == null || clienteNombre.equals("Seleccione un cliente")) {
            throw new DatosInvalidosException("Debe seleccionar un cliente");
        }
        
        //  2. VALIDAR VEHÍCULO SELECCIONADO 
        String vehiculoInfo = (String) comboVeiculo.getSelectedItem();
        System.out.println("Vehiculo seleccionado: " + vehiculoInfo);
        
        if (vehiculoInfo == null || vehiculoInfo.equals("Seleccione un vehiculo")) {
            throw new DatosInvalidosException("Debe seleccionar un vehiculo");
        }
        
        // 3. BUSCAR CLIENTE EN LA BASE DE DATOS 
        Cliente cliente = clienteDAO.buscarPorNombre(clienteNombre);
        System.out.println("Cliente encontrado: " + (cliente != null ? cliente.toString() : "NULL"));
        
        if (cliente == null) {
            throw new DatosInvalidosException("Cliente no encontrado en la base de datos");
        }
        
        // 4. EXTRAER PLACA Y BUSCAR VEHÍCULO 
        String placa = extraerPlacaDeCombo(vehiculoInfo);
        System.out.println("Placa extraida: " + placa);
        
        if (placa.isEmpty()) {
            throw new DatosInvalidosException("No se pudo extraer la placa del vehiculo seleccionado");
        }
        
        Vehiculo vehiculo = vehiculoDAO.buscarPorPlaca(placa);
        System.out.println("Vehiculo encontrado: " + (vehiculo != null ? vehiculo.toString() : "NULL"));
        
        if (vehiculo == null) {
            throw new DatosInvalidosException("Vehiculo con placa " + placa + " no encontrado");
        }
        
        // 5. VALIDAR SERVICIOS SELECCIONADOS 
        List<Servicio.TipoServicio> tiposSeleccionados = new ArrayList<>();
        if (chekMecanica.isSelected()) {
            tiposSeleccionados.add(Servicio.TipoServicio.MECANICA);
            System.out.println("Servicio agregado: MECANICA");
        }
        if (chkPintura.isSelected()) {
            tiposSeleccionados.add(Servicio.TipoServicio.PINTURA);
            System.out.println("Servicio agregado: PINTURA");
        }
        if (chekRevicion.isSelected()) {
            tiposSeleccionados.add(Servicio.TipoServicio.REVISION);
            System.out.println("Servicio agregado: REVISION");
        }
        if (chkOtros.isSelected()) {
            tiposSeleccionados.add(Servicio.TipoServicio.OTROS);
            System.out.println("Servicio agregado: OTROS");
        }
        
        System.out.println("Total servicios seleccionados: " + tiposSeleccionados.size());
        
        if (tiposSeleccionados.isEmpty()) {
            throw new DatosInvalidosException("Debe seleccionar al menos un tipo de servicio");
        }
        
        // 6. OBTENER FECHAS Y OBSERVACIONES 
        String fechaIngreso = txtFechaDeIngresados.getText().trim();
        String fechaEntrega = txtFechaDeEntrega.getText().trim();
        String observaciones = jTextArea1.getText().trim();
        
        System.out.println("Fecha ingreso: " + fechaIngreso);
        System.out.println("Fecha entrega: " + fechaEntrega);
        System.out.println("Observaciones: " + observaciones);
        
        // 7. VALIDAR CONTROLADOR DE ÓRDENES 
        if (controladorOrdenes == null) {
            throw new DatosInvalidosException("Error del sistema: Controlador de ordenes no inicializado");
        }
        
        // 8. REGISTRAR LA ORDEN 
        System.out.println("Llamando a controladorOrdenes.registrarOrden()...");
        
        controladorOrdenes.registrarOrden(
            cliente,
            vehiculo,
            tiposSeleccionados,
            observaciones,
            fechaIngreso,
            fechaEntrega
        );
        
        System.out.println("Orden registrada exitosamente!");
        
        // 9. MOSTRAR MENSAJE DE ÉXITO
        JOptionPane.showMessageDialog(this, 
            "Orden registrada exitosamente", 
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
        
        // 10. LIMPIAR CAMPOS
        limpiarCamposOrden();
        
        System.out.println("=== REGISTRO DE ORDEN COMPLETADO ===");
        
    } catch (DatosInvalidosException e) {
        System.err.println("Error de validacion: " + e.getMessage());
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error de Validacion", JOptionPane.ERROR_MESSAGE);
    } catch (HeadlessException e) {
        System.err.println("Error inesperado: " + e.getMessage());
        JOptionPane.showMessageDialog(this, 
            "Error inesperado al registrar la orden: " + e.getMessage(), 
            "Error del Sistema", JOptionPane.ERROR_MESSAGE);
    }
}


    private void consultarOrden() {
    try {
        controladorOrdenes.consultarOrden(tablaordenes, this);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

   private void modificarOrden() {
    try {
        int filaSeleccionada = tablaordenes.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione una orden para modificar");
        }
        
        // Obtener el número de orden de la tabla
        int numeroOrden = (Integer) tablaordenes.getValueAt(filaSeleccionada, 0);
        
        // Buscar la orden en el controlador
        OrdenTrabajo orden = null;
        List<OrdenTrabajo> ordenes = controladorOrdenes.obtenerTodasLasOrdenes();
        for (OrdenTrabajo o : ordenes) {
            if (o.getNumeroOrden() == numeroOrden) {
                orden = o;
                break;
            }
        }
        
        if (orden == null) {
            throw new DatosInvalidosException("Orden no encontrada");
        }
        
        if (orden.getEstado() == OrdenTrabajo.EstadoOrden.CERRADA) {
            throw new DatosInvalidosException("No se puede modificar una orden cerrada");
        }
        
        // Crear y mostrar ventana de modificación
        mostrarVentanaModificarOrden(orden);
        
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

   private void cerrarOrden() {
    try {
        controladorOrdenes.cerrarOrden(tablaordenes);
        JOptionPane.showMessageDialog(this, 
            "Orden cerrada exitosamente", 
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
   

// MÉTODO AUXILIAR

private Servicio crearServicioPorDefecto(Servicio.TipoServicio tipo) {
    String nombre;
    double costo;
    
    switch (tipo) {
        case MECANICA -> {
            nombre = "Servicio Mecánico General";
            costo = 25000.0;
        }
        case PINTURA -> {
            nombre = "Servicio de Pintura";
            costo = 50000.0;
        }
        case REVISION -> {
            nombre = "Revisión Técnica";
            costo = 15000.0;
        }
        case OTROS -> {
            nombre = "Otros Servicios";
            costo = 20000.0;
        }
        default -> {
            nombre = "Servicio General";
            costo = 10000.0;
        }
    }
    
    return new Servicio(nombre, tipo, costo);
}



    private void limpiarCamposOrden() {
        txtFechaDeIngresados.setText(FormateadorFecha.fechaActual());
        txtFechaDeEntrega.setText("");
        txtCostoTotal.setText("");
        jTextArea1.setText("");
        chekMecanica.setSelected(false);
        chkPintura.setSelected(false);
        chekRevicion.setSelected(false);
        chkOtros.setSelected(false);
        rdbAbierta.setSelected(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
                                                 
    // </editor-fold>                        


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PantallaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new PantallaPrincipal().setVisible(true);
        });
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tbpPestanas = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        btnRegistrar = new javax.swing.JButton();
        btnConsultar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        ComboTiposDeServicios = new javax.swing.JComboBox<>();
        txtServicios = new javax.swing.JTextField();
        txtCosto = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblServicios = new javax.swing.JTable();
        btnConsultarServicios = new javax.swing.JButton();
        btnRegistarServicios = new javax.swing.JButton();
        btnModificarServicios = new javax.swing.JButton();
        btnEliminarServicios = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        comboCliente = new javax.swing.JComboBox<>();
        comboVeiculo = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel10 = new javax.swing.JLabel();
        lbCliente = new javax.swing.JLabel();
        lblVehiculos = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtFechaDeEntrega = new javax.swing.JTextField();
        txtCostoTotal = new javax.swing.JTextField();
        rdbAbierta = new javax.swing.JRadioButton();
        chekMecanica = new javax.swing.JCheckBox();
        btnCerrarOrden = new javax.swing.JButton();
        btnRegistrarOrden = new javax.swing.JButton();
        btnConsultarOrden = new javax.swing.JButton();
        btnModificarOrden = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tablaordenes = new javax.swing.JTable();
        chkPintura = new javax.swing.JCheckBox();
        chkOtros = new javax.swing.JCheckBox();
        chekRevicion = new javax.swing.JCheckBox();
        txtFechaDeIngresados = new javax.swing.JTextField();
        rdbCerada = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        lbMarca = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnRegistarVeiculo = new javax.swing.JButton();
        btnConsultarVehiculo = new javax.swing.JButton();
        btnModificarVehiculo = new javax.swing.JButton();
        btnEliminarVehiculo = new javax.swing.JButton();
        txtMarca = new javax.swing.JTextField();
        txtModelo = new javax.swing.JTextField();
        txtAhno = new javax.swing.JTextField();
        txtPlaca = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblVehiculo = new javax.swing.JTable();
        comboClientes = new javax.swing.JComboBox<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuItemSalir = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Pantalla Principal");

        jLabel1.setText("Nombre");

        jLabel2.setText("Telefono");

        jLabel3.setText("Direccion");

        btnRegistrar.setText("Registrar");

        btnConsultar.setText("Consultar");

        btnModificar.setText("Modificar");

        btnEliminar.setText("Eliminar");

        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre Completo", "Telefono", "Direccion"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblClientes.setEnabled(false);
        jScrollPane1.setViewportView(tblClientes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .addComponent(txtTelefono)
                            .addComponent(txtDireccion))
                        .addGap(42, 42, 42)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnModificar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRegistrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnConsultar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnEliminar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(236, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btnRegistrar)
                    .addComponent(btnConsultar))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnModificar)
                            .addComponent(btnEliminar))))
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(242, Short.MAX_VALUE))
        );

        tbpPestanas.addTab("Clientes", jPanel1);

        jLabel4.setText("Servicio");

        jLabel8.setText("Tipo");

        jLabel9.setText("Costo");

        ComboTiposDeServicios.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Mecanica", "Pintura", "Revicion", "Otros" }));
        ComboTiposDeServicios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ComboTiposDeServiciosActionPerformed(evt);
            }
        });

        tblServicios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Servicios", "Tipo", "Costo"
            }
        ));
        jScrollPane3.setViewportView(tblServicios);

        btnConsultarServicios.setText("Consultar");
        btnConsultarServicios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarServiciosActionPerformed(evt);
            }
        });

        btnRegistarServicios.setText("Registrar");

        btnModificarServicios.setText("Modificar");

        btnEliminarServicios.setText("Eliminar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 507, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(55, 55, 55)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(txtServicios, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(btnRegistarServicios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ComboTiposDeServicios, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCosto, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(39, 39, 39)
                                .addComponent(btnModificarServicios)))
                        .addGap(36, 36, 36)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnConsultarServicios)
                            .addComponent(btnEliminarServicios, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 289, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegistarServicios)
                    .addComponent(txtServicios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConsultarServicios))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ComboTiposDeServicios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnModificarServicios)
                            .addComponent(btnEliminarServicios))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(txtCosto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(46, 46, 46)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(224, Short.MAX_VALUE))
        );

        tbpPestanas.addTab("Servicios", jPanel3);

        comboCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Clientes resgistrados" }));

        comboVeiculo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vehiculos del cliente" }));

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane4.setViewportView(jTextArea1);

        jLabel10.setText("Observaciones");

        lbCliente.setText("Cliente");

        lblVehiculos.setText("Vehiculo");

        jLabel13.setText("Fecha de ingreso");

        jLabel14.setText("Fecha de entrega");

        jLabel15.setText("Costo total");

        rdbAbierta.setText("Abierta");

        chekMecanica.setText("Mecanica");

        btnCerrarOrden.setText("Cerrar orden");

        btnRegistrarOrden.setText("Rgistrar orden");
        btnRegistrarOrden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarOrdenActionPerformed(evt);
            }
        });

        btnConsultarOrden.setText("Consultar orden");
        btnConsultarOrden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsultarOrdenActionPerformed(evt);
            }
        });

        btnModificarOrden.setText("Modificar orden");

        tablaordenes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Mostrar todas la ordenes de registros"
            }
        ));
        jScrollPane6.setViewportView(tablaordenes);

        chkPintura.setText("Pintura");

        chkOtros.setText("Otros");

        chekRevicion.setText("Revicion");

        rdbCerada.setText("Cerrada");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane6))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(lblVehiculos)
                            .addComponent(lbCliente)
                            .addComponent(jLabel15))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(comboCliente, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(comboVeiculo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtFechaDeIngresados)
                            .addComponent(txtFechaDeEntrega)
                            .addComponent(txtCostoTotal))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnRegistrarOrden, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnConsultarOrden, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnModificarOrden, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCerrarOrden, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkOtros)
                                            .addComponent(chekMecanica))
                                        .addGap(40, 40, 40)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(chkPintura)
                                            .addComponent(chekRevicion))))
                                .addGap(27, 27, 27)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rdbCerada)
                                    .addComponent(rdbAbierta)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(78, 78, 78)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)))))
                .addGap(54, 54, 54))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbCliente)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkPintura)
                            .addComponent(chekMecanica)
                            .addComponent(rdbAbierta)
                            .addComponent(btnConsultarOrden))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chekRevicion)
                            .addComponent(rdbCerada))
                        .addGap(48, 48, 48)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(txtFechaDeIngresados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(btnRegistrarOrden)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(chkOtros)
                        .addGap(2, 2, 2)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboVeiculo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnModificarOrden)
                            .addComponent(lblVehiculos))))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCerrarOrden)
                            .addComponent(txtFechaDeEntrega, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCostoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(58, 58, 58)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 106, Short.MAX_VALUE))
        );

        tbpPestanas.addTab("Ordenes de trabajo", jPanel4);

        lbMarca.setText("Marca");

        jLabel5.setText("Modelo");

        jLabel6.setText("Año");

        jLabel7.setText("Placa");

        btnRegistarVeiculo.setText("Registrar");

        btnConsultarVehiculo.setText("Consultar");

        btnModificarVehiculo.setText("Modificar");

        btnEliminarVehiculo.setText("Eliminar");

        txtPlaca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPlacaActionPerformed(evt);
            }
        });

        tblVehiculo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Marca del vehiculo", "Modelo", "Año", "Placa", "Cliente"
            }
        ));
        jScrollPane2.setViewportView(tblVehiculo);

        comboClientes.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cliente" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 595, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(lbMarca))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtModelo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                .addComponent(txtMarca, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(txtAhno, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPlaca, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(61, 61, 61)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnConsultarVehiculo, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                            .addComponent(btnModificarVehiculo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnEliminarVehiculo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRegistarVeiculo, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
                        .addGap(50, 50, 50)
                        .addComponent(comboClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(198, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbMarca)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRegistarVeiculo, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(73, 73, 73)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtAhno, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnModificarVehiculo))
                            .addGap(23, 23, 23)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel7)
                                .addComponent(txtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGap(32, 32, 32)
                            .addComponent(btnEliminarVehiculo)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtModelo, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConsultarVehiculo))))
                .addGap(78, 78, 78)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tbpPestanas.addTab("Vehículos", jPanel2);

        jMenu1.setText("Archivo");

        menuItemSalir.setText("Salir");
        menuItemSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSalirActionPerformed(evt);
            }
        });
        jMenu1.add(menuItemSalir);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Ayuda");

        jMenuItem1.setText("Acerca de");
        jMenu2.add(jMenuItem1);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbpPestanas)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbpPestanas)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarServiciosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarServiciosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnConsultarServiciosActionPerformed

    private void ComboTiposDeServiciosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ComboTiposDeServiciosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ComboTiposDeServiciosActionPerformed

    private void txtPlacaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPlacaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPlacaActionPerformed

    private void menuItemSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSalirActionPerformed

    }//GEN-LAST:event_menuItemSalirActionPerformed

    private void btnConsultarOrdenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarOrdenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnConsultarOrdenActionPerformed

    private void btnRegistrarOrdenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarOrdenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRegistrarOrdenActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> ComboTiposDeServicios;
    private javax.swing.JButton btnCerrarOrden;
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnConsultarOrden;
    private javax.swing.JButton btnConsultarServicios;
    private javax.swing.JButton btnConsultarVehiculo;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnEliminarServicios;
    private javax.swing.JButton btnEliminarVehiculo;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnModificarOrden;
    private javax.swing.JButton btnModificarServicios;
    private javax.swing.JButton btnModificarVehiculo;
    private javax.swing.JButton btnRegistarServicios;
    private javax.swing.JButton btnRegistarVeiculo;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JButton btnRegistrarOrden;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chekMecanica;
    private javax.swing.JCheckBox chekRevicion;
    private javax.swing.JCheckBox chkOtros;
    private javax.swing.JCheckBox chkPintura;
    private javax.swing.JComboBox<String> comboCliente;
    private javax.swing.JComboBox<String> comboClientes;
    private javax.swing.JComboBox<String> comboVeiculo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lbCliente;
    private javax.swing.JLabel lbMarca;
    private javax.swing.JLabel lblVehiculos;
    private javax.swing.JMenuItem menuItemSalir;
    private javax.swing.JRadioButton rdbAbierta;
    private javax.swing.JRadioButton rdbCerada;
    private javax.swing.JTable tablaordenes;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTable tblServicios;
    private javax.swing.JTable tblVehiculo;
    private javax.swing.JTabbedPane tbpPestanas;
    private javax.swing.JTextField txtAhno;
    private javax.swing.JTextField txtCosto;
    private javax.swing.JTextField txtCostoTotal;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtFechaDeEntrega;
    private javax.swing.JTextField txtFechaDeIngresados;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtModelo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPlaca;
    private javax.swing.JTextField txtServicios;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables

   private void cargarClientesEnCombos() {
    // Limpiar combos
    comboClientes.removeAllItems();
    comboCliente.removeAllItems();
    
    // Agregar opción vacía
    comboClientes.addItem("Seleccione un cliente");
    comboCliente.addItem("Seleccione un cliente");
    
    // Cargar clientes reales del DAO
    if (clienteDAO != null) {
        List<Cliente> clientes = clienteDAO.obtenerTodos();
        for (Cliente cliente : clientes) {
            comboClientes.addItem(cliente.getNombre());
            comboCliente.addItem(cliente.getNombre());
        }
       }
   }
   

private String extraerPlacaDeCombo(String vehiculoInfo) {
    System.out.println("Extrayendo placa de: " + vehiculoInfo);
    
    if (vehiculoInfo == null || vehiculoInfo.isEmpty()) {
        System.out.println("vehiculoInfo es null o vacío");
        return "";
    }
    
    // Formato esperado: "Marca Modelo (PLACA)"
    int inicioPlaca = vehiculoInfo.lastIndexOf("(");
    int finPlaca = vehiculoInfo.lastIndexOf(")");
    
    System.out.println("Inicio placa: " + inicioPlaca + ", Fin placa: " + finPlaca);
    
    if (inicioPlaca != -1 && finPlaca != -1 && inicioPlaca < finPlaca) {
        String placa = vehiculoInfo.substring(inicioPlaca + 1, finPlaca);
        System.out.println("Placa extraida: '" + placa + "'");
        return placa;
    } else {
        System.out.println("No se pudo extraer la placa del formato");
        return "";
    }
}

    private void mostrarVentanaModificarOrden(OrdenTrabajo orden) {
    // Crear diálogo personalizado
    JDialog dialogo = new JDialog(this, "Modificar Orden #" + orden.getNumeroOrden(), true);
    dialogo.setLayout(new BorderLayout());
    dialogo.setSize(600, 500);
    dialogo.setLocationRelativeTo(this);
    
    // Panel principal con formulario simple
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel lblTitulo = new JLabel("MODIFICAR ORDEN #" + orden.getNumeroOrden());
    lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    panelPrincipal.add(lblTitulo);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Observaciones
    panelPrincipal.add(new JLabel("Observaciones:"));
    JTextArea txtObservaciones = new JTextArea(orden.getObservaciones(), 5, 30);
    txtObservaciones.setLineWrap(true);
    txtObservaciones.setWrapStyleWord(true);
    JScrollPane scrollObservaciones = new JScrollPane(txtObservaciones);
    panelPrincipal.add(scrollObservaciones);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    // Costo
    panelPrincipal.add(new JLabel("Costo Total:"));
    JTextField txtCosto = new JTextField(String.format("%.2f", orden.getCostoTotal()));
    panelPrincipal.add(txtCosto);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Botones
    JPanel panelBotones = new JPanel(new FlowLayout());
    JButton btnGuardar = new JButton("Guardar Cambios");
    JButton btnCancelar = new JButton("Cancelar");
    
    btnGuardar.addActionListener(e -> {
        try {
            // Actualizar observaciones
            orden.setObservaciones(txtObservaciones.getText().trim());
            
            // Actualizar costo
            String costoStr = txtCosto.getText().trim();
            if (!costoStr.isEmpty()) {
                double nuevoCosto = Double.parseDouble(costoStr);
                orden.setCostoTotal(nuevoCosto);
            }
            
            // Actualizar tabla
            if (controladorOrdenes != null) {
                controladorOrdenes.configurarTabla(tablaordenes);
            }
            
            JOptionPane.showMessageDialog(dialogo, 
                "Orden modificada exitosamente", 
                "Exito", JOptionPane.INFORMATION_MESSAGE);
            
            dialogo.dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "El costo debe ser un numero valido", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "Error al modificar la orden: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    
    btnCancelar.addActionListener(e -> dialogo.dispose());
    
    panelBotones.add(btnGuardar);
    panelBotones.add(btnCancelar);
    
    // Agregar al diálogo
    dialogo.add(panelPrincipal, BorderLayout.CENTER);
    dialogo.add(panelBotones, BorderLayout.SOUTH);
    
    dialogo.setVisible(true);
    }

private void modificarVehiculo() {
    try {
        int filaSeleccionada = tblVehiculo.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un vehiculo para modificar");
        }
        
        // Obtener datos de la fila seleccionada
        String marca = (String) tblVehiculo.getValueAt(filaSeleccionada, 0);
        String modelo = (String) tblVehiculo.getValueAt(filaSeleccionada, 1);
        String año = tblVehiculo.getValueAt(filaSeleccionada, 2).toString();
        String placa = (String) tblVehiculo.getValueAt(filaSeleccionada, 3);
        String clienteNombre = (String) tblVehiculo.getValueAt(filaSeleccionada, 4);
        
        // Mostrar ventana de modificación personalizada
        mostrarVentanaModificarVehiculo(placa, marca, modelo, año, clienteNombre);
        
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


private void mostrarVentanaModificarVehiculo(String placaOriginal, String marcaActual, 
                                           String modeloActual, String añoActual, String clienteActual) {
    
    // Crear diálogo
    JDialog dialogo = new JDialog(this, "Modificar Vehiculo", true);
    dialogo.setLayout(new BorderLayout());
    dialogo.setSize(500, 400);
    dialogo.setLocationRelativeTo(this);
    
    // Panel principal
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel lblTitulo = new JLabel("MODIFICAR VEHICULO - " + placaOriginal);
    lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    panelPrincipal.add(lblTitulo);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Campos de texto
    panelPrincipal.add(new JLabel("Marca:"));
    JTextField txtMarcaMod = new JTextField(marcaActual);
    panelPrincipal.add(txtMarcaMod);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    panelPrincipal.add(new JLabel("Modelo:"));
    JTextField txtModeloMod = new JTextField(modeloActual);
    panelPrincipal.add(txtModeloMod);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    panelPrincipal.add(new JLabel("Año:"));
    JTextField txtAñoMod = new JTextField(añoActual);
    panelPrincipal.add(txtAñoMod);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    panelPrincipal.add(new JLabel("Placa:"));
    JTextField txtPlacaMod = new JTextField(placaOriginal);
    panelPrincipal.add(txtPlacaMod);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    // Combo de clientes
    panelPrincipal.add(new JLabel("Cliente:"));
    JComboBox<String> comboClientesMod = new JComboBox<>();
    comboClientesMod.addItem("Seleccione un cliente");
    
    // Cargar todos los clientes
    List<Cliente> clientes = clienteDAO.obtenerTodos();
    for (Cliente cliente : clientes) {
        comboClientesMod.addItem(cliente.getNombre());
        // Seleccionar el cliente actual
        if (cliente.getNombre().equals(clienteActual)) {
            comboClientesMod.setSelectedItem(cliente.getNombre());
        }
    }
    
    panelPrincipal.add(comboClientesMod);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Botones
    JPanel panelBotones = new JPanel(new FlowLayout());
    JButton btnGuardar = new JButton("Guardar Cambios");
    JButton btnCancelar = new JButton("Cancelar");
    
    btnGuardar.addActionListener(e -> {
        try {
            // Validar datos
            String nuevaMarca = txtMarcaMod.getText().trim();
            String nuevoModelo = txtModeloMod.getText().trim();
            String nuevoAño = txtAñoMod.getText().trim();
            String nuevaPlaca = txtPlacaMod.getText().trim().toUpperCase();
            String nuevoClienteNombre = (String) comboClientesMod.getSelectedItem();
            
            // Validaciones básicas
            if (nuevaMarca.isEmpty() || nuevoModelo.isEmpty() || nuevoAño.isEmpty() || nuevaPlaca.isEmpty()) {
                throw new DatosInvalidosException("Todos los campos son obligatorios");
            }
            
            if (nuevoClienteNombre == null || nuevoClienteNombre.equals("Seleccione un cliente")) {
                throw new DatosInvalidosException("Debe seleccionar un cliente");
            }
            
            // Validar año
            int año = Integer.parseInt(nuevoAño);
            if (año < 1900 || año > java.time.Year.now().getValue() + 1) {
                throw new DatosInvalidosException("El año debe estar entre 1900 y " + (java.time.Year.now().getValue() + 1));
            }
            
            // Buscar vehículo y cliente
            Vehiculo vehiculo = vehiculoDAO.buscarPorPlaca(placaOriginal);
            Cliente nuevoCliente = clienteDAO.buscarPorNombre(nuevoClienteNombre);
            
            if (vehiculo == null) {
                throw new DatosInvalidosException("Vehiculo no encontrado");
            }
            
            if (nuevoCliente == null) {
                throw new DatosInvalidosException("Cliente no encontrado");
            }
            
            // Verificar que la nueva placa no exista (si cambió)
            if (!nuevaPlaca.equals(placaOriginal)) {
                Vehiculo vehiculoExistente = vehiculoDAO.buscarPorPlaca(nuevaPlaca);
                if (vehiculoExistente != null) {
                    throw new DatosInvalidosException("Ya existe un vehiculo con la placa: " + nuevaPlaca);
                }
            }
            
            // Actualizar vehículo
            vehiculo.setMarca(nuevaMarca);
            vehiculo.setModelo(nuevoModelo);
            vehiculo.setAño(año);
            vehiculo.setPlaca(nuevaPlaca);
            vehiculo.setCliente(nuevoCliente);
            
            // Guardar cambios
            vehiculoDAO.actualizar(vehiculo);
            
            // Actualizar tabla
            if (controladorVehiculos != null) {
                controladorVehiculos.configurarTabla(tblVehiculo);
            }
            
            // Actualizar combos
            cargarClientesEnCombos();
            
            JOptionPane.showMessageDialog(dialogo, 
                "Vehiculo modificado exitosamente", 
                "Exito", JOptionPane.INFORMATION_MESSAGE);
            
            dialogo.dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "El año debe ser un numero valido", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException | ArchivoException | DatosInvalidosException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "Error al modificar vehiculo: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    
    btnCancelar.addActionListener(e -> dialogo.dispose());
    
    panelBotones.add(btnGuardar);
    panelBotones.add(btnCancelar);
    
    // Agregar al diálogo
    dialogo.add(panelPrincipal, BorderLayout.CENTER);
    dialogo.add(panelBotones, BorderLayout.SOUTH);
    
    dialogo.setVisible(true);
}

private void modificarCliente() {
    try {
        int filaSeleccionada = tblClientes.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione un cliente para modificar");
        }
        
        // Obtener datos de la fila seleccionada
        String id = (String) tblClientes.getValueAt(filaSeleccionada, 0);
        String nombreActual = (String) tblClientes.getValueAt(filaSeleccionada, 1);
        String telefonoActual = (String) tblClientes.getValueAt(filaSeleccionada, 2);
        String direccionActual = (String) tblClientes.getValueAt(filaSeleccionada, 3);
        
        // Mostrar ventana de modificación
        mostrarVentanaModificarCliente(id, nombreActual, telefonoActual, direccionActual);
        
    } catch (DatosInvalidosException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void mostrarVentanaModificarCliente(String clienteId, String nombreActual, 
                                          String telefonoActual, String direccionActual) {
    
    // Crear diálogo
    JDialog dialogo = new JDialog(this, "Modificar Cliente", true);
    dialogo.setLayout(new BorderLayout());
    dialogo.setSize(400, 300);
    dialogo.setLocationRelativeTo(this);
    
    // Panel principal
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel lblTitulo = new JLabel("MODIFICAR CLIENTE - " + clienteId);
    lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    panelPrincipal.add(lblTitulo);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Campos de texto
    panelPrincipal.add(new JLabel("Nombre:"));
    JTextField txtNombreMod = new JTextField(nombreActual);
    panelPrincipal.add(txtNombreMod);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    panelPrincipal.add(new JLabel("Telefono:"));
    JTextField txtTelefonoMod = new JTextField(telefonoActual);
    panelPrincipal.add(txtTelefonoMod);
    panelPrincipal.add(Box.createVerticalStrut(10));
    
    panelPrincipal.add(new JLabel("Direccion:"));
    JTextField txtDireccionMod = new JTextField(direccionActual);
    panelPrincipal.add(txtDireccionMod);
    panelPrincipal.add(Box.createVerticalStrut(20));
    
    // Botones
    JPanel panelBotones = new JPanel(new FlowLayout());
    JButton btnGuardar = new JButton("Guardar Cambios");
    JButton btnCancelar = new JButton("Cancelar");
    
    btnGuardar.addActionListener((ActionEvent e) -> {
        try {
            // Obtener nuevos datos
            String nuevoNombre = txtNombreMod.getText().trim();
            String nuevoTelefono = txtTelefonoMod.getText().trim();
            String nuevaDireccion = txtDireccionMod.getText().trim();
            
            // Validaciones
            if (nuevoNombre.isEmpty()) {
                throw new DatosInvalidosException("El nombre es obligatorio");
            }
            if (nuevoTelefono.isEmpty() || nuevoTelefono.length() != 8 || !nuevoTelefono.matches("\\d+")) {
                throw new DatosInvalidosException("El telefono debe tener 8 digitos");
            }
            if (nuevaDireccion.isEmpty()) {
                throw new DatosInvalidosException("La dirección es obligatoria");
            }
            
            // Buscar cliente
            int id = Integer.parseInt(clienteId.replace("CLI-", ""));
            Cliente cliente = clienteDAO.buscarPorId(id);
            
            if (cliente == null) {
                throw new DatosInvalidosException("Cliente no encontrado");
            }
            
            // Actualizar cliente
            cliente.setNombre(nuevoNombre);
            cliente.setTelefono(nuevoTelefono);
            cliente.setDireccion(nuevaDireccion);
            
            // Guardar cambios
            clienteDAO.actualizar(cliente);
            
            // Actualizar tabla
            if (controladorClientes != null) {
                controladorClientes.configurarTabla(tblClientes);
            }
            
            // Actualizar combos
            cargarClientesEnCombos();
            
            JOptionPane.showMessageDialog(dialogo, 
                "Cliente modificado exitosamente", 
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            dialogo.dispose();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "ID de cliente inválido", 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HeadlessException | ArchivoException | DatosInvalidosException ex) {
            JOptionPane.showMessageDialog(dialogo, 
                "Error al modificar cliente: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    
    btnCancelar.addActionListener(e -> dialogo.dispose());
    
    panelBotones.add(btnGuardar);
    panelBotones.add(btnCancelar);
    
    // Agregar al diálogo
    dialogo.add(panelPrincipal, BorderLayout.CENTER);
    dialogo.add(panelBotones, BorderLayout.SOUTH);
    
    dialogo.setVisible(true);
}
}

