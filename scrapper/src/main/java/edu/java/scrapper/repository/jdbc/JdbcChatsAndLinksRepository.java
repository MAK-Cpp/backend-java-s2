package edu.java.scrapper.repository.jdbc;

import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkResponse;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class JdbcChatsAndLinksRepository extends JdbcTemplate {
    @SuppressWarnings("checkstyle:LineLength")
    private static final String FIND_ALL_LINKS_BY_CHAT_ID_SQL =
        "SELECT l.link_id, l.uri, l.last_update FROM chats_and_links cl JOIN links l ON cl.link_id = l.link_id WHERE cl.chat_id = ?";
    private static final String FIND_ALL_CHATS_BY_LINK_ID_SQL =
        "SELECT chat_id FROM chats_and_links WHERE link_id = ?";

    @Autowired
    public JdbcChatsAndLinksRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void add(Long chatId, Long linkId) {
        update("INSERT INTO chats_and_links (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING", chatId, linkId);
    }

    public void remove(Long chatId, Long linkId) {
        update("DELETE FROM chats_and_links WHERE (chat_id, link_id) = (?, ?)", chatId, linkId);
    }

    public void remove(Long chatId) {
        update("DELETE FROM chats_and_links WHERE chat_id = ?", chatId);
    }

    public List<LinkResponse> findAllLinks(Long chatId) {
        return query(FIND_ALL_LINKS_BY_CHAT_ID_SQL, JdbcLinkRepository.LINK_DTO_ROW_MAPPER, chatId);
    }

    public List<ChatResponse> findAllChats(Long linkId) {
        return query(FIND_ALL_CHATS_BY_LINK_ID_SQL, JdbcChatRepository.CHAT_DTO_ROW_MAPPER, linkId);
    }
}
