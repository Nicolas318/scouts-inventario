package com.example.inventariotiendas

import com.google.firebase.Timestamp

/**
 * Modelo de datos para una tienda de campaña
 */
data class Tienda(
    var id: String = "",
    var tipo_tienda: String = "",
    var nombre_tienda: String = "",
    var num_piquetas: Int = 0,
    var num_gomas: Int = 0,
    var estado: String = "",
    var fecha_revision: Timestamp = Timestamp.now(),
    var reparaciones: List<String>? = null,
    var marks: List<Map<String, Any>>? = null,   // <-- Cambiado a Any
    var grupo: String = ""  // ← NUEVO CAMPO
)
