package edu.java.scrapper;

import edu.java.scrapper.dto.ChatDTO;
import edu.java.scrapper.repository.JdbcChatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JdbcChatTest extends IntegrationTest {
    private static final List<ChatDTO> CHATS = List.of(
        new ChatDTO(123L),
        new ChatDTO(1538L),
        new ChatDTO(226L)
    );
    @Autowired
    private JdbcChatRepository chatRepository;

    private void fillDB() {
        CHATS.forEach(chatDTO -> chatRepository.add(chatDTO.chatId()));
    }

    @Test
    @Transactional
    @Rollback
    void addTest() {
        fillDB();
        chatRepository.add(384L);
        final List<ChatDTO> result = new ArrayList<>(CHATS);
        result.add(new ChatDTO(384L));
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
