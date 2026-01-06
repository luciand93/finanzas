import streamlit as st
import pandas as pd
import plotly.express as px
from datetime import datetime
import os

# --- CONFIGURACIÓN DE LA PÁGINA ---
st.set_page_config(page_title="Finanzas Proactivas", layout="wide", page_icon="💰")

# --- LÓGICA DE DATOS (BACKEND) ---
FILE_NAME = "finanzas.csv"
COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual"]

def load_data():
    if os.path.exists(FILE_NAME):
        df = pd.read_csv(FILE_NAME)
        df['Fecha'] = pd.to_datetime(df['Fecha'])
        return df
    return pd.DataFrame(columns=COLUMNS)

def save_transaction(tipo, fecha, categoria, concepto, importe, frecuencia):
    # Lógica de Prorrateo
    impacto = importe / 12 if frecuencia == "Anual" else importe
    
    new_data = {
        "Fecha": [pd.to_datetime(fecha)],
        "Tipo": [tipo],
        "Categoría": [categoria],
        "Concepto": [concepto],
        "Importe": [importe],
        "Frecuencia": [frecuencia],
        "Impacto_Mensual": [impacto]
    }
    
    df_new = pd.DataFrame(new_data)
    
    if os.path.exists(FILE_NAME):
        df_new.to_csv(FILE_NAME, mode='a', header=False, index=False)
    else:
        df_new.to_csv(FILE_NAME, index=False)
    
    st.session_state.data_changed = True

# --- INICIALIZACIÓN DEL ESTADO ---
if 'data_changed' not in st.session_state:
    st.session_state.data_changed = False

df = load_data()

# --- SIDEBAR: REGISTRO ---
st.sidebar.header("📥 Registrar Transacción")

with st.sidebar.form("transaccion_form", clear_on_submit=True):
    tipo = st.radio("Tipo", ["Ingreso", "Gasto"], horizontal=True)
    fecha = st.date_input("Fecha", datetime.now())
    categoria = st.selectbox("Categoría", 
                             ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"])
    concepto = st.text_input("Concepto", placeholder="Ej: Alquiler, Nómina...")
    importe = st.number_input("Importe ($)", min_value=0.0, step=10.0)
    frecuencia = st.selectbox("Frecuencia", ["Mensual", "Anual", "Puntual"])
    
    submit = st.form_submit_button("Guardar Transacción")
    
    if submit:
        if importe > 0 and concepto:
            save_transaction(tipo, fecha, categoria, concepto, importe, frecuencia)
            st.success("¡Transacción guardada!")
            st.rerun()
        else:
            st.warning("Por favor, completa el importe y el concepto.")

# --- PANEL PRINCIPAL ---
st.title("🚀 Panel de Finanzas Proactivas")
st.markdown("---")

if df.empty:
    st.info("No hay datos registrados. Comienza agregando una transacción en el panel lateral.")
else:
    tab1, tab2, tab3 = st.tabs(["📊 Resumen General", "🔍 Análisis Detallado", "🤖 Asesor Proactivo"])

    # Cálculos base para el Dashboard
    month_now = datetime.now().month
    year_now = datetime.now().year
    
    df_mes = df[(df['Fecha'].dt.month == month_now) & (df['Fecha'].dt.year == year_now)]
    
    # KPIs
    ingresos_mes = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
    gastos_reales_mes = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
    
    # El Gasto Prorrateado usa TODOS los gastos "Mensuales" + (Anuales/12) sin importar el mes de pago
    gasto_prorrateado = df[df['Tipo'] == "Gasto"]['Impacto_Mensual'].sum() / len(df['Fecha'].dt.to_period('M').unique())

    with tab1:
        col1, col2, col3 = st.columns(3)
        col1.metric("Ingresos (Mes Actual)", f"${ingresos_mes:,.2f}")
        col2.metric("Gastos Reales (Caja)", f"${gastos_reales_mes:,.2f}")
        col3.metric("Gasto Mensual Prorrateado", f"${gasto_prorrateado:,.2f}", 
                   help="Suma de gastos mensuales + (gastos anuales / 12)")

        # Gráfico de Barras Apiladas (Evolución)
        df_evolucion = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
        df_evolucion['Fecha'] = df_evolucion['Fecha'].astype(str)
        
        fig_bar = px.bar(df_evolucion, x='Fecha', y='Importe', color='Tipo',
                         title="Evolución Mensual: Ingresos vs Gastos",
                         barmode='group', color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'})
        st.plotly_chart(fig_bar, use_container_width=True)

    with tab2:
        col_a, col_b = st.columns([1, 1])
        
        with col_a:
            # Gráfico de Donut por Categoría (Prorrateado)
            df_gastos = df[df['Tipo'] == "Gasto"]
            fig_donut = px.pie(df_gastos, values='Impacto_Mensual', names='Categoría', 
                               hole=0.5, title="Distribución de Gastos (Impacto Real)")
            st.plotly_chart(fig_donut, use_container_width=True)
            
        with col_b:
            st.subheader("Últimos Movimientos")
            st.dataframe(df.sort_values(by="Fecha", ascending=False).head(10), use_container_width=True)

    with tab3:
        st.subheader("Análisis de Salud Financiera")
        
        if ingresos_mes > 0:
            # 1. Regla de Ahorro
            ahorro_potencial = ingresos_mes - gasto_prorrateado
            pct_ahorro = (ahorro_potencial / ingresos_mes)
            
            if pct_ahorro < 0.10:
                st.error(f"⚠️ **Alerta de Fragilidad:** Tu capacidad de ahorro es del {pct_ahorro:.1%}. Está por debajo del 10% recomendado.")
            else:
                st.success(f"✅ **Buen ritmo:** Estás ahorrando un {pct_ahorro:.1%} de tus ingresos.")

            # 2. Alerta de Gastos Fijos
            categorias_fijas = ["Vivienda", "Seguros", "Transporte"]
            gastos_fijos = df[df['Categoría'].isin(categorias_fijas)]['Impacto_Mensual'].sum() / len(df['Fecha'].dt.to_period('M').unique())
            ratio_fijos = gastos_fijos / ingresos_mes
            
            if ratio_fijos > 0.50:
                st.warning(f"🏠 **Costos Estructurales Altos:** Tus gastos fijos representan el {ratio_fijos:.1%} de tus ingresos. Intenta reducirlos por debajo del 50%.")
            else:
                st.info(f"ℹ️ Tus gastos fijos están controlados ({ratio_fijos:.1%} de los ingresos).")
                
            # 3. Proyección Anual
            proyeccion_anual = gasto_prorrateado * 12
            st.metric("Proyección de Gasto Anual", f"${proyeccion_anual:,.2f}")
            st.write(f"Al ritmo actual, tus gastos totales en un año ascenderán a **${proyeccion_anual:,.2f}**. Asegúrate de que tus ingresos anuales superen esta cifra.")
        else:
            st.info("Registra tus ingresos del mes para activar el asesoramiento.")

# --- FOOTER ---
st.sidebar.markdown("---")
st.sidebar.caption("💡 Tip: Los gastos anuales se dividen entre 12 automáticamente para el cálculo de impacto mensual.")
