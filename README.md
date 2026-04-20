# SafeDrive AI: Sistema ADAS y EDR con Edge AI

---

## 1. Descripción del Proyecto

**SafeDrive AI** es una solución de software orientada a democratizar la seguridad vial pasiva, transformando cualquier smartphone Android en un **Sistema Avanzado de Asistencia al Conductor (ADAS)** y un **Registrador de Datos de Eventos (EDR o "Caja Negra")**.

El proyecto surge de la necesidad de proteger a los más de 15 millones de vehículos en España que carecen del sistema **eCall** obligatorio. SafeDrive AI utiliza **Inteligencia Artificial en el borde (Edge AI)** a través de modelos TensorFlow Lite para procesar en tiempo real la telemetría del vehículo mediante una arquitectura de **Fusión Sensorial Pasiva** (acelerómetro, giroscopio, micrófono y GPS).

### Objetivos Principales:

-   **OE-01:** Detección de impactos con >95% de precisión teórica.

-   **OE-02:** Tiempo de respuesta SOS <15 segundos desde el impacto.

-   **OE-05:** Compatibilidad con Android 10+ y certificación para Android Auto.


## 2. Arquitectura General de la Aplicación

La aplicación la idea es seguir el patrón de arquitectura **MVVM (Model-View-ViewModel)** para separar la lógica de negocio de la interfaz de usuario:

Pero por desgracia, el desarrollo se ha visto afectado por la falta de dedicación del tiempo y recursos, lo que ha llevado a una implementación parcial del MVVM, con una fuerte dependencia de la lógica en el ViewModel y un acoplamiento directo entre la captura de sensores y la interfaz de usuario. 

Esto ha resultado en un código menos modular y más difícil de mantener, aunque se han logrado implementar las funcionalidades básicas de integración de sensores y detección de impactos.

## 3. Módulos Principales del Sistema (en desarrollo)

1.  **Dashboard de Telemetría:** Visualización en tiempo real de datos de sensores y estado del sistema.

2.  **Diagnóstico de permisos:** Gestión de permisos críticos (ubicación, micrófono, sensores) con flujos de solicitud claros.

3.  **Mapa:** Integración de OpenStreetMap para mostrar ubicación.

4.  **Caja Negra EDR:** Registro de eventos con detalles de telemetría pre y post-impacto, exportable en formato CSV.

## 4. Instrucciones para Ejecutar la App

1.  **Requisitos:** Android Studio Ladybug, dispositivo Android 10+ y archivo del modelo en `ml/scripts/safedrive_cu03_model.tflite`.

2.  **Clonación:** `git clone https://github.com/xji650/SafeDrive_AI.git`.

3.  **Sincronización:** Abrir en Android Studio y sincronizar archivos Gradle.

4.  **Ejecución:** Seleccionar dispositivo físico y pulsar 'Run'.


## 5. Estructura del Repositorio

```
/root
├── app/       # Código y documentación del App Android
│   ├── data/       # Local, Remote, Repository
│   │   ├── local/     # Room Database
│   │   │   ├── dao/       # Libro de órdenes (Data Access Object)
│   │   │   ├── entity/    # Modelos de datos (Room)
│   │   │   └── mapper/    # Conversión de datos (Room)
│   │   ├── remote/        # Firebase Firestore y Storage
│   │   └── repository/    # IncidentRepository (orquestador de datos)
│   │
│   ├── di/         # Inyección de dependencias
│   ├── domain/     # Dominio del sistema
│   │   ├── model/      # Modelos de negocio (Accidente, SensorData)
│   │   ├── usecase/    # Casos de uso (IncidentDetectorUC, SyncDataUC)
│   │   └── utils/      # Utilidades (Thresholds, Constants)
│   ├── sensors/    # SensorDataManager, ActivityReceiver
│   ├── ui/         # Vista del sistema + ViewModel (Dashboard, Map, EDR,...)
│   └── ml/         # Integración de modelos
│
├── ml/        # Código y documentación del Sistema de ML
│   ├── data/        # Datasets (dataset.zip)
│   ├── experiments/ # Resultados de entrenamiento (.txt)
│   ├── models/      # Modelos finales (.tflite)
│   ├── scripts/     # Notebooks (.ipynb)
│   └── ML_EXPERIMENTS.md
└── README.md  # Este archivo

```

---

## 6. Descripción de la Arquitectura del Backend

El sistema "SafeDrive AI" utiliza una arquitectura **Serverless** (sin servidor) basada en los servicios en la nube de **Google Firebase**. Se ha optado por un modelo **Híbrido y Offline-First**, donde el dispositivo móvil actúa como nodo principal de procesamiento y la nube actúa como repositorio seguro e inmutable.

El backend se compone de dos pilares fundamentales separados por el "peso" y el propósito de los datos:

* **A. Firebase Cloud Firestore (Base de Datos Operacional):**
    * **Tipo:** Base de datos NoSQL orientada a documentos.
    * **Función:** Almacenar el "Atestado Rápido" o metadatos del incidente.
    * **Datos que guarda:** Información estructurada y ligera, como la fuerza G máxima, velocidad al impacto, coordenadas GPS (latitud/longitud), amplitud de audio y el *timestamp* (identificador único).
    * **Justificación:** Al ser una base de datos ultrarrápida, permite a la aplicación móvil descargar y mostrar el historial de accidentes del usuario de forma casi instantánea, consumiendo un mínimo ancho de banda.

* **B. Firebase Cloud Storage (Repositorio Forense):**
    * **Tipo:** Almacenamiento de objetos y archivos.
    * **Función:** Almacenar la telemetría completa de la "Caja Negra" (EDR).
    * **Datos que guarda:** Archivos `.json` pesados que contienen la ventana de tiempo del accidente (por ejemplo, los 5 segundos previos y posteriores al impacto registrados a alta frecuencia).
    * **Justificación:** Aísla los datos pesados de la base de datos principal. Estos archivos actúan como prueba pericial detallada (aceleración en los 3 ejes, frenadas, etc.) y solo se descargan cuando un perito o investigador necesita reconstruir el accidente a nivel técnico.

## 7. Flujo de Datos del Sistema (Data Flow)

El ciclo de vida de los datos, desde que el vehículo arranca hasta que el accidente queda registrado de forma inmutable en la nube, sigue este flujo secuencial:

```
Sensores (Hardware) ➔ 2. Room + JSON (Almacenamiento Local) ➔ 3. Firebase (Nube)
```
### **Fase 1: Monitorización Continua (RAM buffer)**
1.  **Recolección:** Mientras la app está activa, los sensores del dispositivo (Acelerómetro, Micrófono, GPS) capturan datos continuamente a alta frecuencia (ej. 300 puntos por segundo).
2.  **Búfer Circular:** El `BlackBoxManager` almacena esta información temporalmente en la memoria RAM del teléfono en una "ventana rodante" (ej. los últimos 5 segundos). Los datos más antiguos se borran constantemente para no saturar la memoria.

### **Fase 2: Detección o *Trigger* (Disparo)**
3.  **Evaluación:** El caso de uso `IncidentDetectorUC` analiza los datos en tiempo real.
4.  **Impacto:** Si la fuerza G o la amplitud del audio supera el umbral crítico configurado, el sistema dispara la alerta de accidente y "congela" el búfer circular.

### **Fase 3: Persistencia Local (Offline-First)**
5.  **Volcado Forense:** El `BlackBoxManager` coge los 5 segundos congelados en RAM y los escribe en el disco duro del teléfono en formato físico (crea el archivo `EDR_EVENT_[timestamp].json`).
6.  **Registro Relacional:** Al mismo tiempo, se extraen los picos máximos (velocidad, fuerza G) y se guardan como una nueva fila en la base de datos local **Room** (SQLite) a través del `IncidentDao`. El campo `isSynced` se inicializa en `false`.

### **Fase 4: Sincronización con la Nube (Backend)**
7.  **Comprobación de Red:** El sistema invoca el método `syncWithCloud()` del repositorio para intentar subir los datos. Si no hay internet, el flujo se pausa aquí y se reintentará en el futuro sin perder datos.
8.  **Subida Pesada (Storage):** Si hay conexión, la app busca el archivo `.json` en el almacenamiento local y lo sube a la ruta de **Firebase Storage** (`/telemetry/id_vehiculo/...`).
9.  **Subida Ligera (Firestore):** Tras asegurar el archivo, la app envía los metadatos del accidente a la colección de **Firestore** (`/vehiculos/id_vehiculo/accidentes/...`).
10. **Confirmación:** Una vez Firebase confirma la recepción de ambos, la base de datos local (Room) actualiza el estado del accidente marcando `isSynced = true`.

---
