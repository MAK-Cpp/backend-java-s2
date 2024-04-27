package edu.java.scrapper.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.configuration.HttpClientConfig;
import edu.java.scrapper.client.stackoverflow.StackOverflowClient;
import edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl;
import edu.java.scrapper.response.stackoverflow.AnswerResponse;
import edu.java.test.client.ClientTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl.FILTER;
import static edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl.ORDER;
import static edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl.SITE;
import static edu.java.scrapper.client.stackoverflow.StackOverflowClientImpl.SORT;
import static org.assertj.core.api.Assertions.assertThat;

class StackOverflowClientImplTest extends ClientTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = getPort();
    private static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final StackOverflowClient STACK_OVERFLOW_CLIENT
        = new StackOverflowClientImpl(WebClient.builder(), URL, RETRY);

    public static Stream<Arguments> testGetQuestionAnswers() {
        return Stream.of(
            Arguments.of(
                "65954571",
                ANSWERS_ON_65954571,
                new AnswerResponse(List.of(
                    new AnswerResponse.Answer(
                        76987303,
                        65954571,
                        "Assuming you are facing this issue in your local Apple M1/M2, and if you don&#39;t want to add any additional dependencies then you can use this below solution:\r\n\r\nStep 1)Download any IntelX64 based JDK distribution.\r\n\r\nSample download source:\r\nhttps://learn.microsoft.com/en-us/java/openjdk/download(In my case, I was using jdk11 hence I downloaded ```microsoft-jdk-11.0.20-macOS-x64.tar.gz``` from this page)\r\n\r\n\r\nStep 2)Set your java to this new jdk from your IDE(Intellij Idea for my case)\r\n\r\n\r\n\r\n\r\nThis worked like a charm for me",
                        OffsetDateTime.of(2023, 8, 27, 13, 18, 13, 0, ZoneOffset.UTC),
                        new AnswerResponse.AnswerOwner(
                            "https://stackoverflow.com/users/5160940/dockyard",
                            "DockYard",
                            989,
                            5160940
                        )
                    )
                ))
            ),
            Arguments.of(
                "218384",
                """
                    {
                        "items": [
                            {
                                "owner": {
                                    "account_id": 13598,
                                    "reputation": 102659,
                                    "user_id": 27439,
                                    "user_type": "registered",
                                    "accept_rate": 100,
                                    "profile_image": "https://i.stack.imgur.com/eMKDL.jpg?s=256&g=1",
                                    "display_name": "Vincent Ramdhanie",
                                    "link": "https://stackoverflow.com/users/27439/vincent-ramdhanie"
                                },
                                "is_accepted": true,
                                "community_owned_date": 1509494214,
                                "score": 4195,
                                "last_activity_date": 1662599267,
                                "last_edit_date": 1662599267,
                                "creation_date": 1224510864,
                                "answer_id": 218510,
                                "question_id": 218384,
                                "content_license": "CC BY-SA 4.0",
                                "body_markdown": "There are two overarching types of variables in Java: \\r\\n\\r\\n 1. *Primitives*: variables that contain data. If you want to manipulate the data in a primitive variable you can manipulate that variable directly. By convention primitive types start with a lowercase letter. For example variables of type `int` or `char` are primitives.\\r\\n\\r\\n 2. *References*: variables that contain the memory address of an `Object` i.e. variables that *refer* to an `Object`. If you want to manipulate the `Object` that a reference variable refers to you must *dereference* it. Dereferencing usually entails using `.` to access a method or field, or using `[` to index an array. By convention reference types are usually denoted with a type that starts in uppercase. For example variables of type `Object` are references.\\r\\n\\r\\nConsider the following code where you declare a variable of *primitive* type `int` and don&#39;t initialize it:\\r\\n\\r\\n```java\\r\\nint x;\\r\\nint y = x + x;\\r\\n```\\r\\n\\r\\nThese two lines will crash the program because no value is specified for `x` and we are trying to use `x`&#39;s value to specify `y`. All primitives have to be initialized to a usable value before they are manipulated. \\r\\n\\r\\nNow here is where things get interesting. *Reference* variables can be set to `null` which means &quot;**I am referencing *nothing***&quot;. You can get a `null` value in a reference variable if you explicitly set it that way, or a reference variable is uninitialized and the compiler does not catch it (Java will automatically set the variable to `null`).   \\r\\n\\r\\nIf a reference variable is set to null either explicitly by you or through Java automatically, and you attempt to *dereference* it you get a `NullPointerException`.\\r\\n\\r\\nThe `NullPointerException` (NPE) typically occurs when you declare a variable but did not create an object and assign it to the variable before trying to use the contents of the variable. So you have a reference to something that does not actually exist.\\r\\n\\r\\nTake the following code:\\r\\n```\\r\\nInteger num;\\r\\nnum = new Integer(10);\\r\\n```\\r\\n\\r\\nThe first line declares a variable named `num`, but it does not actually contain a reference value yet. Since you have not yet said what to point to, Java sets it to `null`.\\r\\n\\r\\nIn the second line, the `new` keyword is used to instantiate (or create) an object of type `Integer`, and the reference variable `num` is assigned to that `Integer` object.\\r\\n\\r\\nIf you attempt to dereference `num` *before* creating the object you get a `NullPointerException`. In the most trivial cases, the compiler will catch the problem and let you know that &quot;`num may not have been initialized`,&quot; but sometimes you may write code that does not directly create the object.\\r\\n\\r\\nFor instance, you may have a method as follows:\\r\\n\\r\\n    public void doSomething(SomeObject obj) {\\r\\n       // Do something to obj, assumes obj is not null\\r\\n       obj.myMethod();\\r\\n    }\\r\\n\\r\\nIn which case, you are not creating the object `obj`, but rather assuming that it was created before the `doSomething()` method was called. Note, it is possible to call the method like this:\\r\\n\\r\\n    doSomething(null);\\r\\n\\r\\nIn which case, `obj` is `null`, and the statement `obj.myMethod()` will throw a `NullPointerException`.\\r\\n\\r\\nIf the method is intended to do something to the passed-in object as the above method does, it is appropriate to throw the `NullPointerException` because it&#39;s a programmer error and the programmer will need that information for debugging purposes.\\r\\n\\r\\nIn addition to `NullPointerException`s thrown as a result of the method&#39;s logic, you can also check the method arguments for `null` values and throw NPEs explicitly by adding something like the following near the beginning of a method:\\r\\n\\r\\n    // Throws an NPE with a custom error message if obj is null\\r\\n    Objects.requireNonNull(obj, &quot;obj must not be null&quot;);\\r\\n\\r\\nNote that it&#39;s helpful to say in your error message clearly *which* object cannot be `null`. The advantage of validating this is that 1) you can return your own clearer error messages and 2) for the rest of the method you know that unless `obj` is reassigned, it is not null and can be dereferenced safely.\\r\\n\\r\\nAlternatively, there may be cases where the purpose of the method is not solely to operate on the passed in object, and therefore a null parameter may be acceptable. In this case, you would need to check for a **null parameter** and behave differently. You should also explain this in the documentation. For example, `doSomething()` could be written as:\\r\\n\\r\\n    /**\\r\\n      * @param obj An optional foo for ____. May be null, in which case\\r\\n      *  the result will be ____.\\r\\n      */\\r\\n    public void doSomething(SomeObject obj) {\\r\\n        if(obj == null) {\\r\\n           // Do something\\r\\n        } else {\\r\\n           // Do something else\\r\\n        }\\r\\n    }\\r\\n\\r\\nFinally, [How to pinpoint the exception &amp; cause using Stack Trace](https://stackoverflow.com/q/3988788/2775450)\\r\\n\\r\\n&gt; What methods/tools can be used to determine the cause so that you stop\\r\\n&gt; the exception from causing the program to terminate prematurely?\\r\\n\\r\\nSonar with find bugs can detect NPE.\\r\\nhttps://stackoverflow.com/questions/20899931/can-sonar-catch-null-pointer-exceptions-caused-by-jvm-dynamically\\r\\n\\r\\nNow Java 14 has added a new language feature to show the root cause of NullPointerException. This language feature has been part of SAP commercial JVM since 2006.\\r\\n\\r\\nIn Java 14, the following is a sample NullPointerException Exception message:\\r\\n\\r\\n&gt;  in thread &quot;main&quot; java.lang.NullPointerException: Cannot invoke &quot;java.util.List.size()&quot; because &quot;list&quot; is null\\r\\n\\r\\n### List of situations that cause a `NullPointerException` to occur\\r\\n\\r\\nHere are all the situations in which a `NullPointerException` occurs, that are directly* mentioned by the Java Language Specification:\\r\\n\\r\\n- Accessing (i.e. getting or setting) an *instance* field of a null reference. (static fields don&#39;t count!)\\r\\n- Calling an *instance* method of a null reference. (static methods don&#39;t count!)\\r\\n- `throw null;`\\r\\n- Accessing elements of a null array.\\r\\n- Synchronising on null - `synchronized (someNullReference) { ... }`\\r\\n- Any integer/floating point operator can throw a `NullPointerException` if one of its operands is a boxed null reference\\r\\n- An unboxing conversion throws a `NullPointerException` if the boxed value is null.\\r\\n- Calling `super` on a null reference throws a `NullPointerException`. If you are confused, this is talking about qualified superclass constructor invocations:\\r\\n\\r\\n```\\r\\nclass Outer {\\r\\n    class Inner {}\\r\\n}\\r\\nclass ChildOfInner extends Outer.Inner {\\r\\n    ChildOfInner(Outer o) { \\r\\n        o.super(); // if o is null, NPE gets thrown\\r\\n    }\\r\\n}\\r\\n```\\r\\n\\r\\n- Using a `for (element : iterable)` loop to loop through a null collection/array.\\r\\n\\r\\n- `switch (foo) { ... }` (whether its an expression or statement) can throw a `NullPointerException` when `foo` is null.\\r\\n\\r\\n- `foo.new SomeInnerClass()` throws a `NullPointerException` when `foo` is null.\\r\\n\\r\\n- Method references of the form `name1::name2` or `primaryExpression::name` throws a `NullPointerException` when evaluated when `name1` or `primaryExpression` evaluates to null.\\r\\n\\r\\n    a note from the JLS here says that, `someInstance.someStaticMethod()` doesn&#39;t throw an NPE, because `someStaticMethod` is static, but `someInstance::someStaticMethod` still throw an NPE!\\r\\n\\r\\n&lt;sub&gt;* Note that the JLS probably also says a lot about NPEs *indirectly*.&lt;/sub&gt;\\r\\n\\r\\n  [1]: https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/util/Objects.html#requireNonNull(T,java.lang.String)\\r\\n\\r\\n"
                            },
                            {
                                "owner": {
                                    "account_id": 970,
                                    "reputation": 400968,
                                    "user_id": 1288,
                                    "user_type": "registered",
                                    "accept_rate": 93,
                                    "profile_image": "https://www.gravatar.com/avatar/fc763c6ff6c160ddad05741e87e517b6?s=256&d=identicon&r=PG",
                                    "display_name": "Bill the Lizard",
                                    "link": "https://stackoverflow.com/users/1288/bill-the-lizard"
                                },
                                "is_accepted": false,
                                "community_owned_date": 1509494214,
                                "score": 967,
                                "last_activity_date": 1619358583,
                                "last_edit_date": 1619358583,
                                "creation_date": 1224508844,
                                "answer_id": 218390,
                                "question_id": 218384,
                                "content_license": "CC BY-SA 4.0",
                                "body_markdown": "`NullPointerException`s are exceptions that occur when you try to use a reference that points to no location in memory (null) as though it were referencing an object.  Calling a method on a null reference or trying to access a field of a null reference will trigger a `NullPointerException`.  These are the most common, but other ways are listed on the [`NullPointerException`][1] javadoc page.\\r\\n\\r\\nProbably the quickest example code I could come up with to illustrate a `NullPointerException` would be:\\r\\n\\r\\n    public class Example {\\r\\n\\r\\n        public static void main(String[] args) {\\r\\n            Object obj = null;\\r\\n            obj.hashCode();\\r\\n        }\\r\\n\\r\\n    }\\r\\n\\r\\nOn the first line inside `main`, I&#39;m explicitly setting the `Object` reference `obj` equal to `null`.  This means I have a reference, but it isn&#39;t pointing to any object.  After that, I try to treat the reference as though it points to an object by calling a method on it.  This results in a `NullPointerException` because there is no code to execute in the location that the reference is pointing.\\r\\n\\r\\n(This is a technicality, but I think it bears mentioning: A reference that points to null isn&#39;t the same as a C pointer that points to an invalid memory location.  A null pointer is literally not pointing *anywhere*, which is subtly different than pointing to a location that happens to be invalid.)\\r\\n\\r\\n  [1]: http://docs.oracle.com/javase/7/docs/api/java/lang/NullPointerException.html"
                            }
                        ],
                        "has_more": false,
                        "quota_max": 300,
                        "quota_remaining": 239
                    }
                    """,
                new AnswerResponse(List.of(
                    new AnswerResponse.Answer(
                        218510,
                        218384,
                        "There are two overarching types of variables in Java: \r\n\r\n 1. *Primitives*: variables that contain data. If you want to manipulate the data in a primitive variable you can manipulate that variable directly. By convention primitive types start with a lowercase letter. For example variables of type `int` or `char` are primitives.\r\n\r\n 2. *References*: variables that contain the memory address of an `Object` i.e. variables that *refer* to an `Object`. If you want to manipulate the `Object` that a reference variable refers to you must *dereference* it. Dereferencing usually entails using `.` to access a method or field, or using `[` to index an array. By convention reference types are usually denoted with a type that starts in uppercase. For example variables of type `Object` are references.\r\n\r\nConsider the following code where you declare a variable of *primitive* type `int` and don&#39;t initialize it:\r\n\r\n```java\r\nint x;\r\nint y = x + x;\r\n```\r\n\r\nThese two lines will crash the program because no value is specified for `x` and we are trying to use `x`&#39;s value to specify `y`. All primitives have to be initialized to a usable value before they are manipulated. \r\n\r\nNow here is where things get interesting. *Reference* variables can be set to `null` which means &quot;**I am referencing *nothing***&quot;. You can get a `null` value in a reference variable if you explicitly set it that way, or a reference variable is uninitialized and the compiler does not catch it (Java will automatically set the variable to `null`).   \r\n\r\nIf a reference variable is set to null either explicitly by you or through Java automatically, and you attempt to *dereference* it you get a `NullPointerException`.\r\n\r\nThe `NullPointerException` (NPE) typically occurs when you declare a variable but did not create an object and assign it to the variable before trying to use the contents of the variable. So you have a reference to something that does not actually exist.\r\n\r\nTake the following code:\r\n```\r\nInteger num;\r\nnum = new Integer(10);\r\n```\r\n\r\nThe first line declares a variable named `num`, but it does not actually contain a reference value yet. Since you have not yet said what to point to, Java sets it to `null`.\r\n\r\nIn the second line, the `new` keyword is used to instantiate (or create) an object of type `Integer`, and the reference variable `num` is assigned to that `Integer` object.\r\n\r\nIf you attempt to dereference `num` *before* creating the object you get a `NullPointerException`. In the most trivial cases, the compiler will catch the problem and let you know that &quot;`num may not have been initialized`,&quot; but sometimes you may write code that does not directly create the object.\r\n\r\nFor instance, you may have a method as follows:\r\n\r\n    public void doSomething(SomeObject obj) {\r\n       // Do something to obj, assumes obj is not null\r\n       obj.myMethod();\r\n    }\r\n\r\nIn which case, you are not creating the object `obj`, but rather assuming that it was created before the `doSomething()` method was called. Note, it is possible to call the method like this:\r\n\r\n    doSomething(null);\r\n\r\nIn which case, `obj` is `null`, and the statement `obj.myMethod()` will throw a `NullPointerException`.\r\n\r\nIf the method is intended to do something to the passed-in object as the above method does, it is appropriate to throw the `NullPointerException` because it&#39;s a programmer error and the programmer will need that information for debugging purposes.\r\n\r\nIn addition to `NullPointerException`s thrown as a result of the method&#39;s logic, you can also check the method arguments for `null` values and throw NPEs explicitly by adding something like the following near the beginning of a method:\r\n\r\n    // Throws an NPE with a custom error message if obj is null\r\n    Objects.requireNonNull(obj, &quot;obj must not be null&quot;);\r\n\r\nNote that it&#39;s helpful to say in your error message clearly *which* object cannot be `null`. The advantage of validating this is that 1) you can return your own clearer error messages and 2) for the rest of the method you know that unless `obj` is reassigned, it is not null and can be dereferenced safely.\r\n\r\nAlternatively, there may be cases where the purpose of the method is not solely to operate on the passed in object, and therefore a null parameter may be acceptable. In this case, you would need to check for a **null parameter** and behave differently. You should also explain this in the documentation. For example, `doSomething()` could be written as:\r\n\r\n    /**\r\n      * @param obj An optional foo for ____. May be null, in which case\r\n      *  the result will be ____.\r\n      */\r\n    public void doSomething(SomeObject obj) {\r\n        if(obj == null) {\r\n           // Do something\r\n        } else {\r\n           // Do something else\r\n        }\r\n    }\r\n\r\nFinally, [How to pinpoint the exception &amp; cause using Stack Trace](https://stackoverflow.com/q/3988788/2775450)\r\n\r\n&gt; What methods/tools can be used to determine the cause so that you stop\r\n&gt; the exception from causing the program to terminate prematurely?\r\n\r\nSonar with find bugs can detect NPE.\r\nhttps://stackoverflow.com/questions/20899931/can-sonar-catch-null-pointer-exceptions-caused-by-jvm-dynamically\r\n\r\nNow Java 14 has added a new language feature to show the root cause of NullPointerException. This language feature has been part of SAP commercial JVM since 2006.\r\n\r\nIn Java 14, the following is a sample NullPointerException Exception message:\r\n\r\n&gt;  in thread &quot;main&quot; java.lang.NullPointerException: Cannot invoke &quot;java.util.List.size()&quot; because &quot;list&quot; is null\r\n\r\n### List of situations that cause a `NullPointerException` to occur\r\n\r\nHere are all the situations in which a `NullPointerException` occurs, that are directly* mentioned by the Java Language Specification:\r\n\r\n- Accessing (i.e. getting or setting) an *instance* field of a null reference. (static fields don&#39;t count!)\r\n- Calling an *instance* method of a null reference. (static methods don&#39;t count!)\r\n- `throw null;`\r\n- Accessing elements of a null array.\r\n- Synchronising on null - `synchronized (someNullReference) { ... }`\r\n- Any integer/floating point operator can throw a `NullPointerException` if one of its operands is a boxed null reference\r\n- An unboxing conversion throws a `NullPointerException` if the boxed value is null.\r\n- Calling `super` on a null reference throws a `NullPointerException`. If you are confused, this is talking about qualified superclass constructor invocations:\r\n\r\n```\r\nclass Outer {\r\n    class Inner {}\r\n}\r\nclass ChildOfInner extends Outer.Inner {\r\n    ChildOfInner(Outer o) { \r\n        o.super(); // if o is null, NPE gets thrown\r\n    }\r\n}\r\n```\r\n\r\n- Using a `for (element : iterable)` loop to loop through a null collection/array.\r\n\r\n- `switch (foo) { ... }` (whether its an expression or statement) can throw a `NullPointerException` when `foo` is null.\r\n\r\n- `foo.new SomeInnerClass()` throws a `NullPointerException` when `foo` is null.\r\n\r\n- Method references of the form `name1::name2` or `primaryExpression::name` throws a `NullPointerException` when evaluated when `name1` or `primaryExpression` evaluates to null.\r\n\r\n    a note from the JLS here says that, `someInstance.someStaticMethod()` doesn&#39;t throw an NPE, because `someStaticMethod` is static, but `someInstance::someStaticMethod` still throw an NPE!\r\n\r\n&lt;sub&gt;* Note that the JLS probably also says a lot about NPEs *indirectly*.&lt;/sub&gt;\r\n\r\n  [1]: https://docs.oracle.com/en/java/javase/15/docs/api/java.base/java/util/Objects.html#requireNonNull(T,java.lang.String)\r\n\r\n",
                        Instant.ofEpochSecond(1224510864L).atOffset(ZoneOffset.UTC),
                        new AnswerResponse.AnswerOwner(
                            "https://stackoverflow.com/users/27439/vincent-ramdhanie",
                            "Vincent Ramdhanie",
                            102659,
                            27439
                        )
                    ),
                    new AnswerResponse.Answer(
                        218390,
                        218384,
                        "`NullPointerException`s are exceptions that occur when you try to use a reference that points to no location in memory (null) as though it were referencing an object.  Calling a method on a null reference or trying to access a field of a null reference will trigger a `NullPointerException`.  These are the most common, but other ways are listed on the [`NullPointerException`][1] javadoc page.\r\n\r\nProbably the quickest example code I could come up with to illustrate a `NullPointerException` would be:\r\n\r\n    public class Example {\r\n\r\n        public static void main(String[] args) {\r\n            Object obj = null;\r\n            obj.hashCode();\r\n        }\r\n\r\n    }\r\n\r\nOn the first line inside `main`, I&#39;m explicitly setting the `Object` reference `obj` equal to `null`.  This means I have a reference, but it isn&#39;t pointing to any object.  After that, I try to treat the reference as though it points to an object by calling a method on it.  This results in a `NullPointerException` because there is no code to execute in the location that the reference is pointing.\r\n\r\n(This is a technicality, but I think it bears mentioning: A reference that points to null isn&#39;t the same as a C pointer that points to an invalid memory location.  A null pointer is literally not pointing *anywhere*, which is subtly different than pointing to a location that happens to be invalid.)\r\n\r\n  [1]: http://docs.oracle.com/javase/7/docs/api/java/lang/NullPointerException.html",
                        Instant.ofEpochSecond(1224508844L).atOffset(ZoneOffset.UTC),
                        new AnswerResponse.AnswerOwner(
                            "https://stackoverflow.com/users/1288/bill-the-lizard",
                            "Bill the Lizard",
                            400968,
                            1288
                        )
                    )
                ))
            )
        );
    }

    @BeforeEach
    public void beforeEach() {
        wireMockServer = new WireMockServer(HTTP_ENDPOINT_PORT);
        wireMockServer.start();
        configureFor("localhost", HTTP_ENDPOINT_PORT);
    }

    @ParameterizedTest
    @MethodSource
    public void testGetQuestionAnswers(String questionId, String body, AnswerResponse result) {
        MappingBuilder builder = get(
            "/questions/" + questionId
                + "/answers?filter=" + FILTER
                + "&order=" + ORDER
                + "&site=" + SITE
                + "&sort=" + SORT
        );
        stubFor(
            builder
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
        AnswerResponse output = STACK_OVERFLOW_CLIENT.getQuestionAnswers(questionId);
        assertThat(output).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetQuestionAnswers(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final String questionId = "65954571";
        final String body = ANSWERS_ON_65954571;
        final StackOverflowClient stackOverflowClient =
            new StackOverflowClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/questions/" + questionId + "/answers?filter=" + FILTER + "&order=" + ORDER + "&site=" + SITE + "&sort=" + SORT),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> stackOverflowClient.getQuestionAnswers(questionId),
            failStatus
        );
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }

    public static final String ANSWERS_ON_65954571 = """
        {
            "items": [
                {
                    "owner": {
                        "account_id": 6691602,
                        "reputation": 989,
                        "user_id": 5160940,
                        "user_type": "registered",
                        "profile_image": "https://i.stack.imgur.com/4w35p.jpg?s=256&g=1",
                        "display_name": "DockYard",
                        "link": "https://stackoverflow.com/users/5160940/dockyard"
                    },
                    "is_accepted": false,
                    "score": 1,
                    "last_activity_date": 1693142293,
                    "creation_date": 1693142293,
                    "answer_id": 76987303,
                    "question_id": 65954571,
                    "content_license": "CC BY-SA 4.0",
                    "body_markdown": "Assuming you are facing this issue in your local Apple M1/M2, and if you don&#39;t want to add any additional dependencies then you can use this below solution:\\r\\n\\r\\nStep 1)Download any IntelX64 based JDK distribution.\\r\\n\\r\\nSample download source:\\r\\nhttps://learn.microsoft.com/en-us/java/openjdk/download(In my case, I was using jdk11 hence I downloaded ```microsoft-jdk-11.0.20-macOS-x64.tar.gz``` from this page)\\r\\n\\r\\n\\r\\nStep 2)Set your java to this new jdk from your IDE(Intellij Idea for my case)\\r\\n\\r\\n\\r\\n\\r\\n\\r\\nThis worked like a charm for me"
                }
            ],
            "has_more": false,
            "quota_max": 300,
            "quota_remaining": 241
        }
        """;
}
