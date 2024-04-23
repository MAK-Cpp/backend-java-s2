package edu.java.scrapper.configuration;

import edu.java.scrapper.repository.jdbc.JdbcChatRepository;
import edu.java.scrapper.repository.jdbc.JdbcChatsAndLinksRepository;
import edu.java.scrapper.repository.jdbc.JdbcLinkRepository;
import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.jdbc.JdbcChatService;
import edu.java.scrapper.service.jdbc.JdbcLinkService;
import edu.java.scrapper.service.jdbc.JdbcLinkUpdater;
import edu.java.scrapper.validator.LinkValidator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
public class JdbcAccessConfig {
    @Bean
    @Autowired
    public ChatService chatService(
        JdbcChatRepository chatRepository,
        JdbcChatsAndLinksRepository chatsAndLinksRepository
    ) {
        return new JdbcChatService(chatRepository, chatsAndLinksRepository);
    }

    @Bean
    @Autowired
    public LinkService linkService(
        JdbcChatRepository chatRepository,
        JdbcLinkRepository linkRepository,
        JdbcChatsAndLinksRepository chatsAndLinksRepository,
        List<LinkValidator> linkValidators
    ) {
        return new JdbcLinkService(chatRepository, linkRepository, chatsAndLinksRepository, linkValidators);
    }

    @Bean
    @Autowired
    public LinkUpdater linkUpdater(JdbcLinkRepository jdbcLinkRepository) {
        return new JdbcLinkUpdater(jdbcLinkRepository);
    }
}
