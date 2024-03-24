package edu.java.scrapper;

import edu.java.scrapper.repository.JdbcChatRepository;
import edu.java.scrapper.repository.JdbcLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class JdbcLinkTest extends IntegrationTest {
    private JdbcLinkRepository linkRepository;
    private JdbcChatRepository chatRepository;

    @Autowired
    public JdbcLinkTest(JdbcLinkRepository linkRepository, JdbcChatRepository chatRepository) {
        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
    }

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
