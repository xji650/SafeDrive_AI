# SafeDrive AI - Experimento 4 - MLP Normalizado con entrada de dataset escala real

Este documento resume la resolución del problema de escalas y la configuración del último experimento de detección de accidentes.

## 1. El Problema de Incompatibilidad

Se detectó una discrepancia entre los datos generados sintéticamente y los datos reportados por el hardware del dispositivo Android:

* **Sensores del Móvil:** Reportan en **unidades físicas reales** (Decibelios para audio, G-Force para aceleración, Grados/segundo para rotación).
* **Modelo Original:** Esperaba valores normalizados manualmente (ej. 0.0 a 1.0) para ciertas variables como la Firma Acústica.
* **Resultado:** El modelo "colapsaba" o daba predicciones erróneas al recibir valores reales (ej. 80 dB) en campos donde esperaba valores decimales pequeños.

## 2. Ajuste a Escala Real (Real Scale)

Para simplificar la integración en Android y mejorar la interpretabilidad del modelo, se ha ajustado todo el pipeline a **Escala Real**:

* **Peak_G:** 0.8 - 15.0 Gs.
* **Jerk:** 0.1 - 20.0 G/s.
* **Firma_Acustica:** 30.0 - 120.0 dB.
* **Cambio_Angular:** 0.0 - 500.0 º/s.
* **Velocidad:** 0.0 - 200.0 km/h.

Con este ajuste, la App de Android **ya no requiere realizar cálculos de normalización previos**; envía los datos brutos del sensor directamente al intérprete de TensorFlow Lite.

## 3. Arquitectura del Modelo MLP

En el último experimento, **se ha mantenido la misma arquitectura de Red Neuronal Perceptrón Multicapa (MLP)** para asegurar la continuidad de las pruebas:

* **Capa de Entrada:** 5 dimensiones.
* **Capa de Normalización (Keras Normalization):** Se ha integrado como la primera capa del modelo. Esta capa utiliza los parámetros `mean` y `variance` del dataset real para escalar los datos internamente.
* **Capas Densas:** 24 neuronas (ReLU) --> 12 neuronas (ReLU).
* **Capa de Salida:** 3 neuronas (Softmax) para las clases: `Normal`, `Susto` y `Choque`.

---

### Visualización del Proceso de Normalización Interna

Para entender cómo el modelo gestiona ahora estos números grandes sin que tú tengas que modificar el código de Android, puedes usar este simulador que muestra la transformación de los datos reales a valores que la IA puede procesar.