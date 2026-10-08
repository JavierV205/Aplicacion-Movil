package com.example.moviluz.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de datos para representar una medición de intensidad de luz de un nodo IoT.
 *
 * Firebase Firestore requiere un constructor sin argumentos para la deserialización;
 * en Kotlin, al asignar valores por defecto a todos los atributos de un data class,
 * el compilador genera automáticamente el constructor vacío necesario.
 */
data class LightMeasurement(
    // Anotación DocumentId: Firestore inyecta automáticamente el ID del documento al leerlo
    @DocumentId
    val id: String? = null,

    // Identificador del dispositivo/sensor IoT (ej. "ESP32_SALA_01")
    val deviceId: String = "",

    // Campo numérico: nivel o intensidad de luz medida (ej. en Lux o porcentaje 0 - 100)
    val lightIntensity: Double = 0.0,

    // Ubicación física de la medición (ej. "Invernadero", "Oficina")
    val location: String = "",

    // Marca de tiempo del servidor al momento de registrar la lectura
    @ServerTimestamp
    val timestamp: Date? = null
)
