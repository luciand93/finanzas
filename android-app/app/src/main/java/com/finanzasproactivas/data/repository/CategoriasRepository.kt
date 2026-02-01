package com.finanzasproactivas.data.repository

import android.content.Context
import android.content.SharedPreferences

class CategoriasRepository(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "finanzas_prefs"
        private const val KEY_CATEGORIAS = "categorias"
        private val CATEGORIAS_DEFAULT = listOf("Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros")
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun obtenerCategorias(): List<String> {
        val categoriasJson = prefs.getString(KEY_CATEGORIAS, null)
        return if (categoriasJson != null && categoriasJson.isNotEmpty()) {
            categoriasJson.split(",").filter { it.isNotEmpty() }
        } else {
            CATEGORIAS_DEFAULT
        }
    }
    
    fun guardarCategorias(categorias: List<String>) {
        val categoriasJson = categorias.joinToString(",")
        prefs.edit().putString(KEY_CATEGORIAS, categoriasJson).apply()
    }
    
    fun agregarCategoria(categoria: String) {
        val categorias = obtenerCategorias().toMutableList()
        if (!categorias.contains(categoria)) {
            categorias.add(categoria)
            guardarCategorias(categorias)
        }
    }
    
    fun eliminarCategoria(categoria: String) {
        val categorias = obtenerCategorias().toMutableList()
        categorias.remove(categoria)
        guardarCategorias(categorias)
    }
    
    fun resetearCategorias() {
        guardarCategorias(CATEGORIAS_DEFAULT)
    }
}
