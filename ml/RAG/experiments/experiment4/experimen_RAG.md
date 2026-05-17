# Experimento4: RAG Multimodal & Edge AI (Arquitectura Final)

Este experimento constituye la fase final de investigación y desarrollo del sistema RAG (Retrieval-Augmented Generation) local para el asistente de seguridad vial **SafeDrive AI**. Se centra en la implementación de una arquitectura "Edge" capaz de procesar normativas legales y protocolos médicos complejos de forma privada y eficiente.

## Objetivo
Desarrollar y validar la arquitectura definitiva del sistema RAG local, optimizando la densidad semántica del conocimiento indexado y garantizando inferencias precisas mediante modelos de lenguaje masivos ejecutados en local (Ollama).

## Stack Tecnológico
*   **Orquestador:** [LangChain](https://python.langchain.com/) (LCEL - LangChain Expression Language).
*   **Base de Datos Vectorial:** [ChromaDB](https://www.trychroma.com/) (Persistencia local).
*   **Embeddings:** `nomic-embed-text` (Ollama).
*   **LLM (Cerebro):** `qwen2.5:7b` (Ollama).
*   **Visión Artificial:** `qwen2.5-vl` (Ollama) / Integración Gemini API (opcional).
*   **Procesamiento PDF:** `pypdfium2` (Alta velocidad).
*   **Análisis de Datos:** `scikit-learn` (PCA) y `matplotlib`.

## Metricas del Dataset (Real Scale)
Para este experimento se ha procesado el corpus completo de SafeDrive AI:
*   **Fuentes:** 17 documentos PDF (BOE, Manuales DGT, Protocolos de Auxilio).
*   **Volumen:** 2.237 páginas procesadas.
*   **Indexación:** 15.135 fragmentos (chunks) vectorizados.
*   **Configuración de Chunking:**
    *   Tamaño: 512 caracteres.
    *   Solapamiento (Overlap): 150 caracteres.
    *   Estrategia: `RecursiveCharacterTextSplitter` con separadores optimizados para artículos legales.

## Flujo de Implementación
1.  **Ingesta Multimodal:** Extracción de texto nativo y análisis de componentes gráficos.
2.  **Fragmentación de Alta Densidad:** División del texto para mantener la cohesión de leyes y protocolos médicos.
3.  **Vectores y Persistencia:** Generación de embeddings con Nomic y almacenamiento en ChromaDB con métrica de distancia coseno.
4.  **Pipeline RAG Edge:** Cadena de inferencia local que recupera los 6 fragmentos más relevantes para alimentar al modelo Qwen 2.5.
5.  **Visualización Semántica:** Proyección PCA para verificar el agrupamiento por conceptos (Tráfico vs. Médico).

## Resultados de las Pruebas de Estrés
El sistema fue sometido a casos de control críticos:
*   **Legal:** Consultas sobre responsabilidad en partes amistosos.
*   **Médico:** Protocolos de actuación ante hemorragias post-choque.
*   **Seguridad:** Posicionamiento de vehículos de auxilio en curvas de baja visibilidad.

**Conclusión:** El modelo demuestra una alta fidelidad al contexto recuperado, citando fuentes exactas (Documento + Página) y evitando alucinaciones gracias al prompt restrictivo.

## Visualización y Exportación
El experimento incluye:
*   **Mapa de Calor PCA:** Visualización en 2D de cómo el modelo diferencia entre las distintas fuentes de datos.
*   **Proyector de Embeddings:** Generación de archivos `embeddings.tsv` y `metadata.tsv` compatibles con [TensorFlow Projector](https://projector.tensorflow.org/) para inspección tridimensional de los datos.

---
**SafeDrive AI** - *App Mob + IA*