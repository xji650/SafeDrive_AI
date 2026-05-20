# SafeDrive AI
## Android en Sistemas ADAS y EDR mediante Edge AI y Fusión Sensorial

---

### 1. Introducción

**Problema que se quiere resolver**
Los sistemas de Seguridad Vial Pasiva, como los Sistemas Avanzados de Asistencia al Conductor (ADAS) y los Registradores de Datos de Eventos (EDR o "Caja Negra"), están actualmente restringidos a vehículos de gama media-alta o a flotas corporativas, debido a su elevado coste de instalación y mantenimiento. Esta barrera deja a la gran mayoría de conductores sin un registro objetivo e inmutable de las condiciones previas y durante un siniestro. La idea surge tras identificar una carencia crítica en el sector asegurador (inspirada por la experiencia de profesionales del sector, como Mapfre): la falta de datos telemáticos fiables y accesibles para la reconstrucción objetiva de accidentes, lo que complica la gestión de partes, la determinación de responsabilidades y la atención de emergencia.

**Objetivo de la aplicación**
Democratizar la seguridad vial pasiva convirtiendo cualquier smartphone Android en un sistema ADAS/EDR autónomo. SafeDrive AI procesa telemetría vehicular en tiempo real mediante **Inteligencia Artificial en el borde (Edge AI)** y modelos TensorFlow Lite, aplicando una arquitectura de **Fusión Sensorial Pasiva** (acelerómetro, giroscopio, micrófono y GPS). El sistema registra incidencias críticas generando un archivo EDR en formato JSON (300 muestras, ~3-5 segundos de datos previos al impacto), activa protocolos de emergencia en menos de 15 segundos y ofrece retroalimentación continua para el reentrenamiento del modelo.

---

### 2. Descripción del Sistema

**Idea general del proyecto**
SafeDrive AI es una aplicación móvil (con soporte planificado para Android Auto) que monitoriza de forma continua el entorno y la dinámica del vehículo. Mediante IA de fusión sensorial ejecutada localmente (`safedrive_v4.tflite`), detecta colisiones en tiempo real y, de confirmarse un incidente crítico, notifica automáticamente a contactos de emergencia mediante SMS y realiza una llamada al 112 en un lapso inferior a 15 segundos, maximizando la probabilidad de supervivencia y asistencia temprana.

**Funcionalidades principales**
- **EDR Inteligente:** Registra incidentes aplicando fusión sensorial + filtro heurístico estricto (`Velocidad ≥ 15 km/h AND Peak_G ≥ 3.5 G AND Audio ≥ 85 dB`). Genera reportes forenses en JSON y resúmenes ejecutivos en PDF.
- **Bucle de Retroalimentación (Feedback Loop):** El sistema permite validar/corregir falsos positivos/negativos, almacenando estas etiquetas para reentrenar y mejorar continuamente la IA.
- **Asistente IA para Seguridad Vial:** Chatbot especializado que resuelve dudas sobre normativa de tráfico, protocolos post-accidente y gestiones de seguros.
- **Dashboard de Monitorización:** Interfaz en tiempo real que muestra el estado de salud de los sensores, telemetría activa (velocidad, mapa GPS, niveles acústicos, monitor de actividad) y el estado del sistema (listo/detectando/sincronizando).
- **Chatbot Avanzado con Contexto:** Historial persistente de las últimas 10 preguntas (5 intercambios), permite copiar/compartir respuestas, botón de interrupción de generación ("Stop") para optimizar la experiencia, y respuestas concisas adaptadas a entornos de estrés.

---

### 3. Arquitectura de la Aplicación

**Estructura general del sistema**
La aplicación sigue el patrón arquitectónico **MVVM (Model-View-ViewModel)**, garantizando una separación estricta entre la lógica de negocio, la gestión de datos y la interfaz de usuario. Esto facilita la mantenibilidad, la testabilidad unitaria y la escalabilidad del código en Kotlin.

**Sensores utilizados**
1. **Acelerómetro (`AccelerometerProvider`):** Mide fuerzas en ejes X, Y, Z. Calcula la Fuerza G total y el *Jerk* (derivada temporal de la aceleración), parámetro crítico para identificar impactos bruscos y diferenciarlos de vibraciones normales.
2. **Giroscopio (`GyroscopeProvider`):** Mide velocidad angular. Fundamental para detectar rotaciones violentas, derrapes o eventuales vuelcos.
3. **Micrófono (`AudioProvider`):** Captura audio ambiente mediante `AudioRecord`. Calcula la amplitud RMS convertida a decibelios (dB) para detectar firmas acústicas de colisión (cristales rotos, despliegue de airbags, frenazos extremos). **No almacena audio** por diseño, garantizando la privacidad.
4. **GPS / Ubicación (`LocationProvider`):** Utiliza `FusedLocationProviderClient` de Google Play Services. Obtiene coordenadas geográficas y velocidad instantánea (convertida a km/h) para geolocalizar y contextualizar cinéticamente cada evento.

**Integración con sensores, APIs y Backend**
La arquitectura sigue un enfoque **Offline-First** con sincronización asíncrona hacia Firebase:
1. **Capa de Hardware:** Proveedores encapsulados con `SensorManager` (~60Hz) y `AudioRecord`. Los flujos de datos se exponen mediante **Kotlin Coroutines y `StateFlow`**, permitiendo una reacción reactiva y eficiente en toda la app.
2. **Capa de Lógica (`Incide![img.png](img.png)ntDetectorUC`):**
  - **Búfer Circular:** `BlackBoxManager` mantiene las últimas 300 muestras (~5s) en RAM.
  - **Escudo Heurístico:** Filtra descarta eventos triviales (<3.5G, <15 km/h).
  - **Edge AI:** Invoca `safedrive_v4.tflite` para clasificación.
  - **Protocolo de Emergencia:** Congela el búfer y dispara persistencia si se confirma impacto.
3. **Backend y Sincronización:**
  - **Local (Room DB + Archivos JSON):** Almacena metadatos rápidos y vuelca la telemetría cruda a `EDR_EVENT_[timestamp].json`.
  - **Nube (Firebase):** `Cloud Firestore` sincroniza metadatos para acceso multi-dispositivo. `Cloud Storage` sube los JSON forenses. Se marca `isSynced = true` tras confirmación dual, permitiendo consulta offline inmediata sin pérdida de datos en zonas sin cobertura.

---

### 4. Sistema de Machine Learning

**Problema ML abordado**
Desarrollar un clasificador de accidentes vehicular en tiempo real, ejecutable de forma local en smartphones (Edge AI). El modelo debe procesar ráfagas de datos multimodales (IMU, GPS, audio) para discernir instantáneamente entre conducción normal, maniobras evasivas y colisiones críticas, minimizando falsos positivos y latencia.

**Dataset utilizado**
- **Dataset Real:** `road_accident_imu_dataset_8000` (8.000 muestras combinando conducción real y pruebas controladas de impacto).
- **Dataset Sintético:** 7.000 muestras escaladas a física real para cubrir escenarios de colisión difíciles de replicar de forma ética/segura. Modela 3 clases:
  - `0 - Normal:` 0.8-1.2 G, Jerk <2.0, 30-60 dB, <30º/s, 0-120 km/h.
  - `1 - Agresiva/Frenazo:` 1.2-4.0 G, Jerk 2.0-7.0, 60-85 dB, 30-120º/s.
  - `2 - Accidente:` >4.0 G (hasta 15 G), Jerk 7.0-20.0, 90-120 dB, 120-500º/s.

**Modelos evaluados**
- **Random Forest (100 árboles):** Usado como *benchmark* de control. Alcanzó 100% de acierto en datos sintéticos, validando la calidad matemática del dataset. Descartado para producción por incompatibilidad nativa con TFLite y alto consumo de batería al evaluar 100 árboles a 60 Hz.
- **MLP (Perceptrón Multicapa):** Modelo final seleccionado. Arquitectura ligera:
  - **Entrada:** 5 dimensiones (G, Jerk, dB, ΔAngular, Velocidad).
  - **Normalización Integrada (Keras):** Escalado interno dentro del `.tflite`, eliminando dependencias de preprocesamiento en Kotlin y reduciendo errores de despliegue.
  - **Capas Densas:** 24 → 12 neuronas con activación ReLU para capturar no linealidades complejas (ej: alta G + alto ruido + rotación brusca).
  - **Salida:** Softmax de 3 clases (probabilidades normalizadas).

**Métricas de entrada (Rangos de normalización)**
`Peak_G:` 0.8 - 15.0 G | `Jerk:` 0.1 - 20.0 G/s | `Firma_Acústica:` 30.0 - 120.0 dB | `Cambio_Angular:` 0.0 - 500.0 º/s | `Velocidad:` 0.0 - 200.0 km/h

**RAG en Detalle (Asistente IA)**
- **Indexación:** Carga de manuales (BOE, primeros auxilios, partes de seguro) vía `PyPDFLoader`. Chunking optimizado: `SIZE=512`, `OVERLAP=150` (adaptado a la estructura legal y al límite nativo de `nomic-embed-text`).
- **Embeddings:** `nomic-embed-text` elegido por su bajo peso (<50MB), ventana de contexto de 8192 tokens y rapidez. Se descartó `nomic-embed-large` por latencia y consumo.
- **Base de Datos Vectorial:** **ChromaDB** ejecutado localmente. Priorizado sobre Pinecone para garantizar funcionamiento offline, privacidad absoluta y eliminación de costes/latencia de red.
- **Consulta y Prompting:** Embedding de query + similitud coseno. Prompt estructurado para: priorizar contexto BOE, responder en español, ser conciso (entorno de emergencia) y rechazar alucinaciones ("No lo sé" si no hay evidencia documental).
- **Generación LLM:** `qwen2.5:7b` seleccionado por su superior adherencia a instrucciones en español, precisión en terminología vial y baja latencia de inferencia en hardware local.

---

### 5. Resultados

**Resultados experimentales**
- **Redes Neuronales (TFLite):**
  1. Dataset sintético normalizado + MLP → Alto acierto, validación de arquitectura.
  2. Dataset sintético "realista" + MLP con normalización integrada → Mejora de robustez frente a ruido.
  3. Dataset sintético + Random Forest → Benchmark de control (100% exactitud matemática).
  4. Dataset real (`8000`) + MLP con normalización → Validación de generalización en señales crudas reales.
  5. Dataset fusionado (Sintético + Real) + RF → Confirmación de alineación entre clases sintéticas y reales.
  6. Dataset fusionado + MLP con normalización → **Modelo final `safedrive_v4.tflite`**, equilibrando precisión, tamaño (<2MB) y consumo energético.
- **Sistema RAG:**
  1. Llama 3 y Qwen2.5 sin RAG → Respuestas genéricas, propensión a alucinaciones en normativa específica.
  2. Qwen2.5 + RAG + Gemma2.5/Qwen2.5VL (multimodal) → Descartados por consumo excesivo de tokens free-tier y latencia inaceptable (>4s).
  3. Qwen2.5 + RAG inicial → Carecía de normativa actualizada (ej. Baliza V16).
  4. **Qwen2.5 + RAG mejorado** → Integración de BOE actualizado y normativas recientes. Respuestas precisas, concisas y 100% fundamentadas en documentos locales.

**Funcionamiento del modelo en la aplicación**
- **Inferencia TFLite:** Integrada mediante la clase `IncidentClassifier`, que carga el modelo `.tflite`, gestiona el tensor de entrada y retorna probabilidades en tiempo real sin bloquear el hilo principal.
- **RAG/Chatbot:** Desplegado como servicio local (`ChromaDB` + `Ollama`). Comunicación con la app móvil mediante API REST en **FastAPI**. Configurado con `K=3` para recuperación de contexto relevante. La app consume el servicio a través del cliente `OllamaClient`, manejando streaming de tokens y cancelación de solicitudes.

---

### 6. Demostración

*(Nota: En un entorno de entrega real, esta sección incluiría capturas de pantalla, enlaces a video o un APK de pruebas)*
- **Flujo 1:** Inicio de app → Dashboard mostrando sensores activos (GPS fijado, audio en ~45dB, G estable ~1.0).
- **Flujo 2:** Simulación de impacto (sacudida controlada o inyección de datos) → Detección <200ms → Congelación de JSON EDR → Notificación SMS a contactos → Llamada automática 112 en <15s.
- **Flujo 3:** Apertura de Chatbot → Pregunta: *"¿Qué hacer tras un choque con baliza V16 obligatoria?"* → Respuesta en ~2.5s citando BOE actual, con botón de compartir y opción de detener.
- **Flujo 4:** Exportación de incidente → Generación automática de PDF + JSON forense → Sincronización asíncrona a Firebase.

---

### 7. Conclusiones

**Dificultades encontradas**
- **Gestión de sensores en segundo plano:** La teoría de Android vs. la realidad de optimización de batería (Doze Mode, restricciones de `ForegroundService`, throttling de GPS) requirió ajustes finos en la gestión de ciclos de muestreo y wake-locks.
- **Dataset limitado:** La escasez de datasets públicos de colisiones reales con telemetría multimodal obligó a generar y validar datos sintéticos, introduciendo desafíos de *domain gap*.
- **Validación física:** La imposibilidad ética y logística de provocar accidentes reales limita las pruebas a simulaciones controladas y datos históricos.
- **CI/CD e infraestructura:** La implementación de un pipeline completo de integración continua, firmado automático y despliegue en stores consumió tiempo de desarrollo crítico.
- **Tiempo de desarrollo:** La complejidad de integrar Edge AI, RAG local, arquitectura Offline-First y UI pulida exigió priorizar funcionalidades core sobre refinamiento cosmético.

**Posibles mejoras futuras**
- **Integración Baliza V16:** Mapa interactivo con geolocalización de balizas inteligentes y estado en tiempo real.
- **Embeddings on-device:** Migración de generación de embeddings a **Google AI Edge SDK (MediaPipe)** o conversión a TFLite para eliminar dependencia de servidor local.
- **Asistente 100% local:** Optimización de Qwen2.5 o uso de modelos Nano/Gemma optimizados para ejecución directa en NPU del smartphone.
- **Vector DB nativa móvil:** Sustituir ChromaDB por **ObjectBox** o almacenamiento binario optimizado para reducir footprint de memoria.
- **Android Auto nativo:** Adaptación completa de la UI/UX y gestión de permisos para el ecosistema automotriz de Google.
- **Reentrenamiento automatizado:** Pipeline que recolecte feedback validado por usuarios desde Firebase, reentrenue el MLP y despliegue nuevas versiones de `.tflite` vía OTA.
- **CI/CD robusto:** Automatización de builds, tests unitarios/instrumentados, análisis estático (SonarQube) y despliegue a Firebase App Distribution/Play Console.
