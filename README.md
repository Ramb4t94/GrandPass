# 🚀 GrandPass - Gestión de Acceso y Estacionamiento Inteligente

Bienvenido al repositorio de GrandPass, una solución integral para la gestión inteligente de accesos y estacionamiento. Este proyecto se compone de una aplicación web para administración y un sistema Android para usuarios.

## 🌟 Visión General del Proyecto

GrandPass busca optimizar la experiencia de gestión de espacios y acceso, ofreciendo una plataforma robusta y fácil de usar tanto para administradores como para usuarios finales.

### Componentes Principales:

* **Aplicación Web (React):** Panel de administración para la gestión de usuarios, vehículos, espacios de estacionamiento, registros de acceso, y más.
* **Aplicación Android:** Aplicación móvil para que los usuarios puedan interactuar con el sistema (e.g., reservar espacios, gestionar accesos, ver historial).

## ✨ Características Principales

### Aplicación Web (GRAND-PASS-ADMIN-REACT)

* Gestión de usuarios y perfiles.
* Administración de vehículos y permisos.
* Monitorización en tiempo real de la ocupación de estacionamientos.
* Generación de reportes y estadísticas.
* Configuración de reglas de acceso y seguridad.

### Aplicación Android

* Registro y login de usuarios.
* Visualización de disponibilidad de estacionamiento.
* Reserva y liberación de espacios.
* Acceso mediante credenciales o QR (si aplica).
* Historial de accesos y estacionamiento.

## 🛠️ Tecnologías Utilizadas

### Aplicación Web (GRAND-PASS-ADMIN-REACT)

* **Frontend:** React, JavaScript (o TypeScript si lo estás usando), HTML, CSS.
* **Manejo de estado:** (Por ejemplo: Redux, Context API, Zustand, Recoil)
* **UI Framework:** (Por ejemplo: Material-UI, Ant Design, Bootstrap)

### Aplicación Android

* **Lenguaje:** Kotlin (o Java)
* **Framework:** Android SDK
* **Base de Datos (si aplica en el cliente):** (Por ejemplo: Room, SQLite)
* **Otras librerías:** (Por ejemplo: Retrofit para API, Glide para imágenes, etc.)

### Backend (Asunción - si hay un backend central)

* **Lenguaje/Framework:** (Por ejemplo: Node.js con Express, Python con Django/Flask, Java con Spring Boot, PHP con Laravel, etc.)
* **Base de Datos:** (Por ejemplo: PostgreSQL, MySQL, MongoDB, Firebase)
* **Autenticación:** (Por ejemplo: JWT, OAuth)

## 🚀 Cómo Empezar

Sigue estas instrucciones para poner en marcha el proyecto en tu entorno local.

### Prerrequisitos

Asegúrate de tener instalado lo siguiente:

* **Node.js y npm (para la aplicación web)
* **Android Studio y SDK de Android** (para la aplicación Android)
* **Git**

### Configuración del Proyecto Web (GRAND-PASS-ADMIN-REACT)

1.  Navega a la carpeta de la aplicación web:
    ```bash
    cd GRAND-PASS-ADMIN-REACT
    ```
2.  Instala las dependencias:
    ```bash
    npm install 
    ```
3.  Levantar la pagina web con el siguiente comando
    ```env
    npm rundev
    ```
    
4.  deberia salir el siguiente mensaje apretar ctrl+click sobre la direccion url creada
    ```bash
> vite


  VITE v4.5.13  ready in 467 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h to show help
    ```
    La aplicación se abrirá en tu navegador en `http://localhost:3000` (o el puerto configurado).

### Configuración del Proyecto Android (app)

1.  Abre Android Studio.
2.  Selecciona `Open an Existing Project` y navega hasta la carpeta `GrandPassProyecto/app` (o el nombre real de tu carpeta raíz de Android).
3.  Android Studio debería sincronizar automáticamente el proyecto y descargar las dependencias Gradle.
4.  Si es necesario, crea un archivo `local.properties` en la raíz de tu proyecto Android (la misma carpeta donde está `build.gradle.kts (Project: AppTest)`) con la ruta a tu SDK de Android:
    ```properties
    # local.properties
    sdk.dir=/Users/tu_usuario/Library/Android/sdk # Ejemplo para macOS
    # o sdk.dir=C\:\\Users\\tu_usuario\\AppData\\Local\\Android\\Sdk # Ejemplo para Windows
    ```
    (Asegúrate de que este archivo esté en tu `.gitignore`.)
5.  Ejecuta la aplicación en un emulador o dispositivo conectado.

## 📞 Contacto

* Ramb4t94
* **GitHub:** [Ramb4t94](https://github.com/Ramb4t94)
* **Email:** (Opcional, si quieres incluirlo)

---
