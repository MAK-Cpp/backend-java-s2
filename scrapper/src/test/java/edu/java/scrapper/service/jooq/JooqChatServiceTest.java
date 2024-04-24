package edu.java.scrapper.service.jooq;

import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.ChatServiceTest;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(properties = "app.database-access-type=jooq")
@Transactional
@Rollback
public class JooqChatServiceTest extends ChatServiceTest {
    @Autowired
    public JooqChatServiceTest(LinkUpdater linkUpdater, LinkService linkService, ChatService chatService) {
        super(linkUpdater, linkService, chatService);
    }

    @ParameterizedTest
    @MethodSource("chatIdsStream")
    public void testRegisterUser(Long chatId) {
        testRegisterUserFunction(chatId);
    }

    @ParameterizedTest
    @MethodSource("chatIdsStream")
    public void testDeleteUser(Long chatId) {
        testDeleteUserFunction(chatId);
    }

    @Test
    public void testGetAllChats() {
        testGetAllChatsFunction();
    }

    @Test
    public void testGetAllChatsByLinkId() {
        final Map<Long, String> links = new HashMap<>();
        fillDB().forEach(x -> links.put(x.getValue().getLink().getId(), x.getValue().getLink().getUri().toString()));
        for (var link : links.entrySet()) {
            testGetAllChatsFunction(link.getKey(), link.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("chatIdsStream")
    public void testGetChat(Long chatId) {
        testGetChatFunction(chatId);
    }

    @Test
    public void testGetLinkAlias() {
        testGetLinkAliasFunction();
    }
}
