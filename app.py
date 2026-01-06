import streamlit as st
import pandas as pd
import plotly.express as px
from datetime import datetime
import os
import re

# --- CONFIGURACIÓN ---
st.set_page_config(page_title="Finanzas Proactivas €", layout="wide", page_icon="💶")

FILE_NAME = "finanzas.csv"
COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual"]

# Diccionario para traducir meses
MESES_ES = {
    "January": "Enero", "February": "Febrero", "March": "Marzo", "April": "Abril",
    "May": "Mayo", "June": "Junio", "July": "Julio", "August": "Agosto",
    "September": "Septiembre", "October": "Octubre", "November": "Noviembre", "December": "Diciembre"
}

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

def translate_period(period_str):
    """Convierte un periodo tipo '2026-01' a 'Enero 2026'"""
    date_obj = datetime.strptime(period_str, "%Y-%m")
    mes_en = date_obj.strftime("%B")
    return f"{MESES_ES.get(mes_en, mes_en)} {date_obj.year}"

# --- CARGA ---
df = load_data()

# --- SIDEBAR ---
st.sidebar.header("📥 Nuevo Registro")
with st.sidebar.form("form_reg", clear_on_submit=True):
    tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=1, horizontal=True)
    fecha_texto = st.text_input("Fecha (dd/mm/yyyy)", value=datetime.now().strftime("%d/%m/%Y"))
    cat = st.selectbox("Categoría", ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"])
    con = st.text_input("Concepto")
    imp = st.number_input("Importe (€)", min_value=0.0)
    fre = st.selectbox("Frecuencia", ["Mensual", "Anual", "Puntual"])
    
    if st.form_submit_button("Guardar"):
        if re.match(r"^\d{2}/\d{2}/\d{4}$", fecha_texto):
            try:
                fecha_dt = datetime.strptime(fecha_texto, "%d/%m/%Y")
                if imp > 0 and con:
                    impacto = imp / 12 if fre == "Anual" else imp
                    new_row = pd.DataFrame([[fecha_dt, tipo, cat, con, imp, fre, impacto]], columns=COLUMNS)
                    df = pd.concat([df, new_row], ignore_index=True)
                    save_all_data(df)
                    st.sidebar.success("¡Guardado!")
                    st.rerun()
                else:
                    st.sidebar.error("Importe y concepto obligatorios.")
            except ValueError:
                st.sidebar.error("Fecha no válida.")
        else:
            st.sidebar.error("Usa el formato dd/mm/yyyy.")

# --- PANEL PRINCIPAL ---
st.title("🚀 Gestión Financiera Proactiva (€)")

if df.empty:
    st.info("Registra datos para comenzar.")
else:
    tab1, tab2, tab3 = st.tabs(["📊 Dashboard", "🔍 Historial", "⚙️ Edición Ágil"])

    with tab1:
        # Cálculos KPIs
        m, y = datetime.now().month, datetime.now().year
        df_mes = df[(df['Fecha'].dt.month == m) & (df['Fecha'].dt.year == y)]
        i_mes = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
        g_mes = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
        
        c1, c2, c3 = st.columns(3)
        c1.metric("Ingresos Mes", f"{i_mes:,.2f} €")
        c2.metric("Gastos Mes", f"{g_mes:,.2f} €")
        
        # --- LÓGICA DE TRADUCCIÓN PARA EL GRÁFICO ---
        df_ev = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
        df_ev['Fecha_Ref'] = df_ev['Fecha'].astype(str) # Guardamos el string original para ordenar
        df_ev['Mes_Castellano'] = df_ev['Fecha_Ref'].apply(translate_period)
        
        # Ordenamos por la fecha original para que el gráfico no se desordene alfabéticamente
        df_ev = df_ev.sort_values("Fecha")

        fig = px.bar(df_ev, x='Mes_Castellano', y='Importe', color='Tipo', 
                     barmode='group', title="Evolución Mensual",
                     labels={'Importe': 'Euros (€)', 'Mes_Castellano': 'Mes'},
                     color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'})
        
        st.plotly_chart(fig, use_container_width=True)

    with tab2:
        df_h = df.sort_values(by="Fecha", ascending=False).copy()
        df_h['Fecha'] = df_h['Fecha'].dt.strftime('%d/%m/%Y')
        st.dataframe(df_h, use_container_width=True)

    with tab3:
        st.subheader("📝 Editor Ágil")
        edited_df = st.data_editor(
            df,
            column_config={
                "Fecha": st.column_config.DateColumn("Fecha", format="DD/MM/YYYY"),
                "Tipo": st.column_config.SelectboxColumn("Tipo", options=["Ingreso", "Gasto"]),
                "Categoría": st.column_config.SelectboxColumn("Categoría", options=["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"]),
                "Frecuencia": st.column_config.SelectboxColumn("Frecuencia", options=["Mensual", "Anual", "Puntual"]),
                "Importe": st.column_config.NumberColumn("Importe (€)", format="%.2f €"),
                "Impacto_Mensual": st.column_config.NumberColumn("Impacto (€)", disabled=True, format="%.2f €")
            },
            num_rows="dynamic",
            use_container_width=True
        )

        if st.button("💾 Guardar Cambios"):
            edited_df['Impacto_Mensual'] = edited_df.apply(
                lambda x: x['Importe'] / 12 if x['Frecuencia'] == "Anual" else x['Importe'], axis=1
            )
            save_all_data(edited_df)
            st.success("Base de datos actualizada.")
            st.rerun()
