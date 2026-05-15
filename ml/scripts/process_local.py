import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import pandas as pd

# 1. Conectar con tu Firebase (Reemplaza con tu archivo de credenciales)
cred = credentials.Certificate("ruta/a/tus/credenciales-firebase.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

print("Descargando feedback de usuarios...")

# 2. Buscar TODOS los accidentes en todos los vehículos
# Usamos collection_group para buscar en todos los "vehiculos" a la vez
incidentes = db.collection_group('accidentes').stream()

datos_reales = []

# 3. Transformar cada JSON independiente en una fila de nuestro Dataset
for doc in incidentes:
    dato = doc.to_dict()

    # Solo cogemos los que tienen 'type' (feedback confirmado)
    if 'type' in dato:
        datos_reales.append({
            'Peak_G': dato.get('maxGForce', 0.0),
            'Jerk': dato.get('jerkAtImpact', 0.0),
            'Firma_Acustica': dato.get('amplitudeMicrophone', 0.0),
            'Cambio_Angular': dato.get('angleAtImpact', 0.0),
            'Velocidad': dato.get('speedAtImpact', 0.0),
            'Label': dato.get('type')  # 0: Normal, 1: Susto, 2: Choque
        })

# 4. Convertir a DataFrame y guardar como CSV
df_real = pd.DataFrame(datos_reales)

# Limpiar posibles nulos
df_real = df_real.dropna()

df_real.to_csv('datos_reales_feedback.csv', index=False)

print(f"¡Éxito! Se ha creado el dataset con {len(df_real)} incidentes reales.")
print(df_real.head())