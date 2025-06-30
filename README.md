# 🚀 GrandPass - Fomentando la conexión social en adultos mayores

Bienvenido al repositorio de GrandPass. Este proyecto se compone de una aplicación web para administración y un sistema Android para usuarios.

## 🌟 Visión General del Proyecto

GrandPass busca reunir adultos mayores para realizar actividades recreativas, ofreciendo una plataforma robusta y fácil de usar tanto para administradores como para usuarios finales.

### Componentes Principales:

* **Aplicación Web (React):** Panel de administración.
* **Aplicación Android:** Aplicación móvil para que los usuarios puedan interactuar con el sistema.

## ✨ Características Principales

### Aplicación Web (GRAND-PASS-ADMIN-REACT)

* Gestión de usuarios y perfiles.
* Administración de juegos.
* Monitorización en tiempo real de busqueda de partidas.

### Aplicación Android(AppTest)

* Registro y login de usuarios.
* Visualización de actividades.
* Busqueda mediante geolocalizacion.
* Matching de usuarios.

## 🛠️ Tecnologías Utilizadas

### Aplicación Web (GRAND-PASS-ADMIN-REACT)

* **Frontend:** React, JavaScript, HTML, CSS, JSX.
* **Construcción/Bundler:** Vite.
* **Estilos:** Tailwind CSS, PostCSS.
* **Formato de Datos:** JSON.

### Aplicación Android(AppTest)

* **Lenguaje:** Kotlin
* **Framework:** Android SDK
* **Sistema de Construcción:** Gradle.

### Backend

* **Base de Datos:** Firebase Firestore. Permite almacenar, consultar y administrar información de forma sencilla y escalable, utilizando el SDK de Firebase para JavaScript para operaciones como consultas, creación, edición, eliminación y actualización dinámica de datos.


## 🚀 Cómo Empezar

Sigue estas instrucciones para poner en marcha el proyecto en tu entorno local.

### Prerrequisitos

Asegúrate de tener instalado lo siguiente:

* **Node.js(https://nodejs.org/en) y npm** (para la aplicación web)
* **Android Studio y SDK de Android** (para la aplicación Android)
* **Git**

### Configuración del Proyecto Web (GRAND-PASS-ADMIN-REACT)

1.  Navega a la carpeta de la aplicación web:
    ```bash
    cd GRAND-PASS-ADMIN-REACT
    ```
2.  **este paso es para que no haya problema en los siguientes** aplica estos comandos para que no haya problemade permisos
    ```bash
    Set-ExecutionPolicy RemoteSigned -Scope CurrentUser #para hacer un set
    Get-ExecutionPolicy #luego de este si la respuesta es RemoteSigned no deberia haber problemas
    
    ```
3.  Instala las dependencias:
    ```bash
    npm install 
    ```
    
4.  Levantar la pagina web con el siguiente comando
    ```env
    npm rundev
    ```
    
5.  deberia salir el siguiente mensaje apretar ctrl+click sobre la direccion url creada
   ```bash
> grand-pass-admin@0.0.1 dev
> vite
  VITE v4.5.13  ready in 467 ms
  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h to show help
 ```

### Configuración del Proyecto Android (app)

1.  Abre Android Studio.
2.  Selecciona `Open an Existing Project` y navega hasta la carpeta `GrandPassProyecto/AppTest`.
3.  Android Studio debería sincronizar automáticamente el proyecto y descargar las dependencias Gradle.
4.  Si es necesario, crea un archivo `local.properties` en la raíz de tu proyecto Android (la misma carpeta donde está `build.gradle.kts (Project: AppTest)`) con la ruta a tu SDK de Android:
    ```properties
    # local.properties
    sdk.dir=/Users/tu_usuario/Library/Android/sdk # Ejemplo para macOS
    # o sdk.dir=C\:\\Users\\tu_usuario\\AppData\\Local\\Android\\Sdk # Ejemplo para Windows
    ```
    (Asegúrate de que este archivo esté en tu `.gitignore`.)
5.  Ejecuta la aplicación en un emulador o dispositivo conectado.

**Generación del Archivo APK (Depuración)**
Para generar un APK de depuración (usado para pruebas):

En Android Studio, ve a Build > Build Bundle(s) / APK(s) > Build APK(s).

Una vez finalizado, haz clic en "locate" en la notificación de Android Studio para encontrar el archivo APK (generalmente en app/build/outputs/apk/debug/app-debug.apk).

---
