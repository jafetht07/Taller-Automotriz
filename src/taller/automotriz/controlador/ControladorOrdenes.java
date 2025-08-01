// ===== ARCHIVO: ControladorOrdenes.java =====
// Package: taller.automotriz.controlador

package taller.automotriz.controlador;

import java.awt.HeadlessException;
import taller.automotriz.modelo.*;
import taller.automotriz.persistencia.*;
import taller.automotriz.excepciones.DatosInvalidosException;
import taller.automotriz.utiles.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import taller.automotriz.excepciones.ArchivoException;

public class ControladorOrdenes {
    private final OrdenTrabajoDAO ordenDAO;
    private final VehiculoDAO vehiculoDAO;
    private final ServicioDAO servicioDAO;
    private DefaultTableModel modeloTabla;
    
    public ControladorOrdenes(ClienteDAO clienteDAO, VehiculoDAO vehiculoDAO, ServicioDAO servicioDAO) {
        this.vehiculoDAO = vehiculoDAO;
        this.servicioDAO = servicioDAO;
        this.ordenDAO = new OrdenTrabajoDAO(clienteDAO, vehiculoDAO, servicioDAO);
        cargarOrdenes();
    }
    
    public void configurarTabla(JTable tabla) {
        String[] columnas = {"Número", "Cliente", "Vehículo", "Fecha Ingreso", "Fecha Entrega", "Estado", "Total"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setModel(modeloTabla);
        actualizarTabla();
    }
    
    public void registrarOrden(Cliente cliente, Vehiculo vehiculo, 
                             List<Servicio.TipoServicio> tiposServicios,
                             String observaciones, String fechaIngresoStr, String fechaEntregaStr) 
            throws DatosInvalidosException {
        try {
            OrdenTrabajo orden = new OrdenTrabajo(cliente, vehiculo);
            orden.setObservaciones(observaciones);
            
            // Configurar fechas
            if (!fechaIngresoStr.isEmpty()) {
                LocalDate fechaIngreso = FormateadorFecha.parsearDesdeFormulario(fechaIngresoStr);
                if (fechaIngreso != null) {
                    orden.setFechaIngreso(fechaIngreso);
                }
            }
            
            if (!fechaEntregaStr.isEmpty()) {
                LocalDate fechaEntrega = FormateadorFecha.parsearDesdeFormulario(fechaEntregaStr);
                if (fechaEntrega != null) {
                    orden.setFechaEntrega(fechaEntrega);
                }
            }
            
            // Agregar servicios según los tipos seleccionados
            List<Servicio> serviciosDisponibles = servicioDAO.obtenerTodos();
            for (Servicio.TipoServicio tipo : tiposServicios) {
                // Buscar un servicio del tipo solicitado
                Servicio servicioDelTipo = null;
                for (Servicio servicio : serviciosDisponibles) {
                    if (servicio.getTipo() == tipo) {
                        servicioDelTipo = servicio;
                        break;
                    }
                }
                
                // Si no hay servicio de ese tipo, crear uno por defecto
                if (servicioDelTipo == null) {
                    servicioDelTipo = crearServicioPorDefecto(tipo);
                    servicioDAO.guardar(servicioDelTipo);
                }
                
                orden.agregarServicio(servicioDelTipo);
            }
            
            orden.calcularTotal();
            ordenDAO.guardar(orden);
            actualizarTabla();
            
        } catch (ArchivoException e) {
            throw new DatosInvalidosException("Error al registrar orden: " + e.getMessage());
        }
    }
    
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
    
    public void consultarOrden(JTable tabla, JFrame parent) {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada >= 0) {
            int numeroOrden = (Integer) tabla.getValueAt(filaSeleccionada, 0);
            OrdenTrabajo orden = ordenDAO.buscarPorNumero(numeroOrden);
            
            if (orden != null) {
                mostrarDetallesOrden(orden, parent);
            }
        } else {
            JOptionPane.showMessageDialog(parent, 
                "Seleccione una orden de la tabla", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void modificarOrden(JTable tabla, JFrame parent) throws DatosInvalidosException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione una orden para modificar");
        }
        
        try {
            int numeroOrden = (Integer) tabla.getValueAt(filaSeleccionada, 0);
            OrdenTrabajo orden = ordenDAO.buscarPorNumero(numeroOrden);
            
            if (orden == null) {
                throw new DatosInvalidosException("Orden no encontrada");
            }
            
            if (orden.getEstado() == OrdenTrabajo.EstadoOrden.CERRADA) {
                throw new DatosInvalidosException("No se puede modificar una orden cerrada");
            }
            
            // Mostrar diálogo de modificación simple
            String nuevasObservaciones = JOptionPane.showInputDialog(parent, 
                "Observaciones:", 
                orden.getObservaciones());
            
            if (nuevasObservaciones != null) {
                orden.setObservaciones(nuevasObservaciones);
                ordenDAO.actualizar(orden);
                actualizarTabla();
                
                JOptionPane.showMessageDialog(parent, 
                    "Orden modificada exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (HeadlessException | ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al modificar orden: " + e.getMessage());
        }
    }
    
    public void cerrarOrden(JTable tabla) throws DatosInvalidosException {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            throw new DatosInvalidosException("Seleccione una orden para cerrar");
        }
        
        try {
            int numeroOrden = (Integer) tabla.getValueAt(filaSeleccionada, 0);
            OrdenTrabajo orden = ordenDAO.buscarPorNumero(numeroOrden);
            
            if (orden != null && orden.getEstado() == OrdenTrabajo.EstadoOrden.ABIERTA) {
                orden.cerrarOrden();
                ordenDAO.actualizar(orden);
                actualizarTabla();
            } else {
                throw new DatosInvalidosException("La orden ya está cerrada o no existe");
            }
        } catch (ArchivoException | DatosInvalidosException e) {
            throw new DatosInvalidosException("Error al cerrar orden: " + e.getMessage());
        }
    }
    
    private void mostrarDetallesOrden(OrdenTrabajo orden, JFrame parent) {
        String detalles = String.format("""
                                        ORDEN DE TRABAJO #%d
                                        
                                        Cliente: %s
                                        Tel\u00e9fono: %s
                                        Veh\u00edculo: %s
                                        Placa: %s
                                        Fecha Ingreso: %s
                                        Fecha Entrega: %s
                                        Estado: %s
                                        Observaciones: %s
                                        
                                        SERVICIOS:
                                        %s
                                        
                                        TOTAL: $%.2f""",
            orden.getNumeroOrden(),
            orden.getCliente().getNombre(),
            orden.getCliente().getTelefono(),
            orden.getVehiculo().getMarca() + " " + orden.getVehiculo().getModelo(),
            orden.getVehiculo().getPlaca(),
            FormateadorFecha.formatearParaMostrar(orden.getFechaIngreso()),
            orden.getFechaEntrega() != null ? FormateadorFecha.formatearParaMostrar(orden.getFechaEntrega()) : "Pendiente",
            orden.getEstado().getDescripcion(),
            orden.getObservaciones().isEmpty() ? "Sin observaciones" : orden.getObservaciones(),
            obtenerListaServicios(orden),
            orden.getCostoTotal()
        );
        
        JTextArea textArea = new JTextArea(detalles);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        
        JOptionPane.showMessageDialog(parent, scrollPane, 
            "Detalles de la Orden #" + orden.getNumeroOrden(), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String obtenerListaServicios(OrdenTrabajo orden) {
        StringBuilder sb = new StringBuilder();
        for (Servicio servicio : orden.getServicios()) {
            sb.append("- ").append(servicio.getNombreServicio())
              .append(" (").append(servicio.getTipo().getDescripcion()).append(")")
              .append(" - $").append(String.format("%.2f", servicio.getCosto())).append("\n");
        }
        return sb.toString();
    }
    
    private void cargarOrdenes() {
        try {
            ordenDAO.cargarDesdeArchivo();
        } catch (ArchivoException e) {
            JOptionPane.showMessageDialog(null, 
                "Error al cargar órdenes: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarTabla() {
        if (modeloTabla != null) {
            modeloTabla.setRowCount(0);
            List<OrdenTrabajo> ordenes = ordenDAO.obtenerTodas();
            
            for (OrdenTrabajo orden : ordenes) {
                Object[] fila = {
                    orden.getNumeroOrden(),
                    orden.getCliente().getNombre(),
                    orden.getVehiculo().getMarca() + " " + orden.getVehiculo().getModelo() + " (" + orden.getVehiculo().getPlaca() + ")",
                    FormateadorFecha.formatearParaMostrar(orden.getFechaIngreso()),
                    orden.getFechaEntrega() != null ? 
                        FormateadorFecha.formatearParaMostrar(orden.getFechaEntrega()) : "Pendiente",
                    orden.getEstado().getDescripcion(),
                    String.format("$%.2f", orden.getCostoTotal())
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
    public List<OrdenTrabajo> obtenerTodasLasOrdenes() {
        return ordenDAO.obtenerTodas();
    }
}
