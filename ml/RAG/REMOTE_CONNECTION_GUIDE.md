# 📘 Manual de Conexión Remota SafeDrive AI (LLM + RAG)

Este manual te permitirá conectar la aplicación Android con tu servidor local de Inteligencia Artificial (Ollama) y tu base de datos vectorial (ChromaDB) aunque estés usando datos móviles o una red WiFi externa, eliminando las restricciones de la red local.

## 📋 Requisitos Previos

1.  **En el PC:**
    * Ollama instalado con el modelo `qwen2.5:7b` y `nomic-embed-text` descargados.
    * Python con FastAPI/ChromaDB (tu servidor RAG) funcionando en el puerto 8000.
    * [Ngrok](https://ngrok.com/download) instalado en el sistema (`winget install ngrok.ngrok` o ejecutable local).
    * Una cuenta gratuita creada en [ngrok.com](https://ngrok.com).
2.  **En el Móvil:**
    * App SafeDrive AI instalada en el dispositivo Android o emulador.

---

## Paso 1: Configurar Ollama para Conexiones Externas

Por defecto, Ollama solo escucha peticiones que nacen dentro de tu propio PC (`localhost`). Debes obligarlo a escuchar al entorno de red:

1.  Cierra Ollama por completo desde los iconos ocultos de Windows (junto al reloj: clic derecho en la llama -> **Quit**).
2.  Abre una terminal (**PowerShell**) y ejecuta estas variables de entorno seguidas del arranque del servicio:
    ```powershell
    $env:OLLAMA_HOST="0.0.0.0"
    $env:OLLAMA_ORIGINS="*"
    ollama serve
    ```
    *Deja esta ventana de PowerShell abierta de fondo.*

---

## Paso 2: Ejecutar tu Servidor RAG (Python + FastAPI)

Asegúrate de que tu script de Python esté levantado y escuchando correctamente en el puerto `8000`.

1.  Abre otra terminal en la carpeta de tu proyecto (`C:\workspace\PEX_Mob\Machine_learning\...\MLFruits\ML\`).
2.  Ejecuta el servidor:
    ```bash
    python main.py
    ```
    *(Verás en la consola el mensaje: `INFO: Cargando ChromaDB local...` y el estado listo en el puerto 8000).*

---

## Paso 3: Autenticar y Configurar los Túneles simultáneos de Ngrok

Dado que la arquitectura de SafeDrive AI requiere exponer dos puertos diferentes (el `8000` para el contexto de ChromaDB y el `11434` para el modelo de Ollama), y la cuenta gratuita de Ngrok no permite abrir dos terminales CLI independientes a la vez, utilizaremos el archivo de configuración unificado de Ngrok.

### 3.1. Añadir tu credencial de seguridad (Authtoken)
Busca tu clave secreta gratuita en tu panel de Ngrok ([dashboard.ngrok.com/get-started/your-authtoken](https://dashboard.ngrok.com/get-started/your-authtoken)), abre una terminal nueva de Windows y ejecútalo para identificarte:
```powershell
ngrok config add-authtoken C:\Users\LittleDragon\AppData\Local/ngrok/ngrok.yml

```

### 3.2. Configurar el archivo multi-túnel

Para indicarle a Ngrok que abra ambos puertos bajo el mismo proceso, ejecuta el siguiente comando para abrir el archivo de configuración en tu bloc de notas:

```powershell
ngrok config edit

```

Borra todo lo que aparezca en ese archivo de texto y **pega exactamente este bloque de configuración**:

```yaml
version: "3"
agent:
  authtoken: TU_AUTHTOKEN_SECRETO_AQUÍ
tunnels:
  ollama_tunnel:
    proto: http
    addr: 11434
  rag_tunnel:
    proto: http
    addr: 8000

```

*Guarda el archivo de texto y ciérralo.*

### 3.3. Lanzar todos los túneles a la vez

En la misma terminal, ejecuta el comando de arranque unificado:

```powershell
ngrok start --all

```

La consola de Ngrok cambiará a una pantalla negra interactiva. Verás una sección llamada **Forwarding** con dos URLs públicas distintas que apuntan a tus puertos locales:

1. Busca la URL que redirige a `http://localhost:11434` (Ej: `https://a1b2-34-56.ngrok-free.app`). **Esta es la URL pública de Ollama**.
2. Busca la URL que redirige a `http://localhost:8000` (Ej: `https://7x8y-90-12.ngrok-free.app`). **Esta es la URL pública de tu RAG**.

---

## Paso 4: Configurar la App en el Móvil (OllamaClient.kt)

Para que el teléfono Android envíe las peticiones a través de internet hacia los túneles seguros que acabas de abrir, abre tu proyecto en Android Studio, localiza el objeto de configuración del cliente de red (`OllamaClient.kt`) y actualiza las constantes con tus nuevas URLs de Ngrok:

```kotlin
object OllamaClient {
    // ── CONFIGURACIÓN DE RUTAS REMOTAS (NGROK) ──
    
    // Pega aquí la URL de Ngrok que apunta al puerto 11434
    private const val BASE_URL = "[https://a1b2-34-56.ngrok-free.app](https://a1b2-34-56.ngrok-free.app)"

    // Pega aquí la URL de Ngrok que apunta al puerto 8000 (FastAPI)
    private const val CHROMA_URL = "[https://7x8y-90-12.ngrok-free.app](https://7x8y-90-12.ngrok-free.app)"
    
    private const val USE_MOCK = false
    
    // ... resto del código del cliente
}

```

*Compila de nuevo la aplicación (`Run app`) en tu emulador o en tu smartphone físico.*

---

## 🛠 Comandos Rápidos de Referencia (Resumen)

| Servicio / Componente | Comando de Activación en PC | Puerto Interno | Tipo de Endpoint |
| --- | --- | --- | --- |
| **1. Cerebro Local (Ollama)** | `$env:OLLAMA_HOST="0.0.0.0"; ollama serve` | 11434 | Local (PC) |
| **2. Base de Datos RAG (Python)** | `python main.py` | 8000 | Local (PC) |
| **3. Configurar Credencial** | `ngrok config add-authtoken <token>` | Config | Global |
| **4. Pasarela Global (Ngrok)** | `ngrok start --all` | Múltiple | **Público (HTTPS)** |

---

## ⚠️ Solución de Problemas Avanzada

* **Error 502 Bad Gateway / ERR_NGROK_502:** Ngrok está en línea en internet pero no puede hablar con el puerto dentro de tu PC. Revisa que no hayas cerrado la ventana de la terminal de Ollama o la de tu script de Python (`main.py`).
* **Error 404 Not Found (Model Error):** El túnel funciona bien, pero Ollama devuelve un error porque el String del modelo seleccionado en la interfaz del móvil no coincide exactamente con el nombre del modelo local. Asegúrate de estar pidiendo `"qwen2.5:7b"` o el modelo exacto que tengas listado al ejecutar `ollama list` en tu PC.
* **Error de Timeout (Tiempo de espera agotado):** El tráfico enviado desde el móvil tarda en viajar por internet a través de Ngrok, procesar los embeddings y redactar la respuesta. Asegúrate de que los valores de `readTimeout` en tu archivo `OllamaClient.kt` estén configurados como mínimo en `30000` (30 segundos) para Chroma y `120000` (2 minutos) para Ollama.
* **Cambio de URLs al reiniciar:** Recuerda que bajo el plan gratuito de Ngrok, cada vez que cierres la consola y la vuelvas a abrir con `ngrok start --all`, las URLs generadas cambiarán de forma aleatoria. Deberás actualizar las constantes de Android Studio con las nuevas direcciones de esa sesión.

```

```