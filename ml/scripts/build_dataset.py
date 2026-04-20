import pandas as pd
from sklearn.model_selection import train_test_split
import os

# 1. Crear las carpetas finales
os.makedirs('ml/data/processed/dataset/train', exist_ok=True)
os.makedirs('ml/data/processed/dataset/test', exist_ok=True)
os.makedirs('ml/data/processed/dataset/validation', exist_ok=True)

# 2. LEER LOS INGREDIENTES
# Nota: Asegúrate de tener los otros dos archivos antes de ejecutar esto, o comenta sus líneas.
df_sintetico = pd.read_csv('ml/data/processed/source/datos_sinteticos.csv')
df_kaggle = pd.read_csv('ml/data/processed/source/kaggle_data.csv')
#df_room = pd.read_csv('ml/data/processed/source/mis_datos_room.csv')

# 3. LA BATIDORA (Ajustado a los archivos que tengas)
dataset_completo = pd.concat([df_sintetico], ignore_index=True) # Añade df_kaggle y df_room cuando los tengas

# 4. MEZCLAR
dataset_completo = dataset_completo.sample(frac=1, random_state=42).reset_index(drop=True)

# 5. REPARTIR LOS TROZOS
train, temp = train_test_split(dataset_completo, test_size=0.3, random_state=42)
val, test = train_test_split(temp, test_size=0.5, random_state=42)

# 6. GUARDAR EL PLATO FINAL
train.to_csv('ml/data/processed/dataset/train/train.csv', index=False)
val.to_csv('ml/data/processed/dataset/validation/validation.csv', index=False)
test.to_csv('ml/data/processed/dataset/test/test.csv', index=False)

print(f"¡Fusión completada! Datos totales: {len(dataset_completo)} filas.")