import streamlit as st
import pandas as pd
import plotly.express as px
from datetime import datetime
import os
import locale

# --- INTENTO DE CONFIGURACIÓN DE IDIOMA (LOCALE) ---
# Esto ayuda a que Python maneje fechas en español, aunque el calendario visual
# depende principalmente del navegador del usuario.
try:
    locale.setlocale(locale.LC_ALL, 'es_ES.UTF-8')
except locale.Error:
    try:
        locale.setlocale(locale.LC_ALL, 'es_ES')
    except locale.Error:
        pass # Si el servidor no tiene el idioma instalado, usa el por defecto

# --- CONFIGURACIÓN DE LA PÁGINA ---
st.set_page_config(
    page_title="Finanzas Proactivas €", 
    layout="wide", 
    page_icon="💶",
    initial_sidebar_state="expanded"
)

# --- ARCHIVOS Y CONSTANTES ---
FILE_NAME = "finanzas.csv"
CAT_FILE_NAME = "categorias.csv"
COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual"]

# Diccionario de respaldo por si el locale falla en la nube
MESES_ES = {
    "January": "Enero", "February": "Febrero", "March": "Marzo", "April": "Abril",
    "May": "Mayo", "June": "Junio", "July": "Julio", "August": "Agosto",
    "September": "Septiembre", "October": "Octubre", "November": "Noviembre", "December": "Diciembre"
}

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
            if not df_cat.empty and "Categoría" in df_cat.columns:
                return df_cat['Categoría'].tolist()
        except Exception:
            pass
    return default_cats

def save_categories(lista_categorias):
    lista_limpia = list(dict.fromkeys(lista_categorias)) 
    df_cat = pd.DataFrame({"Categoría": lista_limpia})
    df_cat.to_csv(CAT_FILE_NAME, index=False)

def translate_period(period_str):
    """Traduce la fecha para el gráfico, usando locale si es posible o diccionario como backup"""
    date_obj = datetime.strptime(period_str, "%Y-%m")
    try:
        # Intenta usar el locale configurado (ej: Enero 2026)
        return date_obj.strftime("%B %Y").capitalize()
    except:
        # Fallback manual si el sistema no soporta locale español
        mes_en = date_obj.strftime("%B")
        return f"{MESES_ES.get(mes_en, mes_en)} {date_obj.year}"

# --- INICIO ---
df = load_data()
lista_categorias = load_categories()

# --- SIDEBAR ---
st.sidebar.header("📥 Nuevo Movimiento")

with st.sidebar.form("form_reg", clear_on_submit=True):
    tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=1, horizontal=True)
    
    # El calendario visual heredará el idioma del navegador/móvil
    fecha = st.date_input("Fecha", datetime.now(), format="DD/MM/YYYY")
    
    cat = st.selectbox("Categoría", lista_categorias)
    con = st.text_input("Concepto", placeholder="Ej: Cena, Gasolina...")
    
    imp = st.number_input("Importe (€)", min_value=0.0, step=10.0, format="%.2f")
    fre = st.selectbox("Frecuencia", ["Mensual", "Anual", "Puntual"])
    
    submit = st.form_submit_button("💾 Guardar Transacción", use_container_width=True)
    
    if submit:
        if imp > 0 and con:
            impacto = imp / 12 if fre == "Anual" else imp
            new_row = pd.DataFrame([[pd.to_datetime(fecha), tipo, cat, con, imp, fre, impacto]], columns=COLUMNS)
            df = pd.concat([df, new_row], ignore_index=True)
            save_all_data(df)
            st.success("¡Guardado!")
            st.rerun()
        else:
            st.error("Falta importe o concepto.")

# --- DASHBOARD ---
st.title("🚀 Finanzas (€)")

if df.empty:
    st.info("Abre el menú lateral para añadir tu primer movimiento.")
else:
    tab1, tab2, tab3, tab4 = st.tabs(["📊 Resumen", "🔍 Historial", "📝 Editar", "⚙️ Config"])

    with tab1:
        m, y = datetime.now().month, datetime.now().year
        df_mes = df[(df['Fecha'].dt.month == m) & (df['Fecha'].dt.year == y)]
        i_mes = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
        g_mes = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
        
        c1, c2 = st.columns(2)
        c1.metric("Ingresos Mes", f"{i_mes:,.2f} €")
        c2.metric("Gastos Mes", f"{g_mes:,.2f} €")
        st.metric("Balance", f"{(i_mes - g_mes):,.2f} €")
        
        # Gráfico
        st.subheader("Evolución Mensual")
        df_ev = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
        df_ev['Fecha_Ref'] = df_ev['Fecha'].astype(str)
        # Aplicamos la traducción (locale o diccionario)
        df_ev['Mes_Castellano'] = df_ev['Fecha_Ref'].apply(translate_period)
        df_ev = df_ev.sort_values("Fecha")

        fig = px.bar(df_ev, x='Mes_Castellano', y='Importe', color='Tipo', 
                     barmode='group',
                     labels={'Importe': '€', 'Mes_Castellano': ''},
                     color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'})
        
        fig.update_layout(legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1))
        st.plotly_chart(fig, use_container_width=True)

    with tab2:
        df_h = df.sort_values(by="Fecha", ascending=False).copy()
        df_h['Fecha'] = df_h['Fecha'].dt.strftime('%d/%m/%Y')
        st.dataframe(df_h, use_container_width=True)

    with tab3:
        st.write("Edita directamente en la tabla:")
        edited_df = st.data_editor(
            df,
            column_config={
                "Fecha": st.column_config.DateColumn("Fecha", format="DD/MM/YYYY"),
                "Tipo": st.column_config.SelectboxColumn("Tipo", options=["Ingreso", "Gasto"]),
                "Categoría": st.column_config.SelectboxColumn("Categoría", options=lista_categorias),
                "Frecuencia": st.column_config.SelectboxColumn("Frecuencia", options=["Mensual", "Anual", "Puntual"]),
                "Importe": st.column_config.NumberColumn("Importe (€)", format="%.2f €"),
                "Impacto_Mensual": st.column_config.NumberColumn("Impacto", disabled=True)
            },
            num_rows="dynamic",
            use_container_width=True
        )

        if st.button("💾 Actualizar Todo", use_container_width=True):
            edited_df['Impacto_Mensual'] = edited_df.apply(
                lambda x: x['Importe'] / 12 if x['Frecuencia'] == "Anual" else x['Importe'], axis=1
            )
            save_all_data(edited_df)
            st.success("Actualizado")
            st.rerun()

    with tab4:
        st.write("Gestión de Categorías:")
        df_cat_editor = pd.DataFrame({"Categoría": lista_categorias})
        
        edited_cats_df = st.data_editor(
            df_cat_editor,
            num_rows="dynamic",
            use_container_width=True,
            column_config={"Categoría": st.column_config.TextColumn("Nombre", required=True)}
        )
        
        if st.button("💾 Guardar Categorías", use_container_width=True):
            new_cats_list = [c for c in edited_cats_df["Categoría"].tolist() if c and str(c).strip() != ""]
            save_categories(new_cats_list)
            st.success("Guardado")
            st.rerun()
