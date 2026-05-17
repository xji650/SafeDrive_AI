# Documentación de Experimentación ML (Versión MLP Básico)

Este documento detalla el proceso de diseño, generación de datos y entrenamiento del modelo de detección de accidentes para el sistema SafeDrive CU-03.

## 1. Metodología de Experimentación

El flujo de trabajo se divide en un pipeline automatizado de tres etapas: generación de datos sintéticos realistas, fusión de fuentes y entrenamiento de una red neuronal densa.

### Fase A: Generación de Dataset Sintético (MVP)
**Objetivo:** Simular condiciones físicas de telemetría vehicular ante la ausencia de datasets reales.
Se modelan **5 características (features)** basadas en sensores móviles:
- **Conducción Normal (Label 0):** Fuerzas G bajas (0.1 - 1.5).
- **Posible Susto (Label 1):** Frenazos o derrapes (1.5G - 4.0G).
- **Choque Grave (Label 2):** Impactos críticos con fuerzas G elevadas (4.0G - 12.0G) y alta firma acústica.

**Resultado:** 7.000 muestras totales guardadas en `ml/data/processed/source/datos_sinteticos.csv`.

---

## 2. Configuración de Entrenamiento

Se utiliza una arquitectura de **Red Neuronal Artificial (ANN)** tipo MLP (Perceptrón Multicapa).

### Arquitectura de Capas
- **Input Layer:** 5 neuronas (correspondientes a las 5 variables de sensores).
- **Hidden Layer 1:** 24 unidades, activación ReLU.
- **Hidden Layer 2:** 12 unidades, activación ReLU.
- **Output Layer:** 3 unidades, activación Softmax (Clasificación multiclase).

### Hiperparámetros
- **Épocas:** 40
- **Batch Size:** 16
- **Optimizador:** Adam
- **Loss:** Sparse Categorical Crossentropy

---

## 3. Métricas y Resultados

El modelo se evalúa mediante una partición de test (15% del total) que el sistema no ha visto durante el aprendizaje.

- **Métricas:** Consultar `ml/experiments/experiment1/experiment1.txt` para más info.
  - Accuracy: 76.67%, 
  - Loss: 0.6077
  
- **Análisis:** El modelo demuestra una separación clara entre eventos normales y colisiones gracias a la consistencia de los umbrales físicos inyectados.

---

## 4. Artefactos Generados

- **Modelo Producción:** `ml/models/v1/safedrive_cu03_model.tflite` (Para despliegue en Android).

---

## 5. Instrucciones de Reproducción

1. Ejecutar el notebook bloque a bloque.
2. La **Sección 1** crea la estructura de directorios necesaria.
3. La **Sección 2** fusiona los CSV sintéticos (y reales si existieran en la carpeta `source`).
4. La **Sección 3** realiza el entrenamiento y genera el archivo `.tflite`.