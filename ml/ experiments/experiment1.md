# Documentación de Experimentación ML

Este documento detalla el proceso de diseño, generación de datos y entrenamiento del modelo de detección de accidentes para el sistema SafeDrive.

## 1. Metodología de Experimentación

Debido a la naturaleza crítica del proyecto y la ausencia de datasets públicos con telemetría real de colisiones vehiculares para sensores específicos (CU-03), el flujo de trabajo se dividió en dos fases técnicas integradas en el script de entrenamiento.

### Fase A: Generación de Dataset Sintético (MVP)

**Script:** `scripts/safedrive_trainer.ipynb` (Sección 1)

**Objetivo:** Crear una base de datos balanceada que simule condiciones físicas de conducción.

**Lógica de Generación:**
Se utilizaron funciones aleatorias controladas para modelar tres escenarios:

- **Conducción Normal (Label 0):** Valores de G-force y Jerk bajos.
- **Incidente Menor/Bache (Label 1):** Picos moderados en aceleración y firma acústica.
- **Colisión Grave (Label 2):** Valores críticos de Peak_G (>0.75) y cambios bruscos en el giroscopio.

**Resultado:**
Generación de archivos CSV estructurados en carpetas `train/`, `test/` y `validation/`.

### Fase B: Entrenamiento y Producción

**Script:** `scripts/safedrive_trainer.ipynb` (Sección 2)

**Configuración:**
Carga de los datasets generados (o subidos por el usuario) para la creación del modelo final.

---

## 2. Configuración de Entrenamiento

Se ha implementado una arquitectura de Red Neuronal Profunda (DNN) optimizada para dispositivos móviles:

- **Algoritmo:** Red Neuronal Secuencial (TensorFlow/Keras)

### Arquitectura de Capas

- Input Layer: 4 neuronas (sensores)
- Hidden Layer 1: 24 unidades, activación ReLU
- Hidden Layer 2: 12 unidades, activación ReLU
- Output Layer: 3 unidades, activación Softmax (clasificación multiclase)

### Hiperparámetros

- Épocas: 40
- Batch Size: 16
- Optimizador: Adam (tasa de aprendizaje adaptativa)

---

## 3. Métricas y Resultados Experimentales

El modelo fue evaluado utilizando un set de datos "ciego" (Test Set) para garantizar la capacidad de generalización.

- **Accuracy:** 76.67%
- **Loss:** 0.6077

### Análisis de Predicción

- Alta precisión detectando **Colisiones Graves (Label 2)**.
- Ligera confusión entre **Baches (Label 1)** y falsos positivos, esperada por similitud en vibración inicial.

---

## 4. Resultados de Salida

Tras la experimentación, se generaron los siguientes artefactos:

- **Modelo Compilado:** `models/safedrive_cu03_model.tflite`
- **Dataset de Validación:** `data/validation/`

---

## 5. Instrucciones de Reproducción

1. Cargar el archivo `scripts/safedrive_trainer.ipynb` en Google Colab.
2. Asegurarse de que el entorno tiene instaladas las librerías:
    - tensorflow
    - pandas
    - sklearn
3. Ejecutar la **Sección 1** para regenerar los datos o subir un `dataset.zip` propio.
4. Ejecutar la **Sección 2** para realizar el entrenamiento y la conversión automática a TFLite.

---

## Notas

Este proyecto utiliza datos sintéticos como aproximación inicial (MVP). Para mejorar la precisión y robustez del modelo, se recomienda entrenar con datos reales provenientes de sensores en condiciones controladas.
