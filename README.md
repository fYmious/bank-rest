# Система управления банковскими картами

REST API на Spring Boot для управления банковскими картами с JWT авторизацией, шифрованием номеров карт и ролевым доступом.

## Быстрый старт

### Запуск через Docker Compose

```bash
docker-compose up -d
```

Приложение будет доступно на `http://localhost:8080`.

### Запуск вручную

1. Запустите PostgreSQL:
```bash
docker run -d --name postgres \
  -e POSTGRES_DB=bankcards \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:15
```

2. Соберите и запустите приложение:
```bash
mvn clean package -DskipTests
java -jar target/bankcards-0.0.1-SNAPSHOT.jar
```

## API документация

После запуска Swagger UI доступен по адресу:  
`http://localhost:8080/swagger-ui.html`

## Аутентификация

Все запросы (кроме `/api/auth/**`) требуют JWT токен в заголовке:
```
Authorization: Bearer <token>
```

**Дефолтный администратор:**
- Username: `admin`
- Password: `admin123`

## Роли

| Роль | Возможности |
|------|-------------|
| `ROLE_ADMIN` | Создание/блокировка/активация/удаление карт; управление пользователями; просмотр всех карт |
| `ROLE_USER` | Просмотр своих карт; запрос блокировки; переводы между своими картами |

## Основные эндпоинты

### Аутентификация
- `POST /api/auth/register` — регистрация
- `POST /api/auth/login` — вход

### Карты (пользователь)
- `GET /api/cards` — мои карты (фильтр по статусу, пагинация)
- `GET /api/cards/{id}` — карта по id
- `POST /api/cards/{id}/request-block` — запросить блокировку
- `POST /api/cards/transfer` — перевод между своими картами

### Карты (администратор)
- `POST /api/admin/cards` — создать карту
- `GET /api/admin/cards` — все карты
- `PATCH /api/admin/cards/{id}/block` — заблокировать
- `PATCH /api/admin/cards/{id}/activate` — активировать
- `DELETE /api/admin/cards/{id}` — удалить

### Пользователи (администратор)
- `GET /api/admin/users` — все пользователи
- `PATCH /api/admin/users/{id}/role` — изменить роль
- `DELETE /api/admin/users/{id}` — удалить

## Безопасность

- Номера карт хранятся в зашифрованном виде (AES)
- Отображаются только в маскированном виде: `**** **** **** 1234`
- Пароли хешируются через BCrypt
- Доступ контролируется по ролям через Spring Security

## Тесты 

```bash
mvn test
```
