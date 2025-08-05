Sistema de Gestión de Taller Automotriz
    
Un sistema integral para la gestión de talleres automotrices desarrollado en Java

Descripción
El Sistema de Gestión de Taller Automotriz es una aplicación de escritorio desarrollada en Java que permite administrar eficientemente las operaciones de un taller mecánico. Diseñado especialmente para talleres pequeños y medianos en Costa Rica, ofrece una solución completa para la gestión de clientes, vehículos, servicios y órdenes de trabajo.
Problema que Resuelve
Los talleres automotrices tradicionalmente manejan su información de forma manual (cuadernos, hojas sueltas), lo que genera:
•	Pérdida de información importante
•	 Tiempo excesivo buscando datos
•	 Errores en cálculos de costos
•	Imposibilidad de generar reportes
Este sistema digitaliza y automatiza estos procesos, mejorando la productividad y la experiencia del cliente.

 Características
Gestión de Clientes
•	 Registro completo de clientes
•	 Validación de datos (teléfonos costarricenses)
•	 Búsqueda y modificación de información
•	 Historial de vehículos por cliente
 Gestión de Vehículos
•	 Soporte para autos y motocicletas
•	 Validación de placas costarricenses (ABC-123, AB-1234)
•	 Asociación automática con clientes
•	Control de duplicados
Catálogo de Servicios
•	 Cuatro tipos: Mecánica, Pintura, Revisión, Otros
•	 Gestión de precios dinámicos
•	 Modificación de servicios existentes
•	 Validación de costos
Órdenes de Trabajo
•	 Creación de órdenes integrales
•	 Seguimiento de estados (Abierta/Cerrada)
•	 Cálculo automático de costos
•	 Generación de reportes detallados
•	 Modificación de órdenes abiertas
 Persistencia de Datos
•	Almacenamiento en archivos CSV
•	 Respaldos automáticos
•	 Sincronización en tiempo real
•	 Portabilidad de datos

 Arquitectura
El sistema implementa el patrón MVC (Model-View-Controller) para garantizar:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    VIEW     │◄──►│ CONTROLLER  │◄──►│    MODEL    │
│ (Swing UI)  │    │ (Business)  │    │ (Entities)  │
└─────────────┘    └─────────────┘    └─────────────┘
                           │
                   ┌─────────────┐
                   │    DAO      │
                   │ (CSV Files) │
                   └─────────────┘
 Estructura del Proyecto
src/
├── taller/automotriz/
│   ├── controlador/          # Lógica de negocio
│   ├── modelo/               # Entidades del dominio
│   ├── persistencia/         # Acceso a datos (DAO)
│   ├── excepciones/          # Excepciones personalizadas
│   ├── utiles/               # Utilidades y validadores
│   └── PantallaPrincipal.java # Interfaz principal
└── imagenes/                 # Recursos gráficos


Uso
 Primer Uso
1.	Ejecuta la aplicación
2.	Se crean automáticamente las carpetas de datos
3.	Registra tu primer cliente
4.	Agrega vehículos asociados
5.	Crea tu primera orden de trabajo
 Navegación
La interfaz está organizada en 4 pestañas principales:
Clientes
•	Registrar: Completa nombre, teléfono (8 dígitos), dirección
•	Consultar: Selecciona un cliente de la tabla
•	Modificar: Doble clic o botón modificar
•	Eliminar: Selecciona y confirma eliminación
Vehículos
•	Registrar: Marca, modelo, año, placa y cliente asociado
•	Consultar: Ver detalles de vehículos registrados
•	Modificar: Cambiar datos o reasignar cliente
•	Eliminar: Remover vehículos del sistema
 Servicios
•	Registrar: Nombre, tipo (Mecánica/Pintura/Revisión/Otros), costo
•	Consultar: Ver catálogo completo de servicios
•	Modificar: Actualizar precios y descripciones
•	Eliminar: Remover servicios obsoletos
 Órdenes de Trabajo
•	Crear: Cliente + Vehículo + Servicios + Observaciones
•	Consultar: Ver detalles completos con costos
•	Modificar: Cambiar servicios, fechas, observaciones
•	Cerrar: Finalizar orden y establecer fecha de entrega

 Capturas de Pantalla
Pantalla Principal



<img width="574" height="337" alt="Captura de pantalla 2025-08-04 191136" src="https://github.com/user-attachments/assets/0e37db56-dbb1-4fdb-9010-eefaf21d305e" />

Gestión de Clientes




<img width="833" height="583" alt="Captura de pantalla 2025-08-04 191052" src="https://github.com/user-attachments/assets/fc677073-fd11-4989-816e-2bb37e493a14" />


Órdenes de Trabajo




<img width="845" height="582" alt="Captura de pantalla 2025-08-04 191116" src="https://github.com/user-attachments/assets/af95f42a-9d04-4f79-a2c1-28a493702f6f" />

 

Configuración
 Estructura de Archivos
TallerAutomotriz/
├── TallerAutomotriz.jar
├── datos/                    # Archivos de datos
│   ├── clientes.csv
│   ├── vehiculos.csv
│   ├── servicios.csv
│   ├── ordenes_trabajo.csv
│   └── servicios_orden.csv
└── backup/                   # Respaldos automáticos

 Desarrollo
 Tecnologías Utilizadas
•	Lenguaje: Java 17
•	GUI: Java Swing
•	Persistencia: Archivos CSV
•	Arquitectura: MVC Pattern
•	Manejo de Fechas: LocalDate API
•	Validaciones: Expresiones regulares
 Patrones de Diseño
•	MVC (Model-View-Controller)
•	DAO (Data Access Object)
•	Factory Method (para tipos de vehículo)
•	Template Method (clases abstractas)

 Contacto
 Desarrolladores 
•	Nombre: Jafeth Castellón Gómez y Indania Martínez Hernández.



