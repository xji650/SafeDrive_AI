# Documentación de Experimentación ML — Experimento 2 (MLP Normalizado)

Este documento detalla la evolución del modelo **v2**, integrando preprocesamiento automático y optimizaciones para despliegue móvil.

## 1. Metodología de Experimentación

Se mantiene el pipeline automatizado, pero se refina la Fase B para incluir capas de preprocesamiento dentro de la arquitectura del modelo, garantizando que el modelo sea independiente de la plataforma.

### Fase A: Generación de Dataset Sintético

Se mantienen las **5 características (features)** para asegurar la consistencia con el Experimento 1:

* `Peak_G`, `Jerk`, `Firma_Acustica`, `Cambio_Angular`, `Velocidad`.

### Fase B: Entrenamiento y Producción (Versión V2)

**Script:** `ml/experiments/experiment2/experimento2_normalizado.ipynb`
**Mejora Crítica:** Se implementa una capa de **Normalización Adaptativa** que ajusta la media y la varianza de los datos de los sensores antes de pasar por las neuronas.

---

## 2. Configuración de Entrenamiento

Se ha implementado una arquitectura de Red Neuronal Profunda (DNN) con auto-escalado:

* **Algoritmo:** MLP Secuencial (TensorFlow/Keras)
* **Nombre del Modelo:** `safedrive_cu03_v2`

### Arquitectura de Capas

* **Capa 0 (Input):** 5 neuronas.
* **Capa 1 (Preprocesamiento):** Normalization (Adaptada al dataset de entrenamiento).
* **Capa 2 (Hidden):** 24 unidades, ReLU.
* **Capa 3 (Hidden):** 12 unidades, ReLU.
* **Capa 4 (Output):** 3 unidades, Softmax.

### Hiperparámetros

* **Épocas:** 40
* **Optimización móvil:** Activada (Post-Training Quantization).

---

## 3. Métricas y Resultados Experimentales

El modelo alcanzó la convergencia perfecta gracias a la eliminación de ruidos de escala.

* **Accuracy:** 100.00% (1.0000)
* **Loss:** 0.0000

### Análisis de Predicción

* **Efectividad:** El modelo clasifica correctamente el 100% de las muestras de test.
* **Comparativa:** Mientras que en el Exp 1 había confusión entre baches y choques, la normalización ha permitido al modelo trazar fronteras de decisión exactas.

---

## 4. Artefactos Generados

* **Modelo Keras:** `ml/models/v2/safedrive_cu03_model_v2.keras` (Para inspección)
* **Modelo Producción:** `ml/models/v2/safedrive_cu03_model_v2.tflite` (Para desplegar, Optimizada para Android).

---

## 5. Instrucciones de Reproducción

1. Ejecutar el notebook del Experimento 2.
2. La **Sección 3** ahora incluye el bloque `.adapt(X_train)`, requisito indispensable antes de definir el modelo.
3. El archivo `.tflite` resultante debe sustituir al de la versión v1 en la carpeta `assets` del proyecto Android.