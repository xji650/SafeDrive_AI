## 1. Crear el Entorno Virtual (Aislado)

Asegúrate de que estás en la carpeta `ml\RAG` y ejecuta el comando para crear el contenedor (lo llamaremos `.venv`):

```powershell
& C:\Users\LittleDragon\AppData\Local\Microsoft\WindowsApps\python3.13.exe -m venv .venv

```

*(Tardará unos segundos en crear la carpeta silenciosamente).*

## 2. Activar el Entorno

Ahora dile a PowerShell que quieres "entrar" en ese contenedor. Ojo a los símbolos:

```powershell
.venv\Scripts\Activate.ps1

```

> 🔍 **REVISIÓN OBLIGATORIA:** Mira tu consola. Justo a la izquierda de la ruta de tu carpeta, tiene que aparecer obligatoriamente el texto **`(.venv)`**. Si lo ves, estás dentro del entorno seguro.

*(Si Windows te da un error de "Execution Policies" al intentar activarlo, ejecuta primero `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process` y vuelve a probar el comando de activación).*

## 3. Instalar las dependencias (dentro del entorno)

Como ya estás dentro de `(.venv)`, no uses rutas raras de ejecutables. Llama a `pip` directamente para que lea tu archivo de requisitos:

```powershell
pip install -r scripts/requirements.txt

```

*(Aquí verás cómo se descargan FastAPI, Chroma y Torch. Esta vez déjalo terminar por completo. Se guardará todo dentro de la carpeta del proyecto, sin tocar tu Windows).*

## 4. Arrancar tu script

Una vez termine la descarga sin errores, arranca tu servidor usando el comando `python` a secas (que ahora apunta a tu entorno virtual):

```powershell
python scripts/main.py

```