# Famous Asistente 📱

**Famous Asistente** es un asistente personal y ejecutivo multifuncional para Android, desarrollado con Kotlin y Jetpack Compose. Combina inteligencia artificial ejecutiva con Gemini, reconocimiento y síntesis de voz nativa, base de datos local SQLite con Room, temporizador Pomodoro con respuesta háptica y auditiva, y un rastreador de hábitos con cálculo automático de rachas.

---

## 🚀 Módulos y Funcionalidades

1. **🎙️ Asistente Ejecutivo con IA**:
   - Comandos por voz (`SpeechRecognizer`) y síntesis hablada (`TextToSpeech`).
   - Procesamiento inteligente con Gemini 2.5 Flash para registrar tareas, consultar agenda, tomar notas y configurar temporizadores.
   - Respuestas con sugerencias y acciones rápidas.

2. **✅ Tareas del Día (Room Database)**:
   - Filtros por categoría (`Trabajo`, `Finanzas`, `Campo/Inventario`, `General`, `Salud`, etc.) y estado.
   - Prioridades visuales (`Alta`, `Media`, `Baja`) y fechas de vencimiento.
   - Herramienta **Desglosar con IA**: Convierte cualquier meta o proyecto complejo en 3-4 pasos accionables inmediatos.

3. **🔥 Rastreador de Hábitos**:
   - Registro interactivo de los últimos 7 días con estados de cumplimiento seleccionables.
   - Cálculo automático de rachas diarias (`streak`) con ícono de fuego y metas semanales.
   - Botón de eliminación rápida y modal para nuevos hábitos.

4. **⏱️ Temporizador de Enfoque (Pomodoro)**:
   - Anillo circular interactivo en Canvas: Enfoque Profundo (25m), Pausa Corta (5m) y Pausa Larga (15m).
   - Retroalimentación auditiva y vibración háptica al finalizar cada bloque.
   - Contador de sesiones y minutos concentrados acumulados en el día.

5. **📝 Bloc de Notas Rápidas con IA**:
   - Fijación de notas importantes (`Pin`).
   - Herramientas de IA para notas: Resumir puntos clave, extraer lista de acciones y pulir redacción.
   - Copiado rápido al portapapeles con un solo toque.

6. **📅 Agenda Ejecutiva**:
   - Cronograma diario con horarios de inicio y fin, ubicaciones y temas a tratar.

7. **📊 Métricas y Rendimiento**:
   - Índice de productividad general calculado en tiempo real (0 - 100%).
   - Recomendaciones estratégicas para optimizar el flujo de trabajo diario.

---

## 📦 Descarga e Instalación del APK en tu Teléfono Android

### Opción 1: Descarga directa desde AI Studio
1. En el panel superior derecho de Google AI Studio, haz clic en el menú desplegable **Download / Export** y selecciona **Download APK** (o compila el APK con el botón de compilación).
2. O en el explorador de archivos, ubica el archivo:
   `app/build/outputs/apk/debug/app-debug.apk`
   Haz clic derecho sobre él y pulsa **Descargar**.
3. En tu teléfono Android:
   - Abre el archivo `.apk` descargado.
   - Si tu navegador o gestor de archivos te pide permiso para "Instalar aplicaciones desconocidas", actívalo en Ajustes.
   - Pulsa **Instalar** y abre Famous Asistente.

### Opción 2: Generación automática en GitHub Actions
El repositorio incluye un flujo de trabajo en `.github/workflows/build-apk.yml`.
Cada vez que haces `git push`, GitHub compila automáticamente el APK y lo deja listo para descargar en la pestaña **Actions** > **Famous-Asistente-APK**.
