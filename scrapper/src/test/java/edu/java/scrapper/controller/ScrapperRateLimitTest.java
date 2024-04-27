package edu.java.scrapper.controller;

import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Objects;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.cache.type=jcache",
    "bucket4j.enabled=true"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ScrapperRateLimitTest extends IntegrationTest {
    @MockBean
    private ScrapperController scrapperController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    private static final int CAPACITY = 5;
    private static final String CACHE_NAME = "rate-limit-bucket";

    @DynamicPropertySource
    static void bucket4jProperties(DynamicPropertyRegistry registry) {
        registry.add("rate.limit.capacity", () -> String.valueOf(CAPACITY));
        registry.add("rate.limit.time", () -> "1");
        registry.add("rate.limit.unit", () -> "hours");
    }

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME)).clear();
    }

    public static Stream<Arguments> testRateLimit() {
        return Stream.of(
            Arguments.of(MockMvcRequestBuilders.get("/tg-chat/123")),
            Arguments.of(MockMvcRequestBuilders.post("/tg-chat/123")),
            Arguments.of(MockMvcRequestBuilders.delete("/tg-chat/123")),
            Arguments.of(MockMvcRequestBuilders
                .get("/link")
                .header("tgChatId", 1L)
                .header("alias", "alias")),
            Arguments.of(MockMvcRequestBuilders
                .get("/links")
                .header("tgChatId", 1L)),
            Arguments.of(MockMvcRequestBuilders
                .post("/links")
                .header("tgChatId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "link": "string",
                      "alias": "string"
                    }
                    """)),
            Arguments.of(MockMvcRequestBuilders
                .delete("/links")
                .header("tgChatId", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "alias": "string"
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
