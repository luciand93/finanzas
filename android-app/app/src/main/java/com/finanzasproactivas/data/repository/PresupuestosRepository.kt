package com.finanzasproactivas.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.finanzasproactivas.data.model.Presupuesto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class PresupuestosRepository(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "finanzas_prefs"
        private const val KEY_PRESUPUESTOS = "presupuestos"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun obtenerPresupuestos(mes: Int, año: Int): List<Presupuesto> {
        val presupuestosJson = prefs.getString(KEY_PRESUPUESTOS, null)
        return if (presupuestosJson != null && presupuestosJson.isNotEmpty()) {
            val type = object : TypeToken<List<Presupuesto>>() {}.type
            val todos = gson.fromJson<List<Presupuesto>>(presupuestosJson, type)
            todos.filter { it.mes == mes && it.año == año }
        } else {
            emptyList()
        }
    }
    
    fun obtenerTodosLosPresupuestos(): List<Presupuesto> {
        val presupuestosJson = prefs.getString(KEY_PRESUPUESTOS, null)
        return if (presupuestosJson != null && presupuestosJson.isNotEmpty()) {
            val type = object : TypeToken<List<Presupuesto>>() {}.type
            gson.fromJson<List<Presupuesto>>(presupuestosJson, type)
        } else {
            emptyList()
        }
    }
    
    fun guardarPresupuesto(presupuesto: Presupuesto) {
        val todos = obtenerTodosLosPresupuestos().toMutableList()
        // Eliminar presupuesto existente para la misma categoría, mes y año
        todos.removeAll { 
            it.categoria == presupuesto.categoria && 
            it.mes == presupuesto.mes && 
            it.año == presupuesto.año 
        }
        todos.add(presupuesto)
        val json = gson.toJson(todos)
        prefs.edit().putString(KEY_PRESUPUESTOS, json).apply()
    }
    
    fun eliminarPresupuesto(categoria: String, mes: Int, año: Int) {
        val todos = obtenerTodosLosPresupuestos().toMutableList()
        todos.removeAll { 
            it.categoria == categoria && 
            it.mes == mes && 
            it.año == año 
        }
        val json = gson.toJson(todos)
        prefs.edit().putString(KEY_PRESUPUESTOS, json).apply()
    }
}
