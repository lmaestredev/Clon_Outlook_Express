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

## Funcionalidades Básicas

- **Login y Registro**: Inicio de sesión por email y registro de nuevos usuarios.
- **Gestión de correos**: Enviar, recibir, leer, marcar como leído/no leído, eliminar y ver correos enviados y recibidos.
- **Borradores**: Guardar y editar borradores de correos.
- **Contactos**: Agenda personal de contactos, agregar y eliminar contactos.
- **Persistencia**: Todos los datos se guardan en una base de datos H2 local, asegurando que nada se pierde al cerrar la app.

## Tecnologías Utilizadas

- **Java 21+**
- **Swing** (interfaz gráfica)
- **H2 Database** (persistencia local)
- **JDBC** (acceso a base de datos)
- **PlantUML** (para diagramas UML y DER)

## Repositorio

El código fuente se encuentra en:

[https://github.com/lmaestredev/Clon_Outlook_Express](https://github.com/lmaestre/Clon_Outlook_Express)

## ¿Por qué es atractivo?

- **Fácil de usar**: Interfaz intuitiva y moderna.
- **Multiusuario**: Cada usuario tiene su propia bandeja y contactos.
- **Extensible**: Arquitectura limpia y modular, fácil de mantener y ampliar.
- **Ideal para aprendizaje**: Perfecto para practicar Java, Swing, patrones DAO y persistencia.

---

¡Clon Outlook Express es una excelente base para proyectos educativos o para quienes buscan entender cómo funciona un cliente de correo real con persistencia y GUI en Java!
