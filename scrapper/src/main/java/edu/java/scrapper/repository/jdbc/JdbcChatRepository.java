package edu.java.scrapper.repository.jdbc;

import edu.java.dto.response.ChatResponse;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcChatRepository extends JdbcTemplate {
    public static final RowMapper<ChatResponse> CHAT_DTO_ROW_MAPPER =
        (rs, rowNum) -> new ChatResponse(rs.getLong("chat_id"));

    @Autowired
    public JdbcChatRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Transactional
    public Long add(Long chatId) throws Exception {
        update("INSERT INTO chats (chat_id) VALUES (?)", chatId);
        return chatId;
    }

    @Transactional
    public void remove(Long chatId) {
        update("DELETE FROM chats WHERE chat_id = ?", chatId);
    }

    public boolean exists(Long chatId) {
        return !findAll(chatId).isEmpty();
    }

    public List<ChatResponse> findAll() {
        return query(
            "SELECT chat_id FROM chats",
            CHAT_DTO_ROW_MAPPER
        );
    }

    public List<ChatResponse> findAll(Long chatId) {
        return query(
            "SELECT chat_id FROM chats WHERE chat_id = ?",
            CHAT_DTO_ROW_MAPPER,
            chatId
        );
    }
}
