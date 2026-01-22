package com.finanzasproactivas.data.model

import java.util.Date

data class Movimiento(
    val id: String = "",
    val fecha: Date,
    val tipo: TipoMovimiento,
    val categoria: String,
    val concepto: String,
    val importe: Double,
    val frecuencia: Frecuencia,
    val impactoMensual: Double,
    val esConjunto: Boolean = false
)

enum class TipoMovimiento {
    INGRESO, GASTO
}

enum class Frecuencia {
    PUNTUAL, MENSUAL, ANUAL
}
