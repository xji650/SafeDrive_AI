import chromadb

try:
    # Abrimos la base de datos de tu carpeta de forma directa
    client = chromadb.PersistentClient(path=r"C:\workspace\PEX_Mob\SafeDrive_AI\ml\RAG\chroma_db\dgt_multimodal")

    colecciones = client.list_collections()
    print("=== DIAGNÓSTICO DE CHROMADB ===")
    print(f"Colecciones encontradas: {len(colecciones)}")

    if len(colecciones) == 0:
        print(" ALERTA: No hay ninguna colección. La base de datos está VACÍA.")

    for col in colecciones:
        print(f"\n Colección: '{col.name}'")
        print(f"   Número de documentos guardados: {col.count()}")

        # Mostramos los primeros textos si existen
        if col.count() > 0:
            datos = col.peek(limit=1)
            print(f"   Muestra del primer texto: {datos['documents'][0][:100]}...")

except Exception as e:
    print(f" Error al abrir la base de datos: {e}")