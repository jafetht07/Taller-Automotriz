# Taller Automotriz
> Aplicación de escritorio en Java Swing para gestionar clientes, vehículos, servicios y órdenes de trabajo de un taller automotriz, con persistencia en archivos CSV.

## Español

### ¿Qué es?

Taller Automotriz es una aplicación de escritorio desarrollada en Java con Swing para administrar la operación básica de un taller mecánico: registro de clientes, vehículos asociados, catálogo de servicios y órdenes de trabajo. Es un proyecto académico creado con NetBeans (proyecto Java SE basado en Ant).

Toda la información se guarda en archivos CSV dentro de la carpeta `datos/`, creada automáticamente al iniciar. No requiere base de datos, servidor ni conexión a internet.

### Características

**Clientes**
- Registro, consulta, modificación y eliminación con confirmación.
- Validación de campos obligatorios y de teléfono de 8 dígitos.
- Identificador visible con formato `CLI-0001`.
- Tabla de clientes no editable; al seleccionar una fila se cargan sus datos en el formulario.

**Vehículos**
- Registro, consulta, modificación y eliminación.
- Validación de placas con formato `ABC-123` o `AB-1234`.
- Validación del año entre 1900 y el año actual + 1.
- Rechazo de placas duplicadas.
- Asociación obligatoria a un cliente y filtro de vehículos por cliente seleccionado.
- Soporte de modelo para autos y motocicletas; la interfaz de registro siempre crea `Auto` (ver Limitaciones).

**Servicios**
- Catálogo con cuatro tipos: Mecánica, Pintura, Revisión y Otros.
- Registro, consulta, modificación y eliminación.
- Validación de costo numérico mayor que cero.
- Tabla con ID, nombre, tipo y costo.

**Órdenes de trabajo**
- Creación a partir de un cliente, un vehículo, uno o más tipos de servicio, fechas y observaciones.
- Total calculado como la suma de los servicios agregados a la orden.
- Estados `ABIERTA` y `CERRADA`; al cerrar se registra la fecha de entrega.
- Consulta de detalle en un cuadro de diálogo.
- Modificación de órdenes abiertas limitada a observaciones y costo total (ver Limitaciones).

**General**
- Interfaz de una sola ventana con cuatro pestañas: Clientes, Servicios, Ordenes de trabajo y Vehículos.
- Datos de ejemplo incluidos en `datos/`.
- Sin dependencias externas: solo la biblioteca estándar de Java.

### Arquitectura

El proyecto sigue una separación tipo MVC, con una capa de persistencia propia. La vista concentra el armado de la interfaz y el cableado de eventos; los controladores aplican validaciones y coordinan las operaciones; los DAO mantienen las entidades en memoria y reescriben el archivo CSV correspondiente en cada cambio.

```
PantallaPrincipal (vista Swing, .form)
        │  eventos de botones y combos
        ▼
controlador.*  ──► utiles.Validador / FormateadorFecha
        │
        ▼
persistencia.*DAO ──► ManejadorArchivos ──► datos/*.csv
        ▲
        │
modelo.* (Persona, Cliente, Vehiculo, Auto, Moto,
         Servicio, OrdenTrabajo, Facturable)
```

| Paquete | Rol |
|---|---|
| `taller.automotriz` | `PantallaPrincipal`, ventana principal (`JFrame`) y su archivo `.form` del editor visual de NetBeans. Inicializa los DAO, carga los datos y conecta los eventos. |
| `modelo` | Entidades del dominio. `Persona` (abstracta) → `Cliente`; `Vehiculo` (abstracta) → `Auto` y `Moto`; `Servicio` con el enum `TipoServicio`; `OrdenTrabajo` con el enum `EstadoOrden` e implementación de `Facturable`. Incluyen `toCSV`/`fromCSV` en varias clases. |
| `controlador` | `ControladorClientes`, `ControladorVehiculos`, `ControladorServicios`, `ControladorOrdenes`. Validan entradas, configuran los `DefaultTableModel` de las tablas y llaman a los DAO. |
| `persistencia` | `ClienteDAO`, `VehiculoDAO`, `ServicioDAO`, `OrdenTrabajoDAO` y `ManejadorArchivos` (lectura/escritura de texto y creación de `datos/`). |
| `excepciones` | Excepciones comprobadas: `ArchivoException`, `DatosInvalidosException`, `ClienteNoEncontradoException`. |
| `utiles` | `Validador` (expresiones regulares para teléfono, placa, número y fecha) y `FormateadorFecha` (`yyyy-MM-dd` para archivos, `dd/MM/yyyy` para mostrar). |

En la práctica el MVC es pragmático: `PantallaPrincipal` no solo muestra la interfaz, también instancia DAO, carga los archivos al inicio y ejecuta algunas operaciones directamente (por ejemplo, el diálogo de modificación de clientes).

### Modelo de datos

Las entidades se persisten en cinco archivos de texto plano:

| Entidad | Campos persistidos | Archivo |
|---|---|---|
| `Cliente` | `idCliente, nombre, telefono, direccion` | `clientes.csv` |
| `Vehiculo` (`Auto`/`Moto`) | `marca, modelo, año, placa, idCliente, tipo` | `vehiculos.csv` |
| `Servicio` | `idServicio, nombreServicio, tipo, costo` | `servicios.csv` |
| `OrdenTrabajo` | `numeroOrden, idCliente, placa, fechaIngreso, fechaEntrega, costoTotal, estado, observaciones` | `ordenes_trabajo.csv` |
| Relación orden–servicio | `numeroOrden, idServicio` | `servicios_orden.csv` |

Formato de los CSV:

- Delimitador: coma (`,`). Sin línea de encabezado y sin comillas.
- Una entidad por línea; cada operación de escritura regenera el archivo completo.
- Fechas en `yyyy-MM-dd`; la fecha de entrega puede quedar vacía.
- `tipo` de vehículo: `Auto` o `Moto`. Al cargar, cualquier valor distinto de `Auto` se interpreta como `Moto`.
- `tipo` de servicio: nombre del enum (`MECANICA`, `PINTURA`, `REVISION`, `OTROS`).
- `estado` de orden: `ABIERTA` o `CERRADA`.
- En `ordenes_trabajo.csv`, las comas dentro de las observaciones se sustituyen por `;` para no romper el formato (la conversión es reversible al cargar, pero pierde la distinción con puntos y coma originales).
- `servicios_orden.csv` implementa la relación muchos-a-muchos entre órdenes y servicios.

Ejemplos reales incluidos en el repositorio:

```text
# clientes.csv
1,Jafeth Castellon Gomez,70701809,La Virgen Srapiqui

# vehiculos.csv
Terios,Bego,2009,QWR-012,1,Auto

# servicios.csv
1,Cambio de Compensadores,MECANICA,30000.0

# ordenes_trabajo.csv
1,1,QWR-012,2025-08-01,2025-08-02,30000.0,ABIERTA,se cambian los 4 compensadores 30;000 de mano de obra y 80;000 de repuestos

# servicios_orden.csv
1,1
```

Al iniciar, la carga se hace en este orden: clientes, vehículos, servicios y órdenes. Una orden cuya referencia a cliente o vehículo no exista se descarta silenciosamente durante la carga. Los identificadores se generan con contadores estáticos que se ajustan al máximo ID leído del archivo.

### Requisitos

- **JDK 24** (`javac.source=24` y `javac.target=24` en `nbproject/project.properties`). Verificado con JDK 24.0.1.
- **NetBeans** con soporte para proyectos Java SE basados en Ant (el proyecto es de tipo `j2seproject` y usa el editor visual `.form`).
- **Apache Ant** si se compila desde la línea de comandos sin NetBeans.
- Entorno gráfico disponible: la aplicación es una interfaz Swing, no funciona en modo headless.
- No hay dependencias de terceros (`javac.classpath` está vacío).

### Cómo compilar y ejecutar

**Con NetBeans**

1. Abrir el proyecto (`File > Open Project`) seleccionando la carpeta del repositorio.
2. Ejecutar con `Run Project` (F6) o compilar con `Clean and Build` (Shift+F6).
3. NetBeans genera `dist/Taller.Automotriz.jar` con la clase principal `taller.automotriz.PantallaPrincipal`.

**Con Ant**

```bash
ant clean jar    # compila y genera dist/Taller.Automotriz.jar
ant run          # compila y ejecuta la aplicación
```

**Sin Ant, con el JDK**

Linux/macOS:

```bash
mkdir -p build/classes dist
javac -encoding UTF-8 --release 24 -d build/classes $(find src -name "*.java")
jar --create --file dist/Taller.Automotriz.jar \
    --main-class taller.automotriz.PantallaPrincipal -C build/classes .
java -jar dist/Taller.Automotriz.jar
```

Windows (PowerShell):

```powershell
New-Item -ItemType Directory -Force build\classes, dist | Out-Null
javac -encoding UTF-8 --release 24 -d build\classes (Get-ChildItem -Recurse src -Filter *.java).FullName
jar --create --file dist\Taller.Automotriz.jar --main-class taller.automotriz.PantallaPrincipal -C build\classes .
java -jar dist\Taller.Automotriz.jar
```

También se puede ejecutar sin empaquetar:

```bash
java -cp build/classes taller.automotriz.PantallaPrincipal
```

Notas:

- Ejecutar desde la raíz del proyecto, porque la ruta `datos/` es relativa al directorio de trabajo. Si la carpeta no existe, `ManejadorArchivos` la crea; si los CSV no existen, se inicia con tablas vacías.
- El nombre exacto del artefacto es `Taller.Automotriz.jar` (con punto), según `dist.jar` en `nbproject/project.properties`.
- El JAR no está incluido en el repositorio: hay que compilarlo.

### Estructura del proyecto

```text
Taller-Automotriz/
├── build.xml                     # Script Ant del proyecto
├── manifest.mf                   # Main-Class se agrega durante el build
├── .gitignore                    # Ignora build/ y nbproject/private/
├── nbproject/
│   ├── project.xml
│   ├── project.properties        # source/target 24, main.class, dist.jar
│   ├── build-impl.xml
│   └── genfiles.properties
├── datos/                        # Persistencia CSV (datos de ejemplo)
│   ├── clientes.csv
│   ├── vehiculos.csv
│   ├── servicios.csv
│   ├── ordenes_trabajo.csv
│   └── servicios_orden.csv
└── src/
    └── taller/automotriz/
        ├── PantallaPrincipal.java   # Ventana principal (~88 KB)
        ├── PantallaPrincipal.form   # Diseño en el editor visual de NetBeans
        ├── controlador/             # 4 controladores
        ├── modelo/                  # 8 clases/interfaces del dominio
        ├── persistencia/            # 4 DAO + ManejadorArchivos
        ├── excepciones/             # 3 excepciones comprobadas
        └── utiles/                  # Validador y FormateadorFecha
```

### Limitaciones y mejoras posibles

**Persistencia y consistencia**
- Persistencia en archivos planos, sin base de datos ni transacciones. Cada cambio reescribe el archivo completo, sin escritura atómica ni bloqueo; dos instancias simultáneas pueden sobrescribirse.
- No hay respaldos automáticos ni carpeta `backup/` (esa función no está implementada en el código).
- El formato no soporta comillas ni escapes: una coma en un nombre o una dirección rompe el parseo de esa línea. Solo las observaciones de la orden se protegen sustituyendo `,` por `;`.
- `FileWriter` y `FileReader` usan la codificación por defecto de la plataforma, mientras que el código fuente está en UTF-8; los acentos pueden corromperse al mover los CSV entre sistemas.
- Las eliminaciones no son en cascada: al borrar un cliente o un vehículo quedan órdenes huérfanas que se descartan al volver a cargar. Tampoco se valida que un vehículo o servicio esté en uso antes de eliminarlo.
- Los contadores de ID son estáticos y se inicializan con el máximo leído; tras reiniciar, un ID eliminado puede reutilizarse.

**Funcionalidad**
- El registro de órdenes no permite elegir servicios concretos ni cantidades: por cada tipo marcado se toma el primer servicio del catálogo de ese tipo o se crea uno por defecto con costo fijo (Mecánica 25 000, Pintura 50 000, Revisión 15 000, Otros 20 000).
- El diálogo de modificación de órdenes cambia observaciones y total en memoria, pero no llama a `OrdenTrabajoDAO.actualizar`, por lo que los cambios se pierden al reiniciar. Además, el total editado puede dejar de coincidir con la suma de servicios.
- El campo "Costo total" del formulario de órdenes nunca se rellena con el total calculado.
- Los botones de estado (Abierta/Cerrada) no se aplican al registrar: toda orden nueva se crea como `ABIERTA` y solo se cierra con la acción correspondiente.
- La interfaz de vehículos siempre registra `Auto`; `Moto` existe en el modelo y se puede leer de un CSV editado a mano, pero no crearse desde la aplicación. Los campos `numeroPuertas` y `cilindraje` no se persisten.
- No hay eliminación de órdenes desde la interfaz, aunque el DAO la soporta.
- No existen reportes, facturas ni exportación: `Facturable.generarFactura()` está implementado en el modelo pero no se expone en la interfaz. La consulta de orden solo muestra un detalle en pantalla.
- Los menús `Archivo > Salir` y `Ayuda > Acerca de` no tienen acción asociada.
- No hay búsqueda, filtros, paginación ni validación de duplicados de clientes.
- El formulario usa montos con prefijo `$` sin configuración de moneda ni locale.

**Calidad y proyecto**
- No hay pruebas automatizadas, integración continua ni archivo de licencia.
- Hay código generado por el editor visual mezclado con lógica y usos de `null` como opción vacía en los combos.
- La carga de archivos ocurre en el constructor de la vista y los controladores también pueden recargar datos, lo que duplica responsabilidades.

**Mejoras sugeridas**
- Migrar a SQLite o JDBC con transacciones y claves foráneas; o, como mínimo, usar una librería CSV (OpenCSV) y escritura atómica con respaldo previo.
- Definir reglas de borrado en cascada o bloqueo cuando existan referencias.
- Permitir seleccionar servicios y cantidades, y persistir correctamente la edición de órdenes.
- Generar facturas y reportes exportables (PDF/CSV) y agregar búsqueda y filtros.
- Añadir pruebas unitarias con JUnit y migrar el build a Maven o Gradle.
- Separar la lógica de la vista y centralizar la carga inicial en un servicio de aplicación.

## English

### What is it?

Taller Automotriz is a Java Swing desktop application for managing the basic operations of an auto repair shop: customers, their vehicles, a service catalog, and work orders. It is an academic project built with NetBeans as an Ant-based Java SE project.

All data is stored in CSV files inside the `datos/` folder, which is created automatically at startup. No database, server, or internet connection is required.

### Features

**Customers**
- Create, view, update, and delete records with confirmation.
- Required-field validation and 8-digit phone validation.
- Visible identifier formatted as `CLI-0001`.
- Read-only customer table; selecting a row loads its data into the form.

**Vehicles**
- Create, view, update, and delete records.
- License plate validation for the `ABC-123` or `AB-1234` formats.
- Model year validation between 1900 and the current year + 1.
- Duplicate plates are rejected.
- Mandatory customer association and filtering of vehicles by the selected customer.
- The model supports cars and motorcycles; the registration form always creates an `Auto` (see Limitations).

**Services**
- Catalog with four types: Mecánica, Pintura, Revisión, and Otros.
- Create, view, update, and delete records.
- Numeric cost validation, must be greater than zero.
- Table showing ID, name, type, and cost.

**Work orders**
- Created from a customer, a vehicle, one or more service types, dates, and notes.
- Total computed as the sum of the services added to the order.
- `ABIERTA` and `CERRADA` states; closing an order records the delivery date.
- Detail view in a dialog.
- Editing an open order is limited to notes and total cost (see Limitations).

**General**
- Single-window interface with four tabs: Clientes, Servicios, Ordenes de trabajo, and Vehículos.
- Sample data included in `datos/`.
- No external dependencies: Java standard library only.

### Architecture

The project follows an MVC-style separation with its own persistence layer. The view builds the interface and wires events; controllers apply validation and coordinate operations; DAOs keep entities in memory and rewrite the corresponding CSV file on every change.

```
PantallaPrincipal (Swing view, .form)
        │  button and combo events
        ▼
controlador.*  ──► utiles.Validador / FormateadorFecha
        │
        ▼
persistencia.*DAO ──► ManejadorArchivos ──► datos/*.csv
        ▲
        │
modelo.* (Persona, Cliente, Vehiculo, Auto, Moto,
         Servicio, OrdenTrabajo, Facturable)
```

| Package | Role |
|---|---|
| `taller.automotriz` | `PantallaPrincipal`, the main `JFrame`, plus its NetBeans GUI Builder `.form` file. It initializes DAOs, loads data, and connects events. |
| `modelo` | Domain entities. `Persona` (abstract) → `Cliente`; `Vehiculo` (abstract) → `Auto` and `Moto`; `Servicio` with the `TipoServicio` enum; `OrdenTrabajo` with the `EstadoOrden` enum and a `Facturable` implementation. Several classes include `toCSV`/`fromCSV`. |
| `controlador` | `ControladorClientes`, `ControladorVehiculos`, `ControladorServicios`, `ControladorOrdenes`. They validate input, configure the tables' `DefaultTableModel`, and call DAOs. |
| `persistencia` | `ClienteDAO`, `VehiculoDAO`, `ServicioDAO`, `OrdenTrabajoDAO`, and `ManejadorArchivos` (text I/O and creation of `datos/`). |
| `excepciones` | Checked exceptions: `ArchivoException`, `DatosInvalidosException`, `ClienteNoEncontradoException`. |
| `utiles` | `Validador` (regular expressions for phone, plate, number, and date) and `FormateadorFecha` (`yyyy-MM-dd` for files, `dd/MM/yyyy` for display). |

In practice MVC is pragmatic: `PantallaPrincipal` not only renders the UI but also instantiates DAOs, loads files at startup, and performs some operations directly (for example, the customer edit dialog).

### Data model

Entities are persisted in five plain-text files:

| Entity | Persisted fields | File |
|---|---|---|
| `Cliente` | `idCliente, nombre, telefono, direccion` | `clientes.csv` |
| `Vehiculo` (`Auto`/`Moto`) | `marca, modelo, año, placa, idCliente, tipo` | `vehiculos.csv` |
| `Servicio` | `idServicio, nombreServicio, tipo, costo` | `servicios.csv` |
| `OrdenTrabajo` | `numeroOrden, idCliente, placa, fechaIngreso, fechaEntrega, costoTotal, estado, observaciones` | `ordenes_trabajo.csv` |
| Order–service relation | `numeroOrden, idServicio` | `servicios_orden.csv` |

CSV format:

- Comma-delimited (`,`). No header row and no quoting.
- One entity per line; every write regenerates the whole file.
- Dates use `yyyy-MM-dd`; the delivery date may be empty.
- Vehicle `tipo`: `Auto` or `Moto`. On load, any value other than `Auto` is treated as `Moto`.
- Service `tipo`: enum name (`MECANICA`, `PINTURA`, `REVISION`, `OTROS`).
- Order `estado`: `ABIERTA` or `CERRADA`.
- In `ordenes_trabajo.csv`, commas inside notes are replaced with `;` to keep the format valid (the replacement is reverted on load, but the distinction from original semicolons is lost).
- `servicios_orden.csv` implements the many-to-many relation between orders and services.

Real examples included in the repository:

```text
# clientes.csv
1,Jafeth Castellon Gomez,70701809,La Virgen Srapiqui

# vehiculos.csv
Terios,Bego,2009,QWR-012,1,Auto

# servicios.csv
1,Cambio de Compensadores,MECANICA,30000.0

# ordenes_trabajo.csv
1,1,QWR-012,2025-08-01,2025-08-02,30000.0,ABIERTA,se cambian los 4 compensadores 30;000 de mano de obra y 80;000 de repuestos

# servicios_orden.csv
1,1
```

At startup, loading runs in this order: customers, vehicles, services, then orders. An order whose customer or vehicle reference does not exist is silently dropped during loading. Identifiers are generated by static counters that are advanced to the highest ID read from the file.

### Requirements

- **JDK 24** (`javac.source=24` and `javac.target=24` in `nbproject/project.properties`). Verified with JDK 24.0.1.
- **NetBeans** with support for Ant-based Java SE projects (the project is a `j2seproject` and uses the `.form` visual editor).
- **Apache Ant** if building from the command line without NetBeans.
- A graphical environment: the application is a Swing UI and does not run headless.
- No third-party dependencies (`javac.classpath` is empty).

### Build and run

**With NetBeans**

1. Open the project (`File > Open Project`) and select the repository folder.
2. Run with `Run Project` (F6) or build with `Clean and Build` (Shift+F6).
3. NetBeans produces `dist/Taller.Automotriz.jar` with main class `taller.automotriz.PantallaPrincipal`.

**With Ant**

```bash
ant clean jar    # compiles and produces dist/Taller.Automotriz.jar
ant run          # compiles and runs the application
```

**Without Ant, using only the JDK**

Linux/macOS:

```bash
mkdir -p build/classes dist
javac -encoding UTF-8 --release 24 -d build/classes $(find src -name "*.java")
jar --create --file dist/Taller.Automotriz.jar \
    --main-class taller.automotriz.PantallaPrincipal -C build/classes .
java -jar dist/Taller.Automotriz.jar
```

Windows (PowerShell):

```powershell
New-Item -ItemType Directory -Force build\classes, dist | Out-Null
javac -encoding UTF-8 --release 24 -d build\classes (Get-ChildItem -Recurse src -Filter *.java).FullName
jar --create --file dist\Taller.Automotriz.jar --main-class taller.automotriz.PantallaPrincipal -C build\classes .
java -jar dist\Taller.Automotriz.jar
```

You can also run without packaging:

```bash
java -cp build/classes taller.automotriz.PantallaPrincipal
```

Notes:

- Run from the repository root, because the `datos/` path is relative to the working directory. If the folder does not exist, `ManejadorArchivos` creates it; if the CSV files are missing, the app starts with empty tables.
- The exact artifact name is `Taller.Automotriz.jar` (with a dot), as defined by `dist.jar` in `nbproject/project.properties`.
- The JAR is not committed to the repository; it must be built.

### Project structure

```text
Taller-Automotriz/
├── build.xml                     # Ant project script
├── manifest.mf                   # Main-Class is added during the build
├── .gitignore                    # Ignores build/ and nbproject/private/
├── nbproject/
│   ├── project.xml
│   ├── project.properties        # source/target 24, main.class, dist.jar
│   ├── build-impl.xml
│   └── genfiles.properties
├── datos/                        # CSV persistence (sample data)
│   ├── clientes.csv
│   ├── vehiculos.csv
│   ├── servicios.csv
│   ├── ordenes_trabajo.csv
│   └── servicios_orden.csv
└── src/
    └── taller/automotriz/
        ├── PantallaPrincipal.java   # Main window (~88 KB)
        ├── PantallaPrincipal.form   # NetBeans GUI Builder layout
        ├── controlador/             # 4 controllers
        ├── modelo/                  # 8 domain classes/interfaces
        ├── persistencia/            # 4 DAOs + ManejadorArchivos
        ├── excepciones/             # 3 checked exceptions
        └── utiles/                  # Validador and FormateadorFecha
```

### Limitations and possible improvements

**Persistence and consistency**
- Flat-file persistence with no database or transactions. Every change rewrites the entire file, without atomic writes or locking; two concurrent instances can overwrite each other.
- No automatic backups and no `backup/` folder (that feature is not implemented in the code).
- The format supports neither quoting nor escaping: a comma in a name or address breaks parsing for that line. Only order notes are protected by replacing `,` with `;`.
- `FileWriter` and `FileReader` use the platform default charset while the source is UTF-8; accents can be corrupted when moving CSV files between systems.
- Deletions are not cascaded: deleting a customer or vehicle leaves orphan orders that are dropped on the next load. There is also no check preventing deletion of a vehicle or service that is still in use.
- ID counters are static and initialized from the highest loaded value; after a restart, a deleted ID can be reused.

**Functionality**
- Order creation does not allow selecting specific services or quantities: for each checked type, the first catalog service of that type is used, or a default one is created with a fixed cost (Mecánica 25,000; Pintura 50,000; Revisión 15,000; Otros 20,000).
- The order edit dialog changes notes and total in memory but never calls `OrdenTrabajoDAO.actualizar`, so changes are lost on restart. The edited total can also diverge from the sum of services.
- The "Costo total" field in the order form is never populated with the computed total.
- The state radio buttons are not applied on creation: every new order is `ABIERTA` and can only be closed through the dedicated action.
- The vehicle form always registers an `Auto`; `Moto` exists in the model and can be read from a hand-edited CSV, but cannot be created from the app. The `numeroPuertas` and `cilindraje` fields are not persisted.
- Orders cannot be deleted from the UI, although the DAO supports it.
- There are no reports, invoices, or exports: `Facturable.generarFactura()` is implemented in the model but not exposed in the UI. Viewing an order only shows an on-screen detail dialog.
- The `Archivo > Salir` and `Ayuda > Acerca de` menu items have no associated action.
- There is no search, filtering, pagination, or duplicate-customer validation.
- Amounts are shown with a `$` prefix with no currency or locale configuration.

**Quality and project setup**
- No automated tests, continuous integration, or license file.
- GUI Builder-generated code is mixed with logic, and `null` is used as the empty option in combos.
- File loading happens in the view constructor, while controllers can also reload data, duplicating responsibilities.

**Suggested improvements**
- Migrate to SQLite or JDBC with transactions and foreign keys; at minimum, use a CSV library (OpenCSV) and atomic writes with a previous backup.
- Define cascade or restrict rules for deletions when references exist.
- Allow selecting specific services and quantities, and correctly persist order edits.
- Generate exportable invoices and reports (PDF/CSV) and add search and filters.
- Add JUnit tests and migrate the build to Maven or Gradle.
- Separate logic from the view and centralize initial loading in an application service.

---

Autor: Jafeth (GitHub: [@jafetht07](https://github.com/jafetht07))
