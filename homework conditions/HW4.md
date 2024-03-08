# Домашнее задание №4
### Задача 1

Придумайте схему базы данных, которая подойдёт для хранения информации для отслеживания ссылок.

**Ограничения:**

1. Схема БД должна находиться в каталоге **migrations**.
2. Для описания необходимо использовать язык **SQL**.

Пример файла миграции:
```sql
create table foo
(
    id              bigint generated always as identity,
    name            text                     not null,

    created_at      timestamp with time zone not null,
    created_by      text                     not null,

    primary key (id),
    unique (name)
)
```

_Подсказки:_

1. _Скорее всего вам потребуются сущности_ **_Ссылка и_** **_Чат_**_._
2. _Нужно как-то связать эти сущности между собой._



### Задача 2

- Создайте changelog-файл **migrations/master.xml**
- Добавьте необходимые мета-комментарии в **.sql**-файлы, которые вы создали до этого
- [Примеры из официальной документации Liquibase](https://www.liquibase.org/get-started/best-practices)

Пример **master.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<!--suppress XmlUnusedNamespaceDeclaration -->
<databaseChangeLog
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
      xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
      xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd
              http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">

    <include file="01-foo.sql"/>
    <include file="02-bar.sql"/>
    <include file="03-baz.sql"/>
</databaseChangeLog>
```


### Задача 3

Модифицируйте **compose.yaml** в корне проекта и добавьте запуск миграций. Для этого добавьте следующий сервис:
```yml
liquibase-migrations:
      image: liquibase/liquibase:4.25
      depends_on:
              - postgresql
      command:
              - --changelog-file=master.xml
              - --driver=org.postgresql.Driver
              - --url=jdbc:postgresql://postgresql:5432/scrapper
              - --username=...
              - --password=...
              - update
      volumes:
              - ./migrations:/liquibase/changelog
      networks:
              - backend
```

Выполните команду
```bash
docker compose up -d
```
и проверьте при помощи **DBeaver**/**IDEA**/**etc**, что подключение **jdbc:postgresql://localhost:5432/scrapper** работает, а все необходимые таблицы создаются.



### Задача 4

Использование **compose**-файла удобно для локальной разработки и тестирования, но не совсем подходит для интеграционных тестов.

Для таких тестов используется специальная библиотека **Testcontainers.**

В проекте уже реализован шаблон [singleton container](https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/) для **Postgres** в классе **IntegrationEnvironment**.

Расширьте функционал этого класса и добавьте запуск миграций, т.е. после старта контейнера **Postgres** должны запускаться миграции **Liquibase** из каталога **migrations**.

Для запуска миграций потребуется зависимость:
```xml
<dependency>
      <groupId>org.liquibase</groupId>
      <artifactId>liquibase-core</artifactId>
      <scope>test</scope>
</dependency>
```

Также потребуются зависимости JDBC, с которым мы познакомимся чуть позже:
```xml
<!-- Database -->
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
</dependency>
```

Пример программного (запуск из кода) **liquibase update** есть в [официальном блоге](https://www.liquibase.com/blog/3-ways-to-run-liquibase).

Подсказки:

- jdbcUrl, логин и пароль можно получить из **PostgreSQLContainer**
- Текущий каталог -- **new File(".")**, но работать с каталогами удобнее через **new File(".").toPath()**
- **Path** позволяет легко подняться на уровень выше (**parent**), для перемещения "вниз" можно использовать метод **resolve**

Проверьте, что контейнер успешно запускается написав простейший тест на базе **IntegrationEnvironment**.
