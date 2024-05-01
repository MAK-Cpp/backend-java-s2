package edu.java.bot.controller;

import edu.java.bot.AbstractBotTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Objects;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BotRateLimitTest extends AbstractBotTest {
    @MockBean
    private BotController botController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    private static final int CAPACITY = 5;
    private static final String CACHE_NAME = "test-cache";

    @DynamicPropertySource
    static void bucket4jProperties(DynamicPropertyRegistry registry) {
        registry.add("rate.limit.capacity", () -> String.valueOf(CAPACITY));
        registry.add("rate.limit.time", () -> "1");
        registry.add("rate.limit.unit", () -> "hours");
        registry.add("rate.bucket.cache.name", () -> CACHE_NAME);
        registry.add("rate.spring.cache.name", () -> CACHE_NAME);
    }

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME)).clear();
    }

    @AfterEach
    void tearDown() {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME)).clear();
    }

    public static Stream<Arguments> testRateLimit() {
        return Stream.of(
            Arguments.of(MockMvcRequestBuilders.post("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "id": 1,
                          "url": "https://google.com",
                          "description": "пакет из postman",
                          "chatsAndAliases": [
                            {
                              "id": 428027805,
                              "alias": "google"
                            }
                          ]
                        }"""))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testRateLimit(
        RequestBuilder request
    ) throws Exception {
        for (int i = 0; i < CAPACITY; i++) {
            mockMvc.perform(request).andExpect(status().isOk());
        }
        mockMvc.perform(request).andExpect(status().isTooManyRequests());
    }
}
