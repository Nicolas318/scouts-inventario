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
    // Lista de reparaciones aplicables (Tienda, Chupetes, Vientos)
    var reparaciones: List<String>? = null,
    var marks: List<Map<String, Double>>? = null
)
