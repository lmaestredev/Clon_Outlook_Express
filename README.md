# Clon Outlook Express

Este proyecto es un cliente de correo electrónico de escritorio inspirado en Outlook Express, desarrollado en Java con Swing y persistencia en H2 Database. Permite gestionar correos internos, contactos y borradores de manera sencilla y eficiente.

## Estructura del Proyecto

- **src/models/**: Modelos de dominio (`User`, `Mail`, `UserMail`, `Contact`).
- **src/controllers/**: Lógica de control y orquestación de la app.
- **src/persistence/dao/**: Interfaces DAO para acceso a datos.
- **src/persistence/impl/**: Implementaciones de los DAOs y lógica de persistencia.
- **src/services/**: Servicios de negocio (envío, borradores, etc.).
- **src/ui/**: Interfaz gráfica principal (`MainFrame`) y diálogos (`ComposeMailDialog`, `ContactsDialog`, `LoginDialog`).
- **config/**: Configuración de la base de datos.

## Funcionalidades Principales

### 🔐 Autenticación y Usuarios
- **Sistema de login**: Inicio de sesión por email con validación de credenciales
- **Registro de usuarios**: Creación de nuevas cuentas de usuario
- **Gestión de perfiles**: Cada usuario tiene su propio espacio de trabajo

### 📧 Gestión de Correos
- **Envío de correos**: Composición y envío de mensajes internos
- **Bandeja de entrada**: Visualización y gestión de correos recibidos
- **Correos enviados**: Historial de mensajes enviados
- **Marcado de estado**: Marcar como leído/no leído
- **Borradores**: Guardar y editar correos en progreso

### 👥 Gestión de Contactos
- **Agenda personal**: Lista de contactos por usuario
- **Agregar contactos**: Creación de nuevos contactos
- **Eliminar contactos**: Gestión de la lista de contactos
- **Autocompletado**: Sugerencias automáticas al escribir direcciones

### 💾 Persistencia de Datos
- **Base de datos H2**: Almacenamiento local de todos los datos
- **Persistencia completa**: No se pierden datos al cerrar la aplicación
- **Datos multiusuario**: Separación de datos por usuario

### 🎨 Interfaz de Usuario
- **Interfaz moderna**: Diseño limpio y intuitivo con Swing
- **Diálogos especializados**: Ventanas modales para tareas específicas
- **Navegación fluida**: Transiciones suaves entre funcionalidades

## Tecnologías Utilizadas

- **Java 21+**
- **Swing** (interfaz gráfica)
- **H2 Database** (persistencia local)
- **JDBC** (acceso a base de datos)
- **PlantUML** (para diagramas UML y DER)

## Repositorio

El código fuente se encuentra en:

[https://github.com/lmaestredev/Clon_Outlook_Express](https://github.com/lmaestre/Clon_Outlook_Express)

## Ventajas del proyecto

- **Fácil de usar**: Interfaz intuitiva y moderna.
- **Multiusuario**: Cada usuario tiene su propia bandeja y contactos.
- **Extensible**: Arquitectura limpia y modular, fácil de mantener y ampliar.
- **Ideal para aprendizaje**: Perfecto para practicar Java, Swing, patrones DAO y persistencia.

---

¡Clon Outlook Express es una excelente base para proyectos educativos o para quienes buscan entender cómo funciona un cliente de correo real con persistencia y GUI en Java!
