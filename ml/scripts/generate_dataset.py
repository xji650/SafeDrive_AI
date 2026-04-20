import os
import pandas as pd
import numpy as np

# 1. Crear la estructura de carpetas (Alineado con tu imagen)
folders = [
    'ml/data/processed/source',
    'ml/data/processed/dataset/wrong_data'
]
for folder in folders:
    os.makedirs(folder, exist_ok=True)

# 2. Función para generar datos realistas basados en la física
def generar_datos(n_samples, label):
    # ... (Tu código de física está perfecto, lo mantienes igual) ...
    if label == 0:
        peak_g = np.random.uniform(0.1, 1.5, n_samples)
        jerk = np.random.uniform(0.1, 0.8, n_samples)
        audio = np.random.uniform(0.0, 0.2, n_samples)
        angular = np.random.uniform(0.0, 0.3, n_samples)
        velocidad = np.random.uniform(10, 120, n_samples)
    elif label == 1:
        peak_g = np.random.uniform(1.5, 4.0, n_samples)
        jerk = np.random.uniform(0.8, 2.5, n_samples)
        audio = np.random.uniform(0.1, 0.4, n_samples)
        angular = np.random.uniform(0.3, 0.8, n_samples)
        velocidad = np.random.uniform(20, 100, n_samples)
    elif label == 2:
        peak_g = np.random.uniform(4.0, 12.0, n_samples)
        jerk = np.random.uniform(2.5, 8.0, n_samples)
        audio = np.random.uniform(0.6, 1.0, n_samples)
        angular = np.random.uniform(0.5, 2.0, n_samples)
        velocidad = np.random.uniform(30, 120, n_samples)

    return pd.DataFrame({
        'Peak_G': peak_g,
        'Jerk': jerk,
        'Firma_Acustica': audio,
        'Cambio_Angular': angular,
        'Velocidad': velocidad,
        'Label': [label] * n_samples
    })

# 3. Generar el Dataset "Bueno"
df_0 = generar_datos(4000, 0)
df_1 = generar_datos(1500, 1)
df_2 = generar_datos(1500, 2)

df_sintetico_completo = pd.concat([df_0, df_1, df_2]).sample(frac=1, random_state=42).reset_index(drop=True)

# 4. Generar el Dataset "Basura"
wrong_data = pd.DataFrame({
    'Peak_G': [500.0, -2.0, 0.0, 999.9],
    'Jerk': [0.0, 900.0, -10.0, 0.0],
    'Firma_Acustica': [2.5, -0.5, 0.0, 10.0],
    'Cambio_Angular': [0.0, 0.0, 0.0, 50.0],
    'Velocidad': [-50, 400, 0, 0],
    'Label': [0, 1, 9, 2]
})

# 5. Guardar los archivos CSV (Rutas corregidas)
df_sintetico_completo.to_csv('ml/data/processed/source/datos_sinteticos.csv', index=False)
wrong_data.to_csv('ml/data/processed/dataset/wrong_data/corrupted_data.csv', index=False)

print(f"¡Ingrediente generado con éxito! Se han guardado {len(df_sintetico_completo)} filas en 'ml/data/processed/source/datos_sinteticos.csv'")