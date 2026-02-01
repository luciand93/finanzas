package com.finanzasproactivas.data.model

data class Presupuesto(
    val categoria: String,
    val limite: Double,
    val mes: Int, // 0-11 (enero-diciembre)
    val año: Int
)
