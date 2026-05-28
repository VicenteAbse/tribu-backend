# CLAUDE.md — tribu-backend

## Comandos

```bash
# Arrancar la app (puerto 8080)
./mvnw spring-boot:run

# Build
./mvnw clean package

# Compilar sin tests
./mvnw compile

# Tests
./mvnw test
```

## Stack

- **Spring Boot 3.2.5 / Java 17**
- **Base de datos:** PostgreSQL local (`tribu_dev`) — ver sección de BD
- **Auth:** JWT stateless con JJWT 0.12.3; token en header `Authorization: Bearer <token>`
- **ORM:** Spring Data JPA / Hibernate con `ddl-auto=update`
- **WebSocket:** STOMP sobre WebSocket nativo (sin SockJS) — `spring-boot-starter-websocket`

## Base de datos local

```
Host: 127.0.0.1 (no localhost — falla en TablePlus)
Puerto: 5432
DB: tribu_dev
Usuario: tribu_user
Contraseña: tribu_pass
```

Iniciar/parar Postgres (Homebrew):
```bash
brew services start postgresql@17
brew services stop postgresql@17   # los datos persisten
```

El schema se actualiza automáticamente al arrancar (`ddl-auto=update`). Las imágenes se guardan en columnas `TEXT` como base64 — no hay almacenamiento de archivos externo.

## Arquitectura

Paquete raíz: `com.groupmatch.app`

```
auth/           → login, registro, JWT filter
user/           → perfil, avatar, grupos del usuario, notificaciones
group/          → grupos, swipe, miembros, join requests, eventos, chat
  service/      → GroupService (toda la lógica de negocio)
  GroupRepository, GroupSwipeRepository, GroupMemberRepository,
  JoinRequestRepository, GroupEventRepository
chat/           → MessageEntity, MessageRepository, ChatController, ChatService,
                   ChatNotificationResponse
notification/   → NotificationEntity, NotificationService
domain/
  user/         → UserEntity (email, password hash, gender, birthDate, searchRadiusKm, dailyLikesLeft, avatarBase64)
  group/        → GroupEntity, GroupMemberEntity, GroupSwipeEntity,
                   JoinRequestEntity, GroupEventEntity
common/exception/ → GlobalExceptionHandler (400 con errores por campo en español)
common/websocket/ → WebSocketConfig, WebSocketAuthChannelInterceptor
```

## Ciclo de vida de un grupo

```
OPEN → ACTIVE (cuando likesCount >= minMembers - 1)
```

- Al activarse: todos los usuarios que dieron like se convierten en miembros y reciben notificación
- El creador también recibe notificación
- Grupos `OPEN` y `ACTIVE` aparecen en discovery; `CLOSED` no aparece (aún no se implementa la transición a CLOSED)
- Discovery excluye grupos donde el usuario ya es miembro o ya hizo swipe

## Sistema de roles

`OWNER > ADMIN > MEMBER`

- **OWNER:** el creador del grupo. Puede promover MEMBERs a ADMIN, degradar ADMINs a MEMBER, silenciar/expulsar a cualquiera
- **ADMIN:** puede crear eventos, aprobar/rechazar join requests, silenciar/expulsar MEMBERs (no puede actuar sobre ADMINs ni el OWNER)
- **MEMBER:** solo puede leer y escribir en el chat

## Swipe en grupos ACTIVE

Cuando un nuevo usuario da like a un grupo ya `ACTIVE`:
- `joinPolicy = OPEN` → se agrega como miembro directamente, recibe notificación
- `joinPolicy = APPROVAL_REQUIRED` → se crea un `JoinRequest` y el creador recibe notificación

## Endpoints principales

```
POST   /auth/register
POST   /auth/login
PUT    /auth/change-password

GET    /users/me
PUT    /users/me
PUT    /users/me/avatar          { imageBase64 }
GET    /users/me/groups
GET    /users/me/notifications

GET    /groups/discover          ?latitude=&longitude=&radiusKm=&category=&page=&size=
POST   /groups
GET    /groups/:id
PUT    /groups/:id
PUT    /groups/:id/cover         { imageBase64 }
POST   /groups/:id/swipe         { liked }

GET    /groups/:id/members
DELETE /groups/:id/members/:memberId
POST   /groups/:id/members/:memberId/promote
POST   /groups/:id/members/:memberId/demote
POST   /groups/:id/members/:memberId/mute

GET    /groups/:id/join-requests
POST   /groups/:id/join-requests/:requestId/approve
POST   /groups/:id/join-requests/:requestId/reject

GET    /groups/:id/events
POST   /groups/:id/events

GET    /groups/:id/messages      ?page=&size=
POST   /groups/:id/messages      { content }   ← también disponible vía STOMP (ver abajo)

PUT    /notifications/:id/read
POST   /support/reports          { message }

WS     /ws                       endpoint STOMP (handshake HTTP → upgrade)
STOMP  /app/group/:id/message    enviar mensaje (desde cliente)
STOMP  /topic/group/:uuid        recibir mensajes en tiempo real (broadcast)
STOMP  /topic/user/:uuid/notifications  recibir notificaciones de nuevos mensajes
```

## Chat en tiempo real (WebSocket + STOMP)

El broker es el **SimpleBroker** de Spring (in-memory). No hay Redis ni broker externo.

**Auth WebSocket:** el cliente manda el JWT en el header `Authorization` del frame STOMP `CONNECT`. `WebSocketAuthChannelInterceptor` lo valida y setea el `Principal` en el `StompHeaderAccessor`. El endpoint `/ws` es público en `SecurityConfig` (el WS maneja su propia auth).

**Flujo al enviar un mensaje** (idéntico vía REST o STOMP):
1. `ChatService.sendMessage()` guarda en BD
2. Broadcast `MessageResponse` → `/topic/group/{groupUuid}` (todos en el chat lo reciben)
3. Por cada miembro del grupo excepto el remitente → `ChatNotificationResponse` a `/topic/user/{userUuid}/notifications` (badge en lista de grupos)

**`ChatNotificationResponse`** es el DTO liviano del topic personal: `groupUuid`, `groupName`, `senderName`, `preview` (máx. 60 chars).

El `@MessageMapping("/group/{groupId}/message")` vive en `ChatController` junto al REST. El `Principal` del argumento es el usuario autenticado vía el interceptor.

## Convenciones

- Mensajes de validación en español (`"El nombre es obligatorio"`)
- Paquetes: `com.groupmatch.app.{feature}` para controllers/DTOs/repositories, `com.groupmatch.app.domain.{feature}` para entidades JPA
- DTOs: `*Request` (entrada + validación) y `*Response` (salida)
- Inyección por constructor en toda la app
- Imágenes como base64 en columnas `TEXT` — sin límite de tamaño en la BD, pero el frontend debería comprimir antes de enviar
