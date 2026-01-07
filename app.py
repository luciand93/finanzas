import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots
from datetime import datetime, timedelta
import os
from io import BytesIO

# --- CONFIGURACIÓN PÁGINA ---
st.set_page_config(
    page_title="Finanzas Proactivas €", 
    layout="wide", 
    page_icon="💶",
    initial_sidebar_state="expanded",
    menu_items={
        'Get Help': None,
        'Report a bug': None,
        'About': "Aplicación de Finanzas Personales Mejorada"
    }
)

# --- ESTILOS CSS PERSONALIZADOS ---
st.markdown("""
<style>
    /* Tema moderno y limpio */
    .main {
        padding: 2rem 1rem;
    }
    
    /* Cards personalizados */
    .metric-card {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        padding: 1.5rem;
        border-radius: 15px;
        color: white;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    }
    
    .metric-card-success {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    }
    
    .metric-card-warning {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    
    .metric-card-info {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    
    /* Mejoras en formularios */
    .stForm {
        border: 2px solid #e0e0e0;
        border-radius: 10px;
        padding: 1rem;
        background-color: #fafafa;
    }
    
    /* Botones mejorados */
    .stButton>button {
        border-radius: 10px;
        border: none;
        transition: all 0.3s;
    }
    
    .stButton>button:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }
    
    /* Tabs mejorados */
    .stTabs [data-baseweb="tab-list"] {
        gap: 8px;
    }
    
    .stTabs [data-baseweb="tab"] {
        border-radius: 8px 8px 0 0;
        padding: 10px 20px;
    }
</style>
""", unsafe_allow_html=True)

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

# --- FUNCIONES DE DATOS ---
@st.cache_data
def load_data():
    if os.path.exists(FILE_NAME):
        try:
            df = pd.read_csv(FILE_NAME)
            df['Fecha'] = pd.to_datetime(df['Fecha'], dayfirst=True, errors='coerce')
            if "Es_Conjunto" not in df.columns: 
                df["Es_Conjunto"] = False
            return df.dropna(subset=['Fecha']).copy()
        except: 
            pass
    return pd.DataFrame(columns=COLUMNS)

def save_all_data(df):
    df_to_save = df.copy()
    df_to_save['Fecha'] = df_to_save['Fecha'].dt.strftime("%d/%m/%Y")
    df_to_save.to_csv(FILE_NAME, index=False)
    st.cache_data.clear()

def load_recurrentes():
    if os.path.exists(REC_FILE_NAME):
        try: 
            return pd.read_csv(REC_FILE_NAME)
        except: 
            pass
    return pd.DataFrame(columns=COLUMNS_REC)

def save_recurrentes(df):
    df.to_csv(REC_FILE_NAME, index=False)

@st.cache_data
def load_categories():
    default = ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"]
    if os.path.exists(CAT_FILE_NAME):
        try:
            df = pd.read_csv(CAT_FILE_NAME)
            if not df.empty: 
                return df['Categoría'].tolist()
        except: 
            pass
    return default

def save_categories(lista):
    lista = list(dict.fromkeys(lista))
    pd.DataFrame({"Categoría": lista}).to_csv(CAT_FILE_NAME, index=False)
    st.cache_data.clear()

def get_unique_concepts(df, categoria=None):
    """Obtiene conceptos únicos para autocompletado"""
    if df.empty:
        return []
    df_filtered = df[df['Categoría'] == categoria] if categoria else df
    concepts = df_filtered['Concepto'].unique().tolist()
    return [c for c in concepts if pd.notna(c) and c]

def formatear_periodo_es(fecha_dt):
    if isinstance(fecha_dt, str):
        try: 
            fecha_dt = datetime.strptime(fecha_dt, "%Y-%m")
        except: 
            return fecha_dt
    return f"{MESES_ES_DICT[fecha_dt.month]} {fecha_dt.year}"

def export_to_excel(df):
    """Exporta DataFrame a Excel en memoria"""
    output = BytesIO()
    with pd.ExcelWriter(output, engine='openpyxl') as writer:
        df.to_excel(writer, index=False, sheet_name='Finanzas')
    output.seek(0)
    return output.getvalue()

# --- ESTADO SESIÓN ---
if 'simulacion' not in st.session_state: 
    st.session_state.simulacion = []
if 'conceptos_historial' not in st.session_state:
    st.session_state.conceptos_historial = {}

# --- CARGA DE DATOS ---
df = load_data()
df_rec = load_recurrentes()
lista_cats = load_categories()

# --- SIDEBAR MEJORADO ---
with st.sidebar:
    st.header("📝 Gestión de Movimientos")
    
    modo_simulacion = st.checkbox(
        "🧪 Modo Simulación", 
        help="Prueba gastos sin guardar permanentemente"
    )
    
    color = "orange" if modo_simulacion else "green"
    txt = "MODO ESCENARIO" if modo_simulacion else "MODO REGISTRO"
    st.markdown(f":{color}[**{txt}**]")
    
    if modo_simulacion and len(st.session_state.simulacion) > 0:
        st.info(f"📊 Items simulados: {len(st.session_state.simulacion)}")
    
    st.markdown("---")
    
    # Inicializar estado de plantilla si no existe
    if 'template_cat' not in st.session_state:
        st.session_state.template_cat = None
    if 'template_tipo' not in st.session_state:
        st.session_state.template_tipo = None
    
    # Plantillas rápidas (FUERA del formulario)
    st.markdown("**⚡ Plantillas Rápidas:**")
    col_template1, col_template2 = st.columns(2)
    with col_template1:
        if st.button("🏠 Vivienda", use_container_width=True, key="tpl_vivienda"):
            st.session_state.template_cat = "Vivienda"
            st.session_state.template_tipo = "Gasto"
            st.rerun()
    with col_template2:
        if st.button("🍔 Comida", use_container_width=True, key="tpl_comida"):
            st.session_state.template_cat = "Comida"
            st.session_state.template_tipo = "Gasto"
            st.rerun()
    
    # FORMULARIO MEJORADO
    with st.form("form_reg", clear_on_submit=True):
        st.subheader("➕ Nuevo Movimiento")
        
        # Aplicar plantilla si existe
        tipo_index = 0 if st.session_state.template_tipo == "Ingreso" else 1
        if st.session_state.template_tipo:
            st.session_state.template_tipo = None  # Limpiar después de usar
        
        tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=tipo_index, horizontal=True)
        
        es_conjunto = st.checkbox("👥 Gasto Conjunto (Dividido / 2)")
        
        fecha = st.date_input("Fecha", datetime.now(), format="DD/MM/YYYY")
        
        # Aplicar categoría de plantilla si existe
        cat_index = 0
        if st.session_state.template_cat and st.session_state.template_cat in lista_cats:
            cat_index = lista_cats.index(st.session_state.template_cat)
            st.session_state.template_cat = None  # Limpiar después de usar
        
        cat = st.selectbox("Categoría", lista_cats, index=cat_index, key="cat_select")
        
        # Autocompletado de conceptos
        conceptos_sugeridos = get_unique_concepts(df, cat)
        
        # Opción 1: Seleccionar de sugerencias o escribir nuevo
        if conceptos_sugeridos and len(conceptos_sugeridos) > 0:
            opciones_concepto = ["(Escribe nuevo concepto)"] + conceptos_sugeridos[:10]  # Máximo 10 sugerencias
            concepto_seleccionado = st.selectbox(
                "Concepto",
                options=opciones_concepto,
                key="concepto_select",
                help="Selecciona un concepto usado antes o escribe uno nuevo"
            )
            
            if concepto_seleccionado == "(Escribe nuevo concepto)":
                concepto_input = st.text_input(
                    "Nuevo concepto",
                    help="Escribe un nuevo concepto",
                    key="concepto_input",
                    value=""
                )
            else:
                concepto_input = concepto_seleccionado
        else:
            # Si no hay sugerencias, solo campo de texto
            concepto_input = st.text_input(
                "Concepto",
                help="Escribe un nuevo concepto",
                key="concepto_input",
                value=""
            )
        
        imp_input = st.number_input(
            "Importe Total (€)", 
            min_value=0.0, 
            step=10.0, 
            format="%.2f",
            help="Introduce el importe total"
        )
        
        fre = st.selectbox(
            "Frecuencia", 
            ["Mensual", "Anual", "Puntual"],
            help="¿Con qué frecuencia se repite este movimiento?"
        )
        
        # Cálculo inteligente del importe
        imp_real = imp_input / 2 if es_conjunto and tipo == "Gasto" else imp_input
        if es_conjunto and tipo == "Gasto" and imp_input > 0:
            st.info(f"ℹ️ Se registrarán **{imp_real:.2f} €** (mitad del total)")
        
        # Validación mejorada
        btn_text = "➕ Añadir a Simulación" if modo_simulacion else "💾 Guardar Movimiento"
        
        submitted = st.form_submit_button(btn_text, use_container_width=True, type="primary")
        
        if submitted:
            # Validaciones avanzadas
            errores = []
            if not concepto_input or len(concepto_input.strip()) == 0:
                errores.append("El concepto es obligatorio")
            if imp_input <= 0:
                errores.append("El importe debe ser mayor a 0")
            
            if errores:
                for error in errores:
                    st.error(f"❌ {error}")
            else:
                impacto = imp_real / 12 if fre == "Anual" else imp_real
                
                if modo_simulacion:
                    st.session_state.simulacion.append({
                        "Fecha": fecha.strftime("%d/%m/%Y"), 
                        "Tipo": tipo,
                        "Categoría": cat,
                        "Concepto": f"{concepto_input} (Sim)", 
                        "Importe": imp_real, 
                        "Frecuencia": fre, 
                        "Impacto_Mensual": impacto, 
                        "Es_Conjunto": es_conjunto
                    })
                    st.success("✅ Añadido a simulación")
                    st.rerun()
                else:
                    new_row = pd.DataFrame([[
                        pd.to_datetime(fecha), 
                        tipo, 
                        cat, 
                        concepto_input, 
                        imp_real, 
                        fre, 
                        impacto, 
                        es_conjunto
                    ]], columns=COLUMNS)
                    df = pd.concat([df, new_row], ignore_index=True)
                    save_all_data(df)
                    
                    # Guardar en historial de conceptos
                    if cat not in st.session_state.conceptos_historial:
                        st.session_state.conceptos_historial[cat] = []
                    if concepto_input not in st.session_state.conceptos_historial[cat]:
                        st.session_state.conceptos_historial[cat].append(concepto_input)
                    
                    st.success("✅ Movimiento guardado correctamente")
                    st.rerun()

# --- DASHBOARD PRINCIPAL ---
st.title("💶 Finanzas Personales Pro")

if df.empty:
    st.info("👋 ¡Bienvenido! Empieza añadiendo tu primer movimiento desde la barra lateral.")
else:
    # --- FILTROS GLOBALES ---
    with st.expander("🔍 Filtros y Búsqueda", expanded=False):
        col_filt1, col_filt2, col_filt3, col_filt4 = st.columns(4)
        
        with col_filt1:
            filtro_tipo = st.selectbox("Tipo", ["Todos"] + df['Tipo'].unique().tolist())
        with col_filt2:
            filtro_cat = st.selectbox("Categoría", ["Todas"] + df['Categoría'].unique().tolist())
        with col_filt3:
            fecha_min = df['Fecha'].min().date() if not df.empty else datetime.now().date()
            fecha_max = df['Fecha'].max().date() if not df.empty else datetime.now().date()
            rango_fechas = st.date_input(
                "Rango de fechas",
                (fecha_min, fecha_max),
                format="DD/MM/YYYY"
            )
        with col_filt4:
            busqueda = st.text_input("🔎 Buscar en conceptos", "")
        
        # Aplicar filtros
        df_filtrado = df.copy()
        if filtro_tipo != "Todos":
            df_filtrado = df_filtrado[df_filtrado['Tipo'] == filtro_tipo]
        if filtro_cat != "Todas":
            df_filtrado = df_filtrado[df_filtrado['Categoría'] == filtro_cat]
        if isinstance(rango_fechas, tuple) and len(rango_fechas) == 2:
            df_filtrado = df_filtrado[
                (df_filtrado['Fecha'].dt.date >= rango_fechas[0]) &
                (df_filtrado['Fecha'].dt.date <= rango_fechas[1])
            ]
        if busqueda:
            df_filtrado = df_filtrado[
                df_filtrado['Concepto'].str.contains(busqueda, case=False, na=False)
            ]
    # CÁLCULOS PRINCIPALES
    now = datetime.now()
    df_mes = df_filtrado[
        (df_filtrado['Fecha'].dt.month == now.month) & 
        (df_filtrado['Fecha'].dt.year == now.year)
    ]
    
    ingresos = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
    gastos_mes = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
    
    n_meses = max(len(df['Fecha'].dt.to_period('M').unique()), 1)
    gasto_pro = df[df['Tipo'] == "Gasto"]['Impacto_Mensual'].sum() / n_meses
    
    df_anuales = df[(df['Tipo'] == "Gasto") & (df['Frecuencia'] == "Anual")]
    prov_anual = df_anuales['Impacto_Mensual'].sum() if not df_anuales.empty else 0
    total_conjunto = df[df['Es_Conjunto'] == True]['Importe'].sum()
    
    # MÉTRICAS PRINCIPALES MEJORADAS
    col1, col2, col3, col4 = st.columns(4)
    
    ahorro_real = ingresos - gasto_pro
    with col1:
        st.metric(
            "💰 Ingresos del Mes", 
            f"{ingresos:,.2f} €",
            delta=f"{ingresos - df[df['Tipo']=='Ingreso']['Importe'].sum()/max(n_meses,1):,.2f} €" if n_meses > 1 else None
        )
    with col2:
        st.metric(
            "💸 Gastos del Mes", 
            f"{gastos_mes:,.2f} €",
            delta=f"{gastos_mes - gasto_pro:,.2f} €"
        )
    with col3:
        st.metric(
            "💵 Capacidad de Ahorro", 
            f"{ahorro_real:,.2f} €",
            delta=f"{ahorro_real/ingresos*100:.1f}%" if ingresos > 0 else None
        )
    with col4:
        st.metric(
            "🐷 Hucha Anual (Mes)", 
            f"{prov_anual:,.2f} €",
            help="Cantidad a ahorrar mensualmente para gastos anuales"
        )
    
    # PESTAÑAS MEJORADAS
    tab1, tab2, tab3, tab4, tab5, tab6, tab7 = st.tabs(
        ["📊 Dashboard", "📈 Gráficos Avanzados", "🔍 Tabla Completa", "🔄 Recurrentes", 
         "📝 Editar", "📤 Exportar", "⚙️ Configuración"]
    )
    
    # --- TAB 1: DASHBOARD MEJORADO ---
    with tab1:
        st.subheader("📊 Vista General")
        
        # Métricas extendidas
        col_ext1, col_ext2, col_ext3 = st.columns(3)
        with col_ext1:
            total_ingresos = df_filtrado[df_filtrado['Tipo']=='Ingreso']['Importe'].sum()
            st.metric("📥 Total Ingresos", f"{total_ingresos:,.2f} €")
        with col_ext2:
            total_gastos = df_filtrado[df_filtrado['Tipo']=='Gasto']['Importe'].sum()
            st.metric("📤 Total Gastos", f"{total_gastos:,.2f} €")
        with col_ext3:
            balance = total_ingresos - total_gastos
            st.metric("⚖️ Balance", f"{balance:,.2f} €", 
                     delta=f"{balance/abs(total_ingresos)*100:.1f}%" if total_ingresos > 0 else None)
        
        st.markdown("---")
        
        # Gráfico rápido de evolución
        if len(df_filtrado) > 0:
            df_ev_quick = df_filtrado.groupby([df_filtrado['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
            df_ev_quick['Mes'] = df_ev_quick['Fecha'].dt.to_timestamp().apply(formatear_periodo_es)
            
            fig_quick = px.line(
                df_ev_quick.sort_values("Fecha"), 
                x='Mes', 
                y='Importe', 
                color='Tipo',
                markers=True,
                title="Evolución Temporal",
                color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'}
            )
            fig_quick.update_layout(
                hovermode='x unified',
                height=400,
                template='plotly_white'
            )
            st.plotly_chart(fig_quick, use_container_width=True)
        
        # Simulación (mantener funcionalidad original mejorada)
        st.markdown("---")
        if len(st.session_state.simulacion) > 0:
            st.subheader("🧪 Análisis de Escenario Simulado")
            
            lista_sim = st.session_state.simulacion
            df_sim = pd.DataFrame(lista_sim)
            
            col_tabla, col_metrics = st.columns([2, 1])
            
            with col_tabla:
                st.dataframe(
                    df_sim[['Tipo', 'Concepto', 'Importe', 'Frecuencia', 'Categoría']], 
                    use_container_width=True, 
                    hide_index=True
                )
            
            with col_metrics:
                sim_gastos = df_sim[df_sim['Tipo'] == "Gasto"]['Impacto_Mensual'].sum()
                sim_ingresos = df_sim[df_sim['Tipo'] == "Ingreso"]['Impacto_Mensual'].sum()
                
                nuevo_ingreso_total = ingresos + sim_ingresos
                nuevo_gasto_total = gasto_pro + sim_gastos
                nuevo_ahorro = nuevo_ingreso_total - nuevo_gasto_total
                
                st.metric(
                    "Nuevo Ahorro Proyectado", 
                    f"{nuevo_ahorro:,.2f} €", 
                    delta=f"{(nuevo_ahorro - ahorro_real):,.2f} €"
                )
                
                if nuevo_ahorro < 0:
                    st.error("⛔ Peligro: Déficit proyectado")
                elif nuevo_ahorro < ahorro_real * 0.5:
                    st.warning("⚠️ Ahorro significativamente reducido")
                elif nuevo_ahorro < ahorro_real:
                    st.warning("📉 Ahorro reducido")
                else:
                    st.success("🚀 Ahorro mejorado")
                
                if st.button("🗑️ Borrar Simulación", type="primary", use_container_width=True):
                    st.session_state.simulacion = []
                    st.rerun()
        else:
            st.info("💡 Consejo: Activa el 'Modo Simulación' en la barra lateral para probar gastos sin afectar tus datos reales.")
    
    # --- TAB 2: GRÁFICOS AVANZADOS ---
    with tab2:
        st.subheader("📈 Visualizaciones Avanzadas")
        
        if len(df_filtrado) == 0:
            st.warning("No hay datos para mostrar con los filtros aplicados.")
        else:
            # Selector de tipo de gráfico
            tipo_grafico = st.selectbox(
                "Selecciona el tipo de visualización:",
                ["Evolución Temporal", "Distribución por Categorías", "Comparativa Mensual", 
                 "Tendencias y Proyección", "Heatmap de Gastos", "Gráfico Combinado"]
            )
            
            if tipo_grafico == "Evolución Temporal":
                df_ev = df_filtrado.groupby([df_filtrado['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
                df_ev['Mes'] = df_ev['Fecha'].dt.to_timestamp().apply(formatear_periodo_es)
                df_ev = df_ev.sort_values("Fecha")
                
                # Crear DataFrame pivoteado para alinear los datos por mes
                df_pivot = df_ev.pivot_table(
                    index='Fecha', 
                    columns='Tipo', 
                    values='Importe', 
                    aggfunc='sum', 
                    fill_value=0
                ).reset_index()
                df_pivot['Mes'] = df_pivot['Fecha'].apply(formatear_periodo_es)
                
                # Calcular ahorro correctamente
                if 'Ingreso' in df_pivot.columns and 'Gasto' in df_pivot.columns:
                    df_pivot['Ahorro'] = df_pivot['Ingreso'] - df_pivot['Gasto']
                elif 'Ingreso' in df_pivot.columns:
                    df_pivot['Ahorro'] = df_pivot['Ingreso']
                elif 'Gasto' in df_pivot.columns:
                    df_pivot['Ahorro'] = -df_pivot['Gasto']
                else:
                    df_pivot['Ahorro'] = 0
                
                fig = make_subplots(specs=[[{"secondary_y": True}]])
                
                if 'Ingreso' in df_pivot.columns:
                    fig.add_trace(
                        go.Scatter(
                            x=df_pivot['Mes'],
                            y=df_pivot['Ingreso'],
                            name='Ingresos',
                            mode='lines+markers',
                            line=dict(color='#00CC96', width=3),
                            marker=dict(size=8)
                        ),
                        secondary_y=False
                    )
                
                if 'Gasto' in df_pivot.columns:
                    fig.add_trace(
                        go.Scatter(
                            x=df_pivot['Mes'],
                            y=df_pivot['Gasto'],
                            name='Gastos',
                            mode='lines+markers',
                            line=dict(color='#EF553B', width=3),
                            marker=dict(size=8)
                        ),
                        secondary_y=False
                    )
                
                fig.add_trace(
                    go.Scatter(
                        x=df_pivot['Mes'],
                        y=df_pivot['Ahorro'],
                        name='Ahorro',
                        mode='lines+markers',
                        line=dict(color='#FFA726', width=2, dash='dash'),
                        marker=dict(size=6)
                    ),
                    secondary_y=True
                )
                
                fig.update_xaxes(title_text="Período")
                fig.update_yaxes(title_text="Importe (€)", secondary_y=False)
                fig.update_yaxes(title_text="Ahorro (€)", secondary_y=True)
                fig.update_layout(
                    title="Evolución de Ingresos, Gastos y Ahorro",
                    height=500,
                    hovermode='x unified',
                    template='plotly_white'
                )
                st.plotly_chart(fig, use_container_width=True)
            
            elif tipo_grafico == "Distribución por Categorías":
                df_cat = df_filtrado[df_filtrado['Tipo'] == 'Gasto'].groupby('Categoría')['Importe'].sum().reset_index()
                df_cat = df_cat.sort_values('Importe', ascending=False)
                
                col_pie, col_bar = st.columns(2)
                
                with col_pie:
                    fig_pie = px.pie(
                        df_cat,
                        values='Importe',
                        names='Categoría',
                        title="Distribución de Gastos por Categoría",
                        color_discrete_sequence=px.colors.qualitative.Set3
                    )
                    fig_pie.update_traces(textposition='inside', textinfo='percent+label')
                    st.plotly_chart(fig_pie, use_container_width=True)
                
                with col_bar:
                    fig_bar_cat = px.bar(
                        df_cat,
                        x='Categoría',
                        y='Importe',
                        title="Gastos por Categoría (Ordenado)",
                        color='Importe',
                        color_continuous_scale='Reds'
                    )
                    fig_bar_cat.update_layout(showlegend=False, height=400)
                    fig_bar_cat.update_xaxes(tickangle=45)
                    st.plotly_chart(fig_bar_cat, use_container_width=True)
            
            elif tipo_grafico == "Comparativa Mensual":
                df_mensual = df_filtrado.groupby([
                    df_filtrado['Fecha'].dt.to_period('M'),
                    'Tipo'
                ])['Importe'].sum().unstack(fill_value=0).reset_index()
                df_mensual['Mes'] = df_mensual['Fecha'].dt.to_timestamp().apply(formatear_periodo_es)
                
                fig_comp = go.Figure()
                if 'Ingreso' in df_mensual.columns:
                    fig_comp.add_trace(go.Bar(
                        name='Ingresos',
                        x=df_mensual['Mes'],
                        y=df_mensual['Ingreso'],
                        marker_color='#00CC96'
                    ))
                if 'Gasto' in df_mensual.columns:
                    fig_comp.add_trace(go.Bar(
                        name='Gastos',
                        x=df_mensual['Mes'],
                        y=df_mensual['Gasto'],
                        marker_color='#EF553B'
                    ))
                
                fig_comp.update_layout(
                    barmode='group',
                    title='Comparativa Mensual: Ingresos vs Gastos',
                    xaxis_title='Mes',
                    yaxis_title='Importe (€)',
                    height=500,
                    template='plotly_white'
                )
                st.plotly_chart(fig_comp, use_container_width=True)
            
            elif tipo_grafico == "Tendencias y Proyección":
                df_tend = df_filtrado.groupby(df_filtrado['Fecha'].dt.to_period('M'))['Importe'].sum().reset_index()
                df_tend['Fecha'] = pd.to_datetime(df_tend['Fecha'].astype(str))
                df_tend = df_tend.sort_values('Fecha')
                
                # Calcular media móvil
                df_tend['Media_Movil'] = df_tend['Importe'].rolling(window=3, center=True).mean()
                
                fig_tend = go.Figure()
                fig_tend.add_trace(go.Scatter(
                    x=df_tend['Fecha'],
                    y=df_tend['Importe'],
                    name='Valor Real',
                    mode='markers+lines',
                    marker=dict(size=8, color='#636EFA')
                ))
                fig_tend.add_trace(go.Scatter(
                    x=df_tend['Fecha'],
                    y=df_tend['Media_Movil'],
                    name='Tendencia (Media Móvil)',
                    mode='lines',
                    line=dict(width=3, color='#FFA726', dash='dash')
                ))
                
                fig_tend.update_layout(
                    title='Tendencia de Movimientos Financieros',
                    xaxis_title='Fecha',
                    yaxis_title='Importe Total (€)',
                    height=500,
                    hovermode='x unified',
                    template='plotly_white'
                )
                st.plotly_chart(fig_tend, use_container_width=True)
            
            elif tipo_grafico == "Heatmap de Gastos":
                df_gastos = df_filtrado[df_filtrado['Tipo'] == 'Gasto'].copy()
                if len(df_gastos) > 0:
                    df_gastos['Mes'] = df_gastos['Fecha'].dt.month
                    df_gastos['Año'] = df_gastos['Fecha'].dt.year
                    df_gastos['Mes_Nombre'] = df_gastos['Mes'].map(MESES_ES_DICT)
                    
                    pivot_heat = df_gastos.groupby(['Año', 'Categoría'])['Importe'].sum().reset_index()
                    pivot_heat = pivot_heat.pivot(index='Categoría', columns='Año', values='Importe').fillna(0)
                    
                    fig_heat = px.imshow(
                        pivot_heat,
                        labels=dict(x="Año", y="Categoría", color="Importe (€)"),
                        title="Heatmap: Gastos por Categoría y Año",
                        color_continuous_scale='Reds',
                        aspect="auto"
                    )
                    st.plotly_chart(fig_heat, use_container_width=True, height=500)
                else:
                    st.info("No hay datos de gastos para mostrar el heatmap.")
            
            elif tipo_grafico == "Gráfico Combinado":
                # Gráfico con múltiples métricas
                df_combo = df_filtrado.groupby([
                    df_filtrado['Fecha'].dt.to_period('M'),
                    'Tipo'
                ])['Importe'].sum().unstack(fill_value=0).reset_index()
                df_combo['Mes'] = df_combo['Fecha'].dt.to_timestamp().apply(formatear_periodo_es)
                df_combo['Balance'] = df_combo.get('Ingreso', 0) - df_combo.get('Gasto', 0)
                
                fig_combo = make_subplots(
                    rows=2, cols=1,
                    subplot_titles=('Ingresos y Gastos', 'Balance Mensual'),
                    vertical_spacing=0.1
                )
                
                if 'Ingreso' in df_combo.columns:
                    fig_combo.add_trace(
                        go.Bar(x=df_combo['Mes'], y=df_combo['Ingreso'], name='Ingresos', marker_color='#00CC96'),
                        row=1, col=1
                    )
                if 'Gasto' in df_combo.columns:
                    fig_combo.add_trace(
                        go.Bar(x=df_combo['Mes'], y=df_combo['Gasto'], name='Gastos', marker_color='#EF553B'),
                        row=1, col=1
                    )
                
                fig_combo.add_trace(
                    go.Scatter(
                        x=df_combo['Mes'], 
                        y=df_combo['Balance'], 
                        name='Balance',
                        mode='lines+markers',
                        line=dict(color='#FFA726', width=3),
                        marker=dict(size=8)
                    ),
                    row=2, col=1
                )
                
                fig_combo.update_xaxes(title_text="", row=1, col=1)
                fig_combo.update_xaxes(title_text="Mes", row=2, col=1)
                fig_combo.update_yaxes(title_text="Importe (€)", row=1, col=1)
                fig_combo.update_yaxes(title_text="Balance (€)", row=2, col=1)
                fig_combo.update_layout(height=700, showlegend=True, template='plotly_white')
                
                st.plotly_chart(fig_combo, use_container_width=True)
    
    # --- TAB 3: TABLA COMPLETA MEJORADA ---
    with tab3:
        st.subheader("🔍 Tabla de Movimientos")
        
        if len(df_filtrado) == 0:
            st.warning("No hay movimientos que coincidan con los filtros.")
        else:
            # Estadísticas rápidas
            col_stat1, col_stat2, col_stat3, col_stat4 = st.columns(4)
            with col_stat1:
                st.metric("Total Registros", len(df_filtrado))
            with col_stat2:
                st.metric("Promedio/Registro", f"{df_filtrado['Importe'].mean():,.2f} €")
            with col_stat3:
                st.metric("Máximo", f"{df_filtrado['Importe'].max():,.2f} €")
            with col_stat4:
                st.metric("Mínimo", f"{df_filtrado['Importe'].min():,.2f} €")
            
            # Tabla con formato mejorado
            df_display = df_filtrado.copy()
            df_display['Fecha'] = df_display['Fecha'].dt.strftime("%d/%m/%Y")
            
            st.dataframe(
                df_display.style.format({
                    "Importe": "{:,.2f} €",
                    "Impacto_Mensual": "{:,.2f} €"
                }),
                use_container_width=True,
                height=400
            )
    
    # --- TAB 4: RECURRENTES MEJORADO ---
    with tab4:
        st.subheader("🔄 Generador de Gastos Fijos")
        
        col_list, col_action = st.columns([2, 1])
        
        with col_list:
            st.markdown("**Plantillas de Gastos Recurrentes**")
            edited_rec = st.data_editor(
                df_rec,
                num_rows="dynamic",
                use_container_width=True,
                key="editor_rec",
                column_config={
                    "Importe": st.column_config.NumberColumn("Importe", format="%.2f"),
                    "Tipo": st.column_config.SelectboxColumn("Tipo", options=["Ingreso", "Gasto"]),
                    "Frecuencia": st.column_config.SelectboxColumn("Frecuencia", options=["Mensual", "Anual", "Puntual"])
                }
            )
            
            col_save1, col_save2 = st.columns(2)
            with col_save1:
                if st.button("💾 Guardar Plantillas", use_container_width=True):
                    save_recurrentes(edited_rec)
                    st.success("✅ Plantillas guardadas")
                    st.rerun()
            with col_save2:
                if st.button("🗑️ Limpiar Todo", use_container_width=True):
                    df_rec = pd.DataFrame(columns=COLUMNS_REC)
                    save_recurrentes(df_rec)
                    st.success("✅ Plantillas limpiadas")
                    st.rerun()
        
        with col_action:
            st.markdown("**⚡ Acciones Rápidas**")
            fecha_gen = st.date_input(
                "Generar para fecha:",
                datetime.now(),
                format="DD/MM/YYYY",
                key="fecha_gen"
            )
            
            if st.button("🚀 Cargar Todos los Fijos", type="primary", use_container_width=True):
                if not df_rec.empty:
                    nuevos_movs = []
                    for _, row in df_rec.iterrows():
                        imp_base = row['Importe']
                        imp_final = imp_base / 2 if row['Es_Conjunto'] and row['Tipo'] == "Gasto" else imp_base
                        impacto = imp_final / 12 if row['Frecuencia'] == "Anual" else imp_final
                        nuevos_movs.append([
                            pd.to_datetime(fecha_gen),
                            row['Tipo'],
                            row['Categoría'],
                            row['Concepto'],
                            imp_final,
                            row['Frecuencia'],
                            impacto,
                            row['Es_Conjunto']
                        ])
                    
                    df = pd.concat([df, pd.DataFrame(nuevos_movs, columns=COLUMNS)], ignore_index=True)
                    save_all_data(df)
                    st.success(f"✅ Generados {len(nuevos_movs)} movimientos")
                    st.rerun()
                else:
                    st.warning("No hay plantillas guardadas.")
    
    # --- TAB 5: EDICIÓN MEJORADA ---
    with tab5:
        st.subheader("📝 Editar Movimientos")
        
        if len(df) == 0:
            st.info("No hay movimientos para editar.")
        else:
            edited_df = st.data_editor(
                df,
                num_rows="dynamic",
                use_container_width=True,
                key="editor_main",
                column_config={
                    "Fecha": st.column_config.DateColumn("Fecha", format="DD/MM/YYYY"),
                    "Importe": st.column_config.NumberColumn("Importe", format="%.2f"),
                    "Tipo": st.column_config.SelectboxColumn("Tipo", options=["Ingreso", "Gasto"]),
                    "Frecuencia": st.column_config.SelectboxColumn(
                        "Frecuencia",
                        options=["Mensual", "Anual", "Puntual"]
                    ),
                    "Es_Conjunto": st.column_config.CheckboxColumn("Es Conjunto")
                }
            )
            
            col_edit1, col_edit2 = st.columns(2)
            with col_edit1:
                if st.button("💾 Guardar Cambios", type="primary", use_container_width=True):
                    edited_df['Impacto_Mensual'] = edited_df.apply(
                        lambda x: x['Importe']/12 if x['Frecuencia']=="Anual" else x['Importe'],
                        axis=1
                    )
                    save_all_data(edited_df)
                    st.success("✅ Cambios guardados correctamente")
                    st.rerun()
            with col_edit2:
                if st.button("🔄 Recargar desde archivo", use_container_width=True):
                    st.rerun()
    
    # --- TAB 6: EXPORTACIÓN ---
    with tab6:
        st.subheader("📤 Exportar Datos")
        
        col_exp1, col_exp2 = st.columns(2)
        
        with col_exp1:
            st.markdown("**Exportar como CSV**")
            csv = df_filtrado.to_csv(index=False).encode('utf-8-sig')
            st.download_button(
                label="📥 Descargar CSV",
                data=csv,
                file_name=f"finanzas_{datetime.now().strftime('%Y%m%d')}.csv",
                mime="text/csv",
                use_container_width=True
            )
        
        with col_exp2:
            st.markdown("**Exportar como Excel**")
            try:
                excel_data = export_to_excel(df_filtrado)
                st.download_button(
                    label="📊 Descargar Excel",
                    data=excel_data,
                    file_name=f"finanzas_{datetime.now().strftime('%Y%m%d')}.xlsx",
                    mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    use_container_width=True
                )
            except ImportError:
                st.warning("⚠️ Para exportar a Excel, instala: pip install openpyxl")
                st.code("pip install openpyxl")
        
        st.markdown("---")
        st.markdown("**📋 Resumen de Datos Exportados**")
        st.info(f"Se exportarán {len(df_filtrado)} registros filtrados.")
    
    # --- TAB 7: CONFIGURACIÓN MEJORADA ---
    with tab7:
        st.subheader("⚙️ Configuración")
        
        # Gestión de categorías
        st.markdown("### 📁 Gestión de Categorías")
        new_cats = st.data_editor(
            pd.DataFrame({"Categoría": lista_cats}),
            num_rows="dynamic",
            use_container_width=True,
            key="editor_cats"
        )
        
        if st.button("💾 Guardar Categorías", type="primary"):
            categorias_validas = [c for c in new_cats["Categoría"].tolist() if c and pd.notna(c)]
            if categorias_validas:
                save_categories(categorias_validas)
                st.success("✅ Categorías guardadas")
                st.rerun()
            else:
                st.error("❌ Debe haber al menos una categoría")
        
        st.markdown("---")
        
        # Estadísticas del sistema
        st.markdown("### 📊 Estadísticas del Sistema")
        col_sys1, col_sys2 = st.columns(2)
        with col_sys1:
            st.metric("Total Movimientos", len(df))
            st.metric("Categorías Activas", len(lista_cats))
        with col_sys2:
            st.metric("Plantillas Guardadas", len(df_rec))
            if not df.empty:
                fecha_antigua = df['Fecha'].min()
                fecha_reciente = df['Fecha'].max()
                st.metric("Rango de Datos", f"{fecha_antigua.strftime('%d/%m/%Y')} - {fecha_reciente.strftime('%d/%m/%Y')}")
