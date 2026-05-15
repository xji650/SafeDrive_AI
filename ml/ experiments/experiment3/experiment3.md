# Documentación de Experimentación ML — Experimento 3 (Benchmark de Modelos MPL y Random Forest)

Este documento detalla la fase de validación competitiva entre el modelo de producción (**MLP**) y un modelo de control (**Random Forest**) para asegurar la robustez del sistema SafeDrive.

## 1. Propósito del Experimento (Benchmark)

El objetivo principal de este tercer experimento es realizar un **Benchmark** o prueba comparativa. Al enfrentar dos algoritmos de naturaleza completamente distinta, buscamos validar:

1. **Consistencia de los Datos:** Confirmar que las reglas físicas de los sensores son claras y no dependen de un solo tipo de algoritmo.
2. **Validación del Baseline:** Establecer un punto de referencia estadístico con un modelo robusto para datos tabulares (Random Forest).
3. **Justificación de Arquitectura:** Argumentar técnicamente la elección final del modelo para el despliegue en Android.

---

## 2. Metodología y Configuración

Se ha utilizado el mismo dataset procesado (7,000 muestras) con un split de test del 15% (1,050 muestras).

### Modelo A: MLP (Red Neuronal)

* **Tipo:** Perceptrón Multicapa con capa de Normalización integrada.
* **Fortaleza:** Optimización para dispositivos móviles mediante **TensorFlow Lite**.
* **Estado:** Candidato a Producción.

### Modelo B: Random Forest (Control)

* **Tipo:** Ensamble de 100 árboles de decisión.
* **Fortaleza:** No requiere normalización y es altamente resistente al ruido en datos estructurados.
* **Estado:** Modelo de Validación.

---

## 3. Resultados Obtenidos

La comparativa de rendimiento arroja un empate técnico en precisión, lo que valida la excelente calidad del dataset sintético generado.

| Métrica | Red Neuronal (MLP) | Random Forest |
| --- | --- | --- |
| **Accuracy (Test)** | **100.00%** | **100.00%** |
| **Loss** | 0.0000 | N/A |
| **Estado Final** | **Seleccionado** | Descartado |

### Informe Técnico Detallado (MLP)

El análisis por clase muestra una precisión perfecta (1.00) en todos los niveles de riesgo, con un soporte equilibrado de muestras:

```text
              precision    recall  f1-score   support

      Normal       1.00      1.00      1.00       621
       Susto       1.00      1.00      1.00       213
      Choque       1.00      1.00      1.00       216

    accuracy                           1.00      1050

```

---

## 4. Análisis y Conclusiones

### ¿Por qué ambos obtienen 100%?

El hecho de que ambos modelos alcancen la perfección indica que las fronteras de decisión entre las clases (Normal, Susto, Choque) están claramente definidas por las leyes físicas inyectadas en el generador de datos. No existe solapamiento crítico entre un bache fuerte (4.0G) y un choque grave (>4.0G), lo que permite a los modelos clasificar con total seguridad.

### Selección Final: ¿Por qué MLP si hay empate?

Aunque el Random Forest es igualmente preciso, se ha seleccionado el modelo **MLP (Red Neuronal)** por los siguientes motivos de ingeniería:

1. **Portabilidad (TFLite):** La Red Neuronal se exporta de forma nativa a formato `.tflite`, permitiendo una integración inmediata con el hardware del smartphone.
2. **Consumo de Recursos:** En dispositivos Edge (móviles), una red neuronal pequeña cuantizada es más eficiente en consumo de batería que procesar un bosque de 100 árboles de decisión.
3. **Escalabilidad:** El modelo MLP permite, en fases futuras, integrar capas de memoria (como LSTM) para procesar secuencias temporales de sensores, algo que el Random Forest no permite de forma natural.

---

## 5. Artefactos de Salida

* **Modelo Validado:** `models/v2/safedrive_cu03_model_v3.tflite` (idéntico que la v2)
* **Reporte de Calidad:** `ml/experiments/experiment3/benchmark_results.txt`

---