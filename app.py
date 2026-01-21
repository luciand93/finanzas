import streamlit as st
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots
from datetime import datetime, timedelta
import os
from io import BytesIO
import json

# Intentar importar gspread para Google Sheets
try:
    import gspread
    from google.oauth2.service_account import Credentials
    GSPREAD_AVAILABLE = True
except ImportError:
    GSPREAD_AVAILABLE = False

# Intentar importar Google Generative AI (Gemini)
try:
    import google.generativeai as genai
    GEMINI_AVAILABLE = True
except ImportError:
    GEMINI_AVAILABLE = False

# --- CONFIGURACIÓN PÁGINA ---
st.set_page_config(
    page_title="Finanzas Proactivas €", 
    layout="wide", 
    page_icon="💶",
    initial_sidebar_state="collapsed"  # Colapsado por defecto en móvil
)

# --- CSS PERSONALIZADO PARA MEJOR DISEÑO Y RESPONSIVE ---
st.markdown("""
<style>
    /* Mejoras generales de diseño */
    .main .block-container {
        padding-top: 2rem;
        padding-bottom: 2rem;
    }
    
    /* Mejor espaciado en móviles - Optimizado para Chrome Android */
    @media (max-width: 768px) {
        /* Contenedor principal más compacto */
        .main .block-container {
            padding-left: 0.75rem !important;
            padding-right: 0.75rem !important;
            padding-top: 0.5rem !important;
            padding-bottom: 1rem !important;
            max-width: 100% !important;
        }
        
        /* Reducir padding del header */
        header[data-testid="stHeader"] {
            padding: 0.5rem 0.75rem !important;
        }
        
        /* Sidebar como overlay en móvil */
        section[data-testid="stSidebar"] {
            min-width: 280px !important;
            max-width: 320px !important;
            position: fixed !important;
            z-index: 999 !important;
            height: 100vh !important;
            top: 0 !important;
            left: 0 !important;
            overflow-y: auto !important;
            overflow-x: hidden !important;
            -webkit-overflow-scrolling: touch !important;
        }
        
        /* Contenedor interno de la sidebar */
        section[data-testid="stSidebar"] > div {
            height: auto !important;
            min-height: 100% !important;
            padding-bottom: 2rem !important;
        }
        
        /* Cuando sidebar está abierta, oscurecer fondo */
        section[data-testid="stSidebar"]::after {
            content: '';
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            z-index: -1;
            pointer-events: none;
        }
        
        /* Contenedor principal cuando sidebar está abierta */
        .main .block-container {
            width: 100% !important;
        }
        
        /* Mejorar inputs en móvil - Chrome Android */
        .stTextInput > div > div > input,
        .stNumberInput > div > div > input,
        .stSelectbox > div > div > select {
            font-size: 16px !important;
            padding: 0.625rem 0.75rem !important;
            min-height: 44px !important;
        }
        
        /* Mejorar botones en móvil */
        .stButton > button {
            width: 100% !important;
            margin-bottom: 0.5rem;
            min-height: 44px !important;
            font-size: 0.95rem !important;
            padding: 0.625rem 1rem !important;
        }
        
        /* Tabs más compactos y accesibles */
        .stTabs [data-baseweb="tab-list"] {
            gap: 0.25rem !important;
            flex-wrap: wrap;
        }
        
        .stTabs [data-baseweb="tab"] {
            padding: 0.5rem 0.75rem !important;
            font-size: 0.85rem !important;
            min-width: auto !important;
        }
        
        /* Métricas en una sola columna en móvil */
        [data-testid="stMetricContainer"] {
            margin-bottom: 0.75rem !important;
        }
        
        /* Columnas se apilan en móvil */
        .stColumn {
            width: 100% !important;
            margin-bottom: 0.5rem;
        }
        
        /* Headers más pequeños */
        h1 {
            font-size: 1.5rem !important;
            margin-bottom: 0.75rem !important;
        }
        
        h2 {
            font-size: 1.25rem !important;
            margin-top: 1rem !important;
            margin-bottom: 0.5rem !important;
        }
        
        h3 {
            font-size: 1.1rem !important;
        }
        
        /* Formularios más compactos */
        .stForm {
            padding: 0.75rem !important;
            margin-bottom: 0.75rem !important;
        }
        
        /* Radio buttons más compactos */
        .stRadio > div {
            gap: 0.5rem !important;
            flex-wrap: wrap;
        }
        
        .stRadio > div > label {
            padding: 0.5rem 0.75rem !important;
            font-size: 0.9rem !important;
        }
        
        /* Selectbox más compacto */
        .stSelectbox {
            margin-bottom: 0.5rem !important;
        }
        
        /* Chat messages más compactos */
        .stChatMessage {
            padding: 0.75rem !important;
            margin-bottom: 0.5rem !important;
            font-size: 0.9rem !important;
        }
        
        /* Tablas más compactas */
        .stDataFrame {
            font-size: 0.85rem !important;
        }
        
        /* Espaciado entre elementos reducido */
        .element-container {
            margin-bottom: 0.75rem !important;
        }
        
        /* Info boxes más compactos */
        .stInfo, .stSuccess, .stWarning, .stError {
            padding: 0.75rem !important;
            font-size: 0.9rem !important;
            margin-bottom: 0.5rem !important;
        }
    }
    
    /* Mejorar el date input para móviles - Chrome Android */
    @media (max-width: 768px) {
        /* Asegurar que el calendario sea visible y centrado */
        div[data-baseweb="popover"] {
            z-index: 10000 !important;
            position: fixed !important;
            top: 50% !important;
            left: 50% !important;
            transform: translate(-50%, -50%) !important;
            max-width: 95vw !important;
            max-height: 85vh !important;
            overflow-y: auto !important;
            background: var(--background-color) !important;
            border-radius: 0.75rem !important;
            box-shadow: 0 8px 32px rgba(0,0,0,0.3) !important;
        }
        
        /* Input de fecha optimizado para móvil - SOLO LECTURA para evitar teclado */
        .stDateInput > div > div > input {
            font-size: 16px !important;
            padding: 0.625rem 0.75rem !important;
            min-height: 44px !important;
            cursor: pointer !important;
            -webkit-user-select: none !important;
            user-select: none !important;
        }
        
        /* Prevenir que el input de fecha abra el teclado */
        .stDateInput input[readonly],
        .stDateInput input {
            readonly: true !important;
            -webkit-user-select: none !important;
            user-select: none !important;
        }
        
        /* Calendario más accesible en móvil */
        .rdp {
            font-size: 1rem !important;
        }
        
        .rdp-day {
            width: 2.5rem !important;
            height: 2.5rem !important;
        }
        
        /* Asegurar que la sidebar sea scrollable en móvil */
        section[data-testid="stSidebar"] {
            overflow-y: auto !important;
            overflow-x: hidden !important;
            -webkit-overflow-scrolling: touch !important;
            max-height: 100vh !important;
        }
        
        /* Permitir scroll en contenido de sidebar */
        section[data-testid="stSidebar"] .css-1d391kg,
        section[data-testid="stSidebar"] [data-testid="stSidebarContent"] {
            overflow-y: visible !important;
            height: auto !important;
            max-height: none !important;
        }
        
        /* Asegurar que los elementos dentro de la sidebar no bloqueen el scroll */
        section[data-testid="stSidebar"] form,
        section[data-testid="stSidebar"] .stForm {
            position: relative !important;
            overflow: visible !important;
        }
        
        /* Botón de toggle sidebar más visible */
        button[data-testid="baseButton-header"] {
            min-width: 44px !important;
            min-height: 44px !important;
        }
        
        /* Prevenir que el formulario se oculte al seleccionar fecha */
        form[data-testid="stForm"] {
            position: relative !important;
            z-index: 1 !important;
        }
    }
    
    /* Mejorar diseño del chat */
    .stChatMessage {
        padding: 1rem;
        border-radius: 0.5rem;
        margin-bottom: 0.5rem;
    }
    
    /* Mejorar formularios */
    .stForm {
        border: 1px solid rgba(250, 250, 250, 0.2);
        border-radius: 0.75rem;
        padding: 1.25rem;
        background: rgba(255, 255, 255, 0.03);
        margin-bottom: 1rem;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    
    /* Mejoras visuales en la sidebar */
    section[data-testid="stSidebar"] {
        background: linear-gradient(180deg, #1e1e2e 0%, #2a2a3e 100%);
    }
    
    /* Botones más atractivos */
    .stButton > button {
        border-radius: 0.5rem;
        font-weight: 500;
        transition: all 0.3s ease;
    }
    
    /* Inputs mejorados */
    .stTextInput > div > div > input,
    .stNumberInput > div > div > input,
    .stSelectbox > div > div > select {
        border-radius: 0.5rem;
        border: 1px solid rgba(255, 255, 255, 0.2);
        transition: all 0.3s ease;
    }
    
    .stTextInput > div > div > input:focus,
    .stNumberInput > div > div > input:focus,
    .stSelectbox > div > div > select:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }
    
    /* Radio buttons mejorados */
    .stRadio > div {
        gap: 1rem;
    }
    
    .stRadio > div > label {
        border-radius: 0.5rem;
        padding: 0.5rem 1rem;
        transition: all 0.3s ease;
    }
    
    /* Tabs más atractivos */
    .stTabs [data-baseweb="tab-list"] {
        gap: 0.5rem;
    }
    
    .stTabs [data-baseweb="tab"] {
        border-radius: 0.5rem 0.5rem 0 0;
        padding: 0.75rem 1.5rem;
    }
    
    /* Mejorar visualización general en móvil */
    @media (max-width: 768px) {
        /* Viewport height fix para móviles */
        html, body {
            height: 100%;
            overflow-x: hidden;
        }
        
        /* Mejorar gráficos en móvil */
        .js-plotly-plot {
            width: 100% !important;
        }
        
        /* Captions más pequeños */
        .stCaption {
            font-size: 0.8rem !important;
        }
        
        /* Expandable sections más compactos */
        .streamlit-expanderHeader {
            font-size: 0.95rem !important;
            padding: 0.75rem !important;
        }
    }
    
    /* Animaciones sutiles */
    .stButton > button:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        transition: all 0.3s ease;
    }
    
    /* Mejorar espaciado entre elementos */
    .element-container {
        margin-bottom: 1rem;
    }
    
    /* Mejorar contenedores principales - Aprovechar toda la pantalla */
    .main .block-container {
        max-width: 100% !important;
        padding-left: 1rem !important;
        padding-right: 1rem !important;
        padding-top: 0.5rem !important;
    }
    
    /* Header fijo con botón de alta - Siempre visible */
    .top-header {
        position: sticky !important;
        top: 0 !important;
        z-index: 100 !important;
        background: var(--background-color) !important;
        padding: 0.75rem 1rem !important;
        margin: -1rem -1rem 1rem -1rem !important;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1) !important;
        backdrop-filter: blur(10px);
    }
    
    @media (max-width: 768px) {
        .top-header {
            padding: 0.5rem 0.75rem !important;
            margin: -0.5rem -0.75rem 0.75rem -0.75rem !important;
        }
        
        /* Menú más compacto en móvil */
        .top-header .stSelectbox {
            font-size: 0.9rem !important;
        }
        
        .top-header .stButton > button {
            font-size: 0.85rem !important;
            padding: 0.5rem 0.75rem !important;
        }
    }
    
    /* Aprovechar máximo espacio - reducir padding innecesario */
    .main .block-container {
        padding-top: 0.5rem !important;
    }
    
    /* Eliminar padding superior del título si existe */
    h1:first-of-type {
        margin-top: 0 !important;
        padding-top: 0 !important;
    }
    
    /* Headers más atractivos */
    h1 {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        font-weight: 700;
    }
    
    h2, h3 {
        color: #667eea;
        font-weight: 600;
    }
    
    /* Mejorar info boxes */
    .stInfo {
        border-left: 4px solid #667eea;
        border-radius: 0.5rem;
    }
    
    .stSuccess {
        border-left: 4px solid #00cc96;
        border-radius: 0.5rem;
    }
    
    .stWarning {
        border-left: 4px solid #ff9800;
        border-radius: 0.5rem;
    }
    
    .stError {
        border-left: 4px solid #ef553b;
        border-radius: 0.5rem;
    }
    
    /* Scrollbar personalizado */
    ::-webkit-scrollbar {
        width: 8px;
        height: 8px;
    }
    
    ::-webkit-scrollbar-track {
        background: rgba(255, 255, 255, 0.05);
        border-radius: 4px;
    }
    
    ::-webkit-scrollbar-thumb {
        background: rgba(102, 126, 234, 0.5);
        border-radius: 4px;
    }
    
    ::-webkit-scrollbar-thumb:hover {
        background: rgba(102, 126, 234, 0.7);
    }
    
    /* Estilos para el modal/popup - Arreglado para no bloquear */
    .modal-overlay {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background: rgba(0, 0, 0, 0.75);
        z-index: 99999 !important;
        justify-content: center;
        align-items: center;
        overflow: hidden;
    }
    
    .modal-overlay.show {
        display: flex !important;
    }
    
    .modal-content {
        background: var(--background-color) !important;
        border-radius: 1rem;
        padding: 2rem;
        max-width: 600px;
        width: 90%;
        max-height: 90vh;
        overflow-y: auto;
        overflow-x: hidden;
        box-shadow: 0 10px 40px rgba(0,0,0,0.5);
        position: relative;
        margin: auto;
        z-index: 100000 !important;
    }
    
    /* Prevenir que el body se bloquee cuando el modal está abierto */
    body.modal-open {
        overflow: hidden;
    }
    
    @media (max-width: 768px) {
        .modal-content {
            width: 95%;
            padding: 1.25rem;
            max-height: 95vh;
            border-radius: 0.75rem;
        }
        
        /* Formulario más compacto en móvil */
        .modal-content .stForm {
            padding: 0.75rem !important;
        }
        
        /* Columnas se apilan en móvil dentro del modal */
        .modal-content .stColumn {
            width: 100% !important;
        }
    }
    
    /* Aprovechar mejor el espacio en desktop */
    @media (min-width: 769px) {
        .modal-content {
            max-width: 700px;
        }
    }
    
    .modal-close {
        position: absolute;
        top: 1rem;
        right: 1rem;
        background: rgba(255, 255, 255, 0.1);
        border: none;
        border-radius: 50%;
        width: 2.5rem;
        height: 2.5rem;
        cursor: pointer;
        font-size: 1.5rem;
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
    }
    
    .modal-close:hover {
        background: rgba(255, 255, 255, 0.2);
        transform: rotate(90deg);
    }
    
    /* Menú hamburger atractivo */
    .hamburger-menu {
        position: relative;
        display: inline-block;
    }
    
    .hamburger-btn {
        background: rgba(102, 126, 234, 0.1);
        border: 1px solid rgba(102, 126, 234, 0.3);
        border-radius: 0.5rem;
        padding: 0.5rem 0.75rem;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        transition: all 0.3s ease;
        color: #667eea;
        font-weight: 500;
    }
    
    .hamburger-btn:hover {
        background: rgba(102, 126, 234, 0.2);
        border-color: rgba(102, 126, 234, 0.5);
        transform: translateY(-1px);
    }
    
    .hamburger-icon {
        display: flex;
        flex-direction: column;
        gap: 4px;
        width: 20px;
    }
    
    .hamburger-icon span {
        display: block;
        height: 2px;
        width: 100%;
        background: currentColor;
        border-radius: 2px;
        transition: all 0.3s ease;
    }
    
    .hamburger-menu.active .hamburger-icon span:nth-child(1) {
        transform: rotate(45deg) translate(5px, 5px);
    }
    
    .hamburger-menu.active .hamburger-icon span:nth-child(2) {
        opacity: 0;
    }
    
    .hamburger-menu.active .hamburger-icon span:nth-child(3) {
        transform: rotate(-45deg) translate(7px, -6px);
    }
    
    .menu-dropdown {
        display: none;
        position: absolute;
        top: calc(100% + 0.5rem);
        right: 0;
        background: var(--background-color);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 0.75rem;
        box-shadow: 0 8px 32px rgba(0,0,0,0.3);
        min-width: 250px;
        z-index: 1000;
        overflow: hidden;
        animation: slideDown 0.3s ease;
    }
    
    .hamburger-menu.active .menu-dropdown {
        display: block;
    }
    
    @keyframes slideDown {
        from {
            opacity: 0;
            transform: translateY(-10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    .menu-item {
        padding: 0.75rem 1.25rem;
        cursor: pointer;
        transition: all 0.2s ease;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }
    
    .menu-item:last-child {
        border-bottom: none;
    }
    
    .menu-item:hover {
        background: rgba(102, 126, 234, 0.1);
        padding-left: 1.5rem;
    }
    
    .menu-item.active {
        background: rgba(102, 126, 234, 0.2);
        border-left: 3px solid #667eea;
    }
    
    /* Botón de nuevo movimiento más pequeño e intuitivo */
    .btn-nuevo-mov {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 0.5rem;
        padding: 0.5rem 1rem;
        color: white;
        font-weight: 500;
        font-size: 0.9rem;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    }
    
    .btn-nuevo-mov:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
    
    @media (max-width: 768px) {
        .menu-dropdown {
            min-width: 200px;
            right: -0.5rem;
        }
        
        .menu-item {
            padding: 0.625rem 1rem;
            font-size: 0.9rem;
        }
    }
</style>
""", unsafe_allow_html=True)

# --- JAVASCRIPT PARA PREVENIR PROBLEMAS DEL CALENDARIO EN MÓVIL ---
st.components.v1.html("""
<script>
(function() {
    function fixDateInput() {
        // Prevenir que el input de fecha abra el teclado
        const dateInputs = document.querySelectorAll('.stDateInput input');
        dateInputs.forEach(input => {
            if (!input.hasAttribute('data-fixed')) {
                input.setAttribute('readonly', 'readonly');
                input.setAttribute('inputmode', 'none');
                input.setAttribute('data-fixed', 'true');
                
                // Prevenir focus que abriría el teclado
                input.addEventListener('focus', function(e) {
                    e.preventDefault();
                    setTimeout(() => this.blur(), 0);
                }, true);
                
                // Prevenir touchstart
                input.addEventListener('touchstart', function(e) {
                    // Permitir que el calendario se abra pero prevenir teclado
                    setTimeout(() => {
                        if (document.activeElement === this) {
                            this.blur();
                        }
                    }, 100);
                }, {passive: true});
                
                // Prevenir que el formulario se recargue al cambiar fecha
                input.addEventListener('change', function(e) {
                    e.stopPropagation();
                }, true);
            }
        });
        
        // Prevenir que clicks en el calendario cierren el formulario
        const calendarPopover = document.querySelector('[data-baseweb="popover"]');
        if (calendarPopover) {
            calendarPopover.addEventListener('click', function(e) {
                e.stopPropagation();
            }, true);
            
            // Prevenir clicks en días del calendario
            const calendarDays = calendarPopover.querySelectorAll('.rdp-day, [role="gridcell"]');
            calendarDays.forEach(day => {
                day.addEventListener('click', function(e) {
                    e.stopPropagation();
                    // Forzar que el formulario se mantenga visible
                    setTimeout(() => {
                        const form = document.querySelector('form[data-testid="stForm"]');
                        if (form) {
                            form.style.display = 'block';
                            form.style.visibility = 'visible';
                        }
                    }, 100);
                }, true);
            });
        }
        
        // Observar cambios en el DOM para aplicar fixes a nuevos elementos
        const observer = new MutationObserver(function(mutations) {
            dateInputs.forEach(input => {
                if (input && !input.hasAttribute('data-fixed')) {
                    fixDateInput();
                }
            });
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }
    
    // Ejecutar al cargar y después de un delay
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', fixDateInput);
    } else {
        fixDateInput();
    }
    
    // Ejecutar periódicamente para asegurar que se aplica
    setInterval(fixDateInput, 1000);
})();
</script>
""", height=0)

# --- CONSTANTES ---
FILE_NAME = "finanzas.csv"
CAT_FILE_NAME = "categorias.csv"
REC_FILE_NAME = "recurrentes.csv"
PRESUPUESTOS_FILE = "presupuestos.csv"
HISTORIAL_FILE = "historial_cambios.csv"
BACKUP_DIR = "backups"

# Configuración de Google Sheets (usar variables de entorno en Streamlit Cloud)
GOOGLE_SHEETS_ENABLED = os.getenv('GOOGLE_SHEETS_ENABLED', 'false').lower() == 'true'
GOOGLE_SHEET_ID = os.getenv('GOOGLE_SHEET_ID', '')
GOOGLE_CREDENTIALS_JSON = os.getenv('GOOGLE_CREDENTIALS_JSON', '')

# Configuración de Gemini (Google AI)
# Intentar obtener API key de st.secrets primero, luego de os.getenv
try:
    GEMINI_API_KEY = st.secrets.get('GEMINI_API_KEY', '') or os.getenv('GEMINI_API_KEY', '')
except:
    GEMINI_API_KEY = os.getenv('GEMINI_API_KEY', '')

GEMINI_ENABLED = GEMINI_AVAILABLE and GEMINI_API_KEY != ''
GEMINI_MODEL = None

# Inicializar Gemini si está disponible
def inicializar_gemini():
    """Inicializa el modelo de Gemini, probando diferentes opciones"""
    global GEMINI_MODEL, GEMINI_ENABLED
    
    if not GEMINI_ENABLED:
        return None
    
    try:
        genai.configure(api_key=GEMINI_API_KEY)
        
        # Intentar obtener la lista de modelos disponibles
        try:
            modelos_disponibles = [m.name.split('/')[-1] for m in genai.list_models() 
                                  if 'generateContent' in m.supported_generation_methods]
            
            # Priorizar modelos con "-latest" o modelos comunes
            modelos_prioridad = [
                'gemini-1.5-flash-latest',
                'gemini-1.5-pro-latest',
                'gemini-1.5-flash',
                'gemini-1.5-pro',
                'gemini-pro',
                'models/gemini-pro',
                'models/gemini-1.5-flash'
            ]
            
            modelo_a_usar = None
            
            # Buscar primero en los modelos de prioridad
            for modelo_pref in modelos_prioridad:
                nombre_corto = modelo_pref.replace('models/', '')
                if nombre_corto in modelos_disponibles:
                    modelo_a_usar = nombre_corto
                    break
            
            # Si no encontramos uno de prioridad, usar el primero disponible
            if not modelo_a_usar and modelos_disponibles:
                modelo_a_usar = modelos_disponibles[0]
            
            if modelo_a_usar:
                GEMINI_MODEL = genai.GenerativeModel(modelo_a_usar)
                return GEMINI_MODEL
            else:
                GEMINI_ENABLED = False
                return None
                
        except Exception as e:
            # Si falla listar modelos, intentar con modelos comunes directamente
            modelos_comunes = ['gemini-pro', 'gemini-1.5-flash', 'gemini-1.5-pro']
            for modelo_nombre in modelos_comunes:
                try:
                    GEMINI_MODEL = genai.GenerativeModel(modelo_nombre)
                    return GEMINI_MODEL
                except:
                    continue
            
            GEMINI_ENABLED = False
            return None
            
    except Exception as e:
        GEMINI_ENABLED = False
        GEMINI_MODEL = None
        return None

# Inicializar al cargar el módulo
if GEMINI_ENABLED:
    inicializar_gemini()

# Nombres de las hojas en Google Sheets
SHEET_FINANZAS = "Finanzas"
SHEET_CATEGORIAS = "Categorias"
SHEET_RECURRENTES = "Recurrentes"

MESES_ES_DICT = {
    1: "Enero", 2: "Febrero", 3: "Marzo", 4: "Abril", 5: "Mayo", 6: "Junio",
    7: "Julio", 8: "Agosto", 9: "Septiembre", 10: "Octubre", 11: "Noviembre", 12: "Diciembre"
}

COLUMNS = ["Fecha", "Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Impacto_Mensual", "Es_Conjunto"]
COLUMNS_REC = ["Tipo", "Categoría", "Concepto", "Importe", "Frecuencia", "Es_Conjunto"]

# --- FUNCIONES DE GOOGLE SHEETS ---
@st.cache_resource
def get_google_sheet():
    """Inicializa y retorna la conexión a Google Sheets"""
    if not GSPREAD_AVAILABLE or not GOOGLE_SHEETS_ENABLED:
        return None
    
    try:
        if GOOGLE_CREDENTIALS_JSON and GOOGLE_SHEET_ID:
            # Parsear credenciales desde JSON string
            # Si viene como string, intentar parsearlo
            if isinstance(GOOGLE_CREDENTIALS_JSON, str):
                json_str = GOOGLE_CREDENTIALS_JSON
                
                # Si el JSON tiene saltos de línea reales (de triple comillas en TOML),
                # necesitamos reemplazarlos por \n escapados correctamente
                # JSON no permite saltos de línea reales en strings, deben ser \n
                if '\n' in json_str and not '\\n' in json_str.replace('\n', '', 1):
                    # Si tiene saltos de línea reales, convertirlos a \n escapados
                    import re
                    # Reemplazar saltos de línea dentro de valores de strings JSON
                    # Necesitamos preservar los saltos de línea dentro de las comillas
                    # Primero normalizar
                    json_str = json_str.replace('\r\n', '\n').replace('\r', '\n')
                    # Eliminar saltos de línea fuera de strings (entre propiedades)
                    # Pero preservar \n dentro de los valores de strings
                    lines = json_str.split('\n')
                    cleaned_lines = []
                    in_string = False
                    escape_next = False
                    
                    for line in lines:
                        cleaned_line = line.strip()
                        if cleaned_line.startswith('"') or cleaned_line.startswith("'"):
                            # Determinar si estamos dentro de un string
                            quote_char = cleaned_line[0]
                            in_string = cleaned_line.count(quote_char) % 2 != 0
                        
                        if in_string and not cleaned_line.startswith('"') and not cleaned_line.startswith("'"):
                            # Dentro de un string, agregar \n antes
                            cleaned_lines.append('\\n' + cleaned_line)
                        else:
                            cleaned_lines.append(cleaned_line)
                    
                    # Mejor enfoque: reemplazar saltos de línea por \n escapado
                    json_str = json_str.replace('\n', '\\n')
                
                # Ahora parsear el JSON
                creds_dict = json.loads(json_str)
            else:
                creds_dict = GOOGLE_CREDENTIALS_JSON
            
            scopes = [
                'https://www.googleapis.com/auth/spreadsheets',
                'https://www.googleapis.com/auth/drive'
            ]
            creds = Credentials.from_service_account_info(creds_dict, scopes=scopes)
            client = gspread.authorize(creds)
            return client.open_by_key(GOOGLE_SHEET_ID)
        elif os.path.exists('credentials.json') and GOOGLE_SHEET_ID:
            # Fallback: usar archivo de credenciales local (útil para desarrollo)
            scopes = [
                'https://www.googleapis.com/auth/spreadsheets',
                'https://www.googleapis.com/auth/drive'
            ]
            creds = Credentials.from_service_account_file('credentials.json', scopes=scopes)
            client = gspread.authorize(creds)
            return client.open_by_key(GOOGLE_SHEET_ID)
    except Exception as e:
        st.error(f"Error conectando a Google Sheets: {str(e)}")
        return None
    
    return None

def get_or_create_worksheet(sheet, sheet_name, headers):
    """Obtiene o crea una hoja de cálculo con los encabezados"""
    try:
        worksheet = sheet.worksheet(sheet_name)
        return worksheet
    except gspread.exceptions.WorksheetNotFound:
        # Crear nueva hoja
        worksheet = sheet.add_worksheet(title=sheet_name, rows=1000, cols=20)
        if headers:
            worksheet.append_row(headers)
        return worksheet
    except Exception as e:
        st.error(f"Error obteniendo hoja {sheet_name}: {str(e)}")
        return None

# --- FUNCIONES DE DATOS ---
@st.cache_data
def load_data():
    """Carga datos desde Google Sheets o archivo local"""
    # Intentar cargar desde Google Sheets primero
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, SHEET_FINANZAS, COLUMNS)
                if worksheet:
                    records = worksheet.get_all_records()
                    if records:
                        df = pd.DataFrame(records)
                        df['Fecha'] = pd.to_datetime(df['Fecha'], dayfirst=True, errors='coerce')
                        if "Es_Conjunto" not in df.columns: 
                            df["Es_Conjunto"] = False
                        return df.dropna(subset=['Fecha']).copy()
                    else:
                        # Si está vacía, retornar DataFrame vacío
                        return pd.DataFrame(columns=COLUMNS)
            except Exception as e:
                st.warning(f"Error cargando desde Google Sheets: {str(e)}. Usando archivo local.")
    
    # Fallback: cargar desde archivo local
    if os.path.exists(FILE_NAME):
        try:
            df = pd.read_csv(FILE_NAME)
            df['Fecha'] = pd.to_datetime(df['Fecha'], dayfirst=True, errors='coerce')
            if "Es_Conjunto" not in df.columns: df["Es_Conjunto"] = False
            return df.dropna(subset=['Fecha'])
        except: pass
    return pd.DataFrame(columns=COLUMNS)

def save_all_data(df):
    """Guarda datos en Google Sheets o archivo local"""
    df_to_save = df.copy()
    df_to_save['Fecha'] = df_to_save['Fecha'].dt.strftime("%d/%m/%Y")
    
    # Intentar guardar en Google Sheets primero
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, SHEET_FINANZAS, COLUMNS)
                if worksheet:
                    # Limpiar hoja y escribir nuevos datos
                    worksheet.clear()
                    worksheet.append_row(COLUMNS)
                    
                    # Convertir DataFrame a lista de listas
                    for _, row in df_to_save.iterrows():
                        worksheet.append_row(row.tolist())
                    
                    st.cache_data.clear()
                    return
            except Exception as e:
                st.warning(f"Error guardando en Google Sheets: {str(e)}. Guardando en archivo local.")
    
    # Fallback: guardar en archivo local
    df_to_save.to_csv(FILE_NAME, index=False)

def load_recurrentes():
    """Carga gastos recurrentes desde Google Sheets o archivo local"""
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, SHEET_RECURRENTES, COLUMNS_REC)
                if worksheet:
                    records = worksheet.get_all_records()
                    if records:
                        return pd.DataFrame(records)
                    else:
                        worksheet.append_row(COLUMNS_REC)
            except Exception as e:
                st.warning(f"Error cargando recurrentes desde Google Sheets: {str(e)}")
    
    if os.path.exists(REC_FILE_NAME):
        try: return pd.read_csv(REC_FILE_NAME)
        except: pass
    return pd.DataFrame(columns=COLUMNS_REC)

def save_recurrentes(df):
    """Guarda gastos recurrentes en Google Sheets o archivo local"""
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, SHEET_RECURRENTES, COLUMNS_REC)
                if worksheet:
                    worksheet.clear()
                    worksheet.append_row(COLUMNS_REC)
                    for _, row in df.iterrows():
                        worksheet.append_row(row.tolist())
                    return
            except Exception as e:
                st.warning(f"Error guardando recurrentes en Google Sheets: {str(e)}")
    
    df.to_csv(REC_FILE_NAME, index=False)

def load_categories():
    """Carga categorías desde Google Sheets o archivo local"""
    default = ["Vivienda", "Transporte", "Comida", "Seguros", "Ahorro", "Ingresos", "Otros"]
    
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, SHEET_CATEGORIAS, ["Categoría"])
                if worksheet:
                    records = worksheet.get_all_records()
                    if records:
                        return [r['Categoría'] for r in records if r.get('Categoría')]
                    else:
                        # Inicializar con categorías por defecto
                        for cat in default:
                            worksheet.append_row([cat])
                        return default
            except Exception as e:
                st.warning(f"Error cargando categorías desde Google Sheets: {str(e)}")
    
    if os.path.exists(CAT_FILE_NAME):
        try:
            df = pd.read_csv(CAT_FILE_NAME)
            if not df.empty: return df['Categoría'].tolist()
        except: pass
    return default

def save_categories(lista):
    """Guarda categorías en Google Sheets o archivo local"""
    lista = list(dict.fromkeys(lista)) 
    
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, SHEET_CATEGORIAS, ["Categoría"])
                if worksheet:
                    worksheet.clear()
                    worksheet.append_row(["Categoría"])
                    for cat in lista:
                        worksheet.append_row([cat])
                    st.cache_data.clear()
                    return
            except Exception as e:
                st.warning(f"Error guardando categorías en Google Sheets: {str(e)}")
    
    pd.DataFrame({"Categoría": lista}).to_csv(CAT_FILE_NAME, index=False)
    st.cache_data.clear()

def formatear_periodo_es(fecha_dt):
    if isinstance(fecha_dt, str):
        try: fecha_dt = datetime.strptime(fecha_dt, "%Y-%m")
        except: return fecha_dt
    return f"{MESES_ES_DICT[fecha_dt.month]} {fecha_dt.year}"

# --- FUNCIONES DE PRESUPUESTOS ---
def load_presupuestos():
    """Carga presupuestos mensuales por categoría"""
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, "Presupuestos", ["Categoría", "Presupuesto_Mensual"])
                if worksheet:
                    records = worksheet.get_all_records()
                    if records:
                        return pd.DataFrame(records)
            except: pass
    
    if os.path.exists(PRESUPUESTOS_FILE):
        try:
            return pd.read_csv(PRESUPUESTOS_FILE)
        except: pass
    return pd.DataFrame(columns=["Categoría", "Presupuesto_Mensual"])

def save_presupuestos(df_pres):
    """Guarda presupuestos"""
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, "Presupuestos", ["Categoría", "Presupuesto_Mensual"])
                if worksheet:
                    worksheet.clear()
                    worksheet.append_row(["Categoría", "Presupuesto_Mensual"])
                    for _, row in df_pres.iterrows():
                        worksheet.append_row([row['Categoría'], row['Presupuesto_Mensual']])
                    return
            except: pass
    
    df_pres.to_csv(PRESUPUESTOS_FILE, index=False)

# --- FUNCIONES DE IMPORTACIÓN CSV ---
def importar_desde_csv(uploaded_file, mapeo_columnas):
    """Importa movimientos desde un archivo CSV de banco"""
    try:
        # Leer CSV con diferentes encodings
        for encoding in ['utf-8', 'latin-1', 'iso-8859-1', 'cp1252']:
            try:
                df_import = pd.read_csv(uploaded_file, encoding=encoding, sep=';' if ';' in str(uploaded_file.read(1000)) else ',')
                uploaded_file.seek(0)
                break
            except:
                uploaded_file.seek(0)
                continue
        
        # Aplicar mapeo de columnas
        df_import.columns = df_import.columns.str.strip()
        df_nuevo = pd.DataFrame()
        
        for col_destino, col_origen in mapeo_columnas.items():
            if col_origen and col_origen in df_import.columns:
                df_nuevo[col_destino] = df_import[col_origen]
        
        # Convertir fechas
        if 'Fecha' in df_nuevo.columns:
            df_nuevo['Fecha'] = pd.to_datetime(df_nuevo['Fecha'], dayfirst=True, errors='coerce')
        
        # Convertir importes
        if 'Importe' in df_nuevo.columns:
            df_nuevo['Importe'] = pd.to_numeric(df_nuevo['Importe'].astype(str).str.replace(',', '.').str.replace('€', '').str.strip(), errors='coerce')
        
        # Agregar columnas faltantes
        for col in COLUMNS:
            if col not in df_nuevo.columns:
                if col == 'Tipo':
                    df_nuevo['Tipo'] = df_nuevo['Importe'].apply(lambda x: 'Ingreso' if x > 0 else 'Gasto')
                elif col == 'Frecuencia':
                    df_nuevo['Frecuencia'] = 'Puntual'
                elif col == 'Impacto_Mensual':
                    df_nuevo['Impacto_Mensual'] = df_nuevo['Importe']
                elif col == 'Es_Conjunto':
                    df_nuevo['Es_Conjunto'] = False
                elif col == 'Categoría':
                    df_nuevo['Categoría'] = 'Otros'
        
        return df_nuevo[COLUMNS].dropna(subset=['Fecha', 'Importe'])
    except Exception as e:
        st.error(f"Error importando CSV: {str(e)}")
        return pd.DataFrame()

# --- FUNCIONES DE RECORDATORIOS ---
def get_recordatorios_recurrentes(df_rec):
    """Obtiene recordatorios de gastos recurrentes pendientes"""
    recordatorios = []
    hoy = datetime.now().date()
    
    for _, rec in df_rec.iterrows():
        if rec['Frecuencia'] == 'Mensual':
            # Recordar el día 25 de cada mes
            if hoy.day >= 25:
                recordatorios.append({
                    'tipo': rec['Tipo'],
                    'categoria': rec['Categoría'],
                    'concepto': rec['Concepto'],
                    'importe': rec['Importe'],
                    'mensaje': f"Recordatorio: {rec['Concepto']} ({rec['Importe']:.2f} €) - Mensual"
                })
        elif rec['Frecuencia'] == 'Anual':
            # Recordar en el mes correspondiente
            recordatorios.append({
                'tipo': rec['Tipo'],
                'categoria': rec['Categoría'],
                'concepto': rec['Concepto'],
                'importe': rec['Importe'],
                'mensaje': f"Recordatorio: {rec['Concepto']} ({rec['Importe']:.2f} €) - Anual"
            })
    
    return recordatorios

# --- FUNCIONES DE BACKUP E HISTORIAL ---
def crear_backup(df):
    """Crea un backup de los datos"""
    if not os.path.exists(BACKUP_DIR):
        os.makedirs(BACKUP_DIR)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_file = os.path.join(BACKUP_DIR, f"backup_{timestamp}.csv")
    df_backup = df.copy()
    df_backup['Fecha'] = df_backup['Fecha'].dt.strftime("%d/%m/%Y")
    df_backup.to_csv(backup_file, index=False)
    return backup_file

def registrar_cambio(tipo_cambio, descripcion, usuario="Sistema"):
    """Registra un cambio en el historial"""
    if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
        sheet = get_google_sheet()
        if sheet:
            try:
                worksheet = get_or_create_worksheet(sheet, "Historial", ["Fecha", "Tipo", "Descripcion", "Usuario"])
                if worksheet:
                    worksheet.append_row([
                        datetime.now().strftime("%d/%m/%Y %H:%M:%S"),
                        tipo_cambio,
                        descripcion,
                        usuario
                    ])
                    return
            except: pass
    
    if not os.path.exists(HISTORIAL_FILE):
        pd.DataFrame(columns=["Fecha", "Tipo", "Descripcion", "Usuario"]).to_csv(HISTORIAL_FILE, index=False)
    
    try:
        df_hist = pd.read_csv(HISTORIAL_FILE)
        df_hist = pd.concat([df_hist, pd.DataFrame([{
            "Fecha": datetime.now().strftime("%d/%m/%Y %H:%M:%S"),
            "Tipo": tipo_cambio,
            "Descripcion": descripcion,
            "Usuario": usuario
        }])], ignore_index=True)
        df_hist.to_csv(HISTORIAL_FILE, index=False)
    except: pass

# --- FUNCIONES DE INTELIGENCIA ---
def analizar_patrones(df):
    """Analiza patrones en los gastos"""
    if df.empty:
        return {}
    
    df_gastos = df[df['Tipo'] == 'Gasto'].copy()
    if df_gastos.empty:
        return {}
    
    # Gastos por día de la semana
    df_gastos['Dia_Semana'] = df_gastos['Fecha'].dt.day_name()
    gastos_por_dia = df_gastos.groupby('Dia_Semana')['Importe'].sum().to_dict()
    
    # Categorías más gastadas
    top_categorias = df_gastos.groupby('Categoría')['Importe'].sum().sort_values(ascending=False).head(5).to_dict()
    
    # Promedio mensual
    promedio_mensual = df_gastos.groupby(df_gastos['Fecha'].dt.to_period('M'))['Importe'].sum().mean()
    
    # Detección de gastos inusuales (más de 2 desviaciones estándar)
    media = df_gastos['Importe'].mean()
    std = df_gastos['Importe'].std()
    umbral = media + (2 * std)
    gastos_inusuales = df_gastos[df_gastos['Importe'] > umbral].copy()
    
    return {
        'gastos_por_dia': gastos_por_dia,
        'top_categorias': top_categorias,
        'promedio_mensual': promedio_mensual,
        'gastos_inusuales': gastos_inusuales,
        'media_gasto': media,
        'desviacion': std
    }

def generar_recomendaciones(df, presupuestos, patrones):
    """Genera recomendaciones basadas en el análisis"""
    recomendaciones = []
    now = datetime.now()
    df_mes = df[(df['Fecha'].dt.month == now.month) & (df['Fecha'].dt.year == now.year)]
    
    # Comparar con presupuestos
    if not presupuestos.empty:
        for _, presup in presupuestos.iterrows():
            cat = presup['Categoría']
            presup_mes = presup['Presupuesto_Mensual']
            gasto_mes = df_mes[(df_mes['Categoría'] == cat) & (df_mes['Tipo'] == 'Gasto')]['Importe'].sum()
            
            if gasto_mes > presup_mes * 0.9:
                porcentaje = (gasto_mes / presup_mes) * 100
                recomendaciones.append({
                    'tipo': 'warning' if porcentaje < 100 else 'error',
                    'mensaje': f"⚠️ {cat}: Has gastado {gasto_mes:.2f} € de {presup_mes:.2f} € ({porcentaje:.1f}%)"
                })
    
    # Gastos inusuales
    if 'gastos_inusuales' in patrones and not patrones['gastos_inusuales'].empty:
        for _, gasto in patrones['gastos_inusuales'].head(3).iterrows():
            recomendaciones.append({
                'tipo': 'info',
                'mensaje': f"💡 Gasto inusual detectado: {gasto['Concepto']} ({gasto['Importe']:.2f} €)"
            })
    
    return recomendaciones

# --- FUNCIONES DE GEMINI AI ---
def preparar_contexto_financiero(df, df_presupuestos=None):
    """Prepara un resumen estructurado de los datos financieros para Gemini"""
    if df.empty:
        return "No hay datos financieros disponibles."
    
    now = datetime.now()
    df_mes = df[(df['Fecha'].dt.month == now.month) & (df['Fecha'].dt.year == now.year)]
    df_mes_anterior = df[(df['Fecha'].dt.month == (now.month - 1) % 12 + (1 if now.month == 1 else 0)) & 
                         (df['Fecha'].dt.year == (now.year if now.month > 1 else now.year - 1))]
    
    # Ingresos y gastos
    ingresos_mes = df_mes[df_mes['Tipo'] == "Ingreso"]['Importe'].sum()
    gastos_mes = df_mes[df_mes['Tipo'] == "Gasto"]['Importe'].sum()
    ingresos_mes_anterior = df_mes_anterior[df_mes_anterior['Tipo'] == "Ingreso"]['Importe'].sum() if not df_mes_anterior.empty else 0
    gastos_mes_anterior = df_mes_anterior[df_mes_anterior['Tipo'] == "Gasto"]['Importe'].sum() if not df_mes_anterior.empty else 0
    
    # Gastos por categoría del mes actual
    gastos_por_categoria = df_mes[df_mes['Tipo'] == 'Gasto'].groupby('Categoría')['Importe'].sum().to_dict()
    
    # Gastos recurrentes
    df_recurrentes = df[df['Frecuencia'].isin(['Mensual', 'Anual'])]
    gastos_recurrentes = df_recurrentes.groupby(['Categoría', 'Concepto'])['Importe'].sum().to_dict()
    
    # Top gastos del mes
    top_gastos = df_mes[df_mes['Tipo'] == 'Gasto'].nlargest(5, 'Importe')[['Concepto', 'Categoría', 'Importe']].to_dict('records')
    
    # Promedio mensual histórico
    n_meses = max(len(df['Fecha'].dt.to_period('M').unique()), 1)
    gasto_promedio_historico = df[df['Tipo'] == "Gasto"]['Impacto_Mensual'].sum() / n_meses
    ingreso_promedio_historico = df[df['Tipo'] == "Ingreso"].groupby(df['Fecha'].dt.to_period('M'))['Importe'].sum().mean() if not df[df['Tipo'] == "Ingreso"].empty else 0
    
    contexto = f"""
RESUMEN FINANCIERO DEL MES ACTUAL ({MESES_ES_DICT[now.month]} {now.year}):

INGRESOS:
- Ingresos del mes actual: {ingresos_mes:,.2f} €
- Ingresos del mes anterior: {ingresos_mes_anterior:,.2f} €
- Promedio mensual histórico: {ingreso_promedio_historico:,.2f} €

GASTOS:
- Gastos del mes actual: {gastos_mes:,.2f} €
- Gastos del mes anterior: {gastos_mes_anterior:,.2f} €
- Promedio mensual histórico: {gasto_promedio_historico:,.2f} €
- Balance del mes: {ingresos_mes - gastos_mes:,.2f} €

GASTOS POR CATEGORÍA (MES ACTUAL):
"""
    for categoria, importe in sorted(gastos_por_categoria.items(), key=lambda x: x[1], reverse=True):
        contexto += f"- {categoria}: {importe:,.2f} €\n"
    
    if top_gastos:
        contexto += "\nTOP 5 GASTOS MÁS ALTOS DEL MES:\n"
        for i, gasto in enumerate(top_gastos, 1):
            contexto += f"{i}. {gasto['Concepto']} ({gasto['Categoría']}): {gasto['Importe']:,.2f} €\n"
    
    if df_presupuestos is not None and not df_presupuestos.empty:
        contexto += "\nPRESUPUESTOS MENSUALES:\n"
        for _, presup in df_presupuestos.iterrows():
            if presup['Presupuesto_Mensual'] > 0:
                gasto_cat = gastos_por_categoria.get(presup['Categoría'], 0)
                porcentaje = (gasto_cat / presup['Presupuesto_Mensual']) * 100 if presup['Presupuesto_Mensual'] > 0 else 0
                contexto += f"- {presup['Categoría']}: {gasto_cat:,.2f} € / {presup['Presupuesto_Mensual']:,.2f} € ({porcentaje:.1f}%)\n"
    
    contexto += f"\nTOTAL DE REGISTROS: {len(df)} movimientos"
    contexto += f"\nRANGO DE FECHAS: {df['Fecha'].min().strftime('%d/%m/%Y')} - {df['Fecha'].max().strftime('%d/%m/%Y')}"
    
    return contexto

def chat_con_gemini(pregunta, contexto_financiero, historial_chat=None):
    """Envía una pregunta a Gemini con el contexto financiero"""
    if not GEMINI_ENABLED:
        return "Gemini no está configurado. Por favor, configura GEMINI_API_KEY en las variables de entorno."
    
    # Asegurar que el modelo esté inicializado
    if GEMINI_MODEL is None:
        inicializar_gemini()
        if GEMINI_MODEL is None:
            return "Error: No se pudo inicializar un modelo de Gemini compatible. Verifica tu API key y que tengas acceso a los modelos."
    
    try:
        # Construir el prompt con contexto
        prompt = f"""Eres un asistente financiero experto y amigable. El usuario tiene preguntas sobre sus finanzas personales.

CONTEXTO FINANCIERO ACTUAL:
{contexto_financiero}

INSTRUCCIONES:
- Responde en español de manera clara y concisa
- Usa los datos proporcionados para dar respuestas precisas
- Si la pregunta requiere cálculos, hazlos con los datos disponibles
- Sé proactivo y ofrece recomendaciones útiles cuando sea apropiado
- Si falta información para responder, indícalo claramente
- Formatea los números con 2 decimales y el símbolo € cuando sea apropiado

PREGUNTA DEL USUARIO:
{pregunta}

RESPUESTA:"""
        
        # Generar respuesta
        response = GEMINI_MODEL.generate_content(prompt)
        return response.text
    except Exception as e:
        return f"Error al comunicarse con Gemini: {str(e)}. Por favor, verifica tu API key y conexión."

# --- ESTADO SESIÓN ---
if 'simulacion' not in st.session_state: 
    st.session_state.simulacion = []
if 'chat_history' not in st.session_state:
    st.session_state.chat_history = []
if 'chat_input_key' not in st.session_state:
    st.session_state.chat_input_key = 0
if 'show_modal' not in st.session_state:
    st.session_state.show_modal = False
if 'seccion_actual' not in st.session_state:
    st.session_state.seccion_actual = "🤖 Asesor"

# --- CARGA ---
df = load_data()
df_rec = load_recurrentes()
lista_cats = load_categories()

# --- SIDEBAR ---
st.sidebar.header("📝 Gestión de Movimientos")

# El modo simulación ahora está en el formulario
if 'modo_simulacion' not in st.session_state:
    st.session_state.modo_simulacion = False

if st.session_state.modo_simulacion and len(st.session_state.simulacion) > 0:
    st.sidebar.info(f"Items simulados: {len(st.session_state.simulacion)}")

# RECORDATORIOS DE GASTOS RECURRENTES
recordatorios = get_recordatorios_recurrentes(df_rec)
if recordatorios:
    st.sidebar.markdown("---")
    st.sidebar.markdown("**🔔 Recordatorios:**")
    for rec in recordatorios[:3]:  # Mostrar máximo 3
        st.sidebar.caption(f"💡 {rec['mensaje']}")

# El botón de alta está ahora en el header superior

# Modal/Popup para el formulario
if st.session_state.show_modal:
    # Usar un contenedor con estilo de modal
    st.markdown("""
    <style>
    .modal-wrapper {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.7);
        z-index: 99999;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 1rem;
    }
    .modal-inner {
        background: var(--background-color);
        border-radius: 1rem;
        padding: 2rem;
        max-width: 600px;
        width: 100%;
        max-height: 90vh;
        overflow-y: auto;
        box-shadow: 0 10px 40px rgba(0,0,0,0.5);
        position: relative;
    }
    </style>
    <div class="modal-wrapper">
        <div class="modal-inner">
    """, unsafe_allow_html=True)
    
    st.markdown("### 📝 Nuevo Movimiento")
        
        # Formulario inteligente y adaptativo dentro del modal
        with st.form("form_reg_modal", clear_on_submit=True):
            # Primera fila: Modo Simulación y Tipo
            col_sim, col_tipo = st.columns([1, 2])
            with col_sim:
                modo_simulacion = st.checkbox("🧪 Simulación", help="Prueba sin guardar", value=st.session_state.modo_simulacion, key="modo_sim_modal")
                st.session_state.modo_simulacion = modo_simulacion
            with col_tipo:
                tipo = st.radio("Tipo", ["Ingreso", "Gasto"], index=1, horizontal=True)
            
            # Segunda fila: Gasto Conjunto
            es_conjunto = st.checkbox("👥 Gasto Conjunto (Dividir entre 2)", key="es_conjunto_modal")
            
            # Tercera fila: Fecha y Categoría
            col_fecha, col_cat = st.columns(2)
            with col_fecha:
                fecha = st.date_input(
                    "📅 Fecha", 
                    datetime.now(), 
                    format="DD/MM/YYYY",
                    key="fecha_input_modal"
                )
            with col_cat:
                cat = st.selectbox("Categoría", lista_cats, key="cat_select_modal")
            
            # Cuarta fila: Concepto (ancho completo)
            con = st.text_input("Concepto", key="concepto_input_modal", placeholder="Descripción del movimiento")
            
            # Quinta fila: Importe y Frecuencia
            col_imp, col_fre = st.columns([2, 1])
            with col_imp:
                imp_input = st.number_input(
                    "Importe Total (€)", 
                    min_value=0.0, 
                    step=0.01, 
                    format="%.2f",
                    key="importe_input_modal"
                )
            with col_fre:
                fre = st.selectbox(
                    "Frecuencia", 
                    ["Puntual", "Mensual", "Anual"],
                    key="frecuencia_select_modal"
                )
            
            # Mostrar cálculo si es conjunto
            imp_real = imp_input / 2 if es_conjunto and tipo == "Gasto" else imp_input
            if es_conjunto and tipo == "Gasto" and imp_input > 0:
                st.info(f"ℹ️ Se registrarán **{imp_real:.2f} €** (mitad del total)")

            # Botones de acción
            btn = "➕ Añadir a Simulación" if modo_simulacion else "💾 Guardar"
            
            col_submit, col_cancel = st.columns([2, 1])
            with col_submit:
                submitted = st.form_submit_button(btn, type="primary", use_container_width=True)
            with col_cancel:
                if st.form_submit_button("❌ Cancelar", use_container_width=True):
                    st.session_state.show_modal = False
                    st.rerun()
            
            if submitted:
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
                        st.session_state.show_modal = False
                        st.success("Añadido a simulación")
                        st.rerun()
                    else:
                        # LÓGICA DE GUARDADO REAL
                        new_row = pd.DataFrame([[pd.to_datetime(fecha), tipo, cat, con, imp_real, fre, impacto, es_conjunto]], columns=COLUMNS)
                        df = pd.concat([df, new_row], ignore_index=True)
                        save_all_data(df)
                        registrar_cambio("Alta", f"Nuevo movimiento: {con} ({imp_real:.2f} €)")
                        st.session_state.show_modal = False
                        st.success("Guardado")
                        st.rerun()
                else: 
                    st.error("Faltan datos")
        
        st.markdown("---")
        # Botón para cerrar el modal (fuera del form)
        if st.button("❌ Cerrar", use_container_width=True, key="close_modal_btn"):
            st.session_state.show_modal = False
            st.rerun()
    
    st.markdown("</div></div>", unsafe_allow_html=True)

# --- HEADER SUPERIOR CON BOTÓN DE ALTA Y MENÚ HAMBURGER ---
st.markdown('<div class="top-header">', unsafe_allow_html=True)
col_header1, col_header2, col_header3 = st.columns([3, 1, 1])
with col_header1:
    # Título dinámico según sección
    titulos_secciones = {
        "🤖 Asesor": "🤖 Asesor Financiero",
        "📊 Gráficos": "📊 Visualizaciones",
        "🔍 Tabla": "🔍 Tabla de Movimientos",
        "🔄 Recurrentes": "🔄 Gastos Recurrentes",
        "📝 Editar": "📝 Editar Movimientos",
        "📤 Exportar/Importar": "📤 Exportar / Importar",
        "💰 Presupuestos": "💰 Presupuestos",
        "⚙️ Config": "⚙️ Configuración"
    }
    titulo_actual = titulos_secciones.get(st.session_state.seccion_actual, "🚀 Finanzas Personales")
    st.markdown(f"### {titulo_actual}")
with col_header2:
    # Botón de nuevo movimiento más pequeño e intuitivo
    if st.button("➕ Nuevo", type="primary", use_container_width=True, key="btn_alta_header"):
        st.session_state.show_modal = True
        st.rerun()
with col_header3:
    # Menú hamburger usando popover de Streamlit
    opciones_menu = ["🤖 Asesor", "📊 Gráficos", "🔍 Tabla", "🔄 Recurrentes", "📝 Editar", "📤 Exportar/Importar", "💰 Presupuestos", "⚙️ Config"]
    
    with st.popover("☰ Menú", use_container_width=True):
        st.markdown("**Navegación:**")
        for opcion in opciones_menu:
            if st.button(opcion, key=f"menu_{opcion}", use_container_width=True):
                st.session_state.seccion_actual = opcion
                st.rerun()

st.markdown('</div>', unsafe_allow_html=True)

# JavaScript para recibir mensajes del menú
st.components.v1.html("""
<script>
    window.addEventListener('message', function(event) {
        if (event.data.type === 'selectSection') {
            // Esto se manejará con Streamlit
            window.parent.postMessage({type: 'streamlit', section: event.data.section}, '*');
        }
    });
</script>
""", height=0, key="menu_js")

# --- DASHBOARD ---
if df.empty: 
    st.info("Empieza añadiendo movimientos.")
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

    # Mostrar sección según selección del menú
    seccion_actual = st.session_state.seccion_actual
    
    # --- SECCIÓN: ASESOR INTELIGENTE & SIMULACIÓN ---
    if seccion_actual == "🤖 Asesor":
        # Cargar presupuestos y analizar patrones
        df_presupuestos = load_presupuestos()
        patrones = analizar_patrones(df)
        recomendaciones = generar_recomendaciones(df, df_presupuestos, patrones)
        
        # 1. PARTE SUPERIOR: DATOS REALES
        c1, c2, c3 = st.columns(3)
        ahorro_real = ingresos - gasto_pro
        with c1:
            st.metric("Ingresos Mes", f"{ingresos:,.2f} €")
            st.metric("Gasto Promedio", f"{gasto_pro:,.2f} €")
            st.metric("Capacidad Ahorro", f"{ahorro_real:,.2f} €")
        with c2:
            st.metric("🐷 Hucha Anuales (Mes)", f"{prov_anual:,.2f} €", help="Ahorra esto cada mes")
            if patrones and 'promedio_mensual' in patrones:
                st.metric("📊 Promedio Mensual", f"{patrones['promedio_mensual']:,.2f} €")
        with c3:
            st.metric("👥 Acumulado Conjunto", f"{total_conjunto:,.2f} €")
        
        # RECOMENDACIONES INTELIGENTES
        if recomendaciones:
            st.markdown("---")
            st.subheader("💡 Recomendaciones Inteligentes")
            for rec in recomendaciones[:5]:  # Mostrar máximo 5
                if rec['tipo'] == 'error':
                    st.error(rec['mensaje'])
                elif rec['tipo'] == 'warning':
                    st.warning(rec['mensaje'])
                else:
                    st.info(rec['mensaje'])
        
        # ANÁLISIS DE PATRONES
        if patrones:
            st.markdown("---")
            st.subheader("📈 Análisis de Patrones")
            
            col_pat1, col_pat2 = st.columns(2)
            with col_pat1:
                if 'top_categorias' in patrones and patrones['top_categorias']:
                    st.markdown("**🏆 Top 5 Categorías con Más Gasto:**")
                    for cat, importe in list(patrones['top_categorias'].items())[:5]:
                        st.write(f"- {cat}: {importe:,.2f} €")
            
            with col_pat2:
                if 'gastos_por_dia' in patrones and patrones['gastos_por_dia']:
                    st.markdown("**📅 Gastos por Día de la Semana:**")
                    dias_orden = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
                    dias_es = {'Monday': 'Lunes', 'Tuesday': 'Martes', 'Wednesday': 'Miércoles', 
                              'Thursday': 'Jueves', 'Friday': 'Viernes', 'Saturday': 'Sábado', 'Sunday': 'Domingo'}
                    for dia in dias_orden:
                        if dia in patrones['gastos_por_dia']:
                            st.write(f"- {dias_es.get(dia, dia)}: {patrones['gastos_por_dia'][dia]:,.2f} €")
        
        # 2. CHAT CON GEMINI AI
        st.markdown("---")
        st.subheader("🤖 Asistente IA con Gemini")
        st.caption("Haz preguntas en lenguaje natural sobre tus finanzas y recibe respuestas inteligentes")
        
        if GEMINI_ENABLED and GEMINI_MODEL is not None:
            # Preparar contexto financiero
            contexto_financiero = preparar_contexto_financiero(df, df_presupuestos)
            
            # Mostrar historial de chat
            if st.session_state.chat_history:
                st.markdown("**💬 Conversación:**")
                for mensaje in st.session_state.chat_history[-10:]:  # Mostrar últimos 10 mensajes
                    if mensaje['tipo'] == 'usuario':
                        with st.chat_message("user"):
                            st.write(mensaje['contenido'])
                    else:
                        with st.chat_message("assistant"):
                            st.write(mensaje['contenido'])
            
            # Campo de entrada para preguntas
            st.markdown("**💭 Haz una pregunta sobre tus finanzas:**")
            pregunta = st.text_input(
                "Ejemplos: '¿Cuánto he gastado en comida este mes?', '¿Cuál es mi categoría con más gastos?', '¿Cómo van mis presupuestos?'",
                key=f"pregunta_gemini_{st.session_state.chat_input_key}",
                label_visibility="collapsed"
            )
            
            col_ask, col_clear = st.columns([3, 1])
            with col_ask:
                if st.button("💬 Enviar Pregunta", type="primary", use_container_width=True):
                    if pregunta.strip():
                        with st.spinner("🤔 Pensando..."):
                            respuesta = chat_con_gemini(pregunta, contexto_financiero)
                            
                            # Guardar en historial
                            st.session_state.chat_history.append({
                                'tipo': 'usuario',
                                'contenido': pregunta
                            })
                            st.session_state.chat_history.append({
                                'tipo': 'asistente',
                                'contenido': respuesta
                            })
                            # Incrementar key para limpiar el campo
                            st.session_state.chat_input_key += 1
                        st.rerun()
            
            with col_clear:
                if st.button("🗑️ Limpiar Chat", use_container_width=True):
                    st.session_state.chat_history = []
                    st.session_state.chat_input_key += 1  # También limpiar el input
                    st.rerun()
            
            # Sugerencias de preguntas
            with st.expander("💡 Preguntas sugeridas"):
                sugerencias = [
                    "¿Cuánto he gastado este mes?",
                    "¿Cuál es mi categoría con más gastos?",
                    "¿Cómo van mis presupuestos?",
                    "¿En qué categoría debería ahorrar más?",
                    "¿Cuánto puedo ahorrar este mes?",
                    "Compara mis gastos de este mes con el anterior",
                    "¿Qué gastos recurrentes tengo?",
                    "Dame recomendaciones para mejorar mis finanzas"
                ]
                cols_sug = st.columns(2)
                for i, sug in enumerate(sugerencias):
                    with cols_sug[i % 2]:
                        if st.button(sug, key=f"sug_{i}", use_container_width=True):
                            # Simular pregunta
                            with st.spinner("🤔 Pensando..."):
                                respuesta = chat_con_gemini(sug, contexto_financiero)
                                st.session_state.chat_history.append({
                                    'tipo': 'usuario',
                                    'contenido': sug
                                })
                                st.session_state.chat_history.append({
                                    'tipo': 'asistente',
                                    'contenido': respuesta
                                })
                            st.rerun()
        else:
            st.warning("⚠️ Gemini no está configurado. Para activar el asistente IA:")
            st.info("""
            1. Obtén una API key de Google AI Studio: https://makersuite.google.com/app/apikey
            2. Agrega la variable de entorno: `GEMINI_API_KEY=tu_api_key`
            3. En Streamlit Cloud, ve a Settings → Secrets y agrega:
               ```
               GEMINI_API_KEY=tu_api_key_aqui
               ```
            """)
            if not GEMINI_AVAILABLE:
                st.error("❌ La librería `google-generativeai` no está instalada. Ejecuta: `pip install google-generativeai`")
        
        # 3. PARTE INFERIOR: ZONA DE SIMULACIÓN
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

    # --- SECCIÓN: GRÁFICOS ---
    elif seccion_actual == "📊 Gráficos":
        st.subheader("📊 Visualizaciones Avanzadas")
        
        tipo_visualizacion = st.selectbox(
            "Selecciona el tipo de visualización:",
            ["Evolución Temporal", "Distribución por Categorías", "Gráfico de Sankey (Flujo)", 
             "Gráfico de Burbujas", "Calendario de Gastos", "Heatmap por Día de Semana"]
        )
        
        if tipo_visualizacion == "Evolución Temporal":
            df_ev = df.groupby([df['Fecha'].dt.to_period('M'), 'Tipo'])['Importe'].sum().reset_index()
            df_ev['Mes'] = df_ev['Fecha'].dt.to_timestamp().apply(formatear_periodo_es)
            fig = px.bar(df_ev.sort_values("Fecha"), x='Mes', y='Importe', color='Tipo', barmode='group',
                         color_discrete_map={'Ingreso': '#00CC96', 'Gasto': '#EF553B'},
                         title="Evolución de Ingresos y Gastos")
            st.plotly_chart(fig, use_container_width=True)
        
        elif tipo_visualizacion == "Distribución por Categorías":
            df_cat = df[df['Tipo'] == 'Gasto'].groupby('Categoría')['Importe'].sum().reset_index()
            df_cat = df_cat.sort_values('Importe', ascending=False)
            
            col_pie, col_bar = st.columns(2)
            with col_pie:
                fig_pie = px.pie(df_cat, values='Importe', names='Categoría', 
                                title="Distribución de Gastos por Categoría")
                st.plotly_chart(fig_pie, use_container_width=True)
            with col_bar:
                fig_bar = px.bar(df_cat, x='Categoría', y='Importe', 
                                title="Gastos por Categoría", color='Importe',
                                color_continuous_scale='Reds')
                fig_bar.update_xaxes(tickangle=45)
                st.plotly_chart(fig_bar, use_container_width=True)
        
        elif tipo_visualizacion == "Gráfico de Sankey (Flujo)":
            # Crear flujo: Ingresos -> Categorías -> Ahorro
            df_gastos = df[df['Tipo'] == 'Gasto'].copy()
            if not df_gastos.empty:
                ingresos_total = df[df['Tipo'] == 'Ingreso']['Importe'].sum()
                gastos_total = df_gastos['Importe'].sum()
                ahorro = ingresos_total - gastos_total
                
                # Preparar datos para Sankey
                gastos_por_cat = df_gastos.groupby('Categoría')['Importe'].sum().to_dict()
                
                # Crear nodos y enlaces
                nodes = ['Ingresos'] + list(gastos_por_cat.keys()) + ['Ahorro']
                node_indices = {node: i for i, node in enumerate(nodes)}
                
                # Enlaces desde Ingresos a Categorías
                links_source = []
                links_target = []
                links_value = []
                links_label = []
                
                for cat, valor in gastos_por_cat.items():
                    links_source.append(node_indices['Ingresos'])
                    links_target.append(node_indices[cat])
                    links_value.append(valor)
                    links_label.append(f"{valor:,.2f} €")
                
                # Enlace desde Ingresos a Ahorro
                if ahorro > 0:
                    links_source.append(node_indices['Ingresos'])
                    links_target.append(node_indices['Ahorro'])
                    links_value.append(ahorro)
                    links_label.append(f"{ahorro:,.2f} €")
                
                fig_sankey = go.Figure(data=[go.Sankey(
                    node=dict(
                        pad=15,
                        thickness=20,
                        line=dict(color="black", width=0.5),
                        label=nodes,
                        color=["#00CC96"] + ["#EF553B"] * len(gastos_por_cat) + ["#FFA726"]
                    ),
                    link=dict(
                        source=links_source,
                        target=links_target,
                        value=links_value,
                        label=links_label
                    )
                )])
                
                fig_sankey.update_layout(title_text="Flujo de Dinero: Ingresos → Gastos → Ahorro", 
                                        font_size=10, height=600)
                st.plotly_chart(fig_sankey, use_container_width=True)
            else:
                st.info("No hay suficientes datos para el gráfico de Sankey")
        
        elif tipo_visualizacion == "Gráfico de Burbujas":
            df_gastos = df[df['Tipo'] == 'Gasto'].copy()
            if not df_gastos.empty:
                df_burb = df_gastos.groupby(['Categoría', df_gastos['Fecha'].dt.to_period('M')])['Importe'].sum().reset_index()
                df_burb['Fecha'] = pd.to_datetime(df_burb['Fecha'].astype(str))
                df_burb['Mes'] = df_burb['Fecha'].apply(formatear_periodo_es)
                
                fig_burb = px.scatter(df_burb, x='Mes', y='Categoría', size='Importe', 
                                     color='Importe', hover_data=['Importe'],
                                     title="Gastos por Categoría y Mes (Tamaño = Importe)",
                                     color_continuous_scale='Reds',
                                     size_max=50)
                fig_burb.update_layout(height=500)
                st.plotly_chart(fig_burb, use_container_width=True)
            else:
                st.info("No hay datos de gastos para mostrar")
        
        elif tipo_visualizacion == "Calendario de Gastos":
            df_gastos = df[df['Tipo'] == 'Gasto'].copy()
            if not df_gastos.empty:
                df_gastos['Año'] = df_gastos['Fecha'].dt.year
                df_gastos['Mes'] = df_gastos['Fecha'].dt.month
                df_gastos['Dia'] = df_gastos['Fecha'].dt.day
                df_gastos['Dia_Semana'] = df_gastos['Fecha'].dt.day_name()
                
                # Crear heatmap por día del mes
                pivot_cal = df_gastos.groupby(['Año', 'Mes', 'Dia'])['Importe'].sum().reset_index()
                pivot_cal['Fecha_Str'] = pivot_cal.apply(lambda x: f"{x['Año']}-{x['Mes']:02d}-{x['Dia']:02d}", axis=1)
                
                fig_cal = px.scatter(pivot_cal, x='Dia', y='Mes', size='Importe', 
                                    color='Importe', hover_data=['Fecha_Str', 'Importe'],
                                    title="Calendario de Gastos (Tamaño = Importe del día)",
                                    color_continuous_scale='Reds',
                                    size_max=30)
                fig_cal.update_yaxes(title="Mes", tickmode='linear', dtick=1)
                fig_cal.update_xaxes(title="Día del Mes", tickmode='linear', dtick=1)
                st.plotly_chart(fig_cal, use_container_width=True)
            else:
                st.info("No hay datos de gastos para mostrar")
        
        elif tipo_visualizacion == "Heatmap por Día de Semana":
            df_gastos = df[df['Tipo'] == 'Gasto'].copy()
            if not df_gastos.empty:
                df_gastos['Dia_Semana'] = df_gastos['Fecha'].dt.day_name()
                df_gastos['Mes'] = df_gastos['Fecha'].dt.month
                df_gastos['Mes_Nombre'] = df_gastos['Mes'].map(MESES_ES_DICT)
                
                dias_orden = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']
                dias_es = {'Monday': 'Lunes', 'Tuesday': 'Martes', 'Wednesday': 'Miércoles', 
                          'Thursday': 'Jueves', 'Friday': 'Viernes', 'Saturday': 'Sábado', 'Sunday': 'Domingo'}
                
                pivot_heat = df_gastos.groupby(['Dia_Semana', 'Mes_Nombre'])['Importe'].sum().reset_index()
                pivot_heat = pivot_heat.pivot(index='Dia_Semana', columns='Mes_Nombre', values='Importe').fillna(0)
                
                # Reordenar filas
                pivot_heat = pivot_heat.reindex([d for d in dias_orden if d in pivot_heat.index])
                pivot_heat.index = [dias_es.get(d, d) for d in pivot_heat.index]
                
                fig_heat = px.imshow(pivot_heat, labels=dict(x="Mes", y="Día de la Semana", color="Importe (€)"),
                                    title="Heatmap: Gastos por Día de la Semana y Mes",
                                    color_continuous_scale='Reds', aspect="auto")
                st.plotly_chart(fig_heat, use_container_width=True, height=400)
            else:
                st.info("No hay datos de gastos para mostrar")

    # --- SECCIÓN: TABLA ---
    elif seccion_actual == "🔍 Tabla":
        st.dataframe(df.style.format({"Fecha": lambda t: t.strftime("%d/%m/%Y"), "Importe": "{:,.2f} €"}), use_container_width=True)

    # --- SECCIÓN: RECURRENTES ---
    elif seccion_actual == "🔄 Recurrentes":
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

    # --- SECCIÓN: EDITAR ---
    elif seccion_actual == "📝 Editar":
        st.subheader("📝 Editar Movimientos")
        st.caption("Edita los movimientos directamente en la tabla y haz clic en 'Guardar Cambios'")
        
        # Preparar DataFrame para edición
        df_edit = df.copy()
        df_edit['Fecha'] = df_edit['Fecha'].dt.date  # Convertir a date para el editor
        
        edited_df = st.data_editor(
            df_edit, 
            num_rows="dynamic", 
            use_container_width=True,
            key="editor_movimientos",
            column_config={
                "Fecha": st.column_config.DateColumn(
                    "Fecha", 
                    format="DD/MM/YYYY",
                    required=True
                ),
                "Tipo": st.column_config.SelectboxColumn(
                    "Tipo",
                    options=["Ingreso", "Gasto"],
                    required=True
                ),
                "Categoría": st.column_config.SelectboxColumn(
                    "Categoría",
                    options=lista_cats,
                    required=True
                ),
                "Concepto": st.column_config.TextColumn(
                    "Concepto",
                    required=True
                ),
                "Importe": st.column_config.NumberColumn(
                    "Importe (€)",
                    min_value=0.0,
                    step=0.01,
                    format="%.2f",
                    required=True
                ),
                "Frecuencia": st.column_config.SelectboxColumn(
                    "Frecuencia",
                    options=["Mensual", "Anual", "Puntual"],
                    required=True
                ),
                "Impacto_Mensual": st.column_config.NumberColumn(
                    "Impacto Mensual (€)",
                    min_value=0.0,
                    step=0.01,
                    format="%.2f"
                ),
                "Es_Conjunto": st.column_config.CheckboxColumn(
                    "Es Conjunto"
                )
            }
        )
        
        col_btn1, col_btn2 = st.columns(2)
        with col_btn1:
            if st.button("💾 Guardar Cambios", type="primary", use_container_width=True):
                # Convertir fecha de date a datetime
                edited_df['Fecha'] = pd.to_datetime(edited_df['Fecha'])
                # Recalcular Impacto_Mensual
                edited_df['Impacto_Mensual'] = edited_df.apply(
                    lambda x: x['Importe']/12 if x['Frecuencia']=="Anual" else x['Importe'], 
                    axis=1
                )
                cambios = len(edited_df) - len(df)
                save_all_data(edited_df)
                registrar_cambio("Edición", f"Editados {len(edited_df)} movimientos ({cambios:+d} cambios)")
                st.success("✅ Cambios guardados correctamente")
                st.rerun()
        with col_btn2:
            if st.button("🔄 Recargar", use_container_width=True):
                st.rerun()

    # --- SECCIÓN: EXPORTAR/IMPORTAR ---
    elif seccion_actual == "📤 Exportar/Importar":
        st.subheader("📤 Exportar / 📥 Importar Datos")
        
        tab_exp, tab_imp = st.tabs(["📤 Exportar", "📥 Importar"])
        
        with tab_exp:
            col_exp1, col_exp2 = st.columns(2)
            
            with col_exp1:
                st.markdown("**Exportar como CSV**")
                csv = df.to_csv(index=False).encode('utf-8-sig')
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
                    output = BytesIO()
                    with pd.ExcelWriter(output, engine='openpyxl') as writer:
                        df.to_excel(writer, index=False, sheet_name='Finanzas')
                    output.seek(0)
                    excel_data = output.getvalue()
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
            st.info(f"Se exportarán {len(df)} registros.")
        
        with tab_imp:
            st.markdown("### 📥 Importar desde CSV de Banco")
            st.caption("Sube un archivo CSV exportado de tu banco y mapea las columnas")
            
            uploaded_file = st.file_uploader("Selecciona archivo CSV", type=['csv'], key="csv_uploader")
            
            if uploaded_file is not None:
                try:
                    # Leer muestra del archivo
                    sample = pd.read_csv(uploaded_file, nrows=5, encoding='utf-8')
                    if sample.empty:
                        sample = pd.read_csv(uploaded_file, nrows=5, encoding='latin-1', sep=';')
                    
                    st.markdown("**Vista previa del archivo (primeras 5 filas):**")
                    st.dataframe(sample, use_container_width=True)
                    
                    st.markdown("**Mapeo de columnas:**")
                    col_map1, col_map2 = st.columns(2)
                    
                    with col_map1:
                        fecha_col = st.selectbox("Columna de Fecha", ["(Seleccionar)"] + list(sample.columns), key="map_fecha")
                        importe_col = st.selectbox("Columna de Importe", ["(Seleccionar)"] + list(sample.columns), key="map_importe")
                        concepto_col = st.selectbox("Columna de Concepto/Descripción", ["(Seleccionar)"] + list(sample.columns), key="map_concepto")
                    
                    with col_map2:
                        categoria_col = st.selectbox("Columna de Categoría (opcional)", ["(Ninguna)"] + list(sample.columns), key="map_categoria")
                        tipo_col = st.selectbox("Columna de Tipo (opcional)", ["(Ninguna)"] + list(sample.columns), key="map_tipo")
                    
                    if st.button("📥 Importar Datos", type="primary", use_container_width=True):
                        if fecha_col != "(Seleccionar)" and importe_col != "(Seleccionar)":
                            uploaded_file.seek(0)
                            mapeo = {
                                'Fecha': fecha_col if fecha_col != "(Seleccionar)" else None,
                                'Importe': importe_col if importe_col != "(Seleccionar)" else None,
                                'Concepto': concepto_col if concepto_col != "(Seleccionar)" else None,
                                'Categoría': categoria_col if categoria_col != "(Ninguna)" else None,
                                'Tipo': tipo_col if tipo_col != "(Ninguna)" else None
                            }
                            
                            df_importado = importar_desde_csv(uploaded_file, mapeo)
                            
                            if not df_importado.empty:
                                # Mostrar vista previa
                                st.success(f"✅ Se importaron {len(df_importado)} movimientos")
                                st.dataframe(df_importado.head(10), use_container_width=True)
                                
                                if st.button("💾 Confirmar e Importar", type="primary"):
                                    df = pd.concat([df, df_importado], ignore_index=True)
                                    save_all_data(df)
                                    registrar_cambio("Importación", f"Importados {len(df_importado)} movimientos desde CSV")
                                    st.success("✅ Datos importados correctamente")
                                    st.rerun()
                            else:
                                st.error("❌ No se pudieron importar los datos. Verifica el formato del archivo.")
                        else:
                            st.error("❌ Debes seleccionar al menos Fecha e Importe")
                except Exception as e:
                    st.error(f"Error leyendo archivo: {str(e)}")
                    st.caption("Tip: Asegúrate de que el archivo sea CSV válido con separador de coma o punto y coma")
    
    # --- SECCIÓN: PRESUPUESTOS ---
    elif seccion_actual == "💰 Presupuestos":
        st.subheader("💰 Presupuestos Mensuales")
        st.caption("Establece presupuestos por categoría y recibe alertas cuando te acerques al límite")
        
        df_presupuestos = load_presupuestos()
        
        # Agregar nuevas categorías si no están en presupuestos
        for cat in lista_cats:
            if cat not in df_presupuestos['Categoría'].values if not df_presupuestos.empty else True:
                df_presupuestos = pd.concat([df_presupuestos, pd.DataFrame([{
                    'Categoría': cat,
                    'Presupuesto_Mensual': 0.0
                }])], ignore_index=True)
        
        edited_pres = st.data_editor(
            df_presupuestos[df_presupuestos['Categoría'].isin(lista_cats)],
            num_rows="dynamic",
            use_container_width=True,
            key="editor_presupuestos",
            column_config={
                "Categoría": st.column_config.SelectboxColumn(
                    "Categoría",
                    options=lista_cats,
                    required=True
                ),
                "Presupuesto_Mensual": st.column_config.NumberColumn(
                    "Presupuesto Mensual (€)",
                    min_value=0.0,
                    step=10.0,
                    format="%.2f",
                    required=True
                )
            }
        )
        
        if st.button("💾 Guardar Presupuestos", type="primary", use_container_width=True):
            save_presupuestos(edited_pres)
            st.success("✅ Presupuestos guardados")
            st.rerun()
        
        st.markdown("---")
        
        # Mostrar estado de presupuestos
        if not edited_pres.empty and not df.empty:
            st.subheader("📊 Estado de Presupuestos del Mes Actual")
            now = datetime.now()
            df_mes = df[(df['Fecha'].dt.month == now.month) & (df['Fecha'].dt.year == now.year)]
            
            for _, presup in edited_pres.iterrows():
                if presup['Presupuesto_Mensual'] > 0:
                    cat = presup['Categoría']
                    presup_mes = presup['Presupuesto_Mensual']
                    gasto_mes = df_mes[(df_mes['Categoría'] == cat) & (df_mes['Tipo'] == 'Gasto')]['Importe'].sum()
                    porcentaje = (gasto_mes / presup_mes) * 100 if presup_mes > 0 else 0
                    restante = presup_mes - gasto_mes
                    
                    col_pres1, col_pres2, col_pres3 = st.columns(3)
                    with col_pres1:
                        st.metric(f"{cat}", f"{gasto_mes:,.2f} €", f"de {presup_mes:,.2f} €")
                    with col_pres2:
                        st.progress(min(porcentaje / 100, 1.0))
                        st.caption(f"{porcentaje:.1f}% utilizado")
                    with col_pres3:
                        if porcentaje >= 100:
                            st.error(f"⚠️ Excedido por {abs(restante):,.2f} €")
                        elif porcentaje >= 90:
                            st.warning(f"⚠️ Quedan {restante:,.2f} €")
                        else:
                            st.success(f"✅ Quedan {restante:,.2f} €")
    
    # --- SECCIÓN: CONFIGURACIÓN ---
    elif seccion_actual == "⚙️ Config":
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
        
        # Configuración de Gemini AI
        st.markdown("### 🤖 Asistente IA con Gemini")
        
        if GEMINI_ENABLED and GEMINI_MODEL is not None:
            st.success("✅ Gemini está activo y listo para responder tus preguntas")
            st.info("💡 Ve a la pestaña 'Asesor' para chatear con el asistente IA")
        else:
            st.warning("⚠️ Gemini no está configurado")
            st.markdown("""
            **Para activar el asistente IA de Gemini:**
            
            1. Obtén una API key gratuita de Google AI Studio:
               - Visita: https://makersuite.google.com/app/apikey
               - O: https://aistudio.google.com/app/apikey
               - Inicia sesión con tu cuenta de Google
               - Crea una nueva API key
            
            2. Configura la API key:
               - **En Streamlit Cloud:** Ve a Settings → Secrets y agrega:
                 ```
                 GEMINI_API_KEY=tu_api_key_aqui
                 ```
               - **En local:** Crea un archivo `.streamlit/secrets.toml` con:
                 ```
                 GEMINI_API_KEY = "tu_api_key_aqui"
                 ```
            
            3. Reinicia la aplicación
            
            **Características del asistente:**
            - Responde preguntas sobre tus gastos e ingresos
            - Analiza tus datos financieros en tiempo real
            - Ofrece recomendaciones personalizadas
            - Compara meses y categorías
            - Ayuda con la gestión de presupuestos
            """)
            if not GEMINI_AVAILABLE:
                st.error("❌ La librería no está instalada. Instala: `pip install google-generativeai`")
        
        st.markdown("---")
        
        # Configuración de Google Sheets
        st.markdown("### ☁️ Google Drive / Sheets")
        
        if GOOGLE_SHEETS_ENABLED and GSPREAD_AVAILABLE:
            sheet = get_google_sheet()
            if sheet:
                st.success("✅ Google Sheets conectado correctamente")
                st.info(f"📊 Libro: **{sheet.title}**")
                st.caption("Tus datos se están guardando automáticamente en Google Drive")
            else:
                st.warning("⚠️ Google Sheets no está configurado correctamente")
                st.markdown("""
                **Para configurar Google Sheets:**
                
                1. Crea un proyecto en [Google Cloud Console](https://console.cloud.google.com/)
                2. Habilita la API de Google Sheets y Google Drive
                3. Crea una cuenta de servicio y descarga el archivo JSON de credenciales
                4. Crea una hoja de cálculo en Google Sheets y compártela con el email de la cuenta de servicio
                5. En Streamlit Cloud, agrega estas variables de entorno:
                   - `GOOGLE_SHEETS_ENABLED=true`
                   - `GOOGLE_SHEET_ID=tu_id_de_la_hoja`
                   - `GOOGLE_CREDENTIALS_JSON=contenido_del_json_como_texto`
                """)
        else:
            st.info("💡 **Google Sheets no está habilitado**")
            st.markdown("""
            **Actualmente los datos se guardan solo localmente. Para habilitar Google Sheets:**
            
            1. Instala las dependencias: `pip install gspread google-auth`
            2. Configura las credenciales de Google Cloud
            3. En Streamlit Cloud, configura las variables de entorno:
               - `GOOGLE_SHEETS_ENABLED=true`
               - `GOOGLE_SHEET_ID=tu_id_de_la_hoja`
               - `GOOGLE_CREDENTIALS_JSON=contenido_del_json`
            
            **📝 Estado actual:**
            - Almacenamiento: Archivo local (se pierde al reiniciar Streamlit Cloud)
            - Recomendado: Configurar Google Sheets para persistencia permanente
            """)
        
        st.markdown("---")
        
        # Seguridad y Backup
        st.markdown("### 🔒 Seguridad y Backup")
        
        col_back1, col_back2 = st.columns(2)
        with col_back1:
            if st.button("💾 Crear Backup", use_container_width=True):
                backup_file = crear_backup(df)
                registrar_cambio("Backup", f"Backup creado: {backup_file}")
                st.success(f"✅ Backup creado: {backup_file}")
        
        with col_back2:
            if os.path.exists(BACKUP_DIR):
                backups = [f for f in os.listdir(BACKUP_DIR) if f.startswith('backup_') and f.endswith('.csv')]
                if backups:
                    st.caption(f"📁 {len(backups)} backups disponibles")
                    backup_seleccionado = st.selectbox("Restaurar desde backup:", backups, key="select_backup")
                    if st.button("🔄 Restaurar Backup", use_container_width=True):
                        try:
                            df_backup = pd.read_csv(os.path.join(BACKUP_DIR, backup_seleccionado))
                            df_backup['Fecha'] = pd.to_datetime(df_backup['Fecha'], dayfirst=True, errors='coerce')
                            save_all_data(df_backup)
                            registrar_cambio("Restauración", f"Restaurado desde: {backup_seleccionado}")
                            st.success("✅ Backup restaurado correctamente")
                            st.rerun()
                        except Exception as e:
                            st.error(f"Error restaurando backup: {str(e)}")
        
        st.markdown("---")
        
        # Historial de Cambios
        st.markdown("### 📜 Historial de Cambios")
        if os.path.exists(HISTORIAL_FILE):
            try:
                df_hist = pd.read_csv(HISTORIAL_FILE)
                if not df_hist.empty:
                    st.dataframe(df_hist.tail(20), use_container_width=True, hide_index=True)
                else:
                    st.info("No hay historial de cambios aún")
            except:
                st.info("No hay historial de cambios aún")
        else:
            st.info("No hay historial de cambios aún")
        
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
