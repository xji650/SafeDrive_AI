# SafeDrive AI — Memoria Técnica de Experimentos de Machine Learning
**Sistema de Detección de Accidentes en Dispositivos Edge AI (SafeDrive CU-03)**

Este documento actúa como el registro exhaustivo del proceso de investigación, diseño de ingeniería de datos, modelado neuronal, optimización móvil y análisis comparativo para el sistema inteligente de detección de colisiones de SafeDrive AI.

---

## 1. Descripción del Problema

El objetivo del proyecto es desarrollar un sistema de detección automática de accidentes vehiculares capaz de ejecutarse en tiempo real de forma local (*Edge AI*) en smartphones y entornos integrados como **Android Auto**. El sistema debe procesar ráfagas de datos provenientes de los sensores internos del dispositivo (acelerómetro y giroscopio) junto con la telemetría del vehículo (velocidad por GPS) y señales acústicas ambientales (micrófono) para discernir instantáneamente si se ha producido un impacto crítico que ponga en riesgo la vida del conductor.

### Desafíos Técnicos Principales:
1. **Restricciones de Hardware (Edge Computing):** El modelo debe ser ultraligero, consumir el mínimo porcentaje de batería y ejecutarse con latencias inferiores a 50 ms para permitir un despliegue viable.
2. **El Problema de la Fatiga de Alertas (Falsos Positivos):** Eventos de la vida cotidiana del usuario (como dejar caer el móvil sobre una mesa rígida o cerrar un portazo de forma violenta) generan lecturas instantáneas de aceleración muy elevadas. El sistema debe ser capaz de entender el **contexto cinemático completo** para discriminar estos eventos domésticos de un choque automovilístico real.
3. **Brecha de Distribución de Datos (Sim-to-Real Gap):** Ante la ausencia lógica de datasets públicos masivos con choques de vehículos reales controlados usando smartphones comerciales, el sistema debe inicializarse de forma segura en un entorno controlado y evolucionar dinámicamente mediante interacciones humanas verificadas (*Human-in-the-Loop*).

---

## 2. Estrategia de Datos y Dataset Utilizado

Para solucionar el arranque en frío del modelo, se diseñó e implementó un pipeline automatizado de ingeniería de datos estructurado en cuatro fases modulares de ejecución secuencial:

```

[generate_dataset.py] ──> [process_local.py] ──> [build_dataset.py] ──> [safedrive.ipynb]
(Motor Cinemático)        (Ingesta Firebase)       (Batidora/Split)       (Entrenamiento/TFLite)

```

### Componentes del Pipeline (`ml/scripts/`):

1. **Generación Física (`generate_dataset.py`):** Motor cinemático teórico que simula vectores físicos realistas para construir un dataset base balanceado de **7,000 muestras** totales almacenadas en `ml/data/processed/source/datos_sinteticos.csv`. Las muestras se dividen en tres perfiles operativos:
   * **Clase 0: Falso Positivo / Normal (4,000 muestras):** Conducción estable, frenadas suaves, ruidos normales de habitáculo y pequeñas vibraciones.
   * **Clase 1: Posible Susto / Agresiva (1,500 muestras):** Maniobras evasivas, giros violentos, frenazos de emergencia controlados y aceleraciones bruscas.
   * **Clase 2: Choque Grave / Accidente (1,500 muestras):** Impactos críticos de alta desaceleración con deformación estructural simulada del chasis, alta firma acústica y rotación angular descontrolada.
   
2. **Ingesta de Datos Reales (`process_local.py`):** Script encargado de conectar con el backend en la nube (**Firebase Firestore**), descargando de manera síncrona los documentos independientes de la colección colectiva de accidentes. Aplica una **regla estricta de Ground Truth** que solo extrae los registros confirmados de forma explícita por los usuarios mediante el sistema de feedback en la app móvil (`type is not None`), blindando el reentrenamiento contra datos corruptos o predicciones automatizadas sin validar.

3. **Preparación y Reparto (`build_dataset.py`):** Toma las fuentes de datos disponibles en el directorio `source`, ejecuta un proceso de aleatorización indexada (*shuffle*) para romper cualquier sesgo temporal de inserción y realiza la partición matemática estricta:
   * **70% Entrenamiento (Train):** 4,900 muestras para el ajuste de pesos neuronales.
   * **15% Validación (Validation):** 1,050 muestras para el control de sobreajuste por época.
   * **15% Pruebas (Test):** 1,050 muestras completamente aisladas para la evaluación final del modelo.

### Rangos Físicos de las Variables del Dataset:

| Parámetro | Unidad | Descripción | Conducción Normal (0) | Posible Susto (1) | Choque Grave (2) |
| :--- | :---: | :--- | :---: | :---: | :---: |
| **Peak_G** | G | Pico de fuerza de aceleración (1G = reposo) | 0.8 - 1.2 | 1.2 - 4.0 | **4.0 - 15.0** |
| **Jerk** | G/s | Tasa de cambio instantánea de la aceleración | 0.1 - 2.0 | 2.0 - 7.0 | **7.0 - 20.0** |
| **Firma_Acústica**| dB | Amplitud del micrófono ambiental capturado | 30.0 - 60.0 | 60.0 - 85.0 | **90.0 - 120.0** |
| **Cambio_Angular**| º/s | Velocidad angular detectada por giroscopio | 0.0 - 30.0 | 30.0 - 120.0| **120.0 - 500.0**|
| **Velocidad** | km/h | Velocidad del vehículo registrada vía GPS | 0.0 - 120.0 | 20.0 - 140.0| **30.0 - 200.0** |

---

## 3. El Desafío del Preprocesamiento (Feature Scaling)

Uno de los mayores obstáculos técnicos identificados durante la fase exploratoria de datos fue el **sesgo de magnitud absoluta de las variables**. 

* **El Problema:** Variables como la `Velocidad` se mueven en un rango numérico amplio (0 a 200), mientras que variables de vital importancia como el `Peak_G` se mueven en escalas pequeñas (0.8 a 15). Sin preprocesamiento, el optimizador matemático de la red neuronal prioriza los cambios numéricos grandes de la velocidad, provocando que un impacto brutal de 12G a baja velocidad (ej. colisión en ciudad a 30 km/h) sea clasificado erróneamente como conducción normal.
* **La Solución (Normalización Adaptativa):** Se integró una capa nativa de **Z-Score Normalization** como la primera capa lógica de la Red Neuronal (`keras.layers.Normalization`). Esta capa calcula la media (μ) y la desviación estándar (σ) de cada característica durante la fase de ajuste (`adapt`), transformando todas las entradas bajo la función matemática: 
    ```
    x̂ = (x - μ) / σ
    ```

Esto fuerza a que todas las entradas tengan una media de 0 y una varianza de 1, permitiendo al optimizador ponderar de forma equitativa el comportamiento combinado y la "firma geométrica" del accidente, desvinculándola de su escala numérica pura.

---

## 4. Configuraciones de Entrenamiento y Modelos Evaluados

El proceso experimental se dividió en tres fases incrementales documentadas de manera detallada:

### Modelo 1: MLP Base (Sin Normalización)
* **Arquitectura:** Perceptrón Multicapa (MLP) Secuencial con 5 entradas crudas, 2 capas ocultas densas (24 y 12 neuronas con activación ReLU) y una capa de salida con 3 unidades (activación Softmax).
* **Configuración:** Optimizador Adam (LR = 0.001), Función de pérdida *Sparse Categorical Crossentropy*, entrenado durante 40 épocas con un tamaño de lote (*batch size*) de 16.

### Modelo 2: MLP_v4 (Normalizado y Cuantizado) - *Elegido para producción*
* **Arquitectura:** Capa de Normalización Adaptativa integrada + Capa Densa (24 unidades, ReLU) + Capa Densa (12 unidades, ReLU) + Capa de Salida (3 unidades, Softmax).
* **Configuración:** Idéntica al base, pero aplicando el método `.adapt()` sobre el conjunto de entrenamiento previo al bucle de optimización. Posterior exportación mediante la activación de **Post-Training Quantization** (`tf.lite.Optimize.DEFAULT`) para forzar la reducción de precisión matemática de coma flotante a enteros optimizados.

### Modelo 3: Random Forest (Modelo de Control)
* **Arquitectura:** Clasificador de ensamble clásico compuesto por **100 árboles de decisión** independientes. Implementado como auditoría técnica externa para evaluar el techo de separabilidad estadística del dataset sintético sin intervención de redes profundas.

---

## 5. Métricas Utilizadas

Para evaluar los modelos de forma rigurosa se emplearon cuatro métricas estadísticas calculadas de forma independiente sobre el conjunto de test aislado de 1,050 muestras:

1. **Accuracy (Exactitud):** Porcentaje general de predicciones correctas.
2. **Precision (Precisión por Clase):** Capacidad del modelo de no clasificar un evento normal como accidente (Evita Falsos Positivos).
3. **Recall (Sensibilidad por Clase):** Capacidad del modelo de capturar el 100% de los choques reales ocurridos (Evita Falsos Negativos catastróficos).
4. **F1-Score:** La media armónica entre Precision y Recall. Es la **métrica reina del proyecto**, dado que un modelo comercial no puede permitirse dejar desatendido a un conductor (bajo recall) ni alarmar continuamente a los servicios de emergencia por baches (baja precisión).

---

## 6. Registro de Experimentos y Resultados Comparativos

A continuación se consolidan las métricas extraídas directamente de los logs de consola de los experimentos (`experiment1.txt`, `experiment2.txt` y `benchmark_results.txt`):

### Matriz de Rendimiento Global:

| Exp | Modelo Evaluado | Estrategia de Preprocesamiento | Test Loss | Test Accuracy / F1-Score | Estado de Aceptación                         |
| :---: |:----------------| :--- | :---: | :---: |:---------------------------------------------|
| **#1** | MLP Base        | Ninguna (Datos Crudos) | 0.6077 | 76.67% | **RECHAZADO:** Sesgo masivo hacia velocidad. |
| **#2** | **MLP_v2**      | **Capa Normalizer integrada + TFLite**| **0.0000**| **100.00%** | **Modelo identico que v4**                   |
| **#3** | Random Forest   | Ninguna requerida por diseño | N/A | 100.00% | **VALIDADO:** Confirmó separabilidad limpia. |
| **#2** | **MLP_v4**      | **Capa Normalizer integrada + TFLite**| **0.0000**| **100.00%** | **ELEGIDO PARA PRODUCCIÓN (28 KB)**          |

### Informe Técnico Detallado de Clasificación (Modelo MLP_v4):

| Clase / Métrica | Precision | Recall | F1-Score | Support (Muestras) |
| --- | --- | --- | --- | --- |
| **Falsos Positivos (0)** | 1.00 | 1.00 | 1.00 | 621 |
| **Posibles Sustos (1)** | 1.00 | 1.00 | 1.00 | 213 |
| **Choques Graves (2)** | 1.00 | 1.00 | 1.00 | 216 |
|  |  |  |  |  |
| **Accuracy Global** | - | - | **1.00** | **1050** |
| *Macro Avg* | 1.00 | 1.00 | 1.00 | 1050 |
| *Weighted Avg* | 1.00 | 1.00 | 1.00 | 1050 |

---

## 7. Optimización del Modelo para Móvil 

Aunque el modelo Random Forest (Exp #3) obtuvo métricas perfectas, fue descartado para el despliegue final debido a las restricciones operativas de la plataforma Android. El modelo **MLP_v4** fue optimizado minuciosamente a través de la arquitectura de **TensorFlow Lite**:

1. **Cuantización de Pesos:** Se aplicó la compresión post-entrenamiento que reduce la representación de los pesos neuronales desde `float32` a formato optimizado de tamaño reducido. Esto encogió el tamaño físico del archivo binario final a tan solo **28.1 KB**.
2. **Encapsulamiento del Preprocesamiento:** Al incluir la capa `Normalization` dentro del grafo del archivo `.tflite`, la aplicación de Android en Kotlin inyecta de forma directa los valores en crudo leídos por los sensores (`SensorEvent`). El propio chip de inferencia ejecuta el escalado de datos internamente, eliminando líneas de código redundantes en la app móvil y reduciendo la latencia de cómputo a menos de **5 milisegundos**.
3. **Eficiencia de Batería:** Procesar una matriz de una red neuronal de 2 capas requiere una fracción minúscula de operaciones de CPU/NPU frente a evaluar secuencialmente 100 árboles de decisión lógicos en paralelo, protegiendo la autonomía energética del teléfono del conductor.

---

## 8. Discusión de Resultados y Conclusiones

### Discusión del Sim-to-Real Gap:
El logro de una métrica de precisión y sensibilidad del 100.00% en los experimentos 2 y 3 no debe interpretarse como un modelo infalible en el mundo real, sino como una **consecuencia matemática de la separación física perfecta impuesta en el simulador base** (`generate_dataset.py`). 

En conducción real, los ruidos del chasis de vehículos antiguos o terrenos de tierra irregular generarán un solapamiento geométrico en las fronteras de decisión (ej. picos de 4.2G que no constituyen accidentes). Es aquí donde cobra un valor estratégico incalculable la **Fase 2 de la Estrategia de Datos**: el script `process_local.py` actuará como un embudo que absorberá estas anomalías reales validadas por humanos para reajustar de forma iterativa los pesos neuronales, estabilizando el modelo en producción.

### Arquitectura de Seguridad Implementada (Filtro Heurístico):
Como conclusión de diseño de software e ingeniería de sistemas, para garantizar una experiencia de usuario (UX) óptima y evitar el desgaste innecesario de ciclos de procesamiento del dispositivo, se determinó que el modelo TFLite no debe ejecutarse de forma continua en segundo plano. 

Se implementó un **Escudo Heurístico Pre-IA** en el código nativo de Android: la aplicación mantiene los sensores en un modo de lectura pasiva de bajo consumo y **solo invoca al modelo de inteligencia artificial** cuando se viola una regla de viabilidad cinemática elemental de forma simultánea:

```
Velocidad >= 15 km/h   AND   Peak_G >= 3.5 G   AND   Audio >= 85 dB
```

Si estas tres condiciones físicas de activación no ocurren a la vez (por ejemplo, si el móvil recibe un golpe seco de 10G al caerse en la mesa de noche pero la velocidad GPS es de 0 km/h y no hay estruendo acústico masivo), el evento se descarta instantáneamente en milisegundos por código heurístico estricto, logrando un equilibrio perfecto entre la robustez científica de la Inteligencia Artificial y la eficiencia pragmática del software móvil.

---

# 9. Asistente SafeDrive AI: Implementación de RAG (Retrieval-Augmented Generation)

Además de la detección de impactos, el sistema incorpora un **Asistente de IA Generativa** diseñado para resolver dudas sobre seguridad vial, protocolos de emergencia y manuales técnicos del vehículo, minimizando las alucinaciones del modelo mediante la técnica de **RAG**.

## 9.1 Descripción del Problema y Abordaje
Los modelos de lenguaje (LLM) genéricos como Llama 3 suelen dar respuestas generales, pero carecen de conocimientos específicos sobre normativas locales actualizadas (ej. Ley de Tráfico española) o procedimientos técnicos de SafeDrive AI. 
*   **Problema:** Alucinaciones y falta de contexto local.
*   **Abordaje:** Implementación de una arquitectura de **Recuperación Aumentada (RAG)**. Antes de responder, el sistema busca fragmentos de documentos oficiales en una base de datos vectorial y los inyecta en el "prompt" del modelo como conocimiento base.

## 9.2 Datos Utilizados
Para alimentar el cerebro del asistente se han procesado y vectorizado las siguientes fuentes:
*   **Normativa DGT/BOE:** Fragmentos de la Ley sobre Tráfico y Seguridad Vial.
*   **Manuales de Seguridad:** Protocolos PAS (Proteger, Avisar, Socorrer).
*   **Parte y seguro:** Cómo redactar parte amistoso.

## 9.3 Estructura del Sistema RAG (`ml/RAG/`)
La arquitectura sigue una organización modular para facilitar el despliegue en servidores locales o remotos:
*   `/chroma_db/`: Almacén vectorial persistente que contiene los *embeddings* de los documentos.
*   `/data/`: Documentos originales en formato PDF/Texto.
*   `/scripts/main.py`: Servidor API basado en **FastAPI** que gestiona la búsqueda semántica.
*   `/scripts/06_rag_safedrive_text.ipynb`: Notebook de ingestión, partición (*chunking*) y vectorización.

## 9.4 Tecnologías Utilizadas
*   **Orquestación:** LangChain.
*   **Base de Datos Vectorial:** ChromaDB (Persistente).
*   **Embeddings:** `nomic-embed-text` (vía Ollama).
*   **Inferencia LLM:** qwen2.5:7b + gemma2.5 (visor), qwen2.5:7b + qwen2.5vl (visor), qwen2.5:7b operando en servidores locales.
*   **Backend:** FastAPI con soporte para flujos asíncronos.

## 9.5 Experimentación y Comparativa
Se realizaron pruebas comparativas para determinar la configuración óptima de recuperación de contexto:

| Configuración       | Modelo                            | K (Chunks)  | Resultado / Calidad                                                             |
|:--------------------|:----------------------------------|:-----------:|:--------------------------------------------------------------------------------|
| **Básica**          | Llama 3                           | 0 (Sin RAG) | Respuestas genéricas, a veces ignora leyes locales.                             |
| **RAG-Mulltimodal** | qwen2.5:7b + gemma2.5 / qwen2.5vl |      3      | Muy lento escaneo de imagenes, y el uso de gemma no renta.                      |
| **RAG-estandard**   | qwen2.5:7b                        |      3      | **ÓPTIMO:** Respuestas precisas basadas en el BOE.                              |
| **RAG-Extendido**   | qwen2.5:7b                       |      3      | **ÓPTIMO:** Respuestas precisas basadas en el BOE + actualizaciones baliza v16. |

## 9.6 Resultados y Conclusiones del Asistente
1.  **Precisión Documental:** El uso de RAG aumentó la precisión de las respuestas.
2.  **Eficiencia:** El uso de `nomic-embed-text` permite realizar búsquedas semánticas en un tiempo razonable, lo que garantiza una experiencia de chat fluida.
3.  **Seguridad:** Al inyectar el contexto, se reduce drásticamente la probabilidad de que la IA recomiende acciones peligrosas o fuera de protocolo durante una emergencia.
