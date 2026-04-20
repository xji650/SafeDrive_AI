import pandas as pd
import numpy as np
import os

print("Iniciando procesamiento del dataset de Kaggle (Versión Optimizada)...")

# 1. Definir rutas (Arquitectura Final)
input_path = 'ml/data/raw/road_accident_imu_dataset_8000.csv'
output_path = 'ml/data/processed/source/kaggle_data.csv'

if not os.path.exists(input_path):
    print(f"ERROR: No se encuentra el archivo en {input_path}")
    print("Asegúrate de que el dataset original está guardado ahí.")
    exit()

# 2. Cargar los datos crudos
df_raw = pd.read_csv(input_path)

# 3. EXTRACCIÓN DE CARACTERÍSTICAS (Ajustado a la realidad física)

# A. Calcular Peak_G usando la Intensidad ya calculada y restando la gravedad (1.0G)
df_raw['Peak_G'] = abs((df_raw['Motion_Intensity'] / 9.8) - 1.0)

# B. Calcular Jerk (Latigazo)
df_raw['Jerk'] = df_raw['Peak_G'].diff().abs()
df_raw['Jerk'] = df_raw['Jerk'].fillna(0)

# C. Calcular Cambio Angular
df_raw['Cambio_Angular'] = np.sqrt(df_raw['Gyro_X']**2 + df_raw['Gyro_Y']**2 + df_raw['Gyro_Z']**2)

# D. Velocidad directa
df_raw['Velocidad'] = df_raw['Speed_kmh']

# E. Generación Sintética de la Firma Acústica
# Como descubrimos que los "accidentes" de Kaggle son en realidad sustos leves (0.4G),
# ajustamos el ruido para que sea el de un frenazo (0.1 a 0.4), no el de un choque mortal.
np.random.seed(42)
df_raw['Firma_Acustica'] = np.where(
    df_raw['Crash_Label'] == 1,
    np.random.uniform(0.1, 0.4, len(df_raw)),  # Ruido de derrape/susto
    np.random.uniform(0.0, 0.1, len(df_raw))   # Ruido normal de conducción
)

# 4. RE-ETIQUETADO INTELIGENTE (Mitigación de Falsos Positivos)
def mapear_etiquetas(row):
    # En Kaggle, los impactos son de baja energía (~0.47G), por lo que son "Sustos" (Label 1)
    if row['Crash_Label'] == 1:
        return 1
    else:
        # Por si hay algún frenazo no etiquetado en Kaggle que supere 1.0G
        if row['Peak_G'] > 1.0:
            return 1
        else:
            return 0 # Conducción verdaderamente normal

df_raw['Label'] = df_raw.apply(mapear_etiquetas, axis=1)

# 5. CONSOLIDACIÓN DEL DATASET FINAL
df_processed = df_raw[['Peak_G', 'Jerk', 'Firma_Acustica', 'Cambio_Angular', 'Velocidad', 'Label']]

# 6. Guardar en la "Despensa" (Source)
os.makedirs('ml/data/processed/source', exist_ok=True)
df_processed.to_csv(output_path, index=False)

print(f"¡Éxito! Dataset de Kaggle procesado: {len(df_processed)} filas.")
print(f"Guardado en: {output_path}")