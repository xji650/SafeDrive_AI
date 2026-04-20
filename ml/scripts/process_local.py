import pandas as pd
import numpy as np
import os

# 1. Rutas de archivos
# El 'raw' es lo que exportas de tu móvil. El 'source' es lo que lee la batidora.
input_path = 'ml/data/raw/mis_datos_room.csv'
output_path = 'ml/data/processed/source/local_room_data.csv'

def procesar_datos_locales():
    print(">>> Iniciando procesado de datos locales...")

    # Comprobamos si el archivo existe
    if not os.path.exists(input_path):
        print(f"INFO: No se encontró {input_path}. Saltando procesado local.")
        return

    # 2. Cargar datos de Room
    df_raw = pd.read_csv(input_path)

    # 3. Transformación al estándar SafeDrive
    # Aquí mapeamos los nombres que uses en Android (ej. 'gforce') a nuestro estándar
    df_local = pd.DataFrame()

    # Asumimos que en Room guardas estos nombres (ajústalos si cambian en tu código Kotlin)
    df_local['Peak_G'] = df_raw['max_g_force']
    df_local['Velocidad'] = df_raw['speed_kmh']
    df_local['Firma_Acustica'] = df_raw['audio_level']

    # Si no guardas Jerk o Giroscopio aún, los estimamos para no dejar huecos
    if 'jerk' in df_raw.columns:
        df_local['Jerk'] = df_raw['jerk']
    else:
        df_local['Jerk'] = df_local['Peak_G'] * 0.7 # Estimación física

    if 'gyro_magnitude' in df_raw.columns:
        df_local['Cambio_Angular'] = df_raw['gyro_magnitude']
    else:
        df_local['Cambio_Angular'] = df_local['Peak_G'] * 0.1

    # 4. El Etiquetado basado en Feedback (La Verdad Absoluta)
    # Suponiendo que en Room tienes una columna 'user_confirmation'
    # 0: No fue nada, 1: Fue un susto, 2: Fue un accidente
    if 'user_confirmation' in df_raw.columns:
        df_local['Label'] = df_raw['user_confirmation']
    else:
        # Si no hay feedback, usamos la lógica matemática que comentabas
        def logica_matematica(row):
            if row['Peak_G'] > 4.0: return 2
            if row['Peak_G'] > 1.5: return 1
            return 0
        df_local['Label'] = df_local.apply(logica_matematica, axis=1)

    # 5. Guardar en la 'despensa' para la batidora
    os.makedirs('ml/data/processed/source', exist_ok=True)
    df_local.to_csv(output_path, index=False)
    print(f">>> Éxito: {len(df_local)} registros locales listos en {output_path}")

if __name__ == "__main__":
    procesar_datos_locales()