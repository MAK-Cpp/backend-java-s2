package edu.java.scrapper.service.jdbc;

import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkServiceTest;
import edu.java.scrapper.service.LinkUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.database-access-type=jdbc")
@ActiveProfiles("test")
@Transactional
@Rollback
public class JdbcLinkServiceTest extends LinkServiceTest {
    @Autowired
    public JdbcLinkServiceTest(LinkUpdater linkUpdater, LinkService linkService, ChatService chatService) {
        super(linkUpdater, linkService, chatService);
    }

    @Test
    public void testGetAllLinks() {
        testGetAllLinksFunction();
    }

    @ParameterizedTest
    @MethodSource("chatIdsStream")
    public void testGetAllLinksById(Long chatId) {
        testGetAllLinksFunction(chatId);
    }

    @ParameterizedTest
    @MethodSource("usersAndLinks")
    public void testGetLink(Long chatId, LinkRecord linkRecord) {
        testGetLinkFunction(chatId, linkRecord);
    }

    @ParameterizedTest
    @MethodSource("usersAndLinks")
    public void testAddLink(Long chatId, LinkRecord linkRecord) {
        testAddLinkFunction(chatId, linkRecord);
    }

    @ParameterizedTest
    @MethodSource("usersAndLinks")
    public void testRemoveLink(Long chatId, LinkRecord linkRecord) {
        testRemoveLinkFunction(chatId, linkRecord);
    }
}
