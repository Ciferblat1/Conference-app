# Conference App

Серверное приложение для работы с конференциями. Сейчас проект содержит основу Spring Boot и начальную схему базы данных: пользователей, направлений и заявок. Пользовательский интерфейс и прикладные API пока не реализованы.

## Стек

- Java 25.
- Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA.
- PostgreSQL 17 в Docker.
- Flyway для версионирования схемы базы данных.
- Gradle Wrapper для сборки и запуска.
- Spring Boot Actuator для проверки состояния приложения.

## Требования

- Установленный JDK 25.
- Docker с Docker Compose v2. На Windows можно использовать Docker Desktop с запущенным движком Linux containers.
- Доступ к интернету при первом запуске для загрузки Gradle, зависимостей и образа PostgreSQL.


Проверка инструментов:

```powershell
java -version
docker version
docker compose version
```

## Первый запуск

### 1. Получить проект

```powershell
git clone https://github.com/Ciferblat1/Conference-app.git
cd Conference-app
```

Если репозиторий скачан архивом, откройте терминал в распакованной папке проекта. Все дальнейшие команды выполняются из корня проекта, где находятся `build.gradle` и `docker-compose.yml`.

### 2. Настроить окружение

Создайте `.env` на основе `.env.example`.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```


Пример содержимого `.env`:

```dotenv
POSTGRES_USER=user
POSTGRES_PASSWORD=password
POSTGRES_DB=conference_db
POSTGRES_PORT=5432
```

В `.env.example` указан порт `5432`. Если он свободен, его можно оставить. Если на компьютере уже работает локальный PostgreSQL, выберите свободный порт, например `5433`.

| Переменная | Назначение |
| `POSTGRES_USER` | Пользователь базы данных |
| `POSTGRES_PASSWORD` | Пароль пользователя базы данных |
| `POSTGRES_DB` | Имя базы данных |
| `POSTGRES_PORT` | Порт, через который база доступна на компьютере |

Docker Compose автоматически читает `.env`. Spring Boot читает этот же файл через `spring.config.import: "optional:file:.env[.properties]"` в `src/main/resources/application.yaml`. Поэтому запускайте приложение из корня проекта.

Не добавляйте `.env` с паролями в Git: он уже включён в `.gitignore`. Приведённые учётные данные предназначены только для локальной разработки.

### 3. Запустить базу данных

```powershell
docker compose up -d
```


Правило в `docker-compose.yml` имеет формат `порт компьютера:порт контейнера`:

```yaml
ports:
  - "${POSTGRES_PORT}:5432"
```

PostgreSQL внутри контейнера слушает `5432`, а приложение на компьютере подключается к `localhost:${POSTGRES_PORT}`.

### 4. Запустить приложение

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```



Приложение доступно по адресу `http://localhost:8080`.


`bootRun` работает до остановки приложения. Для остановки нажмите `Ctrl+C`; база в Docker продолжит работать.

## Полезные команды Docker

Команды используют имя сервиса `postgres` из `docker-compose.yml`.

| Команда | Назначение |
| --- | --- |
| `docker compose up -d` | Создать и запустить базу в фоне |
| `docker compose ps` | Проверить состояние и опубликованные порты |
| `docker compose logs --tail 100 postgres` | Показать последние 100 строк логов базы |
| `docker compose logs -f postgres` | Следить за логами; `Ctrl+C` завершает просмотр, но не останавливает базу |
| `docker compose stop` | Остановить контейнеры без удаления |
| `docker compose start` | Запустить ранее остановленные контейнеры |
| `docker compose restart postgres` | Перезапустить базу без применения изменений конфигурации контейнера |
| `docker compose up -d --force-recreate` | Пересоздать контейнер, например после изменения порта в `.env` |
| `docker compose down` | Удалить контейнеры и сеть, сохранив данные базы в volume |
| `docker compose pull postgres` | Скачать актуальный образ в рамках тега `postgres:17-alpine`; для применения затем выполнить `docker compose up -d` |
| `docker volume ls` | Посмотреть Docker volumes |
| `docker compose down -v` | Сбросить базу данных |


### Подключение к базе не через Docker

Для подключения из DBeaver, pgAdmin или IDE используйте хост `localhost`, порт из `POSTGRES_PORT`, имя базы, пользователя и пароль из `.env`.

### Полный сброс локальной базы

**Внимание: следующая команда удаляет volume проекта и все данные базы без возможности восстановления, если нет резервной копии.** Используйте её только для ненужной локальной базы. Сначала остановите приложение.

```powershell
docker compose down -v
docker compose up -d
```

После следующего запуска приложения Flyway создаст схему заново.


## Сборка и тесты

Перед тестами запустите базу и настройте `.env`: текущий тест загружает контекст Spring Boot с подключением к PostgreSQL. Используйте отдельную локальную базу, не рабочую базу с важными данными.

Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat clean bootRun
```

Linux / macOS:

```bash
bash ./gradlew test
bash ./gradlew build
bash ./gradlew clean bootRun
```

`test` запускает тесты, `build` собирает проект и запускает тесты, `clean bootRun` удаляет результаты предыдущей сборки и запускает приложение. Для обычного запуска достаточно `bootRun`.

Исполняемый JAR создаётся в `build/libs`. Его можно запустить из корня проекта:

```powershell
java -jar build/libs/conference-app-0.0.1-SNAPSHOT.jar
```

## Работа со схемой базы

- SQL-миграции находятся в `src/main/resources/db/migration`.
- Новые изменения добавляйте отдельными файлами, например `V2__add_application_fields.sql`.
- Не редактируйте миграции, которые уже успешно применены на общей базе: Flyway проверяет их контрольные суммы.
- Используйте синтаксис именно PostgreSQL, а не MySQL: `IF NOT EXISTS`, без обратных кавычек и `AUTO_INCREMENT`.