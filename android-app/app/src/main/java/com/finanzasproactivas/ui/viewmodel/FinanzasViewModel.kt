package com.finanzasproactivas.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.finanzasproactivas.data.model.Movimiento
import com.finanzasproactivas.data.model.Frecuencia
import com.finanzasproactivas.data.model.Presupuesto
import com.finanzasproactivas.data.repository.SupabaseRepository
import com.finanzasproactivas.data.repository.PresupuestosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

enum class PeriodoEstadisticas {
    MES_ACTUAL,
    GENERAL
}

class FinanzasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SupabaseRepository()
    private val presupuestosRepo = PresupuestosRepository(application)
    
    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _periodoActual = MutableStateFlow(PeriodoEstadisticas.MES_ACTUAL)
    val periodoActual: StateFlow<PeriodoEstadisticas> = _periodoActual.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    init {
        cargarMovimientos()
        verificarYGenerarMovimientosMensuales()
    }
    
    fun cambiarPeriodo(periodo: PeriodoEstadisticas) {
        _periodoActual.value = periodo
    }
    
    fun cargarMovimientos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val datos = repository.obtenerMovimientos()
                _movimientos.value = datos
            } catch (e: Exception) {
                _error.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun guardarMovimiento(movimiento: Movimiento) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val exito = repository.guardarMovimiento(movimiento)
                if (exito) {
                    cargarMovimientos() // Recargar después de guardar
                } else {
                    _error.value = "Error al guardar el movimiento"
                }
            } catch (e: Exception) {
                _error.value = "Error al guardar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun actualizarMovimiento(movimiento: Movimiento) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val exito = repository.actualizarMovimiento(movimiento)
                if (exito) {
                    // Recargar después de actualizar para recalcular todo
                    cargarMovimientos()
                } else {
                    _error.value = "Error al actualizar el movimiento"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun eliminarMovimiento(movimiento: Movimiento) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val exito = repository.eliminarMovimiento(movimiento)
                if (exito) {
                    cargarMovimientos() // Recargar después de eliminar
                } else {
                    _error.value = "Error al eliminar el movimiento"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Obtener movimientos filtrados según el período seleccionado
    private fun getMovimientosFiltrados(): List<Movimiento> {
        return when (_periodoActual.value) {
            PeriodoEstadisticas.MES_ACTUAL -> _movimientos.value.filter { esDelMesActual(it.fecha) }
            PeriodoEstadisticas.GENERAL -> _movimientos.value
        }
    }
    
    // Métricas calculadas basadas en el período seleccionado
    val ingresosDelPeriodo: Double
        get() = getMovimientosFiltrados()
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO }
            .sumOf { it.importe }
    
    val gastosDelPeriodo: Double
        get() = getMovimientosFiltrados()
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
            .sumOf { it.importe }
    
    val capacidadAhorro: Double
        get() = ingresosDelPeriodo - gastosDelPeriodo
    
    // Tasa de ahorro (%)
    val tasaAhorro: Double
        get() = if (ingresosDelPeriodo > 0) {
            (capacidadAhorro / ingresosDelPeriodo * 100)
        } else 0.0
    
    val gastoPromedio: Double
        get() {
            val gastos = getMovimientosFiltrados()
                .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
                .map { it.importe }
            return if (gastos.isNotEmpty()) gastos.average() else 0.0
        }
    
    // Gasto promedio diario (solo para mes actual)
    val gastoPromedioDiario: Double
        get() = if (_periodoActual.value == PeriodoEstadisticas.MES_ACTUAL) {
            val ahora = Calendar.getInstance()
            val diaDelMes = ahora.get(Calendar.DAY_OF_MONTH)
            if (diaDelMes > 0) gastosDelPeriodo / diaDelMes else 0.0
        } else {
            gastosDelPeriodo / 30 // Promedio general
        }
    
    // Proyección de gasto fin de mes
    val proyeccionGastoFinMes: Double
        get() = if (_periodoActual.value == PeriodoEstadisticas.MES_ACTUAL) {
            val ahora = Calendar.getInstance()
            val diasTotales = ahora.getActualMaximum(Calendar.DAY_OF_MONTH)
            gastoPromedioDiario * diasTotales
        } else {
            gastosDelPeriodo
        }
    
    // Gastos fijos (mensuales y anuales)
    val gastosFijos: Double
        get() = getMovimientosFiltrados()
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
            .filter { it.frecuencia == Frecuencia.MENSUAL || it.frecuencia == Frecuencia.ANUAL }
            .sumOf { 
                if (it.frecuencia == Frecuencia.ANUAL) it.importe / 12 else it.importe
            }
    
    // Gastos variables
    val gastosVariables: Double
        get() = gastosDelPeriodo - gastosFijos
    
    // Ahorro necesario mensual para cubrir gastos anuales
    val ahorroNecesarioMensual: Double
        get() {
            val gastosAnuales = _movimientos.value
                .filter { it.frecuencia == Frecuencia.ANUAL }
                .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
                .sumOf { it.importe }
            return gastosAnuales / 12.0
        }
    
    // Salud financiera (0-100)
    val saludFinanciera: Int
        get() {
            var salud = 50
            
            // Basado en tasa de ahorro
            when {
                tasaAhorro > 20 -> salud += 30
                tasaAhorro > 10 -> salud += 20
                tasaAhorro > 0 -> salud += 10
                else -> salud -= 20
            }
            
            // Basado en gastos fijos vs ingresos
            if (ingresosDelPeriodo > 0 && gastosFijos / ingresosDelPeriodo < 0.5) {
                salud += 10
            }
            
            // Penalización si gastos > ingresos
            if (capacidadAhorro < 0) {
                salud -= 20
            }
            
            return salud.coerceIn(0, 100)
        }
    
    // Top categorías de gastos
    fun getTopCategoriasGastos(limit: Int = 5): List<Pair<String, Double>> {
        return getMovimientosFiltrados()
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
            .groupBy { it.categoria }
            .mapValues { it.value.sumOf { m -> m.importe } }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
    }
    
    // Días restantes del mes
    val diasRestantesMes: Int
        get() = if (_periodoActual.value == PeriodoEstadisticas.MES_ACTUAL) {
            val ahora = Calendar.getInstance()
            val diasTotales = ahora.getActualMaximum(Calendar.DAY_OF_MONTH)
            val diaActual = ahora.get(Calendar.DAY_OF_MONTH)
            diasTotales - diaActual
        } else 0
    
    // Métodos legacy (mantener compatibilidad)
    val ingresosDelMes: Double
        get() = _movimientos.value
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.INGRESO }
            .filter { esDelMesActual(it.fecha) }
            .sumOf { it.importe }
    
    val gastosDelMes: Double
        get() = _movimientos.value
            .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
            .filter { esDelMesActual(it.fecha) }
            .sumOf { it.importe }
    
    // Presupuestos del mes actual
    fun obtenerPresupuestosDelMes(): List<Presupuesto> {
        val ahora = Calendar.getInstance()
        return presupuestosRepo.obtenerPresupuestos(
            ahora.get(Calendar.MONTH),
            ahora.get(Calendar.YEAR)
        )
    }
    
    // Presupuestos cerca del límite (más del 80%)
    fun obtenerPresupuestosCercaDelLimite(): List<Pair<Presupuesto, Double>> {
        val presupuestos = obtenerPresupuestosDelMes()
        val ahora = Calendar.getInstance()
        val mes = ahora.get(Calendar.MONTH)
        val año = ahora.get(Calendar.YEAR)
        
        return presupuestos.mapNotNull { presupuesto ->
            val gastoActual = _movimientos.value
                .filter { it.tipo == com.finanzasproactivas.data.model.TipoMovimiento.GASTO }
                .filter { it.categoria == presupuesto.categoria }
                .filter { esDelMes(it.fecha, mes, año) }
                .sumOf { it.importe }
            
            val porcentaje = (gastoActual / presupuesto.limite * 100).coerceAtMost(100.0)
            if (porcentaje >= 80.0) {
                Pair(presupuesto, porcentaje)
            } else null
        }
    }
    
    fun guardarPresupuesto(presupuesto: Presupuesto) {
        presupuestosRepo.guardarPresupuesto(presupuesto)
    }
    
    fun eliminarPresupuesto(categoria: String, mes: Int, año: Int) {
        presupuestosRepo.eliminarPresupuesto(categoria, mes, año)
    }
    
    // Generar movimientos mensuales para un mes específico
    fun generarMovimientosMensuales(mes: Int, año: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val movimientosMensuales = _movimientos.value
                    .filter { it.frecuencia == Frecuencia.MENSUAL }
                
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, año)
                    set(Calendar.MONTH, mes)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                
                // Verificar si ya existen movimientos para ese mes
                val movimientosExistentes = _movimientos.value.filter { movimiento ->
                    val cal = Calendar.getInstance().apply { time = movimiento.fecha }
                    cal.get(Calendar.YEAR) == año && 
                    cal.get(Calendar.MONTH) == mes &&
                    movimiento.frecuencia == Frecuencia.MENSUAL
                }
                
                if (movimientosExistentes.isEmpty()) {
                    // Generar movimientos para cada movimiento mensual
                    movimientosMensuales.forEach { movimientoBase ->
                        val nuevoMovimiento = Movimiento(
                            id = UUID.randomUUID().toString(),
                            fecha = calendar.time,
                            tipo = movimientoBase.tipo,
                            categoria = movimientoBase.categoria,
                            concepto = movimientoBase.concepto,
                            importe = movimientoBase.importe,
                            frecuencia = movimientoBase.frecuencia,
                            impactoMensual = movimientoBase.impactoMensual,
                            esConjunto = movimientoBase.esConjunto
                        )
                        repository.guardarMovimiento(nuevoMovimiento)
                    }
                    cargarMovimientos() // Recargar después de generar
                }
            } catch (e: Exception) {
                _error.value = "Error al generar movimientos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Verificar y generar movimientos mensuales automáticamente
    private fun verificarYGenerarMovimientosMensuales() {
        viewModelScope.launch {
            val ahora = Calendar.getInstance()
            val mesActual = ahora.get(Calendar.MONTH)
            val añoActual = ahora.get(Calendar.YEAR)
            
            // Verificar si ya se generaron movimientos para este mes
            val movimientosDelMes = _movimientos.value.filter { movimiento ->
                val cal = Calendar.getInstance().apply { time = movimiento.fecha }
                cal.get(Calendar.YEAR) == añoActual && 
                cal.get(Calendar.MONTH) == mesActual &&
                movimiento.frecuencia == Frecuencia.MENSUAL
            }
            
            // Si no hay movimientos mensuales para este mes, generarlos
            if (movimientosDelMes.isEmpty()) {
                generarMovimientosMensuales(mesActual, añoActual)
            }
        }
    }
    
    private fun esDelMes(fecha: Date, mes: Int, año: Int): Boolean {
        val cal = Calendar.getInstance().apply { time = fecha }
        return cal.get(Calendar.YEAR) == año && cal.get(Calendar.MONTH) == mes
    }
    
    private fun esDelMesActual(fecha: Date): Boolean {
        val ahora = Calendar.getInstance()
        val fechaMovimiento = Calendar.getInstance().apply { time = fecha }
        return ahora.get(Calendar.YEAR) == fechaMovimiento.get(Calendar.YEAR) &&
                ahora.get(Calendar.MONTH) == fechaMovimiento.get(Calendar.MONTH)
    }
}
