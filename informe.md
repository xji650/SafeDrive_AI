# Informe Técnico: SafeDrive AI

## 1. Introducción

### 1.1. Problema que se quiere resolver
SafeDrive AI es una solución de software orientada a democratizar la seguridad vial pasiva, transformando cualquier smartphone Android en un Sistema Avanzado de Asistencia al Conductor (ADAS) y un Registrador de Datos de Eventos (EDR o "Caja Negra").
El proyecto surge a raíz de una conversación con un amigo que trabaja en Mapfre, sobre la carencia de registro de accidentes de coche.

### 1.2. Objetivo de la aplicación
SafeDrive AI utiliza Inteligencia Artificial en el borde (Edge AI) a través de modelos TensorFlow Lite para procesar en tiempo real la telemetría del vehículo mediante una arquitectura de Fusión Sensorial Pasiva (acelerómetro, giroscopio, micrófono y GPS) y registra incidencias EDR que generan un archivo JSON (300 muestras, 3-5 s).

---

## 2. Descripción del sistema

### 2.1. Idea general del proyecto
*   **App móvil / Android Auto**: Una aplicación que detecta accidentes (con IA, telemetría de fusión sensorial mediante TFLite) y avisa a contactos de emergencia vía SMS y llama al 112 (en menos de 15 s).

### 2.2. Funcionalidades principales de la aplicación
*   **EDR (Event Data Recorder)**: Registra incidencias con IA de fusión sensorial + filtro heurístico (`Velocidad >= 15 km/h` AND `Peak_G >= 3.5 G` AND `Audio >= 85 dB`), genera reporte en JSON e informe en PDF, y ofrece feedback de incidencias que retroalimenta y entrena a la IA.
*   **Asistente IA**: Resuelve dudas sobre tráfico, accidentes, etc.
*   **Dashboard**: Muestra el buen funcionamiento de los sensores (mapa, velocidad, acústica, monitor de actividad...).
*   **Chatbot de asistencia**: Ayuda sobre materias de tráfico, dudas de seguro, etc., con diferentes pestañas, contexto sobre las últimas 10 preguntas anteriores (5 intercambios de preguntas), permite copiar y compartir preguntas, y permite detener la generación si demora mucho tiempo pensando.

---

## 3. Arquitectura de la aplicación

### 3.1. Estructura general del sistema
*   La aplicación sigue el patrón de arquitectura MVVM (Model-View-ViewModel) para separar la lógica de negocio de la interfaz de usuario.
*   **Sensores usados**:
  1.  **Acelerómetro (AccelerometerProvider)**:
    *   Mide las fuerzas de aceleración en los ejes X, Y y Z.
    *   Calcula la Fuerza G total y el Jerk (la tasa de cambio de la aceleración), lo cual es crítico para identificar impactos bruscos.
  2.  **Giroscopio (GyroscopeProvider)**:
    *   Mide la velocidad angular del dispositivo.
    *   Se utiliza para determinar cambios de ángulo o rotaciones violentas (como un vuelco).
  3.  **Micrófono (AudioProvider)**:
    *   Captura el audio ambiente a través de la clase `AudioRecord`.
    *   Calcula la amplitud en decibelios (dB) para detectar sonidos característicos de una colisión (frenazos, rotura de cristales o despliegue de airbags).
  4.  **GPS / Ubicación (LocationProvider)**:
    *   Utiliza la API de Google Play Services (`FusedLocationProviderClient`).
    *   Obtiene la velocidad en tiempo real (convertida a km/h) y las coordenadas geográficas (latitud y longitud) para posicionar el evento en el mapa.

### 3.2. Integración con sensores, APIs o backend
La integración de SafeDrive AI se basa en una arquitectura de Fusión Sensorial Pasiva con un enfoque Offline-First, utilizando Firebase como infraestructura de backend. Desglose técnico de la interacción entre capas:

1.  **Integración con Sensores (Capa de Hardware)**
  *   La aplicación utiliza proveedores específicos para cada sensor, encapsulando la complejidad de las APIs de Android:
    *   **Acelerómetro y Giroscopio**: Usan `SensorManager` con un muestreo `SENSOR_DELAY_UI` (aprox. 60 Hz) para capturar fuerzas G, jerk (tirón) y ángulos de rotación.
    *   **Micrófono**: Utiliza la clase `AudioRecord` en modo `VOICE_RECOGNITION` o `UNPROCESSED` para calcular la amplitud en tiempo real (dB) sin grabar audio, por privacidad.
    *   **GPS**: Emplea `FusedLocationProviderClient` (Google Play Services) para obtener la velocidad en km/h y las coordenadas con alta precisión.
  *   Todos estos datos se exponen mediante Kotlin Coroutines (`StateFlow`), lo que permite que el resto de la app reaccione a los cambios de forma reactiva y eficiente.

2.  **Procesamiento y Lógica (Capa de Aplicación / "API" Interna)**
  *   El "cerebro" del sistema es el caso de uso `IncidentDetectorUC`, que gestiona el flujo de datos:
    1.  **Búfer Circular**: Los datos fluyen hacia `BlackBoxManager`, que mantiene las últimas 300 muestras (aprox. 5 segundos) en memoria RAM.
    2.  **Escudo Heurístico**: Un primer filtro físico descarta eventos leves (ej. < 3.5 G o < 15 km/h).
    3.  **Edge AI**: Si se supera el filtro, se invoca un modelo TensorFlow Lite (`safedrive_v4.tflite`) que clasifica el evento.
    4.  **Protocolo de Emergencia**: Si la IA confirma un accidente, se "congela" el búfer y se inicia la persistencia.

3.  **Backend y Sincronización**
  *   El sistema sigue un modelo híbrido para garantizar que los datos no se pierdan si no hay cobertura en el momento del impacto:
    *   **Persistencia Local (Offline-First)**:
      *   **Room Database**: Guarda los metadatos del incidente (fuerza G máxima, hora, ubicación) como un "atestado rápido".
      *   **Archivos JSON**: El `BlackBoxManager` vuelca los 5 segundos de telemetría bruta a un archivo físico (`EDR_EVENT_[timestamp].json`) en el almacenamiento interno.
    *   **Nube (Firebase)**:
      *   **Cloud Firestore**: Se utiliza para sincronizar los metadatos ligeros. Es lo que permite ver el historial de accidentes rápidamente desde cualquier dispositivo.
      *   **Cloud Storage**: Los archivos `.json` pesados (la "Caja Negra" forense) se suben aquí. Solo se descargan cuando es necesario realizar una reconstrucción técnica del accidente.
      *   **Sincronización**: Una vez que ambos (metadatos y archivo) se confirman en la nube, el registro local se marca como `isSynced = true`.

---

## 4. Sistema de Machine Learning

### 4.1. Problema de ML abordado
El objetivo del proyecto es desarrollar un sistema de detección automática de accidentes vehiculares capaz de ejecutarse en tiempo real de forma local (Edge AI) en smartphones y entornos integrados como Android Auto. El sistema debe procesar ráfagas de datos provenientes de los sensores internos del dispositivo (acelerómetro y giroscopio) junto con la telemetría del vehículo (velocidad por GPS) y señales acústicas ambientales (micrófono) para discernir instantáneamente si se ha producido un impacto crítico que ponga en riesgo la vida del conductor.

### 4.2. Dataset utilizado
*   **road_accident_imu_dataset_8000**: Dataset de 8000 muestras que combina circulación real y pruebas de impacto controlado (crash test).
*   **Dataset sintético de 7000 muestras en escala real**: Para simular condiciones físicas de telemetría vehicular ante la ausencia de datasets reales. Se modelan 5 características (features) basadas en sensores móviles:

  1.  **Etiqueta 0: Conducción Normal (Línea de Base)**
    *   Fuerza G: Entre 0.8 y 1.2 G (representa el estado de reposo y aceleraciones muy suaves).
    *   Jerk (Tirón): Muy bajo (< 2.0), indicando transiciones de velocidad fluidas.
    *   Firma Acústica: 30 a 60 dB (ruido ambiente normal dentro de un habitáculo).
    *   Cambio Angular: Mínimo (< 30º/s), típico de curvas suaves o mantenimiento de carril.
    *   Velocidad: Rango amplio (0-120 km/h) pero con los otros sensores estables.

  2.  **Etiqueta 1: Conducción Agresiva / Frenazo (Pre-Impacto)**
    *   Fuerza G: Sube al rango de 1.2 a 4.0 G. Es el umbral donde el coche está bajo estrés pero no hay colisión física.
    *   Jerk: Incremento notable (2.0 a 7.0), representando "pisotones" al freno o acelerones bruscos.
    *   Firma Acústica: 60 a 85 dB. Corresponde al chirrido de neumáticos, revoluciones altas del motor o ruidos de frenado intenso.
    *   Cambio Angular: 30º a 120º/s. Indica "volantazos" bruscos o maniobras evasivas.

  3.  **Etiqueta 2: Accidente (Impacto Detectado)**
    *   Fuerza G: Superior a 4.0 G (hasta 15 G). Este es el disparador crítico del EDR.
    *   Jerk: Valores extremos (7.0 a 20.0), reflejando la parada instantánea del vehículo tras el choque.
    *   Firma Acústica: 90 a 120 dB. Representa el estruendo de un impacto violento, rotura de cristales o detonación de airbags.
    *   Cambio Angular: 120º a 500º/s. Captura movimientos caóticos como trompos, vuelcos o desplazamientos laterales violentos post-impacto.

### 4.3. Modelos evaluados
*   **Random Forest (100 árboles)**: Aunque no genera TFLite, se ha usado como un Modelo de Control o Benchmark.
  1.  Al obtener un 100% de éxito, confirmó que el dataset era "matemáticamente perfecto" y que si la Red Neuronal (MLP) fallaba, el problema estaba en la arquitectura de la red o en la normalización, no en los datos.
  2.  **Incompatibilidad**: No existe un soporte nativo y eficiente de TensorFlow Lite para modelos de Random Forest de 100 árboles.
  3.  **Consumo de Batería**: Evaluar 100 árboles lógicos uno por uno en cada ciclo de sensor (60 veces por segundo) agotaría la batería del usuario mucho más rápido que las multiplicaciones de matrices optimizadas que hace la MLP.

*   **MLP: Red Neuronal Perceptrón Multicapa**
  *   **Arquitectura del Modelo MLP**: El modelo se ha diseñado para ser ligero y eficiente, permitiendo su ejecución en tiempo real en dispositivos móviles. Su estructura se divide en cuatro bloques funcionales:
    *   **A. Capa de Entrada (5 Dimensiones)**: Recibe los datos brutos directamente de los sensores sin procesamiento previo por parte de la App. Las 5 variables de entrada son:
      1.  Fuerza G: Intensidad del impacto/movimiento.
      2.  Jerk: Rapidez con la que cambia la aceleración (clave para detectar colisiones).
      3.  Firma Acústica (dB): Nivel de ruido ambiental para identificar estruendos.
      4.  Cambio Angular: Rotación del vehículo (detección de vuelcos/derrapes).
      5.  Velocidad: Contexto cinético del vehículo.
    *   **B. Capa de Normalización Integrada (Keras)**: Esta es la mejora clave del Experimento 4. Al incluir la normalización dentro del archivo `.tflite`, el modelo se vuelve "autónomo".
      *   Función: Transforma los valores reales (ej. 100 dB o 120 km/h) a una escala matemática que la red pueda entender (generalmente centrada en 0 con desviación estándar de 1).
      *   Ventaja: Elimina el riesgo de errores de cálculo en el código de Android (Kotlin), ya que el modelo se encarga de su propio escalado interno usando la media (mean) y varianza (variance) aprendidas durante el entrenamiento.
    *   **C. Capas Densas (Procesamiento)**:
      *   Configuración: Dos capas ocultas de 24 y 12 neuronas respectivamente.
      *   Activación ReLU: Se utiliza para introducir "no linealidad". Esto permite que el modelo aprenda que un accidente no es solo "mucha fuerza G", sino una combinación específica (ej: alta G + alto ruido + cambio angular brusco). Si la señal no es lo suficientemente fuerte, ReLU la "apaga" (valor 0), filtrando el ruido de vibraciones comunes.
    *   **D. Capa de Salida (Clasificación Softmax)**: Produce tres valores que suman 1.0 (100%), representando la probabilidad de cada categoría:
      1.  Normal (Clase 0): Conducción estable.
      2.  Susto/Agresiva (Clase 1): Frenazos o maniobras bruscas que no llegan a ser colisión.
      3.  Choque (Clase 2): Impacto confirmado que dispara el protocolo de emergencia y el guardado de la Caja Negra (EDR).

### 4.4. Métricas utilizadas
*   Peak_G: 0.8 - 15.0 Gs.
*   Jerk: 0.1 - 20.0 G/s.
*   Firma_Acústica: 30.0 - 120.0 dB.
*   Cambio_Angular: 0.0 - 500.0 º/s.
*   Velocidad: 0.0 - 200.0 km/h.

### 4.5. RAG en detalle
*   **Indexación**
  1.  **Carga de documentos** (BOE, manual primeros auxilios, procedimiento partes seguro)
    *   Texto: LangChain - PyPDFLoader
    *   Imagen, modelo: (finalmente no implementado, tarda demasiado)
      *   "qwen2.5vl"
      *   Gemma2.5 (límite tokens free tier)
  2.  **Chunking**: Elegidos los siguientes parámetros debido a la optimización técnica y la naturaleza de los datos
    *   `CHUNK_SIZE = 512` (nomic-embed-text tiene un límite nativo de 512 tokens por secuencia, garantizando 100% de texto chunk procesado)
    *   `CHUNK_OVERLAP = 150` (debido al BOE y PAS con frases largas; 150 representa aprox. 30% de solapamiento)
  3.  **Embedding documento**
    *   nomic-embed-large (descartado debido al tiempo de cálculo de similitud en entorno de respuestas críticas, y por ser pesado -ocupa gigas- con ventana de contexto más limitada)
    *   nomic-embed-text (elegido: solo ocupa pocas MB, ventana de contexto superior -8192 tokens-)
  4.  **DB vectorial**
    *   Pinecone (descartado)
    *   ChromaDB (elegido)
    *   *Se priorizó ChromaDB por su capacidad de ejecución local. A diferencia de Pinecone, que requiere conexión constante a la nube, ChromaDB permite que el asistente de SafeDrive AI funcione en entornos de baja conectividad, garantizando la privacidad de los datos del usuario y eliminando latencias de red y costes de infraestructura externa.*

*   **Consulta**
  1.  **Embedding pregunta**
    *   nomic-embed-large (descartado, muy pesado)
    *   nomic-embed-text (elegido)
  2.  **Búsqueda similitud**: Similitud coseno
  3.  **Construcción prompt**
    *   Priorizar el contexto recuperado del BOE.
    *   Responder siempre en español.
    *   Ser conciso y directo (en una emergencia el usuario no quiere leer un ensayo).
    *   Admitir "No lo sé" si la información no está en los manuales, eliminando así las alucinaciones.
  4.  **Generación LLM**
    *   `LLM_MODEL = "qwen2.5:7b"`
    *   *Se seleccionó Qwen 2.5 7B sobre Llama 3 o Gemma porque, en las pruebas de benchmark, demostró una capacidad superior para seguir instrucciones complejas en español y un manejo más preciso de términos técnicos de seguridad vial. Además, su tamaño de 7 mil millones de parámetros permite una inferencia rápida (baja latencia) en hardware local, algo crítico para una respuesta ágil tras un accidente.*

---

## 5. Resultados

### 5.1. Resultados experimentales
*   **LLM**
  1.  Dataset sintético normalizado + MLP
  2.  Dataset sintético real + MLP con normalización
  3.  Dataset sintético + Random Forest para comprobación
  4.  Dataset `road_accident_imu_dataset_8000` + MLP con normalización
  5.  Dataset fusionado (sintético con `road_accident_imu_dataset_8000`) + Random Forest (comprobación y control de dataset): procedimiento de comparación entre Random Forest y MLP para validar benchmarks; no genera TFLite.
  6.  Dataset fusionado (sintético con `road_accident_imu_dataset_8000`) + MLP con normalización

*   **RAG**
  1.  Llama 3 sin RAG
  2.  Qwen2.5:7b sin RAG
  3.  Qwen2.5:7b con RAG + Gemma2.5 (multimodal): consumía mucho y gastaba tokens del free tier, descartado.
  4.  Qwen2.5:7b con RAG + qwen2.5vl (multimodal): Muy lento, no rentable.
  5.  Qwen2.5:7b con RAG: faltaban documentos y normativas como la baliza V16.
  6.  Qwen2.5:7b con RAG mejorado: con BOE actualizado y normativas recientes.

### 5.2. Funcionamiento del modelo en la aplicación
*   **Parte Redes Neuronales TFLite**: Se ha creado una clase `IncidentClassifier` para conectar con el móvil.
*   **Parte RAG**: Servidor local ChromaDB, Ollama local para chatbot asistente. ChromaDB + chatbot mediante FastAPI, `K=3` para relevancia. Se conecta el chatbot con RAG a la app móvil mediante el objeto `OllamaClient`.

---

## 6. Demostración
*   Demostración de la aplicación funcionando (si es posible).

---

## 7. Conclusiones

### 7.1. Dificultades encontradas
*   Manejo de sensores en segundo plano: teoría vs. realidad.
*   Reducir el consumo de batería debido al uso continuo de sensores.
*   Falta de datasets reales y dificultad para realizar pruebas de accidentes reales.
*   Implementación de CI/CD.
*   Falta de tiempo para perfeccionar la aplicación.

### 7.2. Posibles mejoras futuras
*   Mapa de balizas V16.
*   Usar Google AI Edge SDK (MediaPipe) o TensorFlow Lite para generar los embeddings.
*   Asistente local en el móvil.
*   Cambiar ChromaDB por una base de datos vectorial nativa para móvil (como ObjectBox o simplemente guardando los vectores en un archivo binario ligero).
*   Soporte para Android Auto.
*   Automatizar el reentrenamiento del modelo TFLite mediante datos recolectados del feedback guardados en la base de datos Firebase.
*   Completar la implementación de CI/CD.