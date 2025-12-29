# race-log

Репозиторий содержит backend‑модуль на Spring Boot и инфраструктуру для локального запуска через Docker Compose.  
**jar собирается локально**, а Docker использует уже готовый jar из `infra/backend`.

---

## Структура репозитория

```
.
├── settings.gradle                # корневой gradle-проект
├── race-log-backend/              # Spring Boot backend (модуль)
└── infra/
    ├── docker-compose.yml         # Postgres + backend
    └── backend/
        ├── Dockerfile             # runtime-образ, берет готовый jar
        └── race-log-backend-0.0.1.jar
```

---

## Запуск через Docker Compose

Запускать удобнее из `infra/`:

```bash
cd infra
docker compose up --build
```

После запуска:
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

---

## Backend: технологии и ключевые компоненты

Backend — REST API на **Spring Boot** с:
- **Spring Web** — контроллеры REST.
- **Spring Security** — защита эндпоинтов.
- **JWT (jjwt)** — access/refresh токены.
- **Spring Data JPA + PostgreSQL** — хранение данных.
- **Flyway** — миграции БД (создание схемы + наполнения дисциплин).
- **Bucket4j** — rate limit по IP.
- **Springdoc OpenAPI** — Swagger UI / OpenAPI спека.
- **Actuator** — health endpoint.

---

## Пакеты backend

### `racelog`
Корневой пакет приложения.

- `RaceLogApplication` — точка входа (`main`), старт Spring Boot.

---

### `racelog.config`
Конфигурация и фильтры инфраструктуры.

- `SecurityConfig`  
  Настраивает Spring Security:
    - отключение CSRF (актуально для stateless API),
    - stateless sessions (`SessionCreationPolicy.STATELESS`),
    - public endpoints (swagger, auth, health),
    - подключение JWT фильтра до `UsernamePasswordAuthenticationFilter`.

- `JwtProperties`  
  Бин с параметрами JWT из `application.yml` (`app.jwt.*`).

- `IpRateLimitFilter`  
  Rate limiting по IP на основе Bucket4j.  
  Хранит ведра в `ConcurrentHashMap<String, Bucket>`. При превышении — HTTP `429` и JSON с сообщением.

---

### `racelog.auth`
Регистрация/логин и обновление токенов.

- `AuthController` (`/api/v1/auth/...`)  
  REST‑входная точка для регистрации и логина.

- `AuthService`  
  Логика:
    - старт регистрации: генерируется код (4 цифры), сохраняется в БД и «отправляется» через `SmsService`,
    - подтверждение регистрации: проверка кода, создание пользователя, выдача access/refresh,
    - login: проверка пароля, выдача access/refresh,
    - refresh: валидация refresh‑токена и выпуск нового access.

#### `racelog.auth.dto`
DTO для запросов/ответов:
- `RegisterStartRequest`, `RegisterConfirmRequest`, `RegisterStartResponse`
- `LoginRequest`, `LoginResponse`
- `TokenRefreshRequest`

#### `racelog.auth.jwt`
JWT реализация.

- `JwtService`  
  Генерация access/refresh токенов + парсинг/валидация.
    - access: subject = userId, claim `role`
    - refresh: subject = userId, claim `type=refresh`

- `JwtAuthenticationFilter`  
  Достаёт `Bearer ...` из `Authorization`, парсит JWT, поднимает `Authentication` в `SecurityContextHolder`.

#### `racelog.auth.sms`
SMS‑код и отправка.

- `SmsCode` — сущность кода подтверждения (phone, code, expiresAt, used).
- `SmsCodeRepository` — поиск актуального неиспользованного кода.
- `SmsService` — интерфейс отправки.
- `SmsServiceFakeImpl` — «фейковая» отправка (логирует код).

---

### `racelog.user`
Пользователи и профиль.

- `User` — сущность пользователя:
    - `phone`, `login`, `passwordHash`, `role`, `info`
    - `createdAt`, `updatedAt`

- `UserRole` — роли: `ATHLETE`, `COACH`

- `UserRepository` — поиск по телефону/логину, поиск атлетов по подстроке логина.

- `UserController` (`/api/v1/users/...`)
    - `/me` — информация о текущем пользователе,
    - `/{id}` — профиль по id,
    - `/search?query=` — поиск атлетов по логину.

#### `racelog.user.dto`
- `UserMeResponse`
- `UserProfileResponse`
- `UserSearchItem`

---

### `racelog.group`
Группы тренера/участники/статистика.

- `Group` — сущность группы: name, description, inviteCode, coach, timestamps.
- `GroupMember` — связь many‑to‑many (group_id, user_id, joined_at) через `@IdClass`.
- `GroupRepository` — поиск групп тренера, поиск групп участника, поиск по invite‑коду.
- `GroupMemberRepository` — участники группы, проверка членства.
- `GroupService`
    - создать группу (только COACH),
    - получить группы пользователя (coach/athlete логика различается),
    - вступить по invite‑коду (проверка дубля),
    - список участников,
    - статистика по дисциплине: best/last результаты по каждому атлету.

- `GroupController` (`/api/v1/groups/...`)
    - `POST /` — создать группу,
    - `GET /` — мои группы,
    - `POST /join` — вступить,
    - `GET /{groupId}/members` — участники,
    - `GET /{groupId}/stats/discipline/{disciplineId}` — статистика.

#### `racelog.group.dto`
`GroupDtos` — набор record‑DTO:
- Create / Join request
- Group dto
- Member dto
- Discipline stat dto

---

### `racelog.discipline`
Справочник дисциплин.

- `Discipline` — сущность (code, name)
- `DisciplineRepository`
- `DisciplineController`  
  `GET /api/v1/disciplines` — получить список дисциплин.

---

### `racelog.result`
Результаты спортсмена.

- `Result` — сущность результата:
    - athlete, discipline
    - `resultValue` (строка), `resultNumeric` (double, например секунды)
    - competitionName, place, date, info

- `ResultRepository` — выборки по атлету/дисциплине и для списка атлетов.

- `ResultTimeParser`  
  Преобразует строку результата в секунды.
  Поддерживает:
    - `59.87`
    - `4:12.35`
    - `1:02:15.3`

- `ResultService`
    - получение результатов (опционально фильтр по дисциплине),
    - добавление результата (только ATHLETE и только себе),
    - вычисление `resultNumeric` через `ResultTimeParser`.

- `ResultController`
    - `GET /api/v1/athletes/{athleteId}/results?disciplineId=...`
    - `POST /api/v1/athletes/{athleteId}/results`

#### `racelog.result.dto`
`ResultDtos` — record‑DTO:
- CreateRequest
- ResultDto
- ResultCreatedDto

---

### `racelog.common`
Общие ошибки и обработчик исключений.

- `NotFoundException`, `ForbiddenException`
- `ApiError` — единый формат ответа об ошибке.
- `GlobalExceptionHandler`  
  Преобразует исключения в HTTP ответы:
    - 404 / 403 / 400 + валидация (`MethodArgumentNotValidException`) в единый `ApiError`.

---

## Конфигурация приложения

Файл: `race-log-backend/src/main/resources/application.yml`

- Порт: `server.port: 8080`
- Datasource: Postgres (url/user/pass)
- Flyway: включён, миграции из `classpath:db/migration`
- JWT:
    - `app.jwt.secret` — берётся из `APP_JWT_SECRET` (или дефолт)
    - `access-token-minutes`, `refresh-token-days`

---

## Миграции БД (Flyway)

Путь: `race-log-backend/src/main/resources/db/migration`

- `V1__init_schema.sql` — создание таблиц (`users`, `groups`, `group_members`, `disciplines`, `results`, `sms_codes`)
- `V2__...sql` — наполнения справочника дисциплин
