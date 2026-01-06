import streamlit as st
import pandas as pd
import plotly.express as px
from datetime import datetime
import os

# --- CONFIGURACIÓN DE LA PÁGINA ---
st.set_page_config(page_title="Finanzas Proactivas €", layout="wide", page_icon="💶")

# --- LÓGICA DE DATOS ---
FILE_NAME = "finanzas.csv"
COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual"]

def load_data():
    if os.path.exists(FILE_NAME):
        df = pd.read_csv(FILE_NAME)
        df['Fecha'] = pd.to_datetime(df['Fecha'], dayfirst=True)
        return df
    return pd.DataFrame(columns=COLUMNS)

def save_all_data(df):
    # Guardamos el DataFrame completo (útil para ediciones y eliminaciones)
    df_to_save = df.copy()
    df_to_save['Fecha'] = df_to_save['Fecha'].dt.strftime("%d/%m/%Y")
    df_to_save.to_csv(FILE_NAME, index=False)

def calculate_impact(importe, frecuencia):
    return importe / 12 if frecuencia == "Anual" else importe

# --- CARGA INICIAL ---
df = load_data()

# --- SIDEBAR: REGISTRO ---
st.sidebar.header("📥 Registrar Transacción")

with st.sidebar.form("transaccion_form", clear_on_submit=True):
    # 'Gasto' es ahora la opción por defecto (index=1)
    tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=1, horizontal=True)
    fecha = st.date_input("Fecha", datetime.now())
    categoria = st.selectbox("Categoría", 
                             ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"])
    concepto = st.text_input("Concepto")
    importe = st.number_input("Importe (€)", min_value=0.0, step=10.0)
    frecuencia = st.selectbox("Frecuencia", ["Mensual", "Anual", "Puntual"])
    
    submit = st.form_submit_button("Guardar Transacción")
    
    if submit:
        if importe > 0 and concepto:
            impacto = calculate_impact(importe, frecuencia)
            new_row = pd.DataFrame([[fecha, tipo, categoria, concepto, importe, frecuencia, impacto]], columns=COLUMNS)
            df = pd.concat([df, new_row], ignore_index=True)
            save_all_data(df)
            st.success("¡Guardado!")
            st.rerun()

# --- PANEL PRINCIPAL ---
st.title("🚀 Mi Dashboard Financiero (€)")

if df.empty:
    st.info("No hay datos. Registra una transacción.")
else:
    tab1, tab2, tab3, tab4 = st.tabs(["📊 Resumen", "🔍 Detalle", "🤖 Asesor", "⚙️ Editar/Eliminar"])

    # Cálculos para Dashboard (Resumen y Detalle se mantienen igual que en versiones anteriores)
    with tab1:
        month_now, year_now = datetime.now().month, datetime.now().year
        df_mes = df[(df['Fecha'].dt.month == month_now) & (df['Fecha'].dt.year == year_now)]
        ingresos_mes = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
        gastos_reales_mes = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
        num_meses = max(len(df['Fecha'].dt.to_period('M').unique()), 1)
        g_pro = df[df['Tipo'] == "Gasto"]['Impacto_Mensual'].sum() / num_meses
        
        c1, c2, c3 = st.columns(3)
        c1.metric("Ingresos (Mes)", f"{ingresos_mes:,.2f} €")
        c2.metric("Gastos (Caja)", f"{gastos_reales_mes:,.2f} €")
        c3.metric("Gasto Prorrateado", f"{g_pro:,.2f} €")
        
        df_ev = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
        df_ev['Fecha'] = df_ev['Fecha'].astype(str)
        st.plotly_chart(px.bar(df_ev, x='Fecha', y='Importe', color='Tipo', barmode='group', title="Evolución"), use_container_width=True)

    with tab2:
        df_view = df.sort_values(by="Fecha", ascending=False).copy()
        df_view['Fecha'] = df_view['Fecha'].dt.strftime('%d/%m/%Y')
        st.dataframe(df_view, use_container_width=True)

    with tab3:
        # Lógica de asesoría simplificada
        st.write("Análisis basado en tus ingresos y gastos prorrateados.")
        # ... (Mantener lógica de mensajes de éxito/error de versiones previas)

    with tab4:
        st.subheader("Gestión de Transacciones Existentes")
        st.write("Selecciona una fila para modificarla o eliminarla.")
        
        # Mostramos la tabla con el índice visible para poder seleccionar
        st.dataframe(df)
        
        selected_index = st.number_input("Introduce el índice de la fila a gestionar:", min_value=0, max_value=len(df)-1, step=1)
        
        row = df.iloc[selected_index]
        
        with st.expander(f"Editar fila {selected_index} ({row['Concepto']})"):
            edit_tipo = st.radio("Nuevo Tipo", ["Ingreso", "Gasto"], index=0 if row['Tipo']=="Ingreso" else 1, key="e_tipo")
            edit_fecha = st.date_input("Nueva Fecha", row['Fecha'], key="e_fecha")
            edit_cat = st.selectbox("Nueva Categoría", ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"], 
                                    index=["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"].index(row['Categoría']), key="e_cat")
            edit_con = st.text_input("Nuevo Concepto", row['Concepto'], key="e_con")
            edit_imp = st.number_input("Nuevo Importe (€)", value=float(row['Importe']), key="e_imp")
            edit_fre = st.selectbox("Nueva Frecuencia", ["Mensual", "Anual", "Puntual"], 
                                    index=["Mensual", "Anual", "Puntual"].index(row['Frecuencia']), key="e_fre")
            
            col_btn1, col_btn2 = st.columns(2)
            
            if col_btn1.button("Actualizar Registro"):
                df.at[selected_index, 'Tipo'] = edit_tipo
                df.at[selected_index, 'Fecha'] = pd.to_datetime(edit_fecha)
                df.at[selected_index, 'Categoría'] = edit_cat
                df.at[selected_index, 'Concepto'] = edit_con
                df.at[selected_index, 'Importe'] = edit_imp
                df.at[selected_index, 'Frecuencia'] = edit_fre
                df.at[selected_index, 'Impacto_Mensual'] = calculate_impact(edit_imp, edit_fre)
                save_all_data(df)
                st.success("Registro actualizado")
                st.rerun()
                
            if col_btn2.button("Eliminar Registro", type="primary"):
                df = df.drop(df.index[selected_index])
                save_all_data(df)
                st.warning("Registro eliminado")
                st.rerun()
