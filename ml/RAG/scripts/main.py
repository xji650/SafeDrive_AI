import os
import sys  # <-- 1. IMPORTANTE: Importamos la librería del sistema
from fastapi import FastAPI
from pydantic import BaseModel
from langchain_community.vectorstores import Chroma
from langchain_ollama import OllamaEmbeddings
import uvicorn

# 2. BLINDAJE TOTAL PARA WINDOWS:
# Si el servidor detecta caracteres raros o emojis enviados desde el móvil,
# los sustituirá por un carácter seguro en la terminal en lugar de colgarse.
if sys.platform == "win32":
    sys.stdout.reconfigure(errors="replace")
    sys.stderr.reconfigure(errors="replace")

app = FastAPI(title="Servidor RAG - MLFruits")

DIR_DEL_SCRIPT = os.path.dirname(os.path.abspath(__file__))

print("INFO: Cargando ChromaDB local con Nomic Embeddings...")

embeddings = OllamaEmbeddings(
    model="nomic-embed-text",
    base_url="http://localhost:11434"
)

vectorstore = Chroma(
    persist_directory=r"C:\workspace\PEX_Mob\SafeDrive_AI\ml\RAG\chroma_db\dgt_multimodal",
    embedding_function=embeddings,
    collection_name="BOE_dgt_seguridad"
)
retriever = vectorstore.as_retriever(search_kwargs={"k": 3})

class RetrieveRequest(BaseModel):
    query: str

@app.post("/api/v1/retrieve")
async def retrieve_context(request: RetrieveRequest):
    print(f"\n[INFO] BUSCANDO EN CHROMADB: '{request.query}'")

    docs = retriever.invoke(request.query)

    print(f"[INFO] Chunks recuperados: {len(docs)}")

    for i, doc in enumerate(docs):
        origen = doc.metadata.get("source", "Desconocido")
        print(f"   [Chunk {i+1}] -> Origen: {origen} | Texto: {doc.page_content[:60].strip()}...")

    contexto_puro = "\n\n".join([doc.page_content for doc in docs])
    return {"context": contexto_puro}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)