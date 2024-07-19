# Домашнее задание №5

### Общая информация

Типовое приложение представляет [трёхзвенную архитектуру](https://ru.wikipedia.org/wiki/%D0%A2%D1%80%D1%91%D1%85%D1%83%D1%80%D0%BE%D0%B2%D0%BD%D0%B5%D0%B2%D0%B0%D1%8F_%D0%B0%D1%80%D1%85%D0%B8%D1%82%D0%B5%D0%BA%D1%82%D1%83%D1%80%D0%B0):

- controller
- service
- domain

Первый слой controller мы уже реализовали в одном из предыдущих заданий. Также мы создали миграцию и научились применять её к БД.

Теперь нужно научиться загружать данные из БД (domain) и применять к ним некоторую логику обработки (service).



### Задача 1

Реализуйте слой domain для работы с БД при помощи JDBC API.

Нужно реализовать 3 операции для каждой из таблиц с сущностями: **add**, **remove**, **findAll**.

Требования:

- **dao/repository**, если нужно, должны возвращать специфичный **DTO**, а не **Row/RowSet**
- нужно использовать **JdbcTemplate или JdbcClient** (да, голый JDBC, **Hibernate** будет дальше)
- для транзакций нужно использовать **TransactionTemplate** или **@Transactional**

На получившиеся классы следует написать тесты на каждый метод при помощи **IntegrationEnvironment** из прошлого задания, например:
```java
@SpringBootTest
public class JdbcLinkTest extends IntegrationEnvironment {
    @Autowired
    private JdbcLinkDao linkRepository;
    @Autowired
    private JdbcTgChatRepository chatRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
   
    @Test
    @Transactional
    @Rollback
    void addTest() {
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
    }
}
```



### Задача 2

Реализуйте сервисы для добавления, удаления и получения данных из таблиц и добавьте соответствующие вызовы в controller'ы.

Убедитесь, что всё работает (добавление, удаление, получение) при помощи вызовов из **Swagger UI**.

Если возникнет сложность с дизайном интерфейсов, можете взять **следующий код** за основу:
```java
public interface LinkService {
       Link add(long tgChatId, URI url);
       Link remove(long tgChatId, URI url);
       Collection<Link> listAll(long tgChatId);
}

public interface TgChatService {
       void register(long tgChatId);
       void unregister(long tgChatId);
}

public interface LinkUpdater {
       int update();
}
```

**Требования:**

- Для сервисов, связанных с БД, используйте **интерфейсы**, а конкретную имплементацию именуемую префиксом **Jdbc***
- Например, вы создали интерфейс **interface LinkService { ... }**, тогда класс должен быть **class JdbcLinkService implements LinkService { ... }**
- В интерфейсах не должно быть JDBC-специфичных типов, например, **RowSet**
- **Jdbc***-имплементации классов положите в отдельный подпакет **jdbc**



### Задача 3

Текущее приложение умеет добавлять, удалять и показывать список ссылок, но ничего не делает для поиска и оповещения.

В одном из предыдущих заданий мы сделали простой планировщик, который раз в N секунд выводит запись в консоль.

Расширьте функционал планировщика:

- в БД ищется список ссылок, которые давно не проверялись
- при помощи **GithubClient**/**StackOverFlowClient** проверялись обновления устаревших ссылок
- если обновления есть, то вызывается **BotClient** и уведомление об обновлении уходит в приложение **bot**
- нас интересует только факт обновления, а не их характер, т.е. достаточно сказать "есть обновление"

**Важно:** планировщик должен использовать для работы интерфейсы, т.е. сущности без префикса **Jdbc***.



### Задача 4

Метод **findAll** позволяет загрузить все ссылки из БД, но на самом деле мы могли бы сделать фильтрацию (поиск ссылок, которые давно не проверялись) на стороне БД.

Добавьте метод для поиска ссылок по критерию.

В планировщике измените код таким образом, чтобы использовался новый метод.


### Задача 5

За каждый сценарий можно получить 5 баллов, но не более 10 баллов в сумме

Расширьте функционал системы таким образом, чтобы помимо факта обновления присылалась информация о том, что изменилось.

Можете выбрать несколько случаев, например:

* появился новый тикет
* добавили новый коммит/ветка
* в вопросе появился новый ответ
* в вопросе появился новый комментарий

Реализуйте один или несколько сценариев, делать все необязательно, также можно придумать свои сценарии.

Не забудьте отразить изменения в чат-боте.

Изменения потребуют небольших правок в контракте, а так же схеме хранения данных.



### Задание 6 (5 баллов)

JDBC достаточно простой API, при помощи которого мы программируем "на строках", т.к. никакого взаимодействия между SQL-запросами (строки) и Java-кодом нет.

Альтернатива SQL существует -- это построители запросов, такие как Criteria API.

Они гибче чем голые строки, но у них тоже есть недостаток -- типобезопасность.

Например, если в таблице create table mytable(id int) мы изменим тип поля id на другой, например, text, то ошибки компиляции в запросе
```java
jdbcTemplate.query(sql, (rs, rn) -> rs.getInt("id"), ...)
```
мы не получим. Ошибка произойдёт во время выполнения и будет заключаться, что код ожидает int, когда в БД уже давно String.

Чем больше кодовая база, тем чаще возникают такие проблемы, и любой рефакторинг превращается в боль, а если у вас нет тестов -- в кошмар.

Для решения этой проблемы придумали библиотеку JOOQ.

По схеме БД создаётся Java-код, который создаёт обвязки для генерации SQL.

Возьмем простой пример:
```sql
select id, url, last_check_time, created_by, created_at
from link
join subscription on link.id = link_id
where tg_chat_id = ?
```
Тогда такой запрос в случае JOOQ будет выглядеть как:
```java
dslContext.select(LINK.fields())
.from(LINK)
.join(SUBSCRIPTION).on(LINK.ID.eq(SUBSCRIPTION.LINK_ID))
.where(SUBSCRIPTION.TG_CHAT_ID.eq(tgChatId))
.fetchInto(Link.class);
```
Получается так, что если какой-нибудь из типов в схеме БД изменится, то код просто не скомпилируется и мы узнаем о проблеме сразу.

Другой плюс библиотеки -- достаточно умные автоматические мапперы (fetchInto).

Минус у библиотеки один -- это кодогенерация, т.е. после правки схемы БД вам нужно перегенерировать вспомогательный код.

Ваша задача написать JOOQ-реализации интерфейсов из прошлого задания, например, JooqLinkService.

Но перед тем как писать сервисы нам нужно сгенерировать вспомогательный код.

Для генерации кода стоит завести отдельный модуль scrapper-jooq.

Вам потребуются зависимости:
```yaml
org.springframework.boot:spring-boot-starter-jooq
org.jooq:jooq-codegen
org.jooq:jooq-meta-extensions-liquibase
org.liquibase:liquibase-core
```
В модуле напишите функцию-генератор при помощи [программного API](https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/).

Новый код нужно генерировать в модуле scrapper в пакете edu.java.scrapper.domain.jooq.

Если не получится разобраться с документацией -- ниже пример генератора.
```java
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Property;
import org.jooq.meta.jaxb.Target;

public class JooqCodegen {
    public static void main(String[] args) throws Exception {
        Database database = new Database()
            .withName("org.jooq.meta.extensions.liquibase.LiquibaseDatabase")
            .withProperties(
                new Property().withKey("rootPath").withValue("migrations"),
                new Property().withKey("scripts").withValue("master.xml")
            );

        Generate options = new Generate()
            .withGeneratedAnnotation(true)
            .withGeneratedAnnotationDate(false)
            .withNullableAnnotation(true)
            .withNullableAnnotationType("org.jetbrains.annotations.Nullable")
            .withNonnullAnnotation(true)
            .withNonnullAnnotationType("org.jetbrains.annotations.NotNull")
            .withJpaAnnotations(false)
            .withValidationAnnotations(true)
            .withSpringAnnotations(true)
            .withConstructorPropertiesAnnotation(true)
            .withConstructorPropertiesAnnotationOnPojos(true)
            .withConstructorPropertiesAnnotationOnRecords(true)
            .withFluentSetters(false)
            .withDaos(false)
            .withPojos(true);

        Target target = new Target()
            .withPackageName("ru.tinkoff.edu.java.scrapper.domain.jooq")
            .withDirectory("scrapper/src/main/java");

        Configuration configuration = new Configuration()
            .withGenerator(
                new Generator()
                    .withDatabase(database)
                    .withGenerate(options)
                    .withTarget(target)
            );

        GenerationTool.generate(configuration);
    }
}
```
