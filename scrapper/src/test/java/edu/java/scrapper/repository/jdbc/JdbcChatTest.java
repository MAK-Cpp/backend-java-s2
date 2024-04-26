package edu.java.scrapper.repository.jdbc;

import edu.java.dto.response.ChatResponse;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"app.database-access-type=jdbc"})
@ActiveProfiles("test")
public class JdbcChatTest extends IntegrationTest {
    private static final List<ChatResponse> CHATS = List.of(
        new ChatResponse(123L),
        new ChatResponse(1538L),
        new ChatResponse(226L)
    );
    @Autowired
    private JdbcChatRepository chatRepository;

    private void fillDB() {
        CHATS.forEach(chatDTO -> {
            try {
                chatRepository.add(chatDTO.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Transactional
    @Rollback
    void addTest() throws Exception {
        fillDB();
        chatRepository.add(384L);
        final List<ChatResponse> result = new ArrayList<>(CHATS);
        result.add(new ChatResponse(384L));
        assertThat(chatRepository.findAll()).isEqualTo(result);
    }

    @Test
    @Transactional
    @Rollback
    void removeTest() {
        fillDB();
        chatRepository.remove(1538L);
        assertThat(chatRepository.findAll())
            .isEqualTo(List.of(CHATS.getFirst(), CHATS.getLast()));
    }
}
