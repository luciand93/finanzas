import streamlit as st
import pandas as pd
import plotly.express as px
from datetime import datetime
import os
import locale

# --- CONFIGURACIÓN REGIONAL ---
try:
    locale.setlocale(locale.LC_ALL, 'es_ES.UTF-8')
except locale.Error:
    try:
        locale.setlocale(locale.LC_ALL, 'es_ES')
    except locale.Error:
        pass

# --- CONFIGURACIÓN PÁGINA ---
st.set_page_config(
    page_title="Finanzas Proactivas €", 
    layout="wide", 
    page_icon="💶",
    initial_sidebar_state="expanded"
)

# --- ESTADO DE SESIÓN (Para la simulación) ---
if 'simulacion' not in st.session_state:
    st.session_state.simulacion = None # Aquí guardaremos el gasto hipotético

# --- ARCHIVOS ---
FILE_NAME = "finanzas.csv"
CAT_FILE_NAME = "categorias.csv"
COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual"]

# --- FUNCIONES ---
def load_data():
    if os.path.exists(FILE_NAME):
        try:
            df = pd.read_csv(FILE_NAME)
            df['Fecha'] = pd.to_datetime(df['Fecha'], dayfirst=True, errors='coerce')
            df = df.dropna(subset=['Fecha'])
            return df
        except Exception:
            return pd.DataFrame(columns=COLUMNS)
    return pd.DataFrame(columns=COLUMNS)

def save_all_data(df):
    df_to_save = df.copy()
    df_to_save['Fecha'] = df_to_save['Fecha'].dt.strftime("%d/%m/%Y")
    df_to_save.to_csv(FILE_NAME, index=False)

def load_categories():
    default_cats = ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"]
    if os.path.exists(CAT_FILE_NAME):
        try:
            df_cat = pd.read_csv(CAT_FILE_NAME)
            if not df_cat.empty: return df_cat['Categoría'].tolist()
        except: pass
    return default_cats

def save_categories(lista):
    lista = list(dict.fromkeys(lista)) 
    pd.DataFrame({"Categoría": lista}).to_csv(CAT_FILE_NAME, index=False)

def translate_period(period_str):
    d = datetime.strptime(period_str, "%Y-%m")
    try: return d.strftime("%B %Y").capitalize()
    except: return period_str

# --- CARGA DATOS ---
df = load_data()
lista_cats = load_categories()

# --- SIDEBAR: REGISTRO Y SIMULACIÓN ---
st.sidebar.header("📝 Gestión de Movimientos")

# INTERRUPTOR DE MODO SIMULACIÓN
modo_simulacion = st.sidebar.checkbox("🧪 Modo Simulación (¿Puedo permitírmelo?)", 
                                      help="Actívalo para probar un gasto sin guardarlo en la base de datos.")

style_color = "red" if modo_simulacion else "green"
st.sidebar.markdown(f":{style_color}[**ESTADO: {'SIMULANDO ESCENARIO' if modo_simulacion else 'REGISTRO REAL'}**]")

with st.sidebar.form("form_reg", clear_on_submit=not modo_simulacion):
    tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=1, horizontal=True)
    fecha = st.date_input("Fecha", datetime.now(), format="DD/MM/YYYY")
    cat = st.selectbox("Categoría", lista_cats)
    con = st.text_input("Concepto", placeholder="Ej: Nuevo iPhone, Viaje...")
    imp = st.number_input("Importe (€)", min_value=0.0, step=10.0, format="%.2f")
    fre = st.selectbox("Frecuencia", ["Mensual", "Anual", "Puntual"])
    
    # Texto del botón cambia según el modo
    btn_text = "🧪 Simular Impacto" if modo_simulacion else "💾 Guardar Definitivamente"
    submit = st.form_submit_button(btn_text, use_container_width=True)
    
    if submit:
        if imp > 0 and con:
            impacto = imp / 12 if fre == "Anual" else imp
            
            if modo_simulacion:
                # GUARDAMOS EN SESIÓN, NO EN CSV
                st.session_state.simulacion = {
                    "Concepto": con, "Importe": imp, "Impacto_Mensual": impacto, "Tipo": tipo
                }
                st.success("Simulación calculada. Ve a la pestaña '🤖 Asesor' para ver el resultado.")
            else:
                # GUARDADO REAL
                new_row = pd.DataFrame([[pd.to_datetime(fecha), tipo, cat, con, imp, fre, impacto]], columns=COLUMNS)
                df = pd.concat([df, new_row], ignore_index=True)
                save_all_data(df)
                st.session_state.simulacion = None # Limpiar simulación si guardamos algo real
                st.success("¡Guardado!")
                st.rerun()
        else:
            st.error("Faltan datos.")

# --- DASHBOARD ---
st.title("🚀 Finanzas Personales (€)")

if df.empty:
    st.info("Añade movimientos en el menú lateral.")
else:
    # CÁLCULOS GLOBALES (REALES)
    m, y = datetime.now().month, datetime.now().year
    df_mes = df[(df['Fecha'].dt.month == m) & (df['Fecha'].dt.year == y)]
    
    ingresos_reales = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
    gastos_reales = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
    
    # Prorrateo Mensual Promedio (Based on all history)
    n_meses = max(len(df['Fecha'].dt.to_period('M').unique()), 1)
    gasto_pro_real = df[df['Tipo'] == "Gasto"]['Impacto_Mensual'].sum() / n_meses

    # PESTAÑAS
    tab_asesor, tab_resumen, tab_historial, tab_edit, tab_conf = st.tabs(
        ["🤖 Asesor & Simulación", "📊 Resumen", "🔍 Historial", "📝 Editar", "⚙️ Config"]
    )

    # --- TAB 1: EL ASESOR (RECUPERADO Y POTENCIADO) ---
    with tab_asesor:
        st.subheader("🧠 Análisis de Salud Financiera")
        
        col_real, col_sim = st.columns(2)
        
        # --- COLUMNA 1: SITUACIÓN REAL ---
        with col_real:
            st.markdown("### Situación Actual")
            ahorro_real = ingresos_reales - gasto_pro_real
            capacidad_ahorro = (ahorro_real / ingresos_reales) if ingresos_reales > 0 else 0
            
            st.metric("Ingresos (Mes)", f"{ingresos_reales:,.2f} €")
            st.metric("Gasto Promedio Real", f"{gasto_pro_real:,.2f} €")
            st.metric("Ahorro Potencial", f"{ahorro_real:,.2f} €", 
                      delta=f"{capacidad_ahorro:.1%}", delta_color="normal")
            
            if capacidad_ahorro < 0.1:
                st.warning("⚠️ Tu capacidad de ahorro actual es baja (<10%).")
            else:
                st.success("✅ Tu salud financiera actual es buena.")

        # --- COLUMNA 2: SIMULACIÓN ---
        with col_sim:
            st.markdown("### Escenario Simulado")
            
            sim = st.session_state.simulacion
            if sim:
                st.info(f"Simulando: **{sim['Concepto']}** de **{sim['Importe']:,.2f} €**")
                
                # Calculamos el NUEVO gasto prorrateado hipotético
                # Asumimos que este gasto se suma al promedio mensual existente
                nuevo_gasto_pro = gasto_pro_real + sim['Impacto_Mensual'] if sim['Tipo'] == "Gasto" else gasto_pro_real
                nuevo_ahorro = ingresos_reales - nuevo_gasto_pro
                nueva_capacidad = (nuevo_ahorro / ingresos_reales) if ingresos_reales > 0 else 0
                
                # Métricas Comparativas
                st.metric("Nuevo Gasto Promedio", f"{nuevo_gasto_pro:,.2f} €", 
                          delta=f"-{sim['Impacto_Mensual']:,.2f} €", delta_color="inverse")
                
                st.metric("Nuevo Ahorro", f"{nuevo_ahorro:,.2f} €",
                          delta=f"{(nueva_capacidad - capacidad_ahorro):.1%}", delta_color="normal")

                st.markdown("---")
                # VEREDICTO DE LA IA
                if nueva_capacidad < 0:
                    st.error("⛔ **NO RECOMENDADO:** Entrarías en déficit mensual.")
                elif nueva_capacidad < 0.10:
                    st.warning("⚠️ **PRECAUCIÓN:** Tu ahorro bajaría a niveles peligrosos (<10%).")
                elif (capacidad_ahorro - nueva_capacidad) > 0.20:
                    st.warning("📉 **IMPACTO ALTO:** Es un gasto asumible, pero reduce drásticamente tu ahorro.")
                else:
                    st.success("🟢 **ADELANTE:** Puedes permitírtelo manteniendo una economía sana.")
                    
                if st.button("🗑️ Borrar Simulación"):
                    st.session_state.simulacion = None
                    st.rerun()
            else:
                st.markdown("""
                *No hay simulación activa.*
                
                1. Ve a la barra lateral.
                2. Activa **"Modo Simulación"**.
                3. Introduce un gasto (ej. Coche nuevo, Vacaciones).
                4. Pulsa "Simular" para ver aquí el veredicto.
                """)

    # --- TAB 2: RESUMEN (GRÁFICOS) ---
    with tab_resumen:
        c1, c2 = st.columns(2)
        c1.metric("Ingresos Totales (Mes)", f"{ingresos_reales:,.2f} €")
        c2.metric("Salidas de Caja (Mes)", f"{gastos_reales:,.2f} €")
        
        st.subheader("Evolución Real")
        df_ev = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
        df_ev['Mes'] = df_ev['Fecha'].astype(str).apply(translate_period)
        df_ev = df_ev.sort_values("Fecha")
        
        fig = px.bar(df_ev, x='Mes', y='Importe', color='Tipo', barmode='group',
                     color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'})
        fig.update_layout(legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1))
        st.plotly_chart(fig, use_container_width=True)

    # --- RESTO DE TABS (HISTORIAL, EDITAR, CONFIG) ---
    with tab_historial:
        st.dataframe(df.sort_values("Fecha", ascending=False), use_container_width=True)

    with tab_edit:
        st.write("Modifica los registros guardados:")
        edited_df = st.data_editor(df, num_rows="dynamic", use_container_width=True, key="main_editor")
        if st.button("💾 Guardar Cambios Tabla", use_container_width=True):
            edited_df['Impacto_Mensual'] = edited_df.apply(lambda x: x['Importe']/12 if x['Frecuencia']=="Anual" else x['Importe'], axis=1)
            save_all_data(edited_df)
            st.success("Actualizado")
            st.rerun()

    with tab_conf:
        st.write("Tus Categorías:")
        new_cats = st.data_editor(pd.DataFrame({"Categoría": lista_cats}), num_rows="dynamic", use_container_width=True)
        if st.button("💾 Actualizar Categorías", use_container_width=True):
            save_categories([c for c in new_cats["Categoría"].tolist() if c])
            st.success("Categorías guardadas")
            st.rerun()
