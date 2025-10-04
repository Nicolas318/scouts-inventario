# InventarioTiendas

Aplicación Android para la gestión de un inventario de tiendas de campaña utilizadas por un grupo scout. Permite registrar, consultar y actualizar información sobre cada tienda, incluyendo su estado, tipo, número de piquetas y gomas, fecha de revisión y posibles reparaciones.

## ✨ Características

- **Inicio de sesión con Firebase Authentication**
- **Listado dinámico de tiendas con búsqueda integrada**
- **Creación, edición y eliminación de tiendas**
- **Visualización del estado de cada tienda con colores distintivos**
- **Selección de reparaciones necesarias y anotaciones sobre imágenes**
- **Persistencia en Firebase Firestore con sincronización en tiempo real**

## 🛠️ Tecnologías utilizadas

- Kotlin
- Android SDK (View Binding y Jetpack Compose para `MainActivity`)
- Firebase (Auth + Firestore)
- Material Design
- RecyclerView
- Anotaciones personalizadas sobre imágenes (`AnnotationView`)

## 📱 Capturas de pantalla

_¡Próximamente!_

## 🧩 Estructura del proyecto

```
InventarioTiendas/
│
├── LoginActivity.kt             # Pantalla de autenticación de usuario
├── ListaTiendasActivity.kt      # Listado principal con buscador y acceso a detalles
├── DetalleTiendaActivity.kt     # Formulario para crear o editar una tienda
├── TiendaAdapter.kt             # Adaptador para RecyclerView
├── Tienda.kt                    # Modelo de datos para tienda de campaña
├── MainActivity.kt              # Pantalla de ejemplo con Jetpack Compose
└── ui/
    └── AnnotationView.kt        # Vista personalizada para anotar sobre imágenes
```

## 📦 Instalación y ejecución

1. Clona este repositorio:
   ```bash
   git clone https://github.com/tuusuario/InventarioTiendas.git
   ```

2. Abre el proyecto en **Android Studio**.

3. Configura Firebase:
   - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
   - Descarga el archivo `google-services.json` y colócalo en `app/`.

4. Ejecuta la aplicación en un emulador o dispositivo físico.

## ✅ Tareas pendientes

- [ ] Subir capturas de pantalla
- [ ] Validaciones más estrictas en formularios
- [ ] Mejora del diseño visual con Jetpack Compose
- [ ] Internacionalización (soporte multiidioma)

## 🤝 Contribuciones

¡Se agradecen contribuciones! Puedes abrir issues o pull requests con mejoras o correcciones.

## 📄 Licencia

Este proyecto está licenciado bajo la [MIT License](LICENSE).
