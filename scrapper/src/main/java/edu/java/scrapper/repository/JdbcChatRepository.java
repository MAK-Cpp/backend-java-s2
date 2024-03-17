package edu.java.scrapper.repository;

import edu.java.scrapper.dto.ChatDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcChatRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void add(Long chatId, String username) {
        jdbcTemplate.update("INSERT INTO chats (chat_id, username) VALUES (?, ?)", chatId, username);
    }

    @Transactional
    public void remove(Long chatId) {
        jdbcTemplate.update("DELETE FROM chats WHERE chat_id = ?", chatId);
    }

    public List<ChatDTO> findAll() {
        return jdbcTemplate.query(
            "SELECT chat_id, username FROM chats",
            (rs, rowNum) -> new ChatDTO(rs.getLong("chat_id"), rs.getString("username"))
        );
    }
}
