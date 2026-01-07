import streamlit as st
import pandas as pd
import plotly.express as px
from datetime import datetime
import os

# --- CONFIGURACIÓN PÁGINA ---
st.set_page_config(
    page_title="Finanzas Proactivas €", 
    layout="wide", 
    page_icon="💶",
    initial_sidebar_state="expanded"
)

# --- CONSTANTES ---
FILE_NAME = "finanzas.csv"
CAT_FILE_NAME = "categorias.csv"
REC_FILE_NAME = "recurrentes.csv"

MESES_ES_DICT = {
    1: "Enero", 2: "Febrero", 3: "Marzo", 4: "Abril", 5: "Mayo", 6: "Junio",
    7: "Julio", 8: "Agosto", 9: "Septiembre", 10: "Octubre", 11: "Noviembre", 12: "Diciembre"
}

COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual", "Es_Conjunto"]
COLUMNS_REC = ["Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Es_Conjunto"]

# --- FUNCIONES ---
def load_data():
    if os.path.exists(FILE_NAME):
        try:
            df = pd.read_csv(FILE_NAME)
            df['Fecha'] = pd.to_datetime(df['Fecha'], dayfirst=True, errors='coerce')
            if "Es_Conjunto" not in df.columns: df["Es_Conjunto"] = False
            return df.dropna(subset=['Fecha'])
        except: pass
    return pd.DataFrame(columns=COLUMNS)

def save_all_data(df):
    df_to_save = df.copy()
    df_to_save['Fecha'] = df_to_save['Fecha'].dt.strftime("%d/%m/%Y")
    df_to_save.to_csv(FILE_NAME, index=False)

def load_recurrentes():
    if os.path.exists(REC_FILE_NAME):
        try: return pd.read_csv(REC_FILE_NAME)
        except: pass
    return pd.DataFrame(columns=COLUMNS_REC)

def save_recurrentes(df):
    df.to_csv(REC_FILE_NAME, index=False)

def load_categories():
    default = ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"]
    if os.path.exists(CAT_FILE_NAME):
        try:
            df = pd.read_csv(CAT_FILE_NAME)
            if not df.empty: return df['Categoría'].tolist()
        except: pass
    return default

def save_categories(lista):
    lista = list(dict.fromkeys(lista)) 
    pd.DataFrame({"Categoría": lista}).to_csv(CAT_FILE_NAME, index=False)

def formatear_periodo_es(fecha_dt):
    if isinstance(fecha_dt, str):
        try: fecha_dt = datetime.strptime(fecha_dt, "%Y-%m")
        except: return fecha_dt
    return f"{MESES_ES_DICT[fecha_dt.month]} {fecha_dt.year}"

# --- ESTADO SESIÓN ---
if 'simulacion' not in st.session_state: 
    st.session_state.simulacion = []

# --- CARGA ---
df = load_data()
df_rec = load_recurrentes()
lista_cats = load_categories()

# --- SIDEBAR ---
st.sidebar.header("📝 Gestión de Movimientos")
modo_simulacion = st.sidebar.checkbox("🧪 Modo Simulación", help="Prueba gastos sin guardar")

color = "orange" if modo_simulacion else "green"
txt = "MODO ESCENARIO" if modo_simulacion else "MODO REGISTRO"
st.sidebar.markdown(f":{color}[**{txt}**]")

if modo_simulacion and len(st.session_state.simulacion) > 0:
    st.sidebar.info(f"Items simulados: {len(st.session_state.simulacion)}")

with st.sidebar.form("form_reg", clear_on_submit=True):
    tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=1, horizontal=True)
    es_conjunto = st.checkbox("👥 Gasto Conjunto (Div / 2)")
    fecha = st.date_input("Fecha", datetime.now(), format="DD/MM/YYYY")
    cat = st.selectbox("Categoría", lista_cats)
    con = st.text_input("Concepto")
    imp_input = st.number_input("Importe Total (€)", min_value=0.0, step=10.0, format="%.2f")
    fre = st.selectbox("Frecuencia", ["Mensual", "Anual", "Puntual"])
    
    imp_real = imp_input / 2 if es_conjunto and tipo == "Gasto" else imp_input
    if es_conjunto and tipo == "Gasto" and imp_input > 0:
        st.sidebar.caption(f"ℹ️ Se registrarán **{imp_real:.2f} €**")

    btn = "➕ Añadir a Simulación" if modo_simulacion else "💾 Guardar"
    
    if st.form_submit_button(btn, use_container_width=True):
        if imp_input > 0 and con:
            impacto = imp_real / 12 if fre == "Anual" else imp_real
            
            if modo_simulacion:
                # LÓGICA DE SIMULACIÓN CORREGIDA
                st.session_state.simulacion.append({
                    "Fecha": fecha.strftime("%d/%m/%Y"), 
                    "Tipo": tipo, 
                    "Concepto": f"{con} (Sim)",
                    "Importe": imp_real, 
                    "Frecuencia": fre, 
                    "Impacto_Mensual": impacto, 
                    "Es_Conjunto": es_conjunto
                })
                st.success("Añadido a simulación")
                st.rerun()
            else:
                # LÓGICA DE GUARDADO REAL
                new_row = pd.DataFrame([[pd.to_datetime(fecha), tipo, cat, con, imp_real, fre, impacto, es_conjunto]], columns=COLUMNS)
                df = pd.concat([df, new_row], ignore_index=True)
                save_all_data(df)
                st.success("Guardado")
                st.rerun()
        else: st.error("Faltan datos")

# --- DASHBOARD ---
st.title("🚀 Finanzas Personales (€)")

if df.empty: st.info("Empieza añadiendo movimientos.")
else:
    # CÁLCULOS REALES
    now = datetime.now()
    df_mes = df[(df['Fecha'].dt.month == now.month) & (df['Fecha'].dt.year == now.year)]
    
    ingresos = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
    n_meses = max(len(df['Fecha'].dt.to_period('M').unique()), 1)
    gasto_pro = df[df['Tipo'] == "Gasto"]['Impacto_Mensual'].sum() / n_meses
    
    df_anuales = df[(df['Tipo'] == "Gasto") & (df['Frecuencia'] == "Anual")]
    prov_anual = df_anuales['Impacto_Mensual'].sum() if not df_anuales.empty else 0
    total_conjunto = df[df['Es_Conjunto'] == True]['Importe'].sum()

    # PESTAÑAS
    tab1, tab2, tab3, tab4, tab5, tab6 = st.tabs(
        ["🤖 Asesor", "📊 Gráficos", "🔍 Tabla", "🔄 Recurrentes", "📝 Editar", "⚙️ Config"]
    )

    # --- TAB 1: ASESOR & SIMULACIÓN (RESTAURADO) ---
    with tab1:
        # 1. PARTE SUPERIOR: DATOS REALES
        c1, c2, c3 = st.columns(3)
        ahorro_real = ingresos - gasto_pro
        with c1:
            st.metric("Ingresos Mes", f"{ingresos:,.2f} €")
            st.metric("Gasto Promedio", f"{gasto_pro:,.2f} €")
            st.metric("Capacidad Ahorro", f"{ahorro_real:,.2f} €")
        with c2:
            st.metric("🐷 Hucha Anuales (Mes)", f"{prov_anual:,.2f} €", help="Ahorra esto cada mes")
        with c3:
            st.metric("👥 Acumulado Conjunto", f"{total_conjunto:,.2f} €")
        
        # 2. PARTE INFERIOR: ZONA DE SIMULACIÓN
        st.markdown("---")
        if len(st.session_state.simulacion) > 0:
            st.subheader("🧪 Análisis de Escenario Simulado")
            st.caption("Estos son los gastos/ingresos que estás probando (NO se han guardado):")
            
            # Recuperamos la lista y creamos DF temporal
            lista_sim = st.session_state.simulacion
            df_sim = pd.DataFrame(lista_sim)
            
            # Mostramos tabla de items simulados
            col_tabla, col_metrics = st.columns([2, 1])
            
            with col_tabla:
                st.dataframe(df_sim[['Tipo', 'Concepto', 'Importe', 'Frecuencia']], use_container_width=True, hide_index=True)
            
            # Cálculos del impacto simulado
            with col_metrics:
                sim_gastos = df_sim[df_sim['Tipo'] == "Gasto"]['Impacto_Mensual'].sum()
                sim_ingresos = df_sim[df_sim['Tipo'] == "Ingreso"]['Impacto_Mensual'].sum()
                
                nuevo_ingreso_total = ingresos + sim_ingresos
                nuevo_gasto_total = gasto_pro + sim_gastos
                nuevo_ahorro = nuevo_ingreso_total - nuevo_gasto_total
                
                st.metric("Nuevo Ahorro Proyectado", f"{nuevo_ahorro:,.2f} €", 
                          delta=f"{(nuevo_ahorro - ahorro_real):,.2f} €")
                
                if nuevo_ahorro < 0:
                    st.error("⛔ Peligro: Déficit")
                elif nuevo_ahorro < ahorro_real:
                    st.warning("📉 Ahorro reducido")
                else:
                    st.success("🚀 Ahorro mejorado")

                if st.button("🗑️ Borrar Simulación", type="primary", use_container_width=True):
                    st.session_state.simulacion = []
                    st.rerun()
        else:
            st.info("💡 Consejo: Activa el 'Modo Simulación' en la barra lateral para probar gastos sin ensuciar tus datos.")

    with tab2:
        df_ev = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
        df_ev['Mes'] = df_ev['Fecha'].dt.to_timestamp().apply(formatear_periodo_es)
        fig = px.bar(df_ev.sort_values("Fecha"), x='Mes', y='Importe', color='Tipo', barmode='group',
                     color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'})
        st.plotly_chart(fig, use_container_width=True)

    with tab3:
        st.dataframe(df.style.format({"Fecha": lambda t: t.strftime("%d/%m/%Y"), "Importe": "{:,.2f} €"}), use_container_width=True)

    # --- RECURRENTES ---
    with tab4:
        st.subheader("🔄 Generador de Gastos Fijos")
        col_list, col_action = st.columns([2, 1])
        
        with col_list:
            edited_rec = st.data_editor(df_rec, num_rows="dynamic", use_container_width=True, key="editor_rec")
            if st.button("💾 Guardar Plantillas"):
                save_recurrentes(edited_rec)
                st.success("Guardado"); st.rerun()

        with col_action:
            fecha_gen = st.date_input("Generar para fecha:", datetime.now(), format="DD/MM/YYYY")
            if st.button("🚀 Cargar Fijos", type="primary", use_container_width=True):
                if not df_rec.empty:
                    nuevos_movs = []
                    for _, row in df_rec.iterrows():
                        imp_base = row['Importe']
                        imp_final = imp_base / 2 if row['Es_Conjunto'] and row['Tipo'] == "Gasto" else imp_base
                        impacto = imp_final / 12 if row['Frecuencia'] == "Anual" else imp_final
                        nuevos_movs.append([pd.to_datetime(fecha_gen), row['Tipo'], row['Categoría'], row['Concepto'], imp_final, row['Frecuencia'], impacto, row['Es_Conjunto']])
                    
                    df = pd.concat([df, pd.DataFrame(nuevos_movs, columns=COLUMNS)], ignore_index=True)
                    save_all_data(df)
                    st.success(f"Generados {len(nuevos_movs)} movimientos"); st.rerun()

    with tab5:
        edited_df = st.data_editor(df, num_rows="dynamic", use_container_width=True,
                                   column_config={"Fecha": st.column_config.DateColumn("Fecha", format="DD/MM/YYYY")})
        if st.button("Guardar Tabla"):
            edited_df['Impacto_Mensual'] = edited_df.apply(lambda x: x['Importe']/12 if x['Frecuencia']=="Anual" else x['Importe'], axis=1)
            save_all_data(edited_df); st.rerun()

    with tab6:
        new_cats = st.data_editor(pd.DataFrame({"Categoría": lista_cats}), num_rows="dynamic")
        if st.button("Guardar Categorías"):
            save_categories([c for c in new_cats["Categoría"].tolist() if c]); st.rerun()
