# SafeDrive AI: Sistema ADAS y EDR con Edge AI

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
│   ├── sensors/    # SensorDataManager, ActivityReceiver
│   ├── ui/         # Dashboard, EDR (BlackBox), Settings
│   ├── viewmodel/  # Lógica de las vistas
│   └── ml/         # Integración de modelos
│
├── ml/        # Código y documentación del Sistema de ML
│   ├── data/        # Datasets (dataset.zip)
│   ├── experiments/ # Resultados de entrenamiento (.txt)
│   ├── models/      # Modelos finales (.tflite)
│   ├── scripts/     # Notebooks (.ipynb)
│   └── ML_EXPERIMENTS.md
└── README.md  # Este archivo
