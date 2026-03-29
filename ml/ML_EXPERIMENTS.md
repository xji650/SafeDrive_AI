# SafeDrive AI - Definición del Problema y Experimentos Iniciales

## 1. Problema a Resolver (Definición del problema ML)

El objetivo es clasificar eventos telemétricos en tiempo real para detectar accidentes de tráfico. 

El sistema utiliza Inteligencia Artificial en el borde (Edge AI) a través de modelos
TensorFlow Lite para procesar en tiempo real y de forma local la telemetría del vehículo.
Mediante una arquitectura de Fusión Sensorial Pasiva (acelerómetro, giroscopio,
micrófono y GPS), la aplicación detecta colisiones con alta precisión, reduciendo
drásticamente los falsos positivos (por ejemplo, caídas del teléfono o frenazos bruscos sin
impacto).

Se trata de un problema de **Aprendizaje Supervisado de Clasificación Multiclase**.

- **Entrada:** 4 variables (Peak_G, Jerk, Sonido, Giroscopio)
- **Salida:** 3 categorías (Normal, Incidente, Colisión)

---

## 2. Modelos Candidatos

Para este MVP se evaluaron las siguientes opciones:

- **Árboles de Decisión:**  
  Descartados por baja capacidad de generalización en señales continuas.

- **Regresión Logística:**  
  Descartada por la alta no-linealidad de los impactos físicos.

- **Red Neuronal Densa (Seleccionada):**  
  Elegida por su eficiencia en dispositivos móviles y su capacidad para modelar patrones complejos de sensores mediante TensorFlow Lite.

---

## 3. Herramientas Utilizadas

- **Lenguaje:** Python 3
- **Frameworks:** TensorFlow y Keras
- **Procesamiento:** Pandas y Scikit-learn
- **Conversión:** TFLite Converter (despliegue en Android)

---

## 4. Pipeline de Entrenamiento (Configuración)

- **Generación (Sección 1):**  
  Creación de datos sintéticos basados en física de impactos.

- **Ingeniería de datos:**  
  Concatenación y etiquetado automático de clases.

- **División:**  
  Split 60/20/20 (Train / Validation / Test)

- **Entrenamiento (Sección 2):**  
  Red neuronal con 2 capas ocultas:
    - 24 neuronas (ReLU)
    - 12 neuronas (ReLU)

- **Validación:**  
  Monitoreo de la pérdida durante 40 épocas.

- **Exportación:**  
  Generación del modelo en formato `.tflite`.

---

## 5. Experimentos Iniciales Realizados

### Experimento #1: Configuración Base (MVP)

- **Script:** `scripts/safedrive_trainer.ipynb`

### Parámetros principales

- Épocas: 40
- Optimizador: Adam
- Batch Size: 16

### Resultados obtenidos

- **Accuracy:** 76.67%
- **Loss:** 0.6077

### Análisis

El modelo demuestra:

- Alta robustez en la detección de **Colisiones Graves (Label 2)**.
- Confusión marginal entre **Baches** e **Incidentes leves**, consistente con la similitud física en las fuerzas G iniciales.
