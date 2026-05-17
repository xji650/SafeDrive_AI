import os
import pandas as pd
import numpy as np

# 1. Crear la estructura de carpetas
folders = [
    'ml/data/processed/source',
    'ml/data/processed/dataset/wrong_data'
]
for folder in folders:
    os.makedirs(folder, exist_ok=True)

# 2. Función para generar datos en ESCALA REAL (Coincide con la App Android)
def generar_datos(n_samples, label):
    if label == 0:  # Conducción Normal
        peak_g = np.random.uniform(0.8, 1.2, n_samples)      # 1G es reposo
        jerk = np.random.uniform(0.1, 2.0, n_samples)        # Cambios suaves
        audio = np.random.uniform(30.0, 60.0, n_samples)     # Ruido ambiente (dB)
        angular = np.random.uniform(0.0, 30.0, n_samples)    # Giro leve (º/s)
        velocidad = np.random.uniform(0, 120, n_samples)     # km/h
    elif label == 1:  # Conducción Agresiva / Frenazo
        peak_g = np.random.uniform(1.2, 4.0, n_samples)      # Cerca del umbral de alarma
        jerk = np.random.uniform(2.0, 7.0, n_samples)        # Movimientos bruscos
        audio = np.random.uniform(60.0, 85.0, n_samples)     # Frenazos/revoluciones
        angular = np.random.uniform(30.0, 120.0, n_samples)  # Volantazos bruscos
        velocidad = np.random.uniform(20, 140, n_samples)
    elif label == 2:  # ACCIDENTE (Impacto detectado)
        peak_g = np.random.uniform(4.0, 15.0, n_samples)     # Disparo de EDR (>4G)
        jerk = np.random.uniform(7.0, 20.0, n_samples)       # Impacto violento
        audio = np.random.uniform(90.0, 120.0, n_samples)    # Estruendo de choque
        angular = np.random.uniform(120.0, 500.0, n_samples) # Vuelcos o trompos
        velocidad = np.random.uniform(30, 200, n_samples)

    return pd.DataFrame({
        'Peak_G': peak_g,
        'Jerk': jerk,
        'Firma_Acustica': audio,
        'Cambio_Angular': angular,
        'Velocidad': velocidad,
        'Label': [label] * n_samples
    })

# 3. Generar el Dataset "Bueno" con escalas reales
df_0 = generar_datos(4000, 0)
df_1 = generar_datos(1500, 1)
df_2 = generar_datos(1500, 2)

df_sintetico_completo = pd.concat([df_0, df_1, df_2]).sample(frac=1, random_state=42).reset_index(drop=True)

# 4. Generar el Dataset "Basura" (Outliers extremos)
wrong_data = pd.DataFrame({
    'Peak_G': [500.0, -2.0, 0.0, 999.9],
    'Jerk': [0.0, 900.0, -10.0, 0.0],
    'Firma_Acustica': [200.0, -50.0, 0.0, 1000.0],
    'Cambio_Angular': [1000.0, -100.0, 0.0, 5000.0],
    'Velocidad': [-50, 400, 0, 0],
    'Label': [0, 1, 9, 2]
})

# 5. Guardar los archivos CSV
df_sintetico_completo.to_csv('ml/data/processed/source/datos_sinteticos.csv', index=False)
wrong_data.to_csv('ml/data/processed/dataset/wrong_data/corrupted_data.csv', index=False)

print("¡Dataset actualizado a ESCALA REAL!")
print(f"Rango G: {df_sintetico_completo['Peak_G'].min():.1f} - {df_sintetico_completo['Peak_G'].max():.1f}")
print(f"Rango Audio: {df_sintetico_completo['Firma_Acustica'].min():.1f} - {df_sintetico_completo['Firma_Acustica'].max():.1f} dB")
print(f"Rango Giro: {df_sintetico_completo['Cambio_Angular'].min():.1f} - {df_sintetico_completo['Cambio_Angular'].max():.1f} º/s")
print(f"Archivo guardado en: ml/data/processed/source/datos_sinteticos.csv")
