package edu.java.scrapper.service.jooq;

import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.LinkUpdaterTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.database-access-type=jooq")
@Transactional
@Rollback
public class JooqLinkUpdaterTest extends LinkUpdaterTest {
    @Autowired
    public JooqLinkUpdaterTest(LinkUpdater linkUpdater, LinkService linkService, ChatService chatService) {
        super(linkUpdater, linkService, chatService);
    }

    @Test
    void testUpdate() {
        fillDB();
        for (Long chatId : DATABASE_START_VALUES.keySet()) {
            for (LinkRecord linkRecord : DATABASE_START_VALUES.get(chatId)) {
                final Long linkId = linkService.getLink(chatId, linkRecord.alias()).getLink().getId();
                testUpdateFunction(linkId);
            }
        }
    }
}
