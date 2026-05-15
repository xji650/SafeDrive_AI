package com.example.safedriveai.domain.usecases

import javax.inject.Inject

/**
 * Escudo Heurístico: Valida si los datos físicos justifican ejecutar la IA.
 * Previene el drenaje de batería y falsos positivos por caídas de móvil.
 */
class ShouldTriggerDiagnosticUC @Inject constructor() {

    operator fun invoke(gForce: Float, speedKmh: Float, audioDb: Float): Boolean {
        // 1. Filtro de movilidad: Si el coche está parado o casi parado (< 15 km/h),
        // cualquier impacto es probablemente el móvil cayéndose o un portazo.
        if (speedKmh < 15.0f) return false

        // 2. Filtro de intensidad: Umbral de despliegue de seguridad (3.5G).
        // Los frenazos más bruscos rara vez superan los 1.5G.
        if (gForce < 3.5f) return false

        // 3. Filtro acústico: Un impacto real a velocidad genera estruendo (> 85dB).
        if (audioDb < 85.0f) return false

        // Si cumple las 3 condiciones simultáneas, es un evento sospechoso real.
        return true
    }
}
