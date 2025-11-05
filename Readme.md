# TPO Final - eScrims: Plataforma de eSports

Este proyecto es la entrega final para la materia de **Análisis y Diseño Orientado a Objetos (ADOO)** de la Universidad Argentina de la Empresa (UADE).

**Integrantes del Grupo:**
*   [Nombre Completo del Integrante 1] - LU: [Número de LU]
*   [Nombre Completo del Integrante 2] - LU: [Número de LU]
*   ... (Añadir más integrantes si es necesario)

---

## 📖 Descripción del Proyecto

**eScrims** es el backend de una plataforma diseñada para facilitar la organización de *scrims* (partidas de práctica) y partidas amistosas para diversos videojuegos de eSports. La aplicación permite a los jugadores crear, buscar y unirse a partidas, gestionando todo el ciclo de vida del encuentro, desde la formación de equipos hasta el registro de estadísticas finales.

El sistema está construido siguiendo una arquitectura de microservicios robusta y escalable, con un enfoque en el diseño de software de alta calidad mediante la aplicación de patrones de diseño GoF.

### ✨ Características Principales

*   **Autenticación Segura:** Sistema de registro y login basado en tokens **JWT (JSON Web Tokens)**.
*   **Creación de Scrims:** Los usuarios pueden crear partidas personalizadas, definiendo juego, formato, región y requisitos de rango.
*   **Ciclo de Vida Completo:** Gestión automática y manual del estado de un scrim (`Buscando Jugadores`, `Lobby Armado`, `Confirmado`, `En Juego`, `Finalizado`, `Cancelado`) mediante el **Patrón State**.
*   **Sistema de Lobbies y Equipos:** Los creadores pueden aceptar o rechazar postulantes para formar los equipos. El creador se une automáticamente a su propia partida.
*   **MMR Dinámico:** Sistema de **recalculo de rango (MMR)** que se ajusta automáticamente según el resultado de cada scrim, promoviendo un matchmaking equilibrado.
*   **Notificaciones Desacopladas:** Implementación del **Patrón Observer** para notificar a los jugadores sobre eventos clave (ej. lobby lleno), utilizando un sistema flexible basado en los patrones **Abstract Factory** y **Adapter**.
*   **Scheduler Automático:** Un planificador de tareas (`@Scheduled`) se encarga de iniciar las partidas automáticamente cuando llega la fecha y hora programada.
*   **Registro de Estadísticas:** Permite cargar los resultados finales de la partida, incluyendo MVP y KDA de los jugadores.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** Java 17
*   **Framework:** Spring Boot 3.x
*   **Seguridad:** Spring Security (Autenticación con JWT)
*   **Base de Datos:** Spring Data JPA con Hibernate
*   **Base de Datos en Memoria:** H2 Database (para desarrollo y pruebas)
*   **Gestión de Dependencias:** Maven
*   **Pruebas:** JUnit 5, Mockito, Spring Boot Test (MockMvc)

## 📐 Patrones de Diseño Implementados

Este proyecto aplica varios patrones de diseño para asegurar un código desacoplado, mantenible y escalable:

1.  **State:** Gestiona el complejo ciclo de vida del `Scrim`, encapsulando el comportamiento específico de cada estado en su propia clase.
2.  **Observer:** Desacopla la lógica de negocio de las notificaciones. El sistema publica eventos (ej. `ScrimStateChangedEvent`) y los suscriptores reaccionan a ellos.
3.  **Abstract Factory:** Proporciona una interfaz para crear familias de objetos relacionados (`Notifiers`) sin especificar sus clases concretas. Se implementó una `DevNotifierFactory` para logging.
4.  **Adapter:** Permite que diferentes implementaciones de notificadores (ej. un logger de consola) se ajusten a una interfaz común (`Notifier`).
5.  **(MVC) Model-View-Controller:** La arquitectura general de la aplicación sigue los principios de MVC, separando la lógica de negocio (Modelo), la exposición de la API (Controlador) y la representación de datos (Vista, a través de DTOs).

## 🚀 Cómo Ejecutar el Proyecto

Para levantar el servidor backend, sigue estos pasos:

### Pre-requisitos
*   Tener instalado **Java JDK 17** o superior.
*   Tener instalado **Apache Maven**.

### Pasos
1.  **Clonar el repositorio:**
    ```bash
    git clone [URL_DEL_REPOSITORIO]
    cd [NOMBRE_DE_LA_CARPETA]
    ```

2.  **Compilar y ejecutar la aplicación con Maven:**
    Desde la carpeta raíz del proyecto, ejecuta el siguiente comando en tu terminal:
    ```bash
    mvn spring-boot:run
    ```

3.  ¡Listo! El servidor se iniciará y estará escuchando en `http://localhost:8080`.

4.  **Acceder a la Base de Datos en Memoria (H2 Console):**
    Puedes inspeccionar la base de datos en tiempo real accediendo a la siguiente URL en tu navegador:
    *   **URL:** `http://localhost:8080/h2-console`
    *   **JDBC URL:** `jdbc:h2:mem:escrimsdb`
    *   **User Name:** `sa`
    *   **Password:** `password`

## 📡 Guía Rápida de la API

Puedes usar una herramienta como [Postman](https://www.postman.com/) para interactuar con la API.

*   `POST /api/auth/register` - Registra un nuevo usuario.
*   `POST /api/auth/login` - Inicia sesión y obtiene un token JWT.
*   `POST /api/scrims` - Crea un nuevo scrim (requiere token de autenticación).
*   `GET /api/scrims` - Lista todos los scrims disponibles (requiere token).
*   `POST /api/scrims/{id}/postulations` - Permite a un usuario postularse a un scrim (requiere token).
*   `POST /api/scrims/{scrimId}/postulations/{postId}/accept` - Acepta una postulación (solo para el creador).
*   `POST /api/scrims/{id}/confirmations` - Confirma la participación en un lobby lleno (requiere token).
*   `POST /api/scrims/{id}/finalize` - Finaliza una partida y carga las estadísticas (solo para el creador).
*   `POST /api/scrims/{id}/cancelar` - **(Nuevo)** Cancela un scrim antes de que comience (solo para el creador).
*   `GET /api/scrims/{id}/estadisticas` - **(Nuevo)** Obtiene las estadísticas de un scrim finalizado (requiere token).