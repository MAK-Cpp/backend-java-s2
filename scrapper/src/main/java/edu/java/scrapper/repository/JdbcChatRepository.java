package edu.java.scrapper.repository;

import edu.java.dto.response.ChatResponse;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class JdbcChatRepository extends JdbcTemplate {
    private static final RowMapper<ChatResponse> CHAT_DTO_ROW_MAPPER =
        (rs, rowNum) -> new ChatResponse(rs.getLong("chat_id"));

    @Autowired
    public JdbcChatRepository(DataSource dataSource) {
        super(dataSource);
    }

    public Long add(Long chatId) {
        update("INSERT INTO chats (chat_id) VALUES (?) ON CONFLICT DO NOTHING", chatId);
        return chatId;
    }

    public void remove(Long chatId) {
        update("DELETE FROM chats WHERE chat_id = ?", chatId);
    }

    public boolean exists(Long chatId) {
        return !query("SELECT chat_id FROM chats WHERE chat_id = ?", CHAT_DTO_ROW_MAPPER, chatId).isEmpty();
    }

    public List<ChatResponse> findAll() {
        return query(
            "SELECT chat_id FROM chats",
            CHAT_DTO_ROW_MAPPER
        );
    }
}
