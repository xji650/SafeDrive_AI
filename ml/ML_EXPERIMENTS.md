# SafeDrive AI - Definición del Problema y Registro de Experimentos

## 1. Definición del Problema (ML)

El objetivo es clasificar eventos telemétricos en tiempo real para detectar accidentes de tráfico mediante **Edge AI**. El sistema procesa localmente la telemetría para garantizar baja latencia y privacidad.

Se trata de un problema de **Aprendizaje Supervisado de Clasificación Multiclase**.

* **Entrada (5 variables):** `Peak_G`, `Jerk`, `Firma_Acustica`, `Cambio_Angular`, `Velocidad`.
* **Salida (3 categorías):** Normal (0), Incidente/Susto (1), Colisión Grave (2).

---

## 2. Modelos Candidatos y Justificación de Experimentos

La estrategia de experimentación sigue una progresión lógica: de modelos estadísticos clásicos a aprendizaje profundo.

1. **Random Forest (Baseline):** Se elige como punto de referencia (Baseline) por su alta eficacia en datos tabulares y su capacidad para manejar la importancia de las variables.
2. **Red Neuronal MLP (MVP):** Se elige como modelo principal por su compatibilidad nativa con TensorFlow Lite y su capacidad para evolucionar hacia arquitecturas de memoria (LSTM) en futuras fases de datos reales.

---

## 3. Pipeline de Entrenamiento (Configuración)

* **Generación:** Datos sintéticos con lógica física (7.000 muestras).
* **División:** Split **70/15/15** (Entrenamiento / Validación / Test).
* **Arquitectura DNN:**
* Capa de Normalización Adaptativa (en Exp #2).
* 2 capas ocultas: 24 y 12 neuronas (ReLU).
* Salida: 3 neuronas (Softmax).


* **Exportación:** Formato `.tflite` con cuantización de pesos.

---

## 4. Registro de Experimentos y Resultados

| Experimento | Descripción | Accuracy | Loss | Observaciones |
| --- | --- |--------| --- | --- |
| **#1: MLP Base** | Datos crudos, sin normalización. | 76.67% | 0.607 | Confusión por diferencia de escalas (G vs Velocidad). |
| **#2: MLP Pro** | Con Capa Normalizer y Cuantización. | **100%** | **0.000** | Rendimiento perfecto en datos sintéticos. Óptimo para Android. |
| **#3: Random Forest** | Modelo clásico de 100 árboles. | 100%   | *N/A* | Se usa para validar si la lógica física es lineal. |

---

## 5. Justificación de la Selección de Experimentos

¿Por qué hemos realizado estas pruebas específicas?

1. **¿Por qué el paso del Exp #1 al #2?** Los resultados del Exp #1 demostraron que la red "se perdía" con los valores altos de velocidad (0-120) frente a los bajos de fuerza G (0-15). La introducción de la **capa de normalización** en el Exp #2 fue necesaria para estandarizar las entradas, logrando que el modelo alcance la precisión máxima.
2. **¿Por qué incluir Random Forest ahora?** Para descartar el "sobreajuste" (overfitting). Si un árbol de decisión simple ya clasifica bien, confirmamos que nuestra lógica de generación de datos es coherente. Además, permite comparar el peso de los archivos: a veces un Random Forest en Android es más pesado de procesar que una red neuronal pequeña y optimizada.
3. **¿Por qué descartar Regresión Logística?** Porque las colisiones son eventos de energía cinética no lineales. La regresión logística asume relaciones demasiado simples que fallarían ante la complejidad de un derrape o una vibración acústica.

---

## 6. Análisis del Benchmark y Justificación Final

El Experimento #3 (Random Forest) ha servido como **auditoría técnica** de los resultados obtenidos en el Experimento #2.

#### Conclusiones del Duelo de Modelos:

1. **Validación de la Lógica de Datos:** El hecho de que tanto la Red Neuronal (MLP) como el Random Forest alcancen el 100% de precisión confirma que el dataset sintético no tiene errores de etiquetado y que las reglas físicas (umbrales de G-force) son consistentes.
2. **Robustez frente a la Escala:** Mientras que el MLP necesitó una capa de Normalización para ignorar las diferencias de magnitud entre Gs y Velocidad, el Random Forest alcanzó el éxito de forma nativa. Esto demuestra que los datos son "linealmente separables" si se usan los umbrales correctos.
3. **Selección del Modelo de Producción:** A pesar del empate técnico en precisión, se selecciona el **Modelo MLP (v2)** para la implementación en SafeDrive por los siguientes motivos:
* **Portabilidad:** Compatibilidad nativa y optimizada con **TensorFlow Lite**.
* **Eficiencia:** Menor consumo de recursos en ejecución continua (background) en Android comparado con un bosque de árboles.
* **Escalabilidad:** Capacidad de evolucionar hacia redes recurrentes (LSTM) en la Fase 2 (datos temporales).

---

### Notas Técnicas

> El **100% de Accuracy** del Experimento #2 se debe a la naturaleza controlada de los datos sintéticos. Se espera que esta métrica se ajuste a valores más realistas (85-95%) al introducir el dataset de datos reales (Kaggle/Room) en la siguiente fase.

---
